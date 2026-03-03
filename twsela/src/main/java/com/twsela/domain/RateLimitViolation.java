package com.twsela.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

@Entity
@Table(name = "rate_limit_violations")
public class RateLimitViolation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rate_limit_policy_id")
    private Long rateLimitPolicyId;

    @NotBlank
    @Column(name = "violator_type", nullable = false, length = 20)
    private String violatorType;

    @NotBlank
    @Column(name = "violator_value", nullable = false, length = 255)
    private String violatorValue;

    @Column(name = "request_path", length = 500)
    private String requestPath;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_count", nullable = false)
    private Integer requestCount = 0;

    @Column(name = "window_start")
    private Instant windowStart;

    @Column(name = "blocked_at")
    private Instant blockedAt;

    @Column(name = "unblocked_at")
    private Instant unblockedAt;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (blockedAt == null) blockedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRateLimitPolicyId() { return rateLimitPolicyId; }
    public void setRateLimitPolicyId(Long rateLimitPolicyId) { this.rateLimitPolicyId = rateLimitPolicyId; }
    public String getViolatorType() { return violatorType; }
    public void setViolatorType(String violatorType) { this.violatorType = violatorType; }
    public String getViolatorValue() { return violatorValue; }
    public void setViolatorValue(String violatorValue) { this.violatorValue = violatorValue; }
    public String getRequestPath() { return requestPath; }
    public void setRequestPath(String requestPath) { this.requestPath = requestPath; }
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public Integer getRequestCount() { return requestCount; }
    public void setRequestCount(Integer requestCount) { this.requestCount = requestCount; }
    public Instant getWindowStart() { return windowStart; }
    public void setWindowStart(Instant windowStart) { this.windowStart = windowStart; }
    public Instant getBlockedAt() { return blockedAt; }
    public void setBlockedAt(Instant blockedAt) { this.blockedAt = blockedAt; }
    public Instant getUnblockedAt() { return unblockedAt; }
    public void setUnblockedAt(Instant unblockedAt) { this.unblockedAt = unblockedAt; }
    public Long getTenantId() { return tenantId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
