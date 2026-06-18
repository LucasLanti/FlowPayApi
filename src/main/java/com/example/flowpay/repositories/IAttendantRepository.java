package com.example.flowpay.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.flowpay.domains.Attendant;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;

import jakarta.persistence.LockModeType;

public interface IAttendantRepository extends JpaRepository<Attendant, UUID> {
    boolean existsByNameIgnoreCase(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT a
                FROM Attendant a
                WHERE (
                    SELECT COUNT(t)
                    FROM ticket_events t
                    WHERE t.attendant = a
                        AND t.status = :status
                        AND t.team.name = :team
                ) < 3
                AND a.team.name = :team
                ORDER BY (
                    SELECT COUNT(t)
                    FROM ticket_events t
                    WHERE t.attendant = a
                        AND t.status = :status
                        AND t.team.name = :team
                ) ASC
            """)
    List<Attendant> findAvailableAttendant(
            @Param("team") TicketTeamEnum team,
            @Param("status") TicketStatusEnum status,
            Pageable pageable);

}
