package com.app.rentmanagement.demo.controller;



import com.app.rentmanagement.demo.dto.PendingRentDto;
import com.app.rentmanagement.demo.dto.RenterPendingSummaryDto;
import com.app.rentmanagement.demo.dto.ReminderRunResultDto;
import com.app.rentmanagement.demo.exception.BadRequestException;
import com.app.rentmanagement.demo.service.PendingRentReminderService;
import com.app.rentmanagement.demo.service.PendingRentService;
import com.app.rentmanagement.demo.service.RentCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/pending-rents")
@RequiredArgsConstructor
public class PendingRentController {

    private final PendingRentService pendingRentService;
    private final RentCalculationService rentCalculationService;
    private final PendingRentReminderService pendingRentReminderService;

    @GetMapping
    public ResponseEntity<List<PendingRentDto>> getAllPendingRents() {

        return ResponseEntity.ok(
                pendingRentService.getAllPendingRents()
        );
    }

    // Pending renters with month-wise breakdown, up to the given rent month (default: latest due month)
    @GetMapping("/summary")
    public ResponseEntity<List<RenterPendingSummaryDto>> getPendingRenters(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        return ResponseEntity.ok(rentCalculationService.getPendingRenters(asOfRentMonth(month, year)));
    }

    @GetMapping("/renter/{renterId}")
    public ResponseEntity<RenterPendingSummaryDto> getPendingRentDetails(
            @PathVariable Long renterId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        return ResponseEntity.ok(rentCalculationService.getPendingRentDetails(renterId, asOfRentMonth(month, year)));
    }

    // Preview of the owner's pending rent email as it would be sent today (sends nothing)
    @GetMapping(value = "/reminder/preview", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> previewReminderEmail() {
        return ResponseEntity.ok(pendingRentReminderService.generatePendingRentEmail(LocalDate.now()));
    }

    // Sends this month's owner report now; not sent again if this month's report already went out
    @PostMapping("/reminder/send")
    public ResponseEntity<ReminderRunResultDto> sendReminderNow() {
        return ResponseEntity.ok(pendingRentReminderService.sendMonthlyReminders(LocalDate.now()));
    }

    private YearMonth asOfRentMonth(Integer month, Integer year) {
        if (month == null || year == null) {
            return rentCalculationService.getLatestDueRentMonth(LocalDate.now());
        }
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        return YearMonth.of(year, month);
    }
}