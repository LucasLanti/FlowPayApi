package com.example.flowpay.dtos.response;

import java.util.UUID;

import com.example.flowpay.repositories.projections.AttendantWorkloadProjection;
import com.example.flowpay.utils.enums.TicketTeamEnum;

public record DashboardAttendantResponseDto(
        UUID attendantId,
        String attendantName,
        TicketTeamEnum teamName,
        long inProgressTickets) {
    public static DashboardAttendantResponseDto from(AttendantWorkloadProjection projection) {
        return new DashboardAttendantResponseDto(
                projection.getAttendantId(),
                projection.getAttendantName(),
                projection.getTeamName(),
                projection.getInProgressTickets());
    }
}
