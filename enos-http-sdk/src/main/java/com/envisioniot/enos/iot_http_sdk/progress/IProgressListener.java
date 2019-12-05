package com.envisioniot.enos.iot_http_sdk.progress;

/**
 * 用来监听HTTP请求的进度。
 * @author shenjieyuan
 */
public interface IProgressListener
{
    public void onRequestProgress(long bytesWritten, long contentLength);
}
