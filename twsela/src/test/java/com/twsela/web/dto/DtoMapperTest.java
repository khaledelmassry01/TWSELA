package com.twsela.web.dto;

import com.twsela.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DtoMapper — Entity → DTO Mapping")
class DtoMapperTest {

    private User user;
    private Role role;
    private UserStatus status;

    @BeforeEach
    void setUp() {
        role = new Role("COURIER");
        role.setId(2L);
        status = new UserStatus();
        status.setId(1L);
        status.setName("ACTIVE");

        user = new User();
        user.setId(10L);
        user.setName("Ahmed");
        user.setPhone("0501234567");
        user.setRole(role);
        user.setStatus(status);
    }

    // ---- toUserDTO ----

    @Test
    @DisplayName("toUserDTO — maps all fields correctly")
    void toUserDTO_allFields() {
        UserResponseDTO dto = DtoMapper.toUserDTO(user);
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Ahmed", dto.getName());
        assertEquals("0501234567", dto.getPhone());
        assertEquals("COURIER", dto.getRole());
        assertEquals("ACTIVE", dto.getStatus());
    }

    @Test
    @DisplayName("toUserDTO — null user returns null")
    void toUserDTO_null() {
        assertNull(DtoMapper.toUserDTO(null));
    }

    @Test
    @DisplayName("toUserDTO — null role & status handled")
    void toUserDTO_nullRoleStatus() {
        user.setRole(null);
        user.setStatus(null);
        UserResponseDTO dto = DtoMapper.toUserDTO(user);
        assertNull(dto.getRole());
        assertNull(dto.getStatus());
    }

    // ---- toLoginDTO ----

    @Test
    @DisplayName("toLoginDTO — maps fields correctly")
    void toLoginDTO_allFields() {
        LoginResponseDTO dto = DtoMapper.toLoginDTO(user);
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
        assertEquals("Ahmed", dto.getName());
        assertEquals("COURIER", dto.getRole());
    }

    @Test
    @DisplayName("toLoginDTO — null user returns null")
    void toLoginDTO_null() {
        assertNull(DtoMapper.toLoginDTO(null));
    }

    // ---- toShipmentDTO ----

    @Test
    @DisplayName("toShipmentDTO — maps full shipment")
    void toShipmentDTO_full() {
        ShipmentStatus ss = new ShipmentStatus();
        ss.setName("DELIVERED");

        RecipientDetails rd = new RecipientDetails();
        rd.setName("Recipient");
        rd.setPhone("0509999999");

        ShipmentManifest manifest = new ShipmentManifest();
        User courier = new User();
        courier.setName("Courier Ali");
        manifest.setCourier(courier);

        Shipment s = new Shipment();
        s.setId(100L);
        s.setTrackingNumber("TRK-001");
        s.setStatus(ss);
        s.setMerchant(user);
        s.setManifest(manifest);
        s.setRecipientDetails(rd);
        s.setDeliveryFee(new BigDecimal("25.50"));
        s.setCreatedAt(Instant.now());

        ShipmentResponseDTO dto = DtoMapper.toShipmentDTO(s);
        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals("TRK-001", dto.getTrackingNumber());
        assertEquals("DELIVERED", dto.getStatus());
        assertEquals("Ahmed", dto.getMerchantName());
        assertEquals("Courier Ali", dto.getCourierName());
        assertEquals("Recipient", dto.getRecipientName());
        assertEquals(new BigDecimal("25.50"), dto.getDeliveryFee());
    }

    @Test
    @DisplayName("toShipmentDTO — null shipment returns null")
    void toShipmentDTO_null() {
        assertNull(DtoMapper.toShipmentDTO(null));
    }

    @Test
    @DisplayName("toShipmentDTO — null nested objects handled")
    void toShipmentDTO_nullNested() {
        Shipment s = new Shipment();
        s.setId(1L);
        ShipmentResponseDTO dto = DtoMapper.toShipmentDTO(s);
        assertNotNull(dto);
        assertNull(dto.getStatus());
        assertNull(dto.getMerchantName());
        assertNull(dto.getCourierName());
        assertNull(dto.getRecipientName());
    }
}
