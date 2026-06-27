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
@Column(name = "renter_id")
private Long renterId;

@Column(name = "renter_name", nullable = false)
private String renterName;

@Column(name = "mobile_number", nullable = false)
private String mobileNumber;

@Column(name = "id_proof_no")
private String idProofNo;

@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "flat_id")
private Flat flat;


@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id")
    private ParkingDetails parkingDetails;

@Column(name = "joining_date")
private LocalDate joiningDate;

@Column(name = "monthly_rent")
private BigDecimal monthlyRent;

@Column(name = "deposit_paid")
private BigDecimal depositPaid;

@Column(name = "status")
private String status;
}