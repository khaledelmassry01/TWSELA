package com.twsela.service;

import com.twsela.domain.PickWave;
import com.twsela.repository.PickWaveRepository;
import com.twsela.web.dto.WarehouseFulfillmentDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class PickWaveService {

    private final PickWaveRepository repo;

    public PickWaveService(PickWaveRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<PickWaveResponse> getByWarehouse(Long warehouseId) {
        return repo.findByWarehouseIdOrderByCreatedAtDesc(warehouseId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PickWaveResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public PickWaveResponse create(CreatePickWaveRequest req) {
        PickWave w = new PickWave();
        w.setWarehouseId(req.warehouseId());
        w.setWaveNumber(req.waveNumber());
        w.setStrategy(req.strategy() != null ? req.strategy() : "SINGLE_ORDER");
        w.setTotalOrders(req.totalOrders());
        return toResponse(repo.save(w));
    }

    public PickWaveResponse updateStatus(Long id, String status) {
        PickWave w = findOrThrow(id);
        w.setStatus(status);
        if ("IN_PROGRESS".equals(status)) w.setStartedAt(Instant.now());
        if ("COMPLETED".equals(status)) w.setCompletedAt(Instant.now());
        w.setUpdatedAt(Instant.now());
        return toResponse(repo.save(w));
    }

    public PickWaveResponse assignPicker(Long id, Long pickerId) {
        PickWave w = findOrThrow(id);
        w.setAssignedPickerId(pickerId);
        w.setUpdatedAt(Instant.now());
        return toResponse(repo.save(w));
    }

    private PickWave findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("موجة الالتقاط غير موجودة: " + id));
    }

    private PickWaveResponse toResponse(PickWave w) {
        return new PickWaveResponse(w.getId(), w.getWarehouseId(), w.getWaveNumber(),
                w.getStatus(), w.getStrategy(), w.getTotalOrders(), w.getCompletedOrders(),
                w.getAssignedPickerId(), w.getStartedAt(), w.getCompletedAt(), w.getCreatedAt());
    }
}
