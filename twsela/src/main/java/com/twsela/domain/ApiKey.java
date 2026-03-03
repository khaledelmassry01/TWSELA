package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * API Key entity — allows merchants to access the public API (v2).
 * The secret is stored as a BCrypt hash; the plain text is returned only once during creation.
 */
@Entity
@Table(name = "api_keys")
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private User merchant;

    @Column(name = "key_value", nullable = false, unique = true, length = 50)
    private String keyValue;

    @Column(name = "secret_hash", nullable = false)
    private String secretHash;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "scopes", nullable = false, length = 500)
    private String scopes;

    @Column(name = "rate_limit", nullable = false)
    private int rateLimit = 100;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    @Column(name = "request_count", nullable = false)
    private long requestCount = 0;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "tenant_id")
    private Long tenantId;

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getMerchant() { return merchant; }
    public void setMerchant(User merchant) { this.merchant = merchant; }

    public String getKeyValue() { return keyValue; }
    public void setKeyValue(String keyValue) { this.keyValue = keyValue; }

    public String getSecretHash() { return secretHash; }
    public void setSecretHash(String secretHash) { this.secretHash = secretHash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getScopes() { return scopes; }
    public void setScopes(String scopes) { this.scopes = scopes; }

    public int getRateLimit() { return rateLimit; }
    public void setRateLimit(int rateLimit) { this.rateLimit = rateLimit; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(Instant lastUsedAt) { this.lastUsedAt = lastUsedAt; }

    public long getRequestCount() { return requestCount; }
    public void setRequestCount(long requestCount) { this.requestCount = requestCount; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
