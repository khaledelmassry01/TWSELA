package com.twsela.security;

public enum Permission {
    // User Management
    USER_VIEW("user:view", "View users"),
    USER_CREATE("user:create", "Create users"),
    USER_UPDATE("user:update", "Update users"),
    USER_DELETE("user:delete", "Delete users"),
    
    // Shipment Management
    SHIPMENT_VIEW("shipment:view", "View shipments"),
    SHIPMENT_CREATE("shipment:create", "Create shipments"),
    SHIPMENT_UPDATE("shipment:update", "Update shipments"),
    SHIPMENT_DELETE("shipment:delete", "Delete shipments"),
    SHIPMENT_ASSIGN("shipment:assign", "Assign shipments"),
    SHIPMENT_STATUS_UPDATE("shipment:status:update", "Update shipment status"),
    
    // Zone Management
    ZONE_VIEW("zone:view", "View zones"),
    ZONE_CREATE("zone:create", "Create zones"),
    ZONE_UPDATE("zone:update", "Update zones"),
    ZONE_DELETE("zone:delete", "Delete zones"),
    ZONE_ASSIGN("zone:assign", "Assign zones"),
    
    // Dashboard & Reports
    DASHBOARD_VIEW("dashboard:view", "View dashboard"),
    REPORTS_VIEW("reports:view", "View reports"),
    ANALYTICS_VIEW("analytics:view", "View analytics"),
    
    // System Administration
    SYSTEM_CONFIG("system:config", "System configuration"),
    SYSTEM_LOGS("system:logs", "View system logs");

    private final String permission;
    private final String description;

    Permission(String permission, String description) {
        this.permission = permission;
        this.description = description;
    }

    public String getPermission() { return permission; }
    public String getDescription() { return description; }
}
