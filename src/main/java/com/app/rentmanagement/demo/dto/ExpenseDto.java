package com.app.rentmanagement.demo.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDto {

    private Long expenseId;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String category;

    // Optional relation to Flat
    private Long flatId;
    private String flatNo;
}
