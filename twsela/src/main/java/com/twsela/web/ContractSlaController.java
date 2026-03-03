package com.twsela.web;

import com.twsela.domain.ContractSlaTerms;
import com.twsela.service.ContractSlaService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.ContractDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

/**
 * REST controller for contract SLA terms and compliance.
 */
@RestController
@RequestMapping("/api/admin/contracts")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
@Tag(name = "Contract SLA", description = "شروط مستوى الخدمة والالتزام")
public class ContractSlaController {

    private final ContractSlaService slaService;

    public ContractSlaController(ContractSlaService slaService) {
        this.slaService = slaService;
    }

    @GetMapping("/{contractId}/sla")
    @Operation(summary = "عرض شروط SLA للعقد")
    public ResponseEntity<ApiResponse<SlaTermsResponse>> getSlaTerms(@PathVariable Long contractId) {
        ContractSlaTerms sla = slaService.getSlaTerms(contractId)
                .orElse(null);
        if (sla == null) {
            return ResponseEntity.ok(ApiResponse.ok(null, "لا توجد شروط SLA لهذا العقد"));
        }
        return ResponseEntity.ok(ApiResponse.ok(toResponse(sla)));
    }

    @PutMapping("/{contractId}/sla")
    @Operation(summary = "تعديل شروط SLA للعقد")
    public ResponseEntity<ApiResponse<SlaTermsResponse>> updateSlaTerms(
            @PathVariable Long contractId,
            @Valid @RequestBody SlaTermsRequest request) {
        ContractSlaTerms sla = slaService.saveSlaTerms(contractId,
                request.targetDeliveryRate(), request.maxDeliveryHours(),
                request.latePenaltyPerShipment(), request.lostPenaltyFixed(),
                request.slaReviewPeriod());
        return ResponseEntity.ok(ApiResponse.ok(toResponse(sla), "تم تحديث شروط SLA"));
    }

    @GetMapping("/{contractId}/sla/compliance")
    @Operation(summary = "تقرير الالتزام بـ SLA")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSlaCompliance(
            @PathVariable Long contractId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Map<String, Object> report = slaService.checkSlaCompliance(contractId, from, to);
        return ResponseEntity.ok(ApiResponse.ok(report, "تقرير الالتزام"));
    }

    @GetMapping("/{contractId}/sla/penalties")
    @Operation(summary = "حساب غرامات مخالفات SLA")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSlapenalties(
            @PathVariable Long contractId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        Map<String, Object> penalties = slaService.calculatePenalties(contractId, from, to);
        return ResponseEntity.ok(ApiResponse.ok(penalties, "غرامات المخالفات"));
    }

    private SlaTermsResponse toResponse(ContractSlaTerms sla) {
        return new SlaTermsResponse(
                sla.getId(), sla.getContract().getId(),
                sla.getTargetDeliveryRate(), sla.getMaxDeliveryHours(),
                sla.getLatePenaltyPerShipment(), sla.getLostPenaltyFixed(),
                sla.getSlaReviewPeriod());
    }
}
