package com.example.flowpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.flowpay.domains.Attendant;
import com.example.flowpay.domains.Team;
import com.example.flowpay.domains.TicketEvents;
import com.example.flowpay.exceptions.ErrorResponse;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;

final class TestFixtures {
    private TestFixtures() {
    }

    static Team team(TicketTeamEnum name) {
        Team team = new Team();
        team.setId(UUID.randomUUID());
        team.setName(name);
        return team;
    }

    static Attendant attendant(String name) {
        Attendant attendant = new Attendant();
        attendant.setId(UUID.randomUUID());
        attendant.setName(name);
        return attendant;
    }

    static TicketEvents ticket(Team team, Attendant attendant, TicketStatusEnum status) {
        TicketEvents ticket = new TicketEvents();
        ticket.setId(UUID.randomUUID());
        ticket.setCreatedAt(LocalDateTime.of(2026, 6, 17, 4, 21));
        ticket.setUpdatedAt(LocalDateTime.of(2026, 6, 17, 4, 22));
        ticket.setTeam(team);
        ticket.setAttendant(attendant);
        ticket.setStatus(status);
        ticket.setContent("content");
        return ticket;
    }

    static void invokeLifecycle(Object target, String methodName) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }

    static void assertError(ResponseEntity<ErrorResponse> response, HttpStatus status, String key, String message) {
        assertEquals(status, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(key, response.getBody().getKeyMessage());
        assertEquals(message, response.getBody().getMessage());
    }
}
