package com.twsela.web;

import com.twsela.service.CountryService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.CountryDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Countries", description = "إدارة الدول")
public class CountryController {

    private final CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping("/api/countries")
    @Operation(summary = "جلب جميع الدول النشطة")
    public ResponseEntity<ApiResponse<?>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok(countryService.getAllActiveCountries(), "تم جلب الدول"));
    }

    @GetMapping("/api/countries/{code}")
    @Operation(summary = "جلب دولة بالرمز")
    public ResponseEntity<ApiResponse<?>> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.ok(countryService.getByCode(code), "تم جلب الدولة"));
    }

    @PostMapping("/api/admin/countries")
    @Operation(summary = "إضافة دولة جديدة")
    public ResponseEntity<ApiResponse<?>> create(@Valid @RequestBody CreateCountryRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(countryService.createCountry(req), "تم إضافة الدولة"));
    }

    @PutMapping("/api/admin/countries/{code}")
    @Operation(summary = "تحديث بيانات دولة")
    public ResponseEntity<ApiResponse<?>> update(@PathVariable String code,
                                                  @Valid @RequestBody CreateCountryRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(countryService.updateCountry(code, req), "تم تحديث الدولة"));
    }

    @PatchMapping("/api/admin/countries/{code}/toggle")
    @Operation(summary = "تفعيل/تعطيل دولة")
    public ResponseEntity<ApiResponse<?>> toggle(@PathVariable String code,
                                                  @RequestParam boolean active) {
        countryService.toggleActive(code, active);
        return ResponseEntity.ok(ApiResponse.ok("تم تحديث حالة الدولة"));
    }
}
