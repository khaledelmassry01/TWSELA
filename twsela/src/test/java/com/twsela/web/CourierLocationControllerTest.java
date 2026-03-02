package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.CourierLocationHistory;
import com.twsela.domain.User;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = CourierLocationController.class,
    properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000",
        "spring.profiles.active=test"
    }
)
@AutoConfigureMockMvc(addFilters = false)
class CourierLocationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CourierLocationService locationService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    private User courierUser;

    @BeforeEach
    void setUp() {
        courierUser = new User();
        courierUser.setId(1L);
        courierUser.setName("Test Courier");
    }

    @Test
    @DisplayName("POST /api/couriers/location — success")
    void updateLocation_success() throws Exception {
        when(authHelper.getCurrentUserId(any())).thenReturn(1L);
        CourierLocationHistory saved = new CourierLocationHistory();
        saved.setId(1L);
        saved.setLatitude(new BigDecimal("30.0444"));
        saved.setLongitude(new BigDecimal("31.2357"));
        saved.setTimestamp(Instant.now());
        when(locationService.saveLocation(eq(1L), any(), any())).thenReturn(saved);

        mockMvc.perform(post("/api/couriers/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "latitude", 30.0444,
                                "longitude", 31.2357))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.latitude").value(30.0444));
    }

    @Test
    @DisplayName("POST /api/couriers/location — missing coordinates returns 400")
    void updateLocation_missingCoords_returns400() throws Exception {
        when(authHelper.getCurrentUserId(any())).thenReturn(1L);

        mockMvc.perform(post("/api/couriers/location")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/couriers/{id}/location — found")
    void getLastLocation_found() throws Exception {
        LocationDTO loc = new LocationDTO(new BigDecimal("30.0444"), new BigDecimal("31.2357"), Instant.now());
        when(locationService.getLastLocation(1L)).thenReturn(Optional.of(loc));

        mockMvc.perform(get("/api/couriers/1/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.latitude").value(30.0444));
    }

    @Test
    @DisplayName("GET /api/couriers/{id}/location — not found returns ok with null data")
    void getLastLocation_notFound() throws Exception {
        when(locationService.getLastLocation(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/couriers/999/location"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("لا يوجد موقع محفوظ"));
    }

    @Test
    @DisplayName("GET /api/couriers/{id}/location/history — returns list")
    void getLocationHistory_returnsList() throws Exception {
        LocationDTO l1 = new LocationDTO(new BigDecimal("30.0444"), new BigDecimal("31.2357"), Instant.now());
        LocationDTO l2 = new LocationDTO(new BigDecimal("30.0500"), new BigDecimal("31.2400"), Instant.now());
        when(locationService.getLocationHistory(1L)).thenReturn(List.of(l1, l2));

        mockMvc.perform(get("/api/couriers/1/location/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    @DisplayName("GET /api/couriers/{id}/location/history — empty returns empty list")
    void getLocationHistory_empty() throws Exception {
        when(locationService.getLocationHistory(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/couriers/1/location/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }
}
