package com.envisioniot.enos.iot_mqtt_proxy.service.server.netty;

import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.SystemPropertyUtil;

/**
 * 
 * @author jian.tang
 *
 */
public class NettyContextResolver {
    private static final Logger LOG = LoggerFactory.getLogger(NettyContextResolver.class);

    public static ServerNettyContext createServerContext(int nBossThreads, int nWorkerThreads) {
        return doCreateServerContext(nBossThreads, nWorkerThreads);
    }

    public static ServerNettyContext createServerContext(int nThreads) {
        return doCreateServerContext(nThreads, -1);
    }


    private static ServerNettyContext doCreateServerContext(int nBossThreads, int nWorkerThreads) {
        ServerNettyContext context = null;

        if (isUsingLinuxAndAMD64()) {
            try {
                LOG.info("trying to use epoll for server context in Linux ...");
                EventLoopGroup bossGroup = new EpollEventLoopGroup(nBossThreads);
                EventLoopGroup workerGroup = bossGroup;
                if (nWorkerThreads >= 0) {
                    workerGroup = new EpollEventLoopGroup(nWorkerThreads);
                }
                context = new ServerNettyContext(bossGroup, workerGroup, EpollServerSocketChannel.class);
                LOG.info("initialized epoll successfully");
            } catch (Throwable e) {
                LOG.info("failed to init epoll, would try NIO", e);
            }
        }

        if (context == null) {
            EventLoopGroup bossGroup = new NioEventLoopGroup(nBossThreads);
            EventLoopGroup workerGroup = bossGroup;
            if (nWorkerThreads >= 0) {
                workerGroup = new NioEventLoopGroup(nWorkerThreads);
            }
            context = new ServerNettyContext(bossGroup, workerGroup, NioServerSocketChannel.class);
        }

        return context;
    }


    private static boolean isUsingLinuxAndAMD64() {
        String osName = SystemPropertyUtil.get("os.name").toLowerCase(Locale.ENGLISH).trim();
        String arch = SystemPropertyUtil.get("os.arch").toLowerCase(Locale.ENGLISH).trim();
        return osName.contains("linux") && arch.contains("amd64");
    }
}
