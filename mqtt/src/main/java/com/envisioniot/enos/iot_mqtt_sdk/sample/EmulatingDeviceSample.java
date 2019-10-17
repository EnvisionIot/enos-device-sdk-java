package com.envisioniot.enos.iot_mqtt_sdk.sample;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMessageHandler;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.ota.OtaUpgradeCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.ota.OtaUpgradeReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.MeasurepointSetCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.MeasurepointSetReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.ServiceInvocationCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.ServiceInvocationReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.ota.*;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostResponse;
import com.envisioniot.enos.iot_mqtt_sdk.sample.common.Helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This sample emulates how a real device interacts with broker.
 *
 * @author jian.zhang4
 */
public class EmulatingDeviceSample {

    private static final String DEFAULT_SERVER_URL = Helper.SERVER_URL;

    public static void main(String appArgs[]) throws Exception {
        if (appArgs.length > 1) {
            throw new IllegalArgumentException("Only accept one argument. usage: EmulatingDeviceSample server-url");
        }
        String serverUrl = appArgs.length == 0 ? DEFAULT_SERVER_URL : appArgs[0];

        boolean exited = false;
        MqttClient client = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (!exited) {
            System.out.print("> ");

            String input = reader.readLine().trim();
            if (input.toLowerCase().equals("help")) {
                System.out.println("setServerUrl url");
                System.out.println("connect productKey deviceKey deviceSecret");
                System.out.println("publish mpName mpValue mpType [subProductKey subDeviceKey]"); // mp: measure point
                System.out.println("registerMessageHandler");
                System.out.println("loginSubDevice subProductKey subDeviceKey subDeviceSecret");
                System.out.println("logoutSubDevice subProductKey subDeviceKey");
                System.out.println("reportVersion currentVersion");
                System.out.println("enableFirmwareUpgrade");
                System.out.println("disconnect");
                System.out.println("exit");

                continue;
            } else if (input.isEmpty()) {
                continue;
            }

            String[] args = input.split("\\s+");
            switch (args[0]) {
                case "setServerUrl":
                    if (validate(args, 2, false, client, true)) {
                        String oldUrl = serverUrl;
                        serverUrl = args[1];
                        System.out.println("set url: " + oldUrl + " -> " + serverUrl);
                    }
                    break;
                case "connect":
                    if (validate(args, 4, false, client, true)) {
                        try {
                            client = new MqttClient(serverUrl, args[1], args[2], args[3]);
                            client.getProfile().setConnectionTimeout(60).setAutoReconnect(true);
                            client.connect();
                            System.out.println("successfully connected to broker");
                        } catch (Exception e) {
                            client.close();
                            client = null;
                            e.printStackTrace();
                        }
                    }
                    break;
                case "publish":
                    if (validate(args, 4, true, client, false)) {
                        Object value = args[2];

                        if (args[3].equals("double")) {
                            value = Double.valueOf(args[2]);
                        } else if (args[3].equals("float")) {
                            value = Float.valueOf(args[2]);
                        } else if (args[3].equals("int")) {
                            value = Integer.valueOf(args[2]);
                        } else if (!args[3].equals("string")) {
                            System.err.println("Error: don't support measure point type " + args[3]);
                        }

                        String productKey = client.getProfile().getProductKey();
                        String deviceKey = client.getProfile().getDeviceKey();
                        if (args.length >= 6) {
                            productKey = args[4];
                            deviceKey = args[5];
                        }

                        publishMeasurepoint(client, args[1], value, productKey, deviceKey);
                    }
                    break;
                case "registerMessageHandler":
                    if (validate(args, 1, false, client, false)) {
                        client.setArrivedMsgHandler(ServiceInvocationCommand.class, createServiceCommandHandler());
                        client.setArrivedMsgHandler(MeasurepointSetCommand.class, createMeasurepointSetHandler(client));
                    }
                    break;
                case "loginSubDevice":
                    if (validate(args, 4, false, client, false)) {
                        loginSubDevice(client, args[1], args[2], args[3]);
                    }
                    break;
                case "logoutSubDevice":
                    if (validate(args, 3, false, client, false)) {
                        logoutSubDevice(client, args[1], args[2]);
                    }
                    break;
                case "reportVersion":
                    if (validate(args, 2, false, client, false)) {
                        reportVersion(client, args[1]);
                    }
                    break;
                case "enableFirmwareUpgrade":
                    if (validate(args, 1, false, client, false)) {
                        enableFirmwareUpgrade(client);
                    }
                    break;
                case "disconnect":
                    if (validate(args, 1, false, client, false)) {
                        client.disconnect();
                        client.close();

                        client = null;
                    }
                    break;
                case "exit":
                    exited = true;
                    break;
                default:
                    System.out.println("Error: invalid command line. Input help for more details");
            }
        }

        if (client != null) {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        }
    }

    private static boolean validate(String args[], int expectedArgLen, boolean allowMoreArgs,
                                    MqttClient client, boolean expClientNull) {
        if (args.length == expectedArgLen || allowMoreArgs && args.length > expectedArgLen) {
            if (!expClientNull && client == null) {
                System.err.println("Error: device not connected yet");
                return false;
            }

            if (expClientNull && client != null && client.isConnected()) {
                System.err.println("Error: device already connected yet");
                return false;
            }

            return true;
        }

        System.err.println("Error: invalid command line. Input help for more details");
        return false;
    }

