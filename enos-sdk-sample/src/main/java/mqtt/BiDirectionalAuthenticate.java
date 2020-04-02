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
    private static String productKey = "productKey";
    private static String deviceKey = "deviceKey";
    private static String deviceSecret = "deviceSecret";

    // The JKS needs to store the device private key, the device certificate 
    // applied from the EnOS platform, and the EnOS platform CA certificate
    // To learn how to acquire these certificates, see 
    // https://support-cn5.envisioniot.com/docs/device-connection/en/latest/learn/deviceconnection_authentication.html#certificate-based-authentication
    private static String jksPath = "jskPath";
    private static String jksPassword = "jskPassword";

    /**
     * protocol list : ssl,wss
     * IpAddress : can domain name or ip address
     * Port list:  18883,18885
     */
    private static String protocol = "ssl";
    /**
     * EnOS HTTP Broker URL, which can be obtained from Environment Information page in EnOS Console
     */
    private static String IpAddress = "broker_domain_url";
    private static String port = "18883";

    public static void main(String[] args) {
        DefaultProfile defaultProfile = new DefaultProfile(protocol + "://" + IpAddress + ":" + port,
                productKey,
                deviceKey,
                deviceSecret);
        defaultProfile
                .setConnectionTimeout(60)
                .setKeepAlive(180)
                .setAutoReconnect(false)
                .setSSLSecured(true)
                .setSSLJksPath(jksPath, jksPassword);
        
        // if use ECC certificate 
        // defaultProfile.setEccConnect(true);

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
