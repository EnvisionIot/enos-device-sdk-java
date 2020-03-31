package com.envisioniot.enos.iot_mqtt_proxy.share.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 
 * @author jian.tang
 *
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PublishMessage extends BaseMessage {

    private static final long serialVersionUID = 1320886076715564742L;
    private  byte qos;
    private String topicFilter;

    private byte[] payload;
}
