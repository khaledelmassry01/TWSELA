package com.twsela.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import com.twsela.domain.Shipment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Salla (سلة) integration — Saudi e-commerce platform.
 */
@Component
public class SallaIntegration implements ECommerceIntegration {

    private static final Logger log = LoggerFactory.getLogger(SallaIntegration.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> parseOrder(String rawPayload) {
        Map<String, Object> order = new LinkedHashMap<>();
        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            JsonNode data = root.path("data");

            JsonNode customer = data.path("customer");
            order.put("recipientName", customer.path("first_name").asText("") + " " + customer.path("last_name").asText(""));
            order.put("recipientPhone", customer.path("mobile").asText(""));

            JsonNode shipping = data.path("shipping").path("address");
            order.put("recipientAddress", shipping.path("street_number").asText("") + " " + shipping.path("block").asText(""));
            order.put("city", shipping.path("city").asText(""));

            order.put("externalOrderId", data.path("id").asText());
            order.put("externalOrderNumber", data.path("reference_id").asText());

            JsonNode amounts = data.path("amounts");
            order.put("itemValue", amounts.path("total").path("amount").asText("0"));
            order.put("weight", 1.0);

            String paymentMethod = data.path("payment_method").asText("");
            order.put("codAmount", "cod".equalsIgnoreCase(paymentMethod) ? amounts.path("total").path("amount").asText("0") : "0");
            order.put("notes", "Salla Order #" + data.path("reference_id").asText());
        } catch (Exception e) {
            log.error("Failed to parse Salla order: {}", e.getMessage());
            throw new RuntimeException("فشل في تحليل طلب سلة: " + e.getMessage());
        }
        return order;
    }

    @Override
    public void updateFulfillment(Shipment shipment, String status, String accessToken, String storeUrl) {
        log.info("Salla fulfillment update: shipment {} → status {} (store: {})",
                shipment.getTrackingNumber(), status, storeUrl);
    }

    @Override
    public boolean validateWebhook(String payload, String signature, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computed = Base64.getEncoder().encodeToString(hash);
            return computed.equals(signature);
        } catch (Exception e) {
            log.error("Salla webhook validation error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ECommercePlatform getPlatform() {
        return ECommercePlatform.SALLA;
    }
}
