package com.example.flowpay.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flowpay.services.ITeamService;

@RestController
@RequestMapping("/team")
public class TeamController {
    private final ITeamService service;

    public TeamController(ITeamService service) {
        this.service = service;
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTeamsAlias() {
        return ResponseEntity.ok().body(Map.of("data", service.getAllTeams()));
    }
}
