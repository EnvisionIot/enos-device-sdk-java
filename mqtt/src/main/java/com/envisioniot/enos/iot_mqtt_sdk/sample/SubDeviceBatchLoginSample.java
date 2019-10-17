package com.envisioniot.enos.iot_mqtt_sdk.sample;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginBatchRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginBatchResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostResponse;
import com.google.common.collect.ImmutableMap;

import java.util.Random;

public class SubDeviceBatchLoginSample {

    public static void main(String[] args) throws Exception {
        String betaUrl = "tcp://beta-iot-as-mqtt-cn4.eniot.io:11883";

        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(betaUrl, "Lx5Q1X6M", "will_gw02", "rQcAEfBGB5dZTqcaIyjX")
        ));
        client.getProfile().setAutoLoginSubDevice(false);
        client.connect();

        SubDeviceLoginBatchRequest request = SubDeviceLoginBatchRequest.builder()
                .addSubDeviceInfo("x4jwTsoz", "xGKFwfkEXz", "wNDGE8f0EAdh6i7OzNwk")
                .addSubDeviceInfo("x4jwTsoz", "cS9MdVJwO9", "CXqWUK0J47JnH08QLsRN")
                .addSubDeviceInfo("x4jwTsoz", "viWaBURIDm", "nbhsPwqhtSy6pE9ZOkZk")
                .build();

        SubDeviceLoginBatchResponse response = client.publish(request);

        if (response.hasSevereError()) {
            System.out.println("request format not correct: " + response.getMessage());
        } else if (response.isSuccess()) {
            System.out.println("all sub-devices logined successfully");
            System.out.println("logined devices: " + response.getSuccessResults());
        } else {
            System.out.println("part of sub-devices failed: " + response.getMessage());
            System.out.println("logined devices: " + response.getSuccessResults());
            System.out.println("failed devices: " + response.getFailureResults());
        }

        final Random rand = new Random();
        for(SubDeviceLoginBatchResponse.LoginSuccessResult result : response.getSuccessResults()) {
            MeasurepointPostRequest req = MeasurepointPostRequest.builder()
                    .setProductKey(result.productKey)
                    .setDeviceKey(result.deviceKey)
                    .addMeasurePoints(ImmutableMap.of("value", rand.nextDouble()))
                    .build();

            MeasurepointPostResponse rsp = client.publish(req);
            if (rsp.isSuccess()) {
                System.out.println("sent measure point to sub-device: " + result.deviceKey);
            } else {
                System.out.println("failed to measure point to sub-device: " + result.deviceKey);
            }
        }

        System.in.read();

        client.close();
    }

}
