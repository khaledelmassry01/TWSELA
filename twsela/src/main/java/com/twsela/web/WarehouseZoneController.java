package com.twsela.web;

import com.twsela.service.WarehouseZoneService;
import com.twsela.service.StorageBinService;
import com.twsela.service.InventoryMovementService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.WarehouseFulfillmentDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Warehouse Zones", description = "إدارة مناطق المستودع")
public class WarehouseZoneController {

    private final WarehouseZoneService zoneService;
    private final StorageBinService binService;
    private final InventoryMovementService movementService;

    public WarehouseZoneController(WarehouseZoneService zoneService,
                                    StorageBinService binService,
                                    InventoryMovementService movementService) {
        this.zoneService = zoneService;
        this.binService = binService;
        this.movementService = movementService;
    }

    // ── Warehouse Zones ──
    @GetMapping("/api/warehouse/{warehouseId}/zones")
    @Operation(summary = "جلب مناطق المستودع")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getZones(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.ok(zoneService.getByWarehouse(warehouseId), "تم جلب المناطق"));
    }

    @GetMapping("/api/warehouse/zones/{id}")
    @Operation(summary = "جلب منطقة بالمعرف")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getZone(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(zoneService.getById(id), "تم جلب المنطقة"));
    }

    @PostMapping("/api/warehouse/zones")
    @Operation(summary = "إنشاء منطقة مستودع")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> createZone(@Valid @RequestBody CreateWarehouseZoneRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(zoneService.create(req), "تم إنشاء المنطقة"));
    }

    @PutMapping("/api/warehouse/zones/{id}")
    @Operation(summary = "تحديث منطقة مستودع")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> updateZone(@PathVariable Long id,
                                                      @Valid @RequestBody CreateWarehouseZoneRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(zoneService.update(id, req), "تم تحديث المنطقة"));
    }

    @PatchMapping("/api/warehouse/zones/{id}/toggle")
    @Operation(summary = "تفعيل/تعطيل منطقة")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> toggleZone(@PathVariable Long id, @RequestParam boolean active) {
        zoneService.toggleActive(id, active);
        return ResponseEntity.ok(ApiResponse.ok("تم تحديث حالة المنطقة"));
    }

    // ── Storage Bins ──
    @GetMapping("/api/warehouse/zones/{zoneId}/bins")
    @Operation(summary = "جلب حاويات المنطقة")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getBins(@PathVariable Long zoneId) {
        return ResponseEntity.ok(ApiResponse.ok(binService.getByZone(zoneId), "تم جلب الحاويات"));
    }

    @GetMapping("/api/warehouse/zones/{zoneId}/bins/available")
    @Operation(summary = "جلب الحاويات المتاحة")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getAvailableBins(@PathVariable Long zoneId) {
        return ResponseEntity.ok(ApiResponse.ok(binService.getAvailable(zoneId), "تم جلب الحاويات المتاحة"));
    }

    @PostMapping("/api/warehouse/bins")
    @Operation(summary = "إنشاء حاوية تخزين")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> createBin(@Valid @RequestBody CreateStorageBinRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(binService.create(req), "تم إنشاء الحاوية"));
    }

    @PutMapping("/api/warehouse/bins/{id}")
    @Operation(summary = "تحديث حاوية تخزين")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> updateBin(@PathVariable Long id,
                                                     @Valid @RequestBody CreateStorageBinRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(binService.update(id, req), "تم تحديث الحاوية"));
    }

    // ── Inventory Movements ──
    @GetMapping("/api/warehouse/{warehouseId}/movements")
    @Operation(summary = "جلب حركات المخزون")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getMovements(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.ok(movementService.getByWarehouse(warehouseId), "تم جلب الحركات"));
    }

    @PostMapping("/api/warehouse/movements")
    @Operation(summary = "تسجيل حركة مخزون")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> createMovement(@Valid @RequestBody CreateInventoryMovementRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(movementService.create(req, null), "تم تسجيل الحركة"));
    }
}
