package com.envisioniot.enos.iot_mqtt_sdk.core.internals;

import java.util.concurrent.ConcurrentHashMap;

class SubTopicCache {
    private ConcurrentHashMap<String, String> topicMap = new ConcurrentHashMap<>();

    boolean exists(String topic) {
        return topicMap.containsKey(topic);
    }

    void put(String topic) {
        topicMap.put(topic, "");
    }

    void remove(String topic) {
        topicMap.remove(topic);
    }

    void clean() {
        topicMap.clear();
    }
}
