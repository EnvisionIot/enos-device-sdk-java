package mqtt.old;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostResponse;
import com.google.common.collect.ImmutableMap;

public class DirectDeviceSample {
    private static final String BETA_URL = "tcp://beta-iot-as-mqtt-cn4.eniot.io:11883";

    public static void main(String[] args) throws Exception {
//        testDeviceWithMirrors();
        testDeviceWithoutMirrors();
    }

    private static void testDeviceWithMirrors() throws Exception {
        // zhoumin account
        // The following direct device has mirror whose device key is HpG2Z2eSoC
        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(BETA_URL, "oDc253LO", "zm1112a", "pmh5jEWEt2Cip5KsGPO3")
        ));
        client.getProfile().setAutoLoginSubDevice(false);
        client.connect();

        MeasurepointPostRequest req = MeasurepointPostRequest.builder()
                .addMeasurePoints(ImmutableMap.of(
                        "Int_value", 9999,
                        "mp_string", "test",
                        "newFile", "enos-connect://iot/test",
                        "20_file", "http://baidu.com/test/here.jsp"
                ))
                .build();
        execRequest(client, req);
        client.close();
    }

    private static void testDeviceWithoutMirrors() throws Exception {
        // zhangjian account
        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(BETA_URL, "x4jwTsoz", "FfHtfAyhC5", "DVCWsRQHeIOvryFy7fIz")
        ));
        client.getProfile().setAutoLoginSubDevice(false);
        client.connect();

        MeasurepointPostRequest req = MeasurepointPostRequest.builder()
                .addMeasurePoints(ImmutableMap.of(
                        "temp", 9999.99,
                        "age", 100
                ))
                .build();
        execRequest(client, req);
        client.close();
    }

    private static void execRequest(MqttClient client, MeasurepointPostRequest req) throws Exception {
        MeasurepointPostResponse response = client.publish(req);
        if (response.isSuccess()) {
            System.out.println("Successfully sent request for " + client.getProfile().getDeviceKey());
        } else {
            System.out.println("failed to send request for " + client.getProfile().getDeviceKey() + ", error: " + response.getMessage());
        }
    }
}
