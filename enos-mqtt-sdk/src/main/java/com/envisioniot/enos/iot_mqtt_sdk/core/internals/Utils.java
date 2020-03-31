package com.envisioniot.enos.iot_mqtt_sdk.core.internals;

import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;

public class Utils {

    static String getRootMessage(Throwable error) {
        if (error == null) {
            return "";
        }

        if (error.getCause() == null) {
            return StringUtil.isNotEmpty(error.getMessage()) ? error.getMessage() : error.getClass().getName();
        }

        return getRootMessage(error.getCause());
    }


}
