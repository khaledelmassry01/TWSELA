package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * حصص وحدود المستأجر.
 */
@Entity
@Table(name = "tenant_quotas", indexes = {
        @Index(name = "idx_tenant_quota_tenant", columnList = "tenant_id"),
        @Index(name = "idx_tenant_quota_type", columnList = "quota_type"),
        @Index(name = "idx_tenant_quota_unique", columnList = "tenant_id, quota_type", unique = true)
})
public class TenantQuota {

    public enum QuotaType {
        MAX_SHIPMENTS_MONTHLY, MAX_USERS, MAX_API_CALLS, MAX_STORAGE_MB, MAX_WEBHOOKS
    }

    public enum ResetPeriod {
        MONTHLY, DAILY, NEVER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(name = "quota_type", nullable = false, length = 30)
    private QuotaType quotaType;

    @Column(name = "max_value", nullable = false)
    private long maxValue;

    @Column(name = "current_value", nullable = false)
    private long currentValue = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "reset_period", nullable = false, length = 10)
    private ResetPeriod resetPeriod = ResetPeriod.MONTHLY;

    @Column(name = "last_reset_at")
    private Instant lastResetAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public boolean isExceeded() { return currentValue >= maxValue; }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public QuotaType getQuotaType() { return quotaType; }
    public void setQuotaType(QuotaType quotaType) { this.quotaType = quotaType; }

    public long getMaxValue() { return maxValue; }
    public void setMaxValue(long maxValue) { this.maxValue = maxValue; }

    public long getCurrentValue() { return currentValue; }
    public void setCurrentValue(long currentValue) { this.currentValue = currentValue; }

    public ResetPeriod getResetPeriod() { return resetPeriod; }
    public void setResetPeriod(ResetPeriod resetPeriod) { this.resetPeriod = resetPeriod; }

    public Instant getLastResetAt() { return lastResetAt; }
    public void setLastResetAt(Instant lastResetAt) { this.lastResetAt = lastResetAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantQuota that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
