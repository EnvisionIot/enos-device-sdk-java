package com.envisioniot.enos.iot_mqtt_sdk.core.internals;

import com.envisioniot.enos.iot_mqtt_sdk.core.ConnCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.IConnectCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMessageHandler;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage.DecodeResult;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttDeliveryMessage;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.ResponseCode;
import com.google.common.base.Preconditions;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

/**
 * stateless processor of mqtt messages
 *
 * @author zhensheng.cai
 * @date 2018/7/5.
 */
@SuppressWarnings("deprecation")
public class DefaultProcessor implements MqttCallback, MqttCallbackExtended {
    private static Logger logger = LoggerFactory.getLogger(DefaultProcessor.class);
    private static final int RECONN_INIT_DELAY_MILLIS = 8000;

    private final MqttConnection connection;

    /**
     * move the states to the independent modules
     */
    private Map<String, MqttResponseToken<? extends IMqttResponse>> rspTokenMap = new ConcurrentHashMap<>();
    private Map<Class<? extends IMqttArrivedMessage>, IMessageHandler<?, ?>> arrivedMsgHandlerMap = new ConcurrentHashMap<>();

    /**
     * User defined connect callback
     */
    private volatile IConnectCallback legacyCallback = null;
    private volatile ConnCallback connCallback = null;

    private volatile boolean onceConnected = false;

    // Indicates if we should manage the auto connect ourselves rather than
    // use auto reconnect feature in paho library.
    private volatile boolean manageAutoConnect = false;

    private volatile Timer reconnectTimer; // Automatic reconnect timer
    private volatile int reconnectDelay = RECONN_INIT_DELAY_MILLIS; // Reconnect delay, starts at 8s

    public DefaultProcessor(MqttConnection connection) {
        this.connection = connection;
    }

    public void setManageAutoConnect(boolean manageAutoConnect) {
        this.manageAutoConnect = manageAutoConnect;
    }

