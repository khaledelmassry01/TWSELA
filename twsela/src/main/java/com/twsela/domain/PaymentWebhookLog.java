package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * سجل webhook وارد من بوابة دفع — يُستخدم لتتبع ومعالجة والتحقق من callbacks.
 */
@Entity
@Table(name = "payment_webhook_logs", indexes = {
        @Index(name = "idx_pwl_provider", columnList = "provider"),
        @Index(name = "idx_pwl_event", columnList = "event_type"),
        @Index(name = "idx_pwl_processed", columnList = "processed"),
        @Index(name = "idx_pwl_created", columnList = "created_at")
})
public class PaymentWebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider", nullable = false, length = 20)
    private String provider;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "signature", length = 500)
    private String signature;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "error", columnDefinition = "TEXT")
    private String error;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Constructors ──
    public PaymentWebhookLog() {}

    // ── Getters / Setters ──
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public boolean isProcessed() { return processed; }
    public void setProcessed(boolean processed) { this.processed = processed; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ── equals / hashCode ──
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentWebhookLog that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
