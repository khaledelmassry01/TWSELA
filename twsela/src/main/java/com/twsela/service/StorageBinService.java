package com.twsela.service;

import com.twsela.domain.StorageBin;
import com.twsela.repository.StorageBinRepository;
import com.twsela.web.dto.WarehouseFulfillmentDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class StorageBinService {

    private final StorageBinRepository repo;

    public StorageBinService(StorageBinRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<StorageBinResponse> getByZone(Long zoneId) {
        return repo.findByWarehouseZoneIdAndActiveTrue(zoneId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public StorageBinResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public List<StorageBinResponse> getAvailable(Long zoneId) {
        return repo.findByWarehouseZoneIdAndOccupiedFalseAndActiveTrue(zoneId).stream()
                .map(this::toResponse).toList();
    }

    public StorageBinResponse create(CreateStorageBinRequest req) {
        StorageBin b = new StorageBin();
        b.setWarehouseZoneId(req.warehouseZoneId());
        b.setBinCode(req.binCode());
        b.setAisle(req.aisle());
        b.setRack(req.rack());
        b.setShelf(req.shelf());
        b.setPosition(req.position());
        b.setBinType(req.binType() != null ? req.binType() : "STANDARD");
        b.setMaxWeight(req.maxWeight());
        b.setMaxItems(req.maxItems());
        return toResponse(repo.save(b));
    }

    public StorageBinResponse update(Long id, CreateStorageBinRequest req) {
        StorageBin b = findOrThrow(id);
        b.setBinCode(req.binCode());
        b.setAisle(req.aisle());
        b.setRack(req.rack());
        b.setShelf(req.shelf());
        b.setPosition(req.position());
        b.setBinType(req.binType() != null ? req.binType() : b.getBinType());
        b.setMaxWeight(req.maxWeight());
        b.setMaxItems(req.maxItems());
        b.setUpdatedAt(Instant.now());
        return toResponse(repo.save(b));
    }

    public void toggleActive(Long id, boolean active) {
        StorageBin b = findOrThrow(id);
        b.setActive(active);
        b.setUpdatedAt(Instant.now());
        repo.save(b);
    }

    private StorageBin findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("حاوية التخزين غير موجودة: " + id));
    }

    private StorageBinResponse toResponse(StorageBin b) {
        return new StorageBinResponse(b.getId(), b.getWarehouseZoneId(), b.getBinCode(),
                b.getAisle(), b.getRack(), b.getShelf(), b.getPosition(),
                b.getBinType(), b.getMaxWeight(), b.getMaxItems(),
                b.getCurrentItems(), b.isOccupied(), b.isActive(), b.getCreatedAt());
    }
}
