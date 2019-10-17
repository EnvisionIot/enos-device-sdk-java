package com.envisioniot.enos.iot_mqtt_sdk.sample;

import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.LoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.login.NormalDeviceLoginInput;
import com.envisioniot.enos.iot_mqtt_sdk.core.profile.DefaultProfile;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tag.*;
import com.envisioniot.enos.iot_mqtt_sdk.sample.common.Helper;

import java.util.*;

public class ManipulateTagSample {

    // TODO check if response is success and throw if not
    public static void updateTags(MqttClient client, Map<String, String> tags) throws EnvisionException {
        TagUpdateRequest request = TagUpdateRequest.builder().addTags(tags).build();
        TagUpdateResponse rsp = client.publish(request);
        if(!rsp.isSuccess()){
            throw new RuntimeException(rsp.toString());
        }
        System.out.println("--> " + rsp);

    }

    public static void queryTags(MqttClient client) throws EnvisionException {
        System.out.println("start query tag");
        TagQueryRequest request = TagQueryRequest.builder()
                .queryAll().build();
        TagQueryResponse rsp = client.publish(request);

        if(!rsp.isSuccess()){
            throw new RuntimeException(rsp.toString());
        }
        System.out.println("--> " + rsp.getData().toString());
    }

    public static void deleteTags(MqttClient client, List<String> tags) throws EnvisionException {

        TagDeleteRequest request = TagDeleteRequest.builder().addTagKeys(tags).build();

        TagDeleteResponse rsp = client.publish(request);

        if(!rsp.isSuccess()){
            throw new RuntimeException(rsp.toString());
        }
        System.out.println("--> " + rsp);

    }

    public static String SERVER_URL = "tcp://localhost:11883";

    public static void main(String[] args) throws EnvisionException {
        LoginInput input = new NormalDeviceLoginInput(SERVER_URL, Helper.GW_PRODUCT_KEY, Helper.GW_DEV_KEY, Helper.GW_DEV_SECRET);
        final MqttClient client = new MqttClient(new DefaultProfile(input));
        client.connect();
        try{
            System.out.println("query tags at now!");
            queryTags(client);
            System.out.println();

            System.out.println("start to insert tags");
            System.out.println("tags will be insert are ...");

            Map<String, String> tags = new HashMap<>();
            tags.put("tag_key_1", "tag_value_1");
            tags.put("tag_key_2", "tag_value_2");
            tags.put("tag_key_3", "tag_value_3");
            tags.put("tag_key_4", "tag_value_4");
            tags.put("tag_key_5", "tag_value_5");

            System.out.println(tags);
            System.out.println("inserting");
            updateTags(client, tags);
            System.out.println("insert over\n");

            System.out.println("query tags at now!");
            queryTags(client);
            System.out.println();

            System.out.println("start to delete these added tags");
            List<String> tagKey = new ArrayList<>();
            tagKey.add("tag_key_1");
            tagKey.add("tag_key_2");
            tagKey.add("tag_key_3");
            tagKey.add("tag_key_4");
            tagKey.add("tag_key_5");
            deleteTags(client, tagKey);
            System.out.println("delete over\n");

            System.out.println("query tags at now!");
            queryTags(client);
        } finally {
            Helper.cleanConnection(client);
        }




    }


}
