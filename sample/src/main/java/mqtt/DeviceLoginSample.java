package mqtt;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.*;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.google.common.collect.ImmutableList;

import mqtt.helper.BaseConnectCallback;
import mqtt.helper.Helper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This sample shows how to login device using different ways.
 *
 * @author jian.zhang4
 */
public class DeviceLoginSample {

    /**
     * Test device login mode using sync way (blocking way).
     *
     * @param loginInput specified login input mode by user
     */
    private static void syncLogin(LoginInput loginInput) {
        MqttClient client = null;

        try {
            client = new MqttClient(new DefaultProfile(loginInput));

            // Use SHA1 for sync login
            client.getProfile().setSignMethod(SignMethod.SHA1);

            long start = System.currentTimeMillis();
            client.connect();

            if (client.isConnected()) {
                System.out.println("[sync] successfully logined (took " + (System.currentTimeMillis() - start) + "ms)");
            } else {
                System.err.println("[sync] failed to login");
            }
        } catch (EnvisionException e) {
            System.err.println("[sync] hit exception " + e.getErrorMessage() + " during login");
            e.printStackTrace();
        }

        Helper.cleanConnection(client);
    }

    /**
     * Test device login mode using async way (non-blocking way).
     *
     * @param loginInput specified login input mode by user
     */
    private static void asyncLogin(final LoginInput loginInput) {
        final MqttClient client = new MqttClient(new DefaultProfile(loginInput));

        // Use SHA256 for sync login
        client.getProfile().setSignMethod(SignMethod.SHA256);

        client.connect(new BaseConnectCallback(client, "[asyn]", true));

        // Client connection may not be ready here. User should do
        // the check in callback before using it.
        System.out.println("[asyn] connection ready: " + client.isConnected());
    }

    private static void testLoginInput(LoginInput input) {
        System.out.println("[" + input.getClass().getSimpleName() + "] test started");

        // 1. test login using blocking way
        syncLogin(input);

        // 2. test login using non-blocking way
        asyncLogin(input);

        try {
            // wait for a few seconds to let async login complete
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            // ignore
        }

        System.out.println("[" + input.getClass().getSimpleName() + "] test completed\n\n");
    }

    public static void main(String[] args) {
        // Test four login modes we have right now
        List<LoginInput> inputs = ImmutableList.of(
                Helper.getDynamicActivatingDeviceLoginInput(Helper.PRODUCT_KEY, Helper.PRODUCT_SECRET, Helper.DEV01_KEY),
                Helper.getNormalDeviceLoginInput(),
                Helper.getMessageIntegrationLoginInput(),
                Helper.getVirtualGatewayLoginInput()
        );

        inputs.forEach(DeviceLoginSample::testLoginInput);
    }
}
