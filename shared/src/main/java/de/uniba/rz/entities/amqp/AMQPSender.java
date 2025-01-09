package de.uniba.rz.entities.amqp;

import java.io.IOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.AMQP;

public class AMQPSender {
    private Channel channel;
    private String queue;

    public AMQPSender(Channel channel, String queue) {
        this.channel = channel;
        this.queue = queue;
    }

    public void send(byte[] bytes, AMQP.BasicProperties properties) throws IOException {
        channel.basicPublish("", queue, false, properties,bytes);
    }
}
