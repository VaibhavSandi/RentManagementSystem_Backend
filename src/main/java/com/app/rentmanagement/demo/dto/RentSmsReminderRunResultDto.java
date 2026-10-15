package com.app.rentmanagement.demo.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentSmsReminderRunResultDto {

    private LocalDate runDate;

    // Rent month checked in this run, e.g. "September 2026"
    private String rentPeriod;

    private String smsProvider;

    // COMPLETED / DISABLED
    private String status;

    private int activeRenters;
    private int sent;
    private int failed;
    private int alreadySent;
    private int skipped;

    private List<RentSmsReminderResultDto> results;
}
