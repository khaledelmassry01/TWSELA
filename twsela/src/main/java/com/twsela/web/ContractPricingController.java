package com.twsela.web;

import com.twsela.domain.CustomPricingRule;
import com.twsela.service.CustomPricingService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.ContractDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for contract pricing rules and price calculation.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Contract Pricing", description = "إدارة أسعار العقود")
public class ContractPricingController {

    private final CustomPricingService pricingService;

    public ContractPricingController(CustomPricingService pricingService) {
        this.pricingService = pricingService;
    }

    @PostMapping("/admin/contracts/{contractId}/pricing")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "إضافة قاعدة تسعير للعقد")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> addPricingRule(
            @PathVariable Long contractId,
            @Valid @RequestBody CreatePricingRuleRequest request) {
        CustomPricingRule rule = pricingService.addPricingRule(
                contractId, request.zoneFromId(), request.zoneToId(),
                request.shipmentType(), request.basePrice(), request.perKgPrice(),
                request.codFeePercent(), request.minimumCharge(),
                request.discountPercent(), request.minMonthlyShipments());
        return ResponseEntity.ok(ApiResponse.ok(toResponse(rule), "تم إضافة قاعدة التسعير"));
    }

    @GetMapping("/admin/contracts/{contractId}/pricing")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "عرض قواعد تسعير العقد")
    public ResponseEntity<ApiResponse<List<PricingRuleResponse>>> getPricingRules(
            @PathVariable Long contractId) {
        List<PricingRuleResponse> rules = pricingService.getPricingRules(contractId).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(rules));
    }

    @PutMapping("/admin/contracts/pricing/{ruleId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "تعديل قاعدة تسعير")
    public ResponseEntity<ApiResponse<PricingRuleResponse>> updatePricingRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody CreatePricingRuleRequest request) {
        CustomPricingRule rule = pricingService.updatePricingRule(
                ruleId, request.basePrice(), request.perKgPrice(),
                request.codFeePercent(), request.minimumCharge(),
                request.discountPercent(), request.minMonthlyShipments(), true);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(rule), "تم تحديث القاعدة"));
    }

    @GetMapping("/pricing/calculate")
    @Operation(summary = "حساب السعر الفعلي للشحنة")
    public ResponseEntity<ApiResponse<Map<String, Object>>> calculatePrice(
            @RequestParam Long merchantId,
            @RequestParam(required = false) Long zoneFromId,
            @RequestParam(required = false) Long zoneToId,
            @RequestParam(defaultValue = "1.0") double weightKg,
            @RequestParam(required = false) BigDecimal codAmount) {
        Map<String, Object> result = pricingService.calculatePrice(merchantId, zoneFromId, zoneToId, weightKg, codAmount);
        return ResponseEntity.ok(ApiResponse.ok(result, "تم حساب السعر"));
    }

    private PricingRuleResponse toResponse(CustomPricingRule rule) {
        return new PricingRuleResponse(
                rule.getId(), rule.getContract().getId(),
                rule.getZoneFrom() != null ? rule.getZoneFrom().getId() : null,
                rule.getZoneFrom() != null ? rule.getZoneFrom().getName() : null,
                rule.getZoneTo() != null ? rule.getZoneTo().getId() : null,
                rule.getZoneTo() != null ? rule.getZoneTo().getName() : null,
                rule.getShipmentType(), rule.getBasePrice(), rule.getPerKgPrice(),
                rule.getCodFeePercent(), rule.getMinimumCharge(),
                rule.getDiscountPercent(), rule.getMinMonthlyShipments(), rule.isActive());
    }
}
