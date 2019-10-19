package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;

import java.util.*;

/**
 * @author zhensheng.cai
 * @date 2018/11/9.
 */
public class IntAttributePostRequest extends BaseMqttRequest<IntAttributePostResponse> {

    private static final long serialVersionUID = -1263009599788931009L;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<Builder, IntAttributePostRequest> {

        public Map<String, Map<String, Object>> attributes;

        public Builder() {
            this.attributes = new LinkedHashMap<>();
        }

        public Builder addAttribute(String deviceKey, String key, Object value) {
            Map<String, Object> attr = attributes.get(deviceKey);
            if (attr == null) {
                attr = new HashMap<>();
                attributes.put(deviceKey, attr);
            }
            attr.put(key, value);
            return this;
        }

        public Builder addAttributes(String deviceKey, Map<String, Object> values) {
            Map<String, Object> attr = attributes.get(deviceKey);
            if (attr == null) {
                attr = new HashMap<>();
                attributes.put(deviceKey, attr);
            }
            attr.putAll(values);
            return this;
        }

        public Builder setAttributes(Map<String, Map<String, Object>> values) {
            this.attributes = values;
            return this;
        }

        @Override
        protected String createMethod() {
            return MethodConstants.INTEGRATION_ATTRIBUTE_POST;
        }

        @Override
        protected Object createParams() {
            List<Map<String, Object>> params = new ArrayList<>();
            if (attributes != null) {
                for (Map.Entry<String, Map<String, Object>> entry : attributes.entrySet()) {
                    Map<String, Object> param = new LinkedHashMap<>();
                    param.put("deviceKey", entry.getKey());
                    param.put("attributes", entry.getValue());
                    params.add(param);
                }
            }
            return params;
        }

        @Override
        protected IntAttributePostRequest createRequestInstance() {
            return new IntAttributePostRequest();
        }

    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.INTEGRATION_ATTRIBUTE_POST;
    }

    @Override
    public Class<IntAttributePostResponse> getAnswerType() {
        return IntAttributePostResponse.class;
    }
}
