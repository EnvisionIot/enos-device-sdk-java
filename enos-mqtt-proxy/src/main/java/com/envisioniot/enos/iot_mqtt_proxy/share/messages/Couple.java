package com.envisioniot.enos.iot_mqtt_proxy.share.messages;

import lombok.Data;

import java.io.Serializable;

/**
 * 
 * @author jian.tang
 *
 */
@Data
public class  Couple implements Serializable {
    private static final long serialVersionUID = 3454804983473544789L;
    private  byte qos;
    private  String topicFilter;

}
