package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.register;

import com.envisioniot.enos.iot_mqtt_sdk.util.StringI18n;

import java.util.HashMap;
import java.util.Map;

/**
 * Description: device register options
 *
 * @author zhonghua.wu
 * @create 2018-07-20 9:45
 */
public class DeviceRegOption {

    public final String deviceKey;
    public final StringI18n deviceName;
    public final String deviceDesc;
    public final String timezone;
    public final Map<String, Object> deviceAttributes;

    public DeviceRegOption(String deviceKey, StringI18n deviceName, String deviceDesc, String timezone) {
        this(deviceKey, deviceName, deviceDesc, timezone, new HashMap<>());
    }

    public DeviceRegOption(String deviceKey, StringI18n deviceName, String deviceDesc, String timezone, Map<String, Object> deviceAttributes) {
        this.deviceKey = deviceKey;
        this.deviceName = deviceName;
        this.deviceDesc = deviceDesc;
        this.timezone = timezone;
        this.deviceAttributes = deviceAttributes;
    }
}
