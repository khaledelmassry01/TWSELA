package com.twsela.web;

import com.twsela.domain.ComplianceReport;
import com.twsela.domain.ComplianceRule;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.ComplianceService;
import com.twsela.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * متحكم الامتثال الأمني.
 */
@RestController
@RequestMapping("/api/compliance")
@Tag(name = "Compliance", description = "إدارة قواعد الامتثال والتقارير")
public class ComplianceController {

    private final ComplianceService complianceService;
    private final AuthenticationHelper authHelper;

    public ComplianceController(ComplianceService complianceService,
                                 AuthenticationHelper authHelper) {
        this.complianceService = complianceService;
        this.authHelper = authHelper;
    }

    @GetMapping("/rules")
    @Operation(summary = "قواعد الامتثال")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ComplianceRule>>> getRules() {
        List<ComplianceRule> rules = complianceService.getAllRules();
        return ResponseEntity.ok(ApiResponse.ok(rules));
    }

    @PostMapping("/check")
    @Operation(summary = "تشغيل فحص امتثال")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplianceReport>> runCheck(Authentication authentication) {
        Long userId = authHelper.getCurrentUserId(authentication);
        ComplianceReport report = complianceService.runComplianceCheck(userId);
        return ResponseEntity.ok(ApiResponse.ok(report, "تم تشغيل فحص الامتثال بنجاح"));
    }

    @GetMapping("/reports/{id}")
    @Operation(summary = "تقرير امتثال مفصل")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplianceReport>> getReport(@PathVariable Long id) {
        ComplianceReport report = complianceService.getReportById(id);
        return ResponseEntity.ok(ApiResponse.ok(report));
    }

    @GetMapping("/status")
    @Operation(summary = "حالة الامتثال العامة")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        Map<String, Object> status = complianceService.getComplianceStatus();
        return ResponseEntity.ok(ApiResponse.ok(status));
    }

    @GetMapping("/reports")
    @Operation(summary = "قائمة تقارير الامتثال")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<ComplianceReport>> getLatestReport() {
        ComplianceReport report = complianceService.getLatestReport();
        return ResponseEntity.ok(ApiResponse.ok(report));
    }
}
