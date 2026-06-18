package com.example.flowpay.controllers;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flowpay.domains.Attendant;
import com.example.flowpay.dtos.AttendantDto;
import com.example.flowpay.dtos.response.PaginatedResponseDto;
import com.example.flowpay.services.IAttendantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/attendant")
public class AttendantController {
    private final IAttendantService service;

    public AttendantController(IAttendantService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> addAttendant(@Valid @RequestBody AttendantDto attendantRequest) {
        service.addAttendant(attendantRequest);
        return ResponseEntity.ok().body(Map.of("message", "Atendente adicionado com sucesso."));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllAttendants(Pageable pageable) {
        Page<Attendant> attendants = service.getAllAttendants(pageable);
        return ResponseEntity.ok().body(PaginatedResponseDto.from(attendants));
    }
}
