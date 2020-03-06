package mqtt.old;

import com.envisioniot.enos.iot_mqtt_sdk.core.ConnCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutResponse;
import lombok.extern.slf4j.Slf4j;

import static mqtt.old.helper.Helper.*;

/**
 * Keep the sample running and we could see onConnectLost event happen from
 * time to time as we don't have write activity. Check (set debug point or
 * enable info level logs) that our sub-devices could auto login.
 *
 * @author jian.zhang4
 */
@Slf4j
public class SubDeviceAutoReconnectSample {

    public static void main(String[] args) throws Exception {
        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(SERVER_URL, GW_PRODUCT_KEY, GW_DEV_KEY, GW_DEV_SECRET)
        ));

        // enable auto connect
        client.getProfile()
                .setTimeToWait(3)
                .setConnectionTimeout(3)
                .setKeepAlive(30)
                .setAutoLoginSubDevice(true)
                .setAutoReconnect(true);

        client.connect(new ConnCallback() {
            @Override
            public void connectComplete(boolean reconnect) {
                log.info("connectComplete: reconnect={}", reconnect);

                if (!reconnect) {
                    // We need to do the sub-device login for the first time. And
                    // in the future, the sdk would do the auto-login for us.
                    loginSubDevices(client);
                }
            }

            @Override
            public void connectLost(Throwable cause) {
                log.info("connectLost", cause);
            }

            @Override
            public void connectFailed(Throwable cause) {
                log.info("connectFailed", cause);
            }
        });

        System.in.read();
        client.close();
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
