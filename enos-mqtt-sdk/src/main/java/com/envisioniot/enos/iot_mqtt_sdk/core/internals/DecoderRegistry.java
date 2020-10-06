package com.envisioniot.enos.iot_mqtt_sdk.core.internals;

import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author zhensheng.cai
 * @date 2018/7/10.
 */
public class DecoderRegistry {

    public static final String DECODER_PACKAGE = "com.envisioniot.enos.iot_mqtt_sdk.message";

    private static final Logger logger = LoggerFactory.getLogger(DecoderRegistry.class);
    private static List<IMqttArrivedMessage> decoderList = new ArrayList<>();
    private static List<IMqttArrivedMessage> unmodefiDecoderList = Collections.unmodifiableList(decoderList);

    static {
        // For uplink response can be dynamically loaded
        // Just need to statically load the encoding method of various Commands
        try {
            String[] classNames = new String[] {
                    "com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.MeasurepointSetCommand",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.ModelDownRawCommand",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.MeasurepointGetCommand",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.downstream.tsl.ServiceInvocationCommand",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.downstream.device.SubDeviceDisableCommand",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.downstream.device.SubDeviceEnableCommand",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.downstream.device.SubDeviceDeleteCommand",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.downstream.activate.DeviceActivateInfoCommand",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.downstream.ota.OtaUpgradeCommand",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.topo.TopoAddResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.topo.TopoDeleteResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.topo.TopoGetResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.AttributeDeleteResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.AttributeUpdateResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.AttributeQueryResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.EventPostResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostBatchResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.TslTemplateGetResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.ModelUpRawResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration.IntEventPostResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration.IntAttributePostResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration.IntMeasurepointPostResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.integration.IntModelUpRawResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume.MeasurepointResumeBatchResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.resume.MeasurepointResumeResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginBatchResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLogoutResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.status.SubDeviceLoginResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.register.DeviceRegisterResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.ota.OtaProgressReportResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.ota.OtaGetVersionResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.ota.OtaVersionReportResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tag.TagDeleteResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tag.TagUpdateResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tag.TagQueryResponse",
                    "com.envisioniot.enos.iot_mqtt_sdk.message.upstream.log.LogPostResponse"
            };
            Arrays.stream(classNames).forEach(n -> {
                IMqttArrivedMessage decoder;
                try
                {
                    decoder = (IMqttArrivedMessage) Class.forName(n).newInstance();
                    decoderList.add(decoder);
                } catch (Exception e)
                {
                    logger.error("register downstream command decoder failed ", e);
                }
            });
        } catch (Throwable e) {
            logger.error("register downstream command decoder failed ", e);
        }
    }

    public static List<IMqttArrivedMessage> getDecoderList() {
        return unmodefiDecoderList;
    }
}
