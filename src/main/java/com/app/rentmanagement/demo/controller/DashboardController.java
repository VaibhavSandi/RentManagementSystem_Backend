package com.app.rentmanagement.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.rentmanagement.demo.dto.DashboardDto;
import com.app.rentmanagement.demo.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/getData")
    public ResponseEntity<DashboardDto> getDashboardData() {

        return ResponseEntity.ok(
                dashboardService.getDashboardData()
        );
    }
}