package com.envisioniot.enos.iot_http_sdk;

import com.google.common.base.Joiner;

import lombok.Value;

/**
 * This class is used to set up static-activated connection
 * @author shenjieyuan
 */
@Value
public class StaticDeviceCredential implements ICredential
{
    private final String productKey;

    private final String deviceKey;

    private final String deviceSecret;
    
    
    @Override
    public String getAuthPath()
    {
        return Joiner.on('/').join("/auth", productKey, deviceKey);
    }
}
