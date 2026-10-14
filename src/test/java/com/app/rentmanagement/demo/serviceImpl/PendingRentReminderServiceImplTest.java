package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.RentMonthStatusDto;
import com.app.rentmanagement.demo.dto.RenterPendingSummaryDto;
import com.app.rentmanagement.demo.dto.ReminderRunResultDto;
import com.app.rentmanagement.demo.entity.RentReminderLog;
import com.app.rentmanagement.demo.repository.RentReminderLogRepository;
import com.app.rentmanagement.demo.service.RentCalculationService;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DataJpaTest
@Import({PendingRentReminderServiceImpl.class, RentReminderLogService.class})
@TestPropertySource(properties = {
        "rent.reminder.owner-email=owner@example.com",
        "rent.reminder.from=rent@example.com"
})
@Transactional(propagation = Propagation.NOT_SUPPORTED) // let each claim commit like in production
class PendingRentReminderServiceImplTest {

    private static final LocalDate OCT_25 = LocalDate.of(2026, 10, 25);

    @Autowired
    private PendingRentReminderServiceImpl reminderService;

    @Autowired
    private RentReminderLogRepository logRepository;

    @MockitoBean
    private RentCalculationService rentCalculationService;

    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        logRepository.deleteAll();
        when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage(Session.getInstance(new Properties())));
        when(rentCalculationService.getLatestDueRentMonth(any())).thenAnswer(inv ->
                YearMonth.from(inv.<LocalDate>getArgument(0)).minusMonths(1));
        // Rahul: Sep pending. Amit: Aug + Sep pending. John has nothing pending, so the
        // calculation service does not return him.
        when(rentCalculationService.getPendingRenters(any())).thenReturn(List.of(
                pending(1L, "Rahul", "rahul@example.com", 10000, 9),
                pending(2L, "Amit", "amit@example.com", 8000, 8, 9)));
    }

    @Test
    void sendsOneConsolidatedEmailToOwnerOnly() throws Exception {
        ReminderRunResultDto result = reminderService.sendMonthlyReminders(OCT_25);

        assertThat(result.getStatus()).isEqualTo("SENT");
        assertThat(result.getTotalPending()).isEqualByComparingTo("26000");

        ArgumentCaptor<MimeMessage> sent = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(sent.capture());

        MimeMessage message = sent.getValue();
        assertThat(message.getAllRecipients()).hasSize(1);
        assertThat(message.getRecipients(Message.RecipientType.TO)[0].toString()).isEqualTo("owner@example.com");
        assertThat(message.getSubject()).isEqualTo("Pending Rent Report - October 2026");
    }

    @Test
    void emailBodyListsPendingRentersMonthWise() {
        String html = reminderService.generatePendingRentEmail(OCT_25);

        assertThat(html).contains("Dear Owner", "25 October 2026",
                "Rahul", "September 2026", "₹10,000",
                "Amit", "August, September 2026", "₹16,000",
                "Total Pending Rent", "₹26,000")
                .doesNotContain("John", "rahul@example.com", "amit@example.com");
    }

    @Test
    void sentOncePerMonthAcrossRepeatedRunsAndRestarts() {
        assertThat(reminderService.sendMonthlyReminders(OCT_25).getStatus()).isEqualTo("SENT");
        // Same day again (scheduler re-run / restart / manual trigger), and the next day
        assertThat(reminderService.sendMonthlyReminders(OCT_25).getStatus()).isEqualTo("ALREADY_SENT");
        assertThat(reminderService.sendMonthlyReminders(OCT_25.plusDays(1)).getStatus()).isEqualTo("ALREADY_SENT");
        verify(mailSender, times(1)).send(any(MimeMessage.class));

        // November is a new report
        ReminderRunResultDto november = reminderService.sendMonthlyReminders(LocalDate.of(2026, 11, 25));
        assertThat(november.getStatus()).isEqualTo("SENT");
        verify(mailSender, times(2)).send(any(MimeMessage.class));

        assertThat(logRepository.findAll())
                .extracting(RentReminderLog::getNotificationKey, RentReminderLog::getStatus)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("OWNER_PENDING_RENT:2026-10", "SENT"),
                        org.assertj.core.groups.Tuple.tuple("OWNER_PENDING_RENT:2026-11", "SENT"));
    }

    @Test
    void noEmailWhenNothingIsPending() {
        when(rentCalculationService.getPendingRenters(any())).thenReturn(List.of());

        assertThat(reminderService.sendMonthlyReminders(OCT_25).getStatus()).isEqualTo("SKIPPED_NO_PENDING");
        assertThat(reminderService.sendMonthlyReminders(OCT_25.plusDays(1)).getStatus()).isEqualTo("ALREADY_SENT");
        verify(mailSender, never()).send(any(MimeMessage.class));
        assertThat(logRepository.findByNotificationKey("OWNER_PENDING_RENT:2026-10").orElseThrow().getStatus())
                .isEqualTo("SKIPPED_NO_PENDING");
    }

    @Test
    void failedSendIsRetriedOnNextRun() {
        doThrow(new MailSendException("SMTP down")).doNothing().when(mailSender).send(any(MimeMessage.class));

        assertThat(reminderService.sendMonthlyReminders(OCT_25).getStatus()).isEqualTo("FAILED");
        assertThat(reminderService.sendMonthlyReminders(OCT_25).getStatus()).isEqualTo("SENT");
        assertThat(reminderService.sendMonthlyReminders(OCT_25).getStatus()).isEqualTo("ALREADY_SENT");

        RentReminderLog entry = logRepository.findByNotificationKey("OWNER_PENDING_RENT:2026-10").orElseThrow();
        assertThat(entry.getAttempts()).isEqualTo(2);
        assertThat(entry.getSentAt()).isNotNull();
    }

    @Test
    void concurrentRunsSendOnlyOnce() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(4);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<ReminderRunResultDto>> runs = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            runs.add(pool.submit(() -> {
                start.await();
                return reminderService.sendMonthlyReminders(OCT_25);
            }));
        }
        start.countDown();

        long sent = 0;
        for (Future<ReminderRunResultDto> run : runs) {
            if ("SENT".equals(run.get(30, TimeUnit.SECONDS).getStatus())) {
                sent++;
            }
        }
        pool.shutdown();

        assertThat(sent).isEqualTo(1);
        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    private static RenterPendingSummaryDto pending(Long id, String name, String email, int rent, int... months) {
        List<RentMonthStatusDto> pendingMonths = new ArrayList<>();
        for (int m : months) {
            YearMonth ym = YearMonth.of(2026, m);
            pendingMonths.add(RentMonthStatusDto.builder()
                    .renterId(id).renterName(name)
                    .rentMonth(m).rentYear(2026)
                    .rentPeriod(ym.getMonth().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.ENGLISH) + " 2026")
                    .rentAmount(new BigDecimal(rent)).paidAmount(BigDecimal.ZERO)
                    .pendingAmount(new BigDecimal(rent)).creditAmount(BigDecimal.ZERO)
                    .status("Pending").build());
        }
        return RenterPendingSummaryDto.builder()
                .renterId(id).renterName(name).email(email).flatNo("F" + id)
                .monthlyRent(new BigDecimal(rent))
                .pendingMonths(pendingMonths)
                .totalPending(new BigDecimal(rent).multiply(BigDecimal.valueOf(months.length)))
                .totalCredit(BigDecimal.ZERO)
                .build();
    }
}
