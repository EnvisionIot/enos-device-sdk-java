package com.envisioniot.enos.iot_http_sdk;

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
}
