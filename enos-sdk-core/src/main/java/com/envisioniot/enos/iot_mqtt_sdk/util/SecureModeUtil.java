package com.envisioniot.enos.iot_mqtt_sdk.util;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * @author zhensheng.cai
 * @date 2019/1/15.
 */
public class SecureModeUtil {
    public static final int VIA_DEVICE_SECRET                  = 2;
    public static final int VIA_PRODUCT_SECRET                 = 3;
    public static final int VIA_PRODUCT_SECRET_FOR_INTEGRATION = 4;

    public final static String INTEGRATION_DK = "%channel%";

    public static SecureMode getSecureMode(String productKey, String productSecret, final String deviceKey, String deviceSecret) {

        if (StringUtil.isNotEmpty(deviceSecret)) {
            return new SecureMode(VIA_DEVICE_SECRET, deviceKey, deviceSecret);
        }

        if (StringUtil.isNotEmpty(productSecret)) {
            if (INTEGRATION_DK.equals(deviceKey)) {
                return new SecureMode(VIA_PRODUCT_SECRET_FOR_INTEGRATION, productKey, productSecret);
            }
            return new SecureMode(VIA_PRODUCT_SECRET, deviceKey, productSecret);
        }

        throw new IllegalArgumentException("deviceSecret or productSecret should be provided");
    }

    private static String getClientIp() {
        try {
            Inet4Address ip = (Inet4Address) InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (UnknownHostException e) {
            return "";
        }
    }

    private static String getClientVersion(String key) {
        try (InputStream in = SecureModeUtil.class.getResourceAsStream("/version.properties")) {
            Properties prop = new Properties();
            prop.load(in);
            return prop.getProperty(key);
        } catch (Throwable e) {
            return "";
        }
    }
}