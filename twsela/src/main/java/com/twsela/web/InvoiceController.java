package com.twsela.web;

import com.twsela.domain.Invoice;
import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.InvoiceService;
import com.twsela.service.PaymentService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.SubscriptionDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for invoice and payment operations.
 */
@RestController
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "إدارة الفواتير والمدفوعات")
public class InvoiceController {

    private static final Logger log = LoggerFactory.getLogger(InvoiceController.class);

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final AuthenticationHelper authHelper;

    public InvoiceController(InvoiceService invoiceService,
                              PaymentService paymentService,
                              AuthenticationHelper authHelper) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
        this.authHelper = authHelper;
    }

    /**
     * Get my invoices (merchant).
     */
    @Operation(summary = "فواتيري", description = "الحصول على فواتيري")
    @GetMapping
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<Page<InvoiceResponse>>> getMyInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        Long merchantId = authHelper.getCurrentUserId(authentication);
        Page<Invoice> invoices = invoiceService.getInvoicesByMerchant(merchantId, PageRequest.of(page, size));
        Page<InvoiceResponse> response = invoices.map(this::toInvoiceResponse);
        return ResponseEntity.ok(ApiResponse.ok(response, "تم جلب الفواتير بنجاح"));
    }

    /**
     * Get invoice by ID.
     */
    @Operation(summary = "تفاصيل فاتورة", description = "الحصول على تفاصيل فاتورة")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT') or hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoice(id);
        return ResponseEntity.ok(ApiResponse.ok(toInvoiceResponse(invoice), "تم جلب الفاتورة بنجاح"));
    }

    /**
     * Pay an invoice.
     */
    @Operation(summary = "دفع فاتورة", description = "دفع فاتورة عبر بوابة الدفع")
    @PostMapping("/{id}/pay")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<ApiResponse<String>> payInvoice(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request,
            Authentication authentication) {
        Long merchantId = authHelper.getCurrentUserId(authentication);
        Invoice invoice = invoiceService.getInvoice(id);
        String currency = request.currency() != null ? request.currency() : "EGP";

        paymentService.initiatePayment(
                id, merchantId,
                PaymentGatewayType.valueOf(request.gateway().toUpperCase()),
                invoice.getTotalAmount(),
                currency
        );
        return ResponseEntity.ok(ApiResponse.ok("تم الدفع بنجاح", "OK"));
    }

    /**
     * Admin: Get all invoices by status.
     */
    @Operation(summary = "فواتير حسب الحالة", description = "الحصول على الفواتير حسب الحالة (مدير)")
    @GetMapping("/admin")
    @PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<InvoiceResponse>>> getInvoicesByStatus(
            @RequestParam Invoice.InvoiceStatus status) {
        List<Invoice> invoices = invoiceService.getInvoicesByStatus(status);
        List<InvoiceResponse> response = invoices.stream().map(this::toInvoiceResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(response, "تم جلب الفواتير بنجاح"));
    }

    /**
     * Admin: Refund an invoice.
     */
    @Operation(summary = "استرداد فاتورة", description = "استرداد مبلغ فاتورة (مدير)")
    @PostMapping("/admin/{id}/refund")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> refundInvoice(@PathVariable Long id) {
        Invoice invoice = invoiceService.refundInvoice(id);
        return ResponseEntity.ok(ApiResponse.ok(toInvoiceResponse(invoice), "تم الاسترداد بنجاح"));
    }

    // ── Mapper ──────────────────────────────────────────────

    private InvoiceResponse toInvoiceResponse(Invoice inv) {
        List<InvoiceItemResponse> items = inv.getItems() != null
                ? inv.getItems().stream()
                    .map(item -> new InvoiceItemResponse(
                            item.getId(),
                            item.getDescription(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getTotalPrice()))
                    .toList()
                : List.of();

        return new InvoiceResponse(
                inv.getId(),
                inv.getInvoiceNumber(),
                inv.getSubscription() != null ? inv.getSubscription().getId() : null,
                inv.getAmount(),
                inv.getTax(),
                inv.getTotalAmount(),
                inv.getStatus(),
                inv.getDueDate(),
                inv.getPaidAt(),
                inv.getPaymentGateway(),
                items,
                inv.getCreatedAt()
        );
    }
}
