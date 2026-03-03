package com.twsela.service;

import com.twsela.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * معالج أحداث الشحنات — يستجيب لتغييرات حالة الشحنات.
 */
@Service
public class ShipmentEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ShipmentEventHandler.class);

    public static final String SHIPMENT_STATUS_CHANGED = "SHIPMENT_STATUS_CHANGED";
    public static final String SHIPMENT_ASSIGNED = "SHIPMENT_ASSIGNED";
    public static final String SHIPMENT_DELIVERED = "SHIPMENT_DELIVERED";

    /**
     * معالجة حدث شحنة.
     */
    public void handle(DomainEvent event) {
        log.info("Handling shipment event: type={}, aggregateId={}", event.getEventType(), event.getAggregateId());

        switch (event.getEventType()) {
            case SHIPMENT_STATUS_CHANGED -> handleStatusChanged(event);
            case SHIPMENT_ASSIGNED -> handleAssigned(event);
            case SHIPMENT_DELIVERED -> handleDelivered(event);
            default -> log.debug("Unhandled shipment event type: {}", event.getEventType());
        }
    }

    private void handleStatusChanged(DomainEvent event) {
        log.info("Shipment {} status changed — payload: {}", event.getAggregateId(), event.getPayload());
        // Trigger notifications, update KPIs, record history
    }

    private void handleAssigned(DomainEvent event) {
        log.info("Shipment {} assigned to courier — payload: {}", event.getAggregateId(), event.getPayload());
        // Notify courier, update dashboard
    }

    private void handleDelivered(DomainEvent event) {
        log.info("Shipment {} delivered — payload: {}", event.getAggregateId(), event.getPayload());
        // Update statistics, trigger settlement, send confirmation
    }
}
