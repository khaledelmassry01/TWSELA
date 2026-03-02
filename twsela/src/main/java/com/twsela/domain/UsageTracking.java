package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Tracks monthly usage metrics for a merchant (shipments created, API calls, webhook events).
 */
@Entity
@Table(name = "usage_tracking", indexes = {
    @Index(name = "idx_usage_merchant_period", columnList = "merchant_id, period", unique = true)
})
public class UsageTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    /** Period in YYYY-MM format */
    @Column(name = "period", nullable = false, length = 7)
    private String period;

    @Column(name = "shipments_created", nullable = false)
    private int shipmentsCreated = 0;

    @Column(name = "api_calls", nullable = false)
    private int apiCalls = 0;

    @Column(name = "webhook_events", nullable = false)
    private int webhookEvents = 0;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated = Instant.now();

    // Constructors
    public UsageTracking() {}

    public UsageTracking(Long merchantId, String period) {
        this.merchantId = merchantId;
        this.period = period;
        this.lastUpdated = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public int getShipmentsCreated() { return shipmentsCreated; }
    public void setShipmentsCreated(int shipmentsCreated) { this.shipmentsCreated = shipmentsCreated; }

    public int getApiCalls() { return apiCalls; }
    public void setApiCalls(int apiCalls) { this.apiCalls = apiCalls; }

    public int getWebhookEvents() { return webhookEvents; }
    public void setWebhookEvents(int webhookEvents) { this.webhookEvents = webhookEvents; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
