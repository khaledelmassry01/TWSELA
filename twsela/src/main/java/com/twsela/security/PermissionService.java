package com.twsela.security;

import com.twsela.domain.Role;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
public class PermissionService {

    public Set<Permission> getPermissionsForRole(Role role) {
        return switch (role.getName()) {
            case "OWNER" -> Set.of(
                Permission.USER_VIEW, Permission.USER_CREATE, Permission.USER_UPDATE, Permission.USER_DELETE,
                Permission.SHIPMENT_VIEW, Permission.SHIPMENT_CREATE, Permission.SHIPMENT_UPDATE, 
                Permission.SHIPMENT_DELETE, Permission.SHIPMENT_ASSIGN, Permission.SHIPMENT_STATUS_UPDATE,
                Permission.ZONE_VIEW, Permission.ZONE_CREATE, Permission.ZONE_UPDATE, Permission.ZONE_DELETE, Permission.ZONE_ASSIGN,
                Permission.DASHBOARD_VIEW, Permission.REPORTS_VIEW, Permission.ANALYTICS_VIEW,
                Permission.SYSTEM_CONFIG, Permission.SYSTEM_LOGS
            );
            case "ADMIN" -> Set.of(
                Permission.USER_VIEW, Permission.USER_CREATE, Permission.USER_UPDATE,
                Permission.SHIPMENT_VIEW, Permission.SHIPMENT_CREATE, Permission.SHIPMENT_UPDATE, 
                Permission.SHIPMENT_ASSIGN, Permission.SHIPMENT_STATUS_UPDATE,
                Permission.ZONE_VIEW, Permission.ZONE_CREATE, Permission.ZONE_UPDATE, Permission.ZONE_ASSIGN,
                Permission.DASHBOARD_VIEW, Permission.REPORTS_VIEW, Permission.ANALYTICS_VIEW
            );
            case "MERCHANT" -> Set.of(
                Permission.SHIPMENT_VIEW, Permission.SHIPMENT_CREATE, Permission.SHIPMENT_UPDATE,
                Permission.DASHBOARD_VIEW, Permission.REPORTS_VIEW
            );
            case "COURIER" -> Set.of(
                Permission.SHIPMENT_VIEW, Permission.SHIPMENT_STATUS_UPDATE,
                Permission.DASHBOARD_VIEW
            );
            case "WAREHOUSE_MANAGER" -> Set.of(
                Permission.SHIPMENT_VIEW, Permission.SHIPMENT_CREATE, Permission.SHIPMENT_UPDATE, 
                Permission.SHIPMENT_ASSIGN, Permission.SHIPMENT_STATUS_UPDATE,
                Permission.DASHBOARD_VIEW, Permission.REPORTS_VIEW
            );
            default -> Set.of();
        };
    }

    public boolean hasPermission(Role role, Permission permission) {
        return getPermissionsForRole(role).contains(permission);
    }

    public List<Permission> getAvailablePermissions() {
        return Arrays.asList(Permission.values());
    }
}
