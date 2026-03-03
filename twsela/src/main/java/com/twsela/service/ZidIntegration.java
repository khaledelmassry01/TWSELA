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
 * Zid (زد) integration — Saudi e-commerce platform.
 */
@Component
public class ZidIntegration implements ECommerceIntegration {

    private static final Logger log = LoggerFactory.getLogger(ZidIntegration.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> parseOrder(String rawPayload) {
        Map<String, Object> order = new LinkedHashMap<>();
        try {
            JsonNode root = objectMapper.readTree(rawPayload);
            JsonNode data = root.has("data") ? root.path("data") : root;

            order.put("recipientName", data.path("customer_name").asText(""));
            order.put("recipientPhone", data.path("customer_phone").asText(""));
            order.put("recipientAddress", data.path("shipping_address").path("street").asText(""));
            order.put("city", data.path("shipping_address").path("city").asText(""));

            order.put("externalOrderId", data.path("id").asText());
            order.put("externalOrderNumber", data.path("order_number").asText());
            order.put("itemValue", data.path("total_price").asText("0"));
            order.put("weight", 1.0);

            String paymentMethod = data.path("payment_method").asText("");
            order.put("codAmount", "cod".equalsIgnoreCase(paymentMethod) ? data.path("total_price").asText("0") : "0");
            order.put("notes", "Zid Order #" + data.path("order_number").asText());
        } catch (Exception e) {
            log.error("Failed to parse Zid order: {}", e.getMessage());
            throw new RuntimeException("فشل في تحليل طلب زد: " + e.getMessage());
        }
        return order;
    }

    @Override
    public void updateFulfillment(Shipment shipment, String status, String accessToken, String storeUrl) {
        log.info("Zid fulfillment update: shipment {} → status {} (store: {})",
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
            log.error("Zid webhook validation error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ECommercePlatform getPlatform() {
        return ECommercePlatform.ZID;
    }
}
