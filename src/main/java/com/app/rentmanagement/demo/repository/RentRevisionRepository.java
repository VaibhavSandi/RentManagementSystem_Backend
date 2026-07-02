package com.app.rentmanagement.demo.repository;

import com.app.rentmanagement.demo.entity.RentRevision;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RentRevisionRepository extends JpaRepository<RentRevision, Long> {
    List<RentRevision> findByRenterRenterIdOrderByEffectiveDateAsc(Long renterId);

        @Transactional
    @Modifying
    @Query("DELETE FROM RentRevision rr WHERE rr.renter.id = :renterId")
    void deleteByRenterId(@Param("renterId") Long renterId);

}
