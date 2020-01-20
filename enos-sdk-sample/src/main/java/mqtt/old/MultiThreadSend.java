package mqtt.old;

import com.envisioniot.enos.iot_mqtt_sdk.core.IConnectCallback;
import com.envisioniot.enos.iot_mqtt_sdk.core.MqttClient;
import com.envisioniot.enos.iot_mqtt_sdk.core.exception.EnvisionException;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostRequest;
import com.envisioniot.enos.iot_mqtt_sdk.message.upstream.tsl.MeasurepointPostResponse;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhensheng.cai
 * @date 2018/11/14.
 */
public class MultiThreadSend {

    private static MqttClient client;

    public static final String region = SimpleSendReceive.beta;

    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    private static AtomicLong counter = new AtomicLong();

    public static void main(String[] args) {
        initWithCallback();

        long begin = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            executor.execute(() -> {
                while (true) {
                    //tps : 50.76029449850161
//                    fastPostMeasurepoint();
//                    postSyncMeasurepoint();
//                    postWithCallbackMeasurepoint();

//                    fastPostMeasurepointQos0();
                    postSyncMeasurepointQos0();


//                    try {
//                        Thread.sleep();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                }
            });
        }


        new Thread(() -> {
            while (true) {

                System.out.println("tps : " + (counter.get() * 1000.0) / (System.currentTimeMillis() - begin));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public static void postWithCallbackMeasurepoint() {
        Random random = new Random();
        MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                .addMeasurePoint("point1", random.nextInt(100)).build();
        request.setQos(1);

        try {
            client.publish(request, null);
            counter.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void fastPostMeasurepoint() {
        Random random = new Random();
        MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                .addMeasurePoint("point1", random.nextInt(100)).build();
        request.setQos(1);

        try {
            client.fastPublish(request);
            counter.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fastPostMeasurepointQos0() {
        Random random = new Random();
        MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                .addMeasurePoint("point1", random.nextInt(100))
                .setQos(0).build();

        try {
            client.fastPublish(request);
            counter.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void postSyncMeasurepoint() {
        Random random = new Random();
        MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                .addMeasurePoint("point1", random.nextInt(100)).build();
        request.setQos(1);

        try {
            MeasurepointPostResponse rsp = client.publish(request);
//            System.out.println("-->" + rsp);
            counter.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void postSyncMeasurepointQos0() {
        Random random = new Random();
        MeasurepointPostRequest request = MeasurepointPostRequest.builder()
                .addMeasurePoint("point1", random.nextInt(100))
                .setQos(0).build();

        try {
            MeasurepointPostResponse rsp = client.publish(request);
            System.out.println("-->" + rsp);
            counter.incrementAndGet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initWithCallback() {
        System.out.println("start connect with callback ... ");
        client = new MqttClient(region,
                SimpleSendReceive.productKey,
                SimpleSendReceive.deviceKey,
                SimpleSendReceive.deviceSecret); // json device

        client.getProfile().setConnectionTimeout(60).setMaxInFlight(10000);
        client.connect(new IConnectCallback() {
            @Override
            public void onConnectSuccess() {
                System.out.println("connect success");
            }

            @Override
            public void onConnectLost() {
                System.out.println("onConnectLost");
            }

            @Override
            public void onConnectFailed(int reasonCode) {
                System.out.println("onConnectFailed : " + reasonCode);
            }

        });
        System.out.println("connect result :" + client.isConnected());
    }


}
