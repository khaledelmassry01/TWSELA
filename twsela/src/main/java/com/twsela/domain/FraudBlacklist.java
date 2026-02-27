package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "fraud_blacklist", indexes = {
    @Index(name = "idx_fraud_entity", columnList = "entity_type, entity_value"),
    @Index(name = "idx_fraud_active", columnList = "is_active")
})
public class FraudBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_value", nullable = false, length = 255)
    private String entityValue;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Constructors
    public FraudBlacklist() {}

    public FraudBlacklist(String entityType, String entityValue, String reason) {
        this.entityType = entityType;
        this.entityValue = entityValue;
        this.reason = reason;
        this.createdAt = Instant.now();
        this.isActive = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public String getEntityValue() { return entityValue; }
    public void setEntityValue(String entityValue) { this.entityValue = entityValue; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @Override
    public String toString() {
        return "FraudBlacklist{" +
                "id=" + id +
                ", entityType='" + entityType + '\'' +
                ", entityValue='" + entityValue + '\'' +
                ", reason='" + reason + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
