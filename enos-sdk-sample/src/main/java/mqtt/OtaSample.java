package mqtt;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.OTAUpdateFailureCause;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMessageHandler;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttDeliveryMessage;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.ota.OtaUpgradeCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.ota.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * device firmware over-the-air
 */
public class OtaSample {

    // EnOS HTTP Broker URL, which can be obtained from Environment Information page in EnOS Console
    static final String BROKER_URL = "tcp://broker_url:11883";

    // Gateway credentials, which can be obtained from Device Details page in EnOS Console
    static final String GW_PRODUCT_KEY = "productKey";
    static final String GW_DEVICE_KEY = "deviceKey";
    static final String GW_DEVICE_SECRET = "deviceSecret";

    static MqttClient client;

    public static void main(String[] args) throws Exception {
        client = new MqttClient(new DefaultProfile(BROKER_URL, GW_PRODUCT_KEY, GW_DEVICE_KEY, GW_DEVICE_SECRET));

        // register arrived msg handler to handle cloud-publish firmware upgrade
        setOtaUpgradeMessageHandler();

        System.out.println("start connect with callback ... ");
        client.connect();

        //report firmware version firstly
        reportVersion("initVersion");

        // or otherwise, in arrived msg handler, you may reject the upgrade
        //reportUpgradeFailureCause();

//        upgradeFirmwareByDeviceReq();
        while (true) {
            TimeUnit.SECONDS.sleep(5);
        }
    }

    public static void setOtaUpgradeMessageHandler() {
        client.setArrivedMsgHandler(OtaUpgradeCommand.class, new IMessageHandler<OtaUpgradeCommand, IMqttDeliveryMessage>() {
            @Override
            public IMqttDeliveryMessage onMessage(OtaUpgradeCommand otaUpgradeCommand, List<String> list) throws Exception {
                System.out.println("receive command: " + otaUpgradeCommand);

                @SuppressWarnings("unused")
                Firmware firmware = otaUpgradeCommand.getFirmwareInfo();

                //TODO: download firmware from firmware.fileUrl

                //mock reporting progress
                reportUpgradeProgress("20", "20");
                TimeUnit.SECONDS.sleep(3);

                reportUpgradeProgress("25", "25");
                TimeUnit.SECONDS.sleep(5);

                reportUpgradeProgress("80", "80");
                TimeUnit.SECONDS.sleep(10);

                //firmware upgrade success, report new version
                reportVersion(otaUpgradeCommand.getFirmwareInfo().version);

                return null;
            }
        });
    }

    public static void reportUpgradeFailureCause() throws Exception {
        client.setArrivedMsgHandler(OtaUpgradeCommand.class, new IMessageHandler<OtaUpgradeCommand, IMqttDeliveryMessage>() {
            @Override
            public IMqttDeliveryMessage onMessage(OtaUpgradeCommand otaUpgradeCommand, List<String> list) throws Exception {
                // device ignore upgrade
                reportUpgradeProgress(OTAUpdateFailureCause.DEVICE_IGNORED_THIS_UPGRADE_CODE, "");
                return null;
            }
        });
    }

    public static void upgradeFirmwareByDeviceReq() throws Exception {
        List<Firmware> firmwareList = getFirmwaresFromCloud();
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
            // start firmware upgrade, download firmware
            reportUpgradeProgress("20", "20");
            TimeUnit.SECONDS.sleep(3);
            reportUpgradeProgress("80", "80");
            TimeUnit.SECONDS.sleep(3);
            reportVersion(version);
        }
    }

    public static void reportVersion(String version) throws Exception {
        OtaVersionReportRequest.Builder builder = new OtaVersionReportRequest.Builder();
        builder.setProductKey(GW_PRODUCT_KEY).setDeviceKey(GW_DEVICE_KEY)
               .setVersion(version);
        OtaVersionReportRequest request = builder.build();
        System.out.println("send =>" + request.toString());
        OtaVersionReportResponse otaVersionReportResponse = client.publish(builder.build());
        System.out.println(otaVersionReportResponse);
    }

    private static void reportUpgradeProgress(String progress, String desc) throws Exception  {
        OtaProgressReportRequest.Builder builder = new OtaProgressReportRequest.Builder();
        builder.setStep(progress).setDesc(desc);
        OtaProgressReportResponse otaProgressReportResponse = client.publish(builder.build());
        System.out.println(otaProgressReportResponse);
    }

    private static List<Firmware> getFirmwaresFromCloud() throws Exception {
        OtaGetVersionRequest.Builder builder = new OtaGetVersionRequest.Builder();
        builder.setProductKey(GW_PRODUCT_KEY).setDeviceKey(GW_DEVICE_KEY);
        OtaGetVersionRequest request = builder.build();
        OtaGetVersionResponse response = client.publish(request);
        System.out.println("send getversion request =>" + request.toString());
        System.out.println("receive getversion response =>" + response.toString());
        return response.getFirmwareList();
    }
}

