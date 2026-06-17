package com.app.rentmanagement.demo.entity;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "renters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Renter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long renterId;

    @Column(nullable = false)
    private String renterName;

    @Column(nullable = false)
    private String mobileNumber;

    private String idProofNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    private LocalDate joiningDate;

    private BigDecimal monthlyRent;

    private BigDecimal depositPaid;

    private String status;
}