    public void onConnectFailed(Throwable error) {
        final int reasonCode = (error instanceof MqttException)
                ? ((MqttException) error).getReasonCode()
                : -1;

        if (connCallback == null) {
            logger.error("Client <{}> connect failed", this.connection.getClientId(), error);
        } else {
            /**
             * If user has defined the callback that takes the exception, we don't
             * log out the exception stack here.
             */
            logger.error("Client <{}> connect failed, error: {}",
                    connection.getClientId(), Utils.getRootMessage(error));
        }

        // We call legacy callback on each conn failure
        if (legacyCallback != null) {
            connection.getExecutorFactory().getCallbackExecutor().execute(() -> {
                legacyCallback.onConnectFailed(reasonCode);
            });
        }

        // We call new callback only on initial conn failure
        if (!onceConnected && connCallback != null) {
            connection.getExecutorFactory().getCallbackExecutor().execute(() -> {
                connCallback.connectFailed(error);
            });
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("{} , {}", topic, mqttMessage);
            }

            DecodeResult result = null;
            List<IMqttArrivedMessage> decoderList = DecoderRegistry.getDecoderList();
            for (IMqttArrivedMessage decoder : decoderList) {
                result = decoder.decode(topic, mqttMessage.getPayload());
                if (result != null) {
                    break;
                }
            }

            // 1. decoder
            if (result == null) {
                logger.error("decode the rcv message failed , from topic {}", topic);
                return;
            }

            IMqttArrivedMessage msg = result.getArrivedMsg();
            if (msg == null) {
                logger.error("decode msg failed , from topic{} ", topic);
                return;
            }


            // 2. handle the msg
            if (msg instanceof IMqttResponse) {
                IMqttResponse mqttRsp = (IMqttResponse) msg;
                MqttResponseToken<IMqttResponse> token = deregisterResponseToken(topic + "_" + mqttRsp.getMessageId());
                if (token == null) {
                    if (connection.isTopicSubscribed(topic)) {
                        logger.error("no request answers the response (it could be caused by too long delay), topic {}, msg {}", topic, msg);
                    } else {
                        logger.error("we don't subscribe topic {}, but received its response {}", topic, msg);
                        // do the un-subscribe as we don't do the subscription at all
                        connection.unsubscribe(topic);
                    }
                    return;
                }
                token.markSuccess(mqttRsp);
            }

            @SuppressWarnings("unchecked") final IMessageHandler<IMqttArrivedMessage, IMqttDeliveryMessage> handler = (IMessageHandler<IMqttArrivedMessage, IMqttDeliveryMessage>) arrivedMsgHandlerMap.get(msg.getClass());
            final List<String> pathList = result.getPathList();


            if (handler != null) {
                connection.getExecutorFactory().getPublishExecutor().execute(() -> {
                    try {
                        IMqttDeliveryMessage deliveryMsg = handler.onMessage(msg, pathList);
                        replyIfNeeded(msg, pathList, deliveryMsg);
                    } catch (Exception e) {
                        logger.error("handle the arrived msg err , may because of registered arrived msg callback ,", e);
                        try {
                            BaseMqttReply reply = buildMqttReply((BaseMqttCommand<?>) msg, pathList,
                                    ResponseCode.COMMAND_HANDLER_EXECUTION_FAILED,
                                    String.format("command handler execution failed, %s", e.getMessage()));
                            connection.fastPublish(reply);
                        } catch (Exception ex) {
                            logger.error("UGLY INTERNAL ERR ! send the err reply failed ", ex);
                        }
                    }
                });
            } else if (msg instanceof BaseMqttCommand) {
                handleCommandWithNoHandler((BaseMqttCommand<?>) msg, pathList);
            }
        } catch (Exception e) {
            logger.error("UGLY INTERNAL ERR!! , processing the arrived  msg err , topic {}  uncaught exception : ",
                    topic, e);
        }
    }


    private void handleCommandWithNoHandler(BaseMqttCommand<?> msg, List<String> pathList) {
        connection.getExecutorFactory().getPublishExecutor().execute(() -> {
            try {
                BaseMqttReply reply = buildMqttReply(msg, pathList,
                        ResponseCode.COMMAND_HANDLER_NOT_REGISTERED,
                        "downstream command handler not registered");
                connection.fastPublish(reply);
            } catch (Exception e) {
                logger.error("handle the msg  {} with no handler failed ,  ", msg, e);
            }
        });
    }

    private void replyIfNeeded(IMqttArrivedMessage msg, List<String> pathList, IMqttDeliveryMessage deliveryMsg) {
        if (deliveryMsg != null) {
            deliveryMsg.setMessageId(msg.getMessageId());
            deliveryMsg.setProductKey(msg.getProductKey());
            deliveryMsg.setDeviceKey(msg.getDeviceKey());
            /*set the reply topic*/
            if (deliveryMsg instanceof BaseMqttReply) {
                ((BaseMqttReply) deliveryMsg).setTopicArgs(pathList);
                /*user code is below 2000 and not equal to 200  */
                if (((BaseMqttReply) deliveryMsg).getCode() < ResponseCode.USER_DEFINED_ERR_CODE &&
                        ((BaseMqttReply) deliveryMsg).getCode() != ResponseCode.SUCCESS) {
                    logger.warn("errCode of reply message is not allowed , " + ((BaseMqttReply) deliveryMsg).getCode());
                }
                try {
                    connection.fastPublish(deliveryMsg);
                } catch (Exception e) {
                    logger.error(
                            "mqtt client publish reply msg to cloud failed ,arrived msg {}  msg to send {} ,  ",
                            msg, deliveryMsg, e);
                }
            }
        }
    }

    private BaseMqttReply buildMqttReply(BaseMqttCommand<?> msg, List<String> pathList, int code, String message) throws IllegalAccessException, InstantiationException {
        BaseMqttReply reply = (BaseMqttReply) msg.getAnswerType().newInstance();
        reply.setMessageId(msg.getMessageId());
        reply.setProductKey(msg.getProductKey());
        reply.setDeviceKey(msg.getDeviceKey());
        reply.setCode(code);
        reply.setMessage(message);
        reply.setTopicArgs(pathList);
        return reply;
    }

    <T extends IMqttResponse> void registerResponseToken(String tokenKey, MqttResponseToken<T> token) {
        rspTokenMap.put(tokenKey, token);
    }

    @SuppressWarnings("unchecked")
    MqttResponseToken<IMqttResponse> deregisterResponseToken(String tokenKey) {
        return (MqttResponseToken<IMqttResponse>) rspTokenMap.remove(tokenKey);
    }

    public void setArrivedMsgHandler(Class<? extends IMqttArrivedMessage> arrivedMsgCls, IMessageHandler<?, ?> handler) {
        arrivedMsgHandlerMap.put(arrivedMsgCls, handler);
    }

    @Deprecated
    public void setConnectCallback(IConnectCallback callback) {
        legacyCallback = callback;
    }

    @Deprecated
    public IConnectCallback getConnectCallback() {
        return legacyCallback;
    }

    public void setConnCallback(ConnCallback callback) {
        this.connCallback = callback;
    }

    @SuppressWarnings("unlikely-arg-type")
    public void removeArrivedMsgHandler(String topic) {
        arrivedMsgHandlerMap.remove(topic);
    }



    @Override
    public void connectionLost(Throwable throwable) {
        // We always clean the cache for this event
        this.connection.cleanSubscribeTopicCache();

        if (connection.getState() != MqttConnection.State.CONNECTED ) {
            // This lost MUST come from close, disconnect or reconnect operation
            // from the user. Since user himself choose to break the connection,
            // there is no need to feed the connect lost event to them.
            logger.info("ignored connection lost for state {}", connection.getState());
            return;
        }

        if (reconnectTimer != null) {
            // If we are still in progress of handling previous lost connection, there is
            // no need to handle the new one.
            logger.info("ignored connection lost as re-connection is in progress");
            return;
        }

        if (connCallback == null) {
            logger.error("Client <{}> Connection Lost", this.connection.getClientId(), throwable);
        }

        if (legacyCallback != null) {
            connection.getExecutorFactory().getCallbackExecutor().execute(() -> {
                legacyCallback.onConnectLost();
            });
        }

        if (connCallback != null) {
            connection.getExecutorFactory().getCallbackExecutor().execute(() -> {
                connCallback.connectLost(throwable);
            });
        }

        if (manageAutoConnect && connection.isReconnectAllowed()) {
            startReconnectTimer();
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        if (logger.isDebugEnabled()) {
            logger.debug("delivery complete");
        }
    }

    boolean isOnceConnected() {
        return onceConnected;
    }

    /**
     * As we manage the auto reconnect ourselves, we can't reply on the first argument.
     */
    @Override
    public void connectComplete(boolean ignored /* don't rely on this */, String serverURI) {
        final boolean reconnect = onceConnected;

        if (!onceConnected) {
            onceConnected = true;
        }

        this.connection.notifyConnectSuccess();
        if (legacyCallback != null) {
            connection.getExecutorFactory().getCallbackExecutor().execute(() -> {
                legacyCallback.onConnectSuccess();
            });
        }

        if (connCallback != null) {
            connection.getExecutorFactory().getCallbackExecutor().execute(() -> {
                connCallback.connectComplete(reconnect);
            });
        }
    }

    /**
     * Here we have to ensure that we only start one timer that
     * does the schedule of reconnection。
     */
    private synchronized void startReconnectTimer() {
        if (reconnectTimer != null) {
            // re-connection is in progress
            return;
        }
        logger.info("start reconnect timer in {}ms", reconnectDelay);
        reconnectTimer = new Timer("MQTT Reconnect: " + connection.getClientId());
        reconnectTimer.schedule(new ReconnectTask(), reconnectDelay);
    }

    private void rescheduleReconnectTimer() {
        Preconditions.checkNotNull(reconnectTimer, "[bug] reconnect timer not initialized");

        if (reconnectDelay < 128000) {
            reconnectDelay = reconnectDelay * 2;
        }

        logger.info("reschedule reconnect timer in {}ms", reconnectDelay);
        reconnectTimer.schedule(new ReconnectTask(), reconnectDelay);
    }

    private void stopReconnectTimer() {
        logger.info("stop reconnect timer now");
        reconnectDelay = RECONN_INIT_DELAY_MILLIS;

        if (reconnectTimer != null) {
            reconnectTimer.cancel();
        }
        reconnectTimer = null;
    }

    private class ReconnectTask extends TimerTask {
        public void run() {
            if (connection.isConnected()) {
                stopReconnectTimer();
                return;
            }

            try {
                connection.reconnect();
            } catch (Exception e) {
                logger.warn("failed to reconnect to broker now, error: {}. Would retry later.", e.getMessage());
            }

            if (connection.isConnected()) {
                logger.info("successfully reconnected to broker");
                stopReconnectTimer();
            } else {
                rescheduleReconnectTimer();
            }
        }
    }
}
