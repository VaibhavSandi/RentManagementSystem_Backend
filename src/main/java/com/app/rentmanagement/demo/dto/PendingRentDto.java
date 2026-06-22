package com.app.rentmanagement.demo.dto;



import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRentDto {

    private Long renterId;

    private String renterName;

    private String flatNo;

    private BigDecimal monthlyRent;

    private BigDecimal paidAmount;

    private BigDecimal pendingAmount;

    private String status;
}