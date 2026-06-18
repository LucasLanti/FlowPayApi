package com.example.flowpay.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.example.flowpay.domains.Team;
import com.example.flowpay.repositories.ITeamRepository;
import com.example.flowpay.utils.enums.TicketTeamEnum;

@Service
@Primary
public class TeamService implements ITeamService {
    private final ITeamRepository teamRepository;

    public TeamService(
            ITeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @Override
    public Iterable<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    @Override
    public Optional<Team> getTeamByName(TicketTeamEnum name) {
        return teamRepository.findByName(name);
    }

    @Override
    public Optional<Team> getTeamById(UUID id) {
        return teamRepository.findById(id);
    }
}
