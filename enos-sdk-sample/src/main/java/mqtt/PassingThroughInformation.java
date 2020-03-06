package mqtt;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMessageHandler;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.ModelDownRawCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.ModelDownRawReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.ModelUpRawRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.ModelUpRawResponse;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PassingThroughInformation {
    // EnOS HTTP Broker URL, which can be obtained from Environment Information page in EnOS Console
    static final String BROKER_URL = "tcp://broker_url:11883";

    // Device credentials, which can be obtained from Device Details page in EnOS Console
    static final String PRODUCT_KEY = "productKey";
    static final String DEVICE_KEY = "deviceKey";
    static final String DEVICE_SECRET = "deviceSecret";

    static MqttClient client;

    // this is a sample handler to handle passing through information from Cloud
    static class PassingThroughInformationHandler implements IMessageHandler<ModelDownRawCommand, ModelDownRawReply> {
        @Override
        public ModelDownRawReply onMessage(ModelDownRawCommand arrivedMessage, List<String> argList)
                throws Exception {
            System.out.println(Hex.encodeHexString(arrivedMessage.getPayload()));
            // a passing through reply can be sent as byte array.
            // a script should be defined in EnOS Console to handle the passing through reply.
            return new ModelDownRawReply(Hex.decodeHex("11000004d2c80000000e".toCharArray()));
        }
    }

    public static void main(String[] args) throws Exception {
        // construct an MQTT client by static device credential
        // BROKER_URL is the URL of EnOS MQTT Broker for Devices, which can be obtained in Environment Information page in EnOS Console
        // ProductKey, DeviceKey and DeviceSecrect can be obtained in Device Details page in EnOS Console
        client = new MqttClient(BROKER_URL, PRODUCT_KEY, DEVICE_KEY, DEVICE_SECRET);

        // enable auto-reconnect feature
        client.getProfile().setConnectionTimeout(60).setAutoReconnect(true);

        // Sample: set handler to handle passing through information from Cloud
        client.setArrivedMsgHandler(ModelDownRawCommand.class, new PassingThroughInformationHandler());

        // connect to EnOS Cloud and register callbacks. onConnectSuccess method will be called 
        client.connect();

        int loop = 10000;
        while ((--loop) >= 0) {
            try {
                // send passing through information to EnOS Cloud
                sendPassingThroughInformation();
            } catch (Exception e) {
                e.printStackTrace();
            }

            TimeUnit.SECONDS.sleep(1L);
        }

        // disconnect EnOS Cloud
        try {
            client.disconnect();
        } catch (EnvisionException e) {
            e.printStackTrace();
        }
    }

    // Sample: send passing through information to EnOS Cloud
    static void sendPassingThroughInformation() throws EnvisionException, DecoderException {
        // a passing through information can be sent as byte array.
        // a script should be defined in EnOS Console to handle the passing through information.
        ModelUpRawRequest request = ModelUpRawRequest.builder()
                .setPayload(Hex.decodeHex("0100000014010004000025f502000a6162636465666768696a0300083ff23d70".toCharArray()))
                .build();

        ModelUpRawResponse response = client.publish(request);
        System.out.println(Hex.encodeHexString(response.getPayload()));
    }

}
