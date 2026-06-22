package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.PendingRentDto;
import com.app.rentmanagement.demo.entity.RentPayment;
import com.app.rentmanagement.demo.repository.RentPaymentRepository;
import com.app.rentmanagement.demo.service.PendingRentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PendingRentServiceImpl implements PendingRentService {

    private final RentPaymentRepository rentPaymentRepository;

    @Override
    public List<PendingRentDto> getAllPendingRents() {

        List<RentPayment> payments =
                rentPaymentRepository.findAllPendingRents();

        return payments.stream()
                .map(this::mapToDto)
                .toList();
    }

    private PendingRentDto mapToDto(RentPayment payment) {

        String status = "PENDING";

        if (payment.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            status = "PARTIAL";
        }

        return PendingRentDto.builder()
                .renterId(payment.getRenter().getRenterId())
                .renterName(payment.getRenter().getRenterName())
                .flatNo(payment.getFlat().getFlatNo())
                .monthlyRent(payment.getMonthlyRent())
                .paidAmount(payment.getAmountPaid())
                .pendingAmount(payment.getPendingAmount())
                .status(status)
                .build();
    }
}