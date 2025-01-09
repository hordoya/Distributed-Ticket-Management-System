package de.uniba.rz.entities.amqp;

import com.rabbitmq.client.AMQP;

public interface AMQPMessageListener<T> {
    void onMessage(T message, AMQP.BasicProperties properties);
}
