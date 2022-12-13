package mqtt.old;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.LoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.google.common.collect.ImmutableList;
import mqtt.old.helper.BaseConnectCallback;
import mqtt.old.helper.Helper;

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

        // dynamic activating device login wait for a few seconds to handle the msg DeviceActivateInfoCommand
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        client.close();
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

    /**
     * test login using blocking way
     * @param input specified login input mode by user
     */
    private static void testDynamicActivatingSyncLogin(LoginInput input) {
        System.out.println("[" + input.getClass().getSimpleName() + "] test started");

        syncLogin(input);
    }

    /**
     * test login using non-blocking way
     * @param input  specified login input mode by user
     */
    private static void testDynamicActivatingAsyncLogin(LoginInput input) {
        asyncLogin(input);

        try {
            // wait for a few seconds to let async login complete
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            // ignore
        }

        System.out.println("[" + input.getClass().getSimpleName() + "] test completed\n\n");
    }

    /**
     * Test four login modes we have right now
     * Details about the four login modes:
     *  {@link com.envisioniot.enos.iot_mqtt_sdk.core.login.DynamicActivatingDeviceLoginInput}
     *  {@link com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput}
     *  {@link com.envisioniot.enos.iot_mqtt_sdk.core.login.MessageIntegrationLoginInput}
     *  {@link com.envisioniot.enos.iot_mqtt_sdk.core.login.VirtualGatewayLoginInput}
     */
    public static void main(String[] args) {
        // Dynamically activate device login
        // Note that for this login mode to work, it must:
        // 1. The device product has dynamic activation enabled
        // 2. The device is logged in using this mode for the first time.If the device has already logged in (i.e. activated) using this mode,
        //    it will fail if it is used again (mainly for security reasons of passing device secrets).
        testDynamicActivatingSyncLogin(Helper.getDynamicActivatingDeviceLoginInput(Helper.DEV_PRODUCT_KEY, Helper.DEV_PRODUCT_SECRET, Helper.DEV02_KEY));
        testDynamicActivatingAsyncLogin(Helper.getDynamicActivatingDeviceLoginInput(Helper.DEV_PRODUCT_KEY, Helper.DEV_PRODUCT_SECRET, Helper.DEV03_KEY));

        List<LoginInput> inputs = ImmutableList.of(
                Helper.getNormalDeviceLoginInput(),
                Helper.getMessageIntegrationLoginInput(),
                Helper.getVirtualGatewayLoginInput()
        );

        inputs.forEach(DeviceLoginSample::testLoginInput);
    }
}
