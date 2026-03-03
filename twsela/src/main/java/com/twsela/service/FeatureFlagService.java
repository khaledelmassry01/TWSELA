package com.twsela.service;

import com.twsela.domain.FeatureFlag;
import com.twsela.domain.FeatureFlagAudit;
import com.twsela.domain.SearchIndex;
import com.twsela.repository.FeatureFlagRepository;
import com.twsela.repository.FeatureFlagAuditRepository;
import com.twsela.repository.SearchIndexRepository;
import com.twsela.web.dto.RateLimitFeatureFlagDTO.*;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FeatureFlagService {

    private final FeatureFlagRepository flagRepo;
    private final FeatureFlagAuditRepository auditRepo;
    private final SearchIndexRepository searchRepo;

    public FeatureFlagService(FeatureFlagRepository flagRepo,
                               FeatureFlagAuditRepository auditRepo,
                               SearchIndexRepository searchRepo) {
        this.flagRepo = flagRepo;
        this.auditRepo = auditRepo;
        this.searchRepo = searchRepo;
    }

    // ── Feature Flags ──
    @Transactional(readOnly = true)
    public List<FeatureFlagResponse> getAllFlags() {
        return flagRepo.findAll().stream().map(this::toFlagResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FeatureFlagResponse> getEnabledFlags() {
        return flagRepo.findByIsEnabledTrue().stream().map(this::toFlagResponse).toList();
    }

    @Transactional(readOnly = true)
    public FeatureFlagResponse getByKey(String featureKey) {
        return toFlagResponse(flagRepo.findByFeatureKey(featureKey)
                .orElseThrow(() -> new ResourceNotFoundException("الميزة غير موجودة")));
    }

    public FeatureFlagResponse createFlag(CreateFeatureFlagRequest req, Long createdById) {
        if (flagRepo.existsByFeatureKey(req.featureKey()))
            throw new DuplicateResourceException("مفتاح الميزة مستخدم بالفعل");
        var f = new FeatureFlag();
        f.setFeatureKey(req.featureKey());
        f.setName(req.name());
        f.setDescription(req.description());
        if (req.isEnabled() != null) f.setIsEnabled(req.isEnabled());
        if (req.rolloutPercentage() != null) f.setRolloutPercentage(req.rolloutPercentage());
        f.setTargetRoles(req.targetRoles());
        f.setTargetTenants(req.targetTenants());
        f.setStartDate(req.startDate());
        f.setEndDate(req.endDate());
        f.setMetadata(req.metadata());
        f.setCreatedById(createdById);
        var saved = flagRepo.save(f);
        logAudit(saved.getId(), "CREATED", null, String.valueOf(saved.getIsEnabled()), createdById, "إنشاء ميزة جديدة");
        return toFlagResponse(saved);
    }

    public FeatureFlagResponse toggleFlag(Long id, boolean enabled, Long changedById, String reason) {
        var f = flagRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("الميزة غير موجودة"));
        String prev = String.valueOf(f.getIsEnabled());
        f.setIsEnabled(enabled);
        var saved = flagRepo.save(f);
        logAudit(id, enabled ? "ENABLED" : "DISABLED", prev, String.valueOf(enabled), changedById, reason);
        return toFlagResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FeatureFlagAuditResponse> getAuditLog(Long flagId) {
        return auditRepo.findByFeatureFlagIdOrderByCreatedAtDesc(flagId).stream()
                .map(this::toAuditResponse).toList();
    }

    // ── Search Indexes ──
    @Transactional(readOnly = true)
    public List<SearchIndexResponse> getActiveIndexes() {
        return searchRepo.findByIsActiveTrue().stream().map(this::toSearchResponse).toList();
    }

    public SearchIndexResponse createIndex(CreateSearchIndexRequest req) {
        var s = new SearchIndex();
        s.setName(req.name());
        s.setEntityType(req.entityType());
        s.setFields(req.fields());
        if (req.language() != null) s.setLanguage(req.language());
        s.setRebuildCronExpression(req.rebuildCronExpression());
        return toSearchResponse(searchRepo.save(s));
    }

    private void logAudit(Long flagId, String action, String prev, String next, Long changedById, String reason) {
        var a = new FeatureFlagAudit();
        a.setFeatureFlagId(flagId);
        a.setAction(action);
        a.setPreviousValue(prev);
        a.setNewValue(next);
        a.setChangedById(changedById);
        a.setReason(reason);
        auditRepo.save(a);
    }

    private FeatureFlagResponse toFlagResponse(FeatureFlag f) {
        return new FeatureFlagResponse(f.getId(), f.getFeatureKey(), f.getName(),
                f.getDescription(), f.getIsEnabled(), f.getRolloutPercentage(),
                f.getTargetRoles(), f.getTargetTenants(), f.getStartDate(),
                f.getEndDate(), f.getCreatedById(), f.getMetadata(), f.getCreatedAt());
    }

    private FeatureFlagAuditResponse toAuditResponse(FeatureFlagAudit a) {
        return new FeatureFlagAuditResponse(a.getId(), a.getFeatureFlagId(), a.getAction(),
                a.getPreviousValue(), a.getNewValue(), a.getChangedById(),
                a.getReason(), a.getCreatedAt());
    }

    private SearchIndexResponse toSearchResponse(SearchIndex s) {
        return new SearchIndexResponse(s.getId(), s.getName(), s.getEntityType(), s.getFields(),
                s.getLanguage(), s.getIsActive(), s.getLastRebuiltAt(), s.getDocumentCount(),
                s.getRebuildCronExpression(), s.getCreatedAt());
    }
}
