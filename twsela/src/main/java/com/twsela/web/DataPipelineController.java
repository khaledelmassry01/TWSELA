package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.DataPipelineService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.ReportingAnalyticsDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/data")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class DataPipelineController {

    private final DataPipelineService dataPipelineService;
    private final AuthenticationHelper authenticationHelper;

    public DataPipelineController(DataPipelineService dataPipelineService,
                                  AuthenticationHelper authenticationHelper) {
        this.dataPipelineService = dataPipelineService;
        this.authenticationHelper = authenticationHelper;
    }

    // ── Data Export Jobs ──

    @GetMapping("/exports")
    public ResponseEntity<ApiResponse<List<DataExportJob>>> getAllExportJobs(
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(dataPipelineService.getAllExportJobs(tenantId)));
    }

    @GetMapping("/exports/{id}")
    public ResponseEntity<ApiResponse<DataExportJob>> getExportJobById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(dataPipelineService.getExportJobById(id)));
    }

    @PostMapping("/exports")
    public ResponseEntity<ApiResponse<DataExportJob>> createExportJob(
            @Valid @RequestBody CreateDataExportJobRequest request,
            @RequestParam(required = false) Long tenantId,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(
                dataPipelineService.createExportJob(request, userId, tenantId), "Data export job created"));
    }

    // ── Data Pipeline Configs ──

    @GetMapping("/pipelines")
    public ResponseEntity<ApiResponse<List<DataPipelineConfig>>> getAllPipelineConfigs(
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(dataPipelineService.getAllPipelineConfigs(tenantId)));
    }

    @GetMapping("/pipelines/{id}")
    public ResponseEntity<ApiResponse<DataPipelineConfig>> getPipelineConfigById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(dataPipelineService.getPipelineConfigById(id)));
    }

    @PostMapping("/pipelines")
    public ResponseEntity<ApiResponse<DataPipelineConfig>> createPipelineConfig(
            @Valid @RequestBody CreateDataPipelineConfigRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(
                dataPipelineService.createPipelineConfig(request, tenantId), "Data pipeline config created"));
    }

    @DeleteMapping("/pipelines/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePipelineConfig(@PathVariable Long id) {
        dataPipelineService.deletePipelineConfig(id);
        return ResponseEntity.ok(ApiResponse.ok("Data pipeline config deleted"));
    }

    // ── Pipeline Executions ──

    @GetMapping("/pipelines/{pipelineId}/executions")
    public ResponseEntity<ApiResponse<List<PipelineExecution>>> getExecutions(
            @PathVariable Long pipelineId) {
        return ResponseEntity.ok(ApiResponse.ok(dataPipelineService.getExecutionsByPipeline(pipelineId)));
    }

    @PostMapping("/pipelines/{pipelineId}/executions")
    public ResponseEntity<ApiResponse<PipelineExecution>> startExecution(
            @PathVariable Long pipelineId,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(
                dataPipelineService.startExecution(pipelineId, tenantId), "Pipeline execution started"));
    }

    // ── Report Widgets ──

    @GetMapping("/widgets")
    public ResponseEntity<ApiResponse<List<ReportWidget>>> getWidgetsByDashboard(
            @RequestParam String dashboardId) {
        return ResponseEntity.ok(ApiResponse.ok(dataPipelineService.getWidgetsByDashboard(dashboardId)));
    }

    @PostMapping("/widgets")
    public ResponseEntity<ApiResponse<ReportWidget>> createWidget(
            @Valid @RequestBody CreateReportWidgetRequest request,
            @RequestParam(required = false) Long tenantId,
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(
                dataPipelineService.createWidget(request, userId, tenantId), "Report widget created"));
    }

    @DeleteMapping("/widgets/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWidget(@PathVariable Long id) {
        dataPipelineService.deleteWidget(id);
        return ResponseEntity.ok(ApiResponse.ok("Report widget deleted"));
    }
}
