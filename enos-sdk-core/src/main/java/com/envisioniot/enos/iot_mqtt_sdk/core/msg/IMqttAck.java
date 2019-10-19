package com.envisioniot.enos.iot_mqtt_sdk.core.msg;

public interface IMqttAck {

    public final static int SUCCESS_CODE = 200;

    int getCode();

    String getMessage();

    default boolean isSuccess() {
        return getCode() == SUCCESS_CODE;
    }
}
