package com.envisioniot.enos.iot_mqtt_sdk.core.login;

/**
 * Describe the input for device to login in common way though the
 * triple (product key, device key, device secret)
 *
 * @author jian.zhang4
 */
public class NormalDeviceLoginInput extends LoginInput {
    private final String productKey;
    private final String deviceKey;
    private final String deviceSecret;

    public NormalDeviceLoginInput(String serverUrl, String productKey, String deviceKey, String deviceSecret) {
        super(serverUrl);
        this.productKey = productKey;
        this.deviceKey = deviceKey;
        this.deviceSecret = deviceSecret;
    }

    @Override
    public String getProductKey() {
        return productKey;
    }

    /**
     * No need for the product secret
     */
    @Override
    public String getProductSecret() {
        return null;
    }

    @Override
    public String getDeviceKey() {
        return deviceKey;
    }

    @Override
    public String getDeviceSecret() {
        return deviceSecret;
    }
}
