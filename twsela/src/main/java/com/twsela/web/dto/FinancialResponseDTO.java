package com.twsela.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class FinancialResponseDTO {
    private Long id;
    private String userName;
    private String payoutType;
    private String status;
    private BigDecimal amount;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Instant paidAt;

    public FinancialResponseDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getPayoutType() { return payoutType; }
    public void setPayoutType(String payoutType) { this.payoutType = payoutType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getPeriodStart() { return periodStart; }
    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
}
