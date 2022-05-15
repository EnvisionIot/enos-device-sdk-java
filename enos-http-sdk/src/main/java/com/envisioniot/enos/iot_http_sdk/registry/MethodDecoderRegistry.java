package com.envisioniot.enos.iot_http_sdk.registry;


import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author mengyuantan
 * @date 2021/6/30 16:25
 */
public class MethodDecoderRegistry {

    private static final Logger logger = LoggerFactory.getLogger(MethodDecoderRegistry.class);

    private final static List<IMqttArrivedMessage> DECODE_LIST = Lists.newArrayList();

    private final static List<String> CLASS_NAMES = ImmutableList.of(
            "com.envisioniot.enos.iot_mqtt_sdk.message.downstream.ota.OtaUpgradeCommand",
            "com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.ServiceInvocationCommand"
    );

    static {
        CLASS_NAMES.forEach(className -> {
            try {
                DECODE_LIST.add((IMqttArrivedMessage) Class.forName(className).newInstance());
            } catch (Throwable t) {
                logger.warn("fail to load class: {}", className, t);
            }
        });
    }

    public static List<IMqttArrivedMessage> getDecodeList() {
        return DECODE_LIST;
    }
}
