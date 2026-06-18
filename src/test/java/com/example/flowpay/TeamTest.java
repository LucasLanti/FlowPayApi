package com.example.flowpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.flowpay.controllers.TeamController;
import com.example.flowpay.domains.Team;
import com.example.flowpay.repositories.ITeamRepository;
import com.example.flowpay.services.ITeamService;
import com.example.flowpay.services.TeamService;
import com.example.flowpay.utils.enums.TicketTeamEnum;

class TeamTest {

    @Test
    void controllerListsTeams() {
        ITeamService service = mock(ITeamService.class);
        TeamController controller = new TeamController(service);
        Team team = TestFixtures.team(TicketTeamEnum.BORROW);

        when(service.getAllTeams()).thenReturn(List.of(team));

        assertEquals(Map.of("data", List.of(team)), controller.getAllTeamsAlias().getBody());
    }

    @Test
    void serviceDelegatesToRepository() {
        ITeamRepository repository = mock(ITeamRepository.class);
        TeamService service = new TeamService(repository);
        UUID id = UUID.randomUUID();
        Team team = TestFixtures.team(TicketTeamEnum.BORROW);

        when(repository.findAll()).thenReturn(List.of(team));
        when(repository.findByName(TicketTeamEnum.BORROW)).thenReturn(Optional.of(team));
        when(repository.findById(id)).thenReturn(Optional.of(team));

        assertEquals(List.of(team), service.getAllTeams());
        assertEquals(Optional.of(team), service.getTeamByName(TicketTeamEnum.BORROW));
        assertEquals(Optional.of(team), service.getTeamById(id));
    }
}
