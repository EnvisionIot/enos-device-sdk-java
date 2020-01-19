package mqtt.old;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginBatchRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginBatchResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostBatchRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostBatchResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.google.common.collect.ImmutableMap;

import java.util.Random;

public class SubDeviceBatchLoginSample {

    public static void main(String[] args) throws Exception {
        // user: 周敏beta测试
        String betaUrl = "tcp://beta-iot-as-mqtt-cn4.eniot.io:11883";

        int batchCount = 1;
        int mpn = 1;

        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(betaUrl, "bChjZTxn", "GXJ0cVMMWv", "t9uKUFdLk2hcLHUyurzg")
        ));
        client.getProfile().setAutoLoginSubDevice(false);
        client.connect();

        SubDeviceLoginBatchRequest request = SubDeviceLoginBatchRequest.builder()
                .addSubDeviceInfo("69nJVYaU", "Nov14201954006AM", "VtCpz81UFOxMVRYP1A1s")
                // "69nJVYaU" also mirrors whose devices are: Nov20201972358PM, Nov182019101752AM
                .addSubDeviceInfo("69nJVYaU", "CqXniesegD", "fqF9Tvs3oL6S4h0ALUEb")
                .setClientId("GXJ0cVMMWv")
                .build();

        SubDeviceLoginBatchResponse response = client.publish(request);

        if (response.hasServerError()) {
            System.out.println("request format not correct: " + response.getMessage());
        } else if (response.isSuccess()) {
            System.out.println("all sub-devices logined successfully");
            System.out.println("logined devices: " + response.getSuccessResults());
        } else {
            System.out.println("part of sub-devices failed: " + response.getMessage());
            System.out.println("logined devices: " + response.getSuccessResults());
            System.out.println("failed devices: " + response.getFailureResults());
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < batchCount; ++i) {
            if (!response.getSuccessResults().isEmpty()) {
                MeasurepointPostBatchRequest.Builder builder = MeasurepointPostBatchRequest.builder();

                final Random rand = new Random();
                for (int j = 0; j < mpn; ++j) {
                    for (SubDeviceLoginBatchResponse.LoginSuccessResult result : response.getSuccessResults()) {
                        MeasurepointPostRequest req = MeasurepointPostRequest.builder()
                                .setProductKey(result.productKey)
                                .setDeviceKey(result.deviceKey)
                                .addMeasurePoints(ImmutableMap.of(
                                        "datetime", "hello",
                                        "int1", 100,
                                        "float1", 88.88
                                        ))
                                .build();

                        builder.addRequest(req);
                    }
                }

                MeasurepointPostBatchResponse rsp = client.publish(builder.build());
                if (rsp.isSuccess()) {
                    System.out.println("sent measurepoint batch request");
                } else {
                    System.out.println("failed to measure point to sub-device: " + rsp.getMessage());
                }
            }
        }

        long cost = System.currentTimeMillis() - start;
        System.out.println("cost: " + cost + "ms");
        System.out.println("please print any key to exit ...");
        System.in.read();

        client.close();
    }

}
