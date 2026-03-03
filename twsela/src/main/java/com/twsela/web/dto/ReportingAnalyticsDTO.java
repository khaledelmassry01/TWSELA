package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class ReportingAnalyticsDTO {

    private ReportingAnalyticsDTO() {}

    // ── Custom Report ──
    public record CreateCustomReportRequest(
            @NotBlank @Size(max = 100) String name,
            String description,
            @NotBlank @Size(max = 30) String reportType,
            String queryConfig,
            String columns,
            String filters,
            Boolean isPublic
    ) {}

    public record UpdateCustomReportRequest(
            @Size(max = 100) String name,
            String description,
            String queryConfig,
            String columns,
            String filters,
            Boolean isPublic
    ) {}

    public record CustomReportResponse(
            Long id, String name, String description, String reportType,
            String queryConfig, String columns, String filters,
            Long createdById, Boolean isPublic, Long tenantId,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}

    // ── Report Schedule ──
    public record CreateReportScheduleRequest(
            @NotNull Long customReportId,
            @NotBlank @Size(max = 50) String cronExpression,
            @Size(max = 10) String format,
            String recipients,
            Boolean enabled
    ) {}

    public record ReportScheduleResponse(
            Long id, Long customReportId, String cronExpression, String format,
            String recipients, Boolean enabled, LocalDateTime lastRunAt,
            LocalDateTime nextRunAt, Long tenantId,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}

    // ── Report Execution ──
    public record CreateReportExecutionRequest(
            @NotNull Long customReportId,
            Long scheduleId
    ) {}

    public record ReportExecutionResponse(
            Long id, Long customReportId, Long scheduleId, String status,
            LocalDateTime startedAt, LocalDateTime completedAt, String fileUrl,
            Long fileSize, Integer rowCount, Long tenantId, LocalDateTime createdAt
    ) {}

    // ── Saved Filter ──
    public record CreateSavedFilterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 30) String entityType,
            String filterConfig,
            Boolean isDefault
    ) {}

    public record SavedFilterResponse(
            Long id, String name, String entityType, String filterConfig,
            Long userId, Boolean isDefault, Long tenantId,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}

    // ── Data Export Job ──
    public record CreateDataExportJobRequest(
            @NotBlank @Size(max = 30) String entityType,
            String filters,
            @Size(max = 10) String format
    ) {}

    public record DataExportJobResponse(
            Long id, String entityType, String filters, String format,
            String status, Long requestedById, String fileUrl, Long fileSize,
            LocalDateTime expiresAt, Long tenantId,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}

    // ── Data Pipeline Config ──
    public record CreateDataPipelineConfigRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 30) String sourceType,
            String sourceConfig,
            String transformRules,
            @NotBlank @Size(max = 30) String destinationType,
            String destConfig,
            @Size(max = 50) String schedule,
            Boolean isActive
    ) {}

    public record DataPipelineConfigResponse(
            Long id, String name, String sourceType, String sourceConfig,
            String transformRules, String destinationType, String destConfig,
            String schedule, Boolean isActive, Long tenantId,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}

    // ── Pipeline Execution ──
    public record PipelineExecutionResponse(
            Long id, Long pipelineConfigId, String status,
            Integer recordsProcessed, Integer recordsFailed,
            LocalDateTime startedAt, LocalDateTime completedAt,
            String errorMessage, Long tenantId, LocalDateTime createdAt
    ) {}

    // ── Report Widget ──
    public record CreateReportWidgetRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Size(max = 30) String reportType,
            @Size(max = 20) String chartType,
            String queryConfig,
            Integer displayOrder,
            @Size(max = 50) String dashboardId
    ) {}

    public record ReportWidgetResponse(
            Long id, String name, String reportType, String chartType,
            String queryConfig, Integer displayOrder, String dashboardId,
            Long userId, Long tenantId,
            LocalDateTime createdAt, LocalDateTime updatedAt
    ) {}
}
