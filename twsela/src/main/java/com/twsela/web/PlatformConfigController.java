package com.twsela.web;

import com.twsela.service.RateLimitService;
import com.twsela.service.FeatureFlagService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.RateLimitFeatureFlagDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Platform Config", description = "إعدادات المنصة")
public class PlatformConfigController {

    private final RateLimitService rateLimitService;
    private final FeatureFlagService featureFlagService;

    public PlatformConfigController(RateLimitService rateLimitService,
                                     FeatureFlagService featureFlagService) {
        this.rateLimitService = rateLimitService;
        this.featureFlagService = featureFlagService;
    }

    // ── Rate Limit Policies ──
    @GetMapping("/api/admin/rate-limits")
    @Operation(summary = "جلب سياسات تحديد المعدل")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getRateLimits() {
        return ResponseEntity.ok(ApiResponse.ok(rateLimitService.getAllPolicies(), "تم جلب السياسات"));
    }

    @PostMapping("/api/admin/rate-limits")
    @Operation(summary = "إنشاء سياسة تحديد المعدل")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> createRateLimit(@Valid @RequestBody CreateRateLimitPolicyRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(rateLimitService.createPolicy(req), "تم إنشاء السياسة"));
    }

    @PatchMapping("/api/admin/rate-limits/{id}/toggle")
    @Operation(summary = "تفعيل/تعطيل سياسة")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> toggleRateLimit(@PathVariable Long id, @RequestParam boolean active) {
        rateLimitService.togglePolicy(id, active);
        return ResponseEntity.ok(ApiResponse.ok("تم تحديث الحالة"));
    }

    // ── Cache Policies ──
    @GetMapping("/api/admin/cache-policies")
    @Operation(summary = "جلب سياسات التخزين المؤقت")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getCachePolicies() {
        return ResponseEntity.ok(ApiResponse.ok(rateLimitService.getActiveCachePolicies(), "تم جلب السياسات"));
    }

    @PostMapping("/api/admin/cache-policies")
    @Operation(summary = "إنشاء سياسة تخزين مؤقت")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> createCachePolicy(@Valid @RequestBody CreateCachePolicyRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(rateLimitService.createCachePolicy(req), "تم إنشاء السياسة"));
    }

    // ── Feature Flags ──
    @GetMapping("/api/admin/feature-flags")
    @Operation(summary = "جلب أعلام الميزات")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getFeatureFlags() {
        return ResponseEntity.ok(ApiResponse.ok(featureFlagService.getAllFlags(), "تم جلب الأعلام"));
    }

    @GetMapping("/api/admin/feature-flags/{key}")
    @Operation(summary = "جلب علم ميزة بالمفتاح")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getFeatureFlag(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.ok(featureFlagService.getByKey(key), "تم جلب العلم"));
    }

    @PostMapping("/api/admin/feature-flags")
    @Operation(summary = "إنشاء علم ميزة")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> createFeatureFlag(@Valid @RequestBody CreateFeatureFlagRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(featureFlagService.createFlag(req, null), "تم إنشاء العلم"));
    }

    @PatchMapping("/api/admin/feature-flags/{id}/toggle")
    @Operation(summary = "تفعيل/تعطيل ميزة")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> toggleFeatureFlag(@PathVariable Long id,
                                                             @RequestParam boolean enabled,
                                                             @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(ApiResponse.ok(featureFlagService.toggleFlag(id, enabled, null, reason), "تم تحديث العلم"));
    }

    @GetMapping("/api/admin/feature-flags/{id}/audit")
    @Operation(summary = "جلب سجل تغييرات الميزة")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAuditLog(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(featureFlagService.getAuditLog(id), "تم جلب السجل"));
    }

    // ── Search Indexes ──
    @GetMapping("/api/admin/search-indexes")
    @Operation(summary = "جلب فهارس البحث")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> getSearchIndexes() {
        return ResponseEntity.ok(ApiResponse.ok(featureFlagService.getActiveIndexes(), "تم جلب الفهارس"));
    }

    @PostMapping("/api/admin/search-indexes")
    @Operation(summary = "إنشاء فهرس بحث")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<?>> createSearchIndex(@Valid @RequestBody CreateSearchIndexRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(featureFlagService.createIndex(req), "تم إنشاء الفهرس"));
    }
}
