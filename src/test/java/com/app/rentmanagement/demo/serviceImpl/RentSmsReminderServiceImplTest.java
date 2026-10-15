package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.RentSmsReminderResultDto;
import com.app.rentmanagement.demo.dto.RentSmsReminderRunResultDto;
import com.app.rentmanagement.demo.entity.RentPayment;
import com.app.rentmanagement.demo.entity.RentReminderLog;
import com.app.rentmanagement.demo.entity.Renter;
import com.app.rentmanagement.demo.repository.RentPaymentRepository;
import com.app.rentmanagement.demo.repository.RentReminderLogRepository;
import com.app.rentmanagement.demo.repository.RenterRepository;
import com.app.rentmanagement.demo.sms.SmsResponse;
import com.app.rentmanagement.demo.sms.SmsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Runs against the real rent ledger (RentCalculationServiceImpl) and reminder log on H2,
 * with only the SMS provider mocked. Rent is collected one month later
 * (rent.due.month-offset=1), so on 15 October the September rent is checked.
 */
@DataJpaTest
@Import({RentSmsReminderServiceImpl.class, RentReminderLogService.class, RentCalculationServiceImpl.class})
@TestPropertySource(properties = {
        "sms.enabled=true",
        "sms.max-attempts=3",
        "sms.signature=Test Apartments",
        "rent.due.month-offset=1",
        "rent.calculation.start-month="
})
@Transactional(propagation = Propagation.NOT_SUPPORTED) // let each claim commit like in production
class RentSmsReminderServiceImplTest {

    private static final LocalDate OCT_15 = LocalDate.of(2026, 10, 15);
    private static final YearMonth AUG = YearMonth.of(2026, 8);
    private static final YearMonth SEP = YearMonth.of(2026, 9);

    @Autowired
    private RentSmsReminderServiceImpl reminderService;

    @Autowired
    private RenterRepository renterRepository;

    @Autowired
    private RentPaymentRepository paymentRepository;

    @Autowired
    private RentReminderLogRepository logRepository;

    @MockitoBean
    private SmsService smsService;

    private int nextMobile = 1;

    @BeforeEach
    void setUp() {
        logRepository.deleteAll();
        paymentRepository.deleteAll();
        renterRepository.deleteAll();
        ReflectionTestUtils.setField(reminderService, "smsEnabled", true);

        when(smsService.providerName()).thenReturn("mock");
        when(smsService.sendSms(anyString(), anyString())).thenReturn(SmsResponse.sent("msg-1"));
    }

    // Case 1
    @Test
    void dueDatePassedAndUnpaid_sendsSms() {
        Renter amit = renter("Amit", "2026-08-10", 8000);

        RentSmsReminderRunResultDto run = reminderService.sendReminders(OCT_15);

        assertThat(statusOf(run, amit)).isEqualTo(RentSmsReminderResultDto.SENT);
        assertThat(run.getRentPeriod()).isEqualTo("September 2026");
        assertThat(run.getSent()).isEqualTo(1);

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(smsService).sendSms(eq(amit.getMobileNumber()), message.capture());
        assertThat(message.getValue()).isEqualTo(
                "Dear Amit, your rent of Rs. 8,000 for September 2026 was due on 10-Oct-2026. "
                        + "Please make the payment at the earliest.\n- Test Apartments");

        RentReminderLog entry = logRepository.findByNotificationKey("RENTER_RENT_SMS:2026-09:" + amit.getRenterId()).orElseThrow();
        assertThat(entry.getStatus()).isEqualTo(RentReminderLog.STATUS_SENT);
        assertThat(entry.getRenterId()).isEqualTo(amit.getRenterId());
        assertThat(entry.getSentAt()).isNotNull();
    }

