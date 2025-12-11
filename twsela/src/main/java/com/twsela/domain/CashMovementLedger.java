package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "cash_movement_ledger")
public class CashMovementLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "shipment_id")
    private Long shipmentId;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "reconciled_at")
    private Instant reconciledAt;

    @Column(name = "immutable", nullable = false)
    private Boolean immutable = true;

    // Enums
    public enum TransactionType {
        COLLECTION,
        DEPOSIT_TO_WAREHOUSE,
        DEPOSIT_TO_BANK,
        WITHDRAWAL
    }

    public enum TransactionStatus {
        PENDING,
        VERIFIED,
        RECONCILED
    }

    // Constructors
    public CashMovementLedger() {}

    public CashMovementLedger(User user, TransactionType transactionType, BigDecimal amount, String description) {
        this.user = user;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.createdAt = Instant.now();
        this.status = TransactionStatus.PENDING;
        this.immutable = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public TransactionType getTransactionType() { return transactionType; }
    public void setTransactionType(TransactionType transactionType) { this.transactionType = transactionType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TransactionStatus getStatus() { return status; }
    public void setStatus(TransactionStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getReconciledAt() { return reconciledAt; }
    public void setReconciledAt(Instant reconciledAt) { this.reconciledAt = reconciledAt; }

    public Boolean getImmutable() { return immutable; }
    public void setImmutable(Boolean immutable) { this.immutable = immutable; }

    @Override
    public String toString() {
        return "CashMovementLedger{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", transactionType=" + transactionType +
                ", amount=" + amount +
                ", shipmentId=" + shipmentId +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", reconciledAt=" + reconciledAt +
                ", immutable=" + immutable +
                '}';
    }
}
