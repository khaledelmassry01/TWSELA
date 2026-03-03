package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.domain.ECommerceConnection.ECommercePlatform;
import com.twsela.domain.ECommerceOrder.OrderStatus;
import com.twsela.repository.ECommerceConnectionRepository;
import com.twsela.repository.ECommerceOrderRepository;
import com.twsela.repository.UserRepository;
import com.twsela.repository.ZoneRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * E-commerce integration service — connects stores, processes incoming orders, syncs fulfillment.
 */
@Service
@Transactional
public class ECommerceService {

    private static final Logger log = LoggerFactory.getLogger(ECommerceService.class);

    private final ECommerceConnectionRepository connectionRepository;
    private final ECommerceOrderRepository orderRepository;
    private final ECommerceIntegrationFactory integrationFactory;
    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;

    public ECommerceService(ECommerceConnectionRepository connectionRepository,
                             ECommerceOrderRepository orderRepository,
                             ECommerceIntegrationFactory integrationFactory,
                             UserRepository userRepository,
                             ZoneRepository zoneRepository) {
        this.connectionRepository = connectionRepository;
        this.orderRepository = orderRepository;
        this.integrationFactory = integrationFactory;
        this.userRepository = userRepository;
        this.zoneRepository = zoneRepository;
    }

    /**
     * Connect a merchant's store.
     */
    public ECommerceConnection connectStore(Long merchantId, ECommercePlatform platform,
                                             String storeUrl, String storeName,
                                             String accessToken, String webhookSecret,
                                             Long defaultZoneId) {
        User merchant = userRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", merchantId));

        // Check for existing connection
        Optional<ECommerceConnection> existing = connectionRepository.findByMerchantIdAndPlatform(merchantId, platform);
        if (existing.isPresent() && existing.get().isActive()) {
            throw new BusinessRuleException("يوجد اتصال نشط بالفعل لهذه المنصة");
        }

        ECommerceConnection connection = new ECommerceConnection();
        connection.setMerchant(merchant);
        connection.setPlatform(platform);
        connection.setStoreUrl(storeUrl);
        connection.setStoreName(storeName != null ? storeName : platform.name());
        connection.setAccessToken(accessToken);
        connection.setWebhookSecret(webhookSecret);
        if (defaultZoneId != null) {
            Zone zone = zoneRepository.findById(defaultZoneId)
                    .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", defaultZoneId));
            connection.setDefaultZone(zone);
        }

        connection = connectionRepository.save(connection);
        log.info("E-commerce connection established: {} ({}) for merchant {}",
                platform, storeUrl, merchantId);
        return connection;
    }

