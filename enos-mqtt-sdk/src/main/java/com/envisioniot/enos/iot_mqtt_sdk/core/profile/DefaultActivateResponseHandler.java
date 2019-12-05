package com.envisioniot.enos.iot_mqtt_sdk.core.profile;

import com.envisioniot.enos.iot_mqtt_sdk.core.internals.MqttConnection;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMessageHandler;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.activate.DeviceActivateInfoCommand;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.activate.DeviceActivateInfoReply;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.ResponseCode;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.register.DeviceBasicInfo;
import com.envisioniot.enos.iot_mqtt_sdk.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


/**
 * Default handler of {@link DeviceActivateInfoCommand} which handle the response with
 * {@link FileProfile} and {@link MqttConnection}. It persists the reply deviceSecret and
 * recreate the connection.
 *
 * @author zhensheng.cai
 * @date 2019/1/3.
 */
public class DefaultActivateResponseHandler implements IMessageHandler<DeviceActivateInfoCommand, DeviceActivateInfoReply> {

    private static Logger logger = LoggerFactory.getLogger(DefaultActivateResponseHandler.class);
    private final MqttConnection mqttConnection;
    private final FileProfile profile;


    public DefaultActivateResponseHandler(MqttConnection mqttConnection) {
        if (!(mqttConnection.getProfile() instanceof FileProfile)) {
            throw new RuntimeException("FileProfile is required but it's " + mqttConnection.getProfile().getClass());
        }
        this.mqttConnection = mqttConnection;
        this.profile = (FileProfile) mqttConnection.getProfile();
    }

    @Override
    public DeviceActivateInfoReply onMessage(DeviceActivateInfoCommand response, List<String> argList) throws Exception {
        logger.info("handle activate reply info {}  by default handler", response.toString());
        DeviceBasicInfo deviceInfo = response.getDeviceInfo();
        String deviceSecret = deviceInfo.deviceSecret;
        boolean reconnectNeeded = false;
        if (StringUtil.isNotEmpty(deviceSecret) && StringUtil.isEmpty(profile.getConfig().getDeviceSecret())) {
            reconnectNeeded = true;
            this.profile.getConfig().setDeviceSecret(deviceSecret);
            this.profile.persist();
        }
        //publish the reply by handler and then recreate the mqtt connection
        DeviceActivateInfoReply reply = DeviceActivateInfoReply.builer()
                .setCode(ResponseCode.SUCCESS)
                .setMessage("persistent the device secret success")
                .setProductKey(response.getProductKey())
                .setDeviceKey(response.getDeviceKey())
                .build();
        reply.setMessageId(response.getMessageId());
        this.mqttConnection.fastPublish(reply);

        if (this.mqttConnection.isConnected() && reconnectNeeded) {
            // Force re-connection as the config is updated.
            mqttConnection.reconnect();
        }

        return null;
    }


}
