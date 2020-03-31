package com.envisioniot.enos.iot_mqtt_proxy.service.client;

import com.envisioniot.enos.iot_mqtt_proxy.service.config.BaseConfig;
import com.envisioniot.enos.iot_mqtt_proxy.service.server.processor.ProtocolProcessor;
import com.envisioniot.enos.iot_mqtt_proxy.service.util.ChannelUtil;
import io.moquette.parser.proto.messages.AbstractMessage;
import io.moquette.parser.proto.messages.ConnAckMessage;
import io.moquette.parser.proto.messages.PingRespMessage;
import io.moquette.parser.proto.messages.SubAckMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;
import java.util.concurrent.atomic.AtomicInteger;
import static io.moquette.parser.proto.messages.AbstractMessage.*;

/**
 * @Author: jian.tang
 * @Date: 2019/12/13 10:57
 */
@Sharable
@Log4j2
public class MqttClientHandler extends ChannelInboundHandlerAdapter {

    private static AtomicInteger receiveMessageFromBrokerCount = new AtomicInteger(0);
    private static AtomicInteger sendMessageToDeviceCount = new AtomicInteger(0);
    private static String BROKERINACTIVE = "brokerInactive";
    private static String BROKEREXCEPTION = "brokerException";
    private ProtocolProcessor serverProcessor;


    public MqttClientHandler(ProtocolProcessor serverProcessor) {
        this.serverProcessor = serverProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object message) {
        try {
            //0 统计收到broker数据的量
            receiveMessageFromBrokerCount.compareAndSet(Integer.MAX_VALUE, 0);
            int rCount = receiveMessageFromBrokerCount.incrementAndGet();
            if (rCount % 100 == 0) {
                log.debug("count of receiving message from broker:" + rCount);
            }
            Channel cpty = ctx.channel().attr(BaseConfig.COUNTER_PARTY_KEY).get();
            if (cpty == null) {
                log.warn("deviceChannel has not been set in attributes of channel:" + ctx.channel().id().asLongText());
                return;
            }

            //1 处理SPI消息
            String clientId = cpty.attr(BaseConfig.CLIENTID_DEVICE).get();
            String userName = cpty.attr(BaseConfig.USERNAME_DEVICE).get();
            AbstractMessage msg = (AbstractMessage) message;
            switch (msg.getMessageType()) {
                case CONNACK:
                    serverProcessor.processConnectAck(clientId, userName, (ConnAckMessage) msg);
                    break;
                case SUBACK:
                    serverProcessor.processSubAck(clientId, userName, (SubAckMessage) msg);
                    break;
                case PINGRESP:
                    serverProcessor.processPingResp(clientId, userName, (PingRespMessage) msg);
                    break;
                default:
                    break;
            }

            //2 转发
            cpty.writeAndFlush(message).addListener(future -> {
                if (future.isSuccess()) {
                    sendMessageToDeviceCount.compareAndSet(Integer.MAX_VALUE, 0);
                    int sCount = sendMessageToDeviceCount.incrementAndGet();
                    if (sCount % 100 == 0) {
                        log.debug("count of sending message to device :" + sCount);
                    }
                }
            });
        } catch (Exception e) {
            log.error("deviceChannel occurred error.", e);
            ctx.fireExceptionCaught(e);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ChannelUtil.closePartyerChannel(ctx.channel(), BROKERINACTIVE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ChannelUtil.closePartyerChannel(ctx.channel(), BROKEREXCEPTION);
        log.warn("brokerChannel exception", cause);
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext ctx) {
        //当broker channel阻塞后，关闭device channel的read
        Channel cpty = ctx.channel().attr(BaseConfig.COUNTER_PARTY_KEY).get();
        if (ctx.channel().isWritable()) {
            cpty.config().setAutoRead(true);
            log.info("set device channel auto-read to true");
        } else {
            cpty.config().setAutoRead(false);
            log.info("set device channel auto-read to false");
        }
    }
}
