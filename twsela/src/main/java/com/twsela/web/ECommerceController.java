package com.twsela.web;

import com.twsela.domain.ECommerceConnection;
import com.twsela.domain.ECommerceOrder;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.ECommerceService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.DeveloperDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for e-commerce store integrations.
 */
@RestController
@RequestMapping("/api/integrations")
@Tag(name = "E-Commerce Integrations", description = "تكامل منصات التجارة الإلكترونية")
public class ECommerceController {

    private final ECommerceService eCommerceService;
    private final AuthenticationHelper authHelper;

    public ECommerceController(ECommerceService eCommerceService, AuthenticationHelper authHelper) {
        this.eCommerceService = eCommerceService;
        this.authHelper = authHelper;
    }

    @PostMapping("/connect")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")
    @Operation(summary = "ربط متجر إلكتروني")
    public ResponseEntity<ApiResponse<ConnectionResponse>> connectStore(
            @Valid @RequestBody ConnectStoreRequest request,
            Authentication auth) {
        Long merchantId = authHelper.getCurrentUserId(auth);
        ECommerceConnection conn = eCommerceService.connectStore(
                merchantId, request.platform(), request.storeUrl(), request.storeName(),
                request.accessToken(), request.webhookSecret(), request.defaultZoneId());
        return ResponseEntity.ok(ApiResponse.ok(toConnectionResponse(conn), "تم ربط المتجر بنجاح"));
    }

    @GetMapping("/connections")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")
    @Operation(summary = "عرض اتصالاتي")
    public ResponseEntity<ApiResponse<List<ConnectionResponse>>> getMyConnections(Authentication auth) {
        Long merchantId = authHelper.getCurrentUserId(auth);
        List<ConnectionResponse> connections = eCommerceService.getConnectionsByMerchant(merchantId).stream()
                .map(this::toConnectionResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(connections));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")
    @Operation(summary = "فصل متجر")
    public ResponseEntity<ApiResponse<Void>> disconnectStore(@PathVariable Long id) {
        eCommerceService.disconnectStore(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "تم فصل المتجر"));
    }

    @GetMapping("/{id}/orders")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")
    @Operation(summary = "طلبات المنصة")
    public ResponseEntity<ApiResponse<List<ECommerceOrderResponse>>> getOrders(@PathVariable Long id) {
        List<ECommerceOrderResponse> orders = eCommerceService.getOrdersByConnection(id).stream()
                .map(this::toOrderResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(orders));
    }

    @PostMapping("/{id}/retry")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")
    @Operation(summary = "إعادة محاولة الطلبات الفاشلة")
    public ResponseEntity<ApiResponse<Map<String, Object>>> retryFailed(@PathVariable Long id) {
        int retried = eCommerceService.retryFailedOrders(id);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("retriedCount", retried), "تمت إعادة المحاولة"));
    }

    @GetMapping("/{id}/stats")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")
    @Operation(summary = "إحصائيات الاتصال")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(@PathVariable Long id) {
        Map<String, Object> stats = eCommerceService.getConnectionStats(id);
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    private ConnectionResponse toConnectionResponse(ECommerceConnection c) {
        return new ConnectionResponse(c.getId(), c.getPlatform(), c.getStoreName(),
                c.getStoreUrl(), c.isActive(), c.isAutoCreateShipments(),
                c.getLastSyncAt(), c.getSyncErrors(), c.getCreatedAt());
    }

    private ECommerceOrderResponse toOrderResponse(ECommerceOrder o) {
        return new ECommerceOrderResponse(o.getId(), o.getExternalOrderId(),
                o.getExternalOrderNumber(), o.getPlatform(), o.getStatus(),
                o.getShipment() != null ? o.getShipment().getId() : null,
                o.getReceivedAt(), o.getProcessedAt(), o.getErrorMessage());
    }
}
