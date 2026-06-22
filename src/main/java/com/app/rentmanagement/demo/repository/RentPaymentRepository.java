package com.app.rentmanagement.demo.repository;

import com.app.rentmanagement.demo.entity.RentPayment;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RentPaymentRepository extends JpaRepository<RentPayment,Long> {

    List<RentPayment> findByRenterRenterId(Long renterId);

    List<RentPayment> findByRentMonthAndRentYear(Integer month,
                                                 Integer year);

    // Optional<RentPayment> findByRenterRenterIdAndRentMonthAndRentYear(
    //         Long renterId,
    //         Integer rentMonth,
    //         Integer rentYear
    // );



    @Query("""
SELECT r
FROM RentPayment r
WHERE r.pendingAmount > 0
""")
List<RentPayment> findAllPendingRents();


@Query("""
       SELECT COALESCE(SUM(r.monthlyRent),0)
       FROM RentPayment r
       WHERE r.rentMonth = :month
       AND r.rentYear = :year
       """)
BigDecimal getCurrentMonthTotalRent(
        @Param("month") Integer month,
        @Param("year") Integer year
);

  
@Transactional
@Modifying
@Query("DELETE FROM RentPayment r WHERE r.renter.renterId = :renterId")
void deleteByRenterId(@Param("renterId") Long renterId);
    Optional<RentPayment> findByRenterRenterIdAndRentMonthAndRentYear(
            Long renterId,
            Integer rentMonth,
            Integer rentYear
    );

    List<RentPayment> findTop5ByOrderByPaymentDateDesc();

    @Query("""
            SELECT COALESCE(SUM(r.amountPaid),0)
            FROM RentPayment r
            WHERE r.rentMonth = :month
            AND r.rentYear = :year
           """)
    BigDecimal getCurrentMonthCollectedAmount(
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    @Query("""
            SELECT COALESCE(SUM(r.pendingAmount),0)
            FROM RentPayment r
            WHERE r.rentMonth = :month
            AND r.rentYear = :year
           """)
    BigDecimal getCurrentMonthPendingAmount(
            @Param("month") Integer month,
            @Param("year") Integer year
    );

    List<RentPayment> findByPendingAmountGreaterThan(BigDecimal amount);



    
}
