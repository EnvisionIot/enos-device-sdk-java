package com.envisioniot.enos.iot_mqtt_proxy.service.util;

import com.envisioniot.enos.iot_mqtt_proxy.service.config.BaseConfig;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class ChannelUtil {

    private static AtomicInteger closedTCPFromDeviceCount = new AtomicInteger(0);
    private static AtomicInteger closedTCPFromBrokerCount = new AtomicInteger(0);

    private static String closeSelf = "close_self";

    public static void closePartyerChannel(Channel channel, String methodName) {
        try {
            Channel cpty = channel.attr(BaseConfig.COUNTER_PARTY_KEY).get();
            if (cpty == null) {
                log.warn(String.format("channelId of partyer has not been set in channel: %s.from method:%s",
                        channel.id().asLongText(), methodName));
                return;
            }
            close(methodName, cpty);

        } catch (Exception e) {
            log.warn(String.format("failed to close channel : %s.from method : %s",
                    channel.id().asLongText(), methodName), e);
        }
    }

    public static void closeChannel(Channel channel, String methodName) {
        try {
            close(closeSelf, channel);
        } catch (Exception e) {
            log.warn(String.format("failed to close channel : %s.from method : %s",
                    channel.id().asLongText(), methodName), e);
        }
    }

    private static void close(String methodName, Channel channel) {
        if (channel.isActive()) {
            // 必须确保另一条连接的所有消息已经成功写好
            channel.writeAndFlush(PooledByteBufAllocator.DEFAULT.buffer()).addListener(Future -> channel.close().addListener(CloseFuture -> {
                if (closeSelf.equals(methodName)) {
                    return;
                }
                String clientId = channel.attr(BaseConfig.CLIENTID_DEVICE).get();
                String userName = channel.attr(BaseConfig.USERNAME_DEVICE).get();
                String isClosed = channel.attr(BaseConfig.CHANNEL_CLOSED).get();
                if (!StringUtils.isEmpty(isClosed)){
                    return;
                }
                if (methodName.contains("device")) {
                    int cdCount = closedTCPFromDeviceCount.incrementAndGet();
                    log.info("succeed to close broker channel because device chanel was closed. " +
                                    "brokerChannelId:{}," +
                                    "clientId:{}," +
                                    "userName:{},count:{}" ,
                            channel.id().asLongText(),clientId,userName,cdCount);
                } else {
                    int cbCount = closedTCPFromBrokerCount.incrementAndGet();
                    log.info("succeed to close device channel because broker channel was closed . " +
                                    "deviceChannelId:{},clientId:{},userName:{},count:{}" ,
                            channel.id().asLongText(),clientId,userName,cbCount);
                }
                channel.attr(BaseConfig.CHANNEL_CLOSED).set("closed");
                // channelCache 更新
                ChannelCache.remove(userName);
            }));
        }
    }



}
