package com.envisioniot.enos.iot_mqtt_proxy.share.messages;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author jian.tang
 *
 */
@Data
public class BaseMessage implements Serializable {
    private static final long serialVersionUID = 6998271193004238374L;
    private String userName;

    private String clientId;

}
