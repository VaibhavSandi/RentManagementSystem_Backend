package com.app.rentmanagement.demo.service;

import com.app.rentmanagement.demo.dto.RentSmsReminderResultDto;
import com.app.rentmanagement.demo.dto.RentSmsReminderRunResultDto;

import java.time.LocalDate;

/**
 * SMS reminder to each active renter whose rent for the latest due rent month is unpaid
 * once its due date (the joining day-of-month) has passed. At most one SMS is sent per
 * renter per rent month, however often this runs.
 */
public interface RentSmsReminderService {

    /** Checks every active renter as of the given date. One renter's failure doesn't stop the others. */
    RentSmsReminderRunResultDto sendReminders(LocalDate today);

    /** Same check for one renter (manual testing); still sends at most once per rent month. */
    RentSmsReminderResultDto sendReminder(Long renterId, LocalDate today);
}
