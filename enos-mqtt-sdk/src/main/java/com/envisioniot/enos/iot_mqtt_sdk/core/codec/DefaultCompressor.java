package com.envisioniot.enos.iot_mqtt_sdk.core.codec;

public class DefaultCompressor implements ICompressor {

    @Override
    public byte[] compress(byte[] payload) {
        return payload;
    }

    @Override
    public byte[] decompress(byte[] payload) {
        return payload;
    }
}
