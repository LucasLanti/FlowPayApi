package com.example.flowpay.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.flowpay.dtos.TicketDto;
import com.example.flowpay.dtos.response.PaginatedResponseDto;
import com.example.flowpay.dtos.response.TicketsResponseDto;
import com.example.flowpay.services.ITicketService;
import com.example.flowpay.utils.enums.TicketStatusEnum;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/ticket")
public class TicketController {
    private final ITicketService service;

    public TicketController(ITicketService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createTicket(@Valid @RequestBody TicketDto ticketRequest) {
        service.addNewTicket(ticketRequest);
        return ResponseEntity.ok().body(Map.of("message", "Ticket criado com sucesso."));
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<?> getTickets(
            @PathVariable UUID teamId,
            @RequestParam(required = false) TicketStatusEnum status,
            Pageable pageable) {
        Page<TicketsResponseDto> tickets = service.getTickets(teamId, status, pageable)
                .map(TicketsResponseDto::from);
        return ResponseEntity.ok().body(PaginatedResponseDto.from(tickets));
    }

    @PatchMapping("/{id}/finish")
    public ResponseEntity<?> finishTicket(@PathVariable UUID id) {
        service.finishTicket(id);
        return ResponseEntity.ok().body(Map.of("message", "Ticket finalizado com sucesso."));
    }
}
