package mqtt.old;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.LoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.register.DeviceRegisterRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.register.DeviceRegisterResponse;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringI18n;

import mqtt.old.helper.Helper;

import java.util.Locale;

public class RegisterDeviceSample {

    public static void main(String[] args) throws Exception {
        LoginInput input = new NormalDeviceLoginInput(Helper.SERVER_URL, Helper.GW_PRODUCT_KEY, Helper.GW_DEV_KEY, Helper.GW_DEV_SECRET);
        final MqttClient client = new MqttClient(new DefaultProfile(input));
        client.connect();

        try {
            // device #1
            StringI18n deviceName1 = new StringI18n("sample_dev_01");
            deviceName1.setLocalizedValue(Locale.US.toString(), "eng_dev_01");
            deviceName1.setLocalizedValue(Locale.SIMPLIFIED_CHINESE.toString(), "中文设备01");

            // device #2
            StringI18n deviceName2 = new StringI18n("sample_dev_02");
            deviceName2.setLocalizedValue(Locale.US.toString(), "eng_dev_02");
            deviceName2.setLocalizedValue(Locale.SIMPLIFIED_CHINESE.toString(), "中文设备02");

            // device #3
            StringI18n deviceName3 = new StringI18n("sample_dev_03");
            deviceName3.setLocalizedValue(Locale.US.toString(), "eng_dev_03");
            deviceName3.setLocalizedValue(Locale.SIMPLIFIED_CHINESE.toString(), "中文设备03");

            DeviceRegisterRequest req = DeviceRegisterRequest
                    .builder()
                    .addRegisterInfo(Helper.PRODUCT_KEY, deviceName1.getDefaultValue(), deviceName1, "dev desc", "+08:00")
                    .addRegisterInfo(Helper.PRODUCT_KEY, deviceName2.getDefaultValue(), deviceName2, "dev desc", "+09:00")
                    .addRegisterInfo(Helper.PRODUCT_KEY, deviceName3.getDefaultValue(), deviceName3, "dev desc", "+10:00")
                    .build();
            DeviceRegisterResponse rsp = client.publish(req);
            if (rsp.isSuccess()) {
                System.out.println("registered device successfully, device list info: " + rsp.getDeviceBasicInfoList());
            } else {
                System.out.println("failed to register device, error: " + rsp.getMessage());
            }
        } finally {
            client.close();
        }

    }
}
