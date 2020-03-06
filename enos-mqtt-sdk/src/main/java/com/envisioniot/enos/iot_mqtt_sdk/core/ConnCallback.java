package com.envisioniot.enos.iot_mqtt_sdk.core;

/**
 * @author jian.zhang4
 */
public interface ConnCallback {
    /**
     * Called when the connection to the server is completed successfully.
     * @param reconnect If true, the connection was the result of automatic reconnect.
     */
    void connectComplete(boolean reconnect);

    /**
     * This method is called when the connection to the server is lost. If the
     * init connection to the server failed, this callback method would never
     * be invoked.
     *
     * @param cause the reason behind the loss of connection.
     */
    void connectLost(Throwable cause);

    /**
     * Called when it failed to build initial connection to the server. Note
     * that this callback method would only be called once. And if the connect
     * failed, no future re-connect would be tried.
     *
     * @param cause
     */
    void connectFailed(Throwable cause);
}
