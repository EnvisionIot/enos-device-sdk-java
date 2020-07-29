package com.envisioniot.enos.iot_mqtt_sdk.core;

import com.envisioniot.enos.iot_mqtt_sdk.util.SecureModeUtil;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;

import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

/**
 * @Author: song.xu
 * @Date: 2020/7/15
 */
public class ClientInfoUtil {

    public static String getClientIp() {
        try {
            Inet4Address ip = (Inet4Address) InetAddress.getLocalHost();
            return ip.getHostAddress();
        } catch (UnknownHostException e) {
            return "";
        }
    }

    public static String getClientVersion() {
        try (InputStream in = SecureModeUtil.class.getResourceAsStream("/version.properties")) {
            Properties prop = new Properties();
            prop.load(in);
            return prop.getProperty("version");
        } catch (Throwable e) {
            return "";
        }
    }
}
