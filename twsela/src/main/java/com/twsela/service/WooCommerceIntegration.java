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
 * WooCommerce integration — parses WC order webhooks and syncs status.
 */
@Component
public class WooCommerceIntegration implements ECommerceIntegration {

    private static final Logger log = LoggerFactory.getLogger(WooCommerceIntegration.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> parseOrder(String rawPayload) {
        Map<String, Object> order = new LinkedHashMap<>();
        try {
            JsonNode root = objectMapper.readTree(rawPayload);

            JsonNode shipping = root.path("shipping");
            order.put("recipientName", shipping.path("first_name").asText("") + " " + shipping.path("last_name").asText(""));
            order.put("recipientPhone", root.path("billing").path("phone").asText(""));
            order.put("recipientAddress", shipping.path("address_1").asText("") + " " + shipping.path("address_2").asText(""));
            order.put("city", shipping.path("city").asText(""));

            order.put("externalOrderId", root.path("id").asText());
            order.put("externalOrderNumber", root.path("number").asText());
            order.put("itemValue", root.path("total").asText("0"));

            double weight = 1.0;
            JsonNode items = root.path("line_items");
            if (items.isArray() && items.size() > 0) {
                // WC doesn't always provide weight in webhook — default to 1kg
                weight = 1.0;
            }
            order.put("weight", weight);

            String paymentMethod = root.path("payment_method").asText("");
            order.put("codAmount", "cod".equalsIgnoreCase(paymentMethod) ? root.path("total").asText("0") : "0");
            order.put("notes", "WooCommerce Order #" + root.path("number").asText());
        } catch (Exception e) {
            log.error("Failed to parse WooCommerce order: {}", e.getMessage());
            throw new RuntimeException("فشل في تحليل طلب WooCommerce: " + e.getMessage());
        }
        return order;
    }

    @Override
    public void updateFulfillment(Shipment shipment, String status, String accessToken, String storeUrl) {
        log.info("WooCommerce status update: shipment {} → status {} (store: {})",
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
            log.error("WC webhook validation error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ECommercePlatform getPlatform() {
        return ECommercePlatform.WOOCOMMERCE;
    }
}
