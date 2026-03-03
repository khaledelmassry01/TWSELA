package com.twsela.web;

import com.twsela.domain.ECommerceOrder;
import com.twsela.service.ECommerceService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.DeveloperDTO.ECommerceOrderResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for receiving e-commerce platform webhooks.
 * These endpoints are permit-all — authentication is done via webhook signature verification.
 */
@RestController
@RequestMapping("/api/ecommerce/webhook")
@Tag(name = "E-Commerce Webhooks", description = "استقبال إشعارات منصات التجارة")
public class ECommerceWebhookController {

    private final ECommerceService eCommerceService;

    public ECommerceWebhookController(ECommerceService eCommerceService) {
        this.eCommerceService = eCommerceService;
    }

    @PostMapping("/shopify/{connectionId}")
    @Operation(summary = "استقبال Webhook من Shopify")
    public ResponseEntity<ApiResponse<ECommerceOrderResponse>> shopifyWebhook(
            @PathVariable Long connectionId,
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Shopify-Hmac-SHA256", required = false) String signature) {
        ECommerceOrder order = eCommerceService.processIncomingOrder(connectionId, rawPayload, signature);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(order)));
    }

    @PostMapping("/woocommerce/{connectionId}")
    @Operation(summary = "استقبال Webhook من WooCommerce")
    public ResponseEntity<ApiResponse<ECommerceOrderResponse>> wooCommerceWebhook(
            @PathVariable Long connectionId,
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-WC-Webhook-Signature", required = false) String signature) {
        ECommerceOrder order = eCommerceService.processIncomingOrder(connectionId, rawPayload, signature);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(order)));
    }

    @PostMapping("/salla/{connectionId}")
    @Operation(summary = "استقبال Webhook من سلة")
    public ResponseEntity<ApiResponse<ECommerceOrderResponse>> sallaWebhook(
            @PathVariable Long connectionId,
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Salla-Signature", required = false) String signature) {
        ECommerceOrder order = eCommerceService.processIncomingOrder(connectionId, rawPayload, signature);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(order)));
    }

    @PostMapping("/zid/{connectionId}")
    @Operation(summary = "استقبال Webhook من زد")
    public ResponseEntity<ApiResponse<ECommerceOrderResponse>> zidWebhook(
            @PathVariable Long connectionId,
            @RequestBody String rawPayload,
            @RequestHeader(value = "X-Zid-Signature", required = false) String signature) {
        ECommerceOrder order = eCommerceService.processIncomingOrder(connectionId, rawPayload, signature);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(order)));
    }

    private ECommerceOrderResponse toResponse(ECommerceOrder o) {
        return new ECommerceOrderResponse(o.getId(), o.getExternalOrderId(),
                o.getExternalOrderNumber(), o.getPlatform(), o.getStatus(),
                o.getShipment() != null ? o.getShipment().getId() : null,
                o.getReceivedAt(), o.getProcessedAt(), o.getErrorMessage());
    }
}
