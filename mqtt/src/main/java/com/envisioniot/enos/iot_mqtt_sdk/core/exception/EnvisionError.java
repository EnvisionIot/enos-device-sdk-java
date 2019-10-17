package com.envisioniot.enos.iot_mqtt_sdk.core.exception;

import lombok.Value;

/**
 * Errors while handling mqtt connection and messages.
 * 
 * Redesigned, to implement an interface instead of enum.
 * 
 * @author shenjieyuan
 */
@Value
public class EnvisionError implements IEnvisionError
{
    public static final EnvisionError SUCCESS = new EnvisionError(0, "success");

    public static final EnvisionError INIT_MQTT_CLIENT_FAILED = new EnvisionError(-100, "INIT_MQTT_CLIENT_FAILED");
    public static final EnvisionError MQTT_CLIENT_CONNECT_FAILED = new EnvisionError(-101,
            "MQTT_CLIENT_CONNECT_FAILED");
    public static final EnvisionError MQTT_CLIENT_PUBLISH_FAILED = new EnvisionError(-102,
            "MQTT_CLIENT_PUBLISH_FAILED");
    public static final EnvisionError MQTT_CLIENT_DISCONNECT_FAILED = new EnvisionError(-103,
            "MQTT_CLIENT_DISCONNECT_FAILED");
    public static final EnvisionError MQTT_CLIENT_SUBSCRIEBE_FAILED = new EnvisionError(-104,
            "MQTT_CLIENT_SUBSCRIEBE_FAILED");
    public static final EnvisionError MQTT_CLIENT_CLOSE_FAILED = new EnvisionError(-105, "MQTT_CLIENT_CLOSE_FAILED");
    public static final EnvisionError INVALID_DEVICE_CREDENTIAL = new EnvisionError(-106, "INVALID_DEVICE_CREDENTIAL");
    public static final EnvisionError INVALID_REPLY_MESSAGE_FORMAT = new EnvisionError(-107,
            "INVALID_REPLY_MESSAGE_FORMAT");
    public static final EnvisionError INVALID_PAYLOAD = new EnvisionError(-108, "INVALID_PAYLOAD");
    public static final EnvisionError EMPTY_PAYLOAD = new EnvisionError(-109, "EMPTY_PAYLOAD");
    public static final EnvisionError GET_LOCAL_MODEL_FAILED = new EnvisionError(-110, "GET_LOCAL_MODEL_FAILED");
    public static final EnvisionError MODEL_VALIDATION_FAILED = new EnvisionError(-111, "MODEL_VALIDATION_FAILED");
    public static final EnvisionError RESPONSE_PARSE_ERR = new EnvisionError(-112, "RESPONSE_PARSE_ERR");
    public static final EnvisionError MQTT_RESPONSE_PARSED_FALED = new EnvisionError(-113,
            "MQTT_RESPONSE_PARSED_FALED");
    public static final EnvisionError UNSUPPPORTED_REQUEST_CALL_TYPE = new EnvisionError(-114,
            "UNSUPPPORTED_REQUEST_CALL_TYPE");
    public static final EnvisionError SESSION_IS_NULL = new EnvisionError(-115, "SESSION_IS_NULL");
    public static final EnvisionError STATUS_IS_UNKNOWN = new EnvisionError(-116, "STATUS_IS_UNKNOWN");
    public static final EnvisionError CODE_ERROR_MISSING_ARGS = new EnvisionError(-117, "CODE_ERROR_MISSING_ARGS");
    public static final EnvisionError CODE_ERROR_ARG_INVALID = new EnvisionError(-118, "CODE_ERROR_ARG_INVALID");
    public static final EnvisionError CANNOT_REGISTER_CALLBACK = new EnvisionError(-119, "CANNOT_REGISTER_CALLBACK");
    public static final EnvisionError DEVICE_SESSION_IS_NULL = new EnvisionError(-120, "SESSION IS NULL");
    public static final EnvisionError CALLBACK_EXECUTION_FAILED = new EnvisionError(-121, "callback execution failed");
    public static final EnvisionError STATUS_ERROR = new EnvisionError(-122, "invalid operation in current status");
    public static final EnvisionError STATUS_NOT_ALLOW_LOGIN = new EnvisionError(-123, "status not allow login");
    public static final EnvisionError STATUS_NOT_ALLOW_LOGOUT = new EnvisionError(-124, "status not allow logout");
    public static final EnvisionError FUTURE_TASK_TIME_OUT = new EnvisionError(-125, "sync request timeout");
    public static final EnvisionError THREAD_INTERRUPTED = new EnvisionError(-126, "thread interrupted");
    public static final EnvisionError QOS_2_NOT_ALLOWED = new EnvisionError(-127, "qos 2 not allowed");

    private final int errorCode;
    private final String errorMessage;
}
