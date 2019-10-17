package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.util.regex.Pattern;

/**
 * @author zhensheng.cai
 * @date 2018/11/9.
 */
public class IntAttributePostResponse extends BaseMqttResponse {
    private static final long serialVersionUID = -4442470288258657460L;

    private static final Pattern pattern = Pattern.compile(ArrivedTopicPattern.INTEGRATION_ATTRIBUTE_POST_REPLY);

    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }
}
