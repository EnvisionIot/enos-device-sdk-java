package com.envisioniot.enos.iot_mqtt_sdk.core.internals.constants;

/**
 * @author zhensheng.cai
 * @date 2018/7/6.
 */
public class MethodConstants {
    public static final String SUB_DEVICE_LOGIN = "combine.login";
    public static final String SUB_DEVICE_LOGIN_BATCH = "combine.login.batch";
    public static final String SUB_DEVICE_LOGOUT = "combine.logout";

    public static final String THING_LOGIN = "thing.login";

    public static final String THING_DISABLE = "thing.disable";
    public static final String THING_ENABLE = "thing.enable";
    public static final String THING_DELETE = "thing.delete";

    public static final String THING_MODEL_UP_RAW = "thing.model.up_raw";

    // public static final String MEASUREPOINT_POST =
    // "thing.event.measurepoint.post";
    public static final String MEASUREPOINT_POST = "thing.measurepoint.post";

    public static final String EVENT_POST = "thing.event.%s.post";

    public static final String TSL_TEMPLATE_GET = "thing.tsltemplate.get";

    public static final String DEVICE_REGISTER = "thing.device.register";

    public static final String TAG_DELETE = "thing.tag.delete";
    public static final String TAG_UPDATE = "thing.tag.update";
    public static final String TAG_QUERY = "thing.tag.query";

    public static final String TOPO_ADD = "thing.topo.add";
    public static final String TOPO_DELETE = "thing.topo.delete";
    public static final String TOPO_GET = "thing.topo.get";

    public static final String SERVICE_INOVKE = "thing.service.%s";


    public static final String OTA_PROGRESS = "ota.device.progress";

    public static final String OTA_INFORM = "ota.device.inform";

    public static final String OTA_GETVERSION = "ota.device.getversion";


    public static final String SUB_DEVICE_DISABLE = "combine.disable";
    public static final String SUB_DEVICE_ENABLE = "combine.enable";
    public static final String SUB_DEVICE_DELETE = "combine.delete";

    public static final String ATTRIBUTE_UPDATE = "thing.attribute.update";
    public static final String ATTRIBUTE_QUERY = "thing.attribute.query";
    public static final String ATTRIBUTE_DELETE = "thing.attribute.delete";

    public static final String MEASUREPOINT_POST_BATCH = "thing.measurepoint.post.batch";

    //message integration
    public static final String INTEGRATION_MEASUREPOINT_POST = "integration.measurepoint.post";
    public static final String INTEGRATION_EVENT_POST = "integration.event.post";
    public static final String INTEGRATION_ATTRIBUTE_POST = "integration.attribute.post";
    public static final String INTEGRATION_MODEL_UP_RAW = "integration.model.up_raw";


    public static final String MEASUREPOINT_RESUME = "thing.measurepoint.resume";
    public static final String MEASUREPOINT_RESUME_BATCH = "thing.measurepoint.resume.batch";

    public static final String LOG_POST = "thing.log.post";
}
