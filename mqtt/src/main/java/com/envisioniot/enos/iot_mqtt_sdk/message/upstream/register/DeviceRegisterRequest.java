package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.register;

import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.util.CheckUtil;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringI18n;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description: sub-device dynamic register request
 *
 * @author zhonghua.wu
 * @create 2018-07-20 10:33
 */
public class DeviceRegisterRequest extends BaseMqttRequest<DeviceRegisterResponse> {
    private static final long serialVersionUID = -5164903941570526819L;
    private static final int MAX_DEVICE_SIZE = 1000;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<Builder, DeviceRegisterRequest> {
        private List<Map<String, Object>> params = new ArrayList<>();

        @Override
        protected String createMethod() {
            return MethodConstants.DEVICE_REGISTER;
        }

        @Override
        protected Object createParams() {
            return params;
        }

        @Override
        protected DeviceRegisterRequest createRequestInstance() {
            return new DeviceRegisterRequest();
        }

        public Builder addBatchRegisterInfo(String productKey, List<DeviceRegOption> regOptions) {
            params.addAll(createBatchRegInfoMap(productKey, regOptions));
            return this;
        }

        public Builder setBatchRegisterInfo(String productKey, List<DeviceRegOption> regOptions) {
            params = createBatchRegInfoMap(productKey, regOptions);
            return this;
        }

        public Builder addRegisterInfo(String productKey, DeviceRegOption regOption) {
            params.add(createRegInfoMap(productKey, regOption));
            return this;
        }

        public Builder addRegisterInfo(String productKey, String deviceKey, StringI18n deviceName, String deviceDesc, String timezone) {
            addRegisterInfo(productKey, new DeviceRegOption(deviceKey, deviceName, deviceDesc, timezone));
            return this;
        }

        public Builder addRegisterInfo(String productKey, String deviceKey, StringI18n deviceName, String deviceDesc, String timezone,
                                          Map<String, Object> deviceAttrs) {
            addRegisterInfo(productKey, new DeviceRegOption(deviceKey, deviceName, deviceDesc, timezone, deviceAttrs));
            return this;
        }

    }

    private DeviceRegisterRequest() {
    }

    private static List<Map<String, Object>> createBatchRegInfoMap(String productKey, List<DeviceRegOption> regOptions) {
        List<Map<String, Object>> params = new ArrayList<>();
        for (DeviceRegOption regOption : regOptions) {
            params.add(createRegInfoMap(productKey, regOption));
        }
        return params;
    }

    private static Map<String, Object> createRegInfoMap(String productKey, DeviceRegOption regOption) {
        Map<String, Object> param = new HashMap<>();
        param.put("productKey", productKey);

        if (regOption.deviceAttributes != null && !regOption.deviceAttributes.isEmpty()) {
            param.put("deviceAttributes", regOption.deviceAttributes);
        }
        if (StringUtil.isNotEmpty(regOption.deviceKey)) {
            param.put("deviceKey", regOption.deviceKey);
        }
        if (regOption.deviceName != null) {
            param.put("deviceName", regOption.deviceName);
        }
        if (StringUtil.isNotEmpty(regOption.deviceDesc)) {
            param.put("deviceDesc", regOption.deviceDesc);
        }
        if (StringUtil.isNotEmpty(regOption.timezone)) {
            param.put("timezone", regOption.timezone);
        }
        return param;
    }


    @Override
    public Class<DeviceRegisterResponse> getAnswerType() {
        return DeviceRegisterResponse.class;
    }

    @Override
    public void check() throws EnvisionException {
        List<Map<String, Object>> params = this.getParams();
        CheckUtil.checkMaxSize(params, MAX_DEVICE_SIZE, "regOptionList");
    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.DEVICE_REGISTER_TOPIC_FMT;
    }
}
