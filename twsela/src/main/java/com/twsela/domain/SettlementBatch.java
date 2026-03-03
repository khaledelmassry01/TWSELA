package com.twsela.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * دفعة تسوية — تجمع المعاملات المالية لفترة زمنية محددة (يومية / أسبوعية / شهرية).
 */
@Entity
@Table(name = "settlement_batches", indexes = {
        @Index(name = "idx_sb_status", columnList = "status"),
        @Index(name = "idx_sb_period", columnList = "period, start_date"),
        @Index(name = "idx_sb_created", columnList = "created_at")
})
public class SettlementBatch {

    public enum SettlementPeriod { DAILY, WEEKLY, BIWEEKLY, MONTHLY }
    public enum BatchStatus { DRAFT, PENDING, PROCESSING, COMPLETED, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settlement_number", nullable = false, unique = true, length = 50)
    private String settlementNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "period", nullable = false, length = 20)
    private SettlementPeriod period;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "total_transactions", nullable = false)
    private Integer totalTransactions = 0;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "total_fees", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalFees = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal netAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BatchStatus status = BatchStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt;

    // ── Constructors ──
    public SettlementBatch() {}

    // ── Getters / Setters ──
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSettlementNumber() { return settlementNumber; }
    public void setSettlementNumber(String settlementNumber) { this.settlementNumber = settlementNumber; }

    public SettlementPeriod getPeriod() { return period; }
    public void setPeriod(SettlementPeriod period) { this.period = period; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getTotalTransactions() { return totalTransactions; }
    public void setTotalTransactions(Integer totalTransactions) { this.totalTransactions = totalTransactions; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalFees() { return totalFees; }
    public void setTotalFees(BigDecimal totalFees) { this.totalFees = totalFees; }

    public BigDecimal getNetAmount() { return netAmount; }
    public void setNetAmount(BigDecimal netAmount) { this.netAmount = netAmount; }

    public BatchStatus getStatus() { return status; }
    public void setStatus(BatchStatus status) { this.status = status; }

    public User getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(User generatedBy) { this.generatedBy = generatedBy; }

    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // ── equals / hashCode ──
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SettlementBatch that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
