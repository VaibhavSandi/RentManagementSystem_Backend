package com.app.rentmanagement.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    private Long totalFlats;

    private Long occupiedFlats;

    private Long vacantFlats;

    private Long activeRenters;

    // Rent month the period figures are for (default: latest due rent month)
    private Integer rentMonth;
    private Integer rentYear;
    private String rentPeriod;

    // Period figures, from the rent ledger for rentMonth
    private BigDecimal expectedRent;

    // Same as expectedRent; kept for existing clients
    private BigDecimal currentMonthRent;

    private BigDecimal collectedAmount;

    // Unpaid balance of rentMonth only
    private BigDecimal pendingAmount;

    // Outstanding rent of all months up to the latest due rent month (as of today)
    private BigDecimal totalOutstanding;

    // Renters with outstanding rent (as of today)
    private Long overdueRenters;

    // e.g. "September 2026": totalOutstanding / overdueRenters are calculated up to this month
    private String outstandingAsOfPeriod;

    // Expenses dated within the calendar month of rentMonth
    private BigDecimal totalExpenses;

    private Long totalRenters;

    private DashboardDto stats;

    private List<RentPaymentDto> recentPayments;

    // Renters with outstanding rent, month-wise, biggest first
    private List<RenterPendingSummaryDto> pendingRenters;

    // Expected / collected / pending for the six rent months ending at rentMonth
    private List<MonthlyRentTotalsDto> monthlyTrend;
}
