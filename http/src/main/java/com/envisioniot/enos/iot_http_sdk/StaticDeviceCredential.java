package com.envisioniot.enos.iot_http_sdk;

import lombok.Value;

/**
 * This class is used to set up static-activated connection
 * @author shenjieyuan
 */
@Value
public class StaticDeviceCredential
{
    private final String productKey;
    private final String deviceKey;
    private final String deviceSecret;
}
