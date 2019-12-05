package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author zhensheng.cai
 * @date 2018/7/10.
 */
public class IntModelUpRawResponse extends BaseMqttResponse {
    private static final long serialVersionUID = -3130733082784463467L;

    private static Pattern pattern = Pattern.compile(ArrivedTopicPattern.INTEGRATION_MODEL_UP_RAW_REPLY);

    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }

    private byte[] payload;

    public IntModelUpRawResponse() {
    }

    public IntModelUpRawResponse(byte[] payload) {
        this.payload = payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }

    @Override
    public DecodeResult decode(String topic, byte[] payload) {
        List<String> path = this.match(topic);
        if (path == null) {
            return null;
        }

        IntModelUpRawResponse arrivedMsg = new IntModelUpRawResponse();
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
        return "IntModelUpRawResponse{" +
                "payload=" + Arrays.toString(payload) +
                "} ";
    }

}
