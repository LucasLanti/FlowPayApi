package com.example.flowpay.dtos.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.flowpay.domains.Attendant;
import com.example.flowpay.domains.Team;
import com.example.flowpay.domains.TicketEvents;
import com.example.flowpay.utils.enums.TicketStatusEnum;
import com.example.flowpay.utils.enums.TicketTeamEnum;
import com.fasterxml.jackson.annotation.JsonFormat;

public record TicketsResponseDto(
        UUID id,
        TeamResponse team,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime updatedAt,
        AttendantResponse attendant,
        TicketStatusEnum status,
        String content) {
    public static TicketsResponseDto from(TicketEvents ticket) {
        return new TicketsResponseDto(
                ticket.getId(),
                TeamResponse.from(ticket.getTeam()),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                AttendantResponse.from(ticket.getAttendant()),
                ticket.getStatus(),
                ticket.getContent());
    }

    public record TeamResponse(
            UUID id,
            TicketTeamEnum name) {
        private static TeamResponse from(Team team) {
            if (team == null) {
                return null;
            }

            return new TeamResponse(team.getId(), team.getName());
        }
    }

    public record AttendantResponse(
            UUID id,
            String name) {
        private static AttendantResponse from(Attendant attendant) {
            if (attendant == null) {
                return null;
            }

            return new AttendantResponse(attendant.getId(), attendant.getName());
        }
    }
}
