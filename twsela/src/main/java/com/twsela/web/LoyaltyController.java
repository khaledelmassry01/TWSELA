package com.twsela.web;

import com.twsela.service.CampaignService;
import com.twsela.service.LoyaltyService;
import com.twsela.service.PromoCodeService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.GamificationLoyaltyDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;
    private final PromoCodeService promoCodeService;
    private final CampaignService campaignService;

    public LoyaltyController(LoyaltyService loyaltyService,
                             PromoCodeService promoCodeService,
                             CampaignService campaignService) {
        this.loyaltyService = loyaltyService;
        this.promoCodeService = promoCodeService;
        this.campaignService = campaignService;
    }

    // === Loyalty Programs ===

    @GetMapping("/loyalty/programs/merchant/{merchantId}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<LoyaltyProgramResponse>> getProgram(@PathVariable Long merchantId) {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyService.getProgramByMerchantId(merchantId)));
    }

    @PostMapping("/loyalty/programs/merchant/{merchantId}/init")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<LoyaltyProgramResponse>> initializeProgram(
            @PathVariable Long merchantId, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyService.initializeProgram(merchantId, tenantId),
                "تم تهيئة برنامج الولاء بنجاح"));
    }

    @GetMapping("/loyalty/programs")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<LoyaltyProgramResponse>>> getPrograms(@RequestParam Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyService.getProgramsByTenant(tenantId)));
    }

    // === Loyalty Transactions ===

    @PostMapping("/loyalty/transactions")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<LoyaltyTransactionResponse>> createTransaction(
            @Valid @RequestBody CreateLoyaltyTransactionRequest request, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyService.createTransaction(request, tenantId),
                "تم إنشاء معاملة الولاء بنجاح"));
    }

    @GetMapping("/loyalty/transactions/{programId}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<List<LoyaltyTransactionResponse>>> getTransactions(@PathVariable Long programId) {
        return ResponseEntity.ok(ApiResponse.ok(loyaltyService.getTransactions(programId)));
    }

    // === Promo Codes ===

    @PostMapping("/promo-codes")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> createPromoCode(
            @Valid @RequestBody CreatePromoCodeRequest request,
            @RequestParam(required = false) Long createdById,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(promoCodeService.createPromoCode(request, createdById, tenantId),
                "تم إنشاء كود الخصم بنجاح"));
    }

    @GetMapping("/promo-codes/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> getPromoCode(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(promoCodeService.getById(id)));
    }

    @GetMapping("/promo-codes/code/{code}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> getPromoCodeByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(promoCodeService.getByCode(code)));
    }

    @GetMapping("/promo-codes")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<PromoCodeResponse>>> getPromoCodes(@RequestParam Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(promoCodeService.getAllByTenant(tenantId)));
    }

    @PatchMapping("/promo-codes/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> deactivatePromoCode(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(promoCodeService.deactivate(id), "تم إلغاء تفعيل كود الخصم"));
    }

    // === Campaigns ===

    @PostMapping("/campaigns")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<CampaignResponse>> createCampaign(
            @Valid @RequestBody CreateCampaignRequest request, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(campaignService.createCampaign(request, tenantId),
                "تم إنشاء الحملة بنجاح"));
    }

    @GetMapping("/campaigns/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<CampaignResponse>> getCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(campaignService.getById(id)));
    }

    @GetMapping("/campaigns")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<CampaignResponse>>> getCampaigns(@RequestParam Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(campaignService.getCampaignsByTenant(tenantId)));
    }

    @PostMapping("/campaigns/{id}/launch")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<CampaignResponse>> launchCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(campaignService.launchCampaign(id), "تم إطلاق الحملة بنجاح"));
    }

    @PostMapping("/campaigns/{id}/complete")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<CampaignResponse>> completeCampaign(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(campaignService.completeCampaign(id), "تم إتمام الحملة بنجاح"));
    }
}
