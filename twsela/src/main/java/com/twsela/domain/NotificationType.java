package com.twsela.domain;

/**
 * Types of notifications in the system.
 */
public enum NotificationType {
    SHIPMENT_STATUS,
    SHIPMENT_ASSIGNED,
    PAYOUT_PROCESSED,
    SYSTEM_ALERT,
    WELCOME,
    PASSWORD_RESET,
    // Sprint 27 — advanced notification event types
    SHIPMENT_CREATED,
    STATUS_CHANGED,
    DELIVERY_ATTEMPT,
    PAYMENT_RECEIVED,
    PAYMENT_FAILED,
    INVOICE_GENERATED,
    SUBSCRIPTION_EXPIRING,
    TICKET_REPLY,
    MANIFEST_ASSIGNED,
    PICKUP_SCHEDULED,
    PICKUP_REMINDER
}
