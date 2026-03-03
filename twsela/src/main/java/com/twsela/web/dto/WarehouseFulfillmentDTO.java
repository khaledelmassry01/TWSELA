package com.twsela.web.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class WarehouseFulfillmentDTO {
    private WarehouseFulfillmentDTO() {}

    // ── WarehouseZone ──
    public record CreateWarehouseZoneRequest(
        @NotNull Long warehouseId,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 20) String code,
        @NotBlank String zoneType,
        @Min(0) int capacity,
        int sortOrder
    ) {}

    public record WarehouseZoneResponse(
        Long id, Long warehouseId, String name, String code,
        String zoneType, int capacity, int currentOccupancy,
        boolean active, int sortOrder, Instant createdAt
    ) {}

    // ── StorageBin ──
    public record CreateStorageBinRequest(
        @NotNull Long warehouseZoneId,
        @NotBlank @Size(max = 30) String binCode,
        String aisle, String rack, String shelf, String position,
        String binType, BigDecimal maxWeight, @Min(1) int maxItems
    ) {}

    public record StorageBinResponse(
        Long id, Long warehouseZoneId, String binCode,
        String aisle, String rack, String shelf, String position,
        String binType, BigDecimal maxWeight, int maxItems,
        int currentItems, boolean occupied, boolean active, Instant createdAt
    ) {}

    // ── InventoryMovement ──
    public record CreateInventoryMovementRequest(
        @NotNull Long warehouseId,
        Long storageBinId,
        String productSku,
        @NotBlank String movementType,
        int quantity,
        String referenceType, Long referenceId,
        String notes
    ) {}

    public record InventoryMovementResponse(
        Long id, Long warehouseId, Long storageBinId,
        String productSku, String movementType, int quantity,
        String referenceType, Long referenceId,
        Long performedById, String notes, Instant createdAt
    ) {}

    // ── ReceivingOrder ──
    public record CreateReceivingOrderRequest(
        @NotNull Long warehouseId,
        Long merchantId,
        @NotBlank String referenceNumber,
        LocalDate expectedDate,
        int totalExpectedItems,
        String notes,
        List<CreateReceivingOrderItemRequest> items
    ) {}

    public record ReceivingOrderResponse(
        Long id, Long warehouseId, Long merchantId,
        String referenceNumber, String status,
        LocalDate expectedDate, Instant arrivedAt, Instant completedAt,
        int totalExpectedItems, int totalReceivedItems,
        String notes, Instant createdAt
    ) {}

    // ── ReceivingOrderItem ──
    public record CreateReceivingOrderItemRequest(
        String productSku, String productName,
        int expectedQuantity
    ) {}

    public record ReceivingOrderItemResponse(
        Long id, Long receivingOrderId, String productSku,
        String productName, int expectedQuantity,
        int receivedQuantity, int damagedQuantity,
        Long assignedBinId, String inspectionNotes, String status
    ) {}

    // ── FulfillmentOrder ──
    public record CreateFulfillmentOrderRequest(
        @NotNull Long warehouseId,
        Long shipmentId, Long merchantId,
        @NotBlank String orderNumber,
        String priority,
        List<CreateFulfillmentOrderItemRequest> items
    ) {}

    public record FulfillmentOrderResponse(
        Long id, Long warehouseId, Long shipmentId,
        Long merchantId, String orderNumber, String status,
        String priority, Long assignedPickerId, Long assignedPackerId,
        Instant pickedAt, Instant packedAt, Instant shippedAt, Instant createdAt
    ) {}

    // ── FulfillmentOrderItem ──
    public record CreateFulfillmentOrderItemRequest(
        String productSku, String productName,
        int quantity, Long sourceBinId, int pickSequence
    ) {}

    public record FulfillmentOrderItemResponse(
        Long id, Long fulfillmentOrderId, String productSku,
        String productName, int quantity, int pickedQuantity,
        Long sourceBinId, int pickSequence, boolean picked
    ) {}

    // ── PickWave ──
    public record CreatePickWaveRequest(
        @NotNull Long warehouseId,
        @NotBlank String waveNumber,
        String strategy, int totalOrders
    ) {}

    public record PickWaveResponse(
        Long id, Long warehouseId, String waveNumber,
        String status, String strategy,
        int totalOrders, int completedOrders,
        Long assignedPickerId, Instant startedAt,
        Instant completedAt, Instant createdAt
    ) {}
}
