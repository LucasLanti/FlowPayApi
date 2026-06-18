package com.example.flowpay.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.flowpay.domains.Attendant;
import com.example.flowpay.dtos.AttendantDto;
import com.example.flowpay.utils.enums.TicketTeamEnum;

public interface IAttendantService {
    List<Attendant> getAvailabilityAttendant(TicketTeamEnum ticketTeam, long pendingReplies);

    void addAttendant(AttendantDto attendantRequest);

    Page<Attendant> getAllAttendants(Pageable pageable);
}
