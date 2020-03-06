package mqtt.old.helper;

import com.envisioniot.enos.iot_mqtt_sdk.core.ConnCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import lombok.extern.slf4j.Slf4j;

/**
 * Helper class that extends {@link ConnCallback}
 *
 * @author jian.zhang4
 */
@Slf4j
public class BaseConnectCallback implements ConnCallback {
    private final MqttClient client;
    protected final String tip;

    /**
     * If this is true, the mqtt connection would be managed by this class
     * and it would be cleaned after onSuccess invocation. Otherwise, user
     * would manage the lifecycle of the connection himself.
     */
    private final boolean cleanConnectionOnSuccess;

    public BaseConnectCallback(MqttClient client, String tip) {
        this(client, tip, false);
    }

    public BaseConnectCallback(MqttClient client, String tip, boolean cleanConnectionOnSuccess) {
        this.client = client;
        this.tip = tip;
        this.cleanConnectionOnSuccess = cleanConnectionOnSuccess;
    }

    @Override
    public void connectComplete(boolean reconnect) {
        log.info("connectComplete: reconnect=" + reconnect);

        // Here we call the user customized callback in async way.
        Thread asyncWorker = new Thread(() -> {
            onSuccess(client);

            if (cleanConnectionOnSuccess) {
                client.close();
            }
        });
        asyncWorker.setDaemon(false);
        asyncWorker.start();
    }

    @Override
    public void connectLost(Throwable error) {
        log.error("connectLost", error);
        client.close();
    }

    @Override
    public void connectFailed(Throwable error) {
        log.error("connectLost", error);
        client.close();
    }

    // User should customize this
    protected void onSuccess(MqttClient client) {
        // do something here
    }
}
