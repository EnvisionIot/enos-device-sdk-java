package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.network;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.util.regex.Pattern;

/**
 * @author mengyuantan
 * @date 2020/10/16 13:58
 */
public class NetworkStatusReportResponse extends BaseMqttResponse {
    private static final long serialVersionUID = 6049281715026111313L;
    private static Pattern pattern = Pattern.compile(ArrivedTopicPattern.NETWORK_STATUS_REPLY);

    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }
}
