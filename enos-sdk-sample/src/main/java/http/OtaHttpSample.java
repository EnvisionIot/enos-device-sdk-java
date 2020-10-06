package http;

import com.envisioniot.enos.iot_http_sdk.HttpConnection;
import com.envisioniot.enos.iot_http_sdk.SessionConfiguration;
import com.envisioniot.enos.iot_http_sdk.StaticDeviceCredential;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMessageHandler;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttDeliveryMessage;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.ota.OtaUpgradeCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.ota.*;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostResponse;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This sample shows how to upgrade firmware over HTTP to EnOS IoT.
 * @author dexiang.guo
 */

public class OtaHttpSample
{
    // EnOS HTTP Broker URL, which can be obtained from Environment Information page in EnOS Console
    static final String BROKER_URL = "http://url";

    // Device credentials, which can be obtained from Device Details page in EnOS Console
    static final String PRODUCT_KEY = "productKey";
    static final String DEVICE_KEY = "deviceKey";
    static final String DEVICE_SECRET = "deviceSecret";
    
    private static MeasurepointPostRequest buildMeasurepointPostRequest()
    {
        // Measurepoints are defined in ThingModel
        return MeasurepointPostRequest.builder()
              .addMeasurePoint("gdx_di_001", 100)
              .build();
    }

    private static void reportVersion(HttpConnection connection, String version) {
        OtaVersionReportRequest otaReportVersionRequest = OtaVersionReportRequest.builder()
                .setVersion(version).setProductKey(PRODUCT_KEY).setDeviceKey(DEVICE_KEY).build();
        try {
            OtaVersionReportResponse otaVersionReportResponse = connection.publish(otaReportVersionRequest, null);
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(otaVersionReportResponse));
        } catch (EnvisionException | IOException e) {
            e.printStackTrace();
        }
    }

    private static List<Firmware> getFirmwaresFromCloud(HttpConnection connection) {
        OtaGetVersionRequest.Builder builder = new OtaGetVersionRequest.Builder();
        builder.setProductKey(PRODUCT_KEY).setDeviceKey(DEVICE_KEY);
        OtaGetVersionRequest request = builder.build();
        OtaGetVersionResponse response;
        try {
            response = connection.publish(request, null);
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(response));
            System.out.println("send getversion request =>" + request.toString());
            System.out.println("receive getversion response =>" + response.toString());
            return response.getFirmwareList();
        } catch (EnvisionException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void upgradeFirmwareByDeviceReq(HttpConnection connection) throws InterruptedException {
        List<Firmware> firmwareList = getFirmwaresFromCloud(connection);
        String version = null;
        for (Firmware firmware : firmwareList) {
            version = firmware.version;
            StringBuffer sb = new StringBuffer();
            sb.append("Firmware=>[");
            sb.append("version=" + firmware.version);
            sb.append("signMethod=" + firmware.signMethod);
            sb.append("sign=" + firmware.sign);
            sb.append("fileUrl=" + firmware.fileUrl);
            sb.append("fileSize=" + firmware.fileSize);
            sb.append("]");
            System.out.println(sb.toString());
        }
        if (version != null) {
            reportUpgradeProgress(connection,"20", "20");
            TimeUnit.SECONDS.sleep(10);
            reportUpgradeProgress(connection,"80", "80");
            TimeUnit.SECONDS.sleep(20);
            reportVersion(connection, version);
        }
    }

    private static void reportUpgradeProgress(HttpConnection connection, String progress, String desc){
        OtaProgressReportRequest.Builder builder = new OtaProgressReportRequest.Builder();
        builder.setStep(progress).setDesc(desc);
        try {
            OtaProgressReportResponse otaProgressReportResponse = connection.publish(builder.build(), null);
            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(otaProgressReportResponse));
        } catch (EnvisionException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void upgradeFirmwareByCloudPush(HttpConnection connection) {
        connection.setArrivedMsgHandler(OtaUpgradeCommand.class, new IMessageHandler<OtaUpgradeCommand, IMqttDeliveryMessage>() {
            @Override
            public IMqttDeliveryMessage onMessage(OtaUpgradeCommand otaUpgradeCommand, List<String> list) throws Exception {
                System.out.println("receive command: " + otaUpgradeCommand);

                Firmware firmware = otaUpgradeCommand.getFirmwareInfo();

                //TODO: download firmware from firmware.fileUrl

                //mock reporting progress
                reportUpgradeProgress(connection, "20", "20");
                TimeUnit.SECONDS.sleep(2);

                reportUpgradeProgress(connection, "25", "25");
                TimeUnit.SECONDS.sleep(20);

                reportUpgradeProgress(connection, "80", "80");
                TimeUnit.SECONDS.sleep(20);

                //firmware upgrade success, report new version
                reportVersion(connection, otaUpgradeCommand.getFirmwareInfo().version);

                return null;
            }
        });
    }

    public static void main(String[] args) throws InterruptedException
    {
        // construct a static device credential via ProductKey, DeviceKey and DeviceSecret
        StaticDeviceCredential credential = new StaticDeviceCredential(
                PRODUCT_KEY, DEVICE_KEY, DEVICE_SECRET);
        
        // construct a http connection
        SessionConfiguration configuration = SessionConfiguration.builder().lifetime(30_000).build();

        HttpConnection connection = new HttpConnection.Builder(BROKER_URL, credential)
                .sessionConfiguration(configuration)
                .build();
        while (true) {
            upgradeFirmwareByCloudPush(connection);
//            reportVersion(connection, "2.1");
//            upgradeFirmwareByDeviceReq(connection);
            //POST measurepoints
            MeasurepointPostRequest request = buildMeasurepointPostRequest();
            try {
                MeasurepointPostResponse respones = connection.publish(request, null);
                System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(respones));
            } catch (EnvisionException | IOException e) {
                e.printStackTrace();
                break;
            }


            // Wait for more than life time, the connection shall automatically re-auth
            System.out.println("current sessionId: " + connection.getSessionId());
            Thread.currentThread().sleep(10000);
        }
        
    }

}

