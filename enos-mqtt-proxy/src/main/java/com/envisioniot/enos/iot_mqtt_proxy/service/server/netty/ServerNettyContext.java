package com.envisioniot.enos.iot_mqtt_proxy.service.server.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;

/**
 * 
 * @author jian.tang
 *
 */
@SuppressWarnings("unchecked")
public class ServerNettyContext extends NettyContext<ServerChannel> {
    private final EventLoopGroup workerGroup;

    public ServerNettyContext(EventLoopGroup bossGroup, EventLoopGroup workerGroup, Class<?> channelClass) {
        super(bossGroup, (Class<ServerChannel>)channelClass);
        this.workerGroup = workerGroup;
    }

    public EventLoopGroup getBossGroup() {
        return getLoopGroup();
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    @Override
    public void shutdown() {
        shutdownLoopGroup(getBossGroup());
        if (getBossGroup() != getWorkerGroup()) {
            shutdownLoopGroup(getWorkerGroup());
        }
    }


}
