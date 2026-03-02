package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.*;
import com.twsela.domain.ReturnShipment.ReturnStatusEnum;
import com.twsela.security.JwtService;
import com.twsela.service.ReturnService;
import com.twsela.web.dto.ReturnRequestDTO;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ReturnController.class,
    properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000",
        "spring.profiles.active=test"
    }
)
@AutoConfigureMockMvc(addFilters = false)
class ReturnControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ReturnService returnService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    private User adminUser;
    private ReturnShipment sampleReturn;

    @BeforeEach
    void setUp() {
        Role adminRole = new Role("ADMIN");
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setName("Admin User");
        adminUser.setRole(adminRole);

        Shipment shipment = new Shipment();
        shipment.setId(10L);
        shipment.setTrackingNumber("TS000010");

        sampleReturn = new ReturnShipment(shipment, "damaged item");
        sampleReturn.setId(100L);
        sampleReturn.setStatus(ReturnStatusEnum.RETURN_REQUESTED);
        sampleReturn.setReturnFee(new BigDecimal("25.00"));
        sampleReturn.setCreatedBy("Admin User");
    }

    @Test
    @DisplayName("POST /api/returns — success")
    void createReturn_success() throws Exception {
        when(authHelper.getCurrentUser(any())).thenReturn(adminUser);
        when(returnService.createReturn(eq(10L), eq("damaged item"), eq("notes"), eq("Admin User")))
                .thenReturn(sampleReturn);

        ReturnRequestDTO request = new ReturnRequestDTO();
        request.setShipmentId(10L);
        request.setReason("damaged item");
        request.setNotes("notes");

        mockMvc.perform(post("/api/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(100));
    }

    @Test
    @DisplayName("POST /api/returns — shipment not found returns 404")
    void createReturn_shipmentNotFound() throws Exception {
        when(authHelper.getCurrentUser(any())).thenReturn(adminUser);
        when(returnService.createReturn(anyLong(), anyString(), any(), anyString()))
                .thenThrow(new ResourceNotFoundException("Shipment", "id", 999L));

        ReturnRequestDTO request = new ReturnRequestDTO();
        request.setShipmentId(999L);
        request.setReason("test reason text");

        mockMvc.perform(post("/api/returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/returns — admin sees all returns")
    void getReturns_admin_seesAll() throws Exception {
        when(authHelper.getCurrentUserRole(any())).thenReturn("ADMIN");
        when(authHelper.getCurrentUserId(any())).thenReturn(1L);
        when(returnService.getAllReturns()).thenReturn(List.of(sampleReturn));

        mockMvc.perform(get("/api/returns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    @DisplayName("GET /api/returns/{id} — found")
    void getReturn_found() throws Exception {
        when(returnService.getReturnById(100L)).thenReturn(sampleReturn);

        mockMvc.perform(get("/api/returns/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.reason").value("damaged item"));
    }

    @Test
    @DisplayName("GET /api/returns/{id} — not found returns 404")
    void getReturn_notFound() throws Exception {
        when(returnService.getReturnById(999L))
                .thenThrow(new ResourceNotFoundException("ReturnShipment", "id", 999L));

        mockMvc.perform(get("/api/returns/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/returns/{id}/status — success")
    void updateStatus_success() throws Exception {
        sampleReturn.setStatus(ReturnStatusEnum.RETURN_APPROVED);
        when(returnService.updateStatus(100L, ReturnStatusEnum.RETURN_APPROVED)).thenReturn(sampleReturn);

        mockMvc.perform(put("/api/returns/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "RETURN_APPROVED"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RETURN_APPROVED"));
    }

    @Test
    @DisplayName("PUT /api/returns/{id}/assign — success")
    void assignCourier_success() throws Exception {
        User courier = new User();
        courier.setId(5L);
        courier.setName("Courier X");
        sampleReturn.setAssignedCourier(courier);
        sampleReturn.setStatus(ReturnStatusEnum.RETURN_PICKUP_ASSIGNED);
        when(returnService.assignCourier(100L, 5L)).thenReturn(sampleReturn);

        mockMvc.perform(put("/api/returns/100/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("courierId", 5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.assignedCourierName").value("Courier X"));
    }
}
