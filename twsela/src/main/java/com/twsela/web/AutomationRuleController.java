package com.twsela.web;

import com.twsela.domain.AutomationRule;
import com.twsela.domain.User;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.AutomationRuleService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.AutomationRuleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/automation-rules")
public class AutomationRuleController {

    private final AutomationRuleService automationRuleService;
    private final AuthenticationHelper authenticationHelper;

    public AutomationRuleController(AutomationRuleService automationRuleService,
                                    AuthenticationHelper authenticationHelper) {
        this.automationRuleService = automationRuleService;
        this.authenticationHelper = authenticationHelper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<AutomationRule>> create(@RequestBody AutomationRuleDto dto,
                                                               Authentication authentication) {
        User currentUser = authenticationHelper.getCurrentUser(authentication);
        AutomationRule rule = new AutomationRule();
        rule.setName(dto.getName());
        rule.setDescription(dto.getDescription());
        rule.setTriggerEvent(dto.getTriggerEvent());
        rule.setConditionExpression(dto.getConditionExpression());
        rule.setActionType(dto.getActionType());
        rule.setActionConfig(dto.getActionConfig());
        if (currentUser.getTenantId() != null) {
            rule.setTenant(new com.twsela.domain.Tenant());
            rule.getTenant().setId(currentUser.getTenantId());
        }
        return ResponseEntity.ok(ApiResponse.ok(automationRuleService.create(rule), "تم إنشاء قاعدة الأتمتة"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<AutomationRule>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(automationRuleService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<List<AutomationRule>>> getByTenant(Authentication authentication) {
        User currentUser = authenticationHelper.getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.ok(automationRuleService.findByTenantId(currentUser.getTenantId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<AutomationRule>> update(@PathVariable Long id,
                                                               @RequestBody AutomationRuleDto dto) {
        AutomationRule updated = new AutomationRule();
        updated.setName(dto.getName());
        updated.setDescription(dto.getDescription());
        updated.setTriggerEvent(dto.getTriggerEvent());
        updated.setConditionExpression(dto.getConditionExpression());
        updated.setActionType(dto.getActionType());
        updated.setActionConfig(dto.getActionConfig());
        return ResponseEntity.ok(ApiResponse.ok(automationRuleService.update(id, updated), "تم تحديث قاعدة الأتمتة"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<AutomationRule>> activate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(automationRuleService.activate(id), "تم تفعيل قاعدة الأتمتة"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<AutomationRule>> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(automationRuleService.deactivate(id), "تم تعطيل قاعدة الأتمتة"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER','ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        automationRuleService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "تم حذف قاعدة الأتمتة"));
    }
}
