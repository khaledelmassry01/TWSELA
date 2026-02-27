package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.security.JwtService;
import com.twsela.service.ShipmentService;
import com.twsela.web.dto.CreateShipmentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
class ShipmentControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ShipmentRepository shipmentRepository;
    @MockBean private ShipmentStatusHistoryRepository statusHistoryRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private ShipmentService shipmentService;
    @MockBean private ZoneRepository zoneRepository;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;

    private User ownerUser;
    private Zone zone;
    private ShipmentStatus pendingStatus;
    private Shipment sampleShipment;
    private Authentication ownerAuth;

    @BeforeEach
    void setUp() {
        Role ownerRole = new Role("OWNER");
        ownerRole.setId(1L);
        ownerUser = new User();
        ownerUser.setId(1L);
        ownerUser.setName("Owner");
        ownerUser.setPhone("0501234567");
        ownerUser.setRole(ownerRole);

        zone = new Zone();
        zone.setId(1L);
        zone.setName("Cairo");
        zone.setDefaultFee(new BigDecimal("50.00"));

        pendingStatus = new ShipmentStatus("PENDING", "قيد الانتظار");
        pendingStatus.setId(1L);

        RecipientDetails recipientDetails = new RecipientDetails("01012345678", "أحمد محمد", "القاهرة");

        sampleShipment = new Shipment();
        sampleShipment.setId(100L);
        sampleShipment.setTrackingNumber("TWS-ABCD1234");
        sampleShipment.setMerchant(ownerUser);
        sampleShipment.setZone(zone);
        sampleShipment.setStatus(pendingStatus);
        sampleShipment.setRecipientDetails(recipientDetails);
        sampleShipment.setItemValue(new BigDecimal("200.00"));
        sampleShipment.setCodAmount(new BigDecimal("200.00"));
        sampleShipment.setDeliveryFee(new BigDecimal("50.00"));
        sampleShipment.setCreatedAt(Instant.now());
        sampleShipment.setUpdatedAt(Instant.now());

        // ShipmentController.getCurrentUser → userRepository.findByPhone(auth.getName())
        ownerAuth = new UsernamePasswordAuthenticationToken(
                "0501234567", null, List.of(new SimpleGrantedAuthority("ROLE_OWNER")));

        when(userRepository.findByPhone("0501234567")).thenReturn(Optional.of(ownerUser));
    }

    // ======== GET /api/shipments ========

    @Test
    @DisplayName("GET /api/shipments — يجب إرجاع قائمة الشحنات مع pagination")
    void getAllShipments_success() throws Exception {
        Page<Shipment> page = new PageImpl<>(List.of(sampleShipment), PageRequest.of(0, 20), 1);
        when(shipmentRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/shipments").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.shipments").isArray());
    }

    // ======== GET /api/shipments/{id} ========

    @Test
    @DisplayName("GET /api/shipments/{id} — يجب إرجاع الشحنة عند وجود المعرف")
    void getShipmentById_found() throws Exception {
        when(shipmentRepository.findById(100L)).thenReturn(Optional.of(sampleShipment));

        mockMvc.perform(get("/api/shipments/100").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.shipment.trackingNumber").value("TWS-ABCD1234"));
    }

    @Test
    @DisplayName("GET /api/shipments/{id} — يجب إرجاع 404 عند عدم وجود الشحنة")
    void getShipmentById_notFound() throws Exception {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/shipments/999").with(authentication(ownerAuth)))
                .andExpect(status().isNotFound());
    }

    // ======== GET /api/shipments/count ========

    @Test
    @DisplayName("GET /api/shipments/count — يجب إرجاع عدد الشحنات")
    void getShipmentsCount_success() throws Exception {
        when(shipmentRepository.count()).thenReturn(42L);

        mockMvc.perform(get("/api/shipments/count").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(42));
    }

    // ======== POST /api/shipments ========

    @Test
    @DisplayName("POST /api/shipments — يجب إنشاء شحنة جديدة بنجاح")
    void createShipment_success() throws Exception {
        CreateShipmentRequest request = new CreateShipmentRequest();
        request.setRecipientName("أحمد محمد");
        request.setRecipientPhone("01012345678");
        request.setRecipientAddress("القاهرة - مصر");
        request.setPackageDescription("ملابس");
        request.setPackageWeight(new BigDecimal("2.5"));
        request.setItemValue(new BigDecimal("500.00"));
        request.setCodAmount(new BigDecimal("500.00"));
        request.setZoneId(1L);
        request.setPriority("STANDARD");
        request.setShippingFeePaidBy("MERCHANT");

        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(shipmentService.getStatusByName("PENDING")).thenReturn(Optional.of(pendingStatus));
        when(shipmentService.createShipment(any(Shipment.class))).thenReturn(sampleShipment);
        when(statusHistoryRepository.save(any(ShipmentStatusHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(post("/api/shipments")
                        .with(authentication(ownerAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/shipments — يجب إرجاع 400 عند بيانات ناقصة")
    void createShipment_missingFields() throws Exception {
        CreateShipmentRequest request = new CreateShipmentRequest();

        mockMvc.perform(post("/api/shipments")
                        .with(authentication(ownerAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ======== GET /api/shipments/warehouse/stats ========

    @Test
    @DisplayName("GET /api/shipments/warehouse/stats — يجب إرجاع إحصائيات المستودع")
    void getWarehouseStats_success() throws Exception {
        ShipmentStatus received = new ShipmentStatus("RECEIVED_AT_HUB", "تم الاستلام");
        received.setId(10L);
        ShipmentStatus assigned = new ShipmentStatus("ASSIGNED_TO_COURIER", "مُعيّن");
        assigned.setId(11L);
        ShipmentStatus returned = new ShipmentStatus("RETURNED_TO_HUB", "مرتجع");
        returned.setId(12L);

        when(shipmentService.getStatusByName("RECEIVED_AT_HUB")).thenReturn(Optional.of(received));
        when(shipmentService.getStatusByName("ASSIGNED_TO_COURIER")).thenReturn(Optional.of(assigned));
        when(shipmentService.getStatusByName("RETURNED_TO_HUB")).thenReturn(Optional.of(returned));
        when(shipmentRepository.countByStatusAndUpdatedAtBetween(any(), any(), any())).thenReturn(5L);
        when(shipmentRepository.countByStatusIn(anyList())).thenReturn(20L);
        when(shipmentRepository.countByStatus(any())).thenReturn(3L);

        mockMvc.perform(get("/api/shipments/warehouse/stats").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receivedToday").exists());
    }

    // ======== POST /api/shipments/warehouse/receive ========

    @Test
    @DisplayName("POST /api/shipments/warehouse/receive — يجب استلام الشحنات بنجاح")
    void receiveShipments_success() throws Exception {
        ShipmentStatus receivedStatus = new ShipmentStatus("RECEIVED_AT_HUB", "تم الاستلام");
        receivedStatus.setId(10L);

        when(shipmentRepository.findByTrackingNumber("TWS-ABCD1234"))
                .thenReturn(Optional.of(sampleShipment));
        when(shipmentService.getStatusByName("RECEIVED_AT_HUB"))
                .thenReturn(Optional.of(receivedStatus));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(inv -> inv.getArgument(0));
        when(statusHistoryRepository.save(any(ShipmentStatusHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Map<String, List<String>> body = Map.of("trackingNumbers", List.of("TWS-ABCD1234"));

        mockMvc.perform(post("/api/shipments/warehouse/receive")
                        .with(authentication(ownerAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
