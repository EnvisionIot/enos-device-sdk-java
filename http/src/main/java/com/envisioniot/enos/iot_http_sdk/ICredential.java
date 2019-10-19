package com.envisioniot.enos.iot_http_sdk;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;

/**
 * This interfaces defines a credential to be used for HTTP connection
 * @author shenjieyuan
 */
public interface ICredential
{
    /**
     * Get the path to do auth request, 
     * e.g. /auth/{productKey}/{deviceKey}
     * @return
     */
    public String getAuthPath();
    
    
    /**
     * Generate the sign to be used in auth request
     * @return
     */
    public String getSign(SignMethod signMethod);

}
