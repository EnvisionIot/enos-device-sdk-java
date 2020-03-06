package com.envisioniot.enos.iot_mqtt_sdk.core.internals;

import com.envisioniot.enos.iot_mqtt_sdk.core.ConnCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.ExecutorFactory;
import com.envisioniot.enos.iot_mqtt_sdk.core.IConnectCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.IResponseCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionError;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.*;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.BaseProfile;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultActivateResponseHandler;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DeviceCredential;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.FileProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.activate.DeviceActivateInfoCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutRequest;
import com.envisioniot.enos.iot_mqtt_sdk.util.SecureModeUtil;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class MqttConnection {

    enum State {
        /**
         * This is the init state and can only transfer to state CONNECTING / CONNECTED.
         */
        NOT_CONNECTED,

        /**
         * This is the temp state used by {@link MqttConnection#connect(ConnCallback)}.
         */
        CONNECTING,

        /**
         * This is the state after we call {@link MqttConnection#connect()} or
         * {@link MqttConnection#connect(ConnCallback)} from NOT_CONNECTED.
         * And it can transfer to CONNECTED again by {@link MqttConnection#reconnect()}
         * or to DISCONNECTED by {@link MqttConnection#disconnect()}
         * or to CLOSED by {@link MqttConnection#close()}
         */
        CONNECTED,

        DISCONNECTING,

        /**
         * This is the state after we call disconnect successfully from CONNECTED.
         * And it can transfer to connect by {@link MqttConnection#reconnect()}
         * and to CLOSED by {@link MqttConnection#close()}
         */
        DISCONNECTED,

        /**
         * Close is in the progress
         */
        CLOSING,

        /**
         * Final state after we release all underlying resources. And user should
         * NEVER the connection any more at this state.
         */
        CLOSED
    }

    private final AtomicLong requestId = new AtomicLong(0);
    private final SubTopicCache subTopicCache = new SubTopicCache();

    private final BaseProfile profile;
    private final MessageBuffer buffer;
    private final ExecutorFactory executorFactory;

    private final DefaultProcessor mqttProcessor;

    // This could be updated because of profile change
    private volatile MqttClient transport;

    private volatile State state = State.NOT_CONNECTED;

    private final Set<DeviceCredential> loginedSubDevices = Sets.newConcurrentHashSet();

    public MqttConnection(BaseProfile profile, ExecutorFactory executorFactory) {
        this(profile, new MessageBuffer(), executorFactory);
    }

    private MqttConnection(BaseProfile profile, MessageBuffer buffer, ExecutorFactory executorFactory) {
            this.profile = profile;
            this.buffer = buffer;
            this.executorFactory = executorFactory;

            this.mqttProcessor = new DefaultProcessor(this);
    }

    State getState() {
        return state;
    }

    public BaseProfile getProfile() {
        return this.profile;
    }

    public ExecutorFactory getExecutorFactory() {
        return this.executorFactory;
    }

    /**
     * Re-connect to mqtt broker. This can only be called at state CONNECTED or DISCONNECTED.
     *
     * @throws EnvisionException
     */
    public synchronized void reconnect() throws EnvisionException {
        if (!isReconnectAllowed()) {
            throw new IllegalStateException("reconnect is not allowed at state: " + state);
        }

        if (state == State.CONNECTING) {
            log.info("connection is ongoing");
            return;
        }

        state = State.CONNECTING;

        // Close current underlying transport firstly
        closeUnderlyingTransport();

        try {
            doConnect();
            state = State.CONNECTED;
        } catch (EnvisionException error){
            state = State.DISCONNECTED;
            throw error;
        }
    }

    boolean isReconnectAllowed() {
        return state == State.CONNECTED || state == State.DISCONNECTED;
    }

    public void notifyConnectSuccess() {
        executorFactory.getPublishExecutor().execute(buffer.createRepublishDisconnetedMessageTask(this));

        if (profile.isAutoLoginSubDevice()) {
            // immutableCopy is necessary here as we don't want the union to be a changeable view.
            Set<DeviceCredential> currSubDevices = Sets.union(loginedSubDevices, profile.getSubDevices()).immutableCopy();
            if (!currSubDevices.isEmpty()) {
                executorFactory.getPublishExecutor().execute(() -> autoLoginSubDevices(currSubDevices));
            }
        }
    }

    private void autoLoginSubDevices(Set<DeviceCredential> subDevices) {
        subDevices.forEach(dev -> {
            try {
                SubDeviceLoginRequest request = SubDeviceLoginRequest.builder().setSubDeviceInfo(dev).build();
                if (request.getSecureMode().getModeId() == SecureModeUtil.VIA_DEVICE_SECRET) {
                    SubDeviceLoginResponse rsp = new Publisher<>(request).execute();
                    if (rsp.isSuccess()) {
                        log.info("auto login sub-device {} successfully", dev);
                    } else {
                        log.error("failed to auto login sub-device {} , rsp {} ", dev, rsp);
                    }
                } else {
                    log.error("don't support auto login sub-device using mode = {} for {}",
                            request.getSecureMode(), dev);
                }
            } catch (Exception e) {
                log.error("failed to login sub-device: " + dev, e);
            }
        });
    }

    /**
     * Connect to broker. This method blocks until it connects successfully or fails.
     *
     * @throws EnvisionException if error happens
     */
    public synchronized void connect() throws EnvisionException {
        if (state != State.NOT_CONNECTED) {
            throw new IllegalStateException("connect is not allowed at state: " + state);
        }

        state = State.CONNECTING;
        doConnect();
        // Mark the state as CONNECTED if no exception is thrown
        state = State.CONNECTED;
    }

    @Deprecated
    public synchronized void connect(IConnectCallback callback) {
        if (state != State.NOT_CONNECTED) {
            // We can't use EnvisionException here as we don't want to mark this method throwing exception
            throw new IllegalStateException("connect is not allowed at state: " + state);
        }

        if (callback == null) {
            throw new IllegalArgumentException("callback should not be null");
        }
        this.mqttProcessor.setConnectCallback(callback);
        doConnectAsync();
    }

    public synchronized void connect(ConnCallback callback) {
        if (state != State.NOT_CONNECTED) {
            // We can't use EnvisionException here as we don't want to mark this method throwing exception
            throw new IllegalStateException("connect is not allowed at state: " + state);
        }

        if (callback == null) {
            throw new IllegalArgumentException("callback should not be null");
        }

        state = State.CONNECTING;

        this.mqttProcessor.setConnCallback(callback);
        doConnectAsync();
    }

    private void doConnectAsync() {
        executorFactory.getConnectExecutor().execute(() -> {
            try {
                doConnect();
                state = State.CONNECTED;
            } catch (EnvisionException e) {
                state = State.NOT_CONNECTED;

                // callback would be invoked in doConnect when error happens.
                // So we don't call the callback here again.
            }
        });
    }

    private void doConnect() throws EnvisionException {
        try {
            initializeUnderlyingTransport();

            MqttConnectOptions connectOptions = profile.createConnectOptions();

            /**
             * bug workaround if auto reconnect is requested. For more details, please view
             * https://www.tapd.cn/20716331/bugtrace/bugs/view?bug_id=1120716331001011106.
             */
            if (connectOptions.isAutomaticReconnect()) {
                mqttProcessor.setManageAutoConnect(true);
                connectOptions.setAutomaticReconnect(false);
            } else {
                // clear previous state
                mqttProcessor.setManageAutoConnect(false);
            }

            // This method blocks since it waits for the connect completion
            this.transport.connect(connectOptions);

            registerDeviceActivateInfoCommand();
        } catch (Throwable e) {
            // No need to log the exception stack here as we would return the exception to client
            log.error("failed to connect to {}, error: {}", profile.getServerUrl(), Utils.getRootMessage(e));

            // Release potential initialized resources
            closeUnderlyingTransport();

            String action = mqttProcessor.isOnceConnected() ? "re-connect" : "connect";
            String errorMsg = "failed to " + action + " to " + profile.getServerUrl();

            EnvisionException error = new EnvisionException(errorMsg, e, EnvisionError.MQTT_CLIENT_CONNECT_FAILED);

            // invoke connect related callbacks
            this.mqttProcessor.onConnectFailed(error);

            throw error;
        }
    }

    public synchronized void disconnect() {
        if (state == State.DISCONNECTED || state == State.DISCONNECTING) {
            log.warn("connection is already disconnected");
            return;
        }

        if (state != State.CONNECTED) {
            throw new IllegalStateException("disconnect is not allowed at state: " + state);
        }

        state = State.DISCONNECTING;
        disconnectUnderlyingTransport();
        state = State.DISCONNECTED;
    }

    public String getClientId() {
        return profile.getClientId();
    }

    public synchronized void close() {
        if (state == State.CLOSED || state == State.CLOSING) {
            return;
        }

        // This step is important
        state = State.CLOSING;

        closeUnderlyingTransport();

        // Shutdown the underlying thread pools
        executorFactory.shutdownExecutorServices();

        state = State.CLOSED;
    }

    private void registerDeviceActivateInfoCommand() {
        if (getProfile().getSecureMode().getModeId() == SecureModeUtil.VIA_PRODUCT_SECRET) {
            //register dynamic activated response handler
            if (getProfile() instanceof FileProfile) {
                setArrivedMsgHandler(DeviceActivateInfoCommand.class, new DefaultActivateResponseHandler(this));
            } else {
                log.warn("mqtt client dynamic activate device, please handle the reply message [{}]",
                        DeviceActivateInfoCommand.class.getSimpleName());
            }
        }
    }

    private void initializeUnderlyingTransport() throws EnvisionException {
        try {
            if (transport != null) {
                log.error("[BUG] underlying transport is already initialized.");
            }

            transport = new MqttClient(profile.getServerUrl(), getClientId(), new MemoryPersistence());
            transport.setCallback(mqttProcessor);
            transport.setTimeToWait(profile.getTimeToWait() * 1000);
        } catch (MqttException e) {
            log.error("failed to create MqttClient", e);
            throw new EnvisionException(e, EnvisionError.INIT_MQTT_CLIENT_FAILED);
        }
    }

    private void disconnectUnderlyingTransport() {
        // Always clean the subscribing cache if transport is to be disconnected
        cleanSubscribeTopicCache();

        try {
            if (transport != null) {
                // Normally we should only call disconnect if the underlying transport
                // is connecting or connected. However, we are unable to check if it's
                // in connecting state. Here we call the disconnect forcibly.
                transport.disconnectForcibly(1000, 3000);
            }
        } catch (MqttException e) {
            // ignore this confusing error
        }
    }

    /**
     * Disconnect with the underlying transport and close it to release resources.
     */
    private void closeUnderlyingTransport() {
        disconnectUnderlyingTransport();

        try {
            if (transport != null) {
                transport.close();
            }
        } catch (MqttException e) {
            // ignore the confusing close exception
        }

        transport = null;
    }

    private DefaultProcessor getProcessor() {
        return mqttProcessor;
    }

    public void fastPublish(IMqttDeliveryMessage request) throws EnvisionException {
        if (state != State.CONNECTED) {
            // We can't use EnvisionException here as we don't want to mark this method throwing exception
            throw new IllegalStateException("fastPublish is not allowed at state: " + state);
        }

        // If we use fast publish, it means that we don't want the reply
        if (request instanceof IAnswerable) {
            String topic = ((IAnswerable) request).getAnswerTopic();
            if (subTopicCache.exists(topic)) {
                unsubscribe(topic);
            }
        }

        new Deliverer<>(request).execute();
    }

    void unsubscribe(String topic) {
        try {
            transport.unsubscribe(topic);
            subTopicCache.remove(topic);
        } catch (Exception e) {
            // normally this should not happen
            log.error("failed to unsubscribe topic {}", topic, e);
        }
    }

    /**
     * This method blocks until the response is received or action timeout is reached (the default
     * operation timeout is defined in profile {@link BaseProfile#setTimeToWait}).
     */
    public <T extends IMqttResponse> T publish(IMqttRequest<T> request) throws EnvisionException {
        if (state != State.CONNECTED) {
            throw new IllegalStateException("publish is not allowed at state: " + state);
        }

        return new Publisher<>(request).execute();
    }

    /**
     * This method doesn't block. So user should check if request is successfully set through the callback.
     * If callback is null, we make use of the same logic as fastPublish.
     *
     * @param request  sent to broker in async way
     * @param callback if it's null, we would fallback to fastPublish method (legacy mode)
     * @param <T>      response type
     */
    public <T extends IMqttResponse> void publish(IMqttRequest<T> request, final IResponseCallback<T> callback) {
        try {
            if (state != State.CONNECTED) {
                throw new IllegalStateException("publish is not allowed at state: " + state);
            }

            if (callback != null) {
                new AsyncPublisher<>(request, callback).execute();
            } else {
                // legacy mode (should we throw if callback is null ?)
                fastPublish(request);
            }
        } catch (EnvisionException e) {
            if (callback != null) {
                // This should NOT happen normally
                log.error("unexpected exception thrown from async publish", e);

                // Note that callback would be called in AsyncPublisher and we
                // should never call the callback multiple times.
            }
            throw new RuntimeException(e);
        }
    }

    /**
     * set the msg handler for specific arrived msg
     */
    public <T extends IMqttArrivedMessage, D extends IMqttDeliveryMessage> void setArrivedMsgHandler(
            Class<T> arrivedMsgCls, IMessageHandler<T, D> handler) {
        getProcessor().setArrivedMsgHandler(arrivedMsgCls, handler);
    }

    public boolean isConnected() {
        if (state == State.CONNECTED) {
            // Also check the underlying connection is really connected
            return transport != null && transport.isConnected();
        }
        return false;
    }

    void cleanSubscribeTopicCache() {
        this.subTopicCache.clean();
    }

    boolean isTopicSubscribed(String topic) {
        return subTopicCache.exists(topic);
    }

    /**
     * This class by default only delivers message to broker without waiting for
     * response. However, it still blocks until message is sent out.
     */
    class Deliverer<T extends IMqttDeliveryMessage, M extends IMqttArrivedMessage> {

        protected final T delivered;

        public Deliverer(T delivered) {
            this.delivered = delivered;
        }

        /**
         * Add necessary internal handling logic before we do the execute. This includes:
         * a) fill necessary information not provided by the request
         * b) check if the request is valid
         * c) file is not allowed in MQTT
         */
        protected void preExecute() throws EnvisionException {
            fillRequest();
            delivered.check();
            if ((delivered instanceof BaseMqttRequest) && 
                (((BaseMqttRequest<?>) delivered).getFiles() != null) &&
                (!((BaseMqttRequest<?>) delivered).getFiles().isEmpty()))
            {
                throw new EnvisionException("file publishing is not supported yet in MQTT");
            }
        }

        protected void fillRequest() {
            if (StringUtil.isEmpty(delivered.getMessageId())) {
                delivered.setMessageId(String.valueOf(requestId.incrementAndGet()));
            }

            // Also populate request version for IMqttRequest
            if (delivered instanceof IMqttRequest) {
                IMqttRequest<?> request = (IMqttRequest<?>) delivered;
                if (StringUtil.isEmpty(request.getVersion())) {
                    request.setVersion(BaseProfile.VERSION);
                }
            }

            if (StringUtil.isEmpty(delivered.getProductKey()) && StringUtil.isEmpty(delivered.getDeviceKey())) {
                delivered.setProductKey(profile.getProductKey());
                delivered.setDeviceKey(profile.getDeviceKey());
            }
        }

        final public M execute() throws EnvisionException {
            preExecute();
            M arrived = doExecute();
            postExecute(arrived);
            return arrived;
        }

        /**
         * @return null as Deliver by default doesn't wait for response
         * @throws EnvisionException
         */
        protected M doExecute() throws EnvisionException {
            doFastPublish();
            return null;
        }

        /**
         * Deliver request to broker. This method blocks until request is sent out and mqtt ack is
         * returned (if qos is not 0). However, this method doesn't wait for broker response.
         * <br/> <br/>
         * If broker response is wanted, relevant subscription must be set up before calling this
         * method. Otherwise, no response would be returned (see {@link Publisher).
         * <br/>
         *
         * @throws EnvisionException if the request fails to be delivered
         */
        protected void doFastPublish() throws EnvisionException {
            try {
                if (transport.isConnected()) {
                    if (delivered.getQos() == 1) {
                        transport.publish(delivered.getMessageTopic(), delivered.encode(), delivered.getQos(), false);
                    }
                    /**
                     * issue: https://github.com/eclipse/paho.mqtt.java/issues/421
                     */
                    else if (delivered.getQos() == 0) {
                        synchronized (transport) {
                            transport.publish(delivered.getMessageTopic(), delivered.encode(), delivered.getQos(), false);
                        }
                    } else {
                        throw new EnvisionException(EnvisionError.QOS_2_NOT_ALLOWED);
                    }
                } else {
                    buffer.putDisconnectedMessage(delivered);
                }

            } catch (MqttException e) {
                log.error("publish message failed messageRequestId {} ", delivered.getMessageTopic());
                throw new EnvisionException(e.getMessage(), e, EnvisionError.MQTT_CLIENT_PUBLISH_FAILED);
            }
        }

        protected void postExecute(M response) {
            // We only consider auto login sub-devices when auto reconnect is enabled.
            if (profile.isAutoReconnect() && profile.isAutoLoginSubDevice()) {
                if (delivered instanceof SubDeviceLoginRequest) {
                    // If delivered is of SubDeviceLoginRequest, its response MUST be null or of IMqttResponse.
                    if ((response == null || ((IMqttResponse) response).isSuccess())) {
                        loginedSubDevices.add(((SubDeviceLoginRequest) delivered).getCredential());
                    }
                } else if (delivered instanceof SubDeviceLogoutRequest) {
                    SubDeviceLogoutRequest req = (SubDeviceLogoutRequest) delivered;
                    loginedSubDevices.removeIf(dev ->
                            Objects.equals(dev.getProductKey(), req.getSubProductKey())
                                    && Objects.equals(dev.getDeviceKey(), req.getSubDeviceKey())
                    );
                }
            }
        }

    }

    /**
     * Publish message to broker and wait for response in sync way (blocking).
     */
    class Publisher<M extends IMqttResponse, T extends IMqttRequest<M>> extends Deliverer<T, M> {

        protected MqttResponseToken<M> token = null;

        public Publisher(T request) {
            super(request);
        }

        @Override
        protected void preExecute() throws EnvisionException {
            super.preExecute();

            subscribeResponseIfNeeded();

            // Set the token after preExecute
            token = getToken();

            getProcessor().registerResponseToken(token.getResponseId(), token);
        }

        /**
         * When we send a request to broker, if we expect a response returned, we need to
         * subscribe relevant topic. The response is different from mqtt ack message which
         * is controlled by qos (we don't receive ack when qos is 0). And response is more
         * business oriented and includes more detailed info about the status of request
         * processing (error code, message, ect.). <br/>
         * <br/>
         * For example, when we send MeasurepointPostRequest (whose topic is /sys/%s/%s/thing/measurepoint/post),
         * we have to subscribe /sys/%s/%s/thing/measurepoint/post_reply topic if we want to
         * receive MeasurepointPostResponse. Otherwise, broker thinks that we are not interested
         * in the response and won't send it to us. <br/>
         *
         * @throws EnvisionException
         */
        private void subscribeResponseIfNeeded() throws EnvisionException {
            try {
                String topic = delivered.getAnswerTopic();
                if (!subTopicCache.exists(topic)) {
                    // it blocks until mqtt ack comes back
                    transport.subscribe(topic, delivered.getQos());
                    subTopicCache.put(topic);
                }
            } catch (MqttException e) {
                throw new EnvisionException(e, EnvisionError.MQTT_CLIENT_SUBSCRIEBE_FAILED);
            }
        }

        @Override
        protected M doExecute() throws EnvisionException {
            try {
                super.doExecute();
                Objects.requireNonNull(token, "response token not set");
                return token.waitForResponse(transport.getTimeToWait());
            } catch (EnvisionException e) {
                if (Objects.nonNull(token)) {
                    // Here we only do the de-register in exceptional case (DefaultProcessor would
                    // help us do the de-register for other case when response is received).
                    getProcessor().deregisterResponseToken(token.getResponseId());
                    token.markFailure(e);
                }
                throw e;
            }
        }

        /**
         * getToken method is used to for response token initialization. We don't allow
         * token to be initialized multiple times. Use the token field directly rather
         * than call this method after initialization.
         */
        protected MqttResponseToken<M> getToken() {
            if (Objects.nonNull(token)) {
                throw new RuntimeException("token is already initialized");
            }
            return new MqttResponseToken<>(getAnswerTopicId());
        }

        protected String getAnswerTopicId() {
            Objects.requireNonNull(delivered.getMessageId(), "request message id not set");
            return delivered.getAnswerTopic() + "_" + delivered.getMessageId();
        }
    }

    /**
     * Publish message to broker and wait for response in async way (non-blocking).
     */
    class AsyncPublisher<M extends IMqttResponse, T extends IMqttRequest<M>> extends Publisher<M, T> {
        private IResponseCallback<M> callback;

        public AsyncPublisher(T request, IResponseCallback<M> callback) {
            super(request);
            this.callback = callback;
        }

        @Override
        protected M doExecute() {
            executorFactory.getPublishExecutor().execute(() -> {
                try {
                    super.doFastPublish();
                } catch (EnvisionException e) {
                    if (Objects.nonNull(token)) {
                        // Here we only do the de-register in exceptional case (DefaultProcessor would
                        // help us do the de-register for other case when response is received).
                        getProcessor().deregisterResponseToken(token.getResponseId());

                        // This would call our fallback and cancel the timeout task for us
                        token.markFailure(e);
                    } else {
                        callback.onFailure(e);
                    }
                }
            });

            // Nothing would be returned from async publish
            return null;
        }

        /**
         * Here we need to wrap original the callback to support timeout feature.
         */
        protected MqttResponseToken<M> getToken() {
            String answerTopicId = getAnswerTopicId();

            IResponseCallback<M> wrapped = callback;
            if (Objects.nonNull(wrapped)) {
                final ScheduledFuture<?> future = executorFactory.getTimeoutScheduler().schedule(
                        () -> {
                            log.error("callback task timeout {}", answerTopicId);
                            getProcessor().deregisterResponseToken(answerTopicId);
                            callback.onFailure(new TimeoutException("callback task timeout " + answerTopicId));
                        },
                        transport.getTimeToWait(),
                        TimeUnit.MILLISECONDS);

                wrapped = new IResponseCallback<M>() {
                    @Override
                    public void onResponse(M response) {
                        callback.onResponse(response);
                        future.cancel(true);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                        future.cancel(true);
                    }
                };
            }

            return new MqttResponseToken<>(answerTopicId, wrapped);
        }
    }
}
