package com.envisioniot.enos.iot_http_sdk;

import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.ota.OtaUpgradeCommand;
import com.envisioniot.enos.iot_mqtt_sdk.util.GsonUtil;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jdk.nashorn.internal.parser.JSONParser;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * This class find the class according method segment in json .
 *
 * @author dexiang.guo
 *
 */
@Slf4j
public class MethodClassMap {
    private final static String METHODKEY = "method";

    private static Map<String, Class<? extends IMqttArrivedMessage>> methodToClass = ImmutableMap.of(
            "default", BaseMqttCommand.class,
            "ota.device.upgrade", OtaUpgradeCommand.class
    );

    private static Class<? extends IMqttArrivedMessage> findClassName(String msg){
        JsonObject object = new JsonParser().parse(msg).getAsJsonObject();
        return methodToClass.get(object.get(METHODKEY).getAsString());

    }

    @Nullable
    public static <T extends IMqttArrivedMessage> T convertFromJson(String msg){
        if(StringUtil.isEmpty(msg)){
            log.warn("Convert to object failed. Msg is null.");
            return null;
        }
        try {
            return GsonUtil.fromJson(msg, findClassName(msg));
        }catch (Exception e){
            log.warn("Convert to object failed. Msg is : " + msg, e);
            return null;
        }
    }
}
