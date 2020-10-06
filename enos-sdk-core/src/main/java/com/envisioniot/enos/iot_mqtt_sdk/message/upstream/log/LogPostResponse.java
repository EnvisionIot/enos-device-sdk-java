package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.log;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.util.regex.Pattern;

/**
 * @author zhensheng.cai
 * @date 2018/7/9.
 */
public class LogPostResponse extends BaseMqttResponse {
    private static final long serialVersionUID = -8771628509472624926L;
    private static final Pattern PATTERN = Pattern.compile(ArrivedTopicPattern.LOG_POST_REPLY);

    @Override
    public Pattern getMatchTopicPattern() {
        return PATTERN;
    }

}
