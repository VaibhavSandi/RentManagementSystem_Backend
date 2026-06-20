package com.app.rentmanagement.demo.controller;


import com.app.rentmanagement.demo.dto.RentPaymentDto;
import com.app.rentmanagement.demo.service.RentPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
    @RequestMapping("/api/rent-payments")
    @RequiredArgsConstructor
    public class RentPaymentController {

        private final RentPaymentService rentPaymentService;

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
    }

