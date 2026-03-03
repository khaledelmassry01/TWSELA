package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Represents a merchant's connection to an external e-commerce platform.
 */
@Entity
@Table(name = "ecommerce_connections",
       uniqueConstraints = @UniqueConstraint(columnNames = {"merchant_id", "platform"}))
public class ECommerceConnection {

    public enum ECommercePlatform {
        SHOPIFY, WOOCOMMERCE, SALLA, ZID
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private User merchant;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private ECommercePlatform platform;

    @Column(name = "store_name", length = 200)
    private String storeName;

    @Column(name = "store_url", nullable = false, length = 500)
    private String storeUrl;

    @Column(name = "access_token", nullable = false, length = 500)
    private String accessToken;

    @Column(name = "webhook_secret", length = 200)
    private String webhookSecret;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "auto_create_shipments", nullable = false)
    private boolean autoCreateShipments = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "default_zone_id")
    private Zone defaultZone;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Column(name = "sync_errors", nullable = false)
    private int syncErrors = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getMerchant() { return merchant; }
    public void setMerchant(User merchant) { this.merchant = merchant; }

    public ECommercePlatform getPlatform() { return platform; }
    public void setPlatform(ECommercePlatform platform) { this.platform = platform; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getStoreUrl() { return storeUrl; }
    public void setStoreUrl(String storeUrl) { this.storeUrl = storeUrl; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getWebhookSecret() { return webhookSecret; }
    public void setWebhookSecret(String webhookSecret) { this.webhookSecret = webhookSecret; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isAutoCreateShipments() { return autoCreateShipments; }
    public void setAutoCreateShipments(boolean autoCreateShipments) { this.autoCreateShipments = autoCreateShipments; }

    public Zone getDefaultZone() { return defaultZone; }
    public void setDefaultZone(Zone defaultZone) { this.defaultZone = defaultZone; }

    public Instant getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; }

    public int getSyncErrors() { return syncErrors; }
    public void setSyncErrors(int syncErrors) { this.syncErrors = syncErrors; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
