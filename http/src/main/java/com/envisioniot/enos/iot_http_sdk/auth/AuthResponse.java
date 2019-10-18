package com.envisioniot.enos.iot_http_sdk.auth;

import lombok.Data;

/**
 * This class defines auth response
 * @author shenjieyuan
 */
@Data
public class AuthResponse 
{
    private int code;
    private AuthResponseData data;
    private String message;
}
