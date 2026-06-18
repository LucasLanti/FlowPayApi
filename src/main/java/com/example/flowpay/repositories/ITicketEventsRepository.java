package com.example.flowpay.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flowpay.domains.Attendant;
import com.example.flowpay.domains.TicketEvents;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import com.example.flowpay.repositories.projections.AttendantWorkloadProjection;

public interface ITicketEventsRepository extends JpaRepository<TicketEvents, UUID> {
    long countByAttendantAndStatusAndTeamName(
            Attendant attendant,
            TicketStatusEnum status,
            TicketTeamEnum team);

    Page<TicketEvents> findByTeamId(UUID teamId, Pageable pageable);

    Page<TicketEvents> findByTeamIdAndStatus(UUID teamId, TicketStatusEnum status, Pageable pageable);

    long countByTeamIdAndStatus(UUID teamId, TicketStatusEnum status);

    @Query("""
                SELECT t.attendant.id AS attendantId,
                    t.attendant.name AS attendantName,
                    t.team.name AS teamName,
                    COUNT(t) AS inProgressTickets
                FROM ticket_events t
                WHERE t.status = :status
                    AND t.attendant IS NOT NULL
                GROUP BY t.attendant.id, t.attendant.name, t.team.name
                ORDER BY COUNT(t) DESC, t.attendant.name ASC
            """)
    List<AttendantWorkloadProjection> findAttendantWorkloads(@Param("status") TicketStatusEnum status);
}
