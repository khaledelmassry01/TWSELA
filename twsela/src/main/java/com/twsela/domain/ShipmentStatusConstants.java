package com.twsela.domain;

/**
 * Centralized constants for all shipment status names.
 * Every status referenced in application code MUST be declared here.
 * The DataInitializer and SQL migration scripts must stay in sync with this list.
 *
 * دورة حياة الشحنة الكاملة:
 * PENDING_APPROVAL → APPROVED → RECEIVED_AT_HUB → ASSIGNED_TO_COURIER → OUT_FOR_DELIVERY
 *   → DELIVERED
 *   → FAILED_ATTEMPT → POSTPONED / PENDING_UPDATE / PENDING_RETURN
 *   → RETURNED_TO_HUB → RETURNED_TO_ORIGIN
 *   → CANCELLED / ON_HOLD / PARTIALLY_DELIVERED / RESCHEDULED
 */
public final class ShipmentStatusConstants {

    private ShipmentStatusConstants() {
        // Utility class — no instances
    }

    // ── Creation & Approval ──────────────────────────────────
    public static final String PENDING              = "PENDING";
    public static final String PENDING_APPROVAL     = "PENDING_APPROVAL";
    public static final String APPROVED             = "APPROVED";

    // ── Warehouse ────────────────────────────────────────────
    public static final String PICKED_UP            = "PICKED_UP";
    public static final String RECEIVED_AT_HUB      = "RECEIVED_AT_HUB";

    // ── Courier Assignment & Transit ─────────────────────────
    public static final String ASSIGNED_TO_COURIER  = "ASSIGNED_TO_COURIER";
    public static final String IN_TRANSIT           = "IN_TRANSIT";
    public static final String OUT_FOR_DELIVERY     = "OUT_FOR_DELIVERY";

    // ── Delivery Outcomes ────────────────────────────────────
    public static final String DELIVERED            = "DELIVERED";
    public static final String PARTIALLY_DELIVERED  = "PARTIALLY_DELIVERED";
    public static final String FAILED_DELIVERY      = "FAILED_DELIVERY";
    public static final String FAILED_ATTEMPT       = "FAILED_ATTEMPT";

    // ── Failed Attempt Sub-statuses ──────────────────────────
    public static final String POSTPONED            = "POSTPONED";
    public static final String PENDING_UPDATE       = "PENDING_UPDATE";
    public static final String PENDING_RETURN       = "PENDING_RETURN";

    // ── Returns ──────────────────────────────────────────────
    public static final String RETURNED_TO_HUB      = "RETURNED_TO_HUB";
    public static final String RETURNED_TO_ORIGIN   = "RETURNED_TO_ORIGIN";

    // ── Other ────────────────────────────────────────────────
    public static final String CANCELLED            = "CANCELLED";
    public static final String ON_HOLD              = "ON_HOLD";
    public static final String RESCHEDULED          = "RESCHEDULED";
    public static final String READY_FOR_DISPATCH   = "READY_FOR_DISPATCH";

    /**
     * Complete ordered list of all statuses — used by DataInitializer.
     * Order follows the logical shipment lifecycle.
     */
    public static final String[] ALL_STATUSES = {
        PENDING,
        PENDING_APPROVAL,
        APPROVED,
        PICKED_UP,
        RECEIVED_AT_HUB,
        READY_FOR_DISPATCH,
        ASSIGNED_TO_COURIER,
        IN_TRANSIT,
        OUT_FOR_DELIVERY,
        DELIVERED,
        PARTIALLY_DELIVERED,
        FAILED_DELIVERY,
        FAILED_ATTEMPT,
        POSTPONED,
        PENDING_UPDATE,
        PENDING_RETURN,
        RETURNED_TO_HUB,
        RETURNED_TO_ORIGIN,
        CANCELLED,
        ON_HOLD,
        RESCHEDULED
    };
}
