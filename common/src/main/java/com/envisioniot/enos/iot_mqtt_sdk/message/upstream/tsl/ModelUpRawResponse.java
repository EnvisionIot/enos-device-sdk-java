package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

/**
 * @author zhensheng.cai
 * @date 2018/7/10.
 */
public class ModelUpRawResponse extends BaseMqttResponse {
    private static final long serialVersionUID = -3130733082784463467L;

    private static Pattern pattern = Pattern.compile(ArrivedTopicPattern.MODEL_UP_RAW_REPLY);

    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }

    private byte[] payload;

    public ModelUpRawResponse()
    {}

    public ModelUpRawResponse(byte[] payload)
    {
        this.payload = payload;
    }

    public void setPayload(byte[] payload)
    {
        this.payload = payload;
    }

    public byte[] getPayload()
    {
        return payload;
    }

    @Override
    public DecodeResult decode(String topic, byte[] payload) {
        List<String> path = this.match(topic);
        if (path == null) {
            return null;
        }

        ModelUpRawResponse arrivedMsg = new ModelUpRawResponse();
        arrivedMsg.setPayload(payload);
        arrivedMsg.setId("unknown");
        arrivedMsg.setMessageTopic(topic);
        if (path.size() > 0) {
            arrivedMsg.setProductKey(path.get(0));
        }
        if (path.size() > 1) {
            arrivedMsg.setDeviceKey(path.get(1));
        }
        return new DecodeResult(arrivedMsg, path);
    }

    @Override
    public String toString() {
        return "ModelUpRawResponse{" +
                "payload=" + Arrays.toString(payload) +
                "} ";
    }
}
