package com.envisioniot.enos.iot_mqtt_sdk.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;

/**
 * Factory that provides thread pools for handling connect, publish, time-out scheduler,
 * async callback. Note that this factory is per MqttClient. <br/>
 * <br/>
 * User can customize the relevant thread pool through the provided <b>set</b> method.
 *
 * @author zhensheng.cai
 * @author jian.zhang4
 */
public class ExecutorFactory implements IExecutorFactory {

    /**
     * Thread pools that handles mqtt publish action in async way.
     */
    private ExecutorService publishExecutor =  new ThreadPoolExecutor(3, 5, 3, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000), new ThreadFactoryBuilder().setNameFormat("publish-executor-%d").build());

    /**
     * We should only have one connect action per MqttClient at the same time. So one
     * thread should be good enough to handle async connection.
     */
    private ExecutorService connectExecutor = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder().setNameFormat("connect-executor-%d").build());

    /**
     * callback timeout pool
     */
    private ScheduledExecutorService timeoutScheduler = new ScheduledThreadPoolExecutor(3,
            new ThreadFactoryBuilder().setNameFormat("timeout-pool-%d").build());

    /**
     * Thread pools that execute async callback provided by user
     */
    private ExecutorService callbackExecutor =  new ThreadPoolExecutor(3, 5, 3, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setNameFormat("callback-executor-%d").build());


    /**
     * shutdown all thread pools managed by this factory
     */
    public void shutdownExecutorServices() {
        publishExecutor.shutdownNow();
        connectExecutor.shutdownNow();
        timeoutScheduler.shutdownNow();
        callbackExecutor.shutdownNow();
    }

    public ExecutorService getPublishExecutor() {
        return publishExecutor;
    }

    public void setPublishExecutor(ExecutorService publishExecutor) {
        this.publishExecutor = publishExecutor;
    }

    public ExecutorService getConnectExecutor() {
        return connectExecutor;
    }

    public void setConnectExecutor(ExecutorService connectExecutor) {
        this.connectExecutor = connectExecutor;
    }

    public ScheduledExecutorService getTimeoutScheduler() {
        return timeoutScheduler;
    }

    public void setTimeoutScheduler(ScheduledExecutorService timeoutScheduler) {
        this.timeoutScheduler = timeoutScheduler;
    }

    public ExecutorService getCallbackExecutor() {
        return callbackExecutor;
    }

    public void setCallbackExecutor(ExecutorService callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }
}
