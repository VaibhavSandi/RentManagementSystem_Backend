package com.app.rentmanagement.demo.dto;



import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RenterDto {
    private Long renterId;
    private String renterName;
    private String mobileNumber;
    private String idProofNo;

    private Long flatId;
    private String flatNo;

    
    private Long parkingId;

    private Integer parkingNumber;

    private boolean isOccupied;

    private LocalDate joiningDate;
    private BigDecimal monthlyRent;
    private BigDecimal depositPaid;
    private String status;
}