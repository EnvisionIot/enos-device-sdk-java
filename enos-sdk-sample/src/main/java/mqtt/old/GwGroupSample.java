package mqtt.old;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionError;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostBatchRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostBatchResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostResponse;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import mqtt.old.helper.Helper;

import java.util.Random;

@Slf4j
public class GwGroupSample {

    private final static String GW01_PK = "pk";
    private final static String GW01_DK = "dk";
    private final static String GW01_SECRET = "secret";

    private final static String GW02_PK = "pk";
    private final static String GW02_DK = "dk";
    private final static String GW02_SECRET = "secret";

    private final static String SUB_DEV01_PK = "pk";
    private final static String SUB_DEV01_DK = "dk";
    private final static String SUB_DEV01_SECRET = "secret";

    public static void main(String[] args) throws Exception {
        MqttClient gw01Client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(Helper.SERVER_URL, GW01_PK, GW01_DK, GW01_SECRET)
        ));
        gw01Client.connect();
        log.info("{} logined successfully", GW01_DK);

        MqttClient gw02Client = new MqttClient(new DefaultProfile(
                new NormalDeviceLoginInput(Helper.SERVER_URL, GW02_PK, GW02_DK, GW02_SECRET)
        ));
        gw02Client.connect();
        log.info("{} logined successfully", GW02_DK);

        try {
            assertFalse(publishSubDeviceMeasurePoints(gw01Client));
            assertFalse(publishSubDeviceMeasurePoints(gw02Client));

            // If a gateway from gateway group wants to manage a sub-device, it
            // must login the sub-device first.
            assertTrue(loginSubDevice(gw01Client));

            assertTrue(publishSubDeviceMeasurePoints(gw01Client));
            assertTrue(publishSubDeviceMeasurePointsUsingBatch(gw01Client));
            // gw02 can batch publish to multiple sub-devices but cannot publish to a single sub-device
            assertFalse(publishSubDeviceMeasurePoints(gw02Client));
            assertTrue(publishSubDeviceMeasurePointsUsingBatch(gw02Client));
            // Unable to log out via gw02 if log in via gw01; the same client must be used for both log in/log out
            assertFalse(logoutSubDevice(gw02Client));

            // The invalid operations from gw02 above should not affect gw01 to
            // continue managing the sub-device
            assertTrue(publishSubDeviceMeasurePointsUsingBatch(gw01Client));
            assertTrue(logoutSubDevice(gw01Client));

            // gw02 take over the sub-device by its own sub-device log in request
            assertTrue(loginSubDevice(gw02Client));

            assertTrue(publishSubDeviceMeasurePoints(gw02Client));
            assertTrue(publishSubDeviceMeasurePointsUsingBatch(gw02Client));
            // gw01 can batch publish to multiple sub-devices but cannot publish to a single sub-device
            assertFalse(publishSubDeviceMeasurePoints(gw01Client));
            assertTrue(publishSubDeviceMeasurePointsUsingBatch(gw01Client));
            // Unable to log out via gw01 if log in via gw02; the same client must be used for both log in/log out
            assertFalse(logoutSubDevice(gw01Client));

            // The invalid operations from gw01 above should not affect gw02 to
            // continue managing the sub-device
            assertTrue(publishSubDeviceMeasurePointsUsingBatch(gw02Client));
            assertTrue(logoutSubDevice(gw02Client));
        } finally {
            gw01Client.close();
            gw02Client.close();
        }

        log.info("HURRAY! The tests passed!");
    }

    private static BaseMqttResponse loginSubDevice(MqttClient client) throws Exception {
        SubDeviceLoginRequest request = SubDeviceLoginRequest.builder()
                .setSubDeviceInfo(SUB_DEV01_PK, SUB_DEV01_DK, SUB_DEV01_SECRET)
                .build();
        return client.publish(request);
    }

    private static BaseMqttResponse logoutSubDevice(MqttClient client) throws Exception {
        SubDeviceLogoutRequest request = SubDeviceLogoutRequest.builder()
                .setSubProductKey(SUB_DEV01_PK)
                .setSubDeviceKey(SUB_DEV01_DK)
                .build();
        return client.publish(request);
    }

    private static BaseMqttResponse publishSubDeviceMeasurePoints(MqttClient client) throws Exception {
        try {
            return client.publish(getSubDevMeasurePointReq());
        } catch (EnvisionException e) {
            if (e.getErrorCode() == EnvisionError.MQTT_CLIENT_SUBSCRIEBE_FAILED.getErrorCode()) {
                MeasurepointPostResponse response = new MeasurepointPostResponse();
                response.setCode(e.getErrorCode());
                response.setMessage(e.getMessage());
                return  response;
            }
            throw e;
        }
    }

    private static MeasurepointPostRequest getSubDevMeasurePointReq() {
        return MeasurepointPostRequest.builder()
                .setProductKey(SUB_DEV01_PK)
                .setDeviceKey(SUB_DEV01_DK)
                .setMeasurePoints(ImmutableMap.of(
                        "value", new Random().nextDouble(),
                        "temp", new Random().nextDouble()
                ))
                .build();
    }

    private static BaseMqttResponse publishSubDeviceMeasurePointsUsingBatch(MqttClient client) throws Exception {
        try {
            MeasurepointPostBatchRequest request = MeasurepointPostBatchRequest.builder()
                    .addRequest(getSubDevMeasurePointReq())
                    .build();
            return client.publish(request);
        } catch (EnvisionException e) {
            if (e.getErrorCode() == EnvisionError.MQTT_CLIENT_SUBSCRIEBE_FAILED.getErrorCode()) {
                MeasurepointPostBatchResponse response = new MeasurepointPostBatchResponse();
                response.setCode(e.getErrorCode());
                response.setMessage(e.getMessage());
                return  response;
            }
            throw e;
        }
    }

    private static void assertTrue(BaseMqttResponse response) {
        if (!response.isSuccess()) {
            throw new RuntimeException("expect succcess but it has error: " + response.getMessage());
        }
    }

    private static void assertFalse(BaseMqttResponse response) {
        if (response.isSuccess()) {
            throw new RuntimeException("expect failure but it's success: " + response);
        }
    }
}
