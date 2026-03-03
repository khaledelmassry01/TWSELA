package com.twsela.service;

import com.twsela.domain.InventoryMovement;
import com.twsela.repository.InventoryMovementRepository;
import com.twsela.web.dto.WarehouseFulfillmentDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryMovementService {

    private final InventoryMovementRepository repo;

    public InventoryMovementService(InventoryMovementRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> getByWarehouse(Long warehouseId) {
        return repo.findByWarehouseIdOrderByCreatedAtDesc(warehouseId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryMovementResponse> getByBin(Long binId) {
        return repo.findByStorageBinIdOrderByCreatedAtDesc(binId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public InventoryMovementResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public InventoryMovementResponse create(CreateInventoryMovementRequest req, Long performedById) {
        InventoryMovement m = new InventoryMovement();
        m.setWarehouseId(req.warehouseId());
        m.setStorageBinId(req.storageBinId());
        m.setProductSku(req.productSku());
        m.setMovementType(req.movementType());
        m.setQuantity(req.quantity());
        m.setReferenceType(req.referenceType());
        m.setReferenceId(req.referenceId());
        m.setNotes(req.notes());
        m.setPerformedById(performedById);
        return toResponse(repo.save(m));
    }

    private InventoryMovement findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("حركة المخزون غير موجودة: " + id));
    }

    private InventoryMovementResponse toResponse(InventoryMovement m) {
        return new InventoryMovementResponse(m.getId(), m.getWarehouseId(), m.getStorageBinId(),
                m.getProductSku(), m.getMovementType(), m.getQuantity(),
                m.getReferenceType(), m.getReferenceId(),
                m.getPerformedById(), m.getNotes(), m.getCreatedAt());
    }
}
