package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a single transaction (credit or debit) on a wallet.
 */
@Entity
@Table(name = "wallet_transactions", indexes = {
    @Index(name = "idx_wt_wallet", columnList = "wallet_id"),
    @Index(name = "idx_wt_created_at", columnList = "created_at"),
    @Index(name = "idx_wt_reference", columnList = "reference_id")
})
public class WalletTransaction {

    public enum TransactionType {
        CREDIT, DEBIT
    }

    public enum TransactionReason {
        COD_COLLECTED,
        DELIVERY_FEE,
        COMMISSION,
        WITHDRAWAL,
        SETTLEMENT,
        RETURN_FEE,
        ADJUSTMENT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private TransactionType type;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    private TransactionReason reason;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "balance_before", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    // Constructors
    public WalletTransaction() {}

    public WalletTransaction(Wallet wallet, TransactionType type, BigDecimal amount,
                             TransactionReason reason, Long referenceId, String description) {
        this.wallet = wallet;
        this.type = type;
        this.amount = amount;
        this.reason = reason;
        this.referenceId = referenceId;
        this.description = description;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionReason getReason() { return reason; }
    public void setReason(TransactionReason reason) { this.reason = reason; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
