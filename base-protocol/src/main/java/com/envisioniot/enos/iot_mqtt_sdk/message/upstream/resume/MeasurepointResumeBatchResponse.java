package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.util.regex.Pattern;

public class MeasurepointResumeBatchResponse extends BaseMqttResponse {


    private static final long serialVersionUID = -7559431496854795924L;

    public static final Pattern pattern = Pattern.compile(ArrivedTopicPattern.MEASUREPOINT_RESUME_BATCH_REPLY);

    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }

}
