package com.example.flowpay.services;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.example.flowpay.domains.Attendant;
import com.example.flowpay.domains.Team;
import com.example.flowpay.dtos.AttendantDto;
import com.example.flowpay.exceptions.BadRequestException;
import com.example.flowpay.repositories.IAttendantRepository;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;

@Service
@Primary
public class AttendantService implements IAttendantService {
    private final IAttendantRepository attendantRepository;
    private final ITeamService teamService;

    public AttendantService(IAttendantRepository attendantRepository, ITeamService teamService) {
        this.attendantRepository = attendantRepository;
        this.teamService = teamService;
    }

    @Override
    public List<Attendant> getAvailabilityAttendant(TicketTeamEnum ticketTeam, long pendingReplies) {
        if (pendingReplies <= 0) {
            return List.of();
        }

        int pageSize = Math.toIntExact(Math.min(pendingReplies, Integer.MAX_VALUE));

        return attendantRepository.findAvailableAttendant(ticketTeam, TicketStatusEnum.IN_PROGRESS,
                PageRequest.of(0, pageSize));
    }

    @Override
    public void addAttendant(AttendantDto attendantRequest) {
        if (attendantRepository.existsByNameIgnoreCase(attendantRequest.getName())) {
            throw new BadRequestException("attendant.name.already_exists");
        }

        Team team = teamService.getTeamById(attendantRequest.getTeamId())
                .orElseThrow(() -> new BadRequestException("attendant.teamId.not_found"));

        Attendant attendant = new Attendant();
        attendant.setName(attendantRequest.getName());
        attendant.setTeam(team);

        attendantRepository.save(attendant);
    }

    @Override
    public Page<Attendant> getAllAttendants(Pageable pageable) {
        return attendantRepository.findAll(pageable);
    }
}
