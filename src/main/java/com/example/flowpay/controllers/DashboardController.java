package com.example.flowpay.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.flowpay.services.IDashboardService;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {
    private final IDashboardService service;

    public DashboardController(IDashboardService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getDashboard() {
        return ResponseEntity.ok().body(Map.of("data", service.getDashboard()));
    }
}
