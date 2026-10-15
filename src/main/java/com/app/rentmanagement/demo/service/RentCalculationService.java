package com.app.rentmanagement.demo.service;

import com.app.rentmanagement.demo.dto.MonthlyRentTotalsDto;
import com.app.rentmanagement.demo.dto.RentMonthStatusDto;
import com.app.rentmanagement.demo.dto.RenterPendingSummaryDto;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

/**
 * Single source of truth for rent-month calculations. Renter Payment, Pending Rent,
 * Dashboard, Reports and the monthly reminder email all go through this service.
 *
 * Rent Month (rent_month/rent_year on rent_payments) is the month the rent is FOR;
 * Payment Date is when it was actually paid. Calculations always use the Rent Month.
 */
public interface RentCalculationService {

    /** Latest rent month that is due on the given date (e.g. Oct 2026 -> Sep 2026 with offset 1). */
    YearMonth getLatestDueRentMonth(LocalDate today);

    /** Every renter who owes rent for, or has paid towards, the given rent month. */
    List<RentMonthStatusDto> getRentersByMonth(YearMonth rentMonth);

    /** Expected / collected / pending totals per rent month, from..to inclusive, oldest first. */
    List<MonthlyRentTotalsDto> getMonthlyTotals(YearMonth from, YearMonth to);

    /** Renters with pending rent up to and including asOfRentMonth, month-wise. */
    List<RenterPendingSummaryDto> getPendingRenters(YearMonth asOfRentMonth);

    /** Month-wise pending details of one renter up to and including asOfRentMonth. */
    RenterPendingSummaryDto getPendingRentDetails(Long renterId, YearMonth asOfRentMonth);
}
