package com.envisioniot.enos.iot_mqtt_sdk.core.utils;

import com.envisioniot.enos.iot_mqtt_sdk.core.codec.EnosByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author mengyuantan
 */
public class BytesUtil {

    public static byte[] getBytes(InputStream is) throws IOException {
        try (EnosByteArrayOutputStream baos = new EnosByteArrayOutputStream()) {
            for (int b = is.read(); b >= 0; b = is.read()) {
                baos.write(b);
            }
            return baos.toByteArray();
        }
    }

}
