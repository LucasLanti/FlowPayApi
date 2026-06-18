package com.example.flowpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.example.flowpay.dtos.TicketDto;
import com.example.flowpay.services.RabbitQueueService;
import com.example.flowpay.services.TicketQueueProcessor;
import com.example.flowpay.services.TicketQueuePublisher;
import com.example.flowpay.services.TicketService;
import com.example.flowpay.utils.enums.TicketTeamEnum;

class QueueFlowTest {

    @Test
    void queueServicesPublishProcessAndReadQueueSize() {
        RabbitTemplate template = mock(RabbitTemplate.class);
        TicketQueuePublisher publisher = new TicketQueuePublisher(template, "exchange", "cards", "borrow", "others");
        TicketDto ticket = new TicketDto("content", UUID.randomUUID());

        publisher.publish(ticket, TicketTeamEnum.CARDS);
        publisher.publish(ticket, TicketTeamEnum.BORROW);
        publisher.publish(ticket, TicketTeamEnum.OTHERS);
        verify(template).convertAndSend("exchange", "cards", ticket);
        verify(template).convertAndSend("exchange", "borrow", ticket);
        verify(template).convertAndSend("exchange", "others", ticket);

        RabbitAdmin admin = mock(RabbitAdmin.class);
        RabbitQueueService queueService = new RabbitQueueService(admin);
        Properties properties = new Properties();
        properties.put(RabbitAdmin.QUEUE_MESSAGE_COUNT, 7);
        when(admin.getQueueProperties("queue")).thenReturn(properties);
        when(admin.getQueueProperties("missing")).thenReturn(null);
        assertEquals(7, queueService.getQueueSize("queue"));
        assertEquals(0, queueService.getQueueSize("missing"));

        TicketService ticketService = mock(TicketService.class);
        RabbitQueueService rabbitQueueService = mock(RabbitQueueService.class);
        TicketQueueProcessor processor = new TicketQueueProcessor(ticketService, rabbitQueueService, "cardsQ", "borrowQ",
                "othersQ");
        when(rabbitQueueService.getQueueSize("cardsQ")).thenReturn(1L);
        when(rabbitQueueService.getQueueSize("borrowQ")).thenReturn(2L);
        when(rabbitQueueService.getQueueSize("othersQ")).thenReturn(3L);
        processor.processPendingTickets();
        verify(ticketService).createNextTicketFromQueue("cardsQ", TicketTeamEnum.CARDS, 1);
        verify(ticketService).createNextTicketFromQueue("borrowQ", TicketTeamEnum.BORROW, 2);
        verify(ticketService).createNextTicketFromQueue("othersQ", TicketTeamEnum.OTHERS, 3);
    }
}
