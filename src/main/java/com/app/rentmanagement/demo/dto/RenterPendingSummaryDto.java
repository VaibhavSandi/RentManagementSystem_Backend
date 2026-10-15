package com.app.rentmanagement.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RenterPendingSummaryDto {

    private Long renterId;
    private String renterName;
    private String email;
    private String mobileNumber;
    private String flatNo;
    private BigDecimal monthlyRent;

    // Pending is calculated up to and including this rent month
    private Integer asOfRentMonth;
    private Integer asOfRentYear;

    // Only months with pendingAmount > 0, oldest first
    private List<RentMonthStatusDto> pendingMonths;

    // Number of rent months with pendingAmount > 0
    private int pendingMonthCount;

    private BigDecimal totalPending;

    // Total excess paid across months (shown separately, not netted off)
    private BigDecimal totalCredit;
}
