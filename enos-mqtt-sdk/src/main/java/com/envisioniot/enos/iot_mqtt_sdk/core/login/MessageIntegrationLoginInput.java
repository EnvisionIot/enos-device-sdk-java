package com.envisioniot.enos.iot_mqtt_sdk.core.login;

/**
 * This input is only for message integration (namely post numerous historical
 * messages from multiple devices to broker in batch through one communication
 * channel verified by this login input). <br/>
 * <br/>
 * When you use this login mode, please ensure that you have created message
 * integration channel for the product your're using (e.g through portal ui).
 *
 * @author jian.zhang4
 */
public class MessageIntegrationLoginInput extends LoginInput {
    // hard-coded device key that's used to identify integration login
    public final static String INTEGRATION_DK = "%channel%";

    private final String productKey;
    private final String productSecret;

    public MessageIntegrationLoginInput(String serverUrl, String productKey, String productSecret) {
        super(serverUrl);
        this.productKey = productKey;
        this.productSecret = productSecret;
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
        return INTEGRATION_DK;
    }

    /**
     * No need for the device secret
     */
    @Override
    public String getDeviceSecret() {
        return null;
    }
}
