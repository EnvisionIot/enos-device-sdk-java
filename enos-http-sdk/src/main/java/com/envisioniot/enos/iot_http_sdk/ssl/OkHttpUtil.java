package com.envisioniot.enos.iot_http_sdk.ssl;

import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import static com.envisioniot.enos.iot_http_sdk.HttpConnectionError.CLIENT_ERROR;

/**
 * @author :charlescai
 * @date :2021-02-26
 */
public class OkHttpUtil {

    private static final int CONNECT_TIMEOUT = 10;

    private static final int READ_TIMEOUT = 120;

    private static final int WRITE_TIMEOUT = 120;

    /**
     * generate ok http client
     * @param sslSecured
     * @param jksPath
     * @param password
     */
    public static OkHttpClient generateHttpsClient(boolean sslSecured, boolean isEccConnect, String jksPath, String password) throws EnvisionException {

        try {
            OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder()
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(false);

            if (!sslSecured) {
                return okHttpBuilder.build();
            }

            if (jksPath == null) {
                return buildDefaultOkHttpsClient(okHttpBuilder);
            } else {
                return buildOkHttpsClient(isEccConnect, jksPath, password, okHttpBuilder);
            }
        } catch (Exception e) {
            throw new EnvisionException(e, CLIENT_ERROR);
        }

    }

    @NotNull
    private static OkHttpClient buildOkHttpsClient(boolean isEccConnect, String jksPath, String password, OkHttpClient.Builder okHttpBuilder) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
        FileInputStream caInputStream = new FileInputStream(jksPath);

        KeyStore keyStore = getKeyStore(caInputStream, password);
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password.toCharArray());
        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        X509TrustManager trustManagerVerifyCa = (X509TrustManager) trustManagers[0];
        // TSL or SSL
        SSLContext sslContext = SSLContext.getInstance("TLS");

        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates,
                                                   String s) throws CertificateException {}
                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates,
                                                   String s) throws CertificateException {}
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        sslContext.init(keyManagers, trustAllCerts, new SecureRandom());
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        sslSocketFactory = new EccSSLSocketFactory(sslSocketFactory, isEccConnect);

        // check cert
        okHttpBuilder.sslSocketFactory(sslSocketFactory, trustManagerVerifyCa);
        okHttpBuilder.hostnameVerifier((String hostname, SSLSession session) -> true);
        return okHttpBuilder.build();
    }

    @NotNull
    private static OkHttpClient buildDefaultOkHttpsClient(OkHttpClient.Builder okHttpBuilder) throws NoSuchAlgorithmException, KeyManagementException {
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) {
                // not check
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{trustManager}, new SecureRandom());
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        okHttpBuilder.sslSocketFactory(sslSocketFactory, trustManager);

        okHttpBuilder.hostnameVerifier((String hostname, SSLSession session) -> true);
        return okHttpBuilder.build();
    }


    private static KeyStore getKeyStore(InputStream caInputStream, String password)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        // default key store
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(caInputStream, password.toCharArray());
        return keyStore;
    }
}

