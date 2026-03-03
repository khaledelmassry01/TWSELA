package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * مستأجر — شركة شحن تستخدم المنصة.
 */
@Entity
@Table(name = "tenants", indexes = {
        @Index(name = "idx_tenant_slug", columnList = "slug", unique = true),
        @Index(name = "idx_tenant_domain", columnList = "domain"),
        @Index(name = "idx_tenant_status", columnList = "status"),
        @Index(name = "idx_tenant_plan", columnList = "plan")
})
public class Tenant {

    public enum TenantStatus {
        ACTIVE, SUSPENDED, TRIAL, CANCELLED
    }

    public enum TenantPlan {
        FREE, BASIC, PRO, ENTERPRISE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false, unique = true, length = 36)
    private String tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    @Column(length = 255)
    private String domain;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantStatus status = TenantStatus.TRIAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TenantPlan plan = TenantPlan.FREE;

    @Column(name = "contact_name", length = 100)
    private String contactName;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Column(columnDefinition = "TEXT")
    private String settings;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
        if (tenantId == null) tenantId = UUID.randomUUID().toString();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }

    public TenantStatus getStatus() { return status; }
    public void setStatus(TenantStatus status) { this.status = status; }

    public TenantPlan getPlan() { return plan; }
    public void setPlan(TenantPlan plan) { this.plan = plan; }

    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getSettings() { return settings; }
    public void setSettings(String settings) { this.settings = settings; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tenant that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
