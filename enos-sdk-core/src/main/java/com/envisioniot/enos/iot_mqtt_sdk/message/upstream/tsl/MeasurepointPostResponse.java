package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.util.regex.Pattern;

/**
 * @author zhensheng.cai
 * @date 2018/7/12.
 */
public class MeasurepointPostResponse extends BaseMqttResponse {

    private static final long serialVersionUID = 1289287472462692026L;
    private static Pattern pattern = Pattern.compile(ArrivedTopicPattern.MEASUREPOINT_POST_REPLY);


    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }
}
