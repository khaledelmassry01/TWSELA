package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * Wallet entity representing a financial account for a user (Merchant, Courier, or Company).
 */
@Entity
@Table(name = "wallets", indexes = {
    @Index(name = "idx_wallet_user", columnList = "user_id", unique = true),
    @Index(name = "idx_wallet_type", columnList = "wallet_type")
})
public class Wallet {

    public enum WalletType {
        MERCHANT, COURIER, COMPANY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "wallet_type", nullable = false, length = 20)
    private WalletType walletType;

    @Column(name = "balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EGP";

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Column(name = "tenant_id")
    private Long tenantId;

    // Constructors
    public Wallet() {}

    public Wallet(User user, WalletType walletType) {
        this.user = user;
        this.walletType = walletType;
        this.balance = BigDecimal.ZERO;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public WalletType getWalletType() { return walletType; }
    public void setWalletType(WalletType walletType) { this.walletType = walletType; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Wallet)) return false;
        Wallet wallet = (Wallet) o;
        return id != null && id.equals(wallet.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
