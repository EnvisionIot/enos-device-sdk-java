package mqtt;

import com.envisioniot.enos.iot_mqtt_sdk.core.ConnCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import lombok.extern.slf4j.Slf4j;

/**
 * @author qiwei.tan
 * @version 1.0
 * @program enos-iot-sdk-parent
 * @ClassName DeviceSSLLoginSample
 * @Description device ssl connect example
 * @date 2020/2/11 10:21
 */
@Slf4j
public class BiDirectionalAuthenticate {
    /**
     * Gateway credentials, which can be obtained from Device Details page in EnOS Console
     */
    private static final String PRODUCT_KEY = "productKey";
    private static final String DEVICE_KEY = "deviceKey";
    private static final String DEVICE_SECRET = "deviceSecret";

    // The JKS needs to store the device private key, the device certificate 
    // applied from the EnOS platform, and the EnOS platform CA certificate
    // To learn how to acquire these certificates, see 
    // https://support-cn5.envisioniot.com/docs/device-connection/en/latest/learn/deviceconnection_authentication.html#certificate-based-authentication
    private static final String JKS_PATH = "jskPath";
    private static final String JKS_PASSWORD = "jskPassword";

    /**
     * PROTOCOL list : ssl,wss
     * Port list:  18883,18885
     * EnOS MQTT Broker URL, which can be obtained from Environment Information page in EnOS Console
     */
    private static final String BROKER_URL = "ssl://broker_url:18883";

    public static void main(String[] args) {
        DefaultProfile defaultProfile = new DefaultProfile(BROKER_URL,
                PRODUCT_KEY,
                DEVICE_KEY,
                DEVICE_SECRET);
        defaultProfile
                .setConnectionTimeout(60)
                .setKeepAlive(180)
                .setAutoReconnect(false)
                .setSSLSecured(true)
                .setSSLJksPath(JKS_PATH, JKS_PASSWORD);

        // if use ECC certificate 
        // defaultProfile.setEccConnect(true);

        /*
         * If bi-directional authentication is enabled, the domain name of the server certificate is not verified by default.
         * You can manually enable the verification as required.
         */
        // defaultProfile.setHostnameVerifyEnabled(true);

        final MqttClient mqttClient = new MqttClient(defaultProfile);
        mqttClient.connect(new ConnCallback() {
            @Override
            public void connectComplete(boolean reconnect) {
                log.info("connectComplete");
            }

            @Override
            public void connectLost(Throwable cause) {
                log.error("connectLost", cause);
            }

            @Override
            public void connectFailed(Throwable cause) {
                log.error("connectFailed", cause);
            }
        });
    }


}
