package com.envisioniot.enos.iot_mqtt_sdk.message;

import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttRequest;
import com.envisioniot.enos.iot_mqtt_sdk.util.ExactValue;
import com.envisioniot.enos.iot_mqtt_sdk.util.GsonUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class describes a message that can be answered/replied by the receiver.
 * In other words, it serves as an input to a network communication between mqtt broker
 * and mqtt client. And an output may be expected.
 * <br/>
 * This could be an up-stream request (normally it's combined with IMqttRequest
 * interface for this case {@link BaseMqttRequest}).
 * <br/>
 * Or it could be a down-stream command (normally it's combined with IMqttCommand
 * interface for this case {@link BaseMqttCommand}).
 */
public class AnswerableMessageBody extends BaseMessageBody implements Serializable {

    private static final long serialVersionUID = -565677564353008496L;
    private String id;
    private String method;
    private String version;
    private ExactValue params;


    public byte[] encode() {
        return GsonUtil.toJson(getJsonPayload()).getBytes();
    }

    protected Map<String, Object> getJsonPayload() {
        Map<String, Object> payload = new HashMap<>();
        if (getId() != null) {
            payload.put("id", getId());
        }
        if (getVersion() != null) {
            payload.put("version", getVersion());
        }
        if (getMethod() != null) {
            payload.put("method", getMethod());
        }
        if (getParams() != null) {
            payload.put("params", getParams());
        }
        return payload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @SuppressWarnings("unchecked")
    public <T> T getParams() {
        return params == null ? null : (T) params.get();
    }

    public void setParams(Object params) {
        this.params = new ExactValue(params);
    }

    @Override
    public String toString() {
        return "AnswerableMessageBody{" + "id='" + id + '\'' + ", method='" + method + '\'' + ", version='" + version
                + '\'' + ", params=" + params + '}';
    }
}