    // Case 2
    @Test
    void dueDateNotReached_noSms() {
        Renter rahul = renter("Rahul", "2026-08-20", 9000);

        RentSmsReminderRunResultDto run = reminderService.sendReminders(OCT_15);

        assertThat(statusOf(run, rahul)).isEqualTo(RentSmsReminderResultDto.NOT_DUE_YET);
        verify(smsService, never()).sendSms(anyString(), anyString());

        // On the due date itself the reminder goes out
        assertThat(statusOf(reminderService.sendReminders(LocalDate.of(2026, 10, 20)), rahul))
                .isEqualTo(RentSmsReminderResultDto.SENT);
    }

    // Case 3
    @Test
    void rentAlreadyPaid_noSms() {
        Renter amit = renter("Amit", "2026-08-10", 8000);
        pay(amit, AUG, 8000);
        pay(amit, SEP, 5000);
        pay(amit, SEP, 3000); // split payment adds up to the full rent

        RentSmsReminderRunResultDto run = reminderService.sendReminders(OCT_15);

        assertThat(statusOf(run, amit)).isEqualTo(RentSmsReminderResultDto.NOT_PENDING);
        verify(smsService, never()).sendSms(anyString(), anyString());
        assertThat(logRepository.count()).isZero();
    }

    @Test
    void partlyPaid_remindsForTheBalance() {
        Renter amit = renter("Amit", "2026-08-10", 8000);
        pay(amit, SEP, 5000);

        reminderService.sendReminders(OCT_15);

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(smsService).sendSms(anyString(), message.capture());
        assertThat(message.getValue()).contains("Rs. 3,000");
    }

    // Case 4
    @Test
    void secondRunSameMonth_isSkipped_nextMonthSendsAgain() {
        Renter amit = renter("Amit", "2026-08-10", 8000);

        RentSmsReminderRunResultDto first = reminderService.sendReminders(OCT_15);
        RentSmsReminderRunResultDto second = reminderService.sendReminders(OCT_15.plusDays(1));

        assertThat(statusOf(first, amit)).isEqualTo(RentSmsReminderResultDto.SENT);
        assertThat(statusOf(second, amit)).isEqualTo(RentSmsReminderResultDto.ALREADY_SENT);
        assertThat(second.getAlreadySent()).isEqualTo(1);
        // The single-renter endpoint can't resend it either
        assertThat(reminderService.sendReminder(amit.getRenterId(), OCT_15).getStatus())
                .isEqualTo(RentSmsReminderResultDto.ALREADY_SENT);
        verify(smsService, times(1)).sendSms(anyString(), anyString());

        // November: October rent is now due and unpaid, so a new reminder goes out
        RentSmsReminderRunResultDto november = reminderService.sendReminders(LocalDate.of(2026, 11, 10));
        assertThat(statusOf(november, amit)).isEqualTo(RentSmsReminderResultDto.SENT);
        assertThat(november.getRentPeriod()).isEqualTo("October 2026");
        verify(smsService, times(2)).sendSms(anyString(), anyString());
        assertThat(logRepository.count()).isEqualTo(2);
    }

    // Case 5
    @Test
    void joinedOn31st_dueOnLastDayOfFebruary() {
        Renter priya = renter("Priya", "2026-01-31", 10000);

        // January rent is due on 28 Feb 2026 (no 31 Feb)
        RentSmsReminderRunResultDto feb27 = reminderService.sendReminders(LocalDate.of(2026, 2, 27));
        assertThat(statusOf(feb27, priya)).isEqualTo(RentSmsReminderResultDto.NOT_DUE_YET);
        assertThat(feb27.getResults().get(0).getDueDate()).isEqualTo(LocalDate.of(2026, 2, 28));

        RentSmsReminderRunResultDto feb28 = reminderService.sendReminders(LocalDate.of(2026, 2, 28));
        assertThat(statusOf(feb28, priya)).isEqualTo(RentSmsReminderResultDto.SENT);

        // February rent is due on 31 March, not 28 March
        RentSmsReminderRunResultDto mar30 = reminderService.sendReminders(LocalDate.of(2026, 3, 30));
        assertThat(statusOf(mar30, priya)).isEqualTo(RentSmsReminderResultDto.NOT_DUE_YET);
        assertThat(mar30.getResults().get(0).getDueDate()).isEqualTo(LocalDate.of(2026, 3, 31));
    }

