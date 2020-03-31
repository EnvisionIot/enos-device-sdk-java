package com.envisioniot.enos.iot_mqtt_proxy.service;

import com.envisioniot.enos.iot_mqtt_proxy.service.client.MqttClient;
import com.envisioniot.enos.iot_mqtt_proxy.service.config.BaseConfig;
import com.envisioniot.enos.iot_mqtt_proxy.service.config.ResourceLoaderConfig;
import com.envisioniot.enos.iot_mqtt_proxy.service.server.netty.NettyAcceptor;
import com.envisioniot.enos.iot_mqtt_proxy.service.server.processor.ProtocolProcessor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * 
 * @author jian.tang
 *
 */
@Log4j2
@Component
public class MqttProxyServer {
    private static BaseConfig props = null;
    private static final String CONFFILE = "proxy.conf";

    @Autowired
    private NettyAcceptor acceptor;

    @Autowired
    private ProtocolProcessor processor;

    @Autowired
    private MqttClient mqttClient;

    /**
     * start server of mqtt-proxy and register shutdown hook
     * 
     * @throws IOException
     * @throws InterruptedException
     * @throws NumberFormatException
     */
    @PostConstruct
    public void init() throws Exception {
        props = new ResourceLoaderConfig(CONFFILE);
        mqttClient.initialize(processor,props);
        acceptor.initialize(processor, props);
        log.info("succeed to start acceptor and client of proxy.");
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.info("stopping acceptor and client of proxy..");
                stopProxyServer();
                log.info("acceptor and client of proxy have been stopped");
            }
        });
    }

    /**
     * stop server hook
     */
    public void stopProxyServer() {
        acceptor.close();
        mqttClient.close();
        log.info("proxy Server stopped");
    }

}
