package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.RentMonthStatusDto;
import com.app.rentmanagement.demo.dto.RenterPendingSummaryDto;
import com.app.rentmanagement.demo.dto.RentSmsReminderResultDto;
import com.app.rentmanagement.demo.dto.RentSmsReminderRunResultDto;
import com.app.rentmanagement.demo.entity.RentReminderLog;
import com.app.rentmanagement.demo.entity.Renter;
import com.app.rentmanagement.demo.exception.ResourceNotFoundException;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.service.RentCalculationService;
import com.app.rentmanagement.demo.service.RentSmsReminderService;
import com.app.rentmanagement.demo.sms.SmsResponse;
import com.app.rentmanagement.demo.sms.SmsService;
import com.app.rentmanagement.demo.util.DateUtil;
import com.app.rentmanagement.demo.util.MobileNumberUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Not transactional on purpose: each log claim/update commits on its own (REQUIRES_NEW),
 * and the SMS call is never made while a database transaction is held open.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RentSmsReminderServiceImpl implements RentSmsReminderService {

    private static final String TYPE = RentReminderLog.TYPE_RENTER_RENT_SMS;

    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.ENGLISH);

    private final RenterRepository renterRepository;
    private final RentCalculationService rentCalculationService;
    private final RentReminderLogService reminderLogService;
    private final SmsService smsService;

    @Value("${sms.enabled:false}")
    private boolean smsEnabled;

    // Total attempts allowed for a month's SMS that keeps failing
    @Value("${sms.max-attempts:3}")
    private int maxAttempts;

    @Value("${sms.signature:Rent Management System}")
    private String signature;

    @Value("${rent.due.month-offset:1}")
    private int dueMonthOffset;

    @Override
    public RentSmsReminderRunResultDto sendReminders(LocalDate today) {
        YearMonth rentMonth = rentCalculationService.getLatestDueRentMonth(today);

        RentSmsReminderRunResultDto result = RentSmsReminderRunResultDto.builder()
                .runDate(today)
                .rentPeriod(rentMonth.format(PERIOD_FORMAT))
                .smsProvider(smsService.providerName())
                .results(new ArrayList<>())
                .build();

        if (!smsEnabled) {
            log.info("Rent SMS reminders are disabled (sms.enabled=false); skipping");
            result.setStatus(RentSmsReminderResultDto.DISABLED);
            return result;
        }

        log.info("Starting monthly rent SMS reminder job for {} (rent month {}, provider {})",
                today, rentMonth, smsService.providerName());

        List<Renter> activeRenters = renterRepository.findAll().stream().filter(this::isActive).toList();
        log.info("Found {} active renters", activeRenters.size());

        Map<Long, RenterPendingSummaryDto> pendingByRenter = rentCalculationService.getPendingRenters(rentMonth).stream()
                .collect(Collectors.toMap(RenterPendingSummaryDto::getRenterId, Function.identity(), (a, b) -> a));

        for (Renter renter : activeRenters) {
            RentSmsReminderResultDto renterResult;
            try {
                renterResult = processRenter(renter, pendingByRenter.get(renter.getRenterId()), rentMonth, today);
            } catch (Exception e) {
                // Keep going: one renter's problem must not stop the rest
                log.error("Renter {} - rent SMS reminder failed unexpectedly", renter.getRenterId(), e);
                renterResult = resultFor(renter, rentMonth, null, null, RentSmsReminderResultDto.ERROR, e.getMessage());
            }
            result.getResults().add(renterResult);
        }

        result.setStatus("COMPLETED");
        result.setActiveRenters(activeRenters.size());
        result.setSent(count(result, RentSmsReminderResultDto.SENT));
        result.setFailed(count(result, RentSmsReminderResultDto.FAILED) + count(result, RentSmsReminderResultDto.ERROR));
        result.setAlreadySent(count(result, RentSmsReminderResultDto.ALREADY_SENT));
        result.setSkipped(activeRenters.size() - result.getSent() - result.getFailed() - result.getAlreadySent());

        log.info("Monthly rent SMS reminder job finished: {} sent, {} failed, {} already sent, {} skipped",
                result.getSent(), result.getFailed(), result.getAlreadySent(), result.getSkipped());
        return result;
    }

    @Override
    public RentSmsReminderResultDto sendReminder(Long renterId, LocalDate today) {
        Renter renter = renterRepository.findById(renterId)
                .orElseThrow(() -> new ResourceNotFoundException("Renter", "renterId", renterId));
        YearMonth rentMonth = rentCalculationService.getLatestDueRentMonth(today);

        if (!smsEnabled) {
            log.info("Rent SMS reminders are disabled (sms.enabled=false); skipping renter {}", renterId);
            return resultFor(renter, rentMonth, null, null, RentSmsReminderResultDto.DISABLED, "sms.enabled is false");
        }
        return processRenter(renter, rentCalculationService.getPendingRentDetails(renterId, rentMonth), rentMonth, today);
    }

    private RentSmsReminderResultDto processRenter(Renter renter, RenterPendingSummaryDto pendingSummary,
                                                   YearMonth rentMonth, LocalDate today) {
        Long renterId = renter.getRenterId();

        if (!isActive(renter)) {
            log.info("Renter {} - inactive; no reminder", renterId);
            return resultFor(renter, rentMonth, null, null, RentSmsReminderResultDto.INACTIVE, null);
        }
        if (renter.getJoiningDate() == null) {
            log.warn("Renter {} - no joining date, due date unknown; skipping", renterId);
            return resultFor(renter, rentMonth, null, null, RentSmsReminderResultDto.NO_JOINING_DATE, null);
        }

        LocalDate dueDate = DateUtil.rentDueDate(renter.getJoiningDate(), rentMonth, dueMonthOffset);
        log.info("Renter {} - rent for {} due date: {}", renterId, rentMonth, dueDate.format(DATE_FORMAT));

        if (today.isBefore(dueDate)) {
            log.info("Renter {} - due date not reached yet", renterId);
            return resultFor(renter, rentMonth, dueDate, null, RentSmsReminderResultDto.NOT_DUE_YET, null);
        }

        RentMonthStatusDto month = pendingMonth(pendingSummary, rentMonth);
        if (month == null) {
            log.info("Renter {} - rent for {} paid or not pending; no reminder", renterId, rentMonth);
            return resultFor(renter, rentMonth, dueDate, null, RentSmsReminderResultDto.NOT_PENDING, null);
        }
        BigDecimal pendingAmount = month.getPendingAmount();
        log.info("Renter {} - rent unpaid for {} (pending {})", renterId, rentMonth, pendingAmount);

        if (renter.getMobileNumber() == null || renter.getMobileNumber().isBlank()) {
            log.warn("Renter {} - no mobile number; SMS not sent", renterId);
            return resultFor(renter, rentMonth, dueDate, pendingAmount, RentSmsReminderResultDto.NO_MOBILE, null);
        }
        String mobile = MobileNumberUtil.normalizeIndianMobile(renter.getMobileNumber());
        if (mobile == null) {
            log.warn("Renter {} - invalid mobile number; SMS not sent", renterId);
            return resultFor(renter, rentMonth, dueDate, pendingAmount, RentSmsReminderResultDto.INVALID_MOBILE, null);
        }

        RentReminderLogService.ClaimResult claim;
        try {
            claim = reminderLogService.claimForRenter(TYPE, rentMonth, renterId, mobile, maxAttempts);
        } catch (DataAccessException e) {
            // Another run or instance claimed the same renter and month at the same moment
            claim = RentReminderLogService.ClaimResult.IN_PROGRESS;
        }

        switch (claim) {
            case ALREADY_SENT -> {
                log.info("Renter {} - reminder already sent for {}; skipping duplicate reminder", renterId, rentMonth);
                return resultFor(renter, rentMonth, dueDate, pendingAmount, RentSmsReminderResultDto.ALREADY_SENT, null);
            }
            case IN_PROGRESS -> {
                log.warn("Renter {} - reminder for {} is in progress or was interrupted; not resending", renterId, rentMonth);
                return resultFor(renter, rentMonth, dueDate, pendingAmount, RentSmsReminderResultDto.IN_PROGRESS, null);
            }
            case RETRY_LIMIT_REACHED -> {
                log.warn("Renter {} - reminder for {} failed {} times; no more retries", renterId, rentMonth, maxAttempts);
                return resultFor(renter, rentMonth, dueDate, pendingAmount, RentSmsReminderResultDto.RETRY_LIMIT_REACHED, null);
            }
            default -> {
                // CLAIMED: this run sends it
            }
        }

        log.info("Renter {} - sending SMS to {} via {}", renterId, MobileNumberUtil.mask(mobile), smsService.providerName());
        SmsResponse response;
        try {
            response = smsService.sendSms(mobile, buildMessage(renter, pendingAmount, rentMonth, dueDate));
        } catch (Exception e) {
            response = SmsResponse.failed(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }

        if (response.isSuccess()) {
            reminderLogService.markSentForRenter(TYPE, rentMonth, renterId);
            log.info("Renter {} - SMS sent successfully", renterId);
            return resultFor(renter, rentMonth, dueDate, pendingAmount, RentSmsReminderResultDto.SENT, null);
        }

        reminderLogService.markFailedForRenter(TYPE, rentMonth, renterId, response.getErrorMessage());
        log.warn("Renter {} - SMS failed: {}", renterId, response.getErrorMessage());
        return resultFor(renter, rentMonth, dueDate, pendingAmount, RentSmsReminderResultDto.FAILED, response.getErrorMessage());
    }

    /**
     * Plain GSM text ("Rs." rather than the rupee sign) so it fits in standard SMS segments;
     * a Unicode character would roughly halve the characters per segment and raise the cost.
     */
    String buildMessage(Renter renter, BigDecimal amount, YearMonth rentMonth, LocalDate dueDate) {
        return "Dear " + renter.getRenterName() + ", your rent of Rs. " + amount(amount)
                + " for " + rentMonth.format(PERIOD_FORMAT) + " was due on " + dueDate.format(DATE_FORMAT)
                + ". Please make the payment at the earliest.\n- " + signature;
    }

    // ── Helpers ────────────────────────────────────────────────────

    // Same rule as the rent ledger: a renter without a status is treated as active
    private boolean isActive(Renter renter) {
        return renter.getStatus() == null || "Active".equalsIgnoreCase(renter.getStatus().trim());
    }

    private static RentMonthStatusDto pendingMonth(RenterPendingSummaryDto summary, YearMonth rentMonth) {
        if (summary == null || summary.getPendingMonths() == null) {
            return null;
        }
        return summary.getPendingMonths().stream()
                .filter(m -> m.getRentYear() == rentMonth.getYear() && m.getRentMonth() == rentMonth.getMonthValue())
                .filter(m -> m.getPendingAmount() != null && m.getPendingAmount().signum() > 0)
                .findFirst()
                .orElse(null);
    }

    private static RentSmsReminderResultDto resultFor(Renter renter, YearMonth rentMonth, LocalDate dueDate,
                                                      BigDecimal pendingAmount, String status, String detail) {
        return RentSmsReminderResultDto.builder()
                .renterId(renter.getRenterId())
                .renterName(renter.getRenterName())
                .rentPeriod(rentMonth.format(PERIOD_FORMAT))
                .dueDate(dueDate)
                .pendingAmount(pendingAmount)
                .status(status)
                .detail(detail)
                .build();
    }

    private static int count(RentSmsReminderRunResultDto result, String status) {
        return (int) result.getResults().stream().filter(r -> status.equals(r.getStatus())).count();
    }

    private static String amount(BigDecimal amount) {
        NumberFormat format = NumberFormat.getNumberInstance(new Locale("en", "IN"));
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        return format.format(amount == null ? BigDecimal.ZERO : amount);
    }
}
