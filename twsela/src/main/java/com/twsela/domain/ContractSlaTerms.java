package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * SLA terms attached to a contract — defines delivery targets and penalties.
 */
@Entity
@Table(name = "contract_sla_terms")
public class ContractSlaTerms {

    public enum SlaReviewPeriod {
        MONTHLY, QUARTERLY
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false, unique = true)
    private Contract contract;

    @Column(name = "target_delivery_rate")
    private double targetDeliveryRate; // e.g. 0.95 for 95%

    @Column(name = "max_delivery_hours")
    private int maxDeliveryHours; // e.g. 48

    @Column(name = "late_penalty_per_shipment", precision = 10, scale = 2)
    private BigDecimal latePenaltyPerShipment;

    @Column(name = "lost_penalty_fixed", precision = 10, scale = 2)
    private BigDecimal lostPenaltyFixed;

    @Enumerated(EnumType.STRING)
    @Column(name = "sla_review_period")
    private SlaReviewPeriod slaReviewPeriod = SlaReviewPeriod.MONTHLY;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public ContractSlaTerms() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Contract getContract() { return contract; }
    public void setContract(Contract contract) { this.contract = contract; }

    public double getTargetDeliveryRate() { return targetDeliveryRate; }
    public void setTargetDeliveryRate(double targetDeliveryRate) { this.targetDeliveryRate = targetDeliveryRate; }

    public int getMaxDeliveryHours() { return maxDeliveryHours; }
    public void setMaxDeliveryHours(int maxDeliveryHours) { this.maxDeliveryHours = maxDeliveryHours; }

    public BigDecimal getLatePenaltyPerShipment() { return latePenaltyPerShipment; }
    public void setLatePenaltyPerShipment(BigDecimal latePenaltyPerShipment) { this.latePenaltyPerShipment = latePenaltyPerShipment; }

    public BigDecimal getLostPenaltyFixed() { return lostPenaltyFixed; }
    public void setLostPenaltyFixed(BigDecimal lostPenaltyFixed) { this.lostPenaltyFixed = lostPenaltyFixed; }

    public SlaReviewPeriod getSlaReviewPeriod() { return slaReviewPeriod; }
    public void setSlaReviewPeriod(SlaReviewPeriod slaReviewPeriod) { this.slaReviewPeriod = slaReviewPeriod; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
