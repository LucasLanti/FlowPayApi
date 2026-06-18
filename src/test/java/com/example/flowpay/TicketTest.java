package com.example.flowpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.flowpay.configs.Translator;
import com.example.flowpay.controllers.TicketController;
import com.example.flowpay.domains.Attendant;
import com.example.flowpay.domains.Team;
import com.example.flowpay.domains.TicketEvents;
import com.example.flowpay.dtos.TicketDto;
import com.example.flowpay.dtos.response.PaginatedResponseDto;
import com.example.flowpay.dtos.response.TicketsResponseDto;
import com.example.flowpay.exceptions.BadRequestException;
import com.example.flowpay.exceptions.NotFoundException;
import com.example.flowpay.repositories.ITicketEventsRepository;
import com.example.flowpay.services.IAttendantService;
import com.example.flowpay.services.ITeamService;
import com.example.flowpay.services.ITicketService;
import com.example.flowpay.services.TicketQueuePublisher;
import com.example.flowpay.services.TicketService;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;

class TicketTest {

    @Test
    void controllerCreatesListsAndFinishesTickets() {
        ITicketService service = mock(ITicketService.class);
        TicketController controller = new TicketController(service);
        TicketDto request = new TicketDto("content", UUID.randomUUID());
        UUID teamId = UUID.randomUUID();
        TicketEvents ticket = TestFixtures.ticket(
                TestFixtures.team(TicketTeamEnum.CARDS),
                TestFixtures.attendant("Attendant"),
                TicketStatusEnum.IN_PROGRESS);

        when(service.getTickets(eq(teamId), eq(TicketStatusEnum.IN_PROGRESS), any()))
                .thenReturn(new PageImpl<>(List.of(ticket), PageRequest.of(0, 10), 1));

        ResponseEntity<?> created = controller.createTicket(request);
        ResponseEntity<?> listed = controller.getTickets(teamId, TicketStatusEnum.IN_PROGRESS, PageRequest.of(0, 10));
        UUID ticketId = UUID.randomUUID();
        ResponseEntity<?> finished = controller.finishTicket(ticketId);

        assertEquals(HttpStatus.OK, created.getStatusCode());
        assertEquals(Map.of("message", "Ticket criado com sucesso."), created.getBody());
        assertInstanceOf(PaginatedResponseDto.class, listed.getBody());
        assertEquals(Map.of("message", "Ticket finalizado com sucesso."), finished.getBody());
        verify(service).addNewTicket(request);
        verify(service).finishTicket(ticketId);
    }

