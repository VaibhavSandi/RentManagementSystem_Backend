package com.app.rentmanagement.demo.dto;



import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class FlatDto {
    private Long flatId;
    private String flatNo;
    private String buildingName;
    private BigDecimal monthlyRent;
    private BigDecimal depositAmount;
    private String status;
}