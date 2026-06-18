package com.example.flowpay.services;

import java.util.Optional;
import java.util.UUID;

import com.example.flowpay.domains.Team;
import com.example.flowpay.utils.enums.TicketTeamEnum;

public interface ITeamService {
    Iterable<Team> getAllTeams();

    Optional<Team> getTeamByName(TicketTeamEnum name);

    Optional<Team> getTeamById(UUID id);
}
