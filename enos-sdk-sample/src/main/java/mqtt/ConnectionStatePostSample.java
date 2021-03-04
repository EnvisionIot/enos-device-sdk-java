package mqtt;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.connection.ConnectionState;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.connection.ConnectionStatePostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.connection.ConnectionStatePostResponse;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import mqtt.old.helper.Helper;

import java.util.concurrent.TimeUnit;

@Slf4j
public class ConnectionStatePostSample {
    private static final String BROKER_URL = Helper.SERVER_URL;

    private static final String PRODUCT_KEY = "YOUR_PRODUCT_KEY";
    private static final String DEVICE_KEY = "YOUR_DEVICE_KEY";
    private static final String DEVICE_SECRET = "YOUR_DEVICE_SECRET";

    public static void main(String[] args) throws Exception {
        MqttClient mqttClient = new MqttClient(BROKER_URL, PRODUCT_KEY, DEVICE_KEY, DEVICE_SECRET);

        // block until the connection completed
        mqttClient.connect();

        for (int i = 0; i < 100; ++i) {
            if ((i % 6) == 0) {
                postFullConnectionState(mqttClient);
            } else {
                postUpdateConnectionState(mqttClient);
            }
            TimeUnit.SECONDS.sleep(5);
        }

        mqttClient.close();
    }

    private static void postFullConnectionState(MqttClient mqttClient) throws Exception {
        ConnectionStatePostRequest request = ConnectionStatePostRequest.builder()
                .isFull(true)
                // For root node, if the corresponding device assetId is not provided, EnOS cloud would auto
                // populate the assetId based on the device information.
                .addNode("#001", ConnectionState.CONNECTED, null, "name1", "reconnect by xxx")
                .addNode("#002", ConnectionState.DISCONNECTED, null, "connection1", "no heartbeat")
                .addNode("#003", ConnectionState.CONNECTED, "assetId3", "name3", "")
                .addNode("#004", ConnectionState.CONNECTED, null, "name4", "")
                .addNode("#005", ConnectionState.CONNECTED, "assetId5", "name5", "")
                .addRelations("#001", Lists.newArrayList("#002", "#003"))
                .addRelations("#001", "#004")
                .addRelations("#004", "#005")
                .build();
        log.info("request: {}", request);

        ConnectionStatePostResponse response = mqttClient.publish(request);
        log.info("response: {}", response);
    }

    private static void postUpdateConnectionState(MqttClient mqttClient) throws Exception {
        ConnectionStatePostRequest request = ConnectionStatePostRequest.builder()
                .isFull(false)
                .addNode("#002", ConnectionState.CONNECTED, null, "connection1", "device reconnected")
                .addNode("#003", ConnectionState.DISCONNECTED, "assetId3", "name3", "connection lost")
                .build();
        log.info("request: {}", request);

        ConnectionStatePostResponse response = mqttClient.publish(request);
        log.info("response: {}", response);
    }
}
