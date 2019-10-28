package com.envisioniot.enos.iot_mqtt_sdk.sample;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginBatchRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginBatchResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostBatchRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostBatchResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.Random;

public class SubDeviceBatchLoginSample {

    public static void main(String[] args) throws Exception {
//        String betaUrl = "tcp://localhost:11883";
        String betaUrl = "tcp://beta-iot-as-mqtt-cn4.eniot.io:11883";

        int batchCount = 200;
        int mpn = 10;

        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(betaUrl, "Lx5Q1X6M", "will_gw02", "rQcAEfBGB5dZTqcaIyjX")
        ));
        client.getProfile().setAutoLoginSubDevice(false);
        client.connect();

        SubDeviceLoginBatchRequest request = SubDeviceLoginBatchRequest.builder()
                .addSubDeviceInfo("x4jwTsoz", "xGKFwfkEXz", "wNDGE8f0EAdh6i7OzNwk")
                .addSubDeviceInfo("x4jwTsoz", "cS9MdVJwO9", "CXqWUK0J47JnH08QLsRN")
                .addSubDeviceInfo("x4jwTsoz", "viWaBURIDm", "nbhsPwqhtSy6pE9ZOkZk")
                .addSubDeviceInfo("x4jwTsoz", "dev_perm_03", "U2kDkCw8Ff0Qhhf51D0G")
                .addSubDeviceInfo("x4jwTsoz", "dev_perm_04", "uHuA8vBaW3xSHDuXcXi8")
                .addSubDeviceInfo("x4jwTsoz", "dev_perm_05", "jvtoRVR5wAWH9MUAYGGj")
                .addSubDeviceInfo("x4jwTsoz", "dev_perm_06", "oBhMxH7uteASxNWYmWwa")
                .addSubDeviceInfo("x4jwTsoz", "dev_perm_07", "n2bNLFECEQDClnclw36R")
                .setClientId("Lx5Q1X6M")
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
                                        "value", rand.nextDouble(),
                                        "temp", rand.nextFloat(),
                                        "struct", ImmutableMap.of(
                                                "books", ImmutableList.of("Computer" + rand.nextInt(), "Redis" + rand.nextInt(), "Netty" + rand.nextInt()),
                                                "name", "test" + rand.nextInt(),
                                                "age", rand.nextInt()
                                        )))
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
