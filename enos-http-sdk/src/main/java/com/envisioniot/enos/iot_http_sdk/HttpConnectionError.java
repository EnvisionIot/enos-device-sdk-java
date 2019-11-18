package com.envisioniot.enos.iot_http_sdk;

import com.envisioniot.enos.iot_mqtt_sdk.core.exception.IEnvisionError;

import lombok.Value;

@Value
public class HttpConnectionError implements IEnvisionError
{
    public static final HttpConnectionError CLIENT_ERROR = new HttpConnectionError(-1, "client execption");
    public static final HttpConnectionError UNSUCCESSFUL_AUTH = new HttpConnectionError(-2, "unable to get authenticated");
    public static final HttpConnectionError SOCKET_ERROR = new HttpConnectionError(-3, "socket error, usually due to too large file");

    private final int errorCode;
    private final String errorMessage;
}
