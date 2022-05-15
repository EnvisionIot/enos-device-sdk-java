package com.envisioniot.enos.iot_http_sdk;

import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMessageHandler;
import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttDeliveryMessage;
import com.envisioniot.enos.iot_mqtt_sdk.message.downstream.ota.OtaUpgradeCommand;
import lombok.extern.log4j.Log4j2;
import okhttp3.Headers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Desc:
 *
 * @author dexiang.guo
 * @date 2020/9/6
 */

@Log4j2
@RunWith(SpringRunner.class)
@SpringBootTest(classes = HttpConnection.class)
public class HttpConnectionTest {
    @Autowired
    private HttpConnection httpConnection;

    private String msgBody = "{\"id\":\"123\",\"version\":\"1.0\",\"method\":\"ota.device.upgrade\",\"params\": {\"version\":\"v1.0\",\"sign\":\"xxxxxxx\",\"signMethod\":\"md5\",\"fileUrl\":\"/1b30e0ea83002000/ota/kadak13\",\"fileSize\":1024}}";

    private Headers headers = new Headers.Builder().add("command-payload", msgBody).build();
    @Test
    public void handleAdditionalMsgTest1(){
        try {
            httpConnection.handleAdditionalMsg(null);
            log.info("handleAdditionalMsg no msg input is OK.");
        }catch (Exception e){
            Assert.fail();
        }
   }

    @Test
    public void handleAdditionalMsgTest2(){
        try {
            httpConnection.handleAdditionalMsg(headers);
            log.info("handleAdditionalMsg no handler is OK.");
        }catch (Exception e){
            Assert.fail();
        }
    }

    @Test
    public void handleAdditionalMsgTest3(){
        httpConnection.setArrivedMsgHandler(OtaUpgradeCommand.class, new IMessageHandler<OtaUpgradeCommand, IMqttDeliveryMessage>() {
            @Override
            public IMqttDeliveryMessage onMessage(OtaUpgradeCommand otaUpgradeCommand, List<String> list) throws Exception {
                Assert.assertNotNull(otaUpgradeCommand);
                log.info("handleAdditionalMsg is OK.");
                return null;
            }
        });
        httpConnection.handleAdditionalMsg(headers);
    }

    @Test
    public void handleAdditionalMsgTest4(){
        httpConnection.setArrivedMsgHandler(OtaUpgradeCommand.class, new IMessageHandler<OtaUpgradeCommand, IMqttDeliveryMessage>() {
            @Override
            public IMqttDeliveryMessage onMessage(OtaUpgradeCommand otaUpgradeCommand, List<String> list) throws Exception {
                throw new Exception("test exception.");
            }
        });
        httpConnection.handleAdditionalMsg(headers);
    }

    @Test
    public void removeArrivedMsgHandlerTest(){
        httpConnection.setArrivedMsgHandler(OtaUpgradeCommand.class, new IMessageHandler<OtaUpgradeCommand, IMqttDeliveryMessage>() {
            @Override
            public IMqttDeliveryMessage onMessage(OtaUpgradeCommand otaUpgradeCommand, List<String> list) throws Exception {
                Assert.fail();
                return null;
            }
        });
        httpConnection.removeArrivedMsgHandler(OtaUpgradeCommand.class);
        httpConnection.handleAdditionalMsg(headers);
        log.info("removeArrivedMsgHandler is OK.");
    }
}