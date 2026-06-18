package com.example.flowpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.flowpay.domains.Attendant;
import com.example.flowpay.domains.Team;
import com.example.flowpay.domains.TicketEvents;
import com.example.flowpay.dtos.AttendantDto;
import com.example.flowpay.dtos.TicketDto;
import com.example.flowpay.exceptions.BadRequestException;
import com.example.flowpay.exceptions.ErrorResponse;
import com.example.flowpay.exceptions.NotFoundException;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;

class DomainFlowTest {

    @Test
    void domainsDtosAndExceptionsExposeState() throws Exception {
        UUID id = UUID.randomUUID();
        Team team = new Team(id, null, null, TicketTeamEnum.CARDS);
        Attendant attendant = new Attendant(id, null, null, "Ana", team);
        TicketEvents ticket = new TicketEvents(id, null, null, attendant, TicketStatusEnum.IN_PROGRESS, team, "content");
        TicketDto ticketDto = new TicketDto("content", id);
        AttendantDto attendantDto = new AttendantDto("Ana", id);
        ErrorResponse error = ErrorResponse.builder().message("m").keyMessage("k").build();
        BadRequestException bad = new BadRequestException("message", "key");
        NotFoundException notFound = new NotFoundException("message", "key");

        TestFixtures.invokeLifecycle(team, "onCreate");
        TestFixtures.invokeLifecycle(attendant, "onCreate");
        TestFixtures.invokeLifecycle(ticket, "onCreate");
        assertNotNull(team.getCreatedAt());
        assertNotNull(attendant.getCreatedAt());
        assertNotNull(ticket.getCreatedAt());
        LocalDateTime teamCreated = team.getCreatedAt();
        TestFixtures.invokeLifecycle(team, "onUpdate");
        TestFixtures.invokeLifecycle(attendant, "onUpdate");
        TestFixtures.invokeLifecycle(ticket, "onUpdate");
        assertFalse(team.getUpdatedAt().isBefore(teamCreated));

        assertEquals(id, team.getId());
        assertEquals("Ana", attendant.getName());
        assertEquals("content", ticket.getContent());
        assertEquals("content", ticketDto.getContent());
        assertEquals(id, ticketDto.getTeamId());
        assertEquals("Ana", attendantDto.getName());
        assertEquals(id, attendantDto.getTeamId());
        assertEquals("m", error.getMessage());
        assertEquals("k", error.getKeyMessage());
        assertEquals("message", bad.getMessage());
        assertEquals("key", bad.getKeyMessage());
        assertEquals("message", notFound.getMessage());
        assertEquals("key", notFound.getKeyMessage());
        assertEquals("single", new BadRequestException("single").getKeyMessage());
        assertEquals("single", new NotFoundException("single").getKeyMessage());
    }
}
