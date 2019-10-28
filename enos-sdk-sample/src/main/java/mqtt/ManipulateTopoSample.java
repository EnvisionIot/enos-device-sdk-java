package mqtt;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.LoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.topo.*;
import mqtt.helper.Helper;

public class ManipulateTopoSample {
    private final static String SERVER_URL = "tcp://localhost:11883";
//    private final static String SERVER_URL = "tcp://beta-iot-as-mqtt-cn4.eniot.io:11883";

    private final static String GW01_PK = "Lx5Q1X6M";
    private final static String GW01_DK = "will_gw01";
    private final static String GW01_SECRET = "JE3cLRjvAxQWIU3sPFPv";

    // Use sign method SHA1
    private static final SubDeviceInfo subDev1 = new SubDeviceInfo(
            "x4jwTsoz",
            "will_dev01",
            "t0FEw4xvOD8zokuRpHJB",
            SignMethod.SHA1);

    // Use sign method SHA256
    private static final SubDeviceInfo subDev2 = new SubDeviceInfo(
            "x4jwTsoz",
            "FfHtfAyhC5",
            "DVCWsRQHeIOvryFy7fIz",
            SignMethod.SHA256);

    private static void doDelete(final MqttClient client) throws Exception {
        TopoDeleteRequest req = TopoDeleteRequest.builder()
                .addSubDevice(subDev1.getProductKey(), subDev1.getDeviceKey())
                .addSubDevice(subDev2.getProductKey(), subDev2.getDeviceKey())
                .build();
        TopoDeleteResponse rsp = client.publish(req);
        if (!rsp.isSuccess()) {
            System.out.println("Error: " + rsp.getMessage());
        }
    }

    private static TopoGetResponse doGet(final MqttClient client) throws Exception {
        TopoGetRequest req = TopoGetRequest.builder().build();
        TopoGetResponse rsp = client.publish(req);
        if (!rsp.isSuccess()) {
            throw new RuntimeException(rsp.toString());
        }
        return rsp;
    }

    private static void doAdd(final MqttClient client) throws Exception {
        TopoAddRequest req = TopoAddRequest.builder().addSubDevice(subDev1).addSubDevice(subDev2).build();
        TopoAddResponse rsp = client.publish(req);
        if (!rsp.isSuccess()) {
            throw new RuntimeException(rsp.toString());
        }
    }

    public static void main(String[] args) throws Exception {
        LoginInput input = new NormalDeviceLoginInput(SERVER_URL, GW01_PK, GW01_DK, GW01_SECRET);
        final MqttClient client = new MqttClient(new DefaultProfile(input));
        client.connect();

        try {
            // 1) remove potential topos
            doDelete(client);

            // 2) fetch current topos
            TopoGetResponse getRsp = doGet(client);

            int subCount = getRsp.getSubDeviceInfo().size();

            // 3) add new topos
            doAdd(client);

            // 4) validate current topo size
            getRsp = doGet(client);
            if (getRsp.getSubDeviceInfo().size() != subCount + 2) {
                throw new RuntimeException("current sub device count is not expected");
            }

            // 5) delete the added topos again
            doDelete(client);

            // 6) validate current topo size
            getRsp = doGet(client);
            if (getRsp.getSubDeviceInfo().size() != subCount) {
                throw new RuntimeException("current sub device count is not expected");
            }

            System.out.println("hurray! Sample tests passed !");
        } finally {
            Helper.cleanConnection(client);
        }


    }

}
