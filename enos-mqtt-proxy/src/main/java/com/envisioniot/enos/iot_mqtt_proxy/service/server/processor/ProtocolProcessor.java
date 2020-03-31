package com.envisioniot.enos.iot_mqtt_proxy.service.server.processor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import javax.annotation.PostConstruct;

import com.envisioniot.enos.iot_mqtt_proxy.service.config.BaseConfig;
import com.envisioniot.enos.iot_mqtt_proxy.service.util.ChannelCache;
import com.envisioniot.enos.iot_mqtt_proxy.share.messages.*;
import io.moquette.parser.proto.messages.*;
import io.moquette.parser.proto.messages.ConnectMessage;
import io.moquette.parser.proto.messages.DisconnectMessage;
import io.moquette.parser.proto.messages.PingReqMessage;
import io.moquette.parser.proto.messages.PingRespMessage;
import io.moquette.parser.proto.messages.PublishMessage;
import io.moquette.parser.proto.messages.SubscribeMessage;
import io.moquette.parser.proto.messages.UnsubscribeMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.envisioniot.enos.iot_mqtt_proxy.service.client.MqttClient;
import com.envisioniot.enos.iot_mqtt_proxy.share.IMQTTMessageInterceptHandler;
import io.netty.channel.Channel;
import org.springframework.util.StringUtils;

/**
 * @author jian.tang
 */
@Component
@Log4j2
public class ProtocolProcessor {

    private static List<IMQTTMessageInterceptHandler> handlerList=new ArrayList<IMQTTMessageInterceptHandler>();

    @Autowired
    private MqttClient mqttClient;

    @PostConstruct
    protected void init(){

        ServiceLoader<IMQTTMessageInterceptHandler> operations = ServiceLoader.load(IMQTTMessageInterceptHandler.class);
        Iterator<IMQTTMessageInterceptHandler> operationIterator = operations.iterator();
        while (operationIterator.hasNext()) {
            handlerList.add(operationIterator.next());
        }

    }

    public void processConnect(Channel channel, ConnectMessage msg) {
        // 1 clientID和userName绑定到deviceChannel和brokerChannel
        channel.attr(BaseConfig.CLIENTID_DEVICE).set(msg.getClientID());
        channel.attr(BaseConfig.USERNAME_DEVICE).set(msg.getUsername());
        Channel brokerChannel = channel.attr(BaseConfig.COUNTER_PARTY_KEY).get();
        brokerChannel.attr(BaseConfig.CLIENTID_DEVICE).set(msg.getClientID());
        brokerChannel.attr(BaseConfig.USERNAME_DEVICE).set(msg.getUsername());
        log.info("clientId and userName are bound to the deviceChannel . channelId:{},clientId:{},userName:{}",
                channel.id
                ().asLongText(),msg.getClientID(),msg.getUsername());
        // 2 clientID和userName放入缓存
        String username = msg.getUsername();
        if (!StringUtils.isEmpty(username)){
            String[] splitName = username.split("&");
            if (splitName.length==2){
                ChannelCache.put(msg.getUsername(),channel.id().asLongText());
            }else{
                log.warn("userName is error,clientId:{} ,userName:{}",msg.getClientID(),msg.getUsername());
            }
        }

        com.envisioniot.enos.iot_mqtt_proxy.share.messages.ConnectMessage connectMessage = new com.envisioniot.enos.iot_mqtt_proxy.share.messages.ConnectMessage();
        connectMessage.setClientId(msg.getClientID());
        connectMessage.setUserName(msg.getUsername());
        for (IMQTTMessageInterceptHandler item : handlerList) {
            item.onConnect(connectMessage);
        }
    }

    public void processSubscribe(String clientId,String userName,SubscribeMessage msg) {
        com.envisioniot.enos.iot_mqtt_proxy.share.messages.SubscribeMessage subscribeMessage = new com.envisioniot.enos.iot_mqtt_proxy.share.messages.SubscribeMessage();
        List<Couple> coupleList = convertCouple(msg.subscriptions());
        subscribeMessage.setClientId(clientId);
        subscribeMessage.setUserName(userName);
        subscribeMessage.setSubscriptions(coupleList);
        for (IMQTTMessageInterceptHandler item : handlerList) {
            item.onSubscribe(subscribeMessage);
        }
    }

    private List<Couple> convertCouple(List<io.moquette.parser.proto.messages.SubscribeMessage.Couple> couples) {
        List<Couple> coupleList = new ArrayList<>();
        for (io.moquette.parser.proto.messages.SubscribeMessage.Couple couple : couples) {
            Couple newCouple = new Couple();
            newCouple.setQos(couple.qos);
            newCouple.setTopicFilter(couple.topicFilter);
            coupleList.add(newCouple);
        }
        return coupleList;
    }

    public void processUnsubscribe(String clientId,String userName,UnsubscribeMessage msg) {
        com.envisioniot.enos.iot_mqtt_proxy.share.messages.UnsubscribeMessage unsubscribeMessage = new com.envisioniot.enos.iot_mqtt_proxy.share.messages.UnsubscribeMessage();
        unsubscribeMessage.setClientId(clientId);
        unsubscribeMessage.setUserName(userName);
        unsubscribeMessage.setSubscriptions(convertCouple(msg.topicFilters(), msg.getQos().byteValue()));
        for (IMQTTMessageInterceptHandler item : handlerList) {
            item.onUnsubscribe(unsubscribeMessage);
        }
    }

