package com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants;

/**
 * Arrived msg topic pattern to match the decoder
 *
 * @author zhensheng.cai
 * @date 2018/7/10.
 */
public class ArrivedTopicPattern {

    /**
     * Reply to Upstream Requests
     */
    // public static final String MEASUREPOINT_POST_REPLY =
    // "/sys/(.*)/(.*)/thing/event/measurepoint/post_reply";
    public static final String MEASUREPOINT_POST_REPLY = "/sys/(.*)/(.*)/thing/measurepoint/post_reply";
    public static final String EVENT_POST_REPLY = "/sys/(.*)/(.*)/thing/event/(.*)/post_reply";

    public static final String MEASUREPOINT_POST_BATCH_REPLY = "/sys/(.*)/(.*)/thing/measurepoint/post/batch_reply";

    // public static final String TSL_TEMPLATE_GET_REPLY =
    // "/sys/(.*)/(.*)/thing/tsltemplate/get_reply";
    public static final String TSL_TEMPLATE_GET_REPLY = "/sys/(.*)/(.*)/thing/tsltemplate/get_reply";
    public static final String MODEL_UP_RAW_REPLY = "/sys/(.*)/(.*)/thing/model/up_raw_reply";

    public static final String DEVICE_REGISTER_REPLY = "/sys/(.*)/(.*)/thing/device/register_reply";

    public static final String TOPO_ADD_REPLY = "/sys/(.*)/(.*)/thing/topo/add_reply";
    public static final String TOPO_DELETE_REPLY = "/sys/(.*)/(.*)/thing/topo/delete_reply";
    public static final String TOPO_GET_REPLY = "/sys/(.*)/(.*)/thing/topo/get_reply";

    public static final String SUB_DEVICE_LOGIN_REPLY = "/ext/session/(.*)/(.*)/combine/login_reply";
    public static final String SUB_DEVICE_LOGIN_BATCH_REPLY = "/ext/session/(.*)/(.*)/combine/login/batch_reply";
    public static final String SUB_DEVICE_LOGOUT_REPLY = "/ext/session/(.*)/(.*)/combine/logout_reply";

    public static final String TAG_UPDATE_REPLY = "/sys/(.*)/(.*)/thing/tag/update_reply";
    public static final String TAG_DELETE_REPLY = "/sys/(.*)/(.*)/thing/tag/delete_reply";

    public static final String TAG_QUERY_REPLY = "/sys/(.*)/(.*)/thing/tag/query_reply";
    public static final String ATTRIBUTE_UPDATE_REPLY = "/sys/(.*)/(.*)/thing/attribute/update_reply";
    public static final String ATTRIBUTE_QUERY_REPLY = "/sys/(.*)/(.*)/thing/attribute/query_reply";
    public static final String ATTRIBUTE_DELETE_REPLY = "/sys/(.*)/(.*)/thing/attribute/delete_reply";

    /**
     * Dynamic Activation Reply
     */
    public static final String ACTIVATE_INFO = "/ext/session/(.*)/(.*)/thing/activate/info";

    /**
     * Downstream Commands
     */
    public static final String MEASUREPOINT_SET_COMMAND = "/sys/(.*)/(.*)/thing/service/measurepoint/set";
    public static final String SERVICE_INVOKE_COMMAND = "/sys/(.*)/(.*)/thing/service/(.*)";
    public static final String MODEL_DOWN_RAW_COMMAND = "/sys/(.*)/(.*)/thing/model/down_raw";
    public static final String MEASUREPOINT_GET_COMMAND = "/sys/(.*)/(.*)/thing/service/measurepoint/get";
    public static final String RRPC_COMMAND = "/sys/(.*)/(.*)/rrpc/request/(.*)";

    public static final String DELETE_DEVICE_COMMAND = "/sys/(.*)/(.*)/thing/delete";
    public static final String ENABLE_DEVICE_COMMAND = "/sys/(.*)/(.*)/thing/enable";
    public static final String DISABLE_DEVICE_COMMAND = "/sys/(.*)/(.*)/thing/disable";

    public static final String SUB_DEVICE_DELETE_COMMAND = "/ext/session/(.*)/(.*)/combine/delete";
    public static final String SUB_DEVICE_ENABLE_COMMAND = "/ext/session/(.*)/(.*)/combine/enable";
    public static final String SUB_DEVICE_DISABLE_COMMAND = "/ext/session/(.*)/(.*)/combine/disable";


    /**
     * OTA
     */
    public static final String DEVICE_OTA_COMMAND = "/sys/(.*)/(.*)/ota/device/upgrade";
    public static final String PROGRESS_REPORT_TOPIC_REPLY = "/sys/(.*)/(.*)/ota/device/progress_reply";
    public static final String VERSION_REPORT_TOPIC_REPLY = "/sys/(.*)/(.*)/ota/device/inform_reply";
    public static final String UPDATE_REQUEST_TOPIC_REPLY = "/sys/(.*)/(.*)/ota/device/request_reply";
    public static final String GET_VERSION_TOPIC_REPLY = "/sys/(.*)/(.*)/ota/device/getversion_reply";

    //message integration reply
    public static final String INTEGRATION_MEASUREPOINT_POST_REPLY = "/sys/(.*)/integration/measurepoint/post_reply";
    public static final String INTEGRATION_EVENT_POST_REPLY = "/sys/(.*)/integration/event/post_reply";
    public static final String INTEGRATION_ATTRIBUTE_POST_REPLY = "/sys/(.*)/integration/attribute/post_reply";
    public static final String INTEGRATION_MODEL_UP_RAW_REPLY = "/sys/(.*)/integration/model/up_raw_reply";

    public static final String MEASUREPOINT_RESUME_REPLY = "/sys/(.*)/(.*)/thing/measurepoint/resume_reply";
    public static final String MEASUREPOINT_RESUME_BATCH_REPLY = "/sys/(.*)/(.*)/thing/measurepoint/resume/batch_reply";

    public static final String LOG_POST_REPLY = "/sys/(.*)/(.*)/thing/log/post_reply";
    public static final String NETWORK_STATUS_REPLY = "/sys/(.*)/(.*)/network/status/report_reply";

    public static final String CONNECTION_STATE_REPLY = "/sys/(.*)/(.*)/connection/state/post_reply";
}
