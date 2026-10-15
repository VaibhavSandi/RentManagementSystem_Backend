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

    // ── Per-renter notifications (one per renter per rent month) ──

    public enum ClaimResult {
        CLAIMED,
        ALREADY_SENT,
        IN_PROGRESS,
        RETRY_LIMIT_REACHED
    }

    public static String renterNotificationKey(String type, YearMonth month, Long renterId) {
        return type + ":" + month + ":" + renterId;
    }

    /**
     * Claims one renter's notification for a month. A FAILED notification is claimed again
     * until it has been attempted maxAttempts times. A row left in SENDING (e.g. the
     * application stopped mid-send) is never claimed again, so it can't be sent twice.
     *
     * A concurrent claim of the same key fails with a DataAccessException; callers treat
     * that as "not claimed".
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ClaimResult claimForRenter(String type, YearMonth month, Long renterId, String recipient, int maxAttempts) {
        String key = renterNotificationKey(type, month, renterId);

        RentReminderLog existing = repository.findByNotificationKey(key).orElse(null);
        if (existing != null) {
            if (RentReminderLog.STATUS_SENDING.equals(existing.getStatus())) {
                return ClaimResult.IN_PROGRESS;
            }
            if (!RentReminderLog.STATUS_FAILED.equals(existing.getStatus())) {
                return ClaimResult.ALREADY_SENT;
            }
            if (existing.getAttempts() >= maxAttempts) {
                return ClaimResult.RETRY_LIMIT_REACHED;
            }
            existing.setStatus(RentReminderLog.STATUS_SENDING);
            existing.setRecipient(recipient);
            existing.setAttempts(existing.getAttempts() + 1);
            existing.setErrorMessage(null);
            repository.saveAndFlush(existing);
            return ClaimResult.CLAIMED;
        }

        repository.saveAndFlush(RentReminderLog.builder()
                .notificationKey(key)
                .notificationMonth(month.toString())
                .notificationType(type)
                .renterId(renterId)
                .recipient(recipient)
                .status(RentReminderLog.STATUS_SENDING)
                .attempts(1)
                .createdAt(LocalDateTime.now())
                .build());
        return ClaimResult.CLAIMED;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markSentForRenter(String type, YearMonth month, Long renterId) {
        updateStatusByKey(renterNotificationKey(type, month, renterId), RentReminderLog.STATUS_SENT, null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailedForRenter(String type, YearMonth month, Long renterId, String errorMessage) {
        updateStatusByKey(renterNotificationKey(type, month, renterId), RentReminderLog.STATUS_FAILED, truncate(errorMessage));
    }

    // ── Monthly owner report ──

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
        updateStatus(type, month, RentReminderLog.STATUS_FAILED, truncate(errorMessage));
    }

    private void updateStatus(String type, YearMonth month, String status, String errorMessage) {
        updateStatusByKey(notificationKey(type, month), status, errorMessage);
    }

    private static String truncate(String errorMessage) {
        return errorMessage != null && errorMessage.length() > 1000 ? errorMessage.substring(0, 1000) : errorMessage;
    }

    private void updateStatusByKey(String key, String status, String errorMessage) {
        repository.findByNotificationKey(key).ifPresent(entry -> {
            entry.setStatus(status);
            entry.setErrorMessage(errorMessage);
            if (RentReminderLog.STATUS_SENT.equals(status)) {
                entry.setSentAt(LocalDateTime.now());
            }
            repository.save(entry);
        });
    }
}
