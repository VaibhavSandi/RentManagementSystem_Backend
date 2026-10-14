package com.app.rentmanagement.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.rentmanagement.demo.entity.ParkingDetails;

public interface ParkingRepository extends JpaRepository<ParkingDetails, Long> {




    List<ParkingDetails> findByIsOccupied(boolean isOccupied);
    List<ParkingDetails> findByRenterRenterId( Long renterId);
    List<ParkingDetails> findByFlatFlatId( Long flatId);

    
} 