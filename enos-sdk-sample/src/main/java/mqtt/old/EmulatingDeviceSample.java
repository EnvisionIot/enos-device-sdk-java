package mqtt.old;

import com.envisioniot.enos.iot_mqtt_sdk.core.ConnCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMessageHandler;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.device.SubDeviceDeleteCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.device.SubDeviceDeleteReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.device.SubDeviceDisableCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.device.SubDeviceDisableReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.ota.OtaUpgradeCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.ota.OtaUpgradeReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.MeasurepointSetCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.MeasurepointSetReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.ServiceInvocationCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.ServiceInvocationReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.ota.*;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume.MeasurepointResumeBatchRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume.MeasurepointResumeRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.*;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import mqtt.old.helper.Helper;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * This sample emulates how a real device interacts with broker.
 *
 * @author jian.zhang4
 */
@Slf4j
public class EmulatingDeviceSample {

    private static final String DEFAULT_SERVER_URL = Helper.SERVER_URL;

    private static volatile boolean ignoreInvalidMeasurePoints = false;

    public static void main(String[] appArgs) throws Exception {
        if (appArgs.length > 1) {
            throw new IllegalArgumentException("Only accept one argument. usage: EmulatingDeviceSample server-url");
        }
        String serverUrl = appArgs.length == 0 ? DEFAULT_SERVER_URL : appArgs[0];

        boolean exited = false;
        MqttClient client = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        boolean realtimePubMode = true;
        while (!exited) {
            System.out.print("> ");

            String input = reader.readLine().trim();
            if ("help".equals(input.toLowerCase())) {
                System.out.println("setServerUrl url");
                System.out.println("connect productKey deviceKey deviceSecret");
                System.out.println("setPublishMode realtime/offline");
                System.out.println("publish mpName1 mpValue1 mpType1 [mpName2 mpValue2 mpType2] [...] [subProductKey subDeviceKey]");
                System.out.println("publishEvent eventId1 param1 value1 type1 [param2 value2 type2] [...] [subProductKey subDeviceKey]");
                System.out.println("updateAttrs attr1 value1 type1 [attr2 value2 type2] [...] [subProductKey subDeviceKey]");
                System.out.println("registerMessageHandler");
                System.out.println("loginSubDevice subProductKey subDeviceKey subDeviceSecret");
                System.out.println("logoutSubDevice subProductKey subDeviceKey");
                System.out.println("reportVersion currentVersion");
                System.out.println("enableFirmwareUpgrade");
                System.out.println("ignoreInvalidMeasurePoints true/false");
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
                            client.getProfile()
                                    .setConnectionTimeout(60)
                                    .setAutoReconnect(true);
                            client.connect(new ConnCallback() {

                                @Override
                                public void connectComplete(boolean reconnect) {
                                    log.info("connectComplete: reconnect=" + reconnect);
                                }

                                @Override
                                public void connectLost(Throwable cause) {
                                    log.info("connectComplete: connectLost, " + cause.getMessage());
                                }

                                @Override
                                public void connectFailed(Throwable cause) {
                                    log.info("connectComplete: connectFailed");
                                }
                            });
                        } catch (Exception e) {
                            client.close();
                            client = null;
                            e.printStackTrace();
                        }
                    }
                    break;
                case "ignoreInvalidMeasurePoints":
                    if (args.length < 2) {
                        System.err.println("Error: invalid command line. Input help for more details");
                    } else {
                        ignoreInvalidMeasurePoints = Boolean.parseBoolean(args[1]);
                    }
                    break;
                case "setPublishMode":
                    if (args.length != 2) {
                        System.err.println("Error: invalid command line. Input help for more details");
                    } else {
                        String mode = args[1];
                        if ("realtime".equals(mode)) {
                            realtimePubMode = true;
                        } else if ("offline".equals(mode)) {
                            realtimePubMode = false;
                        } else {
                            System.err.println("Error: invalid publish mode: " + mode + ". Input help for more details");
                        }
                    }
                    break;
                case "publish":
                    if (validate(args, 4, true, client, false)) {
                        val featureArgs = getModelFeatureArgs(client, args, 1);
                        if (featureArgs != null) {
                            if (realtimePubMode) {
                                publishMeasurePoint(client, featureArgs);
                            } else {
                                publishOfflineMeasurePoint(client, featureArgs);
                            }
                        }
                    }
                    break;
                case "publishEvent":
                    if (validate(args, 5, true, client, false)) {
                        val featureArgs = getModelFeatureArgs(client, args, 2);
                        if (featureArgs != null) {
                            publishEvent(client, args[1], featureArgs);
                        }
                    }
                    break;
                case "updateAttrs":
                    if (validate(args, 4, true, client, false)) {
                        val featureArgs = getModelFeatureArgs(client, args, 1);
                        if (featureArgs != null) {
                            updateAttributes(client, featureArgs);
                        }
                    }
                    break;
                case "registerMessageHandler":
                    if (validate(args, 1, false, client, false)) {
                        client.setArrivedMsgHandler(ServiceInvocationCommand.class, createServiceCommandHandler());
                        client.setArrivedMsgHandler(MeasurepointSetCommand.class, createMeasurepointSetHandler(client));
                        client.setArrivedMsgHandler(SubDeviceDisableCommand.class,
                                (IMessageHandler<SubDeviceDisableCommand, SubDeviceDisableReply>) (arrivedMessage, argList) -> {
                                    System.out.println("argList: " + argList + ", topic: " + arrivedMessage.getMessageTopic());
                                    return null;
                                });
                        client.setArrivedMsgHandler(SubDeviceDeleteCommand.class,
                                (IMessageHandler<SubDeviceDeleteCommand, SubDeviceDeleteReply>) (arrivedMessage, argList) -> {
                                    System.out.println("argList: " + argList + ", topic: " + arrivedMessage.getMessageTopic());
                                    return null;
                                });
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

    private static boolean validate(String[] args, int expectedArgLen, boolean allowMoreArgs,
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

            if ("multiply".equals(serviceName)) {
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

    private static void publishEvent(MqttClient client, String eventId, ModelFeatureArgs featureArgs) {
        try {
            EventPostRequest request = EventPostRequest.builder()
                    .setProductKey(featureArgs.getProductKey())
                    .setDeviceKey(featureArgs.getDeviceKey())
                    .setEventIdentifier(eventId)
                    .setValues(featureArgs.getValues())
                    .build();

            EventPostResponse response = client.publish(request);
            if (response.isSuccess()) {
                System.out.println("\nevent [" + eventId + "] published successfully");
            } else {
                System.out.println("failed to publish event [" + eventId + "]: " + response.getMessage());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void updateAttributes(MqttClient client, ModelFeatureArgs featureArgs) {
        try {
            AttributeUpdateRequest request = AttributeUpdateRequest.builder()
                    .setProductKey(featureArgs.getProductKey())
                    .setDeviceKey(featureArgs.getDeviceKey())
                    .setAttributes(featureArgs.getValues())
                    .build();

            AttributeUpdateResponse response = client.publish(request);
            if (response.isSuccess()) {
                System.out.println("\nattributes [" + featureArgs.getValues() + "] updated successfully");
            } else {
                System.out.println("failed to publish attributes " + featureArgs.getValues() + ": " + response.getMessage());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void publishMeasurePoint(MqttClient client, ModelFeatureArgs featureArgs) {
        try {
            MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                    .setProductKey(featureArgs.getProductKey())
                    .setDeviceKey(featureArgs.getDeviceKey())
                    .addMeasurePoints(featureArgs.getValues())
                    .build();

            final IMqttResponse response;
            if (ignoreInvalidMeasurePoints) {
                // Currently, we only support ignore feature for batch request
                MeasurepointPostBatchRequest batchReq = MeasurepointPostBatchRequest.builder()
                        .addRequest(request)
                        .setSkipInvalidMeasurepoints(true)
                        .build();
                response = client.publish(batchReq);
            } else {
                response = client.publish(request);
            }

            if (response.isSuccess()) {
                System.out.println("\nmeasure points " + featureArgs.getValues() + " published successfully");
            } else {
                System.out.println("failed to publish measure points " + featureArgs.getValues() + ": " + response.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void publishOfflineMeasurePoint(MqttClient client, ModelFeatureArgs featureArgs) {
        try {
            MeasurepointResumeRequest request = MeasurepointResumeRequest.builder()
                    .setProductKey(featureArgs.getProductKey())
                    .setDeviceKey(featureArgs.getDeviceKey())
                    .addMeasurePoints(featureArgs.getValues())
                    .build();

            final IMqttResponse response;
            if (ignoreInvalidMeasurePoints) {
                // Currently, we only support ignore feature for batch request
                MeasurepointResumeBatchRequest batchReq = MeasurepointResumeBatchRequest.builder()
                        .addRequest(request)
                        .setSkipInvalidMeasurepoints(true)
                        .build();
                response = client.publish(batchReq);
            } else {
                response = client.publish(request);
            }

            if (response.isSuccess()) {
                System.out.println("\nmeasure points " + featureArgs.getValues() + " published successfully");
            } else {
                System.out.println("failed to publish measure points " + featureArgs.getValues() + ": " + response.getMessage());
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

    @Nullable
    private static ModelFeatureArgs getModelFeatureArgs(MqttClient client, String[] inputArgs, int start) {
        int remainingLen = inputArgs.length - start;

        int numberOfValues = remainingLen / 3;
        int left = remainingLen - numberOfValues * 3;
        if (left != 0 && left != 2) {
            System.err.println("Error: invalid command line. Input help for more details");
            return null;
        }

        Map<String, Object> values = new HashMap<>(numberOfValues);
        for (int i = 0; i < numberOfValues; ++i) {
            String field = inputArgs[start++];
            Object value = inputArgs[start++];
            String vType = inputArgs[start++];

            if ("double".equals(vType)) {
                value = Double.valueOf((String) value);
            } else if ("float".equals(vType)) {
                value = Float.valueOf((String) value);
            } else if ("int".equals(vType)) {
                value = Integer.valueOf((String) value);
            } else if (!"string".equals(vType)) {
                System.err.println("Error: don't support measure point type [" + vType + "]");
                return null;
            }

            values.put(field, value);
        }

        String productKey = client.getProfile().getProductKey();
        String deviceKey = client.getProfile().getDeviceKey();

        Preconditions.checkState(start == inputArgs.length || start + 2 == inputArgs.length,
                "[BUG] argument parsing logic is buggy");
        if (start < inputArgs.length) {
            productKey = inputArgs[start];
            deviceKey = inputArgs[start + 1];
        }

        return new ModelFeatureArgs(productKey, deviceKey, values);
    }

    @AllArgsConstructor
    @Getter
    private static class ModelFeatureArgs {
        private String productKey;
        private String deviceKey;
        private Map<String, Object> values;
    }
}
