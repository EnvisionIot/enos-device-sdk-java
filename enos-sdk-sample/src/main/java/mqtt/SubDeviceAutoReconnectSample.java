package mqtt;

import static mqtt.helper.Helper.*;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutResponse;

import mqtt.helper.BaseConnectCallback;

/**
 * Keep the sample running and we could see onConnectLost event happen from
 * time to time as we don't have write activity. Check (set debug point or
 * enable info level logs) that our sub-devices could auto login.
 *
 * @author jian.zhang4
 */
public class SubDeviceAutoReconnectSample {
    public static void main(String[] args) throws Exception {
        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(SERVER_URL, GW_PRODUCT_KEY, GW_DEV_KEY, GW_DEV_SECRET)
        ));

        // enable auto connect
        client.getProfile().setKeepAlive(30).setAutoReconnect(true);

        client.connect(new BaseConnectCallback(client, "test sub-devices by manual login", false) {

            @Override
            public void onConnectSuccess() {
                System.out.println("onConnectSuccess: " + super.tip);
            }

            @Override
            public void onConnectLost() {
                System.out.println("onConnectLost: " + super.tip);
            }

            @Override
            public void onConnectFailed(int reasonCode) {
                System.err.println("onConnectFailed: " + super.tip);
            }
        });

        Thread.sleep(5 * 1000);

        loginSubDevices(client);

//      logoutSubDevices(client)
    }

    private static void loginSubDevices(final MqttClient client) {
        SUBDEVICES.forEach(dev -> {
            try {
                SubDeviceLoginRequest request = SubDeviceLoginRequest.builder().setSubDeviceInfo(dev).build();
                SubDeviceLoginResponse response = client.publish(request);
                if (response.isSuccess()) {
                    System.out.println("successfully logined sub-device: " + dev.getDeviceKey());
                } else {
                    System.err.println("failed to login sub-device: " + dev.getDeviceKey() + ", response: " + response);
                }
            } catch (EnvisionException e) {
                e.printStackTrace();
            }
        });
    }

    private static void logoutSubDevices(final MqttClient client) {
        SUBDEVICES.forEach(dev -> {
            try {
                SubDeviceLogoutRequest request =
                        SubDeviceLogoutRequest.builder()
                                .setSubProductKey(dev.getProductKey())
                                .setSubDeviceKey(dev.getDeviceKey())
                                .build();
                SubDeviceLogoutResponse response = client.publish(request);
                if (response.isSuccess()) {
                    System.out.println("successfully logged out sub-device: " + dev.getDeviceKey());
                } else {
                    System.err.println("failed to logout sub-device: " + dev.getDeviceKey() + ", response: " + response);
                }
            } catch (EnvisionException e) {
                e.printStackTrace();
            }
        });
    }
}
