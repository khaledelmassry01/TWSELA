package com.twsela.web;

import com.twsela.domain.ScheduledTask;
import com.twsela.domain.User;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.ScheduledTaskService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.ScheduledTaskDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduled-tasks")
public class ScheduledTaskController {

    private final ScheduledTaskService scheduledTaskService;
    private final AuthenticationHelper authenticationHelper;

    public ScheduledTaskController(ScheduledTaskService scheduledTaskService,
                                   AuthenticationHelper authenticationHelper) {
        this.scheduledTaskService = scheduledTaskService;
        this.authenticationHelper = authenticationHelper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<ScheduledTask>> create(@RequestBody ScheduledTaskDto dto,
                                                              Authentication authentication) {
        User currentUser = authenticationHelper.getCurrentUser(authentication);
        ScheduledTask task = new ScheduledTask();
        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setTaskType(dto.getTaskType());
        task.setCronExpression(dto.getCronExpression());
        task.setConfiguration(dto.getConfiguration());
        if (currentUser.getTenantId() != null) {
            task.setTenant(new com.twsela.domain.Tenant());
            task.getTenant().setId(currentUser.getTenantId());
        }
        return ResponseEntity.ok(ApiResponse.ok(scheduledTaskService.create(task), "تم إنشاء المهمة المجدولة"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<ScheduledTask>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(scheduledTaskService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<ScheduledTask>>> getByTenant(Authentication authentication) {
        User currentUser = authenticationHelper.getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.ok(scheduledTaskService.findByTenantId(currentUser.getTenantId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<ScheduledTask>> update(@PathVariable Long id,
                                                              @RequestBody ScheduledTaskDto dto) {
        ScheduledTask updated = new ScheduledTask();
        updated.setName(dto.getName());
        updated.setDescription(dto.getDescription());
        updated.setTaskType(dto.getTaskType());
        updated.setCronExpression(dto.getCronExpression());
        updated.setConfiguration(dto.getConfiguration());
        return ResponseEntity.ok(ApiResponse.ok(scheduledTaskService.update(id, updated), "تم تحديث المهمة المجدولة"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<ScheduledTask>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(scheduledTaskService.activate(id), "تم تفعيل المهمة المجدولة"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<ScheduledTask>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(scheduledTaskService.deactivate(id), "تم تعطيل المهمة المجدولة"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        scheduledTaskService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "تم حذف المهمة المجدولة"));
    }
}
