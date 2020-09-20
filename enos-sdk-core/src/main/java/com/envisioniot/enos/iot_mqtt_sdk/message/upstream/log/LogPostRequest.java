package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.log;

import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * payload sample
 * <p>
 * {
 *     "id":"123",
 *     "version":"1.0",
 *     "params":{
 *         "logs":{
 *             "traceId":"traceId",
 *             "level": "info",
 *             "module":"name",
 *             "content":"json type"
 *         },
 *         "time":123456
 *     },
 *     "method":"thing.log.post"
 * }
 *
 * @author zhensheng.cai
 * @date 2018/7/3.
 */
public class LogPostRequest extends BaseMqttRequest<LogPostResponse> {
    private static final long serialVersionUID = -8186172184432202539L;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<Builder, LogPostRequest> {
        private final Map<String, Object> params = new HashMap<>();

        public Builder() {
            params.put("logs", new HashMap<String, Object>());
            params.put("time", System.currentTimeMillis());
        }

        public Builder setTraceId(String traceId) {
            return addValue("traceId", traceId);
        }

        public Builder setLevel(String level) {
            return addValue("level", level);
        }

        public Builder setModule(String module) {
            return addValue("module", module);
        }

        public Builder setContent(String content) {
            return addValue("content", content);
        }

        @SuppressWarnings("unchecked")
        public Builder addValue(String point, Object value) {
            Map<String, Object> values = (Map<String, Object>) params.get("logs");
            values.put(point, value);
            return this;
        }


        @SuppressWarnings("unchecked")
        public Builder addValues(Map<String, Object> value) {
            Map<String, Object> values = (Map<String, Object>) params.get("logs");
            values.putAll(value);
            return this;
        }

        public Builder setValues(Map<String, Object> value) {
            params.put("logs", value);
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            params.put("time", timestamp);
            return this;
        }

        @Override
        protected String createMethod() {
            return MethodConstants.LOG_POST;
        }

        @Override
        protected Object createParams() {
            return params;
        }

        @Override
        protected LogPostRequest createRequestInstance() {
            return new LogPostRequest();
        }
    }


    private LogPostRequest() {
        super();
    }

    @Override
    public void check() throws EnvisionException {
        super.check();
    }

    @Override
    public String getMessageTopic() {
        return String.format(_getPK_DK_FormatTopic(), getProductKey(), getDeviceKey());
    }

    @Override
    public Class<LogPostResponse> getAnswerType() {
        return LogPostResponse.class;
    }

    @Override
    public int getQos() {
        return 0;
    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.LOG_POST;
    }


}
