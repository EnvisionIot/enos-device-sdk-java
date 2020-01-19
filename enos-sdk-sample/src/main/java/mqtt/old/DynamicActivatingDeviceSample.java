package mqtt.old;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.DynamicActivatingDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultActivateResponseHandler;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.FileProfile;
import com.envisioniot.enos.iot_mqtt_sdk.util.SecureModeUtil;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;
import com.google.common.base.Preconditions;

import mqtt.old.helper.BaseConnectCallback;
import mqtt.old.helper.Helper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

/**
 * Please refer to {@link DynamicActivatingDeviceLoginInput} for more details.
 *
 * @author jian.zhang4
 */
public class DynamicActivatingDeviceSample {

    private static String PRODUCT_KEY = "aVpQQTDp";
    private static String PRODUCT_SECRET = "aPmYSwVD5VI";

    // The status for the following device MUST be inactive for this test
    private static String DEVICE_KEY = "xD6pcvmzKI";

    /**
     * Reconnect to broker using the updated profile, which includes the
     * device secret returned from broker.
     *
     * @param newProfile profile with device secret populated
     */
    private static void reconnectWithNewProfile(FileProfile newProfile) {
        // This should use (pk, dk, device_secret) to login.
        if (newProfile.getSecureMode().getModeId() != SecureModeUtil.VIA_DEVICE_SECRET) {
            System.err.println("Error: not VIA_DEVICE_SECRET mode");
            return;
        }

        System.out.println("---------------------------------------");

        final MqttClient newClient = new MqttClient(newProfile);
        newClient.connect(new BaseConnectCallback(newClient, "reconnect using VIA_DEVICE_SECRET", true));
    }

    /**
     * Check that device secret should have been populated into profile
     * when DynamicActivatingDeviceLoginInput is employed (see more details
     * in class {@link DefaultActivateResponseHandler}.
     *
     * @param client      the MqttClient instance that need to be cleaned later.
     * @param oldProfile  old profile that's used for dynamic activating device.
     * @param profilePath profile file path
     */
    private static void checkProfile(final MqttClient client, FileProfile oldProfile, String profilePath) {
        // Wait a moment to let config be updated
        try {
            for (int i = 1; i < 60; ++i) {
                if (!StringUtil.isEmpty(oldProfile.getDeviceSecret())) {
                    // device secret populated by DefaultActivateResponseHandler
                    break;
                }
                TimeUnit.SECONDS.sleep(1);
            }

            // wait DefaultActivateResponseHandler to complete replying broker and reconnecting to it
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Reload the profile config
        FileProfile newProfile = new FileProfile(profilePath);
        if (StringUtil.isEmpty(newProfile.getDeviceSecret())) {
            String error = StringUtil.isEmpty(oldProfile.getDeviceSecret())
                    ? "device secret is not returned from broker"
                    : "device secret is returned from broker but not persisted";
            System.err.println("Error: " + error);
        } else {
            System.out.println("received device secret: " + newProfile.getDeviceSecret());

            // Ensure that connection is still alive after DefaultActivateResponseHandler reconnected to broker
            if (!client.isConnected()) {
                System.err.println("Error: connection disconnected during dynamic activating");
            }
        }

        Helper.cleanConnection(client);

        // Test reconnect with updated profile
        if (!StringUtil.isEmpty(newProfile.getDeviceSecret())) {
            reconnectWithNewProfile(newProfile);
        }
    }

    public static void main(String[] args) throws Exception {
        final Path configPath = Files.createTempFile("dy-activate", "config");
        final String absPath = configPath.toAbsolutePath().toString();
        System.out.println("profile abs path: " + absPath);

        /**
         * Here we login the new device through DynamicActivatingDeviceLoginInput
         */
        FileProfile profile = new FileProfile(absPath, Helper.getDynamicActivatingDeviceLoginInput(PRODUCT_KEY, PRODUCT_SECRET, DEVICE_KEY));
        profile.persist();

        Preconditions.checkArgument(StringUtil.isEmpty(profile.getDeviceSecret()),
                "device secret MUST be empty before dynamically activating the device");

        final MqttClient client = new MqttClient(profile);
        client.connect(new BaseConnectCallback(client, "dynamic activating device") {
            @Override
            protected void onSuccess(MqttClient client) {
                checkProfile(client, profile, configPath.toAbsolutePath().toString());
            }
        });
    }


}
