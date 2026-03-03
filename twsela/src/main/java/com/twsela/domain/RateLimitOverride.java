package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(name = "rate_limit_overrides")
public class RateLimitOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rate_limit_policy_id", nullable = false)
    private Long rateLimitPolicyId;

    @NotBlank
    @Column(name = "override_type", nullable = false, length = 20)
    private String overrideType;

    @NotBlank
    @Column(name = "override_value", nullable = false, length = 255)
    private String overrideValue;

    @Column(name = "custom_max_requests")
    private Integer customMaxRequests;

    @Column(name = "custom_window_seconds")
    private Integer customWindowSeconds;

    @Column(length = 500)
    private String reason;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_by_id")
    private Long createdById;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() { createdAt = Instant.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRateLimitPolicyId() { return rateLimitPolicyId; }
    public void setRateLimitPolicyId(Long rateLimitPolicyId) { this.rateLimitPolicyId = rateLimitPolicyId; }
    public String getOverrideType() { return overrideType; }
    public void setOverrideType(String overrideType) { this.overrideType = overrideType; }
    public String getOverrideValue() { return overrideValue; }
    public void setOverrideValue(String overrideValue) { this.overrideValue = overrideValue; }
    public Integer getCustomMaxRequests() { return customMaxRequests; }
    public void setCustomMaxRequests(Integer customMaxRequests) { this.customMaxRequests = customMaxRequests; }
    public Integer getCustomWindowSeconds() { return customWindowSeconds; }
    public void setCustomWindowSeconds(Integer customWindowSeconds) { this.customWindowSeconds = customWindowSeconds; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
