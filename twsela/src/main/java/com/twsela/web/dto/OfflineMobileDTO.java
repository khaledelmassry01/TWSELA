package com.twsela.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public final class OfflineMobileDTO {

    private OfflineMobileDTO() {}

    // OfflineQueue
    public record CreateOfflineQueueRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 30) String operationType,
        @NotBlank String payload,
        Integer priority
    ) {}

    public record OfflineQueueResponse(
        Long id, Long userId, String operationType, String payload,
        Integer priority, String status, LocalDateTime createdOfflineAt,
        LocalDateTime syncedAt, String errorMessage, Long tenantId, LocalDateTime createdAt
    ) {}

    // SyncSession
    public record StartSyncSessionRequest(
        @NotNull Long userId,
        String deviceId
    ) {}

    public record SyncSessionResponse(
        Long id, Long userId, String deviceId, LocalDateTime startedAt,
        LocalDateTime completedAt, Integer itemsSynced, Integer itemsFailed,
        String status, Long tenantId, LocalDateTime createdAt
    ) {}

    // SyncConflict
    public record SyncConflictResponse(
        Long id, Long syncSessionId, String entityType, Long entityId,
        String localData, String serverData, String resolution,
        LocalDateTime resolvedAt, LocalDateTime createdAt
    ) {}

    public record ResolveSyncConflictRequest(
        @NotBlank @Size(max = 20) String resolution
    ) {}

    // DeviceRegistration
    public record RegisterDeviceRequest(
        @NotNull Long userId,
        @NotBlank @Size(max = 100) String deviceId,
        @NotBlank @Size(max = 20) String platform,
        String osVersion,
        String appVersion,
        String pushToken
    ) {}

    public record DeviceRegistrationResponse(
        Long id, Long userId, String deviceId, String platform,
        String osVersion, String appVersion, String pushToken,
        LocalDateTime lastActiveAt, Long tenantId, LocalDateTime createdAt
    ) {}

    // BatteryOptimizationConfig
    public record CreateBatteryConfigRequest(
        @NotBlank @Size(max = 100) String name,
        Integer batteryThreshold,
        Integer locationIntervalSeconds,
        Integer pingIntervalSeconds,
        Integer syncIntervalSeconds
    ) {}

    public record BatteryConfigResponse(
        Long id, String name, Integer batteryThreshold,
        Integer locationIntervalSeconds, Integer pingIntervalSeconds,
        Integer syncIntervalSeconds, LocalDateTime createdAt
    ) {}

    // AppVersionConfig
    public record CreateAppVersionConfigRequest(
        @NotBlank @Size(max = 20) String platform,
        @NotBlank @Size(max = 20) String minVersion,
        @NotBlank @Size(max = 20) String currentVersion,
        String updateUrl,
        Boolean forceUpdate,
        String releaseNotes
    ) {}

    public record AppVersionConfigResponse(
        Long id, String platform, String minVersion, String currentVersion,
        String updateUrl, Boolean forceUpdate, String releaseNotes, LocalDateTime createdAt
    ) {}
}
