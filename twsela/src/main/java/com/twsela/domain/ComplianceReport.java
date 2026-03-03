package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

/**
 * تقرير امتثال أمني.
 */
@Entity
@Table(name = "compliance_reports", indexes = {
        @Index(name = "idx_compliance_report_date", columnList = "report_date"),
        @Index(name = "idx_compliance_report_status", columnList = "status")
})
public class ComplianceReport {

    public enum ReportStatus {
        DRAFT, FINAL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "generated_by")
    private User generatedBy;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "total_rules", nullable = false)
    private int totalRules;

    @Column(name = "passed_rules", nullable = false)
    private int passedRules;

    @Column(name = "failed_rules", nullable = false)
    private int failedRules;

    @Column(name = "warning_rules", nullable = false)
    private int warningRules;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status;

    @Column(name = "created_at")
    private Instant createdAt;

    public ComplianceReport() {
        this.createdAt = Instant.now();
        this.status = ReportStatus.DRAFT;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(User generatedBy) { this.generatedBy = generatedBy; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public int getTotalRules() { return totalRules; }
    public void setTotalRules(int totalRules) { this.totalRules = totalRules; }

    public int getPassedRules() { return passedRules; }
    public void setPassedRules(int passedRules) { this.passedRules = passedRules; }

    public int getFailedRules() { return failedRules; }
    public void setFailedRules(int failedRules) { this.failedRules = failedRules; }

    public int getWarningRules() { return warningRules; }
    public void setWarningRules(int warningRules) { this.warningRules = warningRules; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public ReportStatus getStatus() { return status; }
    public void setStatus(ReportStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComplianceReport that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
