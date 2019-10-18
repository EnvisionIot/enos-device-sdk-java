package com.envisioniot.enos.iot_http_sdk.auth;

import lombok.Value;

/**
 * This class defines the auth request
 * @author shenjieyuan
 */
@Value
public class AuthRequest
{
    private final String secureMode;

    private final long lifetime;
    
    private final String sign;
}
