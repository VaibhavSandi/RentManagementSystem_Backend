package com.app.rentmanagement.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentRevisionDto {
    private Long revisionId;
    private Long renterId;
    private LocalDate effectiveDate;
    private BigDecimal rentAmount;
}
