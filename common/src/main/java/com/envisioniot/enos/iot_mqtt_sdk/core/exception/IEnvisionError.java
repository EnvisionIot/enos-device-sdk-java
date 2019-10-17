package com.envisioniot.enos.iot_mqtt_sdk.core.exception;

/**
 * @author zhensheng.cai
 * @date 2018/7/12.
 */
public interface IEnvisionError
{
    public int getErrorCode();

    public String getErrorMessage();
}
