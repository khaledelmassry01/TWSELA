package com.twsela.web;

import com.twsela.domain.TenantInvitation;
import com.twsela.domain.TenantUser;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.TenantInvitationService;
import com.twsela.service.TenantIsolationService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.TenantUserDTO;
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
 * وحدة تحكم مستخدمي المستأجر والدعوات.
 */
@RestController
@Tag(name = "Tenant Users", description = "إدارة مستخدمي المستأجر")
public class TenantUserController {

    private final TenantIsolationService isolationService;
    private final TenantInvitationService invitationService;
    private final AuthenticationHelper authHelper;

    public TenantUserController(TenantIsolationService isolationService,
                                 TenantInvitationService invitationService,
                                 AuthenticationHelper authHelper) {
        this.isolationService = isolationService;
        this.invitationService = invitationService;
        this.authHelper = authHelper;
    }

    @GetMapping("/api/tenants/{tenantId}/users")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "جلب مستخدمي المستأجر")
    public ResponseEntity<ApiResponse<List<TenantUserDTO.TenantUserResponse>>> getTenantUsers(
            @PathVariable Long tenantId) {
        List<TenantUser> users = isolationService.getTenantUsers(tenantId);
        List<TenantUserDTO.TenantUserResponse> responses = users.stream()
                .map(TenantUserDTO.TenantUserResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping("/api/tenants/{tenantId}/invitations")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "إرسال دعوة جديدة")
    public ResponseEntity<ApiResponse<TenantUserDTO.InvitationResponse>> createInvitation(
            @PathVariable Long tenantId,
            @Valid @RequestBody TenantUserDTO.InvitationRequest request,
            Authentication authentication) {
        Long currentUserId = authHelper.getCurrentUserId(authentication);
        TenantInvitation invitation = invitationService.createInvitation(
                tenantId, request.getPhone(), request.getRole(), currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(TenantUserDTO.InvitationResponse.from(invitation), "تم إرسال الدعوة بنجاح"));
    }

    @GetMapping("/api/tenants/{tenantId}/invitations")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "جلب دعوات المستأجر")
    public ResponseEntity<ApiResponse<List<TenantUserDTO.InvitationResponse>>> getInvitations(
            @PathVariable Long tenantId,
            @RequestParam(defaultValue = "PENDING") TenantInvitation.InvitationStatus status) {
        List<TenantInvitation> invitations = invitationService.getByTenantAndStatus(tenantId, status);
        List<TenantUserDTO.InvitationResponse> responses = invitations.stream()
                .map(TenantUserDTO.InvitationResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping("/api/invitations/{token}/accept")
    @Operation(summary = "قبول دعوة")
    public ResponseEntity<ApiResponse<TenantUserDTO.TenantUserResponse>> acceptInvitation(
            @PathVariable String token, Authentication authentication) {
        Long currentUserId = authHelper.getCurrentUserId(authentication);
        TenantUser tenantUser = invitationService.acceptInvitation(token, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(TenantUserDTO.TenantUserResponse.from(tenantUser), "تم قبول الدعوة بنجاح"));
    }

    @PutMapping("/api/tenants/{tenantId}/users/{userId}/role")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "تغيير دور مستخدم")
    public ResponseEntity<ApiResponse<TenantUserDTO.TenantUserResponse>> changeUserRole(
            @PathVariable Long tenantId, @PathVariable Long userId,
            @Valid @RequestBody TenantUserDTO.ChangeRoleRequest request) {
        TenantUser tenantUser = isolationService.changeUserRole(tenantId, userId, request.getRole());
        return ResponseEntity.ok(ApiResponse.ok(TenantUserDTO.TenantUserResponse.from(tenantUser), "تم تغيير الدور بنجاح"));
    }

    @DeleteMapping("/api/tenants/{tenantId}/users/{userId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "إزالة مستخدم من المستأجر")
    public ResponseEntity<ApiResponse<Void>> removeUser(
            @PathVariable Long tenantId, @PathVariable Long userId) {
        isolationService.removeUserFromTenant(tenantId, userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "تم إزالة المستخدم"));
    }
}
