package com.twsela.web;

import com.twsela.domain.SettlementBatch;
import com.twsela.domain.SettlementItem;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.SettlementService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.GenerateSettlementRequest;
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
 * متحكم التسويات المالية.
 */
@RestController
@RequestMapping("/api/settlements")
@Tag(name = "Settlements", description = "إدارة دفعات التسوية المالية")
public class SettlementController {

    private final SettlementService settlementService;
    private final AuthenticationHelper authHelper;

    public SettlementController(SettlementService settlementService,
                                 AuthenticationHelper authHelper) {
        this.settlementService = settlementService;
        this.authHelper = authHelper;
    }

    @GetMapping
    @Operation(summary = "قائمة التسويات")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SettlementBatch>>> getBatches(
            @RequestParam(required = false) String status) {
        List<SettlementBatch> batches;
        if (status != null) {
            SettlementBatch.BatchStatus batchStatus = SettlementBatch.BatchStatus.valueOf(status.toUpperCase());
            batches = settlementService.getBatchesByStatus(batchStatus);
        } else {
            batches = settlementService.getBatchesByStatus(SettlementBatch.BatchStatus.DRAFT);
        }
        return ResponseEntity.ok(ApiResponse.ok(batches));
    }

    @GetMapping("/{id}")
    @Operation(summary = "تفاصيل التسوية")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBatch(@PathVariable Long id) {
        SettlementBatch batch = settlementService.getBatchById(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("batchId", batch.getId());
        data.put("settlementNumber", batch.getSettlementNumber());
        data.put("period", batch.getPeriod().name());
        data.put("startDate", batch.getStartDate());
        data.put("endDate", batch.getEndDate());
        data.put("totalTransactions", batch.getTotalTransactions());
        data.put("totalAmount", batch.getTotalAmount());
        data.put("totalFees", batch.getTotalFees());
        data.put("netAmount", batch.getNetAmount());
        data.put("status", batch.getStatus().name());
        data.put("processedAt", batch.getProcessedAt());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PostMapping("/generate")
    @Operation(summary = "إنشاء تسوية يدوية")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generateBatch(
            @Valid @RequestBody GenerateSettlementRequest request,
            Authentication authentication) {
        Long userId = authHelper.getCurrentUserId(authentication);
        SettlementBatch.SettlementPeriod period = SettlementBatch.SettlementPeriod.valueOf(request.getPeriod().toUpperCase());

        SettlementBatch batch = settlementService.generateBatch(period, request.getStartDate(), request.getEndDate(), userId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("batchId", batch.getId());
        data.put("settlementNumber", batch.getSettlementNumber());
        data.put("status", batch.getStatus().name());
        return ResponseEntity.ok(ApiResponse.ok(data, "تم إنشاء التسوية بنجاح"));
    }

    @GetMapping("/{id}/items")
    @Operation(summary = "بنود التسوية")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<SettlementItem>>> getBatchItems(@PathVariable Long id) {
        List<SettlementItem> items = settlementService.getBatchItems(id);
        return ResponseEntity.ok(ApiResponse.ok(items));
    }

    @PostMapping("/{id}/process")
    @Operation(summary = "تنفيذ التسوية")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> processBatch(@PathVariable Long id) {
        SettlementBatch batch = settlementService.processBatch(id);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("batchId", batch.getId());
        data.put("status", batch.getStatus().name());
        data.put("netAmount", batch.getNetAmount());
        data.put("processedAt", batch.getProcessedAt());
        return ResponseEntity.ok(ApiResponse.ok(data, "تم معالجة التسوية"));
    }
}
