package com.app.rentmanagement.demo.service;

import java.util.List;

import com.app.rentmanagement.demo.dto.ParkingDetailsDto;

public interface ParkingService {

    List<ParkingDetailsDto> getAllParkingDetails();
    List<ParkingDetailsDto> getAllParkingDetailsbyStatus();
    List<ParkingDetailsDto> getAllOccupiedParkingDetails();
    List<ParkingDetailsDto> getParkingDetailsByFlatId(Long flatId);
    List<ParkingDetailsDto> getParkingDetailsByRenterId(Long renterId);
    
}
