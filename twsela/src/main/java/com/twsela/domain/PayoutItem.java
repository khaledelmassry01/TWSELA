package com.twsela.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payout_items", indexes = {
    @Index(name = "FK_pi_payout", columnList = "payout_id"),
    @Index(name = "idx_pi_source", columnList = "source_type, source_id"),
    @Index(name = "idx_pi_created_at", columnList = "created_at")
})
public class PayoutItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_id", nullable = false)
    @JsonBackReference
    private Payout payout;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    @NotNull(message = "Source type is required")
    private SourceType sourceType;

    @Column(name = "source_id", nullable = false)
    @NotNull(message = "Source ID is required")
    private Long sourceId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @Column(name = "description", nullable = false, length = 255)
    @NotNull(message = "Description is required")
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
    private Instant createdAt = Instant.now();

    public enum SourceType {
        ADJUSTMENT, BONUS, EXPENSE, SHIPMENT
    }

    // Constructors
    public PayoutItem() {}

    public PayoutItem(Payout payout, SourceType sourceType, Long sourceId, BigDecimal amount, String description) {
        this.payout = payout;
        this.sourceType = sourceType;
        this.sourceId = sourceId;
        this.amount = amount;
        this.description = description;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Payout getPayout() { return payout; }
    public void setPayout(Payout payout) { this.payout = payout; }
    public SourceType getSourceType() { return sourceType; }
    public void setSourceType(SourceType sourceType) { this.sourceType = sourceType; }
    public Long getSourceId() { return sourceId; }
    public void setSourceId(Long sourceId) { this.sourceId = sourceId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    // Additional method for testing
    public void setItemType(String itemType) {
        // This method is for testing purposes only
        // In production, use setSourceType with proper enum values
        try {
            this.sourceType = SourceType.valueOf(itemType.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Default to SHIPMENT if invalid type
            this.sourceType = SourceType.SHIPMENT;
        }
    }

    @Override
    public String toString() {
        return "PayoutItem{" +
                "id=" + id +
                ", sourceType=" + sourceType +
                ", sourceId=" + sourceId +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                '}';
    }
}

