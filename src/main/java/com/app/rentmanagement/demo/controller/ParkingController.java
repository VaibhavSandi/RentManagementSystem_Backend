package com.app.rentmanagement.demo.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.app.rentmanagement.demo.dto.ParkingDetailsDto;
import com.app.rentmanagement.demo.service.ParkingService;

import lombok.AllArgsConstructor;

@RequestMapping("/api/parking" )
@RestController
@AllArgsConstructor
public class ParkingController {



    private final ParkingService parkingService;

    @GetMapping("/allParkingDetails")
    public ResponseEntity<List<ParkingDetailsDto>> getAllParkingDetails() {
        return ResponseEntity.ok(parkingService.getAllParkingDetails());
    }

    @GetMapping("/getParkingBystatus")
    public ResponseEntity<List<ParkingDetailsDto>> getparkingbyStatus()
    {

        return ResponseEntity.ok(parkingService.getAllParkingDetailsbyStatus());

    }

    @GetMapping("/getparkingbyflat/{flatId}")
    public ResponseEntity<List<ParkingDetailsDto>> getParkingByFlatId(@PathVariable long flatId)
    {
        return ResponseEntity.ok(parkingService.getParkingDetailsByFlatId(flatId));
    }

    @GetMapping("/getparkingbyrenterId/{renterId}")
    public ResponseEntity<List<ParkingDetailsDto>> getParkingByrenterId(@PathVariable long renterId)
    {
        return ResponseEntity.ok(parkingService.getParkingDetailsByRenterId(renterId));
    }

}

