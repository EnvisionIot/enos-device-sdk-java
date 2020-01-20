package com.envisioniot.enos.iot_mqtt_sdk.core.internals;

import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage;
import com.envisioniot.enos.iot_mqtt_sdk.util.PackageScanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zhensheng.cai
 * @date 2018/7/10.
 */
public class DecoderRegistry {

    public static final String DECODER_PACKAGE = "com.envisioniot.enos.iot_mqtt_sdk.message";

    private static final Logger logger = LoggerFactory.getLogger(DecoderRegistry.class);
    private static List<IMqttArrivedMessage> decoderList = new ArrayList<>();
    private static List<IMqttArrivedMessage> unmodefiDecoderList = Collections.unmodifiableList(decoderList);

    static {
        // For uplink response can be dynamically loaded
        // Just need to statically load the encoding method of various Commands
        try {
            for (Class<?> clss : PackageScanUtil.scan(DECODER_PACKAGE, IMqttArrivedMessage.class)) {
                IMqttArrivedMessage decoder = (IMqttArrivedMessage) clss.newInstance();
                decoderList.add(decoder);
            }
        } catch (Exception e) {
            logger.error("register downstream command decoder failed ", e);
        }
    }

    public static List<IMqttArrivedMessage> getDecoderList() {
        return unmodefiDecoderList;
    }
}
