package com.app.rentmanagement.demo.controller;

import java.time.YearMonth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.rentmanagement.demo.dto.DashboardDto;
import com.app.rentmanagement.demo.exception.BadRequestException;
import com.app.rentmanagement.demo.service.DashboardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // month/year select the rent month for the period figures (default: latest due rent month)
    @GetMapping("/getData")
    public ResponseEntity<DashboardDto> getDashboardData(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        YearMonth rentMonth = null;
        if (month != null && year != null) {
            if (month < 1 || month > 12) {
                throw new BadRequestException("Month must be between 1 and 12");
            }
            rentMonth = YearMonth.of(year, month);
        }

        return ResponseEntity.ok(
                dashboardService.getDashboardData(rentMonth)
        );
    }
}
