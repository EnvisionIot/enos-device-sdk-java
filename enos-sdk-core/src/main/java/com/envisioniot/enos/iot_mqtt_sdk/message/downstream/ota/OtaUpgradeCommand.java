package com.envisioniot.enos.iot_mqtt_sdk.message.downstream.ota;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.ArrivedTopicPattern;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.ota.Firmware;

import java.util.Map;
import java.util.regex.Pattern;

public class OtaUpgradeCommand extends BaseMqttCommand<OtaUpgradeReply> {
    private static final long serialVersionUID = -1562255084521046172L;
    private static final Pattern TOPIC_PATTERN = Pattern.compile(ArrivedTopicPattern.DEVICE_OTA_COMMAND);
    private static final Pattern METHOD_PATTERN = Pattern.compile("ota.device.upgrade");

    @Override
    public Pattern getMatchTopicPattern() {
        return TOPIC_PATTERN;
    }

    @Override
    public Pattern getMethodPattern() {
        return METHOD_PATTERN;
    }

    @Override
    public Class<OtaUpgradeReply> getAnswerType() {
        return OtaUpgradeReply.class;
    }

    public Firmware getFirmwareInfo() {
        Map<String, Object> map = this.getParams();
        Firmware f = new Firmware();
        f.version = (String) map.get("version");
        f.signMethod = (String) map.get("signMethod");
        f.sign = (String) map.get("sign");
        f.fileUrl = (String) map.get("fileUrl");
        f.fileSize = (int) map.get("fileSize");
        return f;
    }
}
