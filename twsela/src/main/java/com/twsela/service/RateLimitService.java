package com.twsela.service;

import com.twsela.domain.RateLimitPolicy;
import com.twsela.domain.RateLimitOverride;
import com.twsela.domain.RateLimitViolation;
import com.twsela.domain.CachePolicy;
import com.twsela.repository.RateLimitPolicyRepository;
import com.twsela.repository.RateLimitOverrideRepository;
import com.twsela.repository.RateLimitViolationRepository;
import com.twsela.repository.CachePolicyRepository;
import com.twsela.web.dto.RateLimitFeatureFlagDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RateLimitService {

    private final RateLimitPolicyRepository policyRepo;
    private final RateLimitOverrideRepository overrideRepo;
    private final RateLimitViolationRepository violationRepo;
    private final CachePolicyRepository cacheRepo;

    public RateLimitService(RateLimitPolicyRepository policyRepo,
                             RateLimitOverrideRepository overrideRepo,
                             RateLimitViolationRepository violationRepo,
                             CachePolicyRepository cacheRepo) {
        this.policyRepo = policyRepo;
        this.overrideRepo = overrideRepo;
        this.violationRepo = violationRepo;
        this.cacheRepo = cacheRepo;
    }

    // ── Policies ──
    @Transactional(readOnly = true)
    public List<RateLimitPolicyResponse> getActivePolicies() {
        return policyRepo.findByIsActiveTrue().stream().map(this::toPolicyResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<RateLimitPolicyResponse> getAllPolicies() {
        return policyRepo.findAll().stream().map(this::toPolicyResponse).toList();
    }

    public RateLimitPolicyResponse createPolicy(CreateRateLimitPolicyRequest req) {
        var p = new RateLimitPolicy();
        p.setName(req.name());
        p.setPolicyType(req.policyType());
        if (req.maxRequests() != null) p.setMaxRequests(req.maxRequests());
        if (req.windowSeconds() != null) p.setWindowSeconds(req.windowSeconds());
        p.setBurstLimit(req.burstLimit());
        p.setCooldownSeconds(req.cooldownSeconds());
        p.setAppliesTo(req.appliesTo());
        p.setDescription(req.description());
        return toPolicyResponse(policyRepo.save(p));
    }

    public void togglePolicy(Long id, boolean active) {
        var p = policyRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("السياسة غير موجودة"));
        p.setIsActive(active);
        policyRepo.save(p);
    }

    // ── Overrides ──
    @Transactional(readOnly = true)
    public List<RateLimitOverrideResponse> getOverrides(Long policyId) {
        return overrideRepo.findByRateLimitPolicyId(policyId).stream().map(this::toOverrideResponse).toList();
    }

    public RateLimitOverrideResponse createOverride(CreateRateLimitOverrideRequest req, Long createdById) {
        var o = new RateLimitOverride();
        o.setRateLimitPolicyId(req.rateLimitPolicyId());
        o.setOverrideType(req.overrideType());
        o.setOverrideValue(req.overrideValue());
        o.setCustomMaxRequests(req.customMaxRequests());
        o.setCustomWindowSeconds(req.customWindowSeconds());
        o.setReason(req.reason());
        o.setExpiresAt(req.expiresAt());
        o.setCreatedById(createdById);
        return toOverrideResponse(overrideRepo.save(o));
    }

    // ── Violations ──
    @Transactional(readOnly = true)
    public List<RateLimitViolationResponse> getViolations(Long policyId) {
        return violationRepo.findByRateLimitPolicyIdOrderByBlockedAtDesc(policyId).stream()
                .map(this::toViolationResponse).toList();
    }

    // ── Cache Policies ──
    @Transactional(readOnly = true)
    public List<CachePolicyResponse> getActiveCachePolicies() {
        return cacheRepo.findByIsActiveTrue().stream().map(this::toCacheResponse).toList();
    }

    public CachePolicyResponse createCachePolicy(CreateCachePolicyRequest req) {
        var c = new CachePolicy();
        c.setName(req.name());
        c.setCacheRegion(req.cacheRegion());
        if (req.ttlSeconds() != null) c.setTtlSeconds(req.ttlSeconds());
        if (req.maxEntries() != null) c.setMaxEntries(req.maxEntries());
        if (req.evictionStrategy() != null) c.setEvictionStrategy(req.evictionStrategy());
        c.setDescription(req.description());
        return toCacheResponse(cacheRepo.save(c));
    }

    private RateLimitPolicyResponse toPolicyResponse(RateLimitPolicy p) {
        return new RateLimitPolicyResponse(p.getId(), p.getName(), p.getPolicyType(),
                p.getMaxRequests(), p.getWindowSeconds(), p.getBurstLimit(),
                p.getCooldownSeconds(), p.getIsActive(), p.getAppliesTo(),
                p.getDescription(), p.getCreatedAt());
    }

    private RateLimitOverrideResponse toOverrideResponse(RateLimitOverride o) {
        return new RateLimitOverrideResponse(o.getId(), o.getRateLimitPolicyId(),
                o.getOverrideType(), o.getOverrideValue(), o.getCustomMaxRequests(),
                o.getCustomWindowSeconds(), o.getReason(), o.getExpiresAt(),
                o.getCreatedById(), o.getCreatedAt());
    }

    private RateLimitViolationResponse toViolationResponse(RateLimitViolation v) {
        return new RateLimitViolationResponse(v.getId(), v.getRateLimitPolicyId(),
                v.getViolatorType(), v.getViolatorValue(), v.getRequestPath(),
                v.getRequestMethod(), v.getRequestCount(), v.getWindowStart(),
                v.getBlockedAt(), v.getUnblockedAt(), v.getCreatedAt());
    }

    private CachePolicyResponse toCacheResponse(CachePolicy c) {
        return new CachePolicyResponse(c.getId(), c.getName(), c.getCacheRegion(),
                c.getTtlSeconds(), c.getMaxEntries(), c.getEvictionStrategy(),
                c.getIsActive(), c.getDescription(), c.getCreatedAt());
    }
}
