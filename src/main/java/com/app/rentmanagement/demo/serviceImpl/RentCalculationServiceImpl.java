package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.MonthlyRentTotalsDto;
import com.app.rentmanagement.demo.dto.RentMonthStatusDto;
import com.app.rentmanagement.demo.dto.RenterPendingSummaryDto;
import com.app.rentmanagement.demo.entity.RentPayment;
import com.app.rentmanagement.demo.entity.RentRevision;
import com.app.rentmanagement.demo.entity.Renter;
import com.app.rentmanagement.demo.entity.Settlement;
import com.app.rentmanagement.demo.exception.ResourceNotFoundException;
import com.app.rentmanagement.demo.repository.RentPaymentRepository;
import com.app.rentmanagement.demo.repository.RentRevisionRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.repository.SettlementRepository;
import com.app.rentmanagement.demo.service.RentCalculationService;
import com.app.rentmanagement.demo.util.CommonUtil;
import com.app.rentmanagement.demo.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RentCalculationServiceImpl implements RentCalculationService {

    public static final String STATUS_PAID = "Paid";
    public static final String STATUS_PARTIAL = "Partial";
    public static final String STATUS_PENDING = "Pending";

    private static final DateTimeFormatter PERIOD_FORMAT =
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);

    private final RenterRepository renterRepository;
    private final RentPaymentRepository rentPaymentRepository;
    private final RentRevisionRepository rentRevisionRepository;
    private final SettlementRepository settlementRepository;

    /**
     * How many months after the rent month the rent is collected.
     * 1 = October collects September rent (the existing behaviour of Pending Rent / Dashboard).
     * 0 = rent is collected in the same month (advance / current-month rent).
     */
    @Value("${rent.due.month-offset:1}")
    private int dueMonthOffset;

    /**
     * Optional "yyyy-MM". No rent is expected before this month, so renters who joined
     * before payments were tracked in this system don't show false arrears.
     */
    @Value("${rent.calculation.start-month:}")
    private String calculationStartMonth;

    @Override
    public YearMonth getLatestDueRentMonth(LocalDate today) {
        return YearMonth.from(today).minusMonths(dueMonthOffset);
    }

    @Override
    public List<RentMonthStatusDto> getRentersByMonth(YearMonth rentMonth) {
        LedgerData data = loadLedgerData();

        List<RentMonthStatusDto> rows = new ArrayList<>();
        for (Renter renter : data.renters()) {
            rows.addAll(buildLedger(renter, data, rentMonth, rentMonth));
        }
        rows.sort(Comparator.comparing(RentMonthStatusDto::getRenterName,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return rows;
    }

    @Override
    public List<MonthlyRentTotalsDto> getMonthlyTotals(YearMonth from, YearMonth to) {
        LedgerData data = loadLedgerData();

        // Same rows as getRentersByMonth, for every month of the range in one pass
        Map<YearMonth, List<RentMonthStatusDto>> rowsByMonth = new HashMap<>();
        for (Renter renter : data.renters()) {
            for (RentMonthStatusDto row : buildLedger(renter, data, from, to)) {
                rowsByMonth.computeIfAbsent(YearMonth.of(row.getRentYear(), row.getRentMonth()), ym -> new ArrayList<>())
                        .add(row);
            }
        }

        List<MonthlyRentTotalsDto> totals = new ArrayList<>();
        for (YearMonth ym = from; !ym.isAfter(to); ym = ym.plusMonths(1)) {
            List<RentMonthStatusDto> rows = rowsByMonth.getOrDefault(ym, List.of());
            totals.add(MonthlyRentTotalsDto.builder()
                    .rentMonth(ym.getMonthValue())
                    .rentYear(ym.getYear())
                    .rentPeriod(ym.format(PERIOD_FORMAT))
                    .expectedRent(sum(rows, RentMonthStatusDto::getRentAmount))
                    .collectedRent(sum(rows, RentMonthStatusDto::getPaidAmount))
                    .pendingRent(sum(rows, RentMonthStatusDto::getPendingAmount))
                    .renterCount(rows.size())
                    .build());
        }
        return totals;
    }

    @Override
    public List<RenterPendingSummaryDto> getPendingRenters(YearMonth asOfRentMonth) {
        LedgerData data = loadLedgerData();

        return data.renters().stream()
                // Dues of settled renters are closed through Final Settlement
                .filter(renter -> !data.settledRenterIds().contains(renter.getRenterId()))
                .map(renter -> buildPendingSummary(renter, data, asOfRentMonth))
                .filter(summary -> summary.getTotalPending().signum() > 0)
                .sorted(Comparator.comparing(RenterPendingSummaryDto::getRenterName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    @Override
    public RenterPendingSummaryDto getPendingRentDetails(Long renterId, YearMonth asOfRentMonth) {
        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new ResourceNotFoundException("Renter", "renterId", renterId));

        LedgerData data = loadLedgerData();

        if (data.settledRenterIds().contains(renterId)) {
            return toSummary(renter, asOfRentMonth, List.of());
        }
        return buildPendingSummary(renter, data, asOfRentMonth);
    }

    // ── Core calculation ───────────────────────────────────────────

    private RenterPendingSummaryDto buildPendingSummary(Renter renter, LedgerData data, YearMonth asOfRentMonth) {
        return toSummary(renter, asOfRentMonth, buildLedger(renter, data, null, asOfRentMonth));
    }

    private RenterPendingSummaryDto toSummary(Renter renter, YearMonth asOfRentMonth, List<RentMonthStatusDto> ledger) {
        List<RentMonthStatusDto> pendingMonths = ledger.stream()
                .filter(row -> row.getPendingAmount().signum() > 0)
                .toList();

        return RenterPendingSummaryDto.builder()
                .renterId(renter.getRenterId())
                .renterName(renter.getRenterName())
                .email(renter.getEmail())
                .mobileNumber(renter.getMobileNumber())
                .flatNo(renter.getFlat() != null ? renter.getFlat().getFlatNo() : null)
                .monthlyRent(CommonUtil.safeAmount(renter.getMonthlyRent()))
                .asOfRentMonth(asOfRentMonth.getMonthValue())
                .asOfRentYear(asOfRentMonth.getYear())
                .pendingMonths(pendingMonths)
                .pendingMonthCount(pendingMonths.size())
                .totalPending(sum(pendingMonths, RentMonthStatusDto::getPendingAmount))
                .totalCredit(sum(ledger, RentMonthStatusDto::getCreditAmount))
                .build();
    }

    /**
     * Month-wise position of one renter between from (null = beginning) and to, inclusive.
     * A month is included when rent is expected for it, or when a payment is tagged to it.
     */
    private List<RentMonthStatusDto> buildLedger(Renter renter, LedgerData data, YearMonth from, YearMonth to) {
        Map<YearMonth, List<RentPayment>> payments =
                data.paymentsByRenter().getOrDefault(renter.getRenterId(), Map.of());
        List<RentRevision> revisions =
                data.revisionsByRenter().getOrDefault(renter.getRenterId(), List.of());

        TreeSet<YearMonth> months = new TreeSet<>();

        ChargeRange range = chargeRange(renter, data);
        if (range != null) {
            YearMonth start = from != null && from.isAfter(range.start()) ? from : range.start();
            YearMonth end = range.end() != null && range.end().isBefore(to) ? range.end() : to;

            for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
                if (range.leavingDate() == null || cycleStart(renter, ym).isBefore(range.leavingDate())) {
                    months.add(ym);
                }
            }
        }

        // Payments are never hidden, even outside the expected range (e.g. advance rent)
        payments.keySet().stream()
                .filter(ym -> (from == null || !ym.isBefore(from)) && !ym.isAfter(to))
                .forEach(months::add);

        return months.stream()
                .map(ym -> buildMonthStatus(renter, ym, payments.getOrDefault(ym, List.of()), revisions))
                .toList();
    }

    private RentMonthStatusDto buildMonthStatus(Renter renter, YearMonth ym,
                                                List<RentPayment> payments, List<RentRevision> revisions) {

        BigDecimal rentAmount = rentFor(renter, ym, revisions);

        // Sum every payment record for the month, so split payments add up correctly
        BigDecimal paidAmount = payments.stream()
                .map(p -> CommonUtil.safeAmount(p.getAmountPaid()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal difference = rentAmount.subtract(paidAmount);
        BigDecimal pendingAmount = difference.max(BigDecimal.ZERO);
        BigDecimal creditAmount = difference.negate().max(BigDecimal.ZERO);

        String status;
        if (pendingAmount.signum() == 0) {
            status = STATUS_PAID;
        } else if (paidAmount.signum() > 0) {
            status = STATUS_PARTIAL;
        } else {
            status = STATUS_PENDING;
        }

        List<LocalDate> paymentDates = payments.stream()
                .map(RentPayment::getPaymentDate)
                .filter(Objects::nonNull)
                .sorted()
                .toList();

        String paymentModes = payments.stream()
                .map(RentPayment::getPaymentMode)
                .filter(mode -> mode != null && !mode.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));

        return RentMonthStatusDto.builder()
                .renterId(renter.getRenterId())
                .renterName(renter.getRenterName())
                .email(renter.getEmail())
                .flatNo(renter.getFlat() != null ? renter.getFlat().getFlatNo() : null)
                .rentMonth(ym.getMonthValue())
                .rentYear(ym.getYear())
                .rentPeriod(ym.format(PERIOD_FORMAT))
                .dueDate(DateUtil.rentDueDate(renter.getJoiningDate(), ym, dueMonthOffset))
                .rentAmount(rentAmount)
                .paidAmount(paidAmount)
                .pendingAmount(pendingAmount)
                .creditAmount(creditAmount)
                .status(status)
                .paymentCount(payments.size())
                .paymentDates(paymentDates)
                .lastPaymentDate(paymentDates.isEmpty() ? null : paymentDates.get(paymentDates.size() - 1))
                .paymentModes(paymentModes.isEmpty() ? null : paymentModes)
                .build();
    }

    /**
     * Months for which rent is expected. Returns null when no month can be generated:
     * a renter marked inactive without a settlement has no known exit date, so only
     * the months that actually have payment records are shown for them.
     */
    private ChargeRange chargeRange(Renter renter, LedgerData data) {
        LocalDate leavingDate = data.leavingDates().get(renter.getRenterId());
        boolean active = renter.getStatus() == null || "Active".equalsIgnoreCase(renter.getStatus());

        if (!active && leavingDate == null) {
            return null;
        }
        if (renter.getJoiningDate() == null) {
            log.warn("Renter {} has no joining date; expected rent cannot be generated", renter.getRenterId());
            return null;
        }

        YearMonth start = YearMonth.from(renter.getJoiningDate());
        YearMonth configuredStart = parseCalculationStartMonth();
        if (configuredStart != null && configuredStart.isAfter(start)) {
            start = configuredStart;
        }

        YearMonth end = leavingDate != null ? YearMonth.from(leavingDate) : null;
        return new ChargeRange(start, end, leavingDate);
    }

    /**
     * Start of the rent cycle for a month: the joining date in the joining month,
     * otherwise the joining day-of-month (clamped to the month length).
     */
    private LocalDate cycleStart(Renter renter, YearMonth ym) {
        LocalDate joiningDate = renter.getJoiningDate();
        if (joiningDate == null) {
            return ym.atDay(1);
        }
        if (YearMonth.from(joiningDate).equals(ym)) {
            return joiningDate;
        }
        return ym.atDay(Math.min(joiningDate.getDayOfMonth(), ym.lengthOfMonth()));
    }

    /** Rent in force at the start of the month's cycle, from rent revisions; falls back to the renter master. */
    private BigDecimal rentFor(Renter renter, YearMonth ym, List<RentRevision> revisions) {
        LocalDate cycleStart = cycleStart(renter, ym);

        BigDecimal amount = null;
        for (RentRevision revision : revisions) {
            if (revision.getEffectiveDate() != null && !revision.getEffectiveDate().isAfter(cycleStart)) {
                amount = revision.getRentAmount();
            }
        }
        return CommonUtil.safeAmount(amount != null ? amount : renter.getMonthlyRent());
    }

    private YearMonth parseCalculationStartMonth() {
        if (calculationStartMonth == null || calculationStartMonth.isBlank()) {
            return null;
        }
        try {
            return YearMonth.parse(calculationStartMonth.trim());
        } catch (DateTimeParseException e) {
            log.warn("Ignoring invalid rent.calculation.start-month '{}', expected yyyy-MM", calculationStartMonth);
            return null;
        }
    }

    private static BigDecimal sum(List<RentMonthStatusDto> rows,
                                  java.util.function.Function<RentMonthStatusDto, BigDecimal> field) {
        return rows.stream().map(field).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ── Data loading ───────────────────────────────────────────────

    private LedgerData loadLedgerData() {
        Map<Long, Map<YearMonth, List<RentPayment>>> paymentsByRenter = new HashMap<>();
        for (RentPayment payment : rentPaymentRepository.findAll()) {
            if (payment.getRenter() == null || payment.getRentYear() == null
                    || payment.getRentMonth() == null || payment.getRentMonth() < 1 || payment.getRentMonth() > 12) {
                continue;
            }
            paymentsByRenter
                    .computeIfAbsent(payment.getRenter().getRenterId(), id -> new HashMap<>())
                    .computeIfAbsent(YearMonth.of(payment.getRentYear(), payment.getRentMonth()), ym -> new ArrayList<>())
                    .add(payment);
        }

        Map<Long, List<RentRevision>> revisionsByRenter = rentRevisionRepository.findAll().stream()
                .filter(revision -> revision.getRenter() != null && revision.getEffectiveDate() != null)
                .sorted(Comparator.comparing(RentRevision::getEffectiveDate))
                .collect(Collectors.groupingBy(revision -> revision.getRenter().getRenterId()));

        Map<Long, LocalDate> leavingDates = new HashMap<>();
        Set<Long> settledRenterIds = new HashSet<>();
        for (Settlement settlement : settlementRepository.findAll()) {
            if (settlement.getRenter() == null) {
                continue;
            }
            Long renterId = settlement.getRenter().getRenterId();
            settledRenterIds.add(renterId);

            LocalDate leavingDate = settlement.getLeavingDate() != null
                    ? settlement.getLeavingDate()
                    : settlement.getCreatedAt() != null ? settlement.getCreatedAt().toLocalDate() : null;
            if (leavingDate != null) {
                leavingDates.merge(renterId, leavingDate, (a, b) -> a.isAfter(b) ? a : b);
            }
        }

        return new LedgerData(renterRepository.findAll(), paymentsByRenter, revisionsByRenter,
                leavingDates, settledRenterIds);
    }

    private record LedgerData(List<Renter> renters,
                              Map<Long, Map<YearMonth, List<RentPayment>>> paymentsByRenter,
                              Map<Long, List<RentRevision>> revisionsByRenter,
                              Map<Long, LocalDate> leavingDates,
                              Set<Long> settledRenterIds) {
    }

    private record ChargeRange(YearMonth start, YearMonth end, LocalDate leavingDate) {
    }
}
