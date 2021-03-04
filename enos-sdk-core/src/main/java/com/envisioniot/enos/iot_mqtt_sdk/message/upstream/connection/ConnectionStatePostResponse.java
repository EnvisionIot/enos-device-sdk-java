package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.connection;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.util.regex.Pattern;

/**
 * @author mengyuantan
 * @date 2021/1/11 12:06
 */
public class ConnectionStatePostResponse extends BaseMqttResponse {
    private static final long serialVersionUID = 1943400097208369291L;
    private static Pattern pattern = Pattern.compile(ArrivedTopicPattern.CONNECTION_STATE_REPLY);

    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }
}
