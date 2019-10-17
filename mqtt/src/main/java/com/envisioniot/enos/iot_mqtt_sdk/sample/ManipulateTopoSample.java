package com.envisioniot.enos.iot_mqtt_sdk.sample;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.LoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.topo.*;
import com.envisioniot.enos.iot_mqtt_sdk.sample.common.Helper;

public class ManipulateTopoSample {
    // Use sign method SHA1
    private static final SubDeviceInfo subDev1 = new SubDeviceInfo(
            "aVpQQTDp",
            "96DI3sDA9v",
            "GCnufouOrmEtj8bxUtKd",
            SignMethod.SHA1);

    // Use sign method SHA256
    private static final SubDeviceInfo subDev2 = new SubDeviceInfo(
            "aVpQQTDp",
            "WSIVIDEvLw",
            "qfZ6YDAC9B8MtBioVNAe",
            SignMethod.SHA256);

    private static void doDelete(final MqttClient client) throws Exception {
        TopoDeleteRequest req = TopoDeleteRequest.builder()
                .addSubDevice(subDev1.getProductKey(), subDev1.getDeviceKey())
                .addSubDevice(subDev2.getProductKey(), subDev2.getDeviceKey())
                .build();
        TopoDeleteResponse rsp = client.publish(req);
        if (!rsp.isSuccess()) {
            throw new RuntimeException(rsp.toString());
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
        LoginInput input = new NormalDeviceLoginInput(Helper.SERVER_URL, Helper.GW_PRODUCT_KEY, Helper.GW_DEV_KEY, Helper.GW_DEV_SECRET);
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
