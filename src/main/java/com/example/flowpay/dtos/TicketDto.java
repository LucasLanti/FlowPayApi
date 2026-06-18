package com.example.flowpay.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TicketDto {
    @NotBlank(message = "{ticket.content.required}")
    @Size(max = 100, message = "{ticket.content.maxLength}")
    private String content;

    @NotNull(message = "{ticket.teamId.required}")
    private UUID teamId;
}
