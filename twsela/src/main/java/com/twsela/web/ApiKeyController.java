package com.twsela.web;

import com.twsela.domain.ApiKey;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.ApiKeyService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.DeveloperDTO.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for API Key management (Developer Portal).
 */
@RestController
@RequestMapping("/api/developer/keys")
@Tag(name = "Developer API Keys", description = "إدارة مفاتيح API")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final AuthenticationHelper authHelper;

    public ApiKeyController(ApiKeyService apiKeyService, AuthenticationHelper authHelper) {
        this.apiKeyService = apiKeyService;
        this.authHelper = authHelper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")
    @Operation(summary = "إنشاء مفتاح API جديد")
    public ResponseEntity<ApiResponse<ApiKeyCreatedResponse>> createKey(
            @Valid @RequestBody CreateApiKeyRequest request,
            Authentication auth) {
        Long merchantId = authHelper.getCurrentUserId(auth);
        Map<String, Object> result = apiKeyService.generateApiKey(merchantId, request.name(), request.scopes());
        ApiKey key = (ApiKey) result.get("apiKey");
        String secret = (String) result.get("secret");

        ApiKeyCreatedResponse response = new ApiKeyCreatedResponse(key.getId(), key.getKeyValue(), secret);
        return ResponseEntity.ok(ApiResponse.ok(response, "تم إنشاء المفتاح. احفظ السر — لن يُعرض مجدداً"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")
    @Operation(summary = "عرض مفاتيحي")
    public ResponseEntity<ApiResponse<List<ApiKeyResponse>>> getMyKeys(Authentication auth) {
        Long merchantId = authHelper.getCurrentUserId(auth);
        List<ApiKeyResponse> keys = apiKeyService.getKeysByMerchant(merchantId).stream()
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(keys));
    }

    @PutMapping("/{id}/rotate")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")
    @Operation(summary = "تدوير مفتاح API")
    public ResponseEntity<ApiResponse<ApiKeyCreatedResponse>> rotateKey(@PathVariable Long id) {
        Map<String, Object> result = apiKeyService.rotateKey(id);
        ApiKey key = (ApiKey) result.get("apiKey");
        String secret = (String) result.get("secret");

        ApiKeyCreatedResponse response = new ApiKeyCreatedResponse(key.getId(), key.getKeyValue(), secret);
        return ResponseEntity.ok(ApiResponse.ok(response, "تم تدوير المفتاح بنجاح"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")
    @Operation(summary = "إلغاء مفتاح API")
    public ResponseEntity<ApiResponse<Void>> revokeKey(@PathVariable Long id) {
        apiKeyService.revokeKey(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "تم إلغاء المفتاح"));
    }

    @GetMapping("/{id}/usage")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")
    @Operation(summary = "إحصائيات استخدام المفتاح")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUsageStats(
            @PathVariable Long id,
            @RequestParam(defaultValue = "30") int days) {
        Instant from = Instant.now().minus(days, ChronoUnit.DAYS);
        Map<String, Object> stats = apiKeyService.getUsageStats(id, from, Instant.now());
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }

    private ApiKeyResponse toResponse(ApiKey key) {
        return new ApiKeyResponse(key.getId(), key.getName(), key.getKeyValue(),
                key.getScopes(), key.getRateLimit(), key.isActive(),
                key.getLastUsedAt(), key.getRequestCount(), key.getCreatedAt());
    }
}
