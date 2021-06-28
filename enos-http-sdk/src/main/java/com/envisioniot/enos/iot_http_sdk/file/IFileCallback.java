package com.envisioniot.enos.iot_http_sdk.file;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.RangeFileBody;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author :charlescai
 * @date :2020-04-21
 */
public interface IFileCallback {
    /**
     * handle the async response of the file download request
     * @param inputStream
     * @throws IOException
     */
    default void onResponse(InputStream inputStream) throws IOException{};

    /**
     * handle the async range response of the file download request
     * @param rangeFileBody
     * @throws IOException
     */
    default void onRangeResponse(RangeFileBody rangeFileBody) throws IOException{}

    /**
     * Handle exception we hit while waiting for the response
     * @param failure
     */
    void onFailure(Exception failure);
}
