package com.twsela.web;

import com.twsela.domain.IpBlacklist;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.IpBlockingService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.SecurityDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * متحكم إدارة عناوين IP (القائمة السوداء).
 */
@RestController
@RequestMapping("/api/security/ip-blacklist")
@Tag(name = "IP Management", description = "إدارة القائمة السوداء لعناوين IP")
public class IpManagementController {

    private final IpBlockingService ipBlockingService;
    private final AuthenticationHelper authHelper;

    public IpManagementController(IpBlockingService ipBlockingService,
                                   AuthenticationHelper authHelper) {
        this.ipBlockingService = ipBlockingService;
        this.authHelper = authHelper;
    }

    @GetMapping
    @Operation(summary = "القائمة السوداء للعناوين")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<IpBlacklist>>> getBlacklist() {
        List<IpBlacklist> blacklist = ipBlockingService.getActiveBlacklist();
        return ResponseEntity.ok(ApiResponse.ok(blacklist));
    }

    @PostMapping
    @Operation(summary = "حظر عنوان IP")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<IpBlacklist>> blockIp(
            @Valid @RequestBody SecurityDTO.IpBlockRequest request,
            Authentication authentication) {
        Long blockedById = authHelper.getCurrentUserId(authentication);
        IpBlacklist blocked = ipBlockingService.blockIp(
                request.getIpAddress(), request.getReason(), blockedById, request.isPermanent());
        return ResponseEntity.ok(ApiResponse.ok(blocked, "تم حظر عنوان IP بنجاح"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "رفع الحظر عن عنوان IP")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unblockIp(@PathVariable Long id) {
        ipBlockingService.unblockIp(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "تم رفع الحظر بنجاح"));
    }
}
