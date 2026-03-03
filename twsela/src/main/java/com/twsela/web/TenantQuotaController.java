package com.twsela.web;

import com.twsela.domain.TenantQuota;
import com.twsela.service.TenantQuotaService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.TenantUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * وحدة تحكم حصص المستأجر.
 */
@RestController
@RequestMapping("/api/tenants/{tenantId}/quotas")
@Tag(name = "Tenant Quotas", description = "إدارة حصص المستأجر")
public class TenantQuotaController {

    private final TenantQuotaService quotaService;

    public TenantQuotaController(TenantQuotaService quotaService) {
        this.quotaService = quotaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "جلب حصص المستأجر")
    public ResponseEntity<ApiResponse<List<TenantUserDTO.QuotaResponse>>> getQuotas(
            @PathVariable Long tenantId) {
        List<TenantQuota> quotas = quotaService.getQuotas(tenantId);
        List<TenantUserDTO.QuotaResponse> responses = quotas.stream()
                .map(TenantUserDTO.QuotaResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PutMapping("/{quotaType}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "تحديث الحد الأقصى لحصة")
    public ResponseEntity<ApiResponse<TenantUserDTO.QuotaResponse>> updateQuota(
            @PathVariable Long tenantId,
            @PathVariable TenantQuota.QuotaType quotaType,
            @RequestParam long maxValue) {
        TenantQuota quota = quotaService.updateMaxValue(tenantId, quotaType, maxValue);
        return ResponseEntity.ok(ApiResponse.ok(TenantUserDTO.QuotaResponse.from(quota), "تم تحديث الحصة"));
    }

    @GetMapping("/usage")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "جلب استخدام الحصص")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUsage(@PathVariable Long tenantId) {
        Map<String, Object> stats = quotaService.getUsageStats(tenantId);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