    // Case 6
    @Test
    void missingOrInvalidMobile_noSms_othersContinue() {
        Renter noMobile = renter("No Mobile", "2026-08-10", 8000);
        noMobile.setMobileNumber("  ");
        renterRepository.save(noMobile);
        Renter badMobile = renter("Bad Mobile", "2026-08-10", 8000);
        badMobile.setMobileNumber("12345");
        renterRepository.save(badMobile);
        Renter amit = renter("Amit", "2026-08-10", 8000);

        RentSmsReminderRunResultDto run = reminderService.sendReminders(OCT_15);

        assertThat(statusOf(run, noMobile)).isEqualTo(RentSmsReminderResultDto.NO_MOBILE);
        assertThat(statusOf(run, badMobile)).isEqualTo(RentSmsReminderResultDto.INVALID_MOBILE);
        assertThat(statusOf(run, amit)).isEqualTo(RentSmsReminderResultDto.SENT);
        verify(smsService, times(1)).sendSms(anyString(), anyString());
    }

    @Test
    void mobileWithCountryCodeAndSpaces_isNormalised() {
        Renter amit = renter("Amit", "2026-08-10", 8000);
        amit.setMobileNumber("+91 98765-43210");
        renterRepository.save(amit);

        reminderService.sendReminders(OCT_15);

        verify(smsService).sendSms(eq("9876543210"), anyString());
    }

    // Case 7
    @Test
    void providerFailure_isRecorded_othersContinue_andRetriedUpToTheLimit() {
        Renter failing = renter("Failing", "2026-08-10", 8000);
        Renter throwing = renter("Throwing", "2026-08-10", 8000);
        Renter amit = renter("Amit", "2026-08-10", 8000);
        when(smsService.sendSms(eq(failing.getMobileNumber()), anyString()))
                .thenReturn(SmsResponse.failed("Twilio API error 500"));
        when(smsService.sendSms(eq(throwing.getMobileNumber()), anyString()))
                .thenThrow(new IllegalStateException("connection reset"));

        RentSmsReminderRunResultDto run = reminderService.sendReminders(OCT_15);

        assertThat(statusOf(run, failing)).isEqualTo(RentSmsReminderResultDto.FAILED);
        assertThat(statusOf(run, throwing)).isEqualTo(RentSmsReminderResultDto.FAILED);
        assertThat(statusOf(run, amit)).isEqualTo(RentSmsReminderResultDto.SENT);
        assertThat(run.getFailed()).isEqualTo(2);

        RentReminderLog entry = logRepository.findByNotificationKey("RENTER_RENT_SMS:2026-09:" + failing.getRenterId()).orElseThrow();
        assertThat(entry.getStatus()).isEqualTo(RentReminderLog.STATUS_FAILED);
        assertThat(entry.getErrorMessage()).isEqualTo("Twilio API error 500");

        // Retried on later runs until sms.max-attempts (3) is reached; the sent one is not resent
        assertThat(statusOf(reminderService.sendReminders(OCT_15.plusDays(1)), failing)).isEqualTo(RentSmsReminderResultDto.FAILED);
        assertThat(statusOf(reminderService.sendReminders(OCT_15.plusDays(2)), failing)).isEqualTo(RentSmsReminderResultDto.FAILED);
        RentSmsReminderRunResultDto fourth = reminderService.sendReminders(OCT_15.plusDays(3));
        assertThat(statusOf(fourth, failing)).isEqualTo(RentSmsReminderResultDto.RETRY_LIMIT_REACHED);
        assertThat(statusOf(fourth, amit)).isEqualTo(RentSmsReminderResultDto.ALREADY_SENT);
        verify(smsService, times(3)).sendSms(eq(failing.getMobileNumber()), anyString());
        verify(smsService, times(1)).sendSms(eq(amit.getMobileNumber()), anyString());
    }

