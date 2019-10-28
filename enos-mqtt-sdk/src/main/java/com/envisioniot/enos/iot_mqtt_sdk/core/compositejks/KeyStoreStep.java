package com.envisioniot.enos.iot_mqtt_sdk.core.compositejks;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public interface KeyStoreStep {

    ProtocolStep keyStoreFromStream(InputStream stream, String password) throws IOException, GeneralSecurityException;

    default ProtocolStep keyStoreFromStream(InputStream stream) throws IOException, GeneralSecurityException {
        return keyStoreFromStream(stream, null);
    }

    default ProtocolStep keyStoreFromFile(String path, String password) throws IOException, GeneralSecurityException {
        try (FileInputStream stream = new FileInputStream(path)) {
            return keyStoreFromStream(stream, password);
        }
    }

    default ProtocolStep keyStoreFromFile(String path) throws IOException, GeneralSecurityException {
        return keyStoreFromFile(path, null);
    }
}
