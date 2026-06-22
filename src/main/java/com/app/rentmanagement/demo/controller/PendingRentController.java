package com.app.rentmanagement.demo.controller;



import com.app.rentmanagement.demo.dto.PendingRentDto;
import com.app.rentmanagement.demo.service.PendingRentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pending-rents")
@RequiredArgsConstructor
public class PendingRentController {

    private final PendingRentService pendingRentService;

    @GetMapping
    public ResponseEntity<List<PendingRentDto>> getAllPendingRents() {

        return ResponseEntity.ok(
                pendingRentService.getAllPendingRents()
        );
    }
}