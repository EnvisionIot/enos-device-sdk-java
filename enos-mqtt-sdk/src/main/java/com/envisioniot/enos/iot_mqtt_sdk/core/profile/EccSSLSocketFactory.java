package com.envisioniot.enos.iot_mqtt_sdk.core.profile;

import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.internal.security.SSLSocketFactoryFactory;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * @author qiwei.tan
 * @version 1.0
 * @program enos-iot-sdk-parent
 * @ClassName EccSSLSocketFactory
 * @Description Ecc connection support
 * @date 2020/2/11 9:28
 */
public class EccSSLSocketFactory extends SSLSocketFactory {
    private SSLSocketFactory factory;

    EccSSLSocketFactory(SSLSocketFactory factory) {
        if (factory == null) {
            try {
                factory = new SSLSocketFactoryFactory().createSocketFactory(null);
            } catch (MqttSecurityException e) {
                throw new RuntimeException("don't not create SSLSocketFactory!");
            }
        }
        this.factory = factory;

    }

    @Override
    public String[] getDefaultCipherSuites() {
        return new String[]{"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"};
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return new String[]{"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384"};
    }

    @Override
    public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException {
        return encapsulated(factory.createSocket(socket, s, i, b));
    }

    private Socket encapsulated(Socket socket) {
        if (socket instanceof SSLSocket) {
            ((SSLSocket) socket).setEnabledCipherSuites(getDefaultCipherSuites());
        }
        return socket;
    }

    @Override
    public Socket createSocket(String s, int i) throws IOException {
        return encapsulated(factory.createSocket(s, i));
    }

    @Override
    public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException {
        return encapsulated(factory.createSocket(s, i, inetAddress, i1));
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
        return encapsulated(factory.createSocket(inetAddress, i));
    }

    @Override
    public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
        return encapsulated(factory.createSocket(inetAddress, i, inetAddress1, i1));
    }
}
