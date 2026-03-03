package com.twsela.web;

import com.twsela.service.ReceivingService;
import com.twsela.service.FulfillmentService;
import com.twsela.service.PickWaveService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.WarehouseFulfillmentDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Fulfillment", description = "إدارة التنفيذ والاستلام")
public class FulfillmentController {

    private final ReceivingService receivingService;
    private final FulfillmentService fulfillmentService;
    private final PickWaveService pickWaveService;

    public FulfillmentController(ReceivingService receivingService,
                                  FulfillmentService fulfillmentService,
                                  PickWaveService pickWaveService) {
        this.receivingService = receivingService;
        this.fulfillmentService = fulfillmentService;
        this.pickWaveService = pickWaveService;
    }

    // ── Receiving Orders ──
    @GetMapping("/api/warehouse/{warehouseId}/receiving")
    @Operation(summary = "جلب أوامر الاستلام")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getReceivingOrders(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.ok(receivingService.getByWarehouse(warehouseId), "تم جلب أوامر الاستلام"));
    }

    @GetMapping("/api/warehouse/receiving/{id}")
    @Operation(summary = "جلب أمر استلام")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getReceivingOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(receivingService.getById(id), "تم جلب أمر الاستلام"));
    }

    @GetMapping("/api/warehouse/receiving/{id}/items")
    @Operation(summary = "جلب عناصر أمر الاستلام")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getReceivingItems(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(receivingService.getItems(id), "تم جلب العناصر"));
    }

    @PostMapping("/api/warehouse/receiving")
    @Operation(summary = "إنشاء أمر استلام")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> createReceiving(@Valid @RequestBody CreateReceivingOrderRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(receivingService.create(req), "تم إنشاء أمر الاستلام"));
    }

    @PatchMapping("/api/warehouse/receiving/{id}/status")
    @Operation(summary = "تحديث حالة أمر الاستلام")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> updateReceivingStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok(receivingService.updateStatus(id, status), "تم تحديث الحالة"));
    }

    // ── Fulfillment Orders ──
    @GetMapping("/api/warehouse/{warehouseId}/fulfillment")
    @Operation(summary = "جلب أوامر التنفيذ")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getFulfillmentOrders(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.ok(fulfillmentService.getByWarehouse(warehouseId), "تم جلب أوامر التنفيذ"));
    }

    @GetMapping("/api/warehouse/fulfillment/{id}")
    @Operation(summary = "جلب أمر تنفيذ")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getFulfillmentOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(fulfillmentService.getById(id), "تم جلب أمر التنفيذ"));
    }

    @GetMapping("/api/warehouse/fulfillment/{id}/items")
    @Operation(summary = "جلب عناصر أمر التنفيذ")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getFulfillmentItems(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(fulfillmentService.getItems(id), "تم جلب العناصر"));
    }

    @PostMapping("/api/warehouse/fulfillment")
    @Operation(summary = "إنشاء أمر تنفيذ")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> createFulfillment(@Valid @RequestBody CreateFulfillmentOrderRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(fulfillmentService.create(req), "تم إنشاء أمر التنفيذ"));
    }

    @PatchMapping("/api/warehouse/fulfillment/{id}/status")
    @Operation(summary = "تحديث حالة أمر التنفيذ")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> updateFulfillmentStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok(fulfillmentService.updateStatus(id, status), "تم تحديث الحالة"));
    }

    @PatchMapping("/api/warehouse/fulfillment/{id}/assign-picker")
    @Operation(summary = "تعيين جامع للطلب")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> assignPicker(@PathVariable Long id, @RequestParam Long pickerId) {
        return ResponseEntity.ok(ApiResponse.ok(fulfillmentService.assignPicker(id, pickerId), "تم تعيين الجامع"));
    }

    // ── Pick Waves ──
    @GetMapping("/api/warehouse/{warehouseId}/pick-waves")
    @Operation(summary = "جلب موجات الالتقاط")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> getPickWaves(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(ApiResponse.ok(pickWaveService.getByWarehouse(warehouseId), "تم جلب موجات الالتقاط"));
    }

    @PostMapping("/api/warehouse/pick-waves")
    @Operation(summary = "إنشاء موجة التقاط")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> createPickWave(@Valid @RequestBody CreatePickWaveRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(pickWaveService.create(req), "تم إنشاء موجة الالتقاط"));
    }

    @PatchMapping("/api/warehouse/pick-waves/{id}/status")
    @Operation(summary = "تحديث حالة موجة الالتقاط")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> updatePickWaveStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.ok(pickWaveService.updateStatus(id, status), "تم تحديث الحالة"));
    }

    @PatchMapping("/api/warehouse/pick-waves/{id}/assign-picker")
    @Operation(summary = "تعيين جامع لموجة الالتقاط")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN','WAREHOUSE_MANAGER')")
    public ResponseEntity<ApiResponse<?>> assignPickWavePicker(@PathVariable Long id, @RequestParam Long pickerId) {
        return ResponseEntity.ok(ApiResponse.ok(pickWaveService.assignPicker(id, pickerId), "تم تعيين الجامع"));
    }
}
