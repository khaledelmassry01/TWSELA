package com.twsela.web;

import com.twsela.domain.PaymentRefund;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.PaymentRefundService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.CreateRefundRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * متحكم الاستردادات — إنشاء وموافقة ورفض طلبات الاسترداد.
 */
@RestController
@RequestMapping("/api/payments/refunds")
@Tag(name = "Payment Refunds", description = "إدارة طلبات استرداد المدفوعات")
public class PaymentRefundController {

    private final PaymentRefundService paymentRefundService;
    private final AuthenticationHelper authHelper;

    public PaymentRefundController(PaymentRefundService paymentRefundService,
                                    AuthenticationHelper authHelper) {
        this.paymentRefundService = paymentRefundService;
        this.authHelper = authHelper;
    }

    @PostMapping
    @Operation(summary = "إنشاء طلب استرداد")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createRefund(
            @Valid @RequestBody CreateRefundRequest request) {
        PaymentRefund refund = paymentRefundService.createRefund(
                request.getPaymentIntentId(), request.getAmount(), request.getReason());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("refundId", refund.getId());
        data.put("status", refund.getStatus().name());
        data.put("amount", refund.getAmount());
        return ResponseEntity.ok(ApiResponse.ok(data, "تم إنشاء طلب الاسترداد بنجاح"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "حالة طلب الاسترداد")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getRefund(@PathVariable Long id) {
        PaymentRefund refund = paymentRefundService.getById(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("refundId", refund.getId());
        data.put("status", refund.getStatus().name());
        data.put("amount", refund.getAmount());
        data.put("reason", refund.getReason());
        data.put("providerRef", refund.getProviderRef());
        data.put("processedAt", refund.getProcessedAt());
        data.put("createdAt", refund.getCreatedAt());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @GetMapping
    @Operation(summary = "قائمة طلبات الاسترداد المعلقة")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<PaymentRefund>>> getPendingRefunds() {
        List<PaymentRefund> refunds = paymentRefundService.getPendingRefunds();
        return ResponseEntity.ok(ApiResponse.ok(refunds));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "الموافقة على طلب استرداد")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approveRefund(
            @PathVariable Long id, Authentication authentication) {
        Long userId = authHelper.getCurrentUserId(authentication);
        PaymentRefund refund = paymentRefundService.approveRefund(id, userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("refundId", refund.getId());
        data.put("status", refund.getStatus().name());
        data.put("providerRef", refund.getProviderRef());
        return ResponseEntity.ok(ApiResponse.ok(data, "تم الموافقة على طلب الاسترداد"));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "رفض طلب استرداد")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rejectRefund(
            @PathVariable Long id, @RequestParam(required = false) String reason) {
        PaymentRefund refund = paymentRefundService.rejectRefund(id, reason);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("refundId", refund.getId());
        data.put("status", refund.getStatus().name());
        return ResponseEntity.ok(ApiResponse.ok(data, "تم رفض طلب الاسترداد"));
    }
}
