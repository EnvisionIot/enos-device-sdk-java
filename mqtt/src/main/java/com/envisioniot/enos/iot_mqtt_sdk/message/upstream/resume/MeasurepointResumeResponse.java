package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.util.regex.Pattern;

public class MeasurepointResumeResponse extends BaseMqttResponse {

    private static final long serialVersionUID = 1289287472462692026L;
    private static Pattern pattern = Pattern.compile(ArrivedTopicPattern.MEASUREPOINT_RESUME_REPLY);


    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }
}
