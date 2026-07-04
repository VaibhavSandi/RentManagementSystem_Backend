package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.PendingRentDto;
import com.app.rentmanagement.demo.entity.RentPayment;
import com.app.rentmanagement.demo.entity.Renter;
import com.app.rentmanagement.demo.repository.RentPaymentRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.service.PendingRentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PendingRentServiceImpl implements PendingRentService {

    private final RentPaymentRepository rentPaymentRepository;
    private final RenterRepository renterRepository;

    @Override
    public List<PendingRentDto> getAllPendingRents() {

        List<RentPayment> payments =
                rentPaymentRepository.findAllPendingRents();

        List<PendingRentDto> pendingList = payments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        LocalDate targetDate = LocalDate.now().minusMonths(1);
        int targetMonth = targetDate.getMonthValue();
        int targetYear = targetDate.getYear();

        List<Renter> activeRenters = renterRepository.findByStatus("Active");

        for (Renter renter : activeRenters) {
            Optional<RentPayment> payment = rentPaymentRepository.findByRenterRenterIdAndRentMonthAndRentYear(
                    renter.getRenterId(), targetMonth, targetYear
            );

            if (payment.isEmpty()) {
                pendingList.add(PendingRentDto.builder()
                        .renterId(renter.getRenterId())
                        .renterName(renter.getRenterName())
                        .flatNo(renter.getFlat().getFlatNo())
                        .monthlyRent(renter.getMonthlyRent())
                        .paidAmount(BigDecimal.ZERO)
                        .pendingAmount(renter.getMonthlyRent())
                        .status("Pending")
                        .build());
            }
        }

        return pendingList;
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