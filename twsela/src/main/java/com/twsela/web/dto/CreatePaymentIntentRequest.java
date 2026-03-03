package com.twsela.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreatePaymentIntentRequest {

    @NotNull(message = "shipmentId is required")
    private Long shipmentId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be greater than 0")
    private BigDecimal amount;

    private String currency = "EGP";

    @NotBlank(message = "gateway is required")
    private String gateway;

    private Long paymentMethodId;

    public CreatePaymentIntentRequest() {}

    public Long getShipmentId() { return shipmentId; }
    public void setShipmentId(Long shipmentId) { this.shipmentId = shipmentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public Long getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(Long paymentMethodId) { this.paymentMethodId = paymentMethodId; }
}