    private static IMessageHandler<ServiceInvocationCommand, ServiceInvocationReply> createServiceCommandHandler() {
        return (ServiceInvocationCommand request, List<String> argList) -> {
            System.out.println("\nreceive service invocation command: " + request + ", args: " + argList);

            // argList: productKey, deviceKey, serviceName
            // If the request is for sub-device, the productKey and device
            // are used to identify the target sub-device.
            String productKey = argList.get(0);
            String deviceKey = argList.get(1);
            String serviceName = argList.get(2);
            System.out.println(String.format("productKey=%s, deviceKey=%s, serviceName=%s",
                    productKey, deviceKey, serviceName));

            System.out.println("service params: " + request.getParams());

            if (serviceName.equals("multiply")) {
                Map<String, Object> params = request.getParams();
                Integer left = (Integer) params.get("left");
                Integer right = (Integer) params.get("right");

                // Set the reply result
                return ServiceInvocationReply.builder().addOutputData("result", left * right).build();
            }

            return ServiceInvocationReply.builder().setMessage("unknown service: " + serviceName).setCode(220).build();
        };
    }

    private static IMessageHandler<MeasurepointSetCommand, MeasurepointSetReply> createMeasurepointSetHandler(final MqttClient client) {
        return (MeasurepointSetCommand request, List<String> argList) -> {
            System.out.println("\nreceive measurepoint set command: " + request + ", args: " + argList);

            // argList: productKey, deviceKey
            // If the request is for sub-device, the productKey and device
            // are used to identify the target sub-device.
            String productKey = argList.get(0);
            String deviceKey = argList.get(1);
            System.out.println(String.format("productKey=%s, deviceKey=%s", productKey, deviceKey));

            System.out.println("measure point data: " + request.getParams());

            /**
             * TODO: implement measurepoint logic that sets device measure point ...
             */

            // echo back our current measure point set in downstream
            // client.publish(MeasurepointPostRequest.builder().addMeasurePoints(request.getParams()).build());

            return MeasurepointSetReply.builder().setMessage("measurepoints " + request.getParams() + " set successfully").build();
        };
    }

    private static void publishMeasurepoint(MqttClient client, String measurepoint, Object value, String productKey, String deviceKey) {
        try {
            MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                    .setProductKey(productKey)
                    .setDeviceKey(deviceKey)
                    .addMeasurePoint(measurepoint, value).build();

            MeasurepointPostResponse response = client.publish(request);
            if (response.isSuccess()) {
                System.out.println("\nmeasure point " + measurepoint + " published successfully");
            } else {
                System.out.println("failed to publish " + measurepoint + ": " + response.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loginSubDevice(MqttClient client, String subProductKey, String subDeviceKey, String subDeviceSecret) {
        try {
            SubDeviceLoginRequest request = SubDeviceLoginRequest.builder()
                    .setSubDeviceInfo(subProductKey, subDeviceKey, subDeviceSecret)
                    .build();

            SubDeviceLoginResponse response = client.publish(request);
            if (response.isSuccess()) {
                System.out.println("logged in sub-device successfully");
            } else {
                System.out.println("failed to login sub-device");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void logoutSubDevice(MqttClient client, String subProductKey, String subDeviceKey) {
        SubDeviceLogoutRequest request = SubDeviceLogoutRequest.builder()
                .setQos(0)
                .setSubProductKey(subProductKey)
                .setSubDeviceKey(subDeviceKey)
                .build();
        try {
            SubDeviceLogoutResponse response = client.publish(request);
            if (response.isSuccess()) {
                System.out.println("logged out sub-device successfully");
            } else {
                System.out.println("failed to logout sub-device");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void reportVersion(MqttClient client, String version) {
        OtaVersionReportRequest.Builder builder = new OtaVersionReportRequest.Builder();
        builder
                .setProductKey(client.getProfile().getProductKey())
                .setDeviceKey(client.getProfile().getDeviceKey())
                .setVersion(version);
        OtaVersionReportRequest request = builder.build();
        try {
            OtaVersionReportResponse response = client.publish(request);
            if (response.isSuccess()) {
                System.out.println("\nversion reported successfully");
            } else {
                System.out.println("failed to report version: " + response.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Here we demo how to upgrade firmware from cloud directive
    private static void enableFirmwareUpgrade(final MqttClient client) {
        client.setArrivedMsgHandler(OtaUpgradeCommand.class,
                (OtaUpgradeCommand otaUpgradeCommand, List<String> args) -> {
                    System.out.println("receive command: " + otaUpgradeCommand);
                    System.out.println("receive args: " + args);

                    Firmware firmware = otaUpgradeCommand.getFirmwareInfo();
                    System.out.println("fireware url: " + firmware.fileUrl);

                    // TODO:
                    // download firmware from firmware.fileUrl and
                    // perform firmware upgrade here
                    // ............................

                    try {
                        //Mock reporting progress
                        reportUpgradeProgress(client, "10", "10");
                        TimeUnit.SECONDS.sleep(1);

                        reportUpgradeProgress(client, "30", "30");
                        TimeUnit.SECONDS.sleep(3);

                        reportUpgradeProgress(client, "80", "80");
                        TimeUnit.SECONDS.sleep(8);

                        reportUpgradeProgress(client, "100", "100");

                        // Firmware upgrade success, report new version
                        reportVersion(client, otaUpgradeCommand.getFirmwareInfo().version);

                        return (new OtaUpgradeReply.Builder()).setMessage("upgrade successfully").build();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return (new OtaUpgradeReply.Builder()).setMessage("upgrade failed").setCode(101).build();
                    }
                });
    }

    private static void reportUpgradeProgress(MqttClient client, String progress, String desc) throws Exception {
        OtaProgressReportRequest.Builder builder = new OtaProgressReportRequest.Builder();
        builder.setStep(progress).setDesc(desc);
        OtaProgressReportResponse response = client.publish(builder.build());
        if (response.isSuccess()) {
            System.out.println("\nupgraded " + progress + "%");
        } else {
            throw new RuntimeException("failed to upgrade: " + response.getMessage());
        }
    }

}
