package com.app.rentmanagement.demo.dto;



import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PendingRentDto {

    private Long renterId;

    private String renterName;

    private String flatNo;

    // Rent month this row is for, e.g. "September 2026"
    private String month;

    private Integer rentMonth;

    private Integer rentYear;

    private LocalDate dueDate;

    private BigDecimal monthlyRent;

    private BigDecimal paidAmount;

    private BigDecimal pendingAmount;

    private String status;
}