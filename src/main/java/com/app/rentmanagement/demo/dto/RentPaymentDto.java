package com.app.rentmanagement.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentPaymentDto {

    private Long paymentId;

    @NotNull(message = "Renter Id is required")
    private Long renterId;

    private String renterName;

    @NotNull(message = "Flat Id is required")
    private Long flatId;

    private String flatNo;

    @NotNull(message = "Rent Month is required")
    @Min(value = 1, message = "Month must be between 1 and 12")
    @Max(value = 12, message = "Month must be between 1 and 12")
    private Integer rentMonth;

    @NotNull(message = "Rent Year is required")
    private Integer rentYear;

    @NotNull(message = "Monthly Rent is required")
    private BigDecimal monthlyRent;

    @NotNull(message = "Amount Paid is required")
    private BigDecimal amountPaid;

    private BigDecimal pendingAmount;

    private String paymentMode;

    private String remark;

    private LocalDate  paymentDate;
}