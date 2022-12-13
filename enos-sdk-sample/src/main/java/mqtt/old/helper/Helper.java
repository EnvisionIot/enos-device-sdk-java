package mqtt.old.helper;

import com.envisioniot.enos.iot_mqtt_sdk.core.login.*;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DeviceCredential;
import com.google.common.collect.ImmutableList;

import java.util.List;

public class Helper {
    /**
     * EnOS MQTT Broker URL, which can be obtained from Environment Information page in EnOS Console
     */
    public static String SERVER_URL = "tcp://broker_url:11883";

    public static String ORG_ID = "orgId";

    /**
     * Non-gate-way product info (for direct connecting device and sub-device)
     */
    public static String DEV_PRODUCT_KEY = "pk";
    public static String DEV_PRODUCT_SECRET = "secret";

    /**
     * gate-way product info
     */
    public static String GW_PRODUCT_KEY = "pk";
    public static String GW_PRODUCT_SECRET = "secret";

    /**
     * device info for direct connecting device (non-gate-way device)
     */
    public static String DEV01_KEY = "dk";
    public static String DEV01_SECRET = "secret";

    public static String DEV02_KEY = "dk";
    public static String DEV02_SECRET = "secret";

    public static String DEV03_KEY = "dk";
    public static String DEV03_SECRET = "secret";

    /**
     * device info for gate-way and its sub-devices
     */
    public static String GW_DEV_KEY = "dk";
    public static String GW_DEV_SECRET = "secret";

    public static String SUB_DEV01_KEY = "dk";
    public static String SUB_DEV01_SECRET = "secret";

    public static String SUB_DEV02_KEY = "dk";
    public static String SUB_DEV02_SECRET = "secret";

    public final static List<DeviceCredential> SUBDEVICES = ImmutableList.of(
            new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET),
            new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV02_KEY, SUB_DEV02_SECRET)
    );

    /**
     * Product and device info for direct connecting
     */

    public static LoginInput getNormalDeviceLoginInput() {
        return new NormalDeviceLoginInput(SERVER_URL, DEV_PRODUCT_KEY, DEV01_KEY, DEV01_SECRET);
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
        return new MessageIntegrationLoginInput(SERVER_URL, DEV_PRODUCT_KEY, DEV_PRODUCT_SECRET);
    }

    public static LoginInput getVirtualGatewayLoginInput() {
        return new VirtualGatewayLoginInput(SERVER_URL, ORG_ID);
    }

}
