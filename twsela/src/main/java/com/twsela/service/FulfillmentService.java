package com.twsela.service;

import com.twsela.domain.FulfillmentOrder;
import com.twsela.domain.FulfillmentOrderItem;
import com.twsela.repository.FulfillmentOrderRepository;
import com.twsela.repository.FulfillmentOrderItemRepository;
import com.twsela.web.dto.WarehouseFulfillmentDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class FulfillmentService {

    private final FulfillmentOrderRepository orderRepo;
    private final FulfillmentOrderItemRepository itemRepo;

    public FulfillmentService(FulfillmentOrderRepository orderRepo, FulfillmentOrderItemRepository itemRepo) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
    }

    @Transactional(readOnly = true)
    public List<FulfillmentOrderResponse> getByWarehouse(Long warehouseId) {
        return orderRepo.findByWarehouseIdOrderByCreatedAtDesc(warehouseId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public FulfillmentOrderResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<FulfillmentOrderItemResponse> getItems(Long orderId) {
        return itemRepo.findByFulfillmentOrderIdOrderByPickSequence(orderId).stream()
                .map(this::toItemResponse).toList();
    }

    public FulfillmentOrderResponse create(CreateFulfillmentOrderRequest req) {
        FulfillmentOrder o = new FulfillmentOrder();
        o.setWarehouseId(req.warehouseId());
        o.setShipmentId(req.shipmentId());
        o.setMerchantId(req.merchantId());
        o.setOrderNumber(req.orderNumber());
        o.setPriority(req.priority() != null ? req.priority() : "STANDARD");
        FulfillmentOrder saved = orderRepo.save(o);

        if (req.items() != null) {
            for (CreateFulfillmentOrderItemRequest itemReq : req.items()) {
                FulfillmentOrderItem item = new FulfillmentOrderItem();
                item.setFulfillmentOrderId(saved.getId());
                item.setProductSku(itemReq.productSku());
                item.setProductName(itemReq.productName());
                item.setQuantity(itemReq.quantity());
                item.setSourceBinId(itemReq.sourceBinId());
                item.setPickSequence(itemReq.pickSequence());
                itemRepo.save(item);
            }
        }
        return toResponse(saved);
    }

    public FulfillmentOrderResponse updateStatus(Long id, String status) {
        FulfillmentOrder o = findOrThrow(id);
        o.setStatus(status);
        if ("PICKED".equals(status)) o.setPickedAt(Instant.now());
        if ("PACKED".equals(status)) o.setPackedAt(Instant.now());
        if ("SHIPPED".equals(status)) o.setShippedAt(Instant.now());
        o.setUpdatedAt(Instant.now());
        return toResponse(orderRepo.save(o));
    }

    public FulfillmentOrderResponse assignPicker(Long id, Long pickerId) {
        FulfillmentOrder o = findOrThrow(id);
        o.setAssignedPickerId(pickerId);
        o.setUpdatedAt(Instant.now());
        return toResponse(orderRepo.save(o));
    }

    private FulfillmentOrder findOrThrow(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("أمر التنفيذ غير موجود: " + id));
    }

    private FulfillmentOrderResponse toResponse(FulfillmentOrder o) {
        return new FulfillmentOrderResponse(o.getId(), o.getWarehouseId(), o.getShipmentId(),
                o.getMerchantId(), o.getOrderNumber(), o.getStatus(), o.getPriority(),
                o.getAssignedPickerId(), o.getAssignedPackerId(),
                o.getPickedAt(), o.getPackedAt(), o.getShippedAt(), o.getCreatedAt());
    }

    private FulfillmentOrderItemResponse toItemResponse(FulfillmentOrderItem i) {
        return new FulfillmentOrderItemResponse(i.getId(), i.getFulfillmentOrderId(),
                i.getProductSku(), i.getProductName(), i.getQuantity(), i.getPickedQuantity(),
                i.getSourceBinId(), i.getPickSequence(), i.isPicked());
    }
}
