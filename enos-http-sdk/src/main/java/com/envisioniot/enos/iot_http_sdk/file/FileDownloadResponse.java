package com.envisioniot.enos.iot_http_sdk.file;

import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttResponse;

import java.util.regex.Pattern;

public class FileDownloadResponse implements IMqttResponse {

    private int id;
    private int code;
    private String message;
    private String data;


    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
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

    public void setMessage(String message) {
        this.message = message;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
