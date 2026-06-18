package com.example.flowpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.flowpay.controllers.DashboardController;
import com.example.flowpay.dtos.response.DashboardAttendantResponseDto;
import com.example.flowpay.dtos.response.DashboardQueueResponseDto;
import com.example.flowpay.dtos.response.DashboardResponseDto;
import com.example.flowpay.repositories.IAttendantRepository;
import com.example.flowpay.repositories.ITicketEventsRepository;
import com.example.flowpay.repositories.projections.AttendantWorkloadProjection;
import com.example.flowpay.services.DashboardService;
import com.example.flowpay.services.IDashboardService;
import com.example.flowpay.services.ITeamService;
import com.example.flowpay.services.RabbitQueueService;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;

class DashboardTest {

    @Test
    void controllerReturnsDashboardData() {
        IDashboardService service = mock(IDashboardService.class);
        DashboardController controller = new DashboardController(service);
        DashboardResponseDto dashboard = new DashboardResponseDto(1, 2, 3, 6, 4, 1, List.of(), List.of());

        when(service.getDashboard()).thenReturn(dashboard);

        ResponseEntity<?> response = controller.getDashboard();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("data", dashboard), response.getBody());
        verify(service).getDashboard();
    }

    @Test
    void serviceBuildsDashboardTotalsAndRows() {
        ITeamService teams = mock(ITeamService.class);
        ITicketEventsRepository tickets = mock(ITicketEventsRepository.class);
        IAttendantRepository attendants = mock(IAttendantRepository.class);
        RabbitQueueService rabbitQueueService = mock(RabbitQueueService.class);
        DashboardService service = new DashboardService(
                teams,
                tickets,
                attendants,
                rabbitQueueService,
                "cards.queue",
                "borrow.queue",
                "others.queue");
        var cards = TestFixtures.team(TicketTeamEnum.CARDS);
        var borrow = TestFixtures.team(TicketTeamEnum.BORROW);
        var others = TestFixtures.team(TicketTeamEnum.OTHERS);
        var projection = attendantWorkload(UUID.randomUUID(), "Ana", TicketTeamEnum.CARDS, 2);

        when(teams.getAllTeams()).thenReturn(List.of(cards, borrow, others));
        when(rabbitQueueService.getQueueSize("cards.queue")).thenReturn(4L);
        when(rabbitQueueService.getQueueSize("borrow.queue")).thenReturn(1L);
        when(rabbitQueueService.getQueueSize("others.queue")).thenReturn(2L);
        when(tickets.countByTeamIdAndStatus(cards.getId(), TicketStatusEnum.IN_PROGRESS)).thenReturn(2L);
        when(tickets.countByTeamIdAndStatus(cards.getId(), TicketStatusEnum.FINISHED)).thenReturn(3L);
        when(tickets.countByTeamIdAndStatus(borrow.getId(), TicketStatusEnum.IN_PROGRESS)).thenReturn(5L);
        when(tickets.countByTeamIdAndStatus(borrow.getId(), TicketStatusEnum.FINISHED)).thenReturn(7L);
        when(tickets.countByTeamIdAndStatus(others.getId(), TicketStatusEnum.IN_PROGRESS)).thenReturn(1L);
        when(tickets.countByTeamIdAndStatus(others.getId(), TicketStatusEnum.FINISHED)).thenReturn(1L);
        when(tickets.findAttendantWorkloads(TicketStatusEnum.IN_PROGRESS)).thenReturn(List.of(projection));
        when(attendants.count()).thenReturn(8L);

        DashboardResponseDto dashboard = service.getDashboard();

        assertEquals(7, dashboard.totalQueuedTickets());
        assertEquals(8, dashboard.totalInProgressTickets());
        assertEquals(11, dashboard.totalFinishedTickets());
        assertEquals(26, dashboard.totalTickets());
        assertEquals(8, dashboard.totalAttendants());
        assertEquals(1, dashboard.attendantsWithTickets());
        assertEquals(3, dashboard.queues().size());
        assertEquals(9, dashboard.queues().getFirst().totalTickets());
        assertEquals("Ana", dashboard.attendants().getFirst().attendantName());
    }

    @Test
    void dtoMapsDashboardRows() {
        var team = TestFixtures.team(TicketTeamEnum.OTHERS);
        var queue = DashboardQueueResponseDto.from(team, 1, 2, 3);
        var projection = attendantWorkload(UUID.randomUUID(), "Bruno", TicketTeamEnum.OTHERS, 3);
        var attendant = DashboardAttendantResponseDto.from(projection);

        assertEquals(team.getId(), queue.teamId());
        assertEquals(TicketTeamEnum.OTHERS, queue.teamName());
        assertEquals(6, queue.totalTickets());
        assertEquals(projection.getAttendantId(), attendant.attendantId());
        assertEquals("Bruno", attendant.attendantName());
        assertInstanceOf(DashboardAttendantResponseDto.class, attendant);
    }

    private AttendantWorkloadProjection attendantWorkload(
            UUID attendantId,
            String attendantName,
            TicketTeamEnum teamName,
            long inProgressTickets) {
        return new AttendantWorkloadProjection() {
            @Override
            public UUID getAttendantId() {
                return attendantId;
            }

            @Override
            public String getAttendantName() {
                return attendantName;
            }

            @Override
            public TicketTeamEnum getTeamName() {
                return teamName;
            }

            @Override
            public long getInProgressTickets() {
                return inProgressTickets;
            }
        };
    }
}
