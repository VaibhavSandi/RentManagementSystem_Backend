package com.app.rentmanagement.demo.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Outcome of the rent SMS reminder for one renter. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentSmsReminderResultDto {

    public static final String SENT = "SENT";
    public static final String FAILED = "FAILED";
    public static final String ALREADY_SENT = "ALREADY_SENT";
    // Another run is sending it, or a previous send stopped midway (never resent automatically)
    public static final String IN_PROGRESS = "IN_PROGRESS";
    public static final String RETRY_LIMIT_REACHED = "RETRY_LIMIT_REACHED";
    public static final String NOT_DUE_YET = "NOT_DUE_YET";
    // Rent for the month is paid in full, or no rent is expected for it
    public static final String NOT_PENDING = "NOT_PENDING";
    public static final String INACTIVE = "INACTIVE";
    public static final String NO_MOBILE = "NO_MOBILE";
    public static final String INVALID_MOBILE = "INVALID_MOBILE";
    public static final String NO_JOINING_DATE = "NO_JOINING_DATE";
    public static final String DISABLED = "DISABLED";
    public static final String ERROR = "ERROR";

    private Long renterId;
    private String renterName;

    // Rent month the reminder is for, e.g. "September 2026"
    private String rentPeriod;
    private LocalDate dueDate;
    private BigDecimal pendingAmount;

    private String status;
    private String detail;
}
