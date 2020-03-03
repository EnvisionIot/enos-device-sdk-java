package com.envisioniot.enos.iot_mqtt_sdk.util;


import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttArrivedMessage;
import com.google.common.reflect.ClassPath;

/**
 * This is a helper class to scan decoder package.
 * However it's not suitable for using in some production application,
 * e.g. all-in-one JAR built by a SpringBoot Application
 * @author shenjieyuan
 */
public class PackageScanUtil {

    private static final Logger logger = LoggerFactory.getLogger(PackageScanUtil.class);
    
    public static void main(String[] args) throws Exception {
        String packageName = "com.envisioniot.enos.iot_mqtt_sdk";
        System.out.println(scan(packageName, IMqttArrivedMessage.class)
                .stream().map(c -> '"' + c.getName() + '"')
                .collect(Collectors.toList()));
    }

    /**
     * Use Guava ClassPath to search for classes.
     * !! TEST ONLY !!
     * @param pkg
     * @param filter
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static List<Class<?>> scan(String pkg, Class<?> filter) throws ClassNotFoundException, IOException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return ClassPath.from(loader).getTopLevelClassesRecursive(pkg)
        .stream()
        .map(info -> {
            try
            {
                return Class.forName(info.getName());
            } catch (ClassNotFoundException e)
            {
                logger.error("", e);
                return null;
            }
        })
        .filter(clazz -> 
        {
            return clazz != null &&
                   !Modifier.isAbstract(clazz.getModifiers()) &&
                   filter.isAssignableFrom(clazz);
        })
        .collect(Collectors.toList());
    }
}