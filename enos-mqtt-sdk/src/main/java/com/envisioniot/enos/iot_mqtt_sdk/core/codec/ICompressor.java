package com.envisioniot.enos.iot_mqtt_sdk.core.codec;

import java.io.IOException;

/**
 * @author mengyuantan
 */
public interface ICompressor {

    /**
     * @param payload
     * @return
     */
    public byte[] compress(byte[] payload) throws IOException;

    /**
     * @param payload
     * @return
     */
    public byte[] decompress(byte[] payload) throws IOException;
}
