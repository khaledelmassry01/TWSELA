package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class PlatformOpsDTO {

    private PlatformOpsDTO() {}

    // ── System Health Check ──
    public record CreateHealthCheckRequest(
            @NotBlank @Size(max = 50) String component,
            @NotBlank @Size(max = 20) String status,
            Long responseTimeMs,
            String details
    ) {}

    public record HealthCheckResponse(
            Long id, String component, String status, Long responseTimeMs,
            String details, LocalDateTime checkedAt, LocalDateTime createdAt
    ) {}

    // ── Archive Policy ──
    public record CreateArchivePolicyRequest(
            @NotBlank @Size(max = 50) String entityType,
            Integer retentionDays,
            @Size(max = 20) String archiveStrategy,
            Boolean compressionEnabled,
            Boolean isActive
    ) {}

    public record ArchivePolicyResponse(
            Long id, String entityType, Integer retentionDays, String archiveStrategy,
            Boolean compressionEnabled, LocalDateTime lastRunAt, Boolean isActive,
            Long tenantId, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}

    // ── Archived Record ──
    public record ArchivedRecordResponse(
            Long id, String originalTable, Long originalId, String archivedData,
            LocalDateTime archivedAt, Long archivePolicyId, LocalDateTime expiresAt,
            Long tenantId, LocalDateTime createdAt
    ) {}

    // ── Cleanup Task ──
    public record CreateCleanupTaskRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 100) String targetTable,
            String conditionExpression,
            Boolean dryRun,
            @Size(max = 50) String schedule,
            Boolean isActive
    ) {}

    public record CleanupTaskResponse(
            Long id, String name, String targetTable, String conditionExpression,
            Boolean dryRun, Integer deletedCount, LocalDateTime lastRunAt,
            String schedule, Boolean isActive, Long tenantId,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}

    // ── Platform Metric ──
    public record CreatePlatformMetricRequest(
            @NotBlank @Size(max = 100) String metricName,
            @NotNull Double metricValue,
            @Size(max = 20) String metricType,
            String labels
    ) {}

    public record PlatformMetricResponse(
            Long id, String metricName, Double metricValue, String metricType,
            String labels, LocalDateTime recordedAt, LocalDateTime createdAt
    ) {}

    // ── System Alert ──
    public record CreateSystemAlertRequest(
            @NotBlank @Size(max = 30) String alertType,
            @Size(max = 10) String severity,
            @NotBlank String message,
            @Size(max = 50) String component
    ) {}

    public record SystemAlertResponse(
            Long id, String alertType, String severity, String message,
            String component, Boolean acknowledged, Long acknowledgedById,
            LocalDateTime acknowledgedAt, LocalDateTime resolvedAt,
            Long tenantId, LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}

    // ── Maintenance Window ──
    public record CreateMaintenanceWindowRequest(
            @NotBlank @Size(max = 200) String title,
            String description,
            @NotNull LocalDateTime startAt,
            @NotNull LocalDateTime endAt,
            String affectedComponents
    ) {}

    public record MaintenanceWindowResponse(
            Long id, String title, String description, LocalDateTime startAt,
            LocalDateTime endAt, String affectedComponents, String status,
            Long createdById, Long tenantId,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}
}
