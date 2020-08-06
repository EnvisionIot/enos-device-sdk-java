package com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants;

/**
 * @author dexiang.guo
 * @date 2020/8/5.
 */

public class OTAUpdateFailureCause {
    public static final String FIRMWARE_DOWNLOAD_FAILED_CODE        = "-2";
    public static final String FIRMWARE_VERIFICATION_FAILED_CODE    = "-3";
    public static final String FAILED_TO_WRITE_FIRMWARE_CODE        = "-4";
    public static final String DEVICE_IGNORED_THIS_UPGRADE_CODE     = "-5";
    public static final String UNDEFINED_FAILURE_CODE               = "-1";
}
