package com.app.rentmanagement.demo.entity;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "flats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flatId;

    @Column(nullable = false)
    private String flatNo;

    private String buildingName;

    @Column(nullable = false)
    private BigDecimal monthlyRent;

    @Column(nullable = false)
    private BigDecimal depositAmount;

    private String status;
}