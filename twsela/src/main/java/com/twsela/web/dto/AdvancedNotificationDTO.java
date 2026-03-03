package com.twsela.web.dto;

import com.twsela.domain.DeviceToken;
import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationPreference.DigestMode;
import com.twsela.domain.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * DTOs for the advanced notification system — preferences, templates, analytics (Sprint 27).
 */
public class AdvancedNotificationDTO {

    // ── Preferences ────────────────────────────────────

    public record PreferenceRequest(
            Map<String, List<String>> enabledChannels,
            LocalTime quietHoursStart,
            LocalTime quietHoursEnd,
            DigestMode digestMode
    ) {}

    public record PreferenceResponse(
            Long id,
            Long userId,
            Map<String, List<String>> enabledChannels,
            LocalTime quietHoursStart,
            LocalTime quietHoursEnd,
            DigestMode digestMode,
            Instant pausedUntil,
            Instant updatedAt
    ) {}

    public record PauseRequest(
            @NotNull Instant pauseUntil
    ) {}

    // ── Device Tokens ──────────────────────────────────

    public record RegisterDeviceRequest(
            @NotBlank String token,
            @NotNull DeviceToken.Platform platform
    ) {}

    public record DeviceTokenResponse(
            Long id,
            String token,
            DeviceToken.Platform platform,
            boolean active,
            Instant lastUsedAt
    ) {}

    // ── Templates ──────────────────────────────────────

    public record TemplateResponse(
            Long id,
            NotificationType eventType,
            NotificationChannel channel,
            String subjectTemplate,
            String bodyTemplateAr,
            String bodyTemplateEn,
            boolean active,
            Instant updatedAt
    ) {}

    public record UpdateTemplateRequest(
            String subjectTemplate,
            String bodyTemplateAr,
            String bodyTemplateEn,
            Boolean active
    ) {}

    public record TestNotificationRequest(
            @NotNull Long recipientUserId,
            Map<String, String> templateVars
    ) {}

    // ── Analytics ──────────────────────────────────────

    public record DeliveryStatsResponse(
            long totalSent,
            long totalDelivered,
            long totalFailed,
            long totalBounced,
            Map<String, Map<String, Long>> channelBreakdown
    ) {}
}
