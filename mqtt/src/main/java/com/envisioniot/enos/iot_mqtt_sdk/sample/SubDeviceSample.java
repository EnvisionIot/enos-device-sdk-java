package com.envisioniot.enos.iot_mqtt_sdk.sample;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DeviceCredential;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostBatchRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostBatchResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.sample.common.BaseConnectCallback;
import com.envisioniot.enos.iot_mqtt_sdk.sample.common.Helper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.envisioniot.enos.iot_mqtt_sdk.sample.common.Helper.*;

/**
 * This sample shows how sub-devices login/logout and publish
 * measure points to broker.
 *
 * @author jian.zhang4
 */
public class SubDeviceSample {
    private static final int PUB_ROUND = 3;

    private static volatile boolean isTestSubDevicesByManuallyLoginDone = false;

    public static void main(String[] args) {
        System.out.println("\n\n----------------------------------");
        testSubDevicesByManuallyLogin();

        /**
         * We can't do run testSubDevicesByManuallyLogin and testSubDevicesByAutoLogin at
         * the same time (they would kick off each other).
         */
        while (!isTestSubDevicesByManuallyLoginDone) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                // ignore
            }
        }

        System.out.println("\n\n----------------------------------");
        testSubDevicesByAutoLogin();
    }

    private static void testSubDevicesByManuallyLogin() {
        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(SERVER_URL, GW_PRODUCT_KEY, GW_DEV_KEY, GW_DEV_SECRET)
        ));

        client.connect(new BaseConnectCallback(client, "test sub-devices by manual login", false) {
            @Override
            protected void onSuccess(MqttClient client) {
                List<DeviceCredential> loginedSubDevices = loginSubDevices(client);
                if (!loginedSubDevices.isEmpty()) {
                    for (int round = 0; round < PUB_ROUND; ++round) {
                        publishMeasurePointsFor(client, loginedSubDevices);
                    }
                }
                logoutSubDevices(client, loginedSubDevices);

                Helper.cleanConnection(client);

                isTestSubDevicesByManuallyLoginDone = true;
            }
        });
    }

    /**
     * Manually login sub-devices
     */
    private static List<DeviceCredential> loginSubDevices(final MqttClient client) {
        List<DeviceCredential> loginedSubDevices = new LinkedList<>();
        SUBDEVICES.forEach(dev -> {
            try {
                SubDeviceLoginRequest request = SubDeviceLoginRequest.builder().setSubDeviceInfo(dev).build();
                SubDeviceLoginResponse response = client.publish(request);
                if (response.isSuccess()) {
                    System.out.println("successfully logined sub-device: " + dev.getDeviceKey());
                    loginedSubDevices.add(dev);
                } else {
                    System.err.println("failed to login sub-device: " + dev.getDeviceKey());
                }
            } catch (EnvisionException e) {
                e.printStackTrace();
            }
        });

        return loginedSubDevices;
    }

    /**
     * When we add sub-device info into profile, they would be auto logged in by MqttClient
     * after the connection is successful.
     */
    private static void testSubDevicesByAutoLogin() {
        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(SERVER_URL, GW_PRODUCT_KEY, GW_DEV_KEY, GW_DEV_SECRET)
        ));

        // We just need to add sub-devices into profile if want them to auto login
        SUBDEVICES.forEach(dev -> client.getProfile().addSubDevice(dev));

        client.connect(new BaseConnectCallback(client, "test sub-devices by auto login", true) {
            @Override
            protected void onSuccess(MqttClient client) {
                // Currently, the drawback of auto-login for sub-devices is that we don't known
                // when and whether sub-devices have successfully logined.
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    // ignore this
                }

                for (int round = 0; round < PUB_ROUND; ++round) {
                    publishMeasurePointsFor(client, SUBDEVICES);
                }

                logoutSubDevices(client, SUBDEVICES);
            }
        });
    }

    private static void publishMeasurePointsFor(final MqttClient client, List<DeviceCredential> subDevices) {
        final Random rand = new Random();
        List<MeasurepointPostRequest> requests =
                subDevices.stream().map(dev ->
                        MeasurepointPostRequest.builder()
                                .setProductKey(dev.getProductKey())
                                .setDeviceKey(dev.getDeviceKey())
                                .addMeasurePoints(ImmutableMap.of("temp", rand.nextDouble(), "value", rand.nextDouble()))
                                .build()
                ).collect(Collectors.toList());

        // Send the requests in batch
        try {
            MeasurepointPostBatchResponse response = client.publish(
                    MeasurepointPostBatchRequest.builder().setRequests(requests).build());
            if (response.isSuccess()) {
                System.out.println("sent " + requests.size() + " measure points");
            } else {
                System.err.println("failed to send out posts to broker: " + response.getMessage());
            }
        } catch (EnvisionException e) {
            e.printStackTrace();
        }
    }

    private static void logoutSubDevices(final MqttClient client, List<DeviceCredential> subDevices) {
        subDevices.forEach(dev -> {
            SubDeviceLogoutRequest request = SubDeviceLogoutRequest.builder()
                    .setQos(0)
                    .setSubProductKey(dev.getProductKey())
                    .setSubDeviceKey(dev.getDeviceKey())
                    .build();
            try {
                // We use fastPublish here since we don't care about the return.
                SubDeviceLogoutResponse response = client.publish(request);
                if (response.isSuccess()) {
                    System.out.println("successfully logged out sub-device: " + dev.getDeviceKey());
                } else {
                    System.err.println("failed to log out sub-device: " + dev.getDeviceKey());
                }
            } catch (EnvisionException e) {
                e.printStackTrace();
            }
        });
    }
}
