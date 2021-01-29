package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.connection;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.DeliveryTopicFormat;
import com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants.MethodConstants;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * @author mengyuantan
 * @date 2021/1/11 12:05
 */
public class ConnectionStatePostRequest extends BaseMqttRequest<ConnectionStatePostResponse> {
    private static final long serialVersionUID = 6688764744988721753L;

    public static ConnectionStatePostRequest.Builder builder() {
        return new ConnectionStatePostRequest.Builder();
    }

    public static class Builder extends BaseMqttRequest.Builder<ConnectionStatePostRequest.Builder, ConnectionStatePostRequest> {
        private final Map<String, Object> params = Maps.newHashMap();
        private final Map<String, Map<String, Object>> nodes = Maps.newHashMap();
        private final Map<String, Set<String>> relationsMap = Maps.newHashMap();
        private boolean isFull = false;

        public Builder() {
        }

        public Builder isFull(boolean isFull) {
            this.isFull = isFull;
            return this;
        }

        public Builder addNode(String nodeId, ConnectionState state, String assetId, String name, String info) {
            Preconditions.checkArgument(StringUtil.isNotEmpty(nodeId), "field [nodeId] is required, but not found");
            Preconditions.checkNotNull(state, "field [state] is required, but not found");

            Map<String, Object> node = new HashMap<>(5);
            // these are required
            node.put("nodeId", nodeId);
            node.put("state", state.getState());

            // these values are optional
            if (StringUtil.isNotEmpty(assetId)) {
                node.put("assetId", assetId);
            }
            if (StringUtil.isNotEmpty(name)) {
                node.put("name", name);
            }
            if (StringUtil.isNotEmpty(info)) {
                node.put("info", info);
            }

            nodes.put(nodeId, node);

            return this;
        }

        public Builder addRelations(String parentNode, List<String> childNodes) {
            Preconditions.checkArgument(StringUtil.isNotEmpty(parentNode), "parentNode should not be empty");
            Preconditions.checkArgument(CollectionUtils.isNotEmpty(childNodes), "childNodes should not be empty");

            relationsMap.computeIfAbsent(parentNode, p -> new HashSet<>()).addAll(childNodes);
            return this;
        }

        public Builder addRelations(String parentNode, String ... childNodes) {
            return addRelations(parentNode, Arrays.asList(childNodes));
        }

        @Override
        protected String createMethod() {
            return MethodConstants.CONNECTION_STATE_POST;
        }

        @Override
        protected Object createParams() {
            Preconditions.checkArgument(isFull || relationsMap.isEmpty(),
                    "[relations] are not supported while [isFull] is false");

            params.put("isFull", isFull);
            params.put("nodes", nodes.values());
            params.put("relations", relationsMap);
            params.put("reportTime", System.currentTimeMillis());

            return params;
        }

        @Override
        protected ConnectionStatePostRequest createRequestInstance() {
            return new ConnectionStatePostRequest();
        }
    }

    @Override
    protected String _getPK_DK_FormatTopic() {
        return DeliveryTopicFormat.CONNECTION_STATE_POST;
    }

    @Override
    public Class<ConnectionStatePostResponse> getAnswerType() {
        return ConnectionStatePostResponse.class;
    }

    private ConnectionStatePostRequest() {

    }
}
