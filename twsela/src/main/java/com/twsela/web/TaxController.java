package com.twsela.web;

import com.twsela.service.TaxService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.CountryDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@Tag(name = "Tax", description = "إدارة الضرائب")
public class TaxController {

    private final TaxService taxService;

    public TaxController(TaxService taxService) {
        this.taxService = taxService;
    }

    @GetMapping("/api/tax/calculate")
    @Operation(summary = "حساب الضريبة لمبلغ في دولة محددة")
    public ResponseEntity<ApiResponse<?>> calculate(
            @RequestParam BigDecimal amount,
            @RequestParam String countryCode) {
        return ResponseEntity.ok(ApiResponse.ok(taxService.calculateTax(amount, countryCode), "نتيجة حساب الضريبة"));
    }

    @GetMapping("/api/admin/tax/rules")
    @Operation(summary = "جلب جميع قواعد الضرائب")
    public ResponseEntity<ApiResponse<?>> getAllRules() {
        return ResponseEntity.ok(ApiResponse.ok(taxService.getAllRules(), "قواعد الضرائب"));
    }

    @GetMapping("/api/admin/tax/rules/{countryCode}")
    @Operation(summary = "جلب قواعد الضرائب لدولة محددة")
    public ResponseEntity<ApiResponse<?>> getRulesByCountry(@PathVariable String countryCode) {
        return ResponseEntity.ok(ApiResponse.ok(taxService.getApplicableRules(countryCode), "قواعد الضرائب"));
    }

    @PostMapping("/api/admin/tax/rules")
    @Operation(summary = "إضافة قاعدة ضريبية")
    public ResponseEntity<ApiResponse<?>> createRule(@Valid @RequestBody CreateTaxRuleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(taxService.createRule(req), "تم إضافة القاعدة الضريبية"));
    }

    @PutMapping("/api/admin/tax/rules/{id}")
    @Operation(summary = "تحديث قاعدة ضريبية")
    public ResponseEntity<ApiResponse<?>> updateRule(@PathVariable Long id,
                                                      @Valid @RequestBody CreateTaxRuleRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(taxService.updateRule(id, req), "تم تحديث القاعدة الضريبية"));
    }
}
