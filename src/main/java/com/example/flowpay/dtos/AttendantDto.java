package com.example.flowpay.dtos;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AttendantDto {
    @NotBlank(message = "{attendant.name.required}")
    private String name;

    @NotNull(message = "{attendant.teamId.required}")
    private UUID teamId;
}
