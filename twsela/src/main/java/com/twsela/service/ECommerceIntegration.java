package com.twsela.service;

import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import com.twsela.domain.Shipment;

import java.util.Map;

/**
 * Interface for e-commerce platform integrations.
 * Each platform implementation handles order-to-shipment mapping and fulfillment sync.
 */
public interface ECommerceIntegration {

    /**
     * Parse a raw webhook payload into a standardized order map.
     * Keys: recipientName, recipientPhone, recipientAddress, city, weight, codAmount, itemValue, notes
     */
    Map<String, Object> parseOrder(String rawPayload);

    /**
     * Send fulfillment update back to the origin platform.
     */
    void updateFulfillment(Shipment shipment, String status, String accessToken, String storeUrl);

    /**
     * Validate webhook signature for authenticity.
     */
    boolean validateWebhook(String payload, String signature, String secret);

    /**
     * Get the platform this integration handles.
     */
    ECommercePlatform getPlatform();
}
