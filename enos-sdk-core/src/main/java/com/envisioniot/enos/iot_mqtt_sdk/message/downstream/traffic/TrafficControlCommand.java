package com.envisioniot.enos.iot_mqtt_sdk.message.downstream.traffic;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttCommand;

import java.util.regex.Pattern;

/**
 * @author mengyuantan
 * @date 2021/9/14 17:36
 */
public class TrafficControlCommand extends BaseMqttCommand<TrafficControlReply> {
    private static final long serialVersionUID = -6511773492855053181L;
    private static Pattern pattern = Pattern.compile(ArrivedTopicPattern.TRAFFIC_CONTROL_COMMAND);

    @Override
    public Class<TrafficControlReply> getAnswerType() {
        return TrafficControlReply.class;
    }

    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }
}
