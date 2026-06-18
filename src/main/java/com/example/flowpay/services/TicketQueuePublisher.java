package com.example.flowpay.services;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.flowpay.dtos.TicketDto;
import com.example.flowpay.utils.enums.TicketTeamEnum;

@Service
public class TicketQueuePublisher {
    private final RabbitTemplate rabbitTemplate;
    private final String ticketExchange;
    private final String cardsRoutingKey;
    private final String borrowRoutingKey;
    private final String othersRoutingKey;

    public TicketQueuePublisher(
            RabbitTemplate rabbitTemplate,
            @Value("${app.rabbitmq.ticket-exchange}") String ticketExchange,
            @Value("${app.rabbitmq.cards.routing-key}") String cardsRoutingKey,
            @Value("${app.rabbitmq.borrow.routing-key}") String borrowRoutingKey,
            @Value("${app.rabbitmq.others.routing-key}") String othersRoutingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.ticketExchange = ticketExchange;
        this.cardsRoutingKey = cardsRoutingKey;
        this.borrowRoutingKey = borrowRoutingKey;
        this.othersRoutingKey = othersRoutingKey;
    }

    public void publish(TicketDto ticket, TicketTeamEnum team) {
        rabbitTemplate.convertAndSend(ticketExchange, getRoutingKey(team), ticket);
    }

    private String getRoutingKey(TicketTeamEnum team) {
        return switch (team) {
            case CARDS -> cardsRoutingKey;
            case BORROW -> borrowRoutingKey;
            case OTHERS -> othersRoutingKey;
        };
    }
}
