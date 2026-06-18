package com.example.flowpay.services;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.flowpay.domains.Attendant;
import com.example.flowpay.domains.TicketEvents;
import com.example.flowpay.dtos.TicketDto;
import com.example.flowpay.utils.enums.TicketStatusEnum;

public interface ITicketService {
    Page<TicketEvents> getTickets(UUID teamId, TicketStatusEnum status, Pageable pageable);

    void addNewTicket(TicketDto ticketRequest);

    void createTicket(TicketDto ticketRequest, Attendant attendant);

    void finishTicket(UUID id);
}
