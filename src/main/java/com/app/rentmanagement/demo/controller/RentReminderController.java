package com.app.rentmanagement.demo.controller;

import com.app.rentmanagement.demo.dto.RentSmsReminderResultDto;
import com.app.rentmanagement.demo.dto.RentSmsReminderRunResultDto;
import com.app.rentmanagement.demo.service.RentSmsReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Manual runs of the renter rent SMS reminder (authenticated like every other API).
 * These only send what the daily job would send today: an unpaid, overdue rent month,
 * at most once per renter per month. They never send arbitrary text or numbers.
 */
@RestController
@RequestMapping("/api/rent-reminders")
@RequiredArgsConstructor
public class RentReminderController {

    private final RentSmsReminderService rentSmsReminderService;

    @Value("${rent.reminder.zone:Asia/Kolkata}")
    private String zone;

    @PostMapping("/run")
    public ResponseEntity<RentSmsReminderRunResultDto> runReminders() {
        return ResponseEntity.ok(rentSmsReminderService.sendReminders(today()));
    }

    @PostMapping("/{renterId}")
    public ResponseEntity<RentSmsReminderResultDto> sendReminder(@PathVariable Long renterId) {
        return ResponseEntity.ok(rentSmsReminderService.sendReminder(renterId, today()));
    }

    private LocalDate today() {
        return LocalDate.now(ZoneId.of(zone));
    }
}
