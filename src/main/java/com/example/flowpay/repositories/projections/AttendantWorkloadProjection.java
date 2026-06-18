package com.example.flowpay.repositories.projections;

import java.util.UUID;

import com.example.flowpay.utils.enums.TicketTeamEnum;

public interface AttendantWorkloadProjection {
    UUID getAttendantId();

    String getAttendantName();

    TicketTeamEnum getTeamName();

    long getInProgressTickets();
}
