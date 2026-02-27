package com.twsela.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "payouts", indexes = {
    @Index(name = "idx_user_period", columnList = "user_id, payout_period_end DESC"),
    @Index(name = "idx_payout_status", columnList = "status_id")
})
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "payout_type", nullable = false)
    private PayoutType payoutType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "status_id", nullable = false)
    private PayoutStatus status;

    @Column(name = "payout_period_start", nullable = false)
    @NotNull(message = "Payout period start is required")
    private LocalDate payoutPeriodStart;

    @Column(name = "payout_period_end", nullable = false)
    @NotNull(message = "Payout period end is required")
    private LocalDate payoutPeriodEnd;

    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "Net amount is required")
    @DecimalMin(value = "0.0", message = "Net amount must be non-negative")
    private BigDecimal netAmount;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "payout", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<PayoutItem> payoutItems = new java.util.HashSet<>();

    @OneToMany(mappedBy = "payout", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Shipment> shipments = new java.util.HashSet<>();

    public enum PayoutType {
        COURIER_SETTLEMENT, MERCHANT_PAYOUT, WAREHOUSE_SETTLEMENT
    }

    // Constructors
    public Payout() {}

    public Payout(User user, PayoutType payoutType, PayoutStatus status, 
                  LocalDate payoutPeriodStart, LocalDate payoutPeriodEnd, BigDecimal netAmount) {
        this.user = user;
        this.payoutType = payoutType;
        this.status = status;
        this.payoutPeriodStart = payoutPeriodStart;
        this.payoutPeriodEnd = payoutPeriodEnd;
        this.netAmount = netAmount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public PayoutType getPayoutType() { return payoutType; }
    public void setPayoutType(PayoutType payoutType) { this.payoutType = payoutType; }
    public PayoutStatus getStatus() { return status; }
    public void setStatus(PayoutStatus status) { this.status = status; }
    public LocalDate getPayoutPeriodStart() { return payoutPeriodStart; }
    public void setPayoutPeriodStart(LocalDate payoutPeriodStart) { this.payoutPeriodStart = payoutPeriodStart; }
    public LocalDate getPayoutPeriodEnd() { return payoutPeriodEnd; }
    public void setPayoutPeriodEnd(LocalDate payoutPeriodEnd) { this.payoutPeriodEnd = payoutPeriodEnd; }
    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Set<PayoutItem> getPayoutItems() { return payoutItems; }
    public void setPayoutItems(Set<PayoutItem> payoutItems) { this.payoutItems = payoutItems; }
    public Set<Shipment> getShipments() { return shipments; }
    public void setShipments(Set<Shipment> shipments) { this.shipments = shipments; }

    @Override
    public String toString() {
        return "Payout{" +
                "id=" + id +
                ", payoutType=" + payoutType +
                ", netAmount=" + netAmount +
                ", user=" + (user != null ? user.getName() : "null") +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Payout)) return false;
        Payout that = (Payout) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}