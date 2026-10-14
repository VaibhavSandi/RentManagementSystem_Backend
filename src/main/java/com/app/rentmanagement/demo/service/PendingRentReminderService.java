package com.app.rentmanagement.demo.service;

import com.app.rentmanagement.demo.dto.ReminderRunResultDto;

import java.time.LocalDate;

public interface PendingRentReminderService {

    /** HTML body of the owner's "Pending Rent Report" email as of the given date. */
    String generatePendingRentEmail(LocalDate asOfDate);

    /**
     * Sends this month's consolidated pending rent report to the owner (the only recipient).
     * Safe to call repeatedly: a month already sent (or skipped because nothing was pending)
     * is not sent again.
     */
    ReminderRunResultDto sendMonthlyReminders(LocalDate today);
}
