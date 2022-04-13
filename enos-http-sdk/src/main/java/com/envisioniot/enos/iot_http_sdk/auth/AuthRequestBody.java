package com.envisioniot.enos.iot_http_sdk.auth;

import com.envisioniot.enos.iot_http_sdk.ClientInfo;
import lombok.Value;

import java.util.Set;

/**
 * This class defines the auth request
 *
 * @author shenjieyuan
 */
@Value
public class AuthRequestBody {
    String signMethod;
    long lifetime;
    String sign;
    ClientInfo clientInfo;
    Set<String> acceptCommandTypes;
}
