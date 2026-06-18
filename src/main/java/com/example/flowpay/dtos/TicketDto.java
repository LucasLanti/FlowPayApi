package com.example.flowpay.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TicketDto {
    private String content;
    @NotNull(message = "{ticket.teamId.required}")
    private UUID teamId;
}
