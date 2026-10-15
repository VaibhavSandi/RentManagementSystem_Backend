package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.RentMonthStatusDto;
import com.app.rentmanagement.demo.dto.RenterPendingSummaryDto;
import com.app.rentmanagement.demo.entity.*;
import com.app.rentmanagement.demo.repository.RentPaymentRepository;
import com.app.rentmanagement.demo.repository.RentRevisionRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.repository.SettlementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Today is taken as 14 October 2026, so the latest due rent month is September 2026. */
class RentCalculationServiceImplTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 14);
    private static final YearMonth AUG = YearMonth.of(2026, 8);
    private static final YearMonth SEP = YearMonth.of(2026, 9);
    private static final YearMonth OCT = YearMonth.of(2026, 10);

    private final RenterRepository renterRepository = mock(RenterRepository.class);
    private final RentPaymentRepository paymentRepository = mock(RentPaymentRepository.class);
    private final RentRevisionRepository revisionRepository = mock(RentRevisionRepository.class);
    private final SettlementRepository settlementRepository = mock(SettlementRepository.class);

    private final List<Renter> renters = new ArrayList<>();
    private final List<RentPayment> payments = new ArrayList<>();
    private final List<RentRevision> revisions = new ArrayList<>();
    private final List<Settlement> settlements = new ArrayList<>();

    private RentCalculationServiceImpl service;
    private long nextId = 1;

    @BeforeEach
    void setUp() {
        service = new RentCalculationServiceImpl(renterRepository, paymentRepository, revisionRepository, settlementRepository);
        ReflectionTestUtils.setField(service, "dueMonthOffset", 1);
        ReflectionTestUtils.setField(service, "calculationStartMonth", "");

        when(renterRepository.findAll()).thenReturn(renters);
        when(paymentRepository.findAll()).thenReturn(payments);
        when(revisionRepository.findAll()).thenReturn(revisions);
        when(settlementRepository.findAll()).thenReturn(settlements);
    }

    @Test
    void octoberCollectsSeptemberRent() {
        assertThat(service.getLatestDueRentMonth(TODAY)).isEqualTo(SEP);

        ReflectionTestUtils.setField(service, "dueMonthOffset", 0);
        assertThat(service.getLatestDueRentMonth(TODAY)).isEqualTo(OCT);
    }

    @Test
    void section21Scenario_AFullyPaid_BPending_CTwoMonthsPending() {
        Renter a = renter("Renter A", "2026-08-01", 10000, "a@example.com");
        Renter b = renter("Renter B", "2026-09-01", 8000, "b@example.com");
        Renter c = renter("Renter C", "2026-08-01", 12000, null);
        pay(a, AUG, 10000, "2026-09-03");
        pay(a, SEP, 10000, "2026-10-05"); // paid in October for September

        List<RentMonthStatusDto> september = service.getRentersByMonth(SEP);
        assertThat(september).extracting(RentMonthStatusDto::getRenterName)
                .containsExactly("Renter A", "Renter B", "Renter C");
        assertThat(row(september, a).getStatus()).isEqualTo("Paid");
        assertThat(row(september, a).getLastPaymentDate()).isEqualTo(LocalDate.of(2026, 10, 5));
        assertThat(row(september, b).getStatus()).isEqualTo("Pending");
        assertThat(row(september, c).getPendingAmount()).isEqualByComparingTo("12000");

        // The 5 Oct payment is September rent, so it is not counted in October
        RentMonthStatusDto aOctober = row(service.getRentersByMonth(OCT), a);
        assertThat(aOctober.getPaidAmount()).isEqualByComparingTo("0");
        assertThat(aOctober.getPaymentCount()).isZero();

        List<RenterPendingSummaryDto> pending = service.getPendingRenters(SEP);
        assertThat(pending).extracting(RenterPendingSummaryDto::getRenterName)
                .containsExactly("Renter B", "Renter C");

        RenterPendingSummaryDto summaryC = pending.get(1);
        assertThat(summaryC.getPendingMonths()).extracting(RentMonthStatusDto::getRentPeriod)
                .containsExactly("August 2026", "September 2026");
        assertThat(summaryC.getTotalPending()).isEqualByComparingTo("24000");
    }

    @Test
    void partialPayment() {
        Renter r = renter("Partial", "2026-09-01", 10000, null);
        pay(r, SEP, 6000, "2026-10-02");

        RentMonthStatusDto sep = row(service.getRentersByMonth(SEP), r);
        assertThat(sep.getStatus()).isEqualTo("Partial");
        assertThat(sep.getPaidAmount()).isEqualByComparingTo("6000");
        assertThat(sep.getPendingAmount()).isEqualByComparingTo("4000");
    }

    @Test
    void splitPaymentsAreSummedNotCountedAsRecords() {
        Renter full = renter("Two halves", "2026-09-01", 10000, null);
        pay(full, SEP, 5000, "2026-10-01");
        pay(full, SEP, 5000, "2026-10-10");

        Renter half = renter("One half", "2026-09-01", 10000, null);
        pay(half, SEP, 5000, "2026-10-01");

        List<RentMonthStatusDto> september = service.getRentersByMonth(SEP);
        assertThat(row(september, full).getStatus()).isEqualTo("Paid");
        assertThat(row(september, full).getPendingAmount()).isEqualByComparingTo("0");
        assertThat(row(september, full).getPaymentDates()).hasSize(2);

        assertThat(row(september, half).getPaidAmount()).isEqualByComparingTo("5000");
        assertThat(row(september, half).getPendingAmount()).isEqualByComparingTo("5000");
    }

    @Test
    void completelyUnpaid() {
        Renter r = renter("Unpaid", "2026-09-01", 10000, null);

        RentMonthStatusDto sep = row(service.getRentersByMonth(SEP), r);
        assertThat(sep.getStatus()).isEqualTo("Pending");
        assertThat(sep.getPendingAmount()).isEqualByComparingTo("10000");
    }

    @Test
    void overpaymentIsCreditNotNegativePending() {
        Renter r = renter("Over", "2026-09-01", 10000, null);
        pay(r, SEP, 12000, "2026-10-01");

        RentMonthStatusDto sep = row(service.getRentersByMonth(SEP), r);
        assertThat(sep.getStatus()).isEqualTo("Paid");
        assertThat(sep.getPendingAmount()).isEqualByComparingTo("0");
        assertThat(sep.getCreditAmount()).isEqualByComparingTo("2000");
        assertThat(service.getPendingRenters(SEP)).isEmpty();
    }

    @Test
    void noRentBeforeJoiningMonth() {
        Renter r = renter("Joined mid Sep", "2026-09-15", 9000, null);

        assertThat(service.getRentersByMonth(AUG)).isEmpty();
        RenterPendingSummaryDto summary = service.getPendingRentDetails(r.getRenterId(), SEP);
        assertThat(summary.getPendingMonths()).extracting(RentMonthStatusDto::getRentPeriod)
                .containsExactly("September 2026");
        assertThat(summary.getPendingMonths().get(0).getDueDate()).isEqualTo(LocalDate.of(2026, 10, 15));
    }

    @Test
    void noRentAfterLeavingAndSettledRentersAreNotReminded() {
        Renter r = renter("Left in Aug", "2026-07-01", 10000, "left@example.com");
        r.setStatus("INACTIVE");
        settlements.add(Settlement.builder().renter(r).leavingDate(LocalDate.of(2026, 8, 20)).build());

        assertThat(service.getRentersByMonth(AUG)).hasSize(1);
        assertThat(service.getRentersByMonth(SEP)).isEmpty();
        assertThat(service.getPendingRenters(SEP)).isEmpty();
    }

    @Test
    void leavingBeforeCycleStartExcludesThatMonth() {
        Renter r = renter("Left before cycle", "2026-06-10", 10000, null);
        settlements.add(Settlement.builder().renter(r).leavingDate(LocalDate.of(2026, 8, 5)).build());

        // August cycle would start on 10 Aug, after the 5 Aug exit
        assertThat(service.getRentersByMonth(YearMonth.of(2026, 7))).hasSize(1);
        assertThat(service.getRentersByMonth(AUG)).isEmpty();
    }

    @Test
    void inactiveWithoutSettlementOnlyShowsRecordedMonths() {
        Renter r = renter("Inactive", "2026-01-01", 10000, null);
        r.setStatus("INACTIVE");
        pay(r, AUG, 10000, "2026-09-01");

        assertThat(service.getRentersByMonth(AUG)).hasSize(1);
        assertThat(service.getRentersByMonth(SEP)).isEmpty();
        assertThat(service.getPendingRenters(SEP)).isEmpty();
    }

    @Test
    void rentRevisionAppliesFromItsEffectiveCycle() {
        Renter r = renter("Revised", "2026-08-01", 10000, null);
        revisions.add(RentRevision.builder().renter(r).effectiveDate(LocalDate.of(2026, 8, 1)).rentAmount(new BigDecimal("10000")).build());
        revisions.add(RentRevision.builder().renter(r).effectiveDate(LocalDate.of(2026, 9, 1)).rentAmount(new BigDecimal("11000")).build());
        r.setMonthlyRent(new BigDecimal("11000"));

        assertThat(row(service.getRentersByMonth(AUG), r).getRentAmount()).isEqualByComparingTo("10000");
        assertThat(row(service.getRentersByMonth(SEP), r).getRentAmount()).isEqualByComparingTo("11000");
    }

    @Test
    void calculationStartMonthSuppressesUntrackedHistory() {
        Renter r = renter("Old renter", "2024-01-05", 10000, null);
        ReflectionTestUtils.setField(service, "calculationStartMonth", "2026-09");

        RenterPendingSummaryDto summary = service.getPendingRentDetails(r.getRenterId(), SEP);
        assertThat(summary.getPendingMonths()).hasSize(1);
        assertThat(summary.getTotalPending()).isEqualByComparingTo("10000");
    }

    @Test
    void futureMonthsAreNotPendingYet() {
        renter("Current", "2026-09-01", 10000, null);

        // Pending is only counted up to the due month (September), not October
        assertThat(service.getPendingRenters(SEP).get(0).getPendingMonths()).hasSize(1);
    }

    // ── Pending Rent Report (per-renter summary) ───────────────────

    @Test
    void report_threeUnpaidMonths() {
        Renter amit = renter("Amit", "2026-07-01", 8000, null);
        pay(amit, YearMonth.of(2026, 7), 8000, "2026-08-02");

        // In November the latest due rent month is October
        RenterPendingSummaryDto summary = service.getPendingRenters(OCT).get(0);

        assertThat(months(summary)).containsExactly("2026-08", "2026-09", "2026-10");
        assertThat(summary.getPendingMonthCount()).isEqualTo(3);
        assertThat(summary.getTotalPending()).isEqualByComparingTo("24000");
        assertThat(summary.getMonthlyRent()).isEqualByComparingTo("8000");
    }

    @Test
    void report_paidMonthBetweenUnpaidMonthsIsExcluded() {
        Renter r = renter("Gap", "2026-08-01", 8000, null);
        pay(r, SEP, 8000, "2026-10-01");

        RenterPendingSummaryDto summary = service.getPendingRenters(OCT).get(0);

        assertThat(months(summary)).containsExactly("2026-08", "2026-10");
        assertThat(summary.getPendingMonthCount()).isEqualTo(2);
        assertThat(summary.getTotalPending()).isEqualByComparingTo("16000");
    }

    @Test
    void report_monthsAcrossTwoYearsKeepTheirYear() {
        renter("Year end", "2025-11-01", 7000, null);

        RenterPendingSummaryDto summary = service.getPendingRenters(YearMonth.of(2026, 2)).get(0);

        assertThat(months(summary)).containsExactly("2025-11", "2025-12", "2026-01", "2026-02");
        assertThat(summary.getPendingMonths()).extracting(RentMonthStatusDto::getRentPeriod)
                .containsExactly("November 2025", "December 2025", "January 2026", "February 2026");
        assertThat(summary.getTotalPending()).isEqualByComparingTo("28000");
    }

    @Test
    void report_partialMonthCountsOnlyTheBalance() {
        Renter r = renter("Partial", "2026-08-01", 8000, null);
        pay(r, AUG, 8000, "2026-09-01");
        pay(r, SEP, 5000, "2026-10-01");

        RenterPendingSummaryDto summary = service.getPendingRenters(SEP).get(0);

        assertThat(summary.getPendingMonthCount()).isEqualTo(1);
        assertThat(summary.getTotalPending()).isEqualByComparingTo("3000");
    }

    @Test
    void report_multipleRenters_noDuplicatesAndFullyPaidExcluded() {
        Renter a = renter("A", "2026-08-01", 8000, null);
        renter("B", "2026-09-01", 7000, null);
        Renter paid = renter("Paid", "2026-09-01", 9000, null);
        pay(paid, SEP, 9000, "2026-10-01");
        pay(a, AUG, 4000, "2026-09-01");
        pay(a, AUG, 4000, "2026-09-05"); // split payment, still one month

        List<RenterPendingSummaryDto> report = service.getPendingRenters(SEP);

        assertThat(report).extracting(RenterPendingSummaryDto::getRenterName).containsExactly("A", "B");
        assertThat(months(report.get(0))).containsExactly("2026-09");
        assertThat(months(report.get(1))).containsExactly("2026-09");
        assertThat(report.stream().map(RenterPendingSummaryDto::getTotalPending).reduce(BigDecimal.ZERO, BigDecimal::add))
                .isEqualByComparingTo("15000");
    }

    @Test
    void report_emptyWhenEveryoneHasPaid() {
        Renter r = renter("Paid", "2026-09-01", 9000, null);
        pay(r, SEP, 9000, "2026-10-01");

        assertThat(service.getPendingRenters(SEP)).isEmpty();
    }

    @Test
    void monthlyTotals_matchPerMonthRows() {
        Renter a = renter("A", "2026-08-01", 8000, null);
        Renter b = renter("B", "2026-09-01", 7000, null);
        pay(a, AUG, 8000, "2026-09-02");
        pay(a, SEP, 5000, "2026-10-02");
        pay(b, SEP, 7000, "2026-10-03");

        List<com.app.rentmanagement.demo.dto.MonthlyRentTotalsDto> totals = service.getMonthlyTotals(YearMonth.of(2026, 7), SEP);

        assertThat(totals).hasSize(3);
        // July: nobody had joined yet
        assertThat(totals.get(0).getExpectedRent()).isEqualByComparingTo("0");
        assertThat(totals.get(1).getExpectedRent()).isEqualByComparingTo("8000");
        assertThat(totals.get(1).getPendingRent()).isEqualByComparingTo("0");
        assertThat(totals.get(2).getRentPeriod()).isEqualTo("September 2026");
        assertThat(totals.get(2).getExpectedRent()).isEqualByComparingTo("15000");
        assertThat(totals.get(2).getCollectedRent()).isEqualByComparingTo("12000");
        assertThat(totals.get(2).getPendingRent()).isEqualByComparingTo("3000");
        assertThat(totals.get(2).getRenterCount()).isEqualTo(2);
    }

    private static List<String> months(RenterPendingSummaryDto summary) {
        return summary.getPendingMonths().stream()
                .map(m -> YearMonth.of(m.getRentYear(), m.getRentMonth()).toString())
                .toList();
    }

    // ── helpers ────────────────────────────────────────────────────

    private Renter renter(String name, String joiningDate, int rent, String email) {
        long id = nextId++;
        Renter renter = Renter.builder()
                .renterId(id)
                .renterName(name)
                .email(email)
                .flat(Flat.builder().flatId(id).flatNo("F" + id).build())
                .joiningDate(LocalDate.parse(joiningDate))
                .monthlyRent(new BigDecimal(rent))
                .status("Active")
                .build();
        renters.add(renter);
        when(renterRepository.findById(id)).thenReturn(Optional.of(renter));
        return renter;
    }

    private void pay(Renter renter, YearMonth rentMonth, int amount, String paymentDate) {
        payments.add(RentPayment.builder()
                .renter(renter)
                .rentMonth(rentMonth.getMonthValue())
                .rentYear(rentMonth.getYear())
                .monthlyRent(new BigDecimal(amount)) // the UI stores the paid amount here; it must be ignored
                .amountPaid(new BigDecimal(amount))
                .paymentDate(LocalDate.parse(paymentDate))
                .paymentMode("UPI")
                .build());
    }

    private static RentMonthStatusDto row(List<RentMonthStatusDto> rows, Renter renter) {
        return rows.stream().filter(r -> r.getRenterId().equals(renter.getRenterId())).findFirst().orElseThrow();
    }
}
