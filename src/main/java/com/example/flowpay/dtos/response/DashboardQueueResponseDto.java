package com.example.flowpay.dtos.response;

import java.util.UUID;

import com.example.flowpay.domains.Team;
import com.example.flowpay.utils.enums.TicketTeamEnum;

public record DashboardQueueResponseDto(
        UUID teamId,
        TicketTeamEnum teamName,
        long queuedTickets,
        long inProgressTickets,
        long finishedTickets,
        long totalTickets) {
    public static DashboardQueueResponseDto from(
            Team team,
            long queuedTickets,
            long inProgressTickets,
            long finishedTickets) {
        return new DashboardQueueResponseDto(
                team.getId(),
                team.getName(),
                queuedTickets,
                inProgressTickets,
                finishedTickets,
                queuedTickets + inProgressTickets + finishedTickets);
    }
}
