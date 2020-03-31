package com.envisioniot.enos.iot_mqtt_proxy.service.server.netty;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
/**
 * 
 * @author jian.tang
 *
 * @param <T>
 */
public abstract class NettyContext<T extends Channel> {
    private static final Logger LOG = LoggerFactory.getLogger(NettyContext.class);

    protected final EventLoopGroup loopGroup;
    protected final Class<T> channelClass;

    public NettyContext(EventLoopGroup loopGroup, Class<T> channelClass) {
        this.loopGroup = loopGroup;
        this.channelClass = channelClass;
    }

    public EventLoopGroup getLoopGroup() {
        return loopGroup;
    }

    public Class<T> getChannelClass() {
        return channelClass;
    }

    public void shutdown() {
        shutdownLoopGroup(getLoopGroup());
    }

    protected void shutdownLoopGroup(EventLoopGroup group) {
        Future<?> workerWaiter = group.shutdownGracefully();

        try {
            workerWaiter.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException iex) {
            LOG.warn("An InterruptedException was caught while waiting for event loops to terminate...");
        }

        if (!group.isTerminated()) {
            LOG.warn("Forcing shutdown of worker event loop...");
            group.shutdownGracefully(0L, 0L, TimeUnit.MILLISECONDS);
        }
    }
}