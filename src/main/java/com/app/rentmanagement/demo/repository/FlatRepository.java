package com.app.rentmanagement.demo.repository;

import com.app.rentmanagement.demo.entity.Flat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlatRepository  extends JpaRepository<Flat,Long> {

    List<Flat> findByStatus(String status);

    boolean existsByFlatNo(String flatNo);


    Long countByStatus(String status);


}
