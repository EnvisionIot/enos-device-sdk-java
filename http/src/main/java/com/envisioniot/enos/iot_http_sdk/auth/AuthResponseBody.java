package com.envisioniot.enos.iot_http_sdk.auth;

import lombok.Data;

/**
 * This class defines auth response
 * @author shenjieyuan
 */
@Data
public class AuthResponseBody 
{
    private int code;
    private AuthResponseBodyData data;
    private String message;
}
