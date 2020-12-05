package mqtt;

import com.envisioniot.enos.iot_mqtt_sdk.core.ConnCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.network.NetworkStatusReportRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.network.NetworkStatusReportResponse;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author mengyuantan
 * @date 2020/10/20 20:52
 */
public class NetworkStatusSample {
    // EnOS HTTP Broker URL, which can be obtained from Environment Information page in EnOS Console
    static final String BROKER_URL = "tcp://broker_url:11883";

    // Device credentials, which can be obtained from Device Details page in EnOS Console
    static final String PRODUCT_KEY = "productKey";
    static final String DEVICE_KEY = "deviceKey";
    static final String DEVICE_SECRET = "deviceSecret";

    private static volatile MqttClient client;
    static {
        initClient();
    }

    public static MqttClient getClient() {
        return client;
    }

    private static synchronized void initClient() {
        client = new MqttClient(BROKER_URL, PRODUCT_KEY, DEVICE_KEY, DEVICE_SECRET);
        System.out.println("connect to: " + BROKER_URL);
        client.connect(new ConnCallback() {
            @Override
            public void connectComplete(boolean reconnect) {
                System.out.println("connect alpha success");
            }

            @Override
            public void connectLost(Throwable cause) {
                System.out.println("connect lost");
            }

            @Override
            public void connectFailed(Throwable cause) {
                System.out.println("connect failed");
            }
        });
    }

    public static void main(String[] args) throws Exception {
        try {
            TimeUnit.SECONDS.sleep(3);
            long postFrequency = 1L;
            int n = 35;
            while (n-- != 0) {
                TimeUnit.SECONDS.sleep(postFrequency);
                System.out.println("sleep " + postFrequency + " seconds, now: " + new Date());
                postLog();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }

    private static void postLog() throws Exception {
        NetworkStatusReportRequest request = NetworkStatusReportRequest.builder()
                .setCollectTime(321L)
                .setRssi(1.01F)
                .setSnr(3.21F)
                .setPacketLossRate(0.12F)
                .setNetworkType("networkType")
                .setErrorInfo("errorInfo")
                .build();

        System.out.println("post [" + request + "]");
        NetworkStatusReportResponse response = client.publish(request);
        System.out.println(response);
    }
}
