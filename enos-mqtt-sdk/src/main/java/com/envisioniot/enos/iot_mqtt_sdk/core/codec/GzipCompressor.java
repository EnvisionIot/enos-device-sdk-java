package com.envisioniot.enos.iot_mqtt_sdk.core.codec;

import com.envisioniot.enos.iot_mqtt_sdk.core.utils.BytesUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author mengyuantan
 */
@Slf4j
public class GzipCompressor implements ICompressor {

    @Override
    public byte[] compress(byte[] payload) throws IOException {
        try (EnosByteArrayOutputStream baos = new EnosByteArrayOutputStream();
             GZIPOutputStream gzipOutputStream = new GZIPOutputStream(baos, true)) {
            gzipOutputStream.write(payload);
            gzipOutputStream.close();
            return baos.toByteArray();
        }
    }

    @Override
    public byte[] decompress(byte[] payload) throws IOException {
        ByteArrayInputStream baIs = new ByteArrayInputStream(payload);
        GZIPInputStream gzipInputStream = new GZIPInputStream(baIs);

        return BytesUtil.getBytes(gzipInputStream);
    }
}
