package com.envisioniot.enos.iot_mqtt_sdk.message.upstream.connection;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author mengyuantan
 * @date 2021/1/19 21:17
 */
@AllArgsConstructor
@Getter
public enum ConnectionState {
    /**
     * Node connected
     */
    CONNECTED(1),

    /**
     * Node disconnected
     */
    DISCONNECTED(2),

    /**
     * Node state unknown
     */
    UNKNOWN(3);

    private final int state;

    public static ConnectionState valueOf(int stateValue) {
        for (ConnectionState nodeState : ConnectionState.values()) {
            if (nodeState.getState() == stateValue) {
                return nodeState;
            }
        }

        return UNKNOWN;
    }
}
