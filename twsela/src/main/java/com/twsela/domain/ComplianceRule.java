package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * قاعدة امتثال أمني.
 */
@Entity
@Table(name = "compliance_rules", indexes = {
        @Index(name = "idx_compliance_rule_category", columnList = "category"),
        @Index(name = "idx_compliance_rule_enabled", columnList = "enabled")
})
public class ComplianceRule {

    public enum Category {
        DATA_PROTECTION, ACCESS_CONTROL, ENCRYPTION, AUDIT, PASSWORD_POLICY
    }

    public enum CheckResult {
        PASS, FAIL, WARNING
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SecurityEvent.Severity severity;

    @Column(name = "check_expression", length = 500)
    private String checkExpression;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "last_checked_at")
    private Instant lastCheckedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_result", length = 20)
    private CheckResult lastResult;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public ComplianceRule() {
        this.createdAt = Instant.now();
        this.enabled = true;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public SecurityEvent.Severity getSeverity() { return severity; }
    public void setSeverity(SecurityEvent.Severity severity) { this.severity = severity; }

    public String getCheckExpression() { return checkExpression; }
    public void setCheckExpression(String checkExpression) { this.checkExpression = checkExpression; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public Instant getLastCheckedAt() { return lastCheckedAt; }
    public void setLastCheckedAt(Instant lastCheckedAt) { this.lastCheckedAt = lastCheckedAt; }

    public CheckResult getLastResult() { return lastResult; }
    public void setLastResult(CheckResult lastResult) { this.lastResult = lastResult; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ComplianceRule that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }
}
