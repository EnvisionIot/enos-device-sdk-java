package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.network;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author mengyuantan
 * @date 2020/10/16 13:58
 */
public class NetworkStatusReportRequest extends BaseMqttRequest<NetworkStatusReportResponse> {
    private static final long serialVersionUID = 1597932076366572356L;

    public static NetworkStatusReportRequest.Builder builder() {
        return new NetworkStatusReportRequest.Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<NetworkStatusReportRequest.Builder, NetworkStatusReportRequest> {
        private Map<String, Object> params = Maps.newHashMap();

        public Builder() {
            params.put("reportTime", System.currentTimeMillis());
        }

        private Builder addValue(String key, Object value) {
            params.put(key, value);
            return this;
        }

        public Builder setCollectTime(long collectTime) {
            return addValue("collectTime", collectTime);
        }

        public Builder setRssi(float rssi) {
            return addValue("rssi", rssi);
        }

        public Builder setSnr(float snr) {
            return addValue("snr", snr);
        }

        public Builder setPacketLossRate(float packetLossRate) {
            return addValue("packetLossRate", packetLossRate);
        }

        public Builder setNetworkType(String networkType) {
            return addValue("networkType", networkType);
        }

        public Builder setErrorInfo(String errorInfo) {
            return addValue("errorInfo", errorInfo);
        }


        @Override
        protected String createMethod() {
            return MethodConstants.NETWORK_STATUS_REPORT;
        }

        @Override
        protected Object createParams() {
            return params;
        }

        @Override
        protected NetworkStatusReportRequest createRequestInstance() {
            return new NetworkStatusReportRequest();
        }
    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.NETWORK_STATUS_REPORT;
    }

    @Override
    public Class<NetworkStatusReportResponse> getAnswerType() {
        return NetworkStatusReportResponse.class;
    }

    private NetworkStatusReportRequest() {

    }
}
