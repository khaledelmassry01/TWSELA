package com.twsela.web;

import com.twsela.service.CarrierService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.MultiCarrierDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carriers")
public class CarrierController {

    private final CarrierService carrierService;

    public CarrierController(CarrierService carrierService) {
        this.carrierService = carrierService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<CarrierResponse>> createCarrier(
            @Valid @RequestBody CreateCarrierRequest request, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(carrierService.createCarrier(request, tenantId),
                "تم إنشاء شركة الشحن بنجاح"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<CarrierResponse>> getCarrier(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(carrierService.getCarrierById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<CarrierResponse>>> getCarriers(@RequestParam Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(carrierService.getAllCarriers(tenantId)));
    }

    @PostMapping("/zone-mappings")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<CarrierZoneMappingResponse>> createZoneMapping(
            @Valid @RequestBody CreateCarrierZoneMappingRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(carrierService.createZoneMapping(request),
                "تم إنشاء ربط المنطقة بنجاح"));
    }

    @GetMapping("/{carrierId}/zone-mappings")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<CarrierZoneMappingResponse>>> getZoneMappings(@PathVariable Long carrierId) {
        return ResponseEntity.ok(ApiResponse.ok(carrierService.getMappingsByCarrier(carrierId)));
    }

    @PostMapping("/rates")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<CarrierRateResponse>> createRate(
            @Valid @RequestBody CreateCarrierRateRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(carrierService.createRate(request), "تم إنشاء السعر بنجاح"));
    }

    @GetMapping("/{carrierId}/rates")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<CarrierRateResponse>>> getRates(@PathVariable Long carrierId) {
        return ResponseEntity.ok(ApiResponse.ok(carrierService.getRatesByCarrier(carrierId)));
    }

    @PostMapping("/shipments")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<CarrierShipmentResponse>> createCarrierShipment(
            @Valid @RequestBody CreateCarrierShipmentRequest request, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(carrierService.createCarrierShipment(request, tenantId),
                "تم ربط الشحنة بشركة الشحن"));
    }

    @GetMapping("/shipments/{shipmentId}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','MERCHANT')")
    public ResponseEntity<ApiResponse<CarrierShipmentResponse>> getCarrierShipment(@PathVariable Long shipmentId) {
        return ResponseEntity.ok(ApiResponse.ok(carrierService.getByShipmentId(shipmentId)));
    }

    @PostMapping("/selection-rules")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<CarrierSelectionRuleResponse>> createSelectionRule(
            @Valid @RequestBody CreateSelectionRuleRequest request, @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(carrierService.createSelectionRule(request, tenantId),
                "تم إنشاء قاعدة الاختيار بنجاح"));
    }

    @GetMapping("/selection-rules")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<CarrierSelectionRuleResponse>>> getSelectionRules(@RequestParam Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(carrierService.getActiveRules(tenantId)));
    }
}
