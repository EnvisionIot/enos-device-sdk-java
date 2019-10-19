package mqtt.helper;

import com.envisioniot.enos.iot_mqtt_sdk.core.IConnectCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;

/**
 * Helper class that extends IConnectCallback
 *
 * @author jian.zhang4
 */
public class BaseConnectCallback implements IConnectCallback {
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
    public void onConnectSuccess() {
        System.out.println("onConnectSuccess: " + tip);

        // Here we call the user customized callback in async way.
        Thread asyncWorker = new Thread(() -> {
            onSuccess(client);

            if (cleanConnectionOnSuccess) {
                Helper.cleanConnection(client);
            }
        });
        asyncWorker.setDaemon(false);
        asyncWorker.start();
    }

    @Override
    public void onConnectLost() {
        System.out.println("onConnectLost: " + tip);
        Helper.cleanConnection(client);
    }

    @Override
    public void onConnectFailed(int reasonCode) {
        System.err.println("onConnectFailed: " + tip);
        Helper.cleanConnection(client);
    }

    // User should customize this
    protected void onSuccess(MqttClient client) {
        // do something here
    }
}
