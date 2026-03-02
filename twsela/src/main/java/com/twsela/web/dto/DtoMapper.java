package com.twsela.web.dto;

import com.twsela.domain.*;

/**
 * Centralised Entity → DTO mapping utility.
 * All controller DTO conversions should go through this class.
 */
public final class DtoMapper {

    private DtoMapper() { /* utility class */ }

    // ── User → UserResponseDTO ───────────────────────────────────

    public static UserResponseDTO toUserDTO(User user) {
        if (user == null) return null;
        UserResponseDTO dto = new UserResponseDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole() != null ? user.getRole().getName() : null);
        dto.setStatus(user.getStatus() != null ? user.getStatus().getName() : null);
        dto.setActive(user.isActive());
        return dto;
    }

    // ── User → LoginResponseDTO ──────────────────────────────────

    public static LoginResponseDTO toLoginDTO(User user) {
        if (user == null) return null;
        return new LoginResponseDTO(
            user.getId(),
            user.getName(),
            user.getPhone(),
            user.getRole() != null ? user.getRole().getName() : null,
            user.getStatus() != null ? user.getStatus().getName() : null
        );
    }

    // ── Shipment → ShipmentResponseDTO ───────────────────────────

    public static ShipmentResponseDTO toShipmentDTO(Shipment shipment) {
        if (shipment == null) return null;
        ShipmentResponseDTO dto = new ShipmentResponseDTO();
        dto.setId(shipment.getId());
        dto.setTrackingNumber(shipment.getTrackingNumber());
        dto.setStatus(shipment.getStatus() != null ? shipment.getStatus().getName() : null);
        dto.setMerchantName(shipment.getMerchant() != null ? shipment.getMerchant().getName() : null);
        // courier name – manifest → courier
        if (shipment.getManifest() != null && shipment.getManifest().getCourier() != null) {
            dto.setCourierName(shipment.getManifest().getCourier().getName());
        }
        dto.setRecipientName(shipment.getRecipientDetails() != null ? shipment.getRecipientDetails().getName() : null);
        dto.setRecipientPhone(shipment.getRecipientDetails() != null ? shipment.getRecipientDetails().getPhone() : null);
        dto.setDeliveryFee(shipment.getDeliveryFee());
        dto.setCreatedAt(shipment.getCreatedAt());
        dto.setUpdatedAt(shipment.getUpdatedAt());
        return dto;
    }
}
