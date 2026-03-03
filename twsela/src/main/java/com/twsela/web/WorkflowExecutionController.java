package com.twsela.web;

import com.twsela.domain.WorkflowExecution;
import com.twsela.domain.WorkflowStepExecution;
import com.twsela.service.WorkflowExecutionService;
import com.twsela.service.WorkflowStepExecutionService;
import com.twsela.web.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflow-executions")
public class WorkflowExecutionController {

    private final WorkflowExecutionService executionService;
    private final WorkflowStepExecutionService stepExecutionService;

    public WorkflowExecutionController(WorkflowExecutionService executionService,
                                       WorkflowStepExecutionService stepExecutionService) {
        this.executionService = executionService;
        this.stepExecutionService = stepExecutionService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<WorkflowExecution>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(executionService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<WorkflowExecution>>> getByStatus(
            @RequestParam(required = false) WorkflowExecution.ExecutionStatus status) {
        if (status != null) {
            return ResponseEntity.ok(ApiResponse.ok(executionService.findByStatus(status)));
        }
        return ResponseEntity.ok(ApiResponse.ok(executionService.findByStatus(WorkflowExecution.ExecutionStatus.RUNNING)));
    }

    @GetMapping("/by-definition/{definitionId}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<WorkflowExecution>>> getByDefinition(@PathVariable Long definitionId) {
        return ResponseEntity.ok(ApiResponse.ok(executionService.findByDefinitionId(definitionId)));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<WorkflowExecution>> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(executionService.cancel(id), "تم إلغاء التنفيذ"));
    }

    @PatchMapping("/{id}/pause")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<WorkflowExecution>> pause(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(executionService.pause(id), "تم إيقاف التنفيذ مؤقتاً"));
    }

    @GetMapping("/{id}/steps")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<WorkflowStepExecution>>> getStepExecutions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(stepExecutionService.findByExecutionId(id)));
    }
}
