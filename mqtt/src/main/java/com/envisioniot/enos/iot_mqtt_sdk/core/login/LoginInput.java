package com.envisioniot.enos.iot_mqtt_sdk.core.login;

/**
 * For different login ways, they have different requirements for the info
 * needed. Here we list the super set of all login info for authentication
 * in this interface and allow for sub-class to customize it.
 *
 * @author jian.zhang4
 */
public abstract class LoginInput {
    private final String serverUrl;

    public LoginInput(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getServerUrl() {
        return this.serverUrl;
    }

    public abstract String getProductKey();

    public abstract String getProductSecret();

    public abstract String getDeviceKey();

    public abstract String getDeviceSecret();

}
