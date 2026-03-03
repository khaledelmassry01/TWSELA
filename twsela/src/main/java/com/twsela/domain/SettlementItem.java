package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * بند تسوية — عنصر واحد ضمن دفعة تسوية (COD / رسوم توصيل / استرداد / تعديل).
 */
@Entity
@Table(name = "settlement_items", indexes = {
        @Index(name = "idx_si_batch", columnList = "batch_id"),
        @Index(name = "idx_si_merchant", columnList = "merchant_id"),
        @Index(name = "idx_si_shipment", columnList = "shipment_id"),
        @Index(name = "idx_si_type", columnList = "type")
})
public class SettlementItem {

    public enum ItemType { COD, DELIVERY_FEE, REFUND, ADJUSTMENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private SettlementBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_intent_id")
    private PaymentIntent paymentIntent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    private Shipment shipment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private User merchant;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal fee = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ItemType type;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ── Constructors ──
    public SettlementItem() {}

    // ── Getters / Setters ──
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public SettlementBatch getBatch() { return batch; }
    public void setBatch(SettlementBatch batch) { this.batch = batch; }

    public PaymentIntent getPaymentIntent() { return paymentIntent; }
    public void setPaymentIntent(PaymentIntent paymentIntent) { this.paymentIntent = paymentIntent; }

    public Shipment getShipment() { return shipment; }
    public void setShipment(Shipment shipment) { this.shipment = shipment; }

    public User getMerchant() { return merchant; }
    public void setMerchant(User merchant) { this.merchant = merchant; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getFee() { return fee; }
    public void setFee(BigDecimal fee) { this.fee = fee; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public ItemType getType() { return type; }
    public void setType(ItemType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    // ── equals / hashCode ──
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SettlementItem that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
