package com.app.rentmanagement.demo.serviceImpl;

import com.app.rentmanagement.demo.entity.RentReminderLog;
import com.app.rentmanagement.demo.repository.RentReminderLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Claims a monthly report in the email log before it is sent, so each report goes out once.
 * Each method commits in its own transaction so a claim is visible to other runs immediately.
 */
@Service
@RequiredArgsConstructor
public class RentReminderLogService {

    private final RentReminderLogRepository repository;

    public static String notificationKey(String type, YearMonth month) {
        return type + ":" + month;
    }

    /**
     * Returns true when the caller now owns the report and should send it.
     * Returns false when it was already sent, skipped, or is being sent by another run.
     * A FAILED report can be claimed again so it is retried on the next run.
     *
     * A concurrent claim of the same key fails with a DataAccessException
     * (unique key / optimistic lock); callers treat that as "not claimed".
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean claim(String type, YearMonth month, String recipient) {
        String key = notificationKey(type, month);

        RentReminderLog existing = repository.findByNotificationKey(key).orElse(null);
        if (existing != null) {
            if (!RentReminderLog.STATUS_FAILED.equals(existing.getStatus())) {
                return false;
            }
            existing.setStatus(RentReminderLog.STATUS_SENDING);
            existing.setRecipient(recipient);
            existing.setAttempts(existing.getAttempts() + 1);
            existing.setErrorMessage(null);
            repository.saveAndFlush(existing);
            return true;
        }

        repository.saveAndFlush(RentReminderLog.builder()
                .notificationKey(key)
                .notificationMonth(month.toString())
                .notificationType(type)
                .recipient(recipient)
                .status(RentReminderLog.STATUS_SENDING)
                .attempts(1)
                .createdAt(LocalDateTime.now())
                .build());
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSent(String type, YearMonth month) {
        updateStatus(type, month, RentReminderLog.STATUS_SENT, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSkippedNoPending(String type, YearMonth month) {
        updateStatus(type, month, RentReminderLog.STATUS_SKIPPED_NO_PENDING, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(String type, YearMonth month, String errorMessage) {
        updateStatus(type, month, RentReminderLog.STATUS_FAILED,
                errorMessage != null && errorMessage.length() > 1000 ? errorMessage.substring(0, 1000) : errorMessage);
    }

    private void updateStatus(String type, YearMonth month, String status, String errorMessage) {
        repository.findByNotificationKey(notificationKey(type, month)).ifPresent(entry -> {
            entry.setStatus(status);
            entry.setErrorMessage(errorMessage);
            if (RentReminderLog.STATUS_SENT.equals(status)) {
                entry.setSentAt(LocalDateTime.now());
            }
            repository.save(entry);
        });
    }
}
