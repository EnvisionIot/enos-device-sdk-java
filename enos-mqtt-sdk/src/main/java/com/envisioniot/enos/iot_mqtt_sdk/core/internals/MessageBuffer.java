package com.envisioniot.enos.iot_mqtt_sdk.core.internals;

import com.envisioniot.enos.iot_mqtt_sdk.core.msg.IMqttDeliveryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * msg request accumulator and disconnected msg buffer
 *
 * @author zhensheng.cai
 * @date 2019/1/8.
 */
public class MessageBuffer {

    private static Logger logger = LoggerFactory.getLogger(MessageBuffer.class);

    private static final int defaultQueueSize = 5000;

    private final Map<String, BlockingQueue<IMqttDeliveryMessage>> msgQueues = new ConcurrentHashMap<>();

    private final BlockingQueue<IMqttDeliveryMessage> disconnectedQueue = new LinkedBlockingQueue<>(defaultQueueSize);

    public void putMessage(IMqttDeliveryMessage message) {
        // putIfAbsent is thread-safe for ConcurrentHashMap
        BlockingQueue<IMqttDeliveryMessage> queue = msgQueues.putIfAbsent(
                message.getMessageTopic(), new LinkedBlockingQueue<>(defaultQueueSize));
        queue.offer(message);
    }

    public int getDisconnectedMessageCount() {
            return disconnectedQueue.size();
    }

    public void putDisconnectedMessage(IMqttDeliveryMessage message) {
        disconnectedQueue.offer(message);
    }


    public Runnable createRepublishDisconnetedMessageTask(final MqttConnection connection) {
        return () -> {
            IMqttDeliveryMessage message;
            while ((message = disconnectedQueue.poll()) != null) {
                try {
                    connection.fastPublish(message);
                } catch (Exception e) {
                    logger.error("fail to publish buffered message: " + message, e);
                }
            }
        };
    }

}
