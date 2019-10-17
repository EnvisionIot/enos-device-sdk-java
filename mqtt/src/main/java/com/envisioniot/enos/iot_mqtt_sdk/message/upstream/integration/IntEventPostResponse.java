package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.util.regex.Pattern;

/**
 * @author zhensheng.cai
 * @date 2018/7/9.
 */
public class IntEventPostResponse extends BaseMqttResponse {
    private static final long serialVersionUID = -216756446742539938L;

    private static Pattern pattern = Pattern.compile(ArrivedTopicPattern.INTEGRATION_EVENT_POST_REPLY);

    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }

}
