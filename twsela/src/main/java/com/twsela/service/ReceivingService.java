package com.twsela.service;

import com.twsela.domain.ReceivingOrder;
import com.twsela.domain.ReceivingOrderItem;
import com.twsela.repository.ReceivingOrderRepository;
import com.twsela.repository.ReceivingOrderItemRepository;
import com.twsela.web.dto.WarehouseFulfillmentDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ReceivingService {

    private final ReceivingOrderRepository orderRepo;
    private final ReceivingOrderItemRepository itemRepo;

    public ReceivingService(ReceivingOrderRepository orderRepo, ReceivingOrderItemRepository itemRepo) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
    }

    @Transactional(readOnly = true)
    public List<ReceivingOrderResponse> getByWarehouse(Long warehouseId) {
        return orderRepo.findByWarehouseIdOrderByCreatedAtDesc(warehouseId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ReceivingOrderResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<ReceivingOrderItemResponse> getItems(Long orderId) {
        return itemRepo.findByReceivingOrderId(orderId).stream()
                .map(this::toItemResponse).toList();
    }

    public ReceivingOrderResponse create(CreateReceivingOrderRequest req) {
        ReceivingOrder o = new ReceivingOrder();
        o.setWarehouseId(req.warehouseId());
        o.setMerchantId(req.merchantId());
        o.setReferenceNumber(req.referenceNumber());
        o.setExpectedDate(req.expectedDate());
        o.setTotalExpectedItems(req.totalExpectedItems());
        o.setNotes(req.notes());
        ReceivingOrder saved = orderRepo.save(o);

        if (req.items() != null) {
            for (CreateReceivingOrderItemRequest itemReq : req.items()) {
                ReceivingOrderItem item = new ReceivingOrderItem();
                item.setReceivingOrderId(saved.getId());
                item.setProductSku(itemReq.productSku());
                item.setProductName(itemReq.productName());
                item.setExpectedQuantity(itemReq.expectedQuantity());
                itemRepo.save(item);
            }
        }
        return toResponse(saved);
    }

    public ReceivingOrderResponse updateStatus(Long id, String status) {
        ReceivingOrder o = findOrThrow(id);
        o.setStatus(status);
        if ("ARRIVED".equals(status)) o.setArrivedAt(Instant.now());
        if ("COMPLETED".equals(status)) o.setCompletedAt(Instant.now());
        o.setUpdatedAt(Instant.now());
        return toResponse(orderRepo.save(o));
    }

    private ReceivingOrder findOrThrow(Long id) {
        return orderRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("أمر الاستلام غير موجود: " + id));
    }

    private ReceivingOrderResponse toResponse(ReceivingOrder o) {
        return new ReceivingOrderResponse(o.getId(), o.getWarehouseId(), o.getMerchantId(),
                o.getReferenceNumber(), o.getStatus(), o.getExpectedDate(),
                o.getArrivedAt(), o.getCompletedAt(),
                o.getTotalExpectedItems(), o.getTotalReceivedItems(),
                o.getNotes(), o.getCreatedAt());
    }

    private ReceivingOrderItemResponse toItemResponse(ReceivingOrderItem i) {
        return new ReceivingOrderItemResponse(i.getId(), i.getReceivingOrderId(),
                i.getProductSku(), i.getProductName(), i.getExpectedQuantity(),
                i.getReceivedQuantity(), i.getDamagedQuantity(),
                i.getAssignedBinId(), i.getInspectionNotes(), i.getStatus());
    }
}
