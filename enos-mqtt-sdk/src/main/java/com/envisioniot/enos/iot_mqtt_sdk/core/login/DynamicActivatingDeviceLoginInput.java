package com.envisioniot.enos.iot_mqtt_sdk.core.login;

/**
 * This class provides info to Login through product secret. And after login,
 * the device secret would be returned from broker. Afterwards, device MUST
 * login through the returned device secret.<br/>
 * <br/>
 * Note that for this login mode to work, it has to satisfy: <br/>
 * <li>
 *     device product has been enabled with Dynamic Activation
 * </li>
 * <li>
 *     it's the first time for the device to use this mode to login. If the device
 *     has once logined (namely activated) using this mode already, it would fail
 *     if such login mode is used again (mainly for security concern over passing
 *     device secret).
 * </li>
 *
 * @author jian.zhang4
 */
public class DynamicActivatingDeviceLoginInput extends LoginInput {

    private final String productKey;
    private final String productSecret;
    private final String deviceKey;


    public DynamicActivatingDeviceLoginInput(String serverUrl, String productKey, String productSecret, String deviceKey) {
        super(serverUrl);
        this.productKey = productKey;
        this.productSecret = productSecret;
        this.deviceKey = deviceKey;
    }

    @Override
    public String getProductKey() {
        return productKey;
    }

    @Override
    public String getProductSecret() {
        return productSecret;
    }

    @Override
    public String getDeviceKey() {
        return deviceKey;
    }

    /**
     * No need for the device secret for dynamically activating the device.
     * Device secret would be returned from broker after this login.
     */
    @Override
    public String getDeviceSecret() {
        return null;
    }
}
