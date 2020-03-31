package com.envisioniot.enos.iot_mqtt_proxy.service.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;


public class ResourceLoaderConfig extends BaseConfig {
    private Properties m_properties = new Properties();

    public ResourceLoaderConfig(String name) throws Exception {
        InputStream in = new FileInputStream(new File(System.getProperty("user.dir") + File.separator + name));
        m_properties.load(in);
    }


    public String getProperty(String name) {
        return m_properties.getProperty(name);
    }


}
