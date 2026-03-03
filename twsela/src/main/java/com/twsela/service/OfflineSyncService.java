package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.web.dto.OfflineMobileDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class OfflineSyncService {

    private final OfflineQueueRepository offlineQueueRepository;
    private final SyncSessionRepository syncSessionRepository;
    private final SyncConflictRepository syncConflictRepository;

    public OfflineSyncService(OfflineQueueRepository offlineQueueRepository,
                              SyncSessionRepository syncSessionRepository,
                              SyncConflictRepository syncConflictRepository) {
        this.offlineQueueRepository = offlineQueueRepository;
        this.syncSessionRepository = syncSessionRepository;
        this.syncConflictRepository = syncConflictRepository;
    }

    public OfflineQueueResponse enqueue(CreateOfflineQueueRequest request, Long tenantId) {
        OfflineQueue q = new OfflineQueue();
        q.setUserId(request.userId());
        q.setOperationType(request.operationType());
        q.setPayload(request.payload());
        if (request.priority() != null) q.setPriority(request.priority());
        q.setTenantId(tenantId);
        q = offlineQueueRepository.save(q);
        return toQueueResponse(q);
    }

    @Transactional(readOnly = true)
    public List<OfflineQueueResponse> getPendingByUser(Long userId) {
        return offlineQueueRepository.findByUserIdAndStatusOrderByPriorityAsc(userId, "PENDING")
                .stream().map(this::toQueueResponse).toList();
    }

    public SyncSessionResponse startSession(StartSyncSessionRequest request, Long tenantId) {
        SyncSession s = new SyncSession();
        s.setUserId(request.userId());
        s.setDeviceId(request.deviceId());
        s.setTenantId(tenantId);
        s = syncSessionRepository.save(s);
        return toSessionResponse(s);
    }

    @Transactional(readOnly = true)
    public SyncSessionResponse getSessionById(Long id) {
        SyncSession s = syncSessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sync session not found"));
        return toSessionResponse(s);
    }

    @Transactional(readOnly = true)
    public List<SyncConflictResponse> getConflictsBySession(Long sessionId) {
        return syncConflictRepository.findBySyncSessionId(sessionId).stream()
                .map(this::toConflictResponse).toList();
    }

    public SyncConflictResponse resolveConflict(Long id, ResolveSyncConflictRequest request) {
        SyncConflict c = syncConflictRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sync conflict not found"));
        c.setResolution(request.resolution());
        c.setResolvedAt(LocalDateTime.now());
        c = syncConflictRepository.save(c);
        return toConflictResponse(c);
    }

    private OfflineQueueResponse toQueueResponse(OfflineQueue q) {
        return new OfflineQueueResponse(q.getId(), q.getUserId(), q.getOperationType(),
                q.getPayload(), q.getPriority(), q.getStatus(), q.getCreatedOfflineAt(),
                q.getSyncedAt(), q.getErrorMessage(), q.getTenantId(), q.getCreatedAt());
    }

    private SyncSessionResponse toSessionResponse(SyncSession s) {
        return new SyncSessionResponse(s.getId(), s.getUserId(), s.getDeviceId(),
                s.getStartedAt(), s.getCompletedAt(), s.getItemsSynced(), s.getItemsFailed(),
                s.getStatus(), s.getTenantId(), s.getCreatedAt());
    }

    private SyncConflictResponse toConflictResponse(SyncConflict c) {
        return new SyncConflictResponse(c.getId(), c.getSyncSessionId(), c.getEntityType(),
                c.getEntityId(), c.getLocalData(), c.getServerData(), c.getResolution(),
                c.getResolvedAt(), c.getCreatedAt());
    }
}
