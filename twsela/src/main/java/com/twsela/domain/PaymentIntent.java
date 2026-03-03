package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * نية دفع — تمثل عملية دفع قبل اكتمالها (pending → processing → succeeded/failed/refunded).
 */
@Entity
@Table(name = "payment_intents", indexes = {
        @Index(name = "idx_pi_shipment", columnList = "shipment_id"),
        @Index(name = "idx_pi_status", columnList = "status"),
        @Index(name = "idx_pi_provider_ref", columnList = "provider_ref"),
        @Index(name = "idx_pi_created", columnList = "created_at")
})
public class PaymentIntent {

    public enum IntentStatus { PENDING, PROCESSING, SUCCEEDED, FAILED, CANCELLED, REFUNDED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id", nullable = false)
    private Shipment shipment;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "EGP";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IntentStatus status = IntentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 20)
    private PaymentTransaction.PaymentGatewayType provider;

    @Column(name = "provider_ref", length = 255)
    private String providerRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    @Column(name = "failed_at")
    private Instant failedAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Constructors ──
    public PaymentIntent() {}

    // ── Getters / Setters ──
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public IntentStatus getStatus() { return status; }
    public void setStatus(IntentStatus status) { this.status = status; }

    public PaymentTransaction.PaymentGatewayType getProvider() { return provider; }
    public void setProvider(PaymentTransaction.PaymentGatewayType provider) { this.provider = provider; }

    public String getProviderRef() { return providerRef; }
    public void setProviderRef(String providerRef) { this.providerRef = providerRef; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public Integer getAttempts() { return attempts; }
    public void setAttempts(Integer attempts) { this.attempts = attempts; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Instant confirmedAt) { this.confirmedAt = confirmedAt; }

    public Instant getFailedAt() { return failedAt; }
    public void setFailedAt(Instant failedAt) { this.failedAt = failedAt; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // ── equals / hashCode ──
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentIntent that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
