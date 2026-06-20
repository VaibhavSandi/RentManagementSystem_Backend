package com.app.rentmanagement.demo.serviceImpl;


import com.app.rentmanagement.demo.dto.RentPaymentDto;
import com.app.rentmanagement.demo.entity.Flat;
import com.app.rentmanagement.demo.entity.RentPayment;
import com.app.rentmanagement.demo.entity.Renter;
import com.app.rentmanagement.demo.exception.BadRequestException;
import com.app.rentmanagement.demo.exception.ResourceNotFoundException;
import com.app.rentmanagement.demo.mapper.RentPaymentMapper;
import com.app.rentmanagement.demo.repository.FlatRepository;
import com.app.rentmanagement.demo.repository.RentPaymentRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.service.RentPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RentPaymentServiceImpl implements RentPaymentService {

    private final RentPaymentRepository rentPaymentRepository;
    private final RenterRepository renterRepository;
    private final FlatRepository flatRepository;

    @Override
    public RentPaymentDto createPayment(RentPaymentDto paymentDto) {

        Renter renter = renterRepository.findById(paymentDto.getRenterId())
                .orElseThrow(() -> new ResourceNotFoundException("Renter", "renterId", paymentDto.getRenterId()));

        Flat flat = flatRepository.findById(paymentDto.getFlatId())
                .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatId", paymentDto.getFlatId()));

        rentPaymentRepository
                .findByRenterRenterIdAndRentMonthAndRentYear(
                        paymentDto.getRenterId(),
                        paymentDto.getRentMonth(),
                        paymentDto.getRentYear()
                )
                .ifPresent(existing -> {
                    throw new BadRequestException("Rent payment already exists for this renter and month");
                });

        if (paymentDto.getPaymentDate() == null) {
            paymentDto.setPaymentDate(LocalDateTime.now());
        }

        RentPayment payment = RentPaymentMapper.toEntity(paymentDto, renter, flat);

        RentPayment savedPayment = rentPaymentRepository.save(payment);

        return RentPaymentMapper.toDto(savedPayment);
    }

    @Override
    public RentPaymentDto getPaymentById(Long paymentId) {

        RentPayment payment = rentPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("RentPayment", "paymentId", paymentId));

        return RentPaymentMapper.toDto(payment);
    }

    @Override
    public List<RentPaymentDto> getAllPayments() {

        return rentPaymentRepository.findAll()
                .stream()
                .map(RentPaymentMapper::toDto)
                .toList();
    }

    @Override
    public List<RentPaymentDto> getPaymentsByRenterId(Long renterId) {

        return rentPaymentRepository.findByRenterRenterId(renterId)
                .stream()
                .map(RentPaymentMapper::toDto)
                .toList();
    }

    @Override
    public RentPaymentDto updatePayment(Long paymentId, RentPaymentDto paymentDto) {

        RentPayment payment = rentPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("RentPayment", "paymentId", paymentId));

        Renter renter = renterRepository.findById(paymentDto.getRenterId())
                .orElseThrow(() -> new ResourceNotFoundException("Renter", "renterId", paymentDto.getRenterId()));

        Flat flat = flatRepository.findById(paymentDto.getFlatId())
                .orElseThrow(() -> new ResourceNotFoundException("Flat", "flatId", paymentDto.getFlatId()));

        payment.setRenter(renter);
        payment.setFlat(flat);
        payment.setRentMonth(paymentDto.getRentMonth());
        payment.setRentYear(paymentDto.getRentYear());
        payment.setMonthlyRent(paymentDto.getMonthlyRent());
        payment.setAmountPaid(paymentDto.getAmountPaid());
        payment.setPaymentMode(paymentDto.getPaymentMode());
        payment.setRemark(paymentDto.getRemark());
        payment.setPaymentDate(
                paymentDto.getPaymentDate() != null
                        ? paymentDto.getPaymentDate()
                        : payment.getPaymentDate()
        );

        RentPayment updatedPayment = rentPaymentRepository.save(payment);

        return RentPaymentMapper.toDto(updatedPayment);
    }

    @Override
    public void deletePayment(Long paymentId) {

        RentPayment payment = rentPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("RentPayment", "paymentId", paymentId));

        rentPaymentRepository.delete(payment);
    }
}