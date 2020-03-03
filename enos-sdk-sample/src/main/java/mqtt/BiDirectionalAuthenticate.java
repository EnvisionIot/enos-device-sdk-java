package mqtt;

import com.envisioniot.enos.iot_mqtt_sdk.core.IConnectCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;

/**
 * @author qiwei.tan
 * @version 1.0
 * @program enos-iot-sdk-parent
 * @ClassName DeviceSSLLoginSample
 * @Description device ssl connect example
 * @date 2020/2/11 10:21
 */
public class BiDirectionalAuthenticate {
    /**
     * Gateway credentials, which can be obtained from Device Details page in EnOS Console
     */
    private static String productKey = "productKey";
    private static String deviceKey = "deviceKey";
    private static String deviceSecret = "deviceSecret";

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
        defaultProfile.setConnectionTimeout(60).setKeepAlive(180).setAutoReconnect(false).setSSLSecured(true)
                .setSSLJksPath(jksPath, jksPassword);
        
        // if use ECC certificate 
        // defaultProfile.setEccConnect(true);

        MqttClient mqttClient = new MqttClient(defaultProfile);
        mqttClient.connect(new IConnectCallback() {

            @Override
            public void onConnectSuccess() {
                System.out.println("connect success");
            }

            @Override
            public void onConnectLost() {
                System.out.println("onConnectLost!");
            }

            @Override
            public void onConnectFailed(int i) {
                System.out.println("onConnectFailed : " + i);
            }
        });
    }


}
