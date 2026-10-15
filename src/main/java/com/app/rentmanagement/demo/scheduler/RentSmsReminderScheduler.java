package com.app.rentmanagement.demo.scheduler;

import com.app.rentmanagement.demo.service.RentSmsReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Runs the renter rent SMS reminder daily (rent.sms-reminder.cron, default 9:00 AM).
 * Nothing is sent while sms.enabled=false. Running daily is safe: the reminder log
 * allows one SMS per renter per rent month.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RentSmsReminderScheduler {

    private final RentSmsReminderService rentSmsReminderService;

    @Value("${rent.reminder.zone:Asia/Kolkata}")
    private String zone;

    @Scheduled(cron = "${rent.sms-reminder.cron:0 0 9 * * *}", zone = "${rent.reminder.zone:Asia/Kolkata}")
    public void runScheduled() {
        try {
            rentSmsReminderService.sendReminders(LocalDate.now(ZoneId.of(zone)));
        } catch (Exception e) {
            log.error("Monthly rent SMS reminder job failed", e);
        }
    }
}
