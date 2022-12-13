package mqtt;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMessageHandler;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.MeasurepointSetCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.MeasurepointSetReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume.MeasurepointResumeRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume.MeasurepointResumeResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tag.TagQueryRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tag.TagQueryResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tag.TagUpdateRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tag.TagUpdateResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SimpleSendReceive {
    // EnOS MQTT Broker URL, which can be obtained from Environment Information page in EnOS Console
    static final String BROKER_URL = "tcp://broker_url:11883";

    // Device credentials, which can be obtained from Device Details page in EnOS Console
    static final String PRODUCT_KEY = "productKey";
    static final String DEVICE_KEY = "deviceKey";
    static final String DEVICE_SECRET = "deviceSecret";

    static MqttClient client;

    // this is a sample handler to handle measurement point - temperature setting
    static class TemperatureSetHandler implements IMessageHandler<MeasurepointSetCommand, MeasurepointSetReply> {
        @Override
        public MeasurepointSetReply onMessage(MeasurepointSetCommand arrivedMessage, List<String> argList)
                throws Exception {
            System.out.println(arrivedMessage);
            if (arrivedMessage.<Map<String, String>>getParams().containsKey("temperature")) {
                return MeasurepointSetReply.builder()
                        .setCode(200)           // 200 means a success reply
                        .build();
            } else {
                return MeasurepointSetReply.builder()
                        .setCode(405)           // device can make a negative reply
                        .setMessage("measurepoint setting not allowed")
                        .build();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        // construct an MQTT client by static device credential
        // BROKER_URL is the URL of EnOS MQTT Broker for Devices, which can be obtained in Environment Information page in EnOS Console
        // ProductKey, DeviceKey and DeviceSecrect can be obtained in Device Details page in EnOS Console
        client = new MqttClient(BROKER_URL, PRODUCT_KEY, DEVICE_KEY, DEVICE_SECRET);

        // enable auto-reconnect feature
        client.getProfile().setConnectionTimeout(60).setAutoReconnect(true);

        // Sample: set handler to handle measurepoint set commands
        client.setArrivedMsgHandler(MeasurepointSetCommand.class, new TemperatureSetHandler());


        // connect to EnOS Cloud and register callbacks. onConnectSuccess method will be called
        client.connect();

        int loop = 10000;
        while ((--loop) >= 0) {
            try {
                // device tags samples
                queryDeviceTags();
                updateDeviceTags();

                // device attributes samples
                queryDeviceAttributes();
                updateDeviceAttributes();

                // reporting measurepoints
                postMeasurepoint();
                resumeMeasurepoint();

                // reporting events
                postEvent();
            } catch (EnvisionException e) {
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

    // Sample: query device tags
    static void queryDeviceTags() throws EnvisionException {
        // device tags can be set and viewed on Device Details page in EnOS Console
        TagQueryRequest request = TagQueryRequest.builder()
                .addKey("tag1").addKey("tag2")
                .build();

        TagQueryResponse response = client.publish(request);
        System.out.println(response);
    }

    // Sample: update device tags
    static void updateDeviceTags() throws EnvisionException {
        // device tags can be set and viewed on Device Details page in EnOS Console
        TagUpdateRequest request = TagUpdateRequest.builder()
                .addTag("Temperature", "36.8")
                .build();

        TagUpdateResponse response = client.publish(request);
        System.out.println(response);
    }

    // Sample: query device attributes
    static void queryDeviceAttributes() throws EnvisionException {
        // device attributes are defined in ThingModel, which can be viewed in Model Details page in EnOS Console
        AttributeQueryRequest request = AttributeQueryRequest.builder()
                .addAttribute("attr1").addAttribute("attr2")
                .build();

        AttributeQueryResponse response = client.publish(request);
        System.out.println(response);
    }

    // Sample: update device attributes
    static void updateDeviceAttributes() throws EnvisionException {
        // device attributes are defined in ThingModel, which can be viewed in Model Details page in EnOS Console
        AttributeUpdateRequest request = AttributeUpdateRequest.builder()
                .addAttribute("Temperature", 36.8)
                .build();

        AttributeUpdateResponse response = client.publish(request);
        System.out.println(response);
    }

    // Sample: post measurepoint
    static void postMeasurepoint() throws EnvisionException {
        // device measurement points are defined in ThingModel, which can be viewed in Model Details page in EnOS Console
        MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                .addMeasreuPointWithQuality("Power", 1, 9)
                .addMeasurePoint("temp", 1.02)
                .addMeasurePoint("branchCurr", Arrays.asList(1.02, 2.02, 7.93))
                .build();

        MeasurepointPostResponse response = client.publish(request);
        System.out.println(response);
    }

    // Sample: post event
    static void resumeMeasurepoint() throws EnvisionException {
        // device measurement points are defined in ThingModel, which can be viewed in Model Details page in EnOS Console
        MeasurepointResumeRequest request = MeasurepointResumeRequest.builder()
                .addMeasreuPointWithQuality("Power", 1, 9)
                .addMeasurePoint("temp", 1.02)
                .addMeasurePoint("branchCurr", Arrays.asList(1.02, 2.02, 7.93))
                .build();

        MeasurepointResumeResponse response = client.publish(request);
        System.out.println(response);
    }

    // Sample: post event
    static void postEvent() throws EnvisionException {
        // device events are defined in ThingModel, which can be viewed in Model Details page in EnOS Console
        EventPostRequest request = EventPostRequest.builder()
                .setEventIdentifier("events")
                .addValue("temp", 1.02)
                .addValue("branchCurr", Arrays.asList(1.02, 2.02, 7.93))
                .build();

        EventPostResponse response = client.publish(request);
        System.out.println(response);
    }
}
