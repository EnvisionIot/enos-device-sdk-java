package com.envisioniot.enos.iot_http_sdk.auth;

import lombok.Value;

/**
 * This class defines the auth request
 * @author shenjieyuan
 */
@Value
public class AuthRequestBody
{
    private final String signMethod;

    private final long lifetime;
    
    private final String sign;
}