    /**
     * Disconnect a store.
     */
    public void disconnectStore(Long connectionId) {
        ECommerceConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("ECommerceConnection", "id", connectionId));
        connection.setActive(false);
        connectionRepository.save(connection);
        log.info("E-commerce connection {} disconnected", connectionId);
    }

    /**
     * Process an incoming order from a webhook.
     */
    public ECommerceOrder processIncomingOrder(Long connectionId, String rawPayload, String signature) {
        ECommerceConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("ECommerceConnection", "id", connectionId));

        if (!connection.isActive()) {
            throw new BusinessRuleException("الاتصال غير نشط");
        }

        ECommerceIntegration integration = integrationFactory.getIntegration(connection.getPlatform());

        // Validate webhook signature
        if (connection.getWebhookSecret() != null && signature != null) {
            boolean valid = integration.validateWebhook(rawPayload, signature, connection.getWebhookSecret());
            if (!valid) {
                throw new BusinessRuleException("توقيع Webhook غير صالح");
            }
        }

        // Parse order
        Map<String, Object> orderData;
        try {
            orderData = integration.parseOrder(rawPayload);
        } catch (Exception e) {
            ECommerceOrder failedOrder = new ECommerceOrder();
            failedOrder.setConnection(connection);
            failedOrder.setExternalOrderId("PARSE_ERROR_" + Instant.now().toEpochMilli());
            failedOrder.setPlatform(connection.getPlatform());
            failedOrder.setStatus(OrderStatus.FAILED);
            failedOrder.setRawPayload(rawPayload);
            failedOrder.setErrorMessage(e.getMessage());
            return orderRepository.save(failedOrder);
        }

        String externalOrderId = (String) orderData.getOrDefault("externalOrderId", "");

        // Check for duplicate
        Optional<ECommerceOrder> existing = orderRepository.findByExternalOrderIdAndPlatform(
                externalOrderId, connection.getPlatform());
        if (existing.isPresent()) {
            log.warn("Duplicate order received: {} ({})", externalOrderId, connection.getPlatform());
            return existing.get();
        }

        // Create order record
        ECommerceOrder order = new ECommerceOrder();
        order.setConnection(connection);
        order.setExternalOrderId(externalOrderId);
        order.setExternalOrderNumber((String) orderData.getOrDefault("externalOrderNumber", ""));
        order.setPlatform(connection.getPlatform());
        order.setRawPayload(rawPayload);

        if (connection.isAutoCreateShipments()) {
            try {
                // In a production system, this would call ShipmentService.createShipment()
                // For now, we mark as SHIPMENT_CREATED and log the data
                order.setStatus(OrderStatus.SHIPMENT_CREATED);
                order.setProcessedAt(Instant.now());
                log.info("Auto-created shipment for {} order {}",
                        connection.getPlatform(), externalOrderId);
            } catch (Exception e) {
                order.setStatus(OrderStatus.FAILED);
                order.setErrorMessage(e.getMessage());
                connection.setSyncErrors(connection.getSyncErrors() + 1);
                connectionRepository.save(connection);
            }
        } else {
            order.setStatus(OrderStatus.RECEIVED);
        }

        connection.setLastSyncAt(Instant.now());
        connectionRepository.save(connection);

        return orderRepository.save(order);
    }

    /**
     * Sync fulfillment: update the origin platform when a shipment status changes.
     */
    public void syncFulfillment(Long orderId, String status) {
        ECommerceOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ECommerceOrder", "id", orderId));

        ECommerceConnection connection = order.getConnection();
        ECommerceIntegration integration = integrationFactory.getIntegration(connection.getPlatform());

        try {
            if (order.getShipment() != null) {
                integration.updateFulfillment(order.getShipment(), status,
                        connection.getAccessToken(), connection.getStoreUrl());
                order.setStatus(OrderStatus.FULFILLED);
                orderRepository.save(order);
            }
        } catch (Exception e) {
            log.error("Failed to sync fulfillment for order {}: {}", orderId, e.getMessage());
        }
    }

    /**
     * Retry failed orders for a connection.
     */
    public int retryFailedOrders(Long connectionId) {
        ECommerceConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("ECommerceConnection", "id", connectionId));

        List<ECommerceOrder> failed = orderRepository.findByConnectionId(connectionId).stream()
                .filter(o -> o.getStatus() == OrderStatus.FAILED)
                .toList();

        int retried = 0;
        for (ECommerceOrder order : failed) {
            try {
                ECommerceIntegration integration = integrationFactory.getIntegration(connection.getPlatform());
                integration.parseOrder(order.getRawPayload());
                order.setStatus(OrderStatus.SHIPMENT_CREATED);
                order.setProcessedAt(Instant.now());
                order.setErrorMessage(null);
                orderRepository.save(order);
                retried++;
            } catch (Exception e) {
                log.warn("Retry failed for order {}: {}", order.getId(), e.getMessage());
            }
        }

        log.info("Retried {} of {} failed orders for connection {}", retried, failed.size(), connectionId);
        return retried;
    }

    /**
     * Get connection stats.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getConnectionStats(Long connectionId) {
        ECommerceConnection connection = connectionRepository.findById(connectionId)
                .orElseThrow(() -> new ResourceNotFoundException("ECommerceConnection", "id", connectionId));

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("connectionId", connectionId);
        stats.put("platform", connection.getPlatform());
        stats.put("totalOrders", orderRepository.findByConnectionId(connectionId).size());
        stats.put("received", orderRepository.countByConnectionIdAndStatus(connectionId, OrderStatus.RECEIVED));
        stats.put("created", orderRepository.countByConnectionIdAndStatus(connectionId, OrderStatus.SHIPMENT_CREATED));
        stats.put("fulfilled", orderRepository.countByConnectionIdAndStatus(connectionId, OrderStatus.FULFILLED));
        stats.put("failed", orderRepository.countByConnectionIdAndStatus(connectionId, OrderStatus.FAILED));
        stats.put("syncErrors", connection.getSyncErrors());
        stats.put("lastSyncAt", connection.getLastSyncAt());
        return stats;
    }

    /**
     * Get connections for a merchant.
     */
    @Transactional(readOnly = true)
    public List<ECommerceConnection> getConnectionsByMerchant(Long merchantId) {
        return connectionRepository.findByMerchantId(merchantId);
    }

    /**
     * Get orders for a connection.
     */
    @Transactional(readOnly = true)
    public List<ECommerceOrder> getOrdersByConnection(Long connectionId) {
        return orderRepository.findByConnectionId(connectionId);
    }

    /**
     * Find connection by ID.
     */
    @Transactional(readOnly = true)
    public ECommerceConnection findConnectionById(Long id) {
        return connectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ECommerceConnection", "id", id));
    }
}
