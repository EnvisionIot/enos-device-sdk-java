package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status;

import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DeviceCredential;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.util.CheckUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SubDeviceLoginBatchRequest extends BaseMqttRequest<SubDeviceLoginBatchResponse> {
    private static final long serialVersionUID = -1L;

    public static SubDeviceLoginBatchRequest.Builder builder() {
        return new SubDeviceLoginBatchRequest.Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<SubDeviceLoginBatchRequest.Builder, SubDeviceLoginBatchRequest> {
        private String clientId = "login.batch";
        private long timestamp = 0;
        private SignMethod signMethod = SignMethod.SHA256;

        private List<DeviceCredential> subDevices = new LinkedList<>();

        public SubDeviceLoginBatchRequest.Builder addSubDeviceInfo(String productKey, String deviceKey, String deviceSecret) {
            return addSubDeviceInfo(new DeviceCredential(productKey, null, deviceKey, deviceSecret));
        }

        public SubDeviceLoginBatchRequest.Builder addSubDeviceInfo(DeviceCredential cred) {
            subDevices.add(cred);
            return this;
        }

        @Override
        protected String createMethod() {
            return MethodConstants.SUB_DEVICE_LOGIN_BATCH;
        }

        @Override
        protected Object createParams() {
            Map<String, Object> params = new HashMap<>();

            params.put("clientId", clientId);

            if (timestamp <= 0) {
                timestamp = System.currentTimeMillis();
            }
            params.put("timestamp", String.valueOf(timestamp));

            params.put("signMethod", signMethod.getName());

            List<Object> subDeviceList = new LinkedList<>();
            for (DeviceCredential cred : subDevices) {
                SubDeviceLoginInfo subDev = new SubDeviceLoginInfo(
                        cred.getProductKey(),
                        cred.getProductSecret(),
                        cred.getDeviceKey(),
                        cred.getDeviceSecret(),
                        signMethod, timestamp, clientId, false);

                Map<String, String> subDevData = new HashMap<>();
                subDevData.put("productKey", cred.getProductKey());
                subDevData.put("deviceKey", cred.getDeviceKey());
                subDevData.put("secureMode", String.valueOf(subDev.getSecureMode().getModeId()));
                subDevData.put("sign", subDev.getSign());

                subDeviceList.add(subDevData);
            }

            if (!subDeviceList.isEmpty()) {
                params.put("subDevices", subDeviceList);
            }

            return params;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public void setSignMethod(SignMethod signMethod) {
            this.signMethod = signMethod;
        }

        @Override
        protected SubDeviceLoginBatchRequest createRequestInstance() {
            return new SubDeviceLoginBatchRequest();
        }

    }

    @Override
    public void check() throws EnvisionException {
        super.check();
        Map<String, String> params = getParams();
        CheckUtil.checkNotEmpty(params.get("timestamp"), "timestamp");
        CheckUtil.checkNotEmpty(params.get("clientId"), "clientId");
        CheckUtil.checkNotEmpty(params.get("signMethod"), "signMethod");
        CheckUtil.checkNotEmpty(params.get("subDevices"), "subDevices");
    }

    @Override
    public Class<SubDeviceLoginBatchResponse> getAnswerType() {
        return SubDeviceLoginBatchResponse.class;
    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.SUB_DEVICE_LOGIN_BATCH_TOPIC_FMT;
    }
}
