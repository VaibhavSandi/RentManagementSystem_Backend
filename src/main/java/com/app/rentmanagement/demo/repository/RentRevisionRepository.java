package com.app.rentmanagement.demo.repository;

import com.app.rentmanagement.demo.entity.RentRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentRevisionRepository extends JpaRepository<RentRevision, Long> {
    List<RentRevision> findByRenterRenterIdOrderByEffectiveDateAsc(Long renterId);
}
