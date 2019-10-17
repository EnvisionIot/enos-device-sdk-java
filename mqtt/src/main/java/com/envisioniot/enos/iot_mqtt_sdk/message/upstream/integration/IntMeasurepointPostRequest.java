package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.util.Pair;

import java.util.*;

/**
 * @author zhensheng.cai
 * @date 2019/1/23.
 */
public class IntMeasurepointPostRequest extends BaseMqttRequest<IntMeasurepointPostResponse> {

    private static final long serialVersionUID = -2790776537600759967L;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<Builder, IntMeasurepointPostRequest> {


        private Map<Pair<String/*dk*/, Long/*time*/>, Map<String/*pointId*/, Object/*value*/>> measurepoints;

        public Builder() {
            this.measurepoints = new LinkedHashMap<>();
        }

        public Builder setMeasurepoints(Map<Pair<String, Long>, Map<String, Object>> measurepoints) {
            this.measurepoints = measurepoints;
            return this;
        }

        public Builder addMeasurepoint(String deviceKey, long time, Map<String, Object> measurepointValues) {
            Map<String, Object> map = this.measurepoints.get(Pair.makePair(deviceKey, time));
            if (map == null) {
                map = new HashMap<>();
                this.measurepoints.put(Pair.makePair(deviceKey, time), map);
            }
            map.putAll(measurepointValues);
            return this;
        }

        @Override
        protected String createMethod() {
            return MethodConstants.INTEGRATION_MEASUREPOINT_POST;
        }

        @Override
        protected Object createParams() {
            List<Map<String, Object>> params = new ArrayList<>();
            if (measurepoints != null) {
                for (Map.Entry<Pair<String, Long>, Map<String, Object>> entry : measurepoints.entrySet()) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("deviceKey", entry.getKey().first);
                    param.put("time", entry.getKey().second);
                    param.put("measurepoints", entry.getValue());
                    params.add(param);
                }
            }
            return params;
        }

        @Override
        protected IntMeasurepointPostRequest createRequestInstance() {
            return new IntMeasurepointPostRequest();
        }

    }


    @Override
    public Class<IntMeasurepointPostResponse> getAnswerType() {
        return IntMeasurepointPostResponse.class;
    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.INTEGRATION_MEASUREPOINT_POST;
    }
}
