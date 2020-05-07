package com.envisioniot.enos.iot_http_sdk.file;

import lombok.Getter;

/**
 * @author :charlescai
 * @date :2020-04-21
 */
@Getter
public enum  FileCategory {
    /**
     * category type : feature(including measurepoint, attribute and event)、OTA
     */
    FEATURE("feature"),
    OTA("ota");

    String name;

    FileCategory(String name) {
        this.name = name;
    }
}
