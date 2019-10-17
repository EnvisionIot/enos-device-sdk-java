package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;

import java.util.Arrays;

/**
 * @author zhensheng.cai
 * @date 2018/7/5.
 */
public class IntModelUpRawRequest extends BaseMqttRequest<IntModelUpRawResponse> {
    private static final long serialVersionUID = -4036143265439236093L;

    private byte[] payload;

    private IntModelUpRawRequest() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<Builder, IntModelUpRawRequest> {
        public Builder() {
        }

        private byte[] payload;

        public Builder setPayload(byte[] payload) {
            this.payload = payload;
            return this;
        }

        @Override
        protected String createMethod() {
            return MethodConstants.INTEGRATION_MODEL_UP_RAW;
        }

        @Override
        protected Object createParams() {
            throw new UnsupportedOperationException("unsupported operation");
        }

        @Override
        protected IntModelUpRawRequest createRequestInstance() {
            return new IntModelUpRawRequest();
        }

        @Override
        public IntModelUpRawRequest build() {
            IntModelUpRawRequest request = createRequestInstance();
            if (StringUtil.isNotEmpty(productKey)) {
                request.setProductKey(productKey);
            }
            if (StringUtil.isNotEmpty(deviceKey)) {
                request.setDeviceKey(deviceKey);
            }
            request.setMethod(createMethod());
            request.setPayload(payload);
            return request;
        }
    }

    @Override
    public String getId() {
        return "unknown";
    }

    @Override
    public void setId(String id) {
        throw new UnsupportedOperationException("cannot set raw request message id");
    }

    @Override
    public String getMessageId() {
        return "unknown";
    }

    @Override
    public void setMessageId(String msgId) {
        throw new UnsupportedOperationException("cannot set raw request message id");
    }

    @Override
    public byte[] encode() {
        return payload;
    }

    public IntModelUpRawRequest setPayload(byte[] payload) {
        this.payload = payload;
        return this;
    }

    @Override
    public Class<IntModelUpRawResponse> getAnswerType() {
        return IntModelUpRawResponse.class;
    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.INTEGRATION_MODEL_UP_RAW;
    }

    @Override
    public String toString() {
        return "IntModelUpRawRequest{" +
                "payload=" + Arrays.toString(payload) +
                "} ";
    }
}
