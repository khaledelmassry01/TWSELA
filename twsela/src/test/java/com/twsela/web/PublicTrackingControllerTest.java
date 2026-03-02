package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.repository.ShipmentRepository;
import com.twsela.security.JwtService;
import com.twsela.service.CourierLocationService;
import com.twsela.web.dto.LocationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = PublicTrackingController.class,
    properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000",
        "spring.profiles.active=test"
    }
)
@AutoConfigureMockMvc(addFilters = false)
class PublicTrackingControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private ShipmentRepository shipmentRepository;
    @MockBean private CourierLocationService locationService;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    private Shipment sampleShipment;
    private ShipmentStatus deliveredStatus;
    private ShipmentStatus inTransitStatus;

    @BeforeEach
    void setUp() {
        deliveredStatus = new ShipmentStatus();
        deliveredStatus.setId(1L);
        deliveredStatus.setName("DELIVERED");

        inTransitStatus = new ShipmentStatus();
        inTransitStatus.setId(2L);
        inTransitStatus.setName("IN_TRANSIT");

        sampleShipment = new Shipment();
        sampleShipment.setId(1L);
        sampleShipment.setTrackingNumber("TS123456789");
        sampleShipment.setStatus(deliveredStatus);

        // Add status history
        ShipmentStatusHistory h1 = new ShipmentStatusHistory();
        ShipmentStatus pendingStatus = new ShipmentStatus();
        pendingStatus.setName("PENDING");
        h1.setStatus(pendingStatus);
        h1.setCreatedAt(Instant.now().minusSeconds(3600));

        ShipmentStatusHistory h2 = new ShipmentStatusHistory();
        h2.setStatus(deliveredStatus);
        h2.setCreatedAt(Instant.now());

        sampleShipment.setStatusHistory(new LinkedHashSet<>(List.of(h1, h2)));
    }

    @Test
    @DisplayName("GET /api/public/tracking/{trackingNumber} — found, delivered")
    void trackShipment_found_delivered() throws Exception {
        when(shipmentRepository.findByTrackingNumber("TS123456789"))
                .thenReturn(Optional.of(sampleShipment));

        mockMvc.perform(get("/api/public/tracking/TS123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.trackingNumber").value("TS123456789"))
                .andExpect(jsonPath("$.data.currentStatus").value("DELIVERED"));
    }

    @Test
    @DisplayName("GET /api/public/tracking/{trackingNumber} — not found returns 404")
    void trackShipment_notFound_returns404() throws Exception {
        when(shipmentRepository.findByTrackingNumber("INVALID"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/public/tracking/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/public/tracking/{trackingNumber} — in transit shows courier location")
    void trackShipment_inTransit_showsCourierLocation() throws Exception {
        sampleShipment.setStatus(inTransitStatus);
        sampleShipment.setDeliveryLatitude(new BigDecimal("30.0131"));
        sampleShipment.setDeliveryLongitude(new BigDecimal("31.2089"));

        // Set up courier via manifest
        User courier = new User();
        courier.setId(5L);
        courier.setName("Ahmed Courier");
        ShipmentManifest manifest = new ShipmentManifest();
        manifest.setCourier(courier);
        sampleShipment.setManifest(manifest);

        when(shipmentRepository.findByTrackingNumber("TS123456789"))
                .thenReturn(Optional.of(sampleShipment));
        when(locationService.getLastLocation(5L))
                .thenReturn(Optional.of(new LocationDTO(
                        new BigDecimal("30.0444"), new BigDecimal("31.2357"), Instant.now())));
        when(locationService.calculateETA(any())).thenReturn(15L);

        mockMvc.perform(get("/api/public/tracking/TS123456789"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentStatus").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.data.courierName").value("Ahmed Courier"))
                .andExpect(jsonPath("$.data.lastCourierLocation.latitude").value(30.0444))
                .andExpect(jsonPath("$.data.estimatedMinutesToDelivery").value(15));
    }

    @Test
    @DisplayName("GET /api/public/tracking/{trackingNumber}/eta — in transit returns ETA")
    void getETA_inTransit_returnsEta() throws Exception {
        sampleShipment.setStatus(inTransitStatus);
        when(shipmentRepository.findByTrackingNumber("TS123456789"))
                .thenReturn(Optional.of(sampleShipment));
        when(locationService.calculateETA(any())).thenReturn(20L);

        mockMvc.perform(get("/api/public/tracking/TS123456789/eta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(20));
    }

    @Test
    @DisplayName("GET /api/public/tracking/{trackingNumber}/eta — delivered returns null ETA")
    void getETA_delivered_returnsNull() throws Exception {
        sampleShipment.setStatus(deliveredStatus);
        when(shipmentRepository.findByTrackingNumber("TS123456789"))
                .thenReturn(Optional.of(sampleShipment));

        mockMvc.perform(get("/api/public/tracking/TS123456789/eta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("الشحنة ليست في الطريق حالياً"));
    }

    @Test
    @DisplayName("GET /api/public/tracking/INVALID/eta — not found returns 404")
    void getETA_notFound_returns404() throws Exception {
        when(shipmentRepository.findByTrackingNumber("INVALID"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/public/tracking/INVALID/eta"))
                .andExpect(status().isNotFound());
    }
}
