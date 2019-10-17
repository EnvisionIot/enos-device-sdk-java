package com.envisioniot.enos.iot_mqtt_sdk.core.profile;

import com.envisioniot.enos.iot_mqtt_sdk.util.SecureMode;
import com.envisioniot.enos.iot_mqtt_sdk.util.SecureModeUtil;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author zhensheng.cai
 * @date 2019/1/14.
 */
public class DeviceCredential implements Serializable {

    private static final long serialVersionUID = -2073358271571983594L;
    private final String productKey;
    private final String productSecret;
    private final String deviceKey;
    private final String deviceSecret;

    public DeviceCredential(String productKey, String productSecret, String deviceKey, String deviceSecret) {
        this.productKey = StringUtil.isEmpty(productKey) ? null : productKey.trim();
        this.productSecret = StringUtil.isEmpty(productSecret) ? null : productSecret.trim();
        this.deviceKey = StringUtil.isEmpty(deviceKey) ? null : deviceKey.trim();
        this.deviceSecret = StringUtil.isEmpty(deviceSecret) ? null : deviceSecret;
    }

    public String getProductKey() {
        return productKey;
    }

    public String getProductSecret() {
        return productSecret;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public String getDeviceSecret() {
        return deviceSecret;
    }

    public SecureMode getSecureMode(){
        return SecureModeUtil.getSecureMode(productKey, productSecret, deviceKey, deviceSecret);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceCredential that = (DeviceCredential) o;
        return Objects.equals(productKey, that.productKey) &&
                Objects.equals(productSecret, that.productSecret) &&
                Objects.equals(deviceKey, that.deviceKey) &&
                Objects.equals(deviceSecret, that.deviceSecret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productKey, productSecret, deviceKey, deviceSecret);
    }

    @Override
    public String toString() {
        return "DeviceCredential{" +
                "productKey='" + productKey + '\'' +
                ", productSecret='" + productSecret + '\'' +
                ", deviceKey='" + deviceKey + '\'' +
                ", deviceSecret='" + deviceSecret + '\'' +
                '}';
    }
}
