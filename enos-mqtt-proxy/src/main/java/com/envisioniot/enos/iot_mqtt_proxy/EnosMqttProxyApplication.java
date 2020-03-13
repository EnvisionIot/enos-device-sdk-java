package com.envisioniot.enos.iot_mqtt_proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.log4j.Log4j2;

/**
 * 
 * @author jian.tang
 *
 */
@SpringBootApplication
@Log4j2
public class EnosMqttProxyApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(EnosMqttProxyApplication.class, args);
        } catch (Exception e) {
           log.info("iot-mqtt-proxy failed to start up");
            e.printStackTrace();
        }
        log.info("iot-mqtt-proxy start up");
    }


}
