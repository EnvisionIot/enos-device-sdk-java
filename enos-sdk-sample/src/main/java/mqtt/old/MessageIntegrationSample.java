package mqtt.old;

import com.envisioniot.enos.iot_mqtt_sdk.core.IResponseCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration.*;
import com.google.common.collect.ImmutableMap;

import mqtt.old.helper.BaseConnectCallback;
import mqtt.old.helper.Helper;

import org.apache.commons.codec.binary.Hex;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Message integration sample.
 *
 * This sample covers message integration for measure points, events,
 * attributes and raw data. And we tried publishing the message using
 * both sync and async way.
 *
 * @author jian.zhang4
 */
public class MessageIntegrationSample {

    private static final String[] DEVICES = new String[]{Helper.DEV01_KEY, Helper.DEV02_KEY};
    private static final int ROUNDS = 10;
    private static final AtomicInteger counter = new AtomicInteger();

    public static void main(String[] args) {
        MqttClient client = new MqttClient(new DefaultProfile(Helper.getMessageIntegrationLoginInput()));

        client.connect(new BaseConnectCallback(client, "message integration", true) {
            @Override
            protected void onSuccess(MqttClient client) {
                performTest(client);
            }
        });
    }

    private static void performTest(final MqttClient client) {
        for (int round = 0; round < ROUNDS; ++round) {
            System.out.println("Round #" + round);

            publishMeasurepoints(client, round);
            publishEvents(client, round);
            publishAttributes(client, round);
        }

        try {
            // wait for async publish to complete
            System.out.println("waiting aysnc requests to be completed ... ");
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            // ignore this
        }

        if (counter.intValue() != ROUNDS * 3) {
            System.err.println("received asyn responses: " + counter.intValue() + "but expect " + ROUNDS);
        }
    }

    private static void publishMeasurepoints(final MqttClient client, int round) {
        IntMeasurepointPostRequest.Builder builder = new IntMeasurepointPostRequest.Builder();

        for (String deviceKey: DEVICES) {
            builder.addMeasurepoint(deviceKey, System.currentTimeMillis(),
                    ImmutableMap.of("temp", 90.5 + round, "value", 21.0 + round));
        }

        IntMeasurepointPostRequest request = builder.build();

        processRequest(client, request, "measure points");
        processRequestAsync(client, request, "measure points");
    }

    private static void publishEvents(final MqttClient client, int round) {
        IntEventPostRequest.Builder builder = new IntEventPostRequest.Builder();

        for (String deviceKey: DEVICES) {
            builder.addEvent(deviceKey, System.currentTimeMillis(),
                    "highTemp", ImmutableMap.of("temp", 120.0 + round, "desc", "temp too high " + round));
            builder.addEvent(deviceKey, System.currentTimeMillis(),
                    "invalidValue", ImmutableMap.of("value", -10.2 + round, "desc", "invalid value " + round));
        }

        IntEventPostRequest request = builder.build();

        processRequest(client, request, "events");
        processRequestAsync(client, request, "events");
    }

    private static void publishAttributes(final MqttClient client, int round) {
        IntAttributePostRequest.Builder builder = new IntAttributePostRequest.Builder();

        for (String deviceKey: DEVICES) {
            builder.addAttribute(deviceKey, "name", "Will-dev " + round);
            builder.addAttribute(deviceKey, "location", "Shanghai " + round);
        }

        IntAttributePostRequest request = builder.build();

        processRequest(client, request, "attributes");
        processRequestAsync(client, request, "attributes");
    }

    private static <T extends BaseMqttResponse> void processRequest(MqttClient client, BaseMqttRequest<T> request, String type) {
        try {
            T response = client.publish(request);
            if (response.isSuccess()) {
                System.out.println("[sync] publish " + type + " successfully");
            } else {
                System.err.println("[sync] Error: failed to publish " + type + ": " + response);
            }
        } catch (EnvisionException e) {
            e.printStackTrace();
        }
    }

    private static <T extends BaseMqttResponse> void processRequestAsync(MqttClient client, BaseMqttRequest<T> request, String type) {
        client.publish(request, new IResponseCallback<T>() {
            @Override
            public void onResponse(T response) {
                counter.incrementAndGet();
                System.out.println("[asyn] publish " + type + " successfully");
            }

            @Override
            public void onFailure(Exception failure) {
                counter.incrementAndGet();
                System.err.println("[asyn] Error: failed to publish " + type + ": " + failure);
                failure.printStackTrace();
            }
        });
    }

    // TODO figure out how this works
    private static void publishRawData(final MqttClient client) throws Exception {
        IntModelUpRawRequest.Builder builder = new IntModelUpRawRequest.Builder();

        String rawStr = "{\n" +
                "    \"id\":\"123\",\n" +
                "    \"version\":\"1.0\",\n" +
                "    \"params\":[\n" +
                "        {\n" +
                "            \"deviceKey\":\"yzDevice3\",\n" +
                "            \"measurepoints\":{\n" +
                "                \"temp\":23.4\n" +
                "            },\n" +
                "            \"time\":1552536407001\n" +
                "        },\n" +
                "        {\n" +
                "            \"deviceKey\":\"yzDevice3\",\n" +
                "            \"measurepoints\":{\n" +
                "                \"temp\":25.22,\n" +
                "                \"intPoint\":10\n" +
                "            },\n" +
                "            \"time\":1552536418207\n" +
                "        }\n" +
                "    ],\n" +
                "    \"method\":\"integration.measurepoint.post\"\n" +
                "}";

        System.out.println(Hex.encodeHexString(rawStr.getBytes()));

        builder.setPayload(rawStr.getBytes());

        IntModelUpRawResponse response = client.publish(builder.build());

        System.out.println(response);
    }
}

