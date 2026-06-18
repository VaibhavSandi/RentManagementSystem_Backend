package com.app.rentmanagement.demo.dto;



import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class SettlementDto {
    private Long settlementId;

    private Long renterId;
    private String renterName;

    private Long flatId;
    private String flatNo;

    private LocalDate leavingDate;
    private BigDecimal depositAmount;
    private BigDecimal pendingRent;
    private BigDecimal deductionAmount;
    private String deductionReason;
    private BigDecimal finalRefundAmount;
}