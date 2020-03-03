package com.envisioniot.enos.iot_mqtt_sdk.message;

import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.ResponseCode;
import com.envisioniot.enos.iot_mqtt_sdk.util.ExactValue;
import com.envisioniot.enos.iot_mqtt_sdk.util.GsonUtil;

import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.BaseMqttReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.BaseMqttResponse;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This class describes the message that's used to acknowledge {@link AnswerableMessageBody}.
 * It serves as the output to a communication between mqtt broker and mqtt client.
 * <br/> <br/>
 * This could be a command reply {@link BaseMqttReply} from client to broker or a request
 * response {@link BaseMqttResponse} from broker to client.
 * <br/>
 */
public class AckMessageBody extends BaseMessageBody implements Serializable {
    private static final long serialVersionUID = -2367357179961511079L;

    private String id;
    private int code;
    private ExactValue data;
    private String message;

    public byte[] encode() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("code", getCode() == 0 ? ResponseCode.SUCCESS : getCode());
        if (getId() != null) {
            payload.put("id", getId());
        }
        if (getData() != null) {
            payload.put("data", getData());
        }
        if (getMessage() != null) {
            payload.put("message", getMessage());
        }
        return GsonUtil.toJson(payload).getBytes();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @SuppressWarnings("unchecked")
    public <T> T getData() {
        return data == null ? null : (T) data.get();
    }

    public void setData(Object data) {
        this.data = new ExactValue(data);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" + "id='" + id + '\'' + ", code=" + code + ", data=" + data + ", message='" + message
                + '\'' + '}';
    }
}
