package com.app.rentmanagement.demo.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderRunResultDto {

    // yyyy-MM of the report month
    private String notificationMonth;

    // Pending calculated up to this rent month, e.g. "September 2026"
    private String asOfRentPeriod;

    private String ownerEmail;

    private int pendingRenterCount;
    private BigDecimal totalPending;

    // SENT / ALREADY_SENT / FAILED / SKIPPED_NO_PENDING / SKIPPED_NO_RECIPIENT / SKIPPED_MAIL_NOT_CONFIGURED
    private String status;
}
