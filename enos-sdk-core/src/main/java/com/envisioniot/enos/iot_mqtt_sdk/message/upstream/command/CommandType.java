package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.command;

import lombok.Getter;

/**
 * @author mengyuantan
 * @date 2021/7/7 18:57
 */
@Getter
public enum CommandType {
    /**
     * command types for accept commands
     */
    CMD_OTA_UPGRADE("CMD_OTA_UPGRADE"),
    CMD_PENDING_INVOKE("CMD_PENDING_INVOKE");

    private final String name;

    CommandType(String name) {
        this.name = name;
    }
}
