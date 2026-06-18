package com.app.rentmanagement.demo.repository;

import com.app.rentmanagement.demo.entity.RentPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RentPaymentRepository extends JpaRepository<RentPayment,Long> {

    List<RentPayment> findByRenterRenterId(Long renterId);

    List<RentPayment> findByRentMonthAndRentYear(Integer month,
                                                 Integer year);

    Optional<RentPayment> findByRenterRenterIdAndRentMonthAndRentYear(
            Long renterId,
            Integer rentMonth,
            Integer rentYear
    );
}
