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
 * Shopify integration — parses Shopify order webhooks and syncs fulfillment.
 */
@Component
public class ShopifyIntegration implements ECommerceIntegration {

    private static final Logger log = LoggerFactory.getLogger(ShopifyIntegration.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> parseOrder(String rawPayload) {
        Map<String, Object> order = new LinkedHashMap<>();
        try {
            JsonNode root = objectMapper.readTree(rawPayload);

            // Shipping address
            JsonNode shipping = root.path("shipping_address");
            order.put("recipientName", shipping.path("name").asText(""));
            order.put("recipientPhone", shipping.path("phone").asText(""));
            order.put("recipientAddress", shipping.path("address1").asText("") + " " + shipping.path("address2").asText(""));
            order.put("city", shipping.path("city").asText(""));

            // Order details
            order.put("externalOrderId", root.path("id").asText());
            order.put("externalOrderNumber", root.path("order_number").asText());
            order.put("itemValue", root.path("total_price").asText("0"));

            // Weight from line items
            double totalWeight = 0;
            JsonNode items = root.path("line_items");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    totalWeight += item.path("grams").asDouble(0) / 1000.0;
                }
            }
            order.put("weight", totalWeight > 0 ? totalWeight : 1.0);

            // COD detection
            String gateway = root.path("gateway").asText("");
            boolean isCOD = "cash_on_delivery".equalsIgnoreCase(gateway) || "cod".equalsIgnoreCase(gateway);
            order.put("isCOD", isCOD);
            if (isCOD) {
                order.put("codAmount", root.path("total_price").asText("0"));
            } else {
                order.put("codAmount", "0");
            }

            order.put("notes", "Shopify Order #" + root.path("order_number").asText());
        } catch (Exception e) {
            log.error("Failed to parse Shopify order: {}", e.getMessage());
            throw new RuntimeException("فشل في تحليل طلب Shopify: " + e.getMessage());
        }
        return order;
    }

    @Override
    public void updateFulfillment(Shipment shipment, String status, String accessToken, String storeUrl) {
        // In production, POST to Shopify Fulfillment API
        log.info("Shopify fulfillment update: shipment {} → status {} (store: {})",
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
            log.error("Webhook validation error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public ECommercePlatform getPlatform() {
        return ECommercePlatform.SHOPIFY;
    }
}
