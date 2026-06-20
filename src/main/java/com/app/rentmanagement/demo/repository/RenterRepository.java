package com.app.rentmanagement.demo.repository;

import com.app.rentmanagement.demo.entity.Renter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RenterRepository extends JpaRepository<Renter,Long> {

    List<Renter> findByStatus(String status);

    List<Renter> findByRenterNameContainingIgnoreCase(String renterName);

    List<Renter> findByMobileNumber(String mobileNumber);

    boolean existsByFlat_FlatId(Long flatId);
}
