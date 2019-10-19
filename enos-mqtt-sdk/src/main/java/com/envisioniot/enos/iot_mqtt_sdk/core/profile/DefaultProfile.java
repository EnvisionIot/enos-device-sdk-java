package com.envisioniot.enos.iot_mqtt_sdk.core.profile;

import com.envisioniot.enos.iot_mqtt_sdk.core.login.LoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.util.SecureModeUtil;

/**
 * @author zhensheng.cai
 * @date 2019/1/3.
 */
public class DefaultProfile extends BaseProfile {

    public DefaultProfile(LoginInput input) {
        this(input.getServerUrl(), input.getProductKey(), input.getProductSecret(), input.getDeviceKey(), input.getDeviceSecret());
    }

    public DefaultProfile(String serverUrl, String productKey, String productSecret) {
        this(serverUrl, productKey, productSecret, SecureModeUtil.INTEGRATION_DK, null);
    }

    public DefaultProfile(String serverUrl, String productKey, String deviceKey, String deviceSecret) {
        this(serverUrl, productKey, null, deviceKey, deviceSecret);
    }

    public DefaultProfile(String serverUrl, String productKey, String productSecret, String deviceKey, String deviceSecret) {
        super();
        this.config.setServerUrl(serverUrl);
        this.config.setProductKey(productKey);
        this.config.setProductSecret(productSecret);
        this.config.setDeviceKey(deviceKey);
        this.config.setDeviceSecret(deviceSecret);
    }

    @Override
    public void reload() {
        //do nothing
    }
}
