package de.uniba.rz.entities.amqp;

import com.rabbitmq.client.Channel;
import de.uniba.rz.entities.general.Helper;

import java.io.IOException;

public class AMQPReceiver<T> {
    private Channel channel;
    private String queueName;

    private String consumerTag;

    public AMQPReceiver(Channel channel, String queue) {
        this.channel = channel;
        this.queueName = queue;
    }

    public void receive(String correlationId, AMQPMessageListener<T> callback) throws IOException {
        consumerTag = channel.basicConsume(queueName, true, (consumerTag, message) -> {
            if (!correlationId.isEmpty() && !message.getProperties().getCorrelationId().equals(correlationId))
                return;

            try {
                T object = Helper.convertByteArrayToObject(message.getBody());
                callback.onMessage(object, message.getProperties());
            } catch (ClassNotFoundException e) {
                callback.onMessage(null, message.getProperties());
            }
        }, consumerTag -> {
            System.out.println("Receiver cancelled");
        });
    }

    public void cancel() throws IOException {
        if (!consumerTag.isEmpty()){
            channel.basicCancel(consumerTag);
        }
    }
}
