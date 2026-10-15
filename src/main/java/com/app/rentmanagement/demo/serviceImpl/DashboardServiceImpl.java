package com.app.rentmanagement.demo.serviceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.app.rentmanagement.demo.dto.DashboardDto;
import com.app.rentmanagement.demo.dto.MonthlyRentTotalsDto;
import com.app.rentmanagement.demo.dto.RenterPendingSummaryDto;
import com.app.rentmanagement.demo.service.RentCalculationService;
import com.app.rentmanagement.demo.mapper.RentPaymentMapper;
import com.app.rentmanagement.demo.repository.FlatRepository;
import com.app.rentmanagement.demo.repository.RentPaymentRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.service.DashboardService;
import com.app.rentmanagement.demo.repository.ExpenseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    // Rent months shown in the collection trend, ending at the selected month
    private static final int TREND_MONTHS = 6;

    private final FlatRepository flatRepository;
    private final RenterRepository renterRepository;
    private final RentPaymentRepository rentPaymentRepository;
    private final ExpenseRepository expenseRepository;
    private final RentCalculationService rentCalculationService;


    public DashboardDto getDashboardData(YearMonth requestedMonth){

        // Same "latest due rent month" as Pending Rent and the reminders (rent.due.month-offset)
        YearMonth latestDueMonth = rentCalculationService.getLatestDueRentMonth(LocalDate.now());
        YearMonth rentMonth = requestedMonth != null ? requestedMonth : latestDueMonth;

        // Expected / collected / pending per rent month, from the rent ledger
        List<MonthlyRentTotalsDto> trend =
                rentCalculationService.getMonthlyTotals(rentMonth.minusMonths(TREND_MONTHS - 1), rentMonth);
        MonthlyRentTotalsDto period = trend.get(trend.size() - 1);

        // Outstanding of all months up to today's due month, regardless of the selected period
        List<RenterPendingSummaryDto> pendingRenters = rentCalculationService.getPendingRenters(latestDueMonth).stream()
                .sorted(Comparator.comparing(RenterPendingSummaryDto::getTotalPending).reversed())
                .toList();
        BigDecimal totalOutstanding = pendingRenters.stream()
                .map(RenterPendingSummaryDto::getTotalPending)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

      LocalDate startDate = rentMonth.atDay(1);
      LocalDate endDate = rentMonth.atEndOfMonth();
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
              .rentMonth(rentMonth.getMonthValue())
              .rentYear(rentMonth.getYear())
              .rentPeriod(rentMonth.format(PERIOD_FORMAT))
              .expectedRent(period.getExpectedRent())
              .currentMonthRent(period.getExpectedRent())
              .collectedAmount(period.getCollectedRent())
              .pendingAmount(period.getPendingRent())
              .totalOutstanding(totalOutstanding)
              .overdueRenters((long) pendingRenters.size())
              .outstandingAsOfPeriod(latestDueMonth.format(PERIOD_FORMAT))
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
                .pendingRenters(pendingRenters)
                .monthlyTrend(trend)
                .build();
    }


    }
