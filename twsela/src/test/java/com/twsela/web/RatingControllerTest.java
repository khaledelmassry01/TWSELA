package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.*;
import com.twsela.repository.CourierRatingRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.security.JwtService;
import com.twsela.web.dto.CourierRatingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RatingController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(RatingControllerTest.TestMethodSecurityConfig.class)
class RatingControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CourierRatingRepository ratingRepository;
    @MockBean private ShipmentRepository shipmentRepository;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    private User testCourier;
    private Shipment testShipment;
    private ShipmentManifest testManifest;

    @BeforeEach
    void setUp() {
        Mockito.when(authHelper.getCurrentUser(any(Authentication.class)))
                .thenAnswer(inv -> (User) ((Authentication) inv.getArgument(0)).getPrincipal());

        // Set up courier
        testCourier = new User();
        testCourier.setId(10L);
        testCourier.setName("مندوب تيست");

        // Set up manifest with courier
        testManifest = new ShipmentManifest();
        testManifest.setId(1L);
        testManifest.setCourier(testCourier);

        // Set up shipment linked to manifest
        testShipment = new Shipment();
        testShipment.setId(1L);
        testShipment.setTrackingNumber("TWS-20250101-000001");
        testShipment.setManifest(testManifest);
    }

    private Authentication createAuth(String roleName) {
        Role role = new Role(roleName);
        role.setId(1L);
        UserStatus activeStatus = new UserStatus("ACTIVE");
        activeStatus.setId(1L);
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setPhone("0501234567");
        user.setRole(role);
        user.setStatus(activeStatus);
        user.setIsDeleted(false);
        return new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + roleName)));
    }

    @Test
    @DisplayName("POST /api/ratings — submits rating successfully")
    void submitRating_success() throws Exception {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        when(ratingRepository.findByShipmentId(1L)).thenReturn(Optional.empty());
        when(ratingRepository.save(any(CourierRating.class))).thenAnswer(inv -> {
            CourierRating r = inv.getArgument(0);
            r.setId(100L);
            return r;
        });

        CourierRatingRequest req = new CourierRatingRequest();
        req.setShipmentId(1L);
        req.setRating(5);
        req.setComment("ممتاز");

        mockMvc.perform(post("/api/ratings")
                        .with(authentication(createAuth("MERCHANT")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rating").value(5));
    }

    @Test
    @DisplayName("POST /api/ratings — returns 404 for missing shipment")
    void submitRating_shipmentNotFound() throws Exception {
        when(shipmentRepository.findById(999L)).thenReturn(Optional.empty());

        CourierRatingRequest req = new CourierRatingRequest();
        req.setShipmentId(999L);
        req.setRating(3);

        mockMvc.perform(post("/api/ratings")
                        .with(authentication(createAuth("MERCHANT")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/ratings — rejects duplicate rating")
    void submitRating_duplicate() throws Exception {
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));
        CourierRating existingRating = new CourierRating(testCourier, testShipment, 4);
        existingRating.setId(50L);
        when(ratingRepository.findByShipmentId(1L)).thenReturn(Optional.of(existingRating));

        CourierRatingRequest req = new CourierRatingRequest();
        req.setShipmentId(1L);
        req.setRating(5);

        mockMvc.perform(post("/api/ratings")
                        .with(authentication(createAuth("MERCHANT")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/ratings — rejects no courier assigned")
    void submitRating_noCourier() throws Exception {
        testShipment.setManifest(null);
        when(shipmentRepository.findById(1L)).thenReturn(Optional.of(testShipment));

        CourierRatingRequest req = new CourierRatingRequest();
        req.setShipmentId(1L);
        req.setRating(3);

        mockMvc.perform(post("/api/ratings")
                        .with(authentication(createAuth("MERCHANT")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /api/ratings/courier/{id} — returns ratings with average")
    void getCourierRatings_success() throws Exception {
        CourierRating r1 = new CourierRating(testCourier, testShipment, 5);
        r1.setId(1L);
        when(ratingRepository.findByCourierIdOrderByCreatedAtDesc(10L)).thenReturn(List.of(r1));
        when(ratingRepository.getAverageRatingByCourierId(10L)).thenReturn(4.5);
        when(ratingRepository.countByCourierId(10L)).thenReturn(5L);

        mockMvc.perform(get("/api/ratings/courier/10")
                        .with(authentication(createAuth("OWNER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.averageRating").value(4.5))
                .andExpect(jsonPath("$.data.totalRatings").value(5));
    }

    @Test
    @DisplayName("GET /api/ratings/shipment/{id} — returns rating")
    void getShipmentRating_success() throws Exception {
        CourierRating r = new CourierRating(testCourier, testShipment, 4);
        r.setId(1L);
        r.setComment("جيد");
        when(ratingRepository.findByShipmentId(1L)).thenReturn(Optional.of(r));

        mockMvc.perform(get("/api/ratings/shipment/1")
                        .with(authentication(createAuth("MERCHANT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.rating").value(4));
    }

    @Test
    @DisplayName("GET /api/ratings/shipment/{id} — returns 404 when no rating")
    void getShipmentRating_notFound() throws Exception {
        when(ratingRepository.findByShipmentId(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ratings/shipment/999")
                        .with(authentication(createAuth("MERCHANT"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/ratings — fails validation for invalid rating value")
    void submitRating_invalidRatingValue() throws Exception {
        CourierRatingRequest req = new CourierRatingRequest();
        req.setShipmentId(1L);
        req.setRating(0); // min is 1

        mockMvc.perform(post("/api/ratings")
                        .with(authentication(createAuth("MERCHANT")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
