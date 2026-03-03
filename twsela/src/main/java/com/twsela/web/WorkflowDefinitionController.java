package com.twsela.web;

import com.twsela.domain.Tenant;
import com.twsela.domain.User;
import com.twsela.domain.WorkflowDefinition;
import com.twsela.domain.WorkflowStep;
import com.twsela.repository.TenantRepository;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.WorkflowDefinitionService;
import com.twsela.service.WorkflowStepService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.WorkflowDefinitionDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowDefinitionController {

    private final WorkflowDefinitionService workflowDefinitionService;
    private final WorkflowStepService workflowStepService;
    private final AuthenticationHelper authenticationHelper;
    private final TenantRepository tenantRepository;

    public WorkflowDefinitionController(WorkflowDefinitionService workflowDefinitionService,
                                        WorkflowStepService workflowStepService,
                                        AuthenticationHelper authenticationHelper,
                                        TenantRepository tenantRepository) {
        this.workflowDefinitionService = workflowDefinitionService;
        this.workflowStepService = workflowStepService;
        this.authenticationHelper = authenticationHelper;
        this.tenantRepository = tenantRepository;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> create(@RequestBody WorkflowDefinitionDto dto,
                                                                   Authentication authentication) {
        User currentUser = authenticationHelper.getCurrentUser(authentication);
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setName(dto.getName());
        definition.setDescription(dto.getDescription());
        definition.setTriggerEvent(dto.getTriggerEvent());
        definition.setPriority(dto.getPriority());
        definition.setCreatedBy(currentUser);

        if (currentUser.getTenantId() != null) {
            tenantRepository.findById(currentUser.getTenantId()).ifPresent(definition::setTenant);
        }

        WorkflowDefinition saved = workflowDefinitionService.create(definition);

        // Create steps if provided
        if (dto.getSteps() != null) {
            for (WorkflowDefinitionDto.StepDto stepDto : dto.getSteps()) {
                WorkflowStep step = new WorkflowStep();
                step.setWorkflowDefinition(saved);
                step.setStepOrder(stepDto.getStepOrder());
                step.setStepType(stepDto.getStepType());
                step.setConfiguration(stepDto.getConfiguration());
                step.setNextStepOnSuccess(stepDto.getNextStepOnSuccess());
                step.setNextStepOnFailure(stepDto.getNextStepOnFailure());
                step.setTimeoutSeconds(stepDto.getTimeoutSeconds());
                workflowStepService.create(step);
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(saved, "تم إنشاء سلسلة العمل بنجاح"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(workflowDefinitionService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<WorkflowDefinition>>> getByTenant(Authentication authentication) {
        User currentUser = authenticationHelper.getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.ok(workflowDefinitionService.findByTenantId(currentUser.getTenantId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> update(@PathVariable Long id,
                                                                   @RequestBody WorkflowDefinitionDto dto) {
        WorkflowDefinition updated = new WorkflowDefinition();
        updated.setName(dto.getName());
        updated.setDescription(dto.getDescription());
        updated.setTriggerEvent(dto.getTriggerEvent());
        updated.setPriority(dto.getPriority());
        return ResponseEntity.ok(ApiResponse.ok(workflowDefinitionService.update(id, updated), "تم تحديث سلسلة العمل"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(workflowDefinitionService.activate(id), "تم تفعيل سلسلة العمل"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<WorkflowDefinition>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(workflowDefinitionService.deactivate(id), "تم تعطيل سلسلة العمل"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        workflowStepService.deleteByDefinitionId(id);
        workflowDefinitionService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "تم حذف سلسلة العمل"));
    }

    @GetMapping("/{id}/steps")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<WorkflowStep>>> getSteps(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(workflowStepService.findByDefinitionId(id)));
    }
}
