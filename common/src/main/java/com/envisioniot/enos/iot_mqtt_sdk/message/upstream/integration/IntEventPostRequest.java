package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.util.Pair;

import java.util.*;

/**
 * @author zhensheng.cai
 * @date 2018/7/3.
 */
public class IntEventPostRequest extends BaseMqttRequest<IntEventPostResponse> {
    private static final long serialVersionUID = -679120066776767162L;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<Builder, IntEventPostRequest> {

        private Map<Pair<String/*dk*/, Long/*time*/>, Map<String/*eventId*/, Map<String, Object>/*output*/>> events;

        public Builder() {
            this.events = new LinkedHashMap<>();
        }

        public Builder setEvents(Map<Pair<String, Long>, Map<String, Map<String, Object>>> events) {
            this.events = events;
            return this;
        }

        public Builder addEvent(String deviceKey, long time, String eventIdentifier, Map<String, Object> eventOutputs) {
            Map<String/*eventId*/, Map<String, Object>/*output*/> eventMap = this.events.get(Pair.makePair(deviceKey, time));
            if (eventMap == null) {
                eventMap = new HashMap<>();
                this.events.put(Pair.makePair(deviceKey, time), eventMap);
            }
            eventMap.put(eventIdentifier, eventOutputs);
            return this;
        }


        @Override
        protected String createMethod() {
            return MethodConstants.INTEGRATION_EVENT_POST;
        }

        @Override
        protected Object createParams() {
            List<Map<String, Object>> params = new ArrayList<>();
            if (events != null) {
                for (Map.Entry<Pair<String, Long>, Map<String, Map<String, Object>>> entry : this.events.entrySet()) {
                    Map<String, Object> param = new HashMap<>();
                    param.put("deviceKey", entry.getKey().first);
                    param.put("time", entry.getKey().second);
                    param.put("events", entry.getValue());
                    params.add(param);
                }
            }
            return params;
        }

        @Override
        protected IntEventPostRequest createRequestInstance() {
            return new IntEventPostRequest();
        }
    }

    private IntEventPostRequest() {
        super();
    }

    @Override
    public String getMessageTopic() {
        return String.format(_getPK_DK_FormatTopic(), getProductKey());
    }

    @Override
    public Class<IntEventPostResponse> getAnswerType() {
        return IntEventPostResponse.class;
    }

    @Override
    public int getQos() {
        return 0;
    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.INTEGRATION_EVENT_POST;
    }
}
