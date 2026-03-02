package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Webhook subscription — a merchant subscribes to receive HTTP callbacks.
 */
@Entity
@Table(name = "webhook_subscriptions", indexes = {
    @Index(name = "idx_ws_merchant", columnList = "merchant_id"),
    @Index(name = "idx_ws_active", columnList = "active")
})
public class WebhookSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private User merchant;

    @Column(nullable = false, length = 500)
    private String url;

    /** HMAC-SHA256 secret for payload signing */
    @Column(nullable = false, length = 128)
    private String secret;

    /** Comma-separated event types, e.g. "SHIPMENT_CREATED,STATUS_CHANGED,DELIVERED" */
    @Column(nullable = false, length = 500)
    private String events;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    public WebhookSubscription() {}

    public WebhookSubscription(User merchant, String url, String secret, String events) {
        this.merchant = merchant;
        this.url = url;
        this.secret = secret;
        this.events = events;
    }

    // ── Getters & Setters ───────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getMerchant() { return merchant; }
    public void setMerchant(User merchant) { this.merchant = merchant; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public String getEvents() { return events; }
    public void setEvents(String events) { this.events = events; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
