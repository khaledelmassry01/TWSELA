package com.twsela.web.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;

public final class RateLimitFeatureFlagDTO {
    private RateLimitFeatureFlagDTO() {}

    // ── Rate Limit Policy ──
    public record CreateRateLimitPolicyRequest(
            @NotBlank String name, @NotBlank String policyType,
            Integer maxRequests, Integer windowSeconds,
            Integer burstLimit, Integer cooldownSeconds,
            String appliesTo, String description) {}

    public record RateLimitPolicyResponse(
            Long id, String name, String policyType,
            Integer maxRequests, Integer windowSeconds,
            Integer burstLimit, Integer cooldownSeconds,
            Boolean isActive, String appliesTo, String description, Instant createdAt) {}

    // ── Rate Limit Override ──
    public record CreateRateLimitOverrideRequest(
            @NotNull Long rateLimitPolicyId,
            @NotBlank String overrideType, @NotBlank String overrideValue,
            Integer customMaxRequests, Integer customWindowSeconds,
            String reason, Instant expiresAt) {}

    public record RateLimitOverrideResponse(
            Long id, Long rateLimitPolicyId, String overrideType, String overrideValue,
            Integer customMaxRequests, Integer customWindowSeconds,
            String reason, Instant expiresAt, Long createdById, Instant createdAt) {}

    // ── Rate Limit Violation ──
    public record RateLimitViolationResponse(
            Long id, Long rateLimitPolicyId, String violatorType, String violatorValue,
            String requestPath, String requestMethod, Integer requestCount,
            Instant windowStart, Instant blockedAt, Instant unblockedAt, Instant createdAt) {}

    // ── Cache Policy ──
    public record CreateCachePolicyRequest(
            @NotBlank String name, @NotBlank String cacheRegion,
            Integer ttlSeconds, Integer maxEntries,
            String evictionStrategy, String description) {}

    public record CachePolicyResponse(
            Long id, String name, String cacheRegion,
            Integer ttlSeconds, Integer maxEntries,
            String evictionStrategy, Boolean isActive,
            String description, Instant createdAt) {}

    // ── Search Index ──
    public record CreateSearchIndexRequest(
            @NotBlank String name, @NotBlank String entityType,
            String fields, String language,
            String rebuildCronExpression) {}

    public record SearchIndexResponse(
            Long id, String name, String entityType, String fields,
            String language, Boolean isActive, Instant lastRebuiltAt,
            Long documentCount, String rebuildCronExpression, Instant createdAt) {}

    // ── Feature Flag ──
    public record CreateFeatureFlagRequest(
            @NotBlank String featureKey, @NotBlank String name,
            String description, Boolean isEnabled,
            Integer rolloutPercentage, String targetRoles,
            String targetTenants, Instant startDate, Instant endDate,
            String metadata) {}

    public record FeatureFlagResponse(
            Long id, String featureKey, String name, String description,
            Boolean isEnabled, Integer rolloutPercentage,
            String targetRoles, String targetTenants,
            Instant startDate, Instant endDate,
            Long createdById, String metadata, Instant createdAt) {}

    // ── Feature Flag Audit ──
    public record FeatureFlagAuditResponse(
            Long id, Long featureFlagId, String action,
            String previousValue, String newValue,
            Long changedById, String reason, Instant createdAt) {}
}
