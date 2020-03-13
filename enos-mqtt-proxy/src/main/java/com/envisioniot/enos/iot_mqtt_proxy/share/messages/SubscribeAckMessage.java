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
public class SubscribeAckMessage extends BaseMessage {

    private static final long serialVersionUID = -4826743609793391569L;
}
