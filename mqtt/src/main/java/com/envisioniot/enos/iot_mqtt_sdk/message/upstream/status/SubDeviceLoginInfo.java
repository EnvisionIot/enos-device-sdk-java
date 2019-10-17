package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignUtil;
import com.envisioniot.enos.iot_mqtt_sdk.util.SecureMode;
import com.envisioniot.enos.iot_mqtt_sdk.util.SecureModeUtil;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Description: sub-device information that's used for login
 *
 * @author zhonghua.wu
 * @author jian.zhang4
 */
public class SubDeviceLoginInfo {

    private final String productKey;
    private final String deviceKey;

    private final String clientId;
    private final long timestamp;
    private final String sign;
    private final SignMethod signMethod;
    private final boolean cleanSession;
    private final Map<String, String> params;
    private final SecureMode secureMode;

    public SubDeviceLoginInfo(String productKey, String deviceKey, String deviceSecret) {
        this(productKey, null, deviceKey, deviceSecret);
    }

    public SubDeviceLoginInfo(String productKey, String productSecret, String deviceKey, String deviceSecret) {
        this(productKey, productSecret, deviceKey, deviceSecret, SignUtil.DEFAULT_SIGN_METHOD, false);
    }

    public SubDeviceLoginInfo(String productKey, String productSecret, String deviceKey,
                              String deviceSecret, SignMethod signMethod, boolean cleanSession) {
        this(productKey, productSecret, deviceKey, deviceSecret, signMethod, System.currentTimeMillis(), cleanSession);
    }

    public SubDeviceLoginInfo(String productKey, String productSecret, String deviceKey,
                              String deviceSecret, SignMethod signMethod, long timestamp,
                              boolean cleanSession) {
        this(productKey, productSecret, deviceKey, deviceSecret, signMethod, timestamp,
                getDefaultClientId(productKey, deviceKey, timestamp), cleanSession);
    }

    public SubDeviceLoginInfo(String productKey, String productSecret, String deviceKey,
                              String deviceSecret, SignMethod signMethod, long timestamp,
                              String clientId, boolean cleanSession) {
        this.productKey = productKey;
        this.deviceKey = deviceKey;
        this.timestamp = timestamp;
        this.clientId = clientId;
        this.signMethod = signMethod;
        this.cleanSession = cleanSession;

        Map<String, String> signParams = Maps.newHashMap();
        signParams.put("productKey", productKey);
        signParams.put("deviceKey", deviceKey);
        signParams.put("clientId", clientId);
        signParams.put("timestamp", String.valueOf(timestamp));

        //Generate signature
        secureMode = SecureModeUtil.getSecureMode(productKey, productSecret, deviceKey, deviceSecret);
        this.sign = SignUtil.sign(secureMode.getSecret(), signParams, signMethod);

        params = Maps.newHashMap(signParams);
        params.put("secureMode", String.valueOf(secureMode.getModeId()));
        params.put("signMethod", this.signMethod.getName());
        params.put("sign", sign);

        if (cleanSession) {
            // it's false by default
            params.put("cleanSession", "true");
        }
    }

    public String getClientId() {
        return clientId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public SignMethod getSignMethod() {
        return signMethod;
    }

    public String getSign() {
        return sign;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public Map<String, String> getParams() {
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

    public SecureMode getSecureMode(){
        return secureMode;
    }

}
