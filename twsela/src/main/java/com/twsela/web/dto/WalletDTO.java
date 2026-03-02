package com.twsela.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * DTO for wallet information including recent transactions.
 */
public class WalletDTO {

    private Long id;
    private BigDecimal balance;
    private String currency;
    private String walletType;
    private Instant updatedAt;
    private List<TransactionDTO> recentTransactions;

    public WalletDTO() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getWalletType() { return walletType; }
    public void setWalletType(String walletType) { this.walletType = walletType; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public List<TransactionDTO> getRecentTransactions() { return recentTransactions; }
    public void setRecentTransactions(List<TransactionDTO> recentTransactions) { this.recentTransactions = recentTransactions; }

    /**
     * Nested DTO for a single wallet transaction.
     */
    public static class TransactionDTO {
        private Long id;
        private String type;
        private BigDecimal amount;
        private String reason;
        private BigDecimal balanceBefore;
        private BigDecimal balanceAfter;
        private String description;
        private Instant createdAt;

        public TransactionDTO() {}

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public BigDecimal getBalanceBefore() { return balanceBefore; }
        public void setBalanceBefore(BigDecimal balanceBefore) { this.balanceBefore = balanceBefore; }
        public BigDecimal getBalanceAfter() { return balanceAfter; }
        public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Instant getCreatedAt() { return createdAt; }
        public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    }
}
