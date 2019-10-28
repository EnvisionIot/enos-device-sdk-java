package com.envisioniot.enos.iot_mqtt_sdk.core.compositejks;

import javax.net.ssl.KeyManagerFactory;

public interface KeyManagerPasswordStep {

    /**
     * @param keyManagerPwd The password to init {@link KeyManagerFactory}. Defaults to KeyStore password, if null.
     */
    SslContextStep usingKeyManagerPassword(String keyManagerPwd);

    default SslContextStep usingKeyManagerPasswordFromKeyStore() {
        return usingKeyManagerPassword(null);
    }

}
