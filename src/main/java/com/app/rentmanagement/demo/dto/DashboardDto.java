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

    private BigDecimal currentMonthRent;

    private BigDecimal collectedAmount;

    private BigDecimal pendingAmount;

    private BigDecimal totalExpenses;

    private Long totalRenters;
      private DashboardDto stats;

    private List<RentPaymentDto> recentPayments;

    private List<PendingRentDto> pendingRents;
}