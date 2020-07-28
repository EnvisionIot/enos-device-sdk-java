package com.envisioniot.enos.iot_mqtt_sdk.core.codec;

import com.envisioniot.enos.iot_mqtt_sdk.core.utils.BytesUtil;
import lombok.extern.slf4j.Slf4j;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author mengyuantan
 */
@Slf4j
public class Lz4Compressor implements ICompressor {

    @Override
    public byte[] compress(byte[] payload) throws IOException {
        try (EnosByteArrayOutputStream baos = new EnosByteArrayOutputStream();
             LZ4BlockOutputStream lz4BlockOutputStream = new LZ4BlockOutputStream(baos)) {
            lz4BlockOutputStream.write(payload);
            lz4BlockOutputStream.close();
            return baos.toByteArray();
        }
    }

    @Override
    public byte[] decompress(byte[] payload) throws IOException {
        ByteArrayInputStream baIs = new ByteArrayInputStream(payload);
        LZ4BlockInputStream lz4BlockInputStream = new LZ4BlockInputStream(baIs);

        return BytesUtil.getBytes(lz4BlockInputStream);
    }
}
