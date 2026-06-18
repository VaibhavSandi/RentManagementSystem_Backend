package com.app.rentmanagement.demo.dto;

import lombok.*;

import java.math.BigDecimal;

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
}