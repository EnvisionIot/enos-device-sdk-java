package com.envisioniot.enos.iot_mqtt_sdk.core;

import com.envisioniot.enos.iot_mqtt_sdk.util.SecureModeUtil;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Properties;

/**
 * @Author: song.xu @Date: 2020/7/15
 */
public class ClientInfoUtil {

    public static String getClientIp() {
        final InetAddress inetAddress = getInetAddress();
        return Objects.nonNull(inetAddress) ? inetAddress.getHostAddress() : "";
    }

    @Nullable
    private static InetAddress getInetAddress() {
        try {
            InetAddress candidateAddress = null;

            final Enumeration<NetworkInterface> networkInterfaces =
                    NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                final NetworkInterface networkInterface = networkInterfaces.nextElement();
                final Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    final InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        if (inetAddress.isSiteLocalAddress()) {
                            return inetAddress;
                        }

                        if (Objects.isNull(candidateAddress)) {
                            candidateAddress = inetAddress;
                        }
                    }
                }
            }

            return Objects.isNull(candidateAddress) ? InetAddress.getLocalHost() : candidateAddress;
        } catch (Throwable t) {
            return null;
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
