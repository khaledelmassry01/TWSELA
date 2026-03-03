package com.twsela.service;

import com.twsela.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * معالج أحداث الدفع — يستجيب لأحداث الدفع والاسترداد.
 */
@Service
public class PaymentEventHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventHandler.class);

    public static final String PAYMENT_RECEIVED = "PAYMENT_RECEIVED";
    public static final String REFUND_PROCESSED = "REFUND_PROCESSED";

    /**
     * معالجة حدث دفع.
     */
    public void handle(DomainEvent event) {
        log.info("Handling payment event: type={}, aggregateId={}", event.getEventType(), event.getAggregateId());

        switch (event.getEventType()) {
            case PAYMENT_RECEIVED -> handlePaymentReceived(event);
            case REFUND_PROCESSED -> handleRefundProcessed(event);
            default -> log.debug("Unhandled payment event type: {}", event.getEventType());
        }
    }

    private void handlePaymentReceived(DomainEvent event) {
        log.info("Payment received for aggregate {} — payload: {}", event.getAggregateId(), event.getPayload());
        // Update wallet, create financial record
    }

    private void handleRefundProcessed(DomainEvent event) {
        log.info("Refund processed for aggregate {} — payload: {}", event.getAggregateId(), event.getPayload());
        // Update wallet, create refund record
    }
}
