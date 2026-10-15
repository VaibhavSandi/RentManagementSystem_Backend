package com.app.rentmanagement.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * One row per notification: the owner's monthly report ("OWNER_PENDING_RENT:2026-10") or one
 * renter's rent SMS for one rent month ("RENTER_RENT_SMS:2026-09:12"). The unique
 * notification_key stops the same notification from being sent twice across restarts,
 * repeated or manual runs, and multiple application instances.
 */
@Entity
@Table(name = "rent_payment_email_log",
        uniqueConstraints = @UniqueConstraint(name = "uq_rent_payment_email_log_key", columnNames = "notification_key"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentReminderLog {

    public static final String TYPE_OWNER_PENDING_RENT = "OWNER_PENDING_RENT";
    public static final String TYPE_RENTER_RENT_SMS = "RENTER_RENT_SMS";

    public static final String STATUS_SENDING = "SENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";
    // No pending rent that month, so no email was sent; the month is closed
    public static final String STATUS_SKIPPED_NO_PENDING = "SKIPPED_NO_PENDING";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_key", nullable = false, length = 100)
    private String notificationKey;

    // yyyy-MM of the month the reminder belongs to (the rent month for renter SMS)
    @Column(name = "notification_month", nullable = false, length = 7)
    private String notificationMonth;

    @Column(name = "notification_type", nullable = false, length = 30)
    private String notificationType;

    @Column(name = "renter_id")
    private Long renterId;

    @Column(name = "recipient", length = 500)
    private String recipient;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Version
    private Long version;
}
