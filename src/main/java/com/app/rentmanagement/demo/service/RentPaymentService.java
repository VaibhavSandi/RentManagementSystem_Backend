package com.app.rentmanagement.demo.service;

import com.app.rentmanagement.demo.dto.RentPaymentDto;

import java.util.List;

public interface RentPaymentService {


    RentPaymentDto createPayment(RentPaymentDto paymentDto);

    RentPaymentDto getPaymentById(Long paymentId);

    List<RentPaymentDto> getAllPayments();

    List<RentPaymentDto> getPaymentsByRenterId(Long renterId);

    RentPaymentDto updatePayment(Long paymentId, RentPaymentDto paymentDto);

    void deletePayment(Long paymentId);
}
