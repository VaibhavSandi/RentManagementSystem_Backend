package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.PendingRentDto;
import com.app.rentmanagement.demo.dto.RentMonthStatusDto;
import com.app.rentmanagement.demo.dto.RenterPendingSummaryDto;
import com.app.rentmanagement.demo.service.PendingRentService;
import com.app.rentmanagement.demo.service.RentCalculationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PendingRentServiceImpl implements PendingRentService {

    private final RentCalculationService rentCalculationService;

    /** One row per pending renter-month, up to the latest due rent month. */
    @Override
    public List<PendingRentDto> getAllPendingRents() {

        List<RenterPendingSummaryDto> pendingRenters = rentCalculationService.getPendingRenters(
                rentCalculationService.getLatestDueRentMonth(LocalDate.now()));

        List<PendingRentDto> pendingList = new ArrayList<>();
        for (RenterPendingSummaryDto renter : pendingRenters) {
            for (RentMonthStatusDto month : renter.getPendingMonths()) {
                pendingList.add(mapToDto(month));
            }
        }
        return pendingList;
    }

    private PendingRentDto mapToDto(RentMonthStatusDto month) {

        return PendingRentDto.builder()
                .renterId(month.getRenterId())
                .renterName(month.getRenterName())
                .flatNo(month.getFlatNo())
                .month(month.getRentPeriod())
                .rentMonth(month.getRentMonth())
                .rentYear(month.getRentYear())
                .dueDate(month.getDueDate())
                .monthlyRent(month.getRentAmount())
                .paidAmount(month.getPaidAmount())
                .pendingAmount(month.getPendingAmount())
                .status(month.getStatus())
                .build();
    }
}
