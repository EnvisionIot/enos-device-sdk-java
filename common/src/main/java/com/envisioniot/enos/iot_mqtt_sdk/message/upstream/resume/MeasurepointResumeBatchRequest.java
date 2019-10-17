package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;

import java.util.*;

/**
 * Make use of on-line channel to send offline messages in batch
 */
public class MeasurepointResumeBatchRequest extends BaseMqttRequest<MeasurepointResumeBatchResponse> {

    private static final long serialVersionUID = -4792098821956005525L;

    private boolean allowOfflineSubDevice;
    private boolean skipInvalidMeasurepoints;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<Builder, MeasurepointResumeBatchRequest> {
        private boolean allowOfflineSubDevice;
        private boolean skipInvalidMeasurepoints;

        List<MeasurepointResumeRequest> requests = new LinkedList<>();

        public Builder setRequests(List<MeasurepointResumeRequest> requests) {
            this.requests = requests;
            return this;
        }

        public Builder addRequests(List<MeasurepointResumeRequest> requests) {
            this.requests.addAll(requests);
            return this;
        }

        public Builder addRequest(MeasurepointResumeRequest request) {
            this.requests.add(request);
            return this;
        }

        public Builder setAllowOfflineSubDevice(boolean allowOfflineSubDevice){
            this.allowOfflineSubDevice = allowOfflineSubDevice;
            return this;
        }

        public Builder setSkipInvalidMeasurepoints(boolean skipInvalidMeasurepoints){
            this.skipInvalidMeasurepoints = skipInvalidMeasurepoints;
            return this;
        }

        @Override
        protected String createMethod() {
            return MethodConstants.MEASUREPOINT_RESUME_BATCH;
        }

        @Override
        protected Object createParams() {
            List<Map<String, Object>> params = new ArrayList<>();
            for (MeasurepointResumeRequest request : requests) {
                Map<String, Object> param = new HashMap<>();
                if (request.getProductKey() != null) {
                    param.put("productKey", request.getProductKey());
                }
                if (request.getDeviceKey() != null) {
                    param.put("deviceKey", request.getDeviceKey());
                }
                Map<String, Object> map = request.getParams();
                if (map.containsKey("measurepoints")) {
                    param.put("measurepoints", map.get("measurepoints"));
                }
                if (map.containsKey("time")) {
                    param.put("time", map.get("time"));
                } else {
                    param.put("time", System.currentTimeMillis());
                }
                params.add(param);
            }
            return params;
        }

        @Override
        protected MeasurepointResumeBatchRequest createRequestInstance() {
            return new MeasurepointResumeBatchRequest();
        }

        @Override
        public MeasurepointResumeBatchRequest build() {
            MeasurepointResumeBatchRequest request = super.build();

            request.setAllowOfflineSubDevice(allowOfflineSubDevice);
            request.setSkipInvalidMeasurepoints(skipInvalidMeasurepoints);

            return request;
        }

    }

    @Override
    public Class<MeasurepointResumeBatchResponse> getAnswerType() {
        return MeasurepointResumeBatchResponse.class;
    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.MEASUREPOINT_RESUME_BATCH;
    }

    @Override
    protected Map<String, Object> getJsonPayload() {
        Map<String, Object> payload = super.getJsonPayload();
        payload.put("allowOfflineSubDevice", getAllowOfflineSubDevice());
        payload.put("skipInvalidMeasurepoints", getSkipInvalidMeasurepoints());
        return payload;
    }

    public void setAllowOfflineSubDevice(boolean allowOfflineSubDevice) {
        this.allowOfflineSubDevice = allowOfflineSubDevice;
    }

    public void setSkipInvalidMeasurepoints(boolean skipInvalidMeasurepoints) {
        this.skipInvalidMeasurepoints = skipInvalidMeasurepoints;
    }

    public Boolean getAllowOfflineSubDevice() {
        return allowOfflineSubDevice;
    }

    public Boolean getSkipInvalidMeasurepoints() {
        return skipInvalidMeasurepoints;
    }
}
