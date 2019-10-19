package com.envisioniot.enos.iot_http_sdk;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;
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
    
    
    @Override
    public String getSign(SignMethod signMethod)
    {
//        Map<String>
//        SignUtil.sign(secret, params, signMethod)
        return null;
    }
    
    public static void main(String[] args)
    {
        long time = 30_000L;
        
        System.out.println(String.valueOf(time));
    }
    
}
