package com.envisioniot.enos.iot_mqtt_sdk.core.compositejks;

import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;

public interface SslContextStep {

    SSLContext buildMergedWithSystem() throws GeneralSecurityException;

    default void buildMergedWithSystemAndSetDefault() throws GeneralSecurityException {
        SSLContext.setDefault(buildMergedWithSystem());
    }

}
