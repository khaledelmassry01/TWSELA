package com.twsela.web;

import com.twsela.service.PaymentWebhookProcessor;
import com.twsela.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * متحكم callbacks بوابات الدفع — يستقبل webhooks من Paymob / Tap / Stripe / Fawry.
 */
@RestController
@RequestMapping("/api/payments/callback")
@Tag(name = "Payment Callbacks", description = "استقبال ومعالجة webhooks من بوابات الدفع")
public class PaymentCallbackController {

    private final PaymentWebhookProcessor webhookProcessor;

    public PaymentCallbackController(PaymentWebhookProcessor webhookProcessor) {
        this.webhookProcessor = webhookProcessor;
    }

    @PostMapping("/paymob")
    @Operation(summary = "Paymob webhook callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handlePaymob(
            @RequestBody String payload,
            @RequestHeader(value = "hmac", required = false) String signature,
            @RequestParam(value = "reference", required = false) String reference) {
        return handleWebhook("PAYMOB", "payment.callback", payload, signature, reference);
    }

    @PostMapping("/tap")
    @Operation(summary = "Tap Payments webhook callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleTap(
            @RequestBody String payload,
            @RequestHeader(value = "tap-signature", required = false) String signature,
            @RequestParam(value = "reference", required = false) String reference) {
        return handleWebhook("TAP", "payment.callback", payload, signature, reference);
    }

    @PostMapping("/stripe")
    @Operation(summary = "Stripe webhook callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleStripe(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature,
            @RequestParam(value = "reference", required = false) String reference) {
        return handleWebhook("STRIPE", "payment.callback", payload, signature, reference);
    }

    @PostMapping("/fawry")
    @Operation(summary = "Fawry webhook callback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleFawry(
            @RequestBody String payload,
            @RequestHeader(value = "fawry-signature", required = false) String signature,
            @RequestParam(value = "reference", required = false) String reference) {
        return handleWebhook("FAWRY", "payment.callback", payload, signature, reference);
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> handleWebhook(
            String provider, String eventType, String payload, String signature, String reference) {
        var webhookLog = webhookProcessor.processWebhook(provider, eventType, payload, signature, reference);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("webhookId", webhookLog.getId());
        data.put("verified", webhookLog.isVerified());
        data.put("processed", webhookLog.isProcessed());
        return ResponseEntity.ok(ApiResponse.ok(data));
    }
}
