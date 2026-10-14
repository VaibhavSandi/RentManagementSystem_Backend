package com.app.rentmanagement.demo.controller;


import com.app.rentmanagement.demo.dto.RentMonthStatusDto;
import com.app.rentmanagement.demo.dto.RentPaymentDto;
import com.app.rentmanagement.demo.exception.BadRequestException;
import com.app.rentmanagement.demo.service.RentCalculationService;
import com.app.rentmanagement.demo.service.RentPaymentService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
    @RequestMapping("/api/rent-payments")
    @RequiredArgsConstructor
    public class RentPaymentController {

        private final RentPaymentService rentPaymentService;
        private final RentCalculationService rentCalculationService;

        @PostMapping("/savePayment")
        public ResponseEntity<RentPaymentDto> createPayment(@RequestBody RentPaymentDto paymentDto) {
            return new ResponseEntity<>(rentPaymentService.createPayment(paymentDto), HttpStatus.CREATED);
        }

        @GetMapping("getpaymentbyid/{id}")
        public ResponseEntity<RentPaymentDto> getPaymentById(@PathVariable("id") Long paymentId) {
            return ResponseEntity.ok(rentPaymentService.getPaymentById(paymentId));
        }

        @GetMapping("getAllPayments")
        public ResponseEntity<?> getAllPayments() {
            return ResponseEntity.ok(rentPaymentService.getAllPayments());
        }

        @GetMapping("/renter/{renterId}")
        public ResponseEntity<?> getPaymentsByRenterId(@PathVariable Long renterId) {
            return ResponseEntity.ok(rentPaymentService.getPaymentsByRenterId(renterId));
        }

        @PutMapping("updatepayment/{id}")
        public ResponseEntity<RentPaymentDto> updatePayment(
                @PathVariable("id") Long paymentId,
                @RequestBody RentPaymentDto paymentDto) {

            return ResponseEntity.ok(rentPaymentService.updatePayment(paymentId, paymentDto));
        }

        @DeleteMapping("deletepayment/{id}")
        public ResponseEntity<String> deletePayment(@PathVariable("id") Long paymentId) {
            rentPaymentService.deletePayment(paymentId);
            return ResponseEntity.ok("Rent payment deleted successfully");
        }

        @GetMapping("/current-month-payments")
        public ResponseEntity<List<RentPaymentDto>> getCurrentMonthPayments() {
            return ResponseEntity.ok(rentPaymentService.getCurrentMonthPayments());
        }


        // Rent position of every renter for one rent month (the month the rent is for, not the payment date)
        @GetMapping("/month-status")
        public ResponseEntity<List<RentMonthStatusDto>> getRentersByMonth(
                @RequestParam Integer month,
                @RequestParam Integer year) {

            if (month < 1 || month > 12) {
                throw new BadRequestException("Month must be between 1 and 12");
            }
            return ResponseEntity.ok(rentCalculationService.getRentersByMonth(YearMonth.of(year, month)));
        }

        // Latest rent month that is due today, used as the default month filter
        @GetMapping("/due-month")
        public ResponseEntity<Map<String, Object>> getLatestDueRentMonth() {
            YearMonth dueMonth = rentCalculationService.getLatestDueRentMonth(LocalDate.now());
            return ResponseEntity.ok(Map.of(
                    "rentMonth", dueMonth.getMonthValue(),
                    "rentYear", dueMonth.getYear()
            ));
        }

        @GetMapping("/filter")
        public ResponseEntity<List<RentPaymentDto>> filterRentPayments(
                @RequestParam(required = false) Long renterId,
                @RequestParam(required = false) String fromDate,
                @RequestParam(required = false) String toDate
        ) {
            List<RentPaymentDto> filteredPayments = rentPaymentService.filterRentPayments(
                    renterId,
                    fromDate != null ? LocalDate.parse(fromDate) : null,
                    toDate != null ? LocalDate.parse(toDate) : null
            );
            return ResponseEntity.ok(filteredPayments);
        }
    }

