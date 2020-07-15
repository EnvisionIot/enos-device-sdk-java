package com.envisioniot.enos.iot_http_sdk;

import lombok.*;

import java.io.Serializable;

/**
 * @author mengyuantan
 */
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
public class ClientInfo implements Serializable {

    private static final long serialVersionUID = -7404427710382195140L;

    private String ipAddress;
    private String sdkVersion;
}
