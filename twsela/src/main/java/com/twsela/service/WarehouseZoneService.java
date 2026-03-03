package com.twsela.service;

import com.twsela.domain.WarehouseZone;
import com.twsela.repository.WarehouseZoneRepository;
import com.twsela.web.dto.WarehouseFulfillmentDTO.*;
import com.twsela.web.exception.DuplicateResourceException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class WarehouseZoneService {

    private final WarehouseZoneRepository repo;

    public WarehouseZoneService(WarehouseZoneRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<WarehouseZoneResponse> getByWarehouse(Long warehouseId) {
        return repo.findByWarehouseIdAndActiveTrue(warehouseId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WarehouseZoneResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public WarehouseZoneResponse create(CreateWarehouseZoneRequest req) {
        if (repo.existsByWarehouseIdAndCode(req.warehouseId(), req.code())) {
            throw new DuplicateResourceException("رمز المنطقة موجود بالفعل: " + req.code());
        }
        WarehouseZone z = new WarehouseZone();
        z.setWarehouseId(req.warehouseId());
        z.setName(req.name());
        z.setCode(req.code());
        z.setZoneType(req.zoneType());
        z.setCapacity(req.capacity());
        z.setSortOrder(req.sortOrder());
        return toResponse(repo.save(z));
    }

    public WarehouseZoneResponse update(Long id, CreateWarehouseZoneRequest req) {
        WarehouseZone z = findOrThrow(id);
        z.setName(req.name());
        z.setZoneType(req.zoneType());
        z.setCapacity(req.capacity());
        z.setSortOrder(req.sortOrder());
        z.setUpdatedAt(Instant.now());
        return toResponse(repo.save(z));
    }

    public void toggleActive(Long id, boolean active) {
        WarehouseZone z = findOrThrow(id);
        z.setActive(active);
        z.setUpdatedAt(Instant.now());
        repo.save(z);
    }

    private WarehouseZone findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("منطقة المستودع غير موجودة: " + id));
    }

    private WarehouseZoneResponse toResponse(WarehouseZone z) {
        return new WarehouseZoneResponse(z.getId(), z.getWarehouseId(), z.getName(), z.getCode(),
                z.getZoneType(), z.getCapacity(), z.getCurrentOccupancy(),
                z.isActive(), z.getSortOrder(), z.getCreatedAt());
    }
}
