package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Make use of on-line channel to send offline messages
 */
public class MeasurepointResumeRequest extends BaseMqttRequest<MeasurepointResumeResponse> {
    private static final long serialVersionUID = 4018722889739885894L;


    public static MeasurepointResumeRequest.Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<Builder, MeasurepointResumeRequest> {
        private Map<String, Object> params = new HashMap<>();

        public Builder() {
            params.put("measurepoints", new HashMap<>());
            params.put("time", System.currentTimeMillis());
        }

        @SuppressWarnings("unchecked")
        public Builder addMeasurePoint(String key, Object value) {
            Map<String, Object> values = (Map<String, Object>) params.get("measurepoints");
            values.put(key, value);
            return this;
        }

        /**
         * @param key measurepoint identifier
         * @param value value of measurepoint
         * @param quality quality of measurepoint
         * @return builder
         */
        @SuppressWarnings("unchecked")
        public Builder addMeasreuPointWithQuality(String key, Object value, Object quality) {
            Map<String, Object> values = (Map<String, Object>) params.get("measurepoints");
            Map<String, Object> valueWithQuality = new HashMap<>();
            valueWithQuality.put("value", value);
            valueWithQuality.put("quality", quality);
            values.put(key, valueWithQuality);
            return this;
        }



        @SuppressWarnings("unchecked")
        public Builder addMeasurePoints(Map<String, Object> value) {
            Map<String, Object> values = (Map<String, Object>) params.get("measurepoints");
            values.putAll(value);
            return this;
        }

        public Builder setMeasurePoints(Map<String, Object> value) {
            params.put("measurepoints", value);
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            params.put("time", timestamp);
            return this;
        }

        @Override
        protected String createMethod() {
            return MethodConstants.MEASUREPOINT_RESUME;
        }

        @Override
        protected Object createParams() {
            return params;
        }

        @Override
        protected MeasurepointResumeRequest createRequestInstance() {
            return new MeasurepointResumeRequest();
        }

    }

    private MeasurepointResumeRequest() {
    }


    @Override
    public Class<MeasurepointResumeResponse> getAnswerType() {
        return MeasurepointResumeResponse.class;
    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.MEASUREPOINT_RESUME;
    }

}
