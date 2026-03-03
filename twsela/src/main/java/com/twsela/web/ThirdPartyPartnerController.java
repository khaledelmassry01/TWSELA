package com.twsela.web;

import com.twsela.service.ThirdPartyPartnerService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.MultiCarrierDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partners")
public class ThirdPartyPartnerController {

    private final ThirdPartyPartnerService partnerService;

    public ThirdPartyPartnerController(ThirdPartyPartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<ThirdPartyPartnerResponse>> createPartner(
            @Valid @RequestBody CreatePartnerRequest request, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(partnerService.createPartner(request, tenantId),
                "تم إنشاء الشريك بنجاح"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<ThirdPartyPartnerResponse>> getPartner(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(partnerService.getPartnerById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<ThirdPartyPartnerResponse>>> getPartners(@RequestParam Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(partnerService.getActivePartners(tenantId)));
    }

    @PostMapping("/handoffs")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<PartnerHandoffResponse>> createHandoff(
            @Valid @RequestBody CreatePartnerHandoffRequest request, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(partnerService.createHandoff(request, tenantId),
                "تم تسليم الشحنة للشريك بنجاح"));
    }

    @GetMapping("/{partnerId}/handoffs")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<PartnerHandoffResponse>>> getHandoffs(@PathVariable Long partnerId) {
        return ResponseEntity.ok(ApiResponse.ok(partnerService.getHandoffsByPartner(partnerId)));
    }

    @PatchMapping("/handoffs/{id}/status")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<PartnerHandoffResponse>> updateHandoffStatus(
            @PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok(partnerService.updateHandoffStatus(id, status)));
    }
}
