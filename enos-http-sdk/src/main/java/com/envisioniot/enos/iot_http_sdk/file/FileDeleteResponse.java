package com.envisioniot.enos.iot_http_sdk.file;


import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttResponse;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author :charlescai
 * @date :2020-04-21
 */
public class FileDeleteResponse implements IMqttResponse {

    private int code;
    private String message;

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public boolean isSuccess()
    {
        return code == SUCCESS_CODE;
    }

    @Override
    public Pattern getMatchTopicPattern() {
        return null;
    }

    @Override
    public String getMessageId() {
        return null;
    }

    @Override
    public void setMessageId(String msgId) {

    }

    @Override
    public String getMessageTopic() {
        return null;
    }

    @Override
    public void setMessageTopic(String topic) {

    }

    @Override
    public String getProductKey() {
        return null;
    }

    @Override
    public void setProductKey(String productKey) {

    }

    @Override
    public String getDeviceKey() {
        return null;
    }

    @Override
    public void setDeviceKey(String deviceKey) {

    }
}
