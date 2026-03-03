package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * طلب استرداد مبلغ — يرتبط بنية دفع ناجحة ويمر بمراحل الموافقة والمعالجة.
 */
@Entity
@Table(name = "payment_refunds", indexes = {
        @Index(name = "idx_pr_intent", columnList = "payment_intent_id"),
        @Index(name = "idx_pr_status", columnList = "status"),
        @Index(name = "idx_pr_created", columnList = "created_at")
})
public class PaymentRefund {

    public enum RefundStatus { PENDING, APPROVED, PROCESSING, PROCESSED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_intent_id", nullable = false)
    private PaymentIntent paymentIntent;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "reason", length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "provider_ref", length = 255)
    private String providerRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "rejected_reason", length = 500)
    private String rejectedReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Constructors ──
    public PaymentRefund() {}

    // ── Getters / Setters ──
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public PaymentIntent getPaymentIntent() { return paymentIntent; }
    public void setPaymentIntent(PaymentIntent paymentIntent) { this.paymentIntent = paymentIntent; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }

    public String getProviderRef() { return providerRef; }
    public void setProviderRef(String providerRef) { this.providerRef = providerRef; }

    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }

    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    public String getRejectedReason() { return rejectedReason; }
    public void setRejectedReason(String rejectedReason) { this.rejectedReason = rejectedReason; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ── equals / hashCode ──
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PaymentRefund that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
