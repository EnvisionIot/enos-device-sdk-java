package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.topo;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignUtil;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Description: sub-device information
 *
 * @author zhonghua.wu
 * @create 2018-07-12 15:45
 */
public class SubDeviceInfo {

    private final String productKey;
    private final String deviceKey;
    private final String clientId;
    private final long timestamp;
    private final String sign;
    private final SignMethod signMethod;

    public SubDeviceInfo(String productKey, String deviceKey, String deviceSecret) {
        this(productKey, deviceKey, deviceSecret, SignUtil.DEFAULT_SIGN_METHOD);
    }

    public SubDeviceInfo(String productKey, String deviceKey, String deviceSecret, SignMethod signMethod) {
        this.productKey = productKey;
        this.deviceKey = deviceKey;

        this.timestamp = System.currentTimeMillis();
        this.clientId = getDefaultClientId(productKey, deviceKey, timestamp);

        this.signMethod = signMethod;

        Map<String, String> params = Maps.newHashMap();
        params.put("productKey", productKey);
        params.put("deviceKey", deviceKey);
        params.put("clientId", clientId);
        params.put("timestamp", String.valueOf(timestamp));
        this.sign = SignUtil.sign(deviceSecret, params, signMethod);
    }

    public String getClientId() {
        return clientId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSign() {
        return sign;
    }

    public Map<String, String> createSignMap() {
        Map<String, String> params = Maps.newHashMap();
        params.put("productKey", productKey);
        params.put("deviceKey", deviceKey);
        params.put("clientId", clientId);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("signMethod", signMethod.getName());
        params.put("sign", sign);
        return params;
    }

    private static String getDefaultClientId(String productKey, String deviceKey, long timestamp) {
        return String.format("%s.%s.%s", productKey, deviceKey, String.valueOf(timestamp));
    }

    public String getProductKey() {
        return productKey;
    }

    public String getDeviceKey() {
        return deviceKey;
    }
}
