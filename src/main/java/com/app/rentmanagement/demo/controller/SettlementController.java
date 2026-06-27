package com.app.rentmanagement.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.rentmanagement.demo.dto.RentPaymentDto;
import com.app.rentmanagement.demo.dto.RenterDto;
import com.app.rentmanagement.demo.dto.SettlementDto;
import com.app.rentmanagement.demo.service.SettlementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/settlements")
@RequiredArgsConstructor
public class SettlementController {

    private final SettlementService settlementService;

    @GetMapping("/active-renters")
    public ResponseEntity<List<RenterDto>> getActiveRenters() {
        return ResponseEntity.ok(settlementService.getActiveRenters());
    }

    @GetMapping("/ledger/{renterId}")
    public ResponseEntity<List<RentPaymentDto>> getLedger(
            @PathVariable Long renterId) {

        return ResponseEntity.ok(
                settlementService.getLedger(renterId));
    }

    @PostMapping("/saveSettlement")
    public ResponseEntity<SettlementDto> saveSettlement(
            @RequestBody SettlementDto dto) {

        return new ResponseEntity<>(
                settlementService.saveSettlement(dto),
                HttpStatus.CREATED);
    }

    @GetMapping("/allSettlements")
    public ResponseEntity<List<SettlementDto>> getAllSettlements() {

        return ResponseEntity.ok(
                settlementService.getAllSettlements());
    }
}