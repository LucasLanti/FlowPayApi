package com.example.flowpay.services;

import java.util.Properties;

import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

@Service
public class RabbitQueueService {
    private final RabbitAdmin rabbitAdmin;

    public RabbitQueueService(RabbitAdmin rabbitAdmin) {
        this.rabbitAdmin = rabbitAdmin;
    }

    public long getQueueSize(String queueName) {
        Properties properties = rabbitAdmin.getQueueProperties(queueName);

        if (properties == null) {
            return 0;
        }

        Number messageCount = (Number) properties.get(RabbitAdmin.QUEUE_MESSAGE_COUNT);
        return messageCount.longValue();
    }
}
