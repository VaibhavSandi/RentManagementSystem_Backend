package com.app.rentmanagement.demo.repository;

import com.app.rentmanagement.demo.entity.Renter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface RenterRepository extends JpaRepository<Renter,Long> {

    List<Renter> findByStatus(String status);

    List<Renter> findByRenterNameContainingIgnoreCase(String renterName);

    List<Renter> findByMobileNumber(String mobileNumber);

    boolean existsByFlat_FlatId(Long flatId);

    Long countByStatus(String status);

    @Query("""
       SELECT COALESCE(SUM(r.monthlyRent),0)
       FROM Renter r
       WHERE r.status = 'ACTIVE'
       """)
BigDecimal getCurrentMonthRent();

    @Query("SELECT r FROM Renter r WHERE r.status = 'Active'")
    List<Renter> findActivateRentersByStatus(String status);
}
