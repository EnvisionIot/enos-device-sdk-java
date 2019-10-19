package com.envisioniot.enos.iot_mqtt_sdk.message;

import java.io.Serializable;

public abstract class BaseMessageBody implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Maximum number of bytes allowed for the message payload
     */
    public static final int MAX_MESSAGE_SIZE = 512 * 1024;  // 512KB

    /**
     * @return serialized byte stream of the message body
     */
    public abstract byte[] encode();

    public int getMessageSize() {
        return encode().length;
    }

    public boolean isMessageTooLarge() {
        return getMessageSize() >= MAX_MESSAGE_SIZE;
    }
}
