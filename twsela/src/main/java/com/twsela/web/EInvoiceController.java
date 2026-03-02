package com.twsela.web;

import com.twsela.service.EInvoiceService;
import com.twsela.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/einvoice")
@Tag(name = "E-Invoice", description = "الفوترة الإلكترونية")
public class EInvoiceController {

    private final EInvoiceService eInvoiceService;

    public EInvoiceController(EInvoiceService eInvoiceService) {
        this.eInvoiceService = eInvoiceService;
    }

    @PostMapping("/generate/{invoiceId}")
    @Operation(summary = "إنشاء فاتورة إلكترونية")
    public ResponseEntity<ApiResponse<?>> generate(
            @PathVariable Long invoiceId,
            @RequestParam String countryCode) {
        return ResponseEntity.ok(ApiResponse.ok(eInvoiceService.generateEInvoice(invoiceId, countryCode), "تم إنشاء الفاتورة الإلكترونية"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "جلب فاتورة إلكترونية")
    public ResponseEntity<ApiResponse<?>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(eInvoiceService.getById(id), "الفاتورة الإلكترونية"));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "تقديم فاتورة إلكترونية للجهة الحكومية")
    public ResponseEntity<ApiResponse<?>> submit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(eInvoiceService.submitToGovernment(id), "تم تقديم الفاتورة"));
    }

    @GetMapping("/pending")
    @Operation(summary = "الفواتير الإلكترونية المعلقة")
    public ResponseEntity<ApiResponse<?>> pending() {
        return ResponseEntity.ok(ApiResponse.ok(eInvoiceService.getPendingInvoices(), "الفواتير المعلقة"));
    }
}
