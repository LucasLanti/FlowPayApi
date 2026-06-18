package com.example.flowpay.configs;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Value("${app.rabbitmq.ticket-exchange}")
    private String ticketExchange;

    @Value("${app.rabbitmq.cards.queue}")
    private String cardsQueue;

    @Value("${app.rabbitmq.cards.routing-key}")
    private String cardsRoutingKey;

    @Value("${app.rabbitmq.borrow.queue}")
    private String borrowQueue;

    @Value("${app.rabbitmq.borrow.routing-key}")
    private String borrowRoutingKey;

    @Value("${app.rabbitmq.others.queue}")
    private String othersQueue;

    @Value("${app.rabbitmq.others.routing-key}")
    private String othersRoutingKey;

    @Bean
    public DirectExchange ticketExchange() {
        return new DirectExchange(ticketExchange, true, false);
    }

    @Bean
    public Queue cardsQueue() {
        return new Queue(cardsQueue, true);
    }

    @Bean
    public Queue borrowQueue() {
        return new Queue(borrowQueue, true);
    }

    @Bean
    public Queue othersQueue() {
        return new Queue(othersQueue, true);
    }

    @Bean
    public Binding cardsBinding(Queue cardsQueue, DirectExchange ticketExchange) {
        return BindingBuilder.bind(cardsQueue)
                .to(ticketExchange)
                .with(cardsRoutingKey);
    }

    @Bean
    public Binding borrowBinding(Queue borrowQueue, DirectExchange ticketExchange) {
        return BindingBuilder.bind(borrowQueue)
                .to(ticketExchange)
                .with(borrowRoutingKey);
    }

    @Bean
    public Binding othersBinding(Queue othersQueue, DirectExchange ticketExchange) {
        return BindingBuilder.bind(othersQueue)
                .to(ticketExchange)
                .with(othersRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
