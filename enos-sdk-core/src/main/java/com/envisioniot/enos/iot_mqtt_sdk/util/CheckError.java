package com.envisioniot.enos.iot_mqtt_sdk.util;

import com.envisioniot.enos.iot_mqtt_sdk.core.exception.IEnvisionError;

import lombok.Value;

@Value
public class CheckError implements IEnvisionError
{
    public static final CheckError CODE_ERROR_MISSING_ARGS = new CheckError(-117, "CODE_ERROR_MISSING_ARGS");
    public static final CheckError CODE_ERROR_ARG_INVALID = new CheckError(-118, "CODE_ERROR_ARG_INVALID");

    private final int errorCode;
    private final String errorMessage;
}
