package com.twsela.web;

import com.twsela.domain.Tenant;
import com.twsela.service.TenantService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.TenantDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * وحدة تحكم المستأجرين.
 */
@RestController
@RequestMapping("/api/tenants")
@Tag(name = "Tenants", description = "إدارة المستأجرين")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "إنشاء مستأجر جديد")
    public ResponseEntity<ApiResponse<TenantDTO.TenantResponse>> createTenant(
            @Valid @RequestBody TenantDTO.CreateTenantRequest request) {
        Tenant tenant = tenantService.createTenant(
                request.getName(), request.getSlug(),
                request.getContactName(), request.getContactPhone(),
                request.getPlan());
        if (request.getContactEmail() != null) {
            tenantService.updateTenant(tenant.getId(), null, null, null,
                    request.getContactEmail(), null);
            tenant = tenantService.findById(tenant.getId());
        }
        return ResponseEntity.ok(ApiResponse.ok(TenantDTO.TenantResponse.from(tenant), "تم إنشاء المستأجر بنجاح"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "جلب كل المستأجرين")
    public ResponseEntity<ApiResponse<List<TenantDTO.TenantSummaryResponse>>> getAllTenants(
            @RequestParam(required = false) Tenant.TenantStatus status) {
        List<Tenant> tenants;
        if (status != null) {
            tenants = tenantService.findByStatus(status);
        } else {
            tenants = tenantService.findAll();
        }
        List<TenantDTO.TenantSummaryResponse> responses = tenants.stream()
                .map(TenantDTO.TenantSummaryResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "جلب بيانات مستأجر")
    public ResponseEntity<ApiResponse<TenantDTO.TenantResponse>> getTenant(@PathVariable Long id) {
        Tenant tenant = tenantService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(TenantDTO.TenantResponse.from(tenant)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "تحديث بيانات مستأجر")
    public ResponseEntity<ApiResponse<TenantDTO.TenantResponse>> updateTenant(
            @PathVariable Long id, @RequestBody TenantDTO.UpdateTenantRequest request) {
        Tenant tenant = tenantService.updateTenant(id, request.getName(),
                request.getContactName(), request.getContactPhone(),
                request.getContactEmail(), request.getDomain());
        return ResponseEntity.ok(ApiResponse.ok(TenantDTO.TenantResponse.from(tenant), "تم تحديث المستأجر بنجاح"));
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "تعليق مستأجر")
    public ResponseEntity<ApiResponse<TenantDTO.TenantResponse>> suspendTenant(@PathVariable Long id) {
        Tenant tenant = tenantService.suspendTenant(id);
        return ResponseEntity.ok(ApiResponse.ok(TenantDTO.TenantResponse.from(tenant), "تم تعليق المستأجر"));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "تفعيل مستأجر")
    public ResponseEntity<ApiResponse<TenantDTO.TenantResponse>> activateTenant(@PathVariable Long id) {
        Tenant tenant = tenantService.activateTenant(id);
        return ResponseEntity.ok(ApiResponse.ok(TenantDTO.TenantResponse.from(tenant), "تم تفعيل المستأجر"));
    }
}
