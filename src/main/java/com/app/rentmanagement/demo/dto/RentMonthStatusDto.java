package com.app.rentmanagement.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Rent position of one renter for one RENT MONTH (the month the rent is for),
 * which is independent of the date(s) on which it was actually paid.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentMonthStatusDto {

    private Long renterId;
    private String renterName;
    private String email;
    private String flatNo;

    // Month the rent is for
    private Integer rentMonth;
    private Integer rentYear;
    private String rentPeriod; // e.g. "September 2026"

    // Date by which this month's rent is expected
    private LocalDate dueDate;

    // Expected rent for the month, from rent revisions / renter master
    private BigDecimal rentAmount;

    // Sum of all payment records tagged to this rent month
    private BigDecimal paidAmount;

    // rentAmount - paidAmount, never negative
    private BigDecimal pendingAmount;

    // Excess paid over rentAmount (advance / credit)
    private BigDecimal creditAmount;

    // Paid / Partial / Pending
    private String status;

    private Integer paymentCount;

    // Actual dates on which payments for this rent month were made
    private List<LocalDate> paymentDates;
    private LocalDate lastPaymentDate;
    private String paymentModes;
}
