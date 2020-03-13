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
public class SubscribeMessage extends BaseMessage {

    private static final long serialVersionUID = -9212527488265799580L;
    public List<Couple> subscriptions;





}
