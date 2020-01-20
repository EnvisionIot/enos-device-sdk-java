package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author zhensheng.cai
 * @date 2018/7/12.
 */
public class TslTemplateGetResponse extends BaseMqttResponse {

    private static final long serialVersionUID = -5129474304666285214L;
    private static Pattern pattern = Pattern.compile(ArrivedTopicPattern.TSL_TEMPLATE_GET_REPLY);


    @SuppressWarnings("unchecked")
    public Map<String, Object> getAttributs() {
        Map<String, Object> data = this.getData();
        return ((Map<String, Object>) data.get("tslAttributeMap"));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMeasurepoints() {
        Map<String, Object> data = this.getData();
        return ((Map<String, Object>) data.get("tslMeasurepointMap"));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getEvents() {
        Map<String, Object> data = this.getData();
        return ((Map<String, Object>) data.get("tslEventMap"));
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getServices() {
        Map<String, Object> data = this.getData();
        return ((Map<String, Object>) data.get("tslServiceMap"));
    }

    @Override
    public Pattern getMatchTopicPattern() {
        return pattern;
    }
}