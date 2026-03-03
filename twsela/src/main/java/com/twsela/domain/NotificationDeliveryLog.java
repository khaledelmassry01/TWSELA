package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Log of every notification delivery attempt across all channels.
 * Tracks delivery status, retry info, and external provider IDs.
 */
@Entity
@Table(name = "notification_delivery_log", indexes = {
    @Index(name = "idx_ndl_notification", columnList = "notification_id"),
    @Index(name = "idx_ndl_status_retry", columnList = "status, next_retry_at"),
    @Index(name = "idx_ndl_channel_status", columnList = "channel, status"),
    @Index(name = "idx_ndl_sent_at", columnList = "sent_at")
})
public class NotificationDeliveryLog {

    public enum DeliveryStatus {
        PENDING, SENT, DELIVERED, FAILED, BOUNCED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "notification_id")
    private Long notificationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(name = "recipient", nullable = false, length = 255)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 15)
    private DeliveryStatus status = DeliveryStatus.PENDING;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount = 0;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Constructors
    public NotificationDeliveryLog() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNotificationId() { return notificationId; }
    public void setNotificationId(Long notificationId) { this.notificationId = notificationId; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }

    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public Instant getNextRetryAt() { return nextRetryAt; }
    public void setNextRetryAt(Instant nextRetryAt) { this.nextRetryAt = nextRetryAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