    private List<Couple> convertCouple(List<String> topics , byte qos ) {
        List<Couple> coupleList = new ArrayList<>();
        for (String item : topics) {
            Couple newCouple = new Couple();
            newCouple.setQos(qos);
            newCouple.setTopicFilter(item);
            coupleList.add(newCouple);
        }
        return coupleList;
    }

    public void processPublish(String clientId,String userName,PublishMessage msg) {
        com.envisioniot.enos.iot_mqtt_proxy.share.messages.PublishMessage publishMessage =
                new com.envisioniot.enos.iot_mqtt_proxy.share.messages.PublishMessage();
        publishMessage.setPayload(msg.getPayload().array());
        publishMessage.setClientId(clientId);
        publishMessage.setUserName(userName);
        publishMessage.setQos(msg.getQos().byteValue());
        publishMessage.setTopicFilter(msg.getTopicName());
        for (IMQTTMessageInterceptHandler item : handlerList) {
            item.onPublish(publishMessage);
        }
    }

    public void processDisconnect(String clientId,String userName,DisconnectMessage msg) {
        com.envisioniot.enos.iot_mqtt_proxy.share.messages.DisconnectMessage disconnectMessage = new com.envisioniot.enos.iot_mqtt_proxy.share.messages.DisconnectMessage();
        disconnectMessage.setClientId(clientId);
        disconnectMessage.setUserName(userName);
        for (IMQTTMessageInterceptHandler item : handlerList) {
            item.onDisconnect(disconnectMessage);
        }
    }

    public void processConnectAck(String clientId,String userName,ConnAckMessage msg){
//        ConnectAckMessage connectAckMessage = new ConnectAckMessage();
//        connectAckMessage.setClientId(clientId);
//        connectAckMessage.setUserName(userName);
//        connectAckMessage.setReturnCode(msg.getReturnCode());
//        for (IMQTTMessageInterceptHandler item : handlerList) {
//            item.onConnectAck(connectAckMessage);
//        }
    }

    public void processPublishAck(String clientId,String userName,PubAckMessage msg){
//        PublishAckMessage publishAckMessage = new PublishAckMessage();
//        publishAckMessage.setClientId(clientId);
//        publishAckMessage.setUserName(userName);
//        for (IMQTTMessageInterceptHandler item : handlerList) {
//            item.onPublishAck(publishAckMessage);
//        }
    }

    public void processPublishRec(String clientId,String userName,PubRecMessage msg){
//        PublishRecMessage publishRecMessage = new PublishRecMessage();
//        publishRecMessage.setClientId(clientId);
//        publishRecMessage.setUserName(userName);
//        for (IMQTTMessageInterceptHandler item : handlerList) {
//            item.onPublishRec(publishRecMessage);
//        }
    }

    public void processPublishRel(String clientId,String userName,PubRelMessage msg){
//        PublishRelMessage publishRelMessage = new PublishRelMessage();
//        publishRelMessage.setClientId(clientId);
//        publishRelMessage.setUserName(userName);
//        for (IMQTTMessageInterceptHandler item : handlerList) {
//            item.onPublishRel(publishRelMessage);
//        }
    }

    public void processPublishComp(String clientId,String userName,PubCompMessage msg){
//        PublishCompMessage publishCompMessage = new PublishCompMessage();
//        publishCompMessage.setClientId(clientId);
//        publishCompMessage.setUserName(userName);
//        for (IMQTTMessageInterceptHandler item : handlerList) {
//            item.onPublishComp(publishCompMessage);
//        }
    }

    public void processSubAck(String clientId,String userName,SubAckMessage msg){
//        SubscribeAckMessage subscribeAckMessage = new SubscribeAckMessage();
//        subscribeAckMessage.setClientId(clientId);
//        subscribeAckMessage.setUserName(userName);
//        for (IMQTTMessageInterceptHandler item : handlerList) {
//            item.onSubscribeAck(subscribeAckMessage);
//        }
    }

    public void processUnSubAck(String clientId,String userName,UnsubAckMessage msg){
//        UnsubscribeAckMessage unsubscribeAckMessage = new UnsubscribeAckMessage();
//        unsubscribeAckMessage.setClientId(clientId);
//        unsubscribeAckMessage.setUserName(userName);
//        for (IMQTTMessageInterceptHandler item : handlerList) {
//            item.onUnsubscribeAck(unsubscribeAckMessage);
//        }
    }

    public void processPingReq(String clientId,String userName,PingReqMessage msg){
//        com.envisioniot.enos.iot_mqtt_proxy.share.messages.PingReqMessage pingReqMessage = new com.envisioniot.enos.iot_mqtt_proxy.share.messages.PingReqMessage();
//        pingReqMessage.setClientId(clientId);
//        pingReqMessage.setUserName(userName);
//        for (IMQTTMessageInterceptHandler item : handlerList) {
//            item.onPingReq(pingReqMessage);
//        }
    }

    public void processPingResp(String clientId, String userName, PingRespMessage msg){
//        com.envisioniot.enos.iot_mqtt_proxy.share.messages.PingRespMessage pingRespMessage = new com.envisioniot.enos.iot_mqtt_proxy.share.messages.PingRespMessage();
//        pingRespMessage.setClientId(clientId);
//        pingRespMessage.setUserName(userName);
//        for (IMQTTMessageInterceptHandler item : handlerList) {
//            item.onPingResp(pingRespMessage);
//        }
    }

    public void processActive(Channel channel){
        mqttClient.addBrokerChannel(channel);
    }

    


}
