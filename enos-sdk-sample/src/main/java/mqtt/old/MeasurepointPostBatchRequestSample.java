package mqtt.old;

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
import com.google.common.collect.ImmutableMap;

import mqtt.old.helper.BaseConnectCallback;

import static mqtt.old.helper.Helper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * This sample shows how sub-devices login/logout and publish
 * measure points to broker.
 *
 * @author jian.zhang4
 */
public class MeasurepointPostBatchRequestSample {
    final static Random rand = new Random();

    public static void main(String[] args) throws InterruptedException {

        //success
        testNormalCase(true, true);
        //success
        testNormalCase(true, false);
        //success
        testNormalCase(false, true);
        //success
        testNormalCase(false, false);


        //sucess
        testOneInvalidMP(true, true);
        //success
        testOneInvalidMP(true, false);
        //failure
        testOneInvalidMP(false, true);
        //failure
        testOneInvalidMP(false, false);


        //success
        testOneOfflineDev(true, true);
        //failure
        testOneOfflineDev(true, false);
        //success
        testOneOfflineDev(false, true);
        //failure
        testOneOfflineDev(false, false);


        //success
        testOneDevWithOfflineAndInvalidMP(true, true);
        //failure
        testOneDevWithOfflineAndInvalidMP(true, false);
        //failure
        testOneDevWithOfflineAndInvalidMP(false, false);
        //failure
        testOneDevWithOfflineAndInvalidMP(false, true);


        //success
        testOneDevWithOfflineAndAnotherdevWithInvalidMP(true, true);
        //false
        testOneDevWithOfflineAndAnotherdevWithInvalidMP(true, false);
        //false
        testOneDevWithOfflineAndAnotherdevWithInvalidMP(false, false);
        //false
        testOneDevWithOfflineAndAnotherdevWithInvalidMP(false, true);

    }

