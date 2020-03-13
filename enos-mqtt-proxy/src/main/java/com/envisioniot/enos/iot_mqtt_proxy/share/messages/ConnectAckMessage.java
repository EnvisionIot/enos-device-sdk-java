package com.envisioniot.enos.iot_mqtt_proxy.share.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: jian.tang
 * @Date: 2020/1/13 20:08
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ConnectAckMessage extends BaseMessage{
    private static final long serialVersionUID = 2160187287901888963L;
    private  byte returnCode;
}
