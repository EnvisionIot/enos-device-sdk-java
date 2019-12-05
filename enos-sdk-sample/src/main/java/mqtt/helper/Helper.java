package mqtt.helper;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.*;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DeviceCredential;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class Helper {
    public static String SERVER_URL = "tcp://10.27.21.6:11883"; // alpha

    public static String ORG_ID = "o15444172373271";

    /**
     * Non-gate-way product info (for direct connecting device and sub-device)
     */
    public static String PRODUCT_KEY = "aVpQQTDp";
    public static String PRODUCT_SECRET = "aPmYSwVD5VI";

    /**
     * gate-way product info
     */
    public static String GW_PRODUCT_KEY = "SvUMZuEx";
    public static String GW_PRODUCT_SECRET = "ybeQ0GsFllm";

    /**
     * device info for direct connecting device (non-gate-way device)
     */
    public static String DEV01_KEY = "xD6pcvmzKI";
    public static String DEV01_SECRET = "0nwcesI7PCooHd14aVal";

    public static String DEV02_KEY = "Vs9Upjt91B";
    public static String DEV02_SECRET = "nPXHyVv9zFkiu8k0WUec";

    /**
     * device info for gate-way and its sub-devices
     */
    public static String GW_DEV_KEY = "uz6ZJIPs2v";
    public static String GW_DEV_SECRET = "MwXMS18qTslA7MIbaTlK";

    public static String SUB_DEV01_KEY = "byUB2HHfCL";
    public static String SUB_DEV01_SECRET = "O1woxf2vVE7VTVsGk53k";

    public static String SUB_DEV02_KEY = "batch_device_01";
    public static String SUB_DEV02_SECRET = "B5rx6YQqENzr08eH8AZQ";

    public final static List<DeviceCredential> SUBDEVICES = ImmutableList.of(
            new DeviceCredential(PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET),
            new DeviceCredential(PRODUCT_KEY, null, SUB_DEV02_KEY, SUB_DEV02_SECRET)
    );

    /**
     * Product and device info for direct connecting
     */

    public static LoginInput getNormalDeviceLoginInput() {
        return new NormalDeviceLoginInput(SERVER_URL, PRODUCT_KEY, DEV01_KEY, DEV01_SECRET);
    }

    /**
     * For each dynamic activating test, the following args needed to be updated to a device
     * that's newly created (whose product is enabled with Dynamic Activating). <br/>
     * <br/>
     * Please refer to {@link DynamicActivatingDeviceLoginInput} for more details about how to use it.
     */
    public static LoginInput getDynamicActivatingDeviceLoginInput(String dyPK, String dyProdSecret, String dyDK) {
        return new DynamicActivatingDeviceLoginInput(SERVER_URL, dyPK, dyProdSecret, dyDK);
    }

    /**
     * Please refer to {@link MessageIntegrationLoginInput} for more details about how to use it.
     */
    public static LoginInput getMessageIntegrationLoginInput() {
        return new MessageIntegrationLoginInput(SERVER_URL, PRODUCT_KEY, PRODUCT_SECRET);
    }

    public static LoginInput getVirtualGatewayLoginInput() {
        return new VirtualGatewayLoginInput(SERVER_URL, ORG_ID);
    }

    public static void cleanConnection(MqttClient client) {
        if (client != null) {
            try {
                if (client.isConnected()) {
                    client.disconnect();
                }
                client.close();
            } catch (EnvisionException e) {
                e.printStackTrace();
            }
        }
    }
}
