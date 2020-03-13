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
public class PingReqMessage extends BaseMessage {

    private static final long serialVersionUID = -1381584440621480399L;
}
