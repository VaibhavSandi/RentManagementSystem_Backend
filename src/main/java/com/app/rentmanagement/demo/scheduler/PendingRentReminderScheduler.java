package com.app.rentmanagement.demo.scheduler;

import com.app.rentmanagement.demo.service.PendingRentReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Sends the monthly pending rent reminder on rent.reminder.day at rent.reminder.hour:minute.
 *
 * The check also runs daily at that time and once at startup, so a reminder missed
 * because the application was down on the reminder day is sent on the next run in
 * the same month. The email log guarantees each reminder is sent only once a month.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "rent.reminder.enabled", havingValue = "true")
public class PendingRentReminderScheduler {

    private final PendingRentReminderService reminderService;

    @Value("${rent.reminder.day:25}")
    private int reminderDay;

    @Value("${rent.reminder.hour:9}")
    private int reminderHour;

    @Value("${rent.reminder.minute:0}")
    private int reminderMinute;

    @Value("${rent.reminder.zone:Asia/Kolkata}")
    private String zone;

    @Scheduled(cron = "0 ${rent.reminder.minute:0} ${rent.reminder.hour:9} * * *", zone = "${rent.reminder.zone:Asia/Kolkata}")
    public void runScheduled() {
        runIfDue("schedule");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        runIfDue("startup");
    }

    void runIfDue(String trigger) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zone));
        if (!isDue(now)) {
            return;
        }
        try {
            log.info("Running pending rent reminder ({})", trigger);
            reminderService.sendMonthlyReminders(now.toLocalDate());
        } catch (Exception e) {
            log.error("Pending rent reminder run failed ({})", trigger, e);
        }
    }

    /** True from the configured day and time until the end of the month. */
    boolean isDue(ZonedDateTime now) {
        int day = Math.min(reminderDay, now.toLocalDate().lengthOfMonth());
        if (now.getDayOfMonth() != day) {
            return now.getDayOfMonth() > day;
        }
        return !now.toLocalTime().isBefore(LocalTime.of(reminderHour, reminderMinute));
    }
}
