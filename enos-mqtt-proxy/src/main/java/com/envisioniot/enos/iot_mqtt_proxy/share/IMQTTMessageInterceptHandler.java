package com.envisioniot.enos.iot_mqtt_proxy.share;

import com.envisioniot.enos.iot_mqtt_proxy.share.messages.*;

/**
 * 
 * @author jian.tang
 *
 */
public interface IMQTTMessageInterceptHandler{
    
    
    void onConnect(ConnectMessage msg);

    void onConnectAck(ConnectAckMessage msg);
    
    void onDisconnect(DisconnectMessage msg);
    
    void onSubscribe(SubscribeMessage msg);

    void onSubscribeAck(SubscribeAckMessage msg);
    
    void onUnsubscribe(UnsubscribeMessage msg);

    void onUnsubscribeAck(UnsubscribeAckMessage msg);
    
    void onPublish(PublishMessage msg);

    void onPublishAck(PublishAckMessage msg);

    void onPublishRec(PublishRecMessage msg);

    void onPublishRel(PublishRelMessage msg);

    void onPublishComp(PublishCompMessage msg);

    void onPingReq(PingReqMessage msg);

    void onPingResp(PingRespMessage msg);





}
