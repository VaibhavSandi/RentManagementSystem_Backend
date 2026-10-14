package com.app.rentmanagement.demo.scheduler;

import com.app.rentmanagement.demo.service.PendingRentReminderService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PendingRentReminderSchedulerTest {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private PendingRentReminderScheduler scheduler(int day) {
        PendingRentReminderScheduler scheduler = new PendingRentReminderScheduler(mock(PendingRentReminderService.class));
        ReflectionTestUtils.setField(scheduler, "reminderDay", day);
        ReflectionTestUtils.setField(scheduler, "reminderHour", 9);
        ReflectionTestUtils.setField(scheduler, "reminderMinute", 0);
        return scheduler;
    }

    @Test
    void dueFromTheConfiguredDayAndTime() {
        PendingRentReminderScheduler scheduler = scheduler(25);

        assertThat(scheduler.isDue(ZonedDateTime.of(2026, 10, 24, 23, 0, 0, 0, IST))).isFalse();
        assertThat(scheduler.isDue(ZonedDateTime.of(2026, 10, 25, 8, 59, 0, 0, IST))).isFalse();
        assertThat(scheduler.isDue(ZonedDateTime.of(2026, 10, 25, 9, 0, 0, 0, IST))).isTrue();
        // Missed on the 25th (application down): caught up later in the month
        assertThat(scheduler.isDue(ZonedDateTime.of(2026, 10, 27, 9, 0, 0, 0, IST))).isTrue();
        assertThat(scheduler.isDue(ZonedDateTime.of(2026, 11, 1, 9, 0, 0, 0, IST))).isFalse();
    }

    @Test
    void dayBeyondMonthLengthFallsOnLastDay() {
        PendingRentReminderScheduler scheduler = scheduler(31);

        assertThat(scheduler.isDue(ZonedDateTime.of(2026, 2, 28, 9, 0, 0, 0, IST))).isTrue();
    }
}
