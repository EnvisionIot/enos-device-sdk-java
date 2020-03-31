package com.envisioniot.enos.iot_mqtt_proxy.share.messages;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 
 * @author jian.tang
 *
 */
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DisconnectMessage extends BaseMessage {

    private static final long serialVersionUID = 1771076639140244847L;
}
