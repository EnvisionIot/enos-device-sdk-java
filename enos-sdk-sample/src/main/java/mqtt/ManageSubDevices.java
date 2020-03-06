package mqtt;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.register.DeviceBasicInfo;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.register.DeviceRegisterRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.register.DeviceRegisterResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.topo.TopoAddRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.topo.TopoAddResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostResponse;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringI18n;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class ManageSubDevices {
    // EnOS HTTP Broker URL, which can be obtained from Environment Information page in EnOS Console
    static final String BROKER_URL = "tcp://broker_url:11883";

    // Gateway credentials, which can be obtained from Device Details page in EnOS Console
    static final String GW_PRODUCT_KEY = "productKey";
    static final String GW_DEVICE_KEY = "deviceKey";
    static final String GW_DEVICE_SECRET = "deviceSecret";

    // Sub-device product key, which can be obtained from Product Details page in EnOS Console
    static final String SUB_DEVICE_PRODUCT_KEY = "subProductKey";

    static String subDeviceKey;
    static String subDeviceSecret;

    static MqttClient client;

    public static void main(String[] args) throws Exception {
        // construct an MQTT client by static device credential
        // BROKER_URL is the URL of EnOS MQTT Broker for Devices, which can be obtained in Environment Information page in EnOS Console
        // ProductKey, DeviceKey and DeviceSecrect can be obtained in Device Details page in EnOS Console
        client = new MqttClient(BROKER_URL, GW_PRODUCT_KEY, GW_DEVICE_KEY, GW_DEVICE_SECRET);

        // enable auto-reconnect feature
        client.getProfile().setConnectionTimeout(60).setAutoReconnect(true);

        // connect to EnOS Cloud and register callbacks. onConnectSuccess method will be called 
        client.connect();

        try {
            // register a new device
            registerDevice();

            // bind the device to current gateway
            addTopology();

            // log in the sub-device
            loginSubDevice();

            int loop = 10000;
            while ((--loop) >= 0) {
                // post measurepoint for the sub-device        
                postMeasurepointForSubDevice();
                TimeUnit.SECONDS.sleep(1L);
            }
        } catch (EnvisionException e) {
            e.printStackTrace();
        }

        // disconnect EnOS Cloud, the sub-device will be automatically log off
        try {
            client.disconnect();
        } catch (EnvisionException e) {
            e.printStackTrace();
        }
    }

    // Sample: register device
    static void registerDevice() throws EnvisionException {
        DeviceRegisterRequest request = DeviceRegisterRequest.builder()
                .addRegisterInfo(SUB_DEVICE_PRODUCT_KEY, null, new StringI18n("sample_dev_01"),
                        "dev desc", "+08:00")
                .build();

        DeviceRegisterResponse response = client.publish(request);
        System.out.println(response);
        if (response.getCode() == 200) {
            DeviceBasicInfo info = response.getDeviceBasicInfoList().iterator().next();
            subDeviceKey = info.deviceKey;
            subDeviceSecret = info.deviceSecret;
        }
    }

    // Sample: add topology
    static void addTopology() throws EnvisionException {
        TopoAddRequest request = TopoAddRequest.builder()
                .addSubDevice(SUB_DEVICE_PRODUCT_KEY, subDeviceKey, subDeviceSecret)
                .build();

        TopoAddResponse response = client.publish(request);
        System.out.println(response);
    }

    // Sample: login sub-device
    static void loginSubDevice() throws EnvisionException {
        SubDeviceLoginRequest request = SubDeviceLoginRequest.builder()
                .setSubDeviceInfo(SUB_DEVICE_PRODUCT_KEY, subDeviceKey, subDeviceSecret)
                .build();

        SubDeviceLoginResponse response = client.publish(request);
        System.out.println(response);
    }

    // Sample: post measurement point for sub-device
    static void postMeasurepointForSubDevice() throws EnvisionException {
        MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                .setProductKey(SUB_DEVICE_PRODUCT_KEY)          // use product key of sub-device
                .setDeviceKey(subDeviceKey)                     // use device key of sub-device
                .addMeasreuPointWithQuality("Power", 1, 9)
                .addMeasurePoint("temp", 1.02)
                .addMeasurePoint("branchCurr", Arrays.asList(1.02, 2.02, 7.93))
                .build();

        MeasurepointPostResponse response = client.publish(request);
        System.out.println(response);
    }
}
