package com.envisioniot.enos.iot_mqtt_sdk.core.compositejks;

public interface KeyManagerAlgorithmStep {

    /**
     * @param keyManagerAlgorithm The algorithm for the custom key store. Defaults to system one, if null.
     */
    KeyManagerPasswordStep usingAlgorithm(String keyManagerAlgorithm);

    default KeyManagerPasswordStep usingDefaultAlgorithm() {
        return usingAlgorithm(null);
    }

    default KeyManagerPasswordStep usingSunX509() {
        return usingAlgorithm("SunX509");
    }
}
