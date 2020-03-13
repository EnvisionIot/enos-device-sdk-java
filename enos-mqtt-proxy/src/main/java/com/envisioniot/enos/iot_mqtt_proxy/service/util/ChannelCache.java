package com.envisioniot.enos.iot_mqtt_proxy.service.util;

import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: jian.tang
 * @Date: 2020/1/7 18:46
 */
@RestController
@RequestMapping("/channel")
public class ChannelCache {

    private static Map<String, String> channelMap = new ConcurrentHashMap<>();

    @RequestMapping("/get")
    public static String get(String userName){
        return channelMap.get(userName);
    }

    public static void put(String userName, String deviceChannelId){
         channelMap.put(userName,deviceChannelId);
    }
    public static void remove(String userName) {
        if (StringUtils.isEmpty(userName)){
            return;
        }
         channelMap.remove(userName);
    }

    @RequestMapping("/count")
    public static int channelCount(){
        return channelMap.size();
    }



}
