package com.app.rentmanagement.demo.serviceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.app.rentmanagement.demo.dto.DashboardDto;
import com.app.rentmanagement.demo.mapper.RentPaymentMapper;
import com.app.rentmanagement.demo.repository.FlatRepository;
import com.app.rentmanagement.demo.repository.RentPaymentRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.service.DashboardService;
import com.app.rentmanagement.demo.service.PendingRentService;
import com.app.rentmanagement.demo.repository.ExpenseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {


    private final FlatRepository flatRepository;
    private final RenterRepository renterRepository;
    private final RentPaymentRepository rentPaymentRepository;
    private final PendingRentService pendingRentService;
    private final ExpenseRepository expenseRepository;


    public DashboardDto getDashboardData(){


        LocalDate today=LocalDate.now();

        Integer currentMonth=today.getMonthValue();
        Integer currentYear=today.getYear();


        BigDecimal collectedAmount=rentPaymentRepository.getCurrentMonthCollectedAmount(currentMonth, currentYear);

      BigDecimal  PendingAmount=rentPaymentRepository.getCurrentMonthPendingAmount(currentMonth, currentYear);

      LocalDate startDate = LocalDate.of(currentYear, currentMonth, 1);
      LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
      BigDecimal totalExpenses = expenseRepository.sumExpensesBetweenDates(startDate, endDate);
      if (totalExpenses == null) {
          totalExpenses = BigDecimal.ZERO;
      }


      DashboardDto stats=DashboardDto.builder()
              .totalFlats(flatRepository.count())
              .occupiedFlats(flatRepository.countByStatus("Occupied"))
              .vacantFlats(flatRepository.countByStatus("Vacant"))
              .totalRenters(renterRepository.count())
              .activeRenters(renterRepository.countByStatus("Active"))
             // .inactiveRenters(renterRepository.countByStatus("Inactive"))
              .currentMonthRent(renterRepository.getCurrentMonthRent())
              .collectedAmount(collectedAmount)
              .pendingAmount(PendingAmount)
              .totalExpenses(totalExpenses)
              .build();


     return DashboardDto.builder()
                .stats(stats)
                .recentPayments(
                        rentPaymentRepository.findTop5ByOrderByPaymentDateDesc()
                                .stream()
                                .map(RentPaymentMapper::toDto)
                                .toList()
                )
                .pendingRents(
                        pendingRentService.getAllPendingRents()
                )
                .build();
    }


    }


