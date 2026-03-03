package com.twsela.web.dto;

import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import com.twsela.domain.ECommerceOrder.OrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * DTOs for API Key Management and E-Commerce Integration endpoints.
 */
public class DeveloperDTO {

    // ═══════════════════════════════════════
    // API Key DTOs
    // ═══════════════════════════════════════

    public record CreateApiKeyRequest(
            @NotBlank String name,
            String scopes
    ) {}

    public record ApiKeyResponse(
            Long id,
            String name,
            String keyValue,
            String scopes,
            int rateLimit,
            boolean active,
            Instant lastUsedAt,
            long requestCount,
            Instant createdAt
    ) {}

    public record ApiKeyCreatedResponse(
            Long id,
            String keyValue,
            String secret
    ) {}

    public record ApiKeyUsageResponse(
            Long keyId,
            String keyValue,
            long totalRequests,
            long totalLifetimeRequests
    ) {}

    // ═══════════════════════════════════════
    // E-Commerce DTOs
    // ═══════════════════════════════════════

    public record ConnectStoreRequest(
            @NotNull ECommercePlatform platform,
            @NotBlank String storeUrl,
            String storeName,
            @NotBlank String accessToken,
            String webhookSecret,
            Long defaultZoneId
    ) {}

    public record ConnectionResponse(
            Long id,
            ECommercePlatform platform,
            String storeName,
            String storeUrl,
            boolean active,
            boolean autoCreateShipments,
            Instant lastSyncAt,
            int syncErrors,
            Instant createdAt
    ) {}

    public record ECommerceOrderResponse(
            Long id,
            String externalOrderId,
            String externalOrderNumber,
            ECommercePlatform platform,
            OrderStatus status,
            Long shipmentId,
            Instant receivedAt,
            Instant processedAt,
            String errorMessage
    ) {}
}
