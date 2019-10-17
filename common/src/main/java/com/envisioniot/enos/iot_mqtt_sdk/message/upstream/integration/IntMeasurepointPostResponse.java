package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.util.regex.Pattern;

/**
 * @author zhensheng.cai
 * @date 2019/1/23.
 */
public class IntMeasurepointPostResponse extends BaseMqttResponse {

    private static final long serialVersionUID = 1283125679626035647L;

    public static final Pattern pattern = Pattern.compile(ArrivedTopicPattern.INTEGRATION_MEASUREPOINT_POST_REPLY);

    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }

}
