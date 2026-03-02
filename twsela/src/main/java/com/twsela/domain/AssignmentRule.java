package com.twsela.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Configurable rules for the smart assignment engine.
 */
@Entity
@Table(name = "assignment_rules", indexes = {
    @Index(name = "idx_rule_active", columnList = "is_active")
})
public class AssignmentRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_key", nullable = false, unique = true, length = 50)
    private String ruleKey;

    @Column(name = "rule_value", nullable = false, length = 100)
    private String ruleValue;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public AssignmentRule() {}

    public AssignmentRule(String ruleKey, String ruleValue, String description) {
        this.ruleKey = ruleKey;
        this.ruleValue = ruleValue;
        this.description = description;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRuleKey() { return ruleKey; }
    public void setRuleKey(String ruleKey) { this.ruleKey = ruleKey; }
    public String getRuleValue() { return ruleValue; }
    public void setRuleValue(String ruleValue) { this.ruleValue = ruleValue; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    /** Parse numeric rule value (returns defaultVal if not parseable). */
    public double getNumericValue(double defaultVal) {
        try { return Double.parseDouble(ruleValue); } catch (NumberFormatException e) { return defaultVal; }
    }

    /** Parse boolean rule value. */
    public boolean getBooleanValue() { return Boolean.parseBoolean(ruleValue); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssignmentRule that = (AssignmentRule) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
