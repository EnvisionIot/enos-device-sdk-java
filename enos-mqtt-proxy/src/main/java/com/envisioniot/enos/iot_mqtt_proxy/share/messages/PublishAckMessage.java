package com.envisioniot.enos.iot_mqtt_proxy.share.messages;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: jian.tang
 * @Date: 2020/1/13 20:12
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PublishAckMessage extends BaseMessage{

    private static final long serialVersionUID = -4557092180227098234L;
}
