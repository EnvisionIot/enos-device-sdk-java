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
import lombok.extern.slf4j.Slf4j;
import mqtt.old.helper.BaseConnectCallback;

@Slf4j
public class SubDeviceBatchLoginSample {

    public static void main(String[] args) throws Exception {
        String betaUrl = "tcp://beta-iot-as-mqtt-cn4.eniot.io:11883";

        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(betaUrl, "bChjZTxn", "GXJ0cVMMWv", "t9uKUFdLk2hcLHUyurzg")
        ));
        client.getProfile().setAutoLoginSubDevice(false);
        client.connect(new BaseConnectCallback(client, "batch-login", true) {
            @Override
            protected void onSuccess(MqttClient client) {
                try {
                    performTest(client);
                } catch (Exception e) {
                    log.error("un-expected error", e);
                }
            }
        });
    }

    private static void performTest(MqttClient client) throws Exception {
        int batchCount = 1;
        int mpn = 1;

        SubDeviceLoginBatchRequest request = SubDeviceLoginBatchRequest.builder()
                .addSubDeviceInfo("69nJVYaU", "Nov14201954006AM", "VtCpz81UFOxMVRYP1A1s")
                // "69nJVYaU" also mirrors whose devices are: Nov20201972358PM, Nov182019101752AM
                .addSubDeviceInfo("69nJVYaU", "CqXniesegD", "fqF9Tvs3oL6S4h0ALUEb")
                .setClientId("GXJ0cVMMWv")
                .build();

        SubDeviceLoginBatchResponse response = client.publish(request);

        if (response.hasServerError()) {
            log.error("format not correct: {}", response.getMessage());
            System.out.println("request format not correct: " + response.getMessage());
        } else if (response.isSuccess()) {
            log.info("all sub-devices logined successfully");
            log.info("logined devices: {}", response.getSuccessResults());
        } else {
            log.info("part of sub-devices failed: {}", response.getMessage());
            log.info("logined devices: {}", response.getSuccessResults());
            log.info("failed devices: {}", response.getFailureResults());
        }

        for (int i = 0; i < batchCount; ++i) {
            if (!response.getSuccessResults().isEmpty()) {
                MeasurepointPostBatchRequest.Builder builder = MeasurepointPostBatchRequest.builder();

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
                    log.info("sent measurepoint batch request");
                } else {
                    log.info("failed to measure point to sub-device: " + rsp.getMessage());
                }
            }
        }
    }

}
