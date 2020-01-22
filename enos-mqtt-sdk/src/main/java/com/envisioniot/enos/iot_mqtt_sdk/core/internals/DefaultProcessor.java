package com.envisioniot.enos.iot_mqtt_sdk.core.internals;

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
public class DefaultProcessor implements MqttCallback, MqttCallbackExtended {
    private static Logger logger = LoggerFactory.getLogger(DefaultProcessor.class);

    private final MqttConnection connection;

    /**
     * move the states to the independent modules
     */
    private Map<String, MqttResponseToken<? extends IMqttResponse>> rspTokenMap = new ConcurrentHashMap<>();
    private Map<Class<? extends IMqttArrivedMessage>, IMessageHandler<?, ?>> arrivedMsgHandlerMap = new ConcurrentHashMap<>();

    /**
     * User defined connect callback
     */
    private volatile IConnectCallback connectCallback = null;

    // Indicates if we should manage the auto connect ourselves rather than
    // use auto reconnect feature in paho library.
    private boolean manageAutoConnect = false;

    private Timer reconnectTimer; // Automatic reconnect timer
    private int reconnectDelay = 8000; // Reconnect delay, starts at 8s

    public DefaultProcessor(MqttConnection connection) {
        this.connection = connection;
    }

    public void setManageAutoConnect(boolean manageAutoConnect) {
        this.manageAutoConnect = manageAutoConnect;
    }

    public void onConnectFailed(int reasonCode) {
        if (reasonCode == MqttException.REASON_CODE_NOT_AUTHORIZED) {
            logger.error("Not authorized mqtt connect request, please refer to EnOS portal connective service log ");
        }
        if (connectCallback != null) {
            connection.getExecutorFactory().getCallbackExecutor().execute(() -> {
                connectCallback.onConnectFailed(reasonCode);
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
                    logger.error("no request answer the response, topic {} , msg {}", topic, msg);
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
                            BaseMqttReply reply = buildMqttReply((BaseMqttCommand) msg, pathList,
                                    ResponseCode.COMMAND_HANDLER_EXECUTION_FAILED,
                                    String.format("command handler execution failed, %s", e.getMessage()));
                            connection.fastPublish(reply);
                        } catch (Exception ex) {
                            logger.error("UGLY INTERNAL ERR ! send the err reply failed ", ex);
                        }
                    }
                });
            } else if (msg instanceof BaseMqttCommand) {
                handleCommandWithNoHandler((BaseMqttCommand) msg, pathList);
            }
        } catch (Exception e) {
            logger.error("UGLY INTERNAL ERR!! , processing the arrived  msg err , topic {}  uncaught exception : ",
                    topic, e);
        }
    }


    private void handleCommandWithNoHandler(BaseMqttCommand msg, List<String> pathList) {
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

    private BaseMqttReply buildMqttReply(BaseMqttCommand msg, List<String> pathList, int code, String message) throws IllegalAccessException, InstantiationException {
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

    public void setConnectCallback(IConnectCallback callback) {
        connectCallback = callback;
    }

    public IConnectCallback getConnectCallback() {
        return connectCallback;
    }

    public void removeArrivedMsgHandler(String topic) {
        arrivedMsgHandlerMap.remove(topic);
    }

    @Override
    public void connectionLost(Throwable throwable) {
        logger.error("Client <{}> Connection Lost", this.connection.getClientId(), throwable);

        logger.info("clear the subscriptions");
        //Clear the cache anyway
        this.connection.cleanSubscribeTopicCache();

        if (connectCallback != null) {
            connection.getExecutorFactory().getCallbackExecutor().execute(() -> {
                connectCallback.onConnectLost();
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

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (logger.isDebugEnabled()) {
            logger.debug("connect complete , reconnect {} , serverUri {} ", reconnect, serverURI);
        }

        this.connection.notifyConnectSuccess();
        if (connectCallback != null) {
            connection.getExecutorFactory().getCallbackExecutor().execute(() -> {
                connectCallback.onConnectSuccess();
            });
        }

    }

    private void startReconnectTimer() {
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
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer = null;
        }
        reconnectDelay = 1000;
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
                logger.error("failed to reconnect to broker now, error: {}. Would retry later.", e.getMessage());
            }

            if (connection.isConnected()) {
                logger.info("successfully reconnected to broker");
                stopReconnectTimer();
            }

            if (!connection.isConnected()) {
                rescheduleReconnectTimer();
            }
        }
    }
}