    @Test
    void serviceFiltersPublishesCreatesAndFinishesTickets() {
        ITicketEventsRepository tickets = mock(ITicketEventsRepository.class);
        IAttendantService attendants = mock(IAttendantService.class);
        ITeamService teams = mock(ITeamService.class);
        TicketQueuePublisher publisher = mock(TicketQueuePublisher.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        Translator translator = mock(Translator.class);
        TicketService service = new TicketService(tickets, attendants, teams, publisher, rabbitTemplate, translator);
        UUID teamId = UUID.randomUUID();
        Team team = TestFixtures.team(TicketTeamEnum.CARDS);
        Attendant attendant = TestFixtures.attendant("Ana");
        TicketDto dto = new TicketDto("help", teamId);
        TicketEvents event = TestFixtures.ticket(team, attendant, TicketStatusEnum.IN_PROGRESS);
        PageRequest pageable = PageRequest.of(0, 10);

        when(teams.getTeamById(teamId)).thenReturn(Optional.of(team));
        when(tickets.findByTeamId(teamId, pageable)).thenReturn(new PageImpl<>(List.of(event)));
        when(tickets.findByTeamIdAndStatus(teamId, TicketStatusEnum.IN_PROGRESS, pageable))
                .thenReturn(new PageImpl<>(List.of(event)));
        when(tickets.findById(event.getId())).thenReturn(Optional.of(event));

        assertEquals(1, service.getTickets(teamId, null, pageable).getContent().size());
        assertEquals(1, service.getTickets(teamId, TicketStatusEnum.IN_PROGRESS, pageable).getContent().size());
        service.addNewTicket(dto);
        service.createTicket(dto, attendant);
        service.finishTicket(event.getId());

        assertEquals(TicketStatusEnum.FINISHED, event.getStatus());
        verify(publisher).publish(dto, TicketTeamEnum.CARDS);
        verify(tickets, times(2)).save(any(TicketEvents.class));
    }

    @Test
    void serviceHandlesTicketErrorsAndQueueBranches() {
        ITicketEventsRepository tickets = mock(ITicketEventsRepository.class);
        IAttendantService attendants = mock(IAttendantService.class);
        ITeamService teams = mock(ITeamService.class);
        TicketQueuePublisher publisher = mock(TicketQueuePublisher.class);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        Translator translator = mock(Translator.class);
        TicketService service = new TicketService(tickets, attendants, teams, publisher, rabbitTemplate, translator);
        UUID teamId = UUID.randomUUID();
        Team team = TestFixtures.team(TicketTeamEnum.CARDS);
        Attendant attendant = TestFixtures.attendant("Ana");

        when(teams.getTeamById(teamId)).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> service.getTickets(teamId, null, PageRequest.of(0, 1)));
        assertThrows(BadRequestException.class, () -> service.addNewTicket(new TicketDto("x", teamId)));
        assertThrows(BadRequestException.class, () -> service.createTicket(new TicketDto("x", teamId), attendant));
        assertThrows(NotFoundException.class, () -> service.finishTicket(UUID.randomUUID()));

        when(attendants.getAvailabilityAttendant(TicketTeamEnum.CARDS, 0)).thenReturn(List.of(attendant));
        service.createNextTicketFromQueue("queue", TicketTeamEnum.CARDS, 0);
        verify(rabbitTemplate, never()).receiveAndConvert("queue");

        when(attendants.getAvailabilityAttendant(TicketTeamEnum.CARDS, 1)).thenReturn(List.of(attendant));
        when(tickets.countByAttendantAndStatusAndTeamName(attendant, TicketStatusEnum.IN_PROGRESS,
                TicketTeamEnum.CARDS))
                .thenReturn(2L);
        when(rabbitTemplate.receiveAndConvert("queue")).thenReturn(null);
        service.createNextTicketFromQueue("queue", TicketTeamEnum.CARDS, 1);

        when(tickets.countByAttendantAndStatusAndTeamName(attendant, TicketStatusEnum.IN_PROGRESS,
                TicketTeamEnum.CARDS))
                .thenReturn(3L);
        service.createNextTicketFromQueue("queue", TicketTeamEnum.CARDS, 1);

        when(teams.getTeamById(teamId)).thenReturn(Optional.of(team));
        when(tickets.countByAttendantAndStatusAndTeamName(attendant, TicketStatusEnum.IN_PROGRESS,
                TicketTeamEnum.CARDS))
                .thenReturn(2L);
        when(rabbitTemplate.receiveAndConvert("queue")).thenReturn(new TicketDto("ok", teamId));
        service.createNextTicketFromQueue("queue", TicketTeamEnum.CARDS, 1);

        when(tickets.countByAttendantAndStatusAndTeamName(attendant, TicketStatusEnum.IN_PROGRESS,
                TicketTeamEnum.CARDS))
                .thenReturn(0L);
        when(attendants.getAvailabilityAttendant(TicketTeamEnum.CARDS, 2)).thenReturn(List.of(attendant));
        when(rabbitTemplate.receiveAndConvert("queue"))
                .thenReturn(new TicketDto("first", teamId), new TicketDto("second", teamId));
        service.createNextTicketFromQueue("queue", TicketTeamEnum.CARDS, 2);

        doThrow(new RuntimeException("boom")).when(rabbitTemplate).receiveAndConvert("broken");
        when(translator.translate("queue.ticket.processing_error")).thenReturn("Erro");
        service.createNextTicketFromQueue("broken", TicketTeamEnum.CARDS, 1);
        verify(tickets, times(3)).save(any(TicketEvents.class));
        verify(attendants, times(4)).getAvailabilityAttendant(eq(TicketTeamEnum.CARDS), eq(1L));
    }

    @Test
    void dtoMapsTicketResponseAndPagination() {
        Team team = TestFixtures.team(TicketTeamEnum.CARDS);
        Attendant attendant = TestFixtures.attendant("Ana");
        TicketEvents ticket = TestFixtures.ticket(team, attendant, TicketStatusEnum.IN_PROGRESS);
        TicketsResponseDto dto = TicketsResponseDto.from(ticket);
        Page<TicketsResponseDto> page = new PageImpl<>(List.of(dto), PageRequest.of(1, 5), 6);
        PaginatedResponseDto<List<TicketsResponseDto>> paginated = PaginatedResponseDto.from(page);
        PaginatedResponseDto<String> custom = PaginatedResponseDto.from(page, "custom");
        TicketsResponseDto nullNested = TicketsResponseDto
                .from(TestFixtures.ticket(null, null, TicketStatusEnum.FINISHED));

        assertEquals(ticket.getId(), dto.id());
        assertEquals(team.getId(), dto.team().id());
        assertEquals(attendant.getId(), dto.attendant().id());
        assertEquals(1, paginated.page());
        assertEquals(6, paginated.total());
        assertEquals(5, paginated.size());
        assertEquals("custom", custom.data());
        assertEquals(null, nullNested.team());
        assertEquals(null, nullNested.attendant());
    }

}
