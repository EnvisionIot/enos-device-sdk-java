package com.envisioniot.enos.iot_mqtt_proxy.service.server.netty;

import com.envisioniot.enos.iot_mqtt_proxy.service.config.BaseConfig;
import com.envisioniot.enos.iot_mqtt_proxy.service.server.processor.ProtocolProcessor;
import io.moquette.parser.netty.MQTTDecoder;
import io.moquette.parser.netty.MQTTEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author jian.tang
 */
@Log4j2
@Component
public class NettyAcceptor {

    ServerNettyContext nettyContext;

    public void initialize(ProtocolProcessor processor, BaseConfig props) throws IOException, NumberFormatException, InterruptedException {
        nettyContext = NettyContextResolver.createServerContext(1, 0);
        final NettyMQTTHandler handler = new NettyMQTTHandler(processor);
        initializePlainTCPTransport(handler, props);
    }

    private void initFactory(String host, int port, final NettyMQTTHandler handler) throws InterruptedException {
        ServerBootstrap b = new ServerBootstrap();
        b.group(nettyContext.getBossGroup(), nettyContext.getWorkerGroup())
                .channel(nettyContext.getChannelClass())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        try {
                            pipeline.addLast("decoder", new MQTTDecoder());
                            pipeline.addLast("encoder", new MQTTEncoder());
                            pipeline.addLast("handler", handler);
                        } catch (Throwable th) {
                            log.error("acceptor error during pipeline creation ", th);
                            throw th;
                        }
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 512)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        // Bind and start to accept incoming connections.
        ChannelFuture f = b.bind(host, port);
        f.sync();
        log.info("acceptor binded host: {}, port: {}", host, port);
    }


    private void initializePlainTCPTransport(final NettyMQTTHandler handler, BaseConfig props) throws IOException, NumberFormatException, InterruptedException {
        String host = props.getProperty(BaseConfig.PROXY_HOST_NAME);
        String tcpPortProp = props.getProperty(BaseConfig.PROXY_PORT_NAME);
        initFactory(host, Integer.parseInt(tcpPortProp), handler);
    }

    public void close() {
        nettyContext.shutdown();
    }


}
