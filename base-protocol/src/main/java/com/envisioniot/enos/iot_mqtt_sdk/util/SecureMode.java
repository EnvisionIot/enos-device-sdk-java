package com.envisioniot.enos.iot_mqtt_sdk.util;

/**
 * @author jian.zhang4
 */
public class SecureMode {

    private final int modeId;

    /**
     * This would server as the mqtt client id
     */
    private final String clientId;

    /**
     * Secret that's used to generate the signature
     */
    private final String secret;

    public SecureMode(int modeId, String clientId, String secret) {
        this.modeId = modeId;
        this.clientId = clientId;
        this.secret = secret;
    }

    public int getModeId() {
        return modeId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getSecret() {
        return secret;
    }
}
