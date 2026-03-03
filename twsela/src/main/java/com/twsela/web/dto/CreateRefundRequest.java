package com.twsela.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateRefundRequest {

    @NotNull(message = "paymentIntentId is required")
    private Long paymentIntentId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    private String reason;

    public CreateRefundRequest() {}

    public Long getPaymentIntentId() { return paymentIntentId; }
    public void setPaymentIntentId(Long paymentIntentId) { this.paymentIntentId = paymentIntentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
