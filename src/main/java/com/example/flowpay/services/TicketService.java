package com.example.flowpay.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.flowpay.configs.Translator;
import com.example.flowpay.domains.Attendant;
import com.example.flowpay.domains.Team;
import com.example.flowpay.domains.TicketEvents;
import com.example.flowpay.dtos.TicketDto;
import com.example.flowpay.exceptions.BadRequestException;
import com.example.flowpay.exceptions.NotFoundException;
import com.example.flowpay.repositories.ITicketEventsRepository;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;

@Service
@Primary
public class TicketService implements ITicketService {
    private static final long MAX_IN_PROGRESS_TICKETS_BY_ATTENDANT = 3;
    private static final String QUEUE_TICKET_PROCESSING_ERROR = "queue.ticket.processing_error";

    private final ITicketEventsRepository ticketEventsRepository;
    private final IAttendantService attendantService;
    private final ITeamService teamService;
    private final TicketQueuePublisher ticketQueuePublisher;
    private final RabbitTemplate rabbitTemplate;
    private final Translator translator;

    public TicketService(
            ITicketEventsRepository ticketEventsRepository,
            IAttendantService attendantService,
            ITeamService teamService,
            TicketQueuePublisher ticketQueuePublisher,
            RabbitTemplate rabbitTemplate,
            Translator translator) {
        this.ticketEventsRepository = ticketEventsRepository;
        this.attendantService = attendantService;
        this.teamService = teamService;
        this.ticketQueuePublisher = ticketQueuePublisher;
        this.rabbitTemplate = rabbitTemplate;
        this.translator = translator;
    }

    @Override
    public Page<TicketEvents> getTickets(UUID teamId, TicketStatusEnum status, Pageable pageable) {
        if (teamService.getTeamById(teamId).isEmpty()) {
            throw new BadRequestException("ticket.teamId.not_found");
        }

        if (status == null) {
            return ticketEventsRepository.findByTeamId(teamId, pageable);
        }

        return ticketEventsRepository.findByTeamIdAndStatus(teamId, status, pageable);
    }

    @Override
    public void addNewTicket(TicketDto ticketRequest) {
        Optional<Team> team = getRequiredTeam(ticketRequest);
        if (team.isEmpty()) {
            throw new BadRequestException("ticket.teamId.not_found");
        }
        ticketQueuePublisher.publish(ticketRequest, team.get().getName());
    }

    @Transactional
    public void createNextTicketFromQueue(String queue, TicketTeamEnum ticketTeam, long pendingReplies) {
        long remainingTickets = pendingReplies;
        List<Attendant> attendants = attendantService.getAvailabilityAttendant(ticketTeam, pendingReplies);

        for (Attendant attendant : attendants) {
            if (remainingTickets <= 0) {
                return;
            }

            long availableSlots = countAvailableSlots(attendant, ticketTeam);

            for (long slot = 0; slot < availableSlots && remainingTickets > 0; slot++) {
                boolean ticketCreated = createNextTicketFromQueue(queue, attendant);

                if (!ticketCreated) {
                    return;
                }

                remainingTickets--;
            }
        }
    }

    @Override
    public void createTicket(TicketDto ticketRequest, Attendant attendant) {
        TicketEvents ticket = new TicketEvents();

        ticket.setContent(ticketRequest.getContent());
        ticket.setTeam(getRequiredTeam(ticketRequest)
                .orElseThrow(() -> new BadRequestException("ticket.teamId.not_found")));
        ticket.setStatus(TicketStatusEnum.IN_PROGRESS);
        ticket.setAttendant(attendant);

        ticketEventsRepository.save(ticket);
    }

    @Override
    public void finishTicket(UUID id) {
        TicketEvents ticket = ticketEventsRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("ticket.not_found"));

        ticket.setStatus(TicketStatusEnum.FINISHED);
        ticketEventsRepository.save(ticket);
    }

    private Optional<Team> getRequiredTeam(TicketDto ticketRequest) {
        return teamService.getTeamById(ticketRequest.getTeamId());
    }

    private long countAvailableSlots(Attendant attendant, TicketTeamEnum ticketTeam) {
        long usedSlots = ticketEventsRepository.countByAttendantAndStatusAndTeamName(
                attendant,
                TicketStatusEnum.IN_PROGRESS,
                ticketTeam);

        return Math.max(0, MAX_IN_PROGRESS_TICKETS_BY_ATTENDANT - usedSlots);
    }

    private boolean createNextTicketFromQueue(String queue, Attendant attendant) {
        try {
            TicketDto ticket = (TicketDto) rabbitTemplate.receiveAndConvert(queue);

            if (ticket == null) {
                return false;
            }

            createTicket(ticket, attendant);
            return true;
        } catch (Exception e) {
            System.err.println(translator.translate(QUEUE_TICKET_PROCESSING_ERROR) + ": " + e.getMessage());
            return false;
        }
    }
}
