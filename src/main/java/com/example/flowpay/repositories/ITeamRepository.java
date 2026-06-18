package com.example.flowpay.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.flowpay.domains.Team;
import com.example.flowpay.utils.enums.TicketTeamEnum;

public interface ITeamRepository extends JpaRepository<Team, UUID> {
    Optional<Team> findByName(TicketTeamEnum name);
}
