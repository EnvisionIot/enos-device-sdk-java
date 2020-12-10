package com.envisioniot.enos.iot_mqtt_sdk.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public interface IExecutorFactory {

    void shutdownExecutorServices();

    ExecutorService getPublishExecutor();

    ExecutorService getConnectExecutor();

    ScheduledExecutorService getTimeoutScheduler();

    ExecutorService getCallbackExecutor();
}
