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
import mqtt.old.helper.Helper;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SubDeviceBatchLoginSample {

    public static void main(String[] args) throws Exception {
        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(
                        Helper.SERVER_URL, "vOQSJ4dN", "mqtt_sample_gwgroup_gw01", "m90jgBApzE5mBBXnkFxG")
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
        int batchCount = 100000;
        int mpn = 10;

        SubDeviceLoginBatchRequest request = SubDeviceLoginBatchRequest.builder()
                .addSubDeviceInfo("K9HMijjG", "mqtt_sample_gwgroup_dev01", "sGCCwSIcD1AJ5wa8OcPW")
                // "mqtt_sample_gwgroup_dev02" has mirrors whose device keys are: gwgroup_mirror_dev01, gwgroup_mirror_dev02
                .addSubDeviceInfo("K9HMijjG", "mqtt_sample_gwgroup_dev02", "DujdqFrTarWOOvHv22Yt")
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
                                        "temp", new Random().nextDouble(),
                                        "timestamp", System.currentTimeMillis(),
                                        "value", new Random().nextInt(10000),
                                        "invalidMp", 200
                                ))
                                .setQos(0)
                                .build();

                        builder.addRequest(req);
                    }
                }

                client.fastPublish(builder.setSkipInvalidMeasurepoints(true).setQos(0).build());
                log.info("sent measurepoint batch request");

//                MeasurepointPostBatchResponse rsp = client.publish(builder.setSkipInvalidMeasurepoints(true).setQos(0).build());
//                if (rsp.isSuccess()) {
//                    log.info("sent measurepoint batch request");
//                } else {
//                    log.info("failed to measure point to sub-device: " + rsp.getMessage());
//                }

//                TimeUnit.SECONDS.sleep(1);
            }
        }
    }

}
