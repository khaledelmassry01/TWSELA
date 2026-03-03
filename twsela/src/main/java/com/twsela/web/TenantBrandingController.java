package com.twsela.web;

import com.twsela.domain.Tenant;
import com.twsela.domain.TenantBranding;
import com.twsela.service.TenantBrandingService;
import com.twsela.service.TenantService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.TenantBrandingDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * وحدة تحكم العلامة التجارية للمستأجر.
 */
@RestController
@Tag(name = "Tenant Branding", description = "إدارة العلامة التجارية")
public class TenantBrandingController {

    private final TenantBrandingService brandingService;
    private final TenantService tenantService;

    public TenantBrandingController(TenantBrandingService brandingService, TenantService tenantService) {
        this.brandingService = brandingService;
        this.tenantService = tenantService;
    }

    @GetMapping("/api/tenants/{tenantId}/branding")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "جلب العلامة التجارية للمستأجر")
    public ResponseEntity<ApiResponse<TenantBrandingDTO.BrandingResponse>> getBranding(
            @PathVariable Long tenantId) {
        TenantBranding branding = brandingService.getByTenantId(tenantId);
        return ResponseEntity.ok(ApiResponse.ok(TenantBrandingDTO.BrandingResponse.from(branding)));
    }

    @PutMapping("/api/tenants/{tenantId}/branding")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "تحديث العلامة التجارية")
    public ResponseEntity<ApiResponse<TenantBrandingDTO.BrandingResponse>> updateBranding(
            @PathVariable Long tenantId, @RequestBody TenantBrandingDTO.BrandingRequest request) {
        TenantBranding branding = brandingService.updateBranding(tenantId,
                request.getPrimaryColor(), request.getSecondaryColor(),
                request.getAccentColor(), request.getFontFamily(),
                request.getCompanyNameAr(), request.getCompanyNameEn(),
                request.getTaglineAr(), request.getTaglineEn(),
                request.getFooterText(), request.getCustomCSS());
        return ResponseEntity.ok(ApiResponse.ok(TenantBrandingDTO.BrandingResponse.from(branding), "تم تحديث العلامة التجارية"));
    }

    @PostMapping("/api/tenants/{tenantId}/branding/logo")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "رفع شعار المستأجر")
    public ResponseEntity<ApiResponse<TenantBrandingDTO.BrandingResponse>> uploadLogo(
            @PathVariable Long tenantId, @RequestParam String logoUrl) {
        TenantBranding branding = brandingService.uploadLogo(tenantId, logoUrl);
        return ResponseEntity.ok(ApiResponse.ok(TenantBrandingDTO.BrandingResponse.from(branding), "تم رفع الشعار"));
    }

    @GetMapping(value = "/api/public/branding/{slug}", produces = "text/css")
    @Operation(summary = "جلب الثيم العام للمستأجر بالـ slug")
    public ResponseEntity<String> getPublicBrandingCSS(@PathVariable String slug) {
        Tenant tenant = tenantService.findBySlug(slug);
        String css = brandingService.generateCSS(tenant.getId());
        return ResponseEntity.ok(css);
    }
}