    @Test
    void failedSendSucceedsOnRetry() {
        Renter amit = renter("Amit", "2026-08-10", 8000);
        when(smsService.sendSms(anyString(), anyString()))
                .thenReturn(SmsResponse.failed("timeout"))
                .thenReturn(SmsResponse.sent("msg-2"));

        assertThat(statusOf(reminderService.sendReminders(OCT_15), amit)).isEqualTo(RentSmsReminderResultDto.FAILED);
        assertThat(statusOf(reminderService.sendReminders(OCT_15.plusDays(1)), amit)).isEqualTo(RentSmsReminderResultDto.SENT);
        assertThat(statusOf(reminderService.sendReminders(OCT_15.plusDays(2)), amit)).isEqualTo(RentSmsReminderResultDto.ALREADY_SENT);

        RentReminderLog entry = logRepository.findByNotificationKey("RENTER_RENT_SMS:2026-09:" + amit.getRenterId()).orElseThrow();
        assertThat(entry.getAttempts()).isEqualTo(2);
        assertThat(entry.getStatus()).isEqualTo(RentReminderLog.STATUS_SENT);
    }

    // Case 8
    @Test
    void inactiveRenter_noSms() {
        Renter left = renter("Left", "2026-08-10", 8000);
        left.setStatus("LEFT");
        renterRepository.save(left);

        RentSmsReminderRunResultDto run = reminderService.sendReminders(OCT_15);

        assertThat(run.getActiveRenters()).isZero();
        assertThat(run.getResults()).isEmpty();
        assertThat(reminderService.sendReminder(left.getRenterId(), OCT_15).getStatus())
                .isEqualTo(RentSmsReminderResultDto.INACTIVE);
        verify(smsService, never()).sendSms(anyString(), anyString());
    }

    @Test
    void missingJoiningDate_skipped() {
        Renter renter = renter("No Date", "2026-08-10", 8000);
        renter.setJoiningDate(null);
        renterRepository.save(renter);

        assertThat(statusOf(reminderService.sendReminders(OCT_15), renter))
                .isEqualTo(RentSmsReminderResultDto.NO_JOINING_DATE);
        verify(smsService, never()).sendSms(anyString(), anyString());
    }

    @Test
    void disabled_sendsNothing() {
        renter("Amit", "2026-08-10", 8000);
        ReflectionTestUtils.setField(reminderService, "smsEnabled", false);

        RentSmsReminderRunResultDto run = reminderService.sendReminders(OCT_15);

        assertThat(run.getStatus()).isEqualTo(RentSmsReminderResultDto.DISABLED);
        verify(smsService, never()).sendSms(anyString(), anyString());
        assertThat(logRepository.count()).isZero();
    }

    // ── Helpers ────────────────────────────────────────────────────

    private Renter renter(String name, String joiningDate, int monthlyRent) {
        return renterRepository.save(Renter.builder()
                .renterName(name)
                .mobileNumber("98765" + String.format("%05d", nextMobile++))
                .joiningDate(LocalDate.parse(joiningDate))
                .monthlyRent(BigDecimal.valueOf(monthlyRent))
                .status("Active")
                .build());
    }

    private void pay(Renter renter, YearMonth rentMonth, int amount) {
        paymentRepository.save(RentPayment.builder()
                .renter(renter)
                .rentMonth(rentMonth.getMonthValue())
                .rentYear(rentMonth.getYear())
                .monthlyRent(renter.getMonthlyRent())
                .amountPaid(BigDecimal.valueOf(amount))
                .paymentDate(rentMonth.plusMonths(1).atDay(5))
                .build());
    }

    private static String statusOf(RentSmsReminderRunResultDto run, Renter renter) {
        return run.getResults().stream()
                .filter(r -> r.getRenterId().equals(renter.getRenterId()))
                .findFirst()
                .orElseThrow()
                .getStatus();
    }
}
