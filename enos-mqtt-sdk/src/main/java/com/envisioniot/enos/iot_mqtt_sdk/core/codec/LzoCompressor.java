package com.envisioniot.enos.iot_mqtt_sdk.core.codec;

import com.envisioniot.enos.iot_mqtt_sdk.core.utils.BytesUtil;
import lombok.extern.slf4j.Slf4j;
import org.anarres.lzo.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author mengyuantan
 */
@Slf4j
public class LzoCompressor implements ICompressor {
    @Override
    public byte[] compress(byte[] payload) throws IOException {
        try (EnosByteArrayOutputStream baos = new EnosByteArrayOutputStream();
             LzoOutputStream lzoOutputStream = new LzoOutputStream(baos,
                     LzoLibrary.getInstance().newCompressor(LzoAlgorithm.LZO1X, LzoConstraint.COMPRESSION))) {
            lzoOutputStream.write(payload);
            lzoOutputStream.close();
            return baos.toByteArray();
        }
    }

    @Override
    public byte[] decompress(byte[] payload) throws IOException {
        ByteArrayInputStream baIs = new ByteArrayInputStream(payload);
        LzoInputStream lzoInputStream = new LzoInputStream(baIs,
                LzoLibrary.getInstance().newDecompressor(LzoAlgorithm.LZO1X, LzoConstraint.COMPRESSION));

        return BytesUtil.getBytes(lzoInputStream);
    }
}
