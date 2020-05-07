package mqtt.old;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DeviceCredential;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume.MeasurepointResumeBatchRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume.MeasurepointResumeBatchResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume.MeasurepointResumeRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume.MeasurepointResumeResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginResponse;
import com.google.common.collect.ImmutableMap;
import mqtt.old.helper.Helper;

import java.util.Random;

public class ResumeRequestSample {

    private final static Random RANDOM = new Random();

    private static MeasurepointResumeRequest buildRequest(final DeviceCredential subDev, String floatMpName) {
        return MeasurepointResumeRequest.builder()
                .setProductKey(subDev.getProductKey())
                .setDeviceKey(subDev.getDeviceKey())
                .addMeasurePoints(ImmutableMap.of(floatMpName, RANDOM.nextDouble()))
                .build();
    }

    private static void resumeSingleMeasurePoint(final MqttClient client, final DeviceCredential subDev) {
        try {
            MeasurepointResumeResponse response = client.publish(buildRequest(subDev, "INV.GenActivePW"));
            if (response.isSuccess()) {
                System.out.println("sent offline single measure point");
            } else {
                System.out.println("failed to send out offline single measure point to broker: " + response.getMessage());
            }
        } catch (EnvisionException e) {
            e.printStackTrace();
        }
    }

    private static void resumeBatchMeasurePoints(final MqttClient client, final DeviceCredential subDev) {
        final Random rand = new Random();

        MeasurepointResumeBatchRequest request = MeasurepointResumeBatchRequest.builder()
                .addRequest(buildRequest(subDev, "value"))
                .addRequest(buildRequest(subDev, "dont.exist.name"))
                .addRequest(buildRequest(subDev, "value"))
                .setSkipInvalidMeasurepoints(true)
                .build();

        try {
            MeasurepointResumeBatchResponse response = client.publish(request);
            if (response.isSuccess()) {
                System.out.println("sent offline batch measure points");
            } else {
                System.out.println("failed to send out offline batch measure points to broker: " + response.getMessage());
            }
        } catch (EnvisionException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        MqttClient client = new MqttClient(new DefaultProfile(new NormalDeviceLoginInput(
                Helper.SERVER_URL, Helper.GW_PRODUCT_KEY, Helper.GW_DEV_KEY, Helper.GW_DEV_SECRET)
        ));
        client.getProfile().setAutoReconnect(false);
        client.connect();

        try {
            DeviceCredential subDev = new DeviceCredential(Helper.DEV_PRODUCT_KEY, null, Helper.SUB_DEV01_KEY, Helper.SUB_DEV01_SECRET);

            SubDeviceLoginRequest request = SubDeviceLoginRequest.builder().setSubDeviceInfo(subDev).build();
            SubDeviceLoginResponse response = client.publish(request);
            if (!response.isSuccess()) {
                System.err.println("failed to login sub-device: " + subDev.getDeviceKey());
                return;
            }
            System.out.println("successfully logined sub-device: " + subDev.getDeviceKey());

            resumeBatchMeasurePoints(client, subDev);

            resumeSingleMeasurePoint(client, subDev);
        } finally {
            client.close();
        }
    }

}
