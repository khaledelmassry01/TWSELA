package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Record of a payment transaction processed through a payment gateway.
 */
@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_ptx_external_id", columnList = "external_id"),
    @Index(name = "idx_ptx_merchant", columnList = "merchant_id"),
    @Index(name = "idx_ptx_invoice", columnList = "invoice_id"),
    @Index(name = "idx_ptx_status", columnList = "status")
})
public class PaymentTransaction {

    public enum PaymentGatewayType {
        PAYMOB, TAP, STRIPE, FAWRY, MANUAL
    }

    public enum PaymentType {
        CHARGE, REFUND
    }

    public enum PaymentStatus {
        PENDING, SUCCESS, FAILED, REFUNDED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gateway", nullable = false, length = 20)
    private PaymentGatewayType gateway;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private PaymentType type;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EGP";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "invoice_id")
    private Long invoiceId;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    // Constructors
    public PaymentTransaction() {}

    public PaymentTransaction(PaymentGatewayType gateway, PaymentType type, BigDecimal amount, String currency) {
        this.gateway = gateway;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.status = PaymentStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public PaymentGatewayType getGateway() { return gateway; }
    public void setGateway(PaymentGatewayType gateway) { this.gateway = gateway; }

    public PaymentType getType() { return type; }
    public void setType(PaymentType type) { this.type = type; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public Long getMerchantId() { return merchantId; }
    public void setMerchantId(Long merchantId) { this.merchantId = merchantId; }

    public Long getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Long invoiceId) { this.invoiceId = invoiceId; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentTransaction)) return false;
        PaymentTransaction that = (PaymentTransaction) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
