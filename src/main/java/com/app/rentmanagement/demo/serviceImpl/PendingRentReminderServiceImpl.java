package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.dto.RentMonthStatusDto;
import com.app.rentmanagement.demo.dto.RenterPendingSummaryDto;
import com.app.rentmanagement.demo.dto.ReminderRunResultDto;
import com.app.rentmanagement.demo.entity.RentReminderLog;
import com.app.rentmanagement.demo.service.PendingRentReminderService;
import com.app.rentmanagement.demo.service.RentCalculationService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sends ONE consolidated pending rent email per month to the owner. Renters are never emailed.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PendingRentReminderServiceImpl implements PendingRentReminderService {

    private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH);

    private static final String CELL = "border:1px solid #ddd;padding:6px 10px;";
    private static final String HEAD = CELL + "background:#f2f2f2;text-align:left;";
    private static final String NUM = CELL + "text-align:right;";

    private static final String TYPE = RentReminderLog.TYPE_OWNER_PENDING_RENT;

    private final RentCalculationService rentCalculationService;
    private final RentReminderLogService reminderLogService;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    // The only recipient of the report
    @Value("${rent.reminder.owner-email:}")
    private String ownerEmail;

    @Value("${rent.reminder.from:${spring.mail.username:}}")
    private String fromAddress;

    // When false (default), no email is sent in a month with no pending rent
    @Value("${rent.reminder.send-when-no-pending:false}")
    private boolean sendWhenNoPending;

    @Override
    public String generatePendingRentEmail(LocalDate asOfDate) {
        YearMonth asOfRentMonth = rentCalculationService.getLatestDueRentMonth(asOfDate);
        return buildOwnerEmail(asOfDate, asOfRentMonth, rentCalculationService.getPendingRenters(asOfRentMonth));
    }

    @Override
    public ReminderRunResultDto sendMonthlyReminders(LocalDate today) {
        YearMonth reportMonth = YearMonth.from(today);
        YearMonth asOfRentMonth = rentCalculationService.getLatestDueRentMonth(today);
        List<RenterPendingSummaryDto> pendingRenters = rentCalculationService.getPendingRenters(asOfRentMonth);
        String recipient = ownerEmail == null ? "" : ownerEmail.trim();

        ReminderRunResultDto result = ReminderRunResultDto.builder()
                .notificationMonth(reportMonth.toString())
                .asOfRentPeriod(asOfRentMonth.format(PERIOD_FORMAT))
                .ownerEmail(recipient)
                .pendingRenterCount(pendingRenters.size())
                .totalPending(totalPending(pendingRenters))
                .build();

        if (recipient.isEmpty()) {
            log.warn("Pending rent report not sent: rent.reminder.owner-email is not set");
            result.setStatus("SKIPPED_NO_RECIPIENT");
            return result;
        }

        if (pendingRenters.isEmpty() && !sendWhenNoPending) {
            // Record the month as done so later runs this month don't send it either
            if (claim(recipient, reportMonth)) {
                reminderLogService.markSkippedNoPending(TYPE, reportMonth);
                log.info("No pending rent found for {}.", reportMonth.format(PERIOD_FORMAT));
                result.setStatus("SKIPPED_NO_PENDING");
            } else {
                result.setStatus("ALREADY_SENT");
            }
            return result;
        }

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.error("Pending rent report not sent: mail is not configured (spring.mail.*)");
            result.setStatus("SKIPPED_MAIL_NOT_CONFIGURED");
            return result;
        }

        if (!claim(recipient, reportMonth)) {
            log.info("Pending rent report for {} was already sent; skipping", reportMonth);
            result.setStatus("ALREADY_SENT");
            return result;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(recipient);
            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }
            helper.setSubject("Pending Rent Report - " + reportMonth.format(PERIOD_FORMAT));
            helper.setText(buildOwnerEmail(today, asOfRentMonth, pendingRenters), true);
            mailSender.send(message);

            reminderLogService.markSent(TYPE, reportMonth);
            log.info("Pending rent report for {} sent to {} ({} renters, total {})",
                    reportMonth, recipient, pendingRenters.size(), result.getTotalPending());
            result.setStatus("SENT");
        } catch (Exception e) {
            log.error("Failed to send pending rent report for {} to {}", reportMonth, recipient, e);
            reminderLogService.markFailed(TYPE, reportMonth, e.getMessage());
            result.setStatus("FAILED");
        }
        return result;
    }

    private boolean claim(String recipient, YearMonth reportMonth) {
        try {
            return reminderLogService.claim(TYPE, reportMonth, recipient);
        } catch (DataAccessException e) {
            // Another run or instance claimed the same month at the same moment
            return false;
        }
    }

    // ── Email body ─────────────────────────────────────────────────

    private String buildOwnerEmail(LocalDate asOfDate, YearMonth asOfRentMonth, List<RenterPendingSummaryDto> pendingRenters) {
        StringBuilder html = new StringBuilder();
        html.append("<div style=\"font-family:Arial,sans-serif;font-size:14px;color:#222;\">")
                .append("<p>Dear Owner,</p>")
                .append("<p>Please find below the list of renters with pending rent as of <strong>")
                .append(asOfDate.format(DATE_FORMAT))
                .append("</strong> (rent considered up to ")
                .append(asOfRentMonth.format(PERIOD_FORMAT))
                .append(").</p>");

        if (pendingRenters.isEmpty()) {
            html.append("<p><strong>No pending rent. All renters have paid.</strong></p>");
        } else {
            html.append("<table style=\"border-collapse:collapse;\">")
                    .append("<tr><th style=\"").append(HEAD).append("\">Renter Name</th>")
                    .append("<th style=\"").append(HEAD).append("\">Flat</th>")
                    .append("<th style=\"").append(HEAD).append("\">Pending Month(s)</th>")
                    .append("<th style=\"").append(HEAD).append("\">Pending Amount</th></tr>");
            for (RenterPendingSummaryDto renter : pendingRenters) {
                html.append("<tr><td style=\"").append(CELL).append("\">").append(esc(renter.getRenterName())).append("</td>")
                        .append("<td style=\"").append(CELL).append("\">").append(esc(renter.getFlatNo())).append("</td>")
                        .append("<td style=\"").append(CELL).append("\">").append(esc(pendingMonthsLabel(renter))).append("</td>")
                        .append("<td style=\"").append(NUM).append("\">").append(inr(renter.getTotalPending())).append("</td></tr>");
            }
            html.append("<tr><td colspan=\"3\" style=\"").append(CELL).append("font-weight:bold;\">Total Pending Rent</td>")
                    .append("<td style=\"").append(NUM).append("font-weight:bold;\">").append(inr(totalPending(pendingRenters)))
                    .append("</td></tr></table>");

            html.append("<h3 style=\"margin-top:24px;\">Month-wise details</h3>");
            for (RenterPendingSummaryDto renter : pendingRenters) {
                html.append("<p style=\"margin-bottom:4px;\"><strong>").append(esc(renter.getRenterName())).append("</strong>");
                if (renter.getFlatNo() != null) {
                    html.append(" &middot; Flat ").append(esc(renter.getFlatNo()));
                }
                html.append("</p>");
                appendMonthTable(html, renter);
            }
        }

        html.append("<p>Regards,<br>Rent Management System</p></div>");
        return html.toString();
    }

    private void appendMonthTable(StringBuilder html, RenterPendingSummaryDto renter) {
        html.append("<table style=\"border-collapse:collapse;margin-bottom:16px;\">")
                .append("<tr><th style=\"").append(HEAD).append("\">Rent Month</th>")
                .append("<th style=\"").append(HEAD).append("\">Rent</th>")
                .append("<th style=\"").append(HEAD).append("\">Paid</th>")
                .append("<th style=\"").append(HEAD).append("\">Pending</th></tr>");
        for (RentMonthStatusDto month : renter.getPendingMonths()) {
            html.append("<tr><td style=\"").append(CELL).append("\">").append(esc(month.getRentPeriod())).append("</td>")
                    .append("<td style=\"").append(NUM).append("\">").append(inr(month.getRentAmount())).append("</td>")
                    .append("<td style=\"").append(NUM).append("\">").append(inr(month.getPaidAmount())).append("</td>")
                    .append("<td style=\"").append(NUM).append("\">").append(inr(month.getPendingAmount())).append("</td></tr>");
        }
        html.append("<tr><td colspan=\"3\" style=\"").append(CELL).append("font-weight:bold;\">Total Pending</td>")
                .append("<td style=\"").append(NUM).append("font-weight:bold;\">").append(inr(renter.getTotalPending()))
                .append("</td></tr>");
        if (renter.getTotalCredit() != null && renter.getTotalCredit().signum() > 0) {
            html.append("<tr><td colspan=\"3\" style=\"").append(CELL).append("\">Advance / excess paid (credit)</td>")
                    .append("<td style=\"").append(NUM).append("\">").append(inr(renter.getTotalCredit())).append("</td></tr>");
        }
        html.append("</table>");
    }

    // ── Helpers ────────────────────────────────────────────────────

    /** e.g. "August, September 2026" or "December 2025, January 2026". */
    static String pendingMonthsLabel(RenterPendingSummaryDto renter) {
        Map<Integer, List<String>> monthsByYear = new LinkedHashMap<>();
        for (RentMonthStatusDto month : renter.getPendingMonths()) {
            monthsByYear.computeIfAbsent(month.getRentYear(), y -> new java.util.ArrayList<>())
                    .add(Month.of(month.getRentMonth()).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        }
        return monthsByYear.entrySet().stream()
                .map(e -> String.join(", ", e.getValue()) + " " + e.getKey())
                .collect(Collectors.joining(", "));
    }

    private static BigDecimal totalPending(List<RenterPendingSummaryDto> renters) {
        return renters.stream().map(RenterPendingSummaryDto::getTotalPending).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static String inr(BigDecimal amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(2);
        return format.format(amount == null ? BigDecimal.ZERO : amount);
    }

    private static String esc(String value) {
        return value == null ? "" : HtmlUtils.htmlEscape(value);
    }
}