    // testNormalCase (two on-line devices with valid measurepoints) [flat 4]
    public static void testNormalCase(boolean skipInvalid, boolean allowoffline) throws InterruptedException {
        System.out.println();
        System.out.println("<-------------------------------------------->");
        System.out.println("test for normal case, " + "skipInvalid = " + skipInvalid + ", allowoffline = " + allowoffline);
        List<DeviceCredential> loginedSubDevices = new ArrayList<>();
        List<DeviceCredential> publishSubDevices = new ArrayList<>();

        loginedSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET));
        loginedSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV02_KEY, SUB_DEV02_SECRET));

        publishSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET));
        publishSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV02_KEY, SUB_DEV02_SECRET));

        Map validMeasurepoints = ImmutableMap.of("temp", rand.nextDouble(), "value", rand.nextDouble());

        testSubDevicesByManuallyLogin(allowoffline, skipInvalid, loginedSubDevices, publishSubDevices, validMeasurepoints, null);

    }

    // testOneOfflineDev (two on-line devices with valid measurepoints) [flat 4]
    public static void testOneOfflineDev(boolean skipInvalid, boolean allowoffline) throws InterruptedException {
        System.out.println("<-------------------------------------------->");
        System.out.println("test for OneOfflineDev, " + "skipInvalid = " + skipInvalid + ", allowoffline = " + allowoffline);
        List<DeviceCredential> loginedSubDevices = new ArrayList<>();
        List<DeviceCredential> publishSubDevices = new ArrayList<>();

        loginedSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET));

        publishSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET));
        publishSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV02_KEY, SUB_DEV02_SECRET));

        Map validMeasurepoints = ImmutableMap.of("temp", rand.nextDouble(), "value", rand.nextDouble());

        testSubDevicesByManuallyLogin(allowoffline, skipInvalid, loginedSubDevices, publishSubDevices, validMeasurepoints, null);

    }

    // testOneInvalidMP (two on-line devices with valid measurepoints) [flat 4]
    public static void testOneInvalidMP(boolean skipInvalid, boolean allowoffline) throws InterruptedException {
        System.out.println("<-------------------------------------------->");
        System.out.println("test for OneInvalidMP, " + "skipInvalid = " + skipInvalid + ", allowoffline = " + allowoffline);
        List<DeviceCredential> loginedSubDevices = new ArrayList<>();
        List<DeviceCredential> publishSubDevices = new ArrayList<>();

        loginedSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET));
        loginedSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV02_KEY, SUB_DEV02_SECRET));

        publishSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET));
        publishSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV02_KEY, SUB_DEV02_SECRET));

        Map validMeasurepoints = ImmutableMap.of("temp", rand.nextDouble(), "value", rand.nextDouble());
        Map invalidMeasurepoints = ImmutableMap.of("temptest", rand.nextDouble(), "valuetest", rand.nextDouble());


        testSubDevicesByManuallyLogin(allowoffline, skipInvalid, loginedSubDevices, publishSubDevices, validMeasurepoints, invalidMeasurepoints);
    }

    // testOneDevWithOfflineAndInvalidMP (two on-line devices with valid measurepoints) [flat 4]
    public static void testOneDevWithOfflineAndInvalidMP(boolean skipInvalid, boolean allowoffline) throws InterruptedException {
        System.out.println("<-------------------------------------------->");
        System.out.println("test for OneDevWithOfflineAndInvalidMP, " + "skipInvalid = " + skipInvalid + ", allowoffline = " + allowoffline);
        List<DeviceCredential> loginedSubDevices = new ArrayList<>();
        List<DeviceCredential> publishSubDevices = new ArrayList<>();

        loginedSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET));

        publishSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET));
        publishSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV02_KEY, SUB_DEV02_SECRET));

        Map validMeasurepoints = ImmutableMap.of("temp", rand.nextDouble(), "value", rand.nextDouble());
        Map invalidMeasurepoints = ImmutableMap.of("temptest", rand.nextDouble(), "valuetest", rand.nextDouble());


        testSubDevicesByManuallyLogin(allowoffline, skipInvalid, loginedSubDevices, publishSubDevices, validMeasurepoints, invalidMeasurepoints);
    }

    //testOneDevWithOfflineAndAnotherdevWithInvalidMP
    public static void testOneDevWithOfflineAndAnotherdevWithInvalidMP(boolean skipInvalid, boolean allowoffline) throws InterruptedException {
        System.out.println("<-------------------------------------------->");
        System.out.println("test for OneDevWithOfflineAndAnotherdevWithInvalidMP, " + "skipInvalid = " + skipInvalid + ", allowoffline = " + allowoffline);
        List<DeviceCredential> loginedSubDevices = new ArrayList<>();
        List<DeviceCredential> publishSubDevices = new ArrayList<>();

        loginedSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET));

        publishSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV02_KEY, SUB_DEV02_SECRET));
        publishSubDevices.add(new DeviceCredential(DEV_PRODUCT_KEY, null, SUB_DEV01_KEY, SUB_DEV01_SECRET));

        Map validMeasurepoints = ImmutableMap.of("temp", rand.nextDouble(), "value", rand.nextDouble());
        Map invalidMeasurepoints = ImmutableMap.of("temptest", rand.nextDouble(), "valuetest", rand.nextDouble());

        testSubDevicesByManuallyLogin(allowoffline, skipInvalid, loginedSubDevices, publishSubDevices, validMeasurepoints, invalidMeasurepoints);
    }


    private static void testSubDevicesByManuallyLogin(boolean allowoffline, boolean skipInvalid, final List<DeviceCredential> loginedSubDevices, List<DeviceCredential> publishSubDevices, Map measurepoints, Map invalidMeasurepints) throws InterruptedException {
        MqttClient client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(SERVER_URL, GW_PRODUCT_KEY, GW_DEV_KEY, GW_DEV_SECRET)
        ));
        client.connect(new BaseConnectCallback(client, null, false) {

            @Override
            protected void onSuccess(MqttClient client) {

                loginSubDevices(client, loginedSubDevices);
                if (!loginedSubDevices.isEmpty()) {
                    publishMeasurePointsFor(client, publishSubDevices, allowoffline, skipInvalid, measurepoints, invalidMeasurepints);
                }
                logoutSubDevices(client, loginedSubDevices);

                client.close();

            }
        });

        Thread.sleep(6000);
    }

    /**
     * Manually login sub-devices
     */
    private static void loginSubDevices(final MqttClient client, List<DeviceCredential> loginedSubDevices) {
        loginedSubDevices.forEach(dev -> {
            try {
                SubDeviceLoginRequest request = SubDeviceLoginRequest.builder().setSubDeviceInfo(dev).build();
                SubDeviceLoginResponse response = client.publish(request);
                if (response.isSuccess()) {
                    System.out.println("successfully logined sub-device: " + dev.getDeviceKey());
                } else {
                    System.err.println("failed to login sub-device: " + dev.getDeviceKey());
                }
            } catch (EnvisionException e) {
                e.printStackTrace();
            }
        });
    }


    private static void publishMeasurePointsFor(final MqttClient client, List<DeviceCredential> subDevices, boolean allow, boolean skip, Map measurepoints, Map invalidMeasurepoints) {

        List<MeasurepointPostRequest> requests = new ArrayList<>();
        requests.add(MeasurepointPostRequest.builder()
                .setProductKey(subDevices.get(0).getProductKey())
                .setDeviceKey(subDevices.get(0).getDeviceKey())
                .addMeasurePoints(measurepoints)
                .build());

        if(invalidMeasurepoints == null){
            invalidMeasurepoints = measurepoints;
        }
        requests.add(MeasurepointPostRequest.builder()
                .setProductKey(subDevices.get(1).getProductKey())
                .setDeviceKey(subDevices.get(1).getDeviceKey())
                .addMeasurePoints(invalidMeasurepoints)
                .build());

        // Send the requests in batch
        try {
            MeasurepointPostBatchResponse response = client.publish(
                    MeasurepointPostBatchRequest.builder().setRequests(requests).setAllowOfflineSubDevice(allow).setSkipInvalidMeasurepoints(skip).build());
            if (response.isSuccess()) {
                System.out.println("sent " + requests.toString() + " measure points");
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
//                if (response.isSuccess()) {
//                    System.out.println("successfully logged out sub-device: " + dev.getDeviceKey());
//                } else {
//                    System.err.println("failed to log out sub-device: " + dev.getDeviceKey());
//                }
            } catch (EnvisionException e) {
                e.printStackTrace();
            }
        });
    }
}
