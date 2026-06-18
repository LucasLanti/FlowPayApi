package com.example.flowpay.dtos.response;

import java.util.List;

public record DashboardResponseDto(
        long totalQueuedTickets,
        long totalInProgressTickets,
        long totalFinishedTickets,
        long totalTickets,
        long totalAttendants,
        long attendantsWithTickets,
        List<DashboardQueueResponseDto> queues,
        List<DashboardAttendantResponseDto> attendants) {
}
