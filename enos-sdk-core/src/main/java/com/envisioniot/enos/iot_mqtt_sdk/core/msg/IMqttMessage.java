package com.envisioniot.enos.iot_mqtt_sdk.core.msg;

/**
 * Top-level interface that describes the mqtt message transferred between
 * mqtt broker and mqtt client. In terms of message flow, we have: <br/>
 * a) message {@link IMqttArrivedMessage} from broker to client  <br/>
 * a) message {@link IMqttDeliveryMessage} from client to broker  <br/>
 */
public interface IMqttMessage {
    String getMessageId();

    void setMessageId(String msgId);

    String getMessageTopic();

    void setMessageTopic(String topic);

    String getProductKey();

    void setProductKey(String productKey);

    String getDeviceKey();

    void setDeviceKey(String deviceKey);
}
