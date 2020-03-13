package com.envisioniot.enos.iot_mqtt_proxy.service.server.netty;

import com.envisioniot.enos.iot_mqtt_proxy.service.config.BaseConfig;
import com.envisioniot.enos.iot_mqtt_proxy.service.server.processor.ProtocolProcessor;
import com.envisioniot.enos.iot_mqtt_proxy.service.util.ChannelUtil;
import io.moquette.parser.proto.messages.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import java.util.concurrent.atomic.AtomicInteger;
import static io.moquette.parser.proto.messages.AbstractMessage.*;

/**
 * @author jian.tang
 */
@Log4j2
@Sharable
public class NettyMQTTHandler extends ChannelInboundHandlerAdapter {

    private ProtocolProcessor serverProcessor;
    public static AtomicInteger receiveMessageCount = new AtomicInteger(0);
    public static AtomicInteger sendMessageCount = new AtomicInteger(0);

    public static String DEVICEINACTIVE = "deviceInactive";
    public static String DEVICEEXCEPTION = "deviceException";


    public NettyMQTTHandler(ProtocolProcessor serverProcessor) {
        this.serverProcessor = serverProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        try {
            //0 统计收到数据的量
            receiveMessageCount.compareAndSet(Integer.MAX_VALUE, 0);
            int rCount = receiveMessageCount.incrementAndGet();
            if (rCount % 100 == 0) {
                log.info("count of receiving message:" + rCount);
            }
            //1 处理SPI消息
            Channel channel = ctx.channel();
            String clientId = channel.attr(BaseConfig.CLIENTID_DEVICE).get();
            String userName = channel.attr(BaseConfig.USERNAME_DEVICE).get();
            AbstractMessage msg = (AbstractMessage) message;
            switch (msg.getMessageType()) {
                case CONNECT:
                    serverProcessor.processConnect(channel, (ConnectMessage) msg);
                    break;
                case SUBSCRIBE:
                    serverProcessor.processSubscribe(clientId, userName, (SubscribeMessage) msg);
                    break;
                case UNSUBSCRIBE:
                    serverProcessor.processUnsubscribe(clientId, userName, (UnsubscribeMessage) msg);
                    break;
                case PUBLISH:
                    serverProcessor.processPublish(clientId, userName, (PublishMessage) msg);
                    break;
                case PUBACK:
                    serverProcessor.processPublishAck(clientId, userName, (PubAckMessage) msg);
                    break;
                case PUBREC:
                    serverProcessor.processPublishRec(clientId, userName, (PubRecMessage) msg);
                    break;
                case PUBREL:
                    serverProcessor.processPublishRel(clientId, userName, (PubRelMessage) msg);
                    break;
                case PUBCOMP:
                    serverProcessor.processPublishComp(clientId, userName, (PubCompMessage) msg);
                    break;
                case PINGREQ:
                    serverProcessor.processPingReq(clientId, userName, (PingReqMessage) msg);
                    break;
                case DISCONNECT:
                    serverProcessor.processDisconnect(clientId, userName, (DisconnectMessage) msg);
                    break;
                default:
                    break;
            }
            //2转发
            Channel cpty = ctx.channel().attr(BaseConfig.COUNTER_PARTY_KEY).get();
            if (cpty == null) {
                log.warn("brokerChannel has not been set in channel:" + ctx.channel().id().asLongText());
                return;
            }
            cpty.writeAndFlush(message).addListener(future -> {
                if (future.isSuccess()) {
                    sendMessageCount.compareAndSet(Integer.MAX_VALUE, 0);
                    int sCount = sendMessageCount.incrementAndGet();
                    if (sCount % 100 == 0) {
                        log.info("count of sending message:" + sCount);
                    }
                }
            });

        } catch (Exception ex) {
            log.error("failed to process the message", ex);
            //一条消息错误，不足以将整个channel停掉
            //ctx.fireExceptionCaught(ex);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //跟broker建立TCP连接
        Channel channel = ctx.channel();
        //将读关掉直到broker channel打开
        channel.config().setAutoRead(false);
        serverProcessor.processActive(channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ChannelUtil.closePartyerChannel(ctx.channel(), DEVICEINACTIVE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ChannelUtil.closeChannel(ctx.channel(), DEVICEEXCEPTION);
        ChannelUtil.closePartyerChannel(ctx.channel(), DEVICEEXCEPTION);
        log.warn("deviceChannel exception", cause);
    }

}
