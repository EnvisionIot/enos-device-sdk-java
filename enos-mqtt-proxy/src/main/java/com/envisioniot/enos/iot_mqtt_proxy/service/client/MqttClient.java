package com.envisioniot.enos.iot_mqtt_proxy.service.client;

import com.envisioniot.enos.iot_mqtt_proxy.service.config.BaseConfig;
import com.envisioniot.enos.iot_mqtt_proxy.service.server.processor.ProtocolProcessor;
import com.envisioniot.enos.iot_mqtt_proxy.service.util.ChannelUtil;
import io.moquette.parser.netty.MQTTDecoder;
import io.moquette.parser.netty.MQTTEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * @Author: jian.tang
 * @Date: 2019/12/13 10:57
 */
@Log4j2
@Component
public class MqttClient {

    //TODO channel数量  对应的IP等
    private static String SERVER_HOST = null;
    private static int PORT = 11883;
    private static AtomicInteger connectedTCPCount = new AtomicInteger(0);
    //可以跟server中的worker共用,或者epoll
    private static EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
    private static Bootstrap bootstrap = new Bootstrap();

    public void initialize(ProtocolProcessor processor, BaseConfig props) {
        SERVER_HOST = props.getProperty(BaseConfig.BROKER_HOST_NAME);
        PORT = Integer.parseInt(props.getProperty(BaseConfig.BROKER_PORT_NAME));
        bootstrap.group(eventLoopGroup);
        bootstrap.channel(NioSocketChannel.class);
        //端口复用
        bootstrap.option(ChannelOption.SO_REUSEADDR, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        //禁用Nagle算法,无延迟
        bootstrap.option(ChannelOption.TCP_NODELAY, false);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000 * 90);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new MQTTDecoder());
                ch.pipeline().addLast(new MQTTEncoder());
                ch.pipeline().addLast(new MqttClientHandler(processor));
            }
        });

    }

    //建立broker处的tcp
    public void addBrokerChannel(final Channel deviceChannel) {
        try {
            ChannelFuture channelFuture = bootstrap.connect(SERVER_HOST, PORT);
            Channel brokerChannel = channelFuture.channel();
            ChannelId brokerChannelId = brokerChannel.id();
            channelFuture.addListener((ChannelFutureListener) future -> {
                if (deviceChannel.isActive() && future.isSuccess()) {
                    brokerChannel.attr(BaseConfig.COUNTER_PARTY_KEY).set(deviceChannel);
                    deviceChannel.attr(BaseConfig.COUNTER_PARTY_KEY).set(brokerChannel);
                    deviceChannel.config().setAutoRead(true);//打开device channel读
                    connectedTCPCount.compareAndSet(Integer.MAX_VALUE, 0);
                    int andIncrement = connectedTCPCount.incrementAndGet();
                    log.info(String.format("succeed to create channel of broker,totalCount:%s,deviceChannelId :%s," +
                            "brokerChannelId:%s", andIncrement, deviceChannel.id().asLongText(), brokerChannelId.asLongText
                            ()));
                } else {
                    ChannelUtil.closeChannel(deviceChannel, "addBrokerChannel");
                }
            });

        } catch (Exception e) {
            ChannelUtil.closeChannel(deviceChannel, "addBrokerChannel");
            log.error("failed to add broker of channel", e);
        }
    }


    public void close() {
        Future<?> workerWaiter = eventLoopGroup.shutdownGracefully();
        try {
            workerWaiter.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException iex) {
            log.warn("An InterruptedException was caught while waiting for event loops of client to terminate...", iex);
        }
        if (!eventLoopGroup.isTerminated()) {
            log.warn("Forcing shutdown of worker event loop of client...");
            eventLoopGroup.shutdownGracefully(0L, 0L, TimeUnit.MILLISECONDS);
        }
    }

}