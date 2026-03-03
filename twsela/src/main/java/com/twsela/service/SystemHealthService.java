package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.web.dto.PlatformOpsDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SystemHealthService {

    private final SystemHealthCheckRepository healthCheckRepository;
    private final ArchivePolicyRepository archivePolicyRepository;
    private final ArchivedRecordRepository archivedRecordRepository;
    private final CleanupTaskRepository cleanupTaskRepository;

    public SystemHealthService(SystemHealthCheckRepository healthCheckRepository,
                               ArchivePolicyRepository archivePolicyRepository,
                               ArchivedRecordRepository archivedRecordRepository,
                               CleanupTaskRepository cleanupTaskRepository) {
        this.healthCheckRepository = healthCheckRepository;
        this.archivePolicyRepository = archivePolicyRepository;
        this.archivedRecordRepository = archivedRecordRepository;
        this.cleanupTaskRepository = cleanupTaskRepository;
    }

    // ── Health Checks ──

    @Transactional(readOnly = true)
    public List<SystemHealthCheck> getAllHealthChecks() {
        return healthCheckRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SystemHealthCheck> getHealthChecksByComponent(String component) {
        return healthCheckRepository.findByComponent(component);
    }

    public SystemHealthCheck createHealthCheck(CreateHealthCheckRequest request) {
        SystemHealthCheck check = new SystemHealthCheck();
        check.setComponent(request.component());
        check.setStatus(request.status());
        check.setResponseTimeMs(request.responseTimeMs() != null ? request.responseTimeMs() : 0L);
        check.setDetails(request.details());
        return healthCheckRepository.save(check);
    }

    // ── Archive Policies ──

    @Transactional(readOnly = true)
    public List<ArchivePolicy> getAllArchivePolicies(Long tenantId) {
        if (tenantId != null) {
            return archivePolicyRepository.findByTenantId(tenantId);
        }
        return archivePolicyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public ArchivePolicy getArchivePolicyById(Long id) {
        return archivePolicyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Archive policy not found with id: " + id));
    }

    public ArchivePolicy createArchivePolicy(CreateArchivePolicyRequest request, Long tenantId) {
        ArchivePolicy policy = new ArchivePolicy();
        policy.setEntityType(request.entityType());
        policy.setRetentionDays(request.retentionDays() != null ? request.retentionDays() : 365);
        policy.setArchiveStrategy(request.archiveStrategy() != null ? request.archiveStrategy() : "MOVE");
        policy.setCompressionEnabled(request.compressionEnabled() != null ? request.compressionEnabled() : true);
        policy.setIsActive(request.isActive() != null ? request.isActive() : true);
        policy.setTenantId(tenantId);
        return archivePolicyRepository.save(policy);
    }

    public void deleteArchivePolicy(Long id) {
        if (!archivePolicyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Archive policy not found with id: " + id);
        }
        archivePolicyRepository.deleteById(id);
    }

    // ── Archived Records ──

    @Transactional(readOnly = true)
    public List<ArchivedRecord> getArchivedRecordsByPolicy(Long policyId) {
        return archivedRecordRepository.findByArchivePolicyId(policyId);
    }

    // ── Cleanup Tasks ──

    @Transactional(readOnly = true)
    public List<CleanupTask> getAllCleanupTasks(Long tenantId) {
        if (tenantId != null) {
            return cleanupTaskRepository.findByTenantId(tenantId);
        }
        return cleanupTaskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CleanupTask getCleanupTaskById(Long id) {
        return cleanupTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cleanup task not found with id: " + id));
    }

    public CleanupTask createCleanupTask(CreateCleanupTaskRequest request, Long tenantId) {
        CleanupTask task = new CleanupTask();
        task.setName(request.name());
        task.setTargetTable(request.targetTable());
        task.setConditionExpression(request.conditionExpression());
        task.setDryRun(request.dryRun() != null ? request.dryRun() : true);
        task.setSchedule(request.schedule());
        task.setIsActive(request.isActive() != null ? request.isActive() : true);
        task.setTenantId(tenantId);
        return cleanupTaskRepository.save(task);
    }

    public void deleteCleanupTask(Long id) {
        if (!cleanupTaskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cleanup task not found with id: " + id);
        }
        cleanupTaskRepository.deleteById(id);
    }
}
