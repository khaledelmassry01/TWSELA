package com.twsela.web;

import com.twsela.service.CurrencyService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.CountryDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@Tag(name = "Currencies", description = "إدارة العملات وأسعار الصرف")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping("/api/currencies")
    @Operation(summary = "جلب جميع العملات النشطة")
    public ResponseEntity<ApiResponse<?>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(currencyService.getAllActiveCurrencies(), "تم جلب العملات"));
    }

    @GetMapping("/api/currencies/convert")
    @Operation(summary = "تحويل مبلغ بين عملتين")
    public ResponseEntity<ApiResponse<?>> convert(
            @RequestParam BigDecimal amount,
            @RequestParam String from,
            @RequestParam String to) {
        return ResponseEntity.ok(ApiResponse.ok(currencyService.convert(amount, from, to), "تم التحويل"));
    }

    @GetMapping("/api/currencies/rate")
    @Operation(summary = "جلب سعر صرف بين عملتين")
    public ResponseEntity<ApiResponse<?>> getRate(
            @RequestParam String base,
            @RequestParam String target) {
        return ResponseEntity.ok(ApiResponse.ok(currencyService.getExchangeRate(base, target), "سعر الصرف"));
    }

    @PutMapping("/api/admin/currencies/exchange-rate")
    @Operation(summary = "تحديث سعر صرف يدوي")
    public ResponseEntity<ApiResponse<?>> updateRate(
            @Valid @RequestBody UpdateExchangeRateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(currencyService.updateRate(req), "تم تحديث سعر الصرف"));
    }
}
