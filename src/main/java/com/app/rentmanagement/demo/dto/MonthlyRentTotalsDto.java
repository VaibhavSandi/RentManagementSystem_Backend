package com.app.rentmanagement.demo.dto;

import lombok.*;

import java.math.BigDecimal;

/** Totals of all renters for one RENT MONTH (the month the rent is for). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyRentTotalsDto {

    private Integer rentMonth;
    private Integer rentYear;
    private String rentPeriod; // e.g. "September 2026"

    // Rent expected for the month (rent revisions / renter master)
    private BigDecimal expectedRent;

    // Payments tagged to the month
    private BigDecimal collectedRent;

    // Unpaid balance of the month, never negative per renter
    private BigDecimal pendingRent;

    // Renters with rent expected or paid for the month
    private int renterCount;
}
