package com.twsela.web;

import com.twsela.domain.PaymentIntent;
import com.twsela.domain.PaymentMethod;
import com.twsela.domain.PaymentTransaction.PaymentGatewayType;
import com.twsela.repository.PaymentMethodRepository;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.PaymentIntentService;
import com.twsela.web.dto.AddPaymentMethodRequest;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.CreatePaymentIntentRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * متحكم نوايا الدفع ووسائل الدفع.
 */
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Intents", description = "إدارة نوايا الدفع ووسائل الدفع المحفوظة")
public class PaymentIntentController {

    private final PaymentIntentService paymentIntentService;
    private final PaymentMethodRepository paymentMethodRepository;
    private final AuthenticationHelper authHelper;

    public PaymentIntentController(PaymentIntentService paymentIntentService,
                                    PaymentMethodRepository paymentMethodRepository,
                                    AuthenticationHelper authHelper) {
        this.paymentIntentService = paymentIntentService;
        this.paymentMethodRepository = paymentMethodRepository;
        this.authHelper = authHelper;
    }

    @PostMapping("/intents")
    @Operation(summary = "إنشاء نية دفع جديدة")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createIntent(
            @Valid @RequestBody CreatePaymentIntentRequest request,
            Authentication authentication) {
        PaymentGatewayType gatewayType = PaymentGatewayType.valueOf(request.getGateway().toUpperCase());
        PaymentIntent intent = paymentIntentService.createIntent(
                request.getShipmentId(), request.getAmount(), request.getCurrency(),
                gatewayType, request.getPaymentMethodId());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("intentId", intent.getId());
        data.put("status", intent.getStatus().name());
        data.put("amount", intent.getAmount());
        data.put("currency", intent.getCurrency());
        data.put("expiresAt", intent.getExpiresAt());
        return ResponseEntity.ok(ApiResponse.ok(data, "تم إنشاء نية الدفع بنجاح"));
    }

    @GetMapping("/intents/{id}")
    @Operation(summary = "الحصول على حالة نية الدفع")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getIntent(@PathVariable Long id) {
        PaymentIntent intent = paymentIntentService.getById(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("intentId", intent.getId());
        data.put("status", intent.getStatus().name());
        data.put("amount", intent.getAmount());
        data.put("currency", intent.getCurrency());
        data.put("provider", intent.getProvider() != null ? intent.getProvider().name() : null);
        data.put("providerRef", intent.getProviderRef());
        data.put("attempts", intent.getAttempts());
        data.put("expiresAt", intent.getExpiresAt());
        data.put("confirmedAt", intent.getConfirmedAt());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    @PostMapping("/intents/{id}/confirm")
    @Operation(summary = "تأكيد نية الدفع")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> confirmIntent(@PathVariable Long id) {
        PaymentIntent intent = paymentIntentService.confirmIntent(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("intentId", intent.getId());
        data.put("status", intent.getStatus().name());
        data.put("providerRef", intent.getProviderRef());
        return ResponseEntity.ok(ApiResponse.ok(data, "تم تأكيد نية الدفع"));
    }

    @PostMapping("/intents/{id}/cancel")
    @Operation(summary = "إلغاء نية الدفع")
    @PreAuthorize("hasAnyRole('MERCHANT', 'OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> cancelIntent(@PathVariable Long id) {
        PaymentIntent intent = paymentIntentService.cancelIntent(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("intentId", intent.getId());
        data.put("status", intent.getStatus().name());
        return ResponseEntity.ok(ApiResponse.ok(data, "تم إلغاء نية الدفع"));
    }

    @GetMapping("/methods")
    @Operation(summary = "وسائل الدفع المحفوظة")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PaymentMethod>>> getMethods(Authentication authentication) {
        Long userId = authHelper.getCurrentUserId(authentication);
        List<PaymentMethod> methods = paymentMethodRepository.findByUserIdAndActiveTrue(userId);
        return ResponseEntity.ok(ApiResponse.ok(methods));
    }

    @PostMapping("/methods")
    @Operation(summary = "إضافة وسيلة دفع جديدة")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> addMethod(
            @Valid @RequestBody AddPaymentMethodRequest request,
            Authentication authentication) {
        Long userId = authHelper.getCurrentUserId(authentication);

        PaymentMethod method = new PaymentMethod();
        method.setType(PaymentMethod.PaymentType.valueOf(request.getType().toUpperCase()));
        method.setProvider(PaymentGatewayType.valueOf(request.getProvider().toUpperCase()));
        method.setLast4(request.getLast4());
        method.setBrand(request.getBrand());
        method.setDefault(request.isDefault());
        method.setTokenizedRef(request.getTokenizedRef());
        method.setMetadata(request.getMetadata());
        method.setActive(true);
        method.setCreatedAt(Instant.now());

        // Set user — need to load from context
        com.twsela.domain.User user = new com.twsela.domain.User();
        user.setId(userId);
        method.setUser(user);

        PaymentMethod saved = paymentMethodRepository.save(method);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("methodId", saved.getId());
        data.put("type", saved.getType().name());
        data.put("provider", saved.getProvider().name());
        data.put("last4", saved.getLast4());
        return ResponseEntity.ok(ApiResponse.ok(data, "تم إضافة وسيلة الدفع بنجاح"));
    }

    @DeleteMapping("/methods/{id}")
    @Operation(summary = "حذف وسيلة دفع")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteMethod(@PathVariable Long id) {
        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new com.twsela.web.exception.ResourceNotFoundException("PaymentMethod", "id", id));
        method.setActive(false);
        method.setUpdatedAt(Instant.now());
        paymentMethodRepository.save(method);
        return ResponseEntity.ok(ApiResponse.ok(null, "تم حذف وسيلة الدفع"));
    }
}
