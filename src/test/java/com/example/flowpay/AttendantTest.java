package com.example.flowpay;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.flowpay.controllers.AttendantController;
import com.example.flowpay.domains.Attendant;
import com.example.flowpay.domains.Team;
import com.example.flowpay.dtos.AttendantDto;
import com.example.flowpay.dtos.response.PaginatedResponseDto;
import com.example.flowpay.exceptions.BadRequestException;
import com.example.flowpay.repositories.IAttendantRepository;
import com.example.flowpay.services.AttendantService;
import com.example.flowpay.services.IAttendantService;
import com.example.flowpay.services.ITeamService;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;

class AttendantTest {

    @Test
    void controllerCreatesAndListsAttendants() {
        IAttendantService service = mock(IAttendantService.class);
        AttendantController controller = new AttendantController(service);
        AttendantDto dto = new AttendantDto("Ana", UUID.randomUUID());
        Attendant attendant = TestFixtures.attendant("Ana");

        when(service.getAllAttendants(any()))
                .thenReturn(new PageImpl<>(List.of(attendant), PageRequest.of(0, 5), 1));

        assertEquals(Map.of("message", "Atendente adicionado com sucesso."), controller.addAttendant(dto).getBody());
        assertInstanceOf(PaginatedResponseDto.class, controller.getAllAttendants(PageRequest.of(0, 5)).getBody());
        verify(service).addAttendant(dto);
    }

    @Test
    void serviceCoversAvailabilityCreateErrorsAndPagination() {
        IAttendantRepository repository = mock(IAttendantRepository.class);
        ITeamService teams = mock(ITeamService.class);
        AttendantService service = new AttendantService(repository, teams);
        UUID teamId = UUID.randomUUID();
        Team team = TestFixtures.team(TicketTeamEnum.OTHERS);
        Attendant attendant = TestFixtures.attendant("Ana");
        PageRequest pageable = PageRequest.of(0, 2);

        assertEquals(List.of(), service.getAvailabilityAttendant(TicketTeamEnum.CARDS, 0));
        when(repository.findAvailableAttendant(eq(TicketTeamEnum.CARDS), eq(TicketStatusEnum.IN_PROGRESS), any()))
                .thenReturn(List.of(attendant));
        assertEquals(List.of(attendant), service.getAvailabilityAttendant(TicketTeamEnum.CARDS, 3));
        when(teams.getTeamById(teamId)).thenReturn(Optional.of(team));
        service.addAttendant(new AttendantDto("Ana", teamId));
        verify(repository).save(any(Attendant.class));
        when(repository.existsByNameIgnoreCase("Duplicado")).thenReturn(true);
        BadRequestException duplicateName = assertThrows(BadRequestException.class,
                () -> service.addAttendant(new AttendantDto("Duplicado", teamId)));
        assertEquals("attendant.name.already_exists", duplicateName.getKeyMessage());
        UUID missingTeamId = UUID.randomUUID();
        when(teams.getTeamById(missingTeamId)).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> service.addAttendant(new AttendantDto("Ana", missingTeamId)));
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(attendant), pageable, 1));
        assertEquals(1, service.getAllAttendants(pageable).getTotalElements());
    }
}
