package com.app.rentmanagement.demo.dto;



import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RentPaymentDto {
    private Long paymentId;

    private Long renterId;
    private String renterName;

    private Long flatId;
    private String flatNo;

    private Integer rentMonth;
    private Integer rentYear;
    private BigDecimal monthlyRent;
    private BigDecimal amountPaid;
    private BigDecimal pendingAmount;

    private String paymentMode;
    private String remark;
    private LocalDateTime paymentDate;
}
