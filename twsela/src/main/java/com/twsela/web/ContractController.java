package com.twsela.web;

import com.twsela.domain.Contract;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.ContractService;
import com.twsela.service.CustomPricingService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.ContractDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for contract management.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Contracts", description = "إدارة العقود")
public class ContractController {

    private final ContractService contractService;
    private final CustomPricingService pricingService;
    private final AuthenticationHelper authHelper;

    public ContractController(ContractService contractService,
                               CustomPricingService pricingService,
                               AuthenticationHelper authHelper) {
        this.contractService = contractService;
        this.pricingService = pricingService;
        this.authHelper = authHelper;
    }

    // ══════════════════════════════════════════════════════════
    // Admin Endpoints
    // ══════════════════════════════════════════════════════════

    @PostMapping("/admin/contracts")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "إنشاء عقد جديد")
    public ResponseEntity<ApiResponse<ContractResponse>> createContract(
            @Valid @RequestBody CreateContractRequest request,
            Authentication auth) {
        Long createdById = authHelper.getCurrentUserId(auth);
        Contract contract = contractService.createContract(
                request.contractType(), request.partyId(),
                request.startDate(), request.endDate(),
                request.autoRenew(), request.renewalNoticeDays(),
                request.termsDocument(), request.notes(), createdById);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(contract), "تم إنشاء العقد"));
    }

    @GetMapping("/admin/contracts")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "عرض جميع العقود")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getAllContracts() {
        List<ContractResponse> contracts = contractService.getAllContracts().stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(contracts));
    }

    @GetMapping("/admin/contracts/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "تفاصيل عقد")
    public ResponseEntity<ApiResponse<ContractResponse>> getContract(@PathVariable Long id) {
        Contract contract = contractService.findById(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(contract)));
    }

    @PutMapping("/admin/contracts/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "تعديل عقد مسودة")
    public ResponseEntity<ApiResponse<ContractResponse>> updateContract(
            @PathVariable Long id, @Valid @RequestBody UpdateContractRequest request) {
        Contract contract = contractService.updateDraft(id,
                request.startDate(), request.endDate(),
                request.autoRenew(), request.renewalNoticeDays(),
                request.termsDocument(), request.notes());
        return ResponseEntity.ok(ApiResponse.ok(toResponse(contract), "تم تحديث العقد"));
    }

    @PostMapping("/admin/contracts/{id}/send-signature")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "إرسال العقد للتوقيع الإلكتروني")
    public ResponseEntity<ApiResponse<ContractResponse>> sendForSignature(@PathVariable Long id) {
        Contract contract = contractService.sendForSignature(id);
        return ResponseEntity.ok(ApiResponse.ok(toResponse(contract), "تم إرسال رمز التحقق للتوقيع"));
    }

    @PutMapping("/admin/contracts/{id}/terminate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "إنهاء عقد")
    public ResponseEntity<ApiResponse<ContractResponse>> terminateContract(
            @PathVariable Long id, @Valid @RequestBody TerminateContractRequest request) {
        Contract contract = contractService.terminateContract(id, request.reason());
        return ResponseEntity.ok(ApiResponse.ok(toResponse(contract), "تم إنهاء العقد"));
    }

    @GetMapping("/admin/contracts/expiring")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "عقود تنتهي قريباً")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getExpiringContracts(
            @RequestParam(defaultValue = "30") int days) {
        List<ContractResponse> contracts = contractService.getExpiringContracts(days).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(contracts));
    }

    // ══════════════════════════════════════════════════════════
    // Authenticated (Merchant/Courier) Endpoints
    // ══════════════════════════════════════════════════════════

    @PostMapping("/contracts/{id}/sign")
    @Operation(summary = "توقيع العقد إلكترونياً")
    public ResponseEntity<ApiResponse<ContractResponse>> signContract(
            @PathVariable Long id, @Valid @RequestBody SignContractRequest request) {
        Contract contract = contractService.signContract(id, request.otp());
        return ResponseEntity.ok(ApiResponse.ok(toResponse(contract), "تم توقيع العقد بنجاح"));
    }

    @GetMapping("/contracts/my")
    @Operation(summary = "عقودي")
    public ResponseEntity<ApiResponse<List<ContractResponse>>> getMyContracts(Authentication auth) {
        Long userId = authHelper.getCurrentUserId(auth);
        List<ContractResponse> contracts = contractService.getContractsByParty(userId).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(contracts));
    }

    // ── Mapper ──────────────────────────────────────────────

    private ContractResponse toResponse(Contract c) {
        int rulesCount = pricingService.getPricingRules(c.getId()).size();
        return new ContractResponse(
                c.getId(), c.getContractNumber(), c.getContractType(),
                c.getParty().getId(), c.getParty().getName(),
                c.getStartDate(), c.getEndDate(), c.getStatus(),
                c.isAutoRenew(), c.getSignedAt(), rulesCount, c.getCreatedAt());
    }
}
