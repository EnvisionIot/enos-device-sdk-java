package com.envisioniot.enos.iot_http_sdk;

import static com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod.SHA256;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.SignMethod;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SessionConfiguration
{
    // default lifetime is 5 minutes
    public static final int DEFAULT_LIFETIME = 300_000;

    @Builder.Default
    private SignMethod signMethod = SHA256;

    @Builder.Default
    private int lifetime = DEFAULT_LIFETIME;
}
