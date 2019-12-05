package com.envisioniot.enos.iot_mqtt_sdk.core.login;

/**
 * This login mode is mainly for testing. Under this mode, all devices
 * can login as sub-device of a virtual gate-way device. Note that
 * virtual gate-way is identified by org and we only allow one such
 * gate-way connecting to broker for each org (new connection would
 * kick off the old one).
 *
 * @author jian.zhang4
 */
public class VirtualGatewayLoginInput extends LoginInput {
    // hard-coded device key that's used to identify virtual gate-way login
    public static final String VIRT_GATEWAY_PK = "%VIRT_GATEWAY%";

    private final String orgId;

    public VirtualGatewayLoginInput(String serverUrl, String orgId) {
        super(serverUrl);
        this.orgId = orgId;
    }

    @Override
    public String getProductKey() {
        return VIRT_GATEWAY_PK;
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
        return orgId;
    }

    /**
     * We need to provide a fake device secret here to pass
     * existing verification. This can be any value.
     */
    @Override
    public String getDeviceSecret() {
        return "*";
    }
}
