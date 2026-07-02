package com.app.rentmanagement.demo.entity;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rent_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentPayment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id")
    private Renter renter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flat_id")
    private Flat flat;

    

    @Column(name = "rent_month", nullable = false)
    private Integer rentMonth;

    @Column(name = "rent_year", nullable = false)
    private Integer rentYear;

    @Column(name = "monthly_rent", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "amount_paid", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountPaid;


@Column(
    name = "pending_amount",
    precision = 10,
    scale = 2,
    insertable = false,
    updatable = false
)
private BigDecimal pendingAmount;
    @Column(name = "payment_mode", length = 50)
    private String paymentMode;

    private String remark;

    @Column(name = "payment_date")
    private LocalDate  paymentDate;

   
}
