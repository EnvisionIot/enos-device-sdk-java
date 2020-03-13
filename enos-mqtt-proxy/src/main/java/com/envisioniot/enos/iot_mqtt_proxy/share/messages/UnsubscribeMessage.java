package com.envisioniot.enos.iot_mqtt_proxy.share.messages;

import java.util.List;
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
public class UnsubscribeMessage extends BaseMessage {

    private static final long serialVersionUID = 8478992138162335516L;
    public List<Couple> subscriptions;

}
