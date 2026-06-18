package com.example.flowpay.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flowpay.domains.Attendant;
import com.example.flowpay.domains.TicketEvents;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ITicketEventsRepository extends JpaRepository<TicketEvents, UUID> {
    long countByAttendantAndStatusAndTeamName(
            Attendant attendant,
            TicketStatusEnum status,
            TicketTeamEnum team);

    Page<TicketEvents> findByTeamId(UUID teamId, Pageable pageable);

    Page<TicketEvents> findByTeamIdAndStatus(UUID teamId, TicketStatusEnum status, Pageable pageable);
}
