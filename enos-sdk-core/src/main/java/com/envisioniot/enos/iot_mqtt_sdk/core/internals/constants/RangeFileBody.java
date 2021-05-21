package com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants;

import lombok.Builder;
import lombok.Data;

import java.io.InputStream;

/**
 * @author :charlescai
 * @date :2021-05-10
 */
@Data
@Builder
public class RangeFileBody {
    private InputStream data;

    private long contentLength;
    private String contentRange;
    private String acceptRanges;
}

