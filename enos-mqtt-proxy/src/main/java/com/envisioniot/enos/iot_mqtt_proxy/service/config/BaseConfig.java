/*
 * Copyright (c) 2012-2017 The original author or authorsgetRockQuestions()
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */
package com.envisioniot.enos.iot_mqtt_proxy.service.config;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;

public abstract class BaseConfig {

    public final static AttributeKey<Channel> COUNTER_PARTY_KEY = AttributeKey.valueOf("cpty");
    public final static AttributeKey<String> CLIENTID_DEVICE = AttributeKey.valueOf("clientId");
    public final static AttributeKey<String> USERNAME_DEVICE = AttributeKey.valueOf("username");
    public final static AttributeKey<String> CHANNEL_CLOSED = AttributeKey.valueOf("closed");

    public static final String PROXY_HOST_NAME = "proxy_host";
    public static final String PROXY_PORT_NAME = "proxy_port";
    public static final String BROKER_HOST_NAME = "broker_host";
    public static final String BROKER_PORT_NAME = "broker_port";

    public abstract String getProperty(String name);

}
