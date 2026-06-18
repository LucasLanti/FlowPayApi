package com.example.flowpay.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.example.flowpay.domains.Team;
import com.example.flowpay.dtos.response.DashboardAttendantResponseDto;
import com.example.flowpay.dtos.response.DashboardQueueResponseDto;
import com.example.flowpay.dtos.response.DashboardResponseDto;
import com.example.flowpay.repositories.IAttendantRepository;
import com.example.flowpay.repositories.ITicketEventsRepository;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;

@Service
@Primary
public class DashboardService implements IDashboardService {
    private final ITeamService teamService;
    private final ITicketEventsRepository ticketEventsRepository;
    private final IAttendantRepository attendantRepository;
    private final RabbitQueueService rabbitQueueService;
    private final String cardsQueue;
    private final String borrowQueue;
    private final String othersQueue;

    public DashboardService(
            ITeamService teamService,
            ITicketEventsRepository ticketEventsRepository,
            IAttendantRepository attendantRepository,
            RabbitQueueService rabbitQueueService,
            @Value("${app.rabbitmq.cards.queue}") String cardsQueue,
            @Value("${app.rabbitmq.borrow.queue}") String borrowQueue,
            @Value("${app.rabbitmq.others.queue}") String othersQueue) {
        this.teamService = teamService;
        this.ticketEventsRepository = ticketEventsRepository;
        this.attendantRepository = attendantRepository;
        this.rabbitQueueService = rabbitQueueService;
        this.cardsQueue = cardsQueue;
        this.borrowQueue = borrowQueue;
        this.othersQueue = othersQueue;
    }

    @Override
    public DashboardResponseDto getDashboard() {
        List<DashboardQueueResponseDto> queues = new ArrayList<>();
        long totalQueuedTickets = 0;
        long totalInProgressTickets = 0;
        long totalFinishedTickets = 0;

        for (Team team : teamService.getAllTeams()) {
            long queuedTickets = rabbitQueueService.getQueueSize(getQueueName(team.getName()));
            long inProgressTickets = ticketEventsRepository.countByTeamIdAndStatus(
                    team.getId(),
                    TicketStatusEnum.IN_PROGRESS);
            long finishedTickets = ticketEventsRepository.countByTeamIdAndStatus(
                    team.getId(),
                    TicketStatusEnum.FINISHED);

            totalQueuedTickets += queuedTickets;
            totalInProgressTickets += inProgressTickets;
            totalFinishedTickets += finishedTickets;
            queues.add(DashboardQueueResponseDto.from(team, queuedTickets, inProgressTickets, finishedTickets));
        }

        List<DashboardAttendantResponseDto> attendants = ticketEventsRepository
                .findAttendantWorkloads(TicketStatusEnum.IN_PROGRESS)
                .stream()
                .map(DashboardAttendantResponseDto::from)
                .toList();

        return new DashboardResponseDto(
                totalQueuedTickets,
                totalInProgressTickets,
                totalFinishedTickets,
                totalQueuedTickets + totalInProgressTickets + totalFinishedTickets,
                attendantRepository.count(),
                attendants.size(),
                queues,
                attendants);
    }

    private String getQueueName(TicketTeamEnum team) {
        return switch (team) {
            case CARDS -> cardsQueue;
            case BORROW -> borrowQueue;
            case OTHERS -> othersQueue;
        };
    }
}
