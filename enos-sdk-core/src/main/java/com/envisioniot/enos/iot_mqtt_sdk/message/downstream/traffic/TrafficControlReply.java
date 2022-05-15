package com.envisioniot.enos.iot_mqtt_sdk.message.downstream.traffic;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttReply;

/**
 * @author mengyuantan
 * @date 2021/9/14 17:20
 */
public class TrafficControlReply extends BaseMqttReply {

    private static final long serialVersionUID = 1950497394006278365L;

    public static TrafficControlReply.Builder builder() {
        return new TrafficControlReply.Builder();
    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.TRAFFIC_CONTROL_COMMAND;
    }

    public static class Builder extends BaseMqttReply.Builder<TrafficControlReply.Builder, TrafficControlReply> {

        @Override
        protected Object createData() {
            return null;
        }

        @Override
        protected TrafficControlReply createRequestInstance() {
            return new TrafficControlReply();
        }
    }
}
