package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * إعدادات المستأجر — تخزين key/value مع تصنيف وتشفير.
 */
@Entity
@Table(name = "tenant_configurations", indexes = {
        @Index(name = "idx_tenant_config_tenant_key", columnList = "tenant_id, config_key", unique = true),
        @Index(name = "idx_tenant_config_category", columnList = "category")
})
public class TenantConfiguration {

    public enum ConfigCategory {
        GENERAL, SHIPMENT, PAYMENT, NOTIFICATION, SMS, SECURITY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConfigCategory category = ConfigCategory.GENERAL;

    @Column(nullable = false)
    private boolean encrypted = false;

    @Column(length = 500)
    private String description;

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

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tenant getTenant() { return tenant; }
    public void setTenant(Tenant tenant) { this.tenant = tenant; }

    public String getConfigKey() { return configKey; }
    public void setConfigKey(String configKey) { this.configKey = configKey; }

    public String getConfigValue() { return configValue; }
    public void setConfigValue(String configValue) { this.configValue = configValue; }

    public ConfigCategory getCategory() { return category; }
    public void setCategory(ConfigCategory category) { this.category = category; }

    public boolean isEncrypted() { return encrypted; }
    public void setEncrypted(boolean encrypted) { this.encrypted = encrypted; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TenantConfiguration that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
