package com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants;

/**
 * @author zhensheng.cai
 * @date 2018/7/12.
 */
public class DeliveryTopicFormat {
    /**
     * Upstream Topic Patterns
     */
    public static final String MEASUREPOINT_POST = "/sys/%s/%s/thing/measurepoint/post";
    public static final String EVENT_POST = "/sys/%s/%s/thing/event/%s/post";
    public static final String TSL_TEMPLATE_GET = "/sys/%s/%s/thing/tsltemplate/get";
    public static final String MODEL_UP_RAW = "/sys/%s/%s/thing/model/up_raw";

    public static final String DEVICE_REGISTER_TOPIC_FMT = "/sys/%s/%s/thing/device/register";

    public static final String TOPO_GET_TOPIC_FMT = "/sys/%s/%s/thing/topo/get";
    public static final String TOPO_DELETE_TOPIC_FMT = "/sys/%s/%s/thing/topo/delete";
    public static final String TOPO_ADD_TOPIC_FMT = "/sys/%s/%s/thing/topo/add";

    public static final String SUB_DEVICE_LOGIN_TOPIC_FMT = "/ext/session/%s/%s/combine/login";
    public static final String SUB_DEVICE_LOGIN_BATCH_TOPIC_FMT = "/ext/session/%s/%s/combine/login/batch";
    public static final String SUB_DEVICE_LOGOUT_TOPIC_FMT = "/ext/session/%s/%s/combine/logout";

    public static final String TAG_DELETE_TOPIC_FMT = "/sys/%s/%s/thing/tag/delete";
    public static final String TAG_UPDATE_TOPIC_FMT = "/sys/%s/%s/thing/tag/update";

    public static final String PROGRESS_REPORT_TOPIC_FMT = "/sys/%s/%s/ota/device/progress";
    public static final String VERSION_REPORT_TOPIC_FMT = "/sys/%s/%s/ota/device/inform";
    public static final String UPDATE_REQUEST_TOPIC_FMT = "/sys/%s/%s/ota/device/request";
    public static final String GET_VERSION_TOPIC_FMT = "/sys/%s/%s/ota/device/getversion";

    public static final String ATTRIBUTE_UPDATE = "/sys/%s/%s/thing/attribute/update";
    public static final String ATTRIBUTE_QUERY = "/sys/%s/%s/thing/attribute/query";
    public static final String ATTRIBUTE_DELETE = "/sys/%s/%s/thing/attribute/delete";

    public static final String TAG_QUERY = "/sys/%s/%s/thing/tag/query";


    /**
     * Downstream Commands Patterns
     */
    public static final String MEASUREPOINT_SET_REPLY = "/sys/%s/%s/thing/service/measurepoint/set_reply";
    public static final String SERVICE_INVOKE_REPLY = "/sys/%s/%s/thing/service/%s_reply";
    public static final String MODEL_DOWN_RAW_REPLY = "/sys/%s/%s/thing/model/down_raw_reply";
    public static final String MEASUREPOINT_GET_REPLY = "/sys/%s/%s/thing/service/measurepoint/get_reply";
    public static final String RRPC_REPLY = "/sys/%s/%s/rrpc/response/%s";

    public static final String ENABLE_DEVICE_REPLY = "/sys/%s/%s/thing/enable_reply";
    public static final String DISABLE_DEVICE_REPLY = "/sys/%s/%s/thing/disable_reply";
    public static final String DELETE_DEVICE_REPLY = "/sys/%s/%s/thing/delete_reply";

    public static final String SUB_DEVICE_ENABLE_REPLY = "/ext/session/%s/%s/combine/enable_reply";
    public static final String SUB_DEVICE_DISABLE_REPLY = "/ext/session/%s/%s/combine/disable_reply";
    public static final String SUB_DEVICE_DELETE_REPLY = "/ext/session/%s/%s/combine/delete_reply";

    public static final String DEVICE_OTA_REPLY = "/sys/%s/%s/ota/device/upgrade_reply";

    public static final String ACTIVATE_INFO_REPLY = "/ext/session/%s/%s/thing/activate/info_reply";

    public static final String MEASUREPOINT_POST_BATCH = "/sys/%s/%s/thing/measurepoint/post/batch";

    // message integration
    public static final String INTEGRATION_MEASUREPOINT_POST = "/sys/%s/integration/measurepoint/post";
    public static final String INTEGRATION_EVENT_POST = "/sys/%s/integration/event/post";
    public static final String INTEGRATION_ATTRIBUTE_POST = "/sys/%s/integration/attribute/post";
    public static final String INTEGRATION_MODEL_UP_RAW = "/sys/%s/integration/model/up_raw";


    // message integration re-using on-line channel
    public static final String MEASUREPOINT_RESUME = "/sys/%s/%s/thing/measurepoint/resume";
    public static final String MEASUREPOINT_RESUME_BATCH = "/sys/%s/%s/thing/measurepoint/resume/batch";

    public static final String LOG_POST = "/sys/%s/%s/thing/log/post";
    public static final String NETWORK_STATUS_REPORT = "/sys/%s/%s/network/status/report";

    public static final String CONNECTION_STATE_POST = "/sys/%s/%s/connection/state/post";

    public static final String TRAFFIC_CONTROL_COMMAND = "/sys/%s/%s/traffic/control";
}
