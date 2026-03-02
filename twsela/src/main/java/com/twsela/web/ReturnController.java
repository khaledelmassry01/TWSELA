package com.twsela.web;

import com.twsela.domain.ReturnShipment;
import com.twsela.domain.ReturnShipment.ReturnStatusEnum;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.ReturnService;
import com.twsela.web.dto.ReturnRequestDTO;
import com.twsela.web.dto.ReturnResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for managing return shipments.
 */
@RestController
@RequestMapping("/api/returns")
@Tag(name = "Returns", description = "إدارة المرتجعات")
public class ReturnController {

    private static final Logger log = LoggerFactory.getLogger(ReturnController.class);

    private final ReturnService returnService;
    private final AuthenticationHelper authHelper;

    public ReturnController(ReturnService returnService, AuthenticationHelper authHelper) {
        this.returnService = returnService;
        this.authHelper = authHelper;
    }

    /**
     * Create a return request (Merchant or Admin).
     */
    @Operation(summary = "إنشاء طلب إرجاع")
    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<ReturnResponseDTO>> createReturn(
            @Valid @RequestBody ReturnRequestDTO request,
            Authentication authentication) {
        String createdBy = authHelper.getCurrentUser(authentication).getName();
        ReturnShipment ret = returnService.createReturn(
                request.getShipmentId(), request.getReason(), request.getNotes(), createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(com.twsela.web.dto.ApiResponse.ok(ReturnService.toDTO(ret), "تم إنشاء طلب الإرجاع"));
    }

    /**
     * Get all returns (role-based filtering).
     */
    @Operation(summary = "قائمة المرتجعات")
    @GetMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT') or hasRole('COURIER')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<List<ReturnResponseDTO>>> getReturns(
            Authentication authentication) {
        String role = authHelper.getCurrentUserRole(authentication);
        Long userId = authHelper.getCurrentUserId(authentication);

        List<ReturnShipment> returns;
        if ("MERCHANT".equalsIgnoreCase(role)) {
            returns = returnService.getReturnsByMerchant(userId);
        } else if ("COURIER".equalsIgnoreCase(role)) {
            returns = returnService.getReturnsByCourier(userId);
        } else {
            returns = returnService.getAllReturns();
        }

        List<ReturnResponseDTO> dtos = returns.stream()
                .map(ReturnService::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(dtos));
    }

    /**
     * Get return by ID.
     */
    @Operation(summary = "تفاصيل المرتجع")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('MERCHANT') or hasRole('COURIER')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<ReturnResponseDTO>> getReturn(@PathVariable Long id) {
        ReturnShipment ret = returnService.getReturnById(id);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(ReturnService.toDTO(ret)));
    }

    /**
     * Update return status (Admin/Owner).
     */
    @Operation(summary = "تحديث حالة المرتجع")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN') or hasRole('COURIER')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<ReturnResponseDTO>> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        ReturnStatusEnum newStatus = ReturnStatusEnum.valueOf(statusStr);
        ReturnShipment ret = returnService.updateStatus(id, newStatus);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(ReturnService.toDTO(ret), "تم تحديث حالة المرتجع"));
    }

    /**
     * Assign courier to return pickup (Admin/Owner).
     */
    @Operation(summary = "تعيين مندوب للمرتجع")
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<ReturnResponseDTO>> assignCourier(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long courierId = body.get("courierId");
        ReturnShipment ret = returnService.assignCourier(id, courierId);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(ReturnService.toDTO(ret), "تم تعيين المندوب"));
    }
}
