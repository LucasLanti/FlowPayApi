package com.example.flowpay.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.flowpay.utils.enums.TicketTeamEnum;

@Component
public class TicketQueueProcessor {
    private final TicketService ticketService;
    private final RabbitQueueService rabbitQueueService;
    private final String cardsQueue;
    private final String borrowQueue;
    private final String othersQueue;

    public TicketQueueProcessor(
            TicketService ticketService,
            RabbitQueueService rabbitQueueService,
            @Value("${app.rabbitmq.cards.queue}") String cardsQueue,
            @Value("${app.rabbitmq.borrow.queue}") String borrowQueue,
            @Value("${app.rabbitmq.others.queue}") String othersQueue) {
        this.ticketService = ticketService;
        this.cardsQueue = cardsQueue;
        this.rabbitQueueService = rabbitQueueService;
        this.borrowQueue = borrowQueue;
        this.othersQueue = othersQueue;
    }

    @Scheduled(fixedDelayString = "${app.rabbitmq.ticket-processing-delay:5000}")
    public void processPendingTickets() {
        processQueue(cardsQueue, TicketTeamEnum.CARDS);
        processQueue(borrowQueue, TicketTeamEnum.BORROW);
        processQueue(othersQueue, TicketTeamEnum.OTHERS);
    }

    private void processQueue(String queue, TicketTeamEnum ticketTeam) {
        ticketService.createNextTicketFromQueue(queue, ticketTeam, rabbitQueueService.getQueueSize(queue));
    }
}
