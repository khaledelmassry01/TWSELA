package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.security.JwtService;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MasterDataController.class)
class MasterDataControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserRepository userRepository;
    @MockBean private ZoneRepository zoneRepository;
    @MockBean private DeliveryPricingRepository deliveryPricingRepository;
    @MockBean private TelemetrySettingsRepository telemetrySettingsRepository;
    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsService userDetailsService;

    private User ownerUser;
    private Zone sampleZone;
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

        sampleZone = new Zone();
        sampleZone.setId(1L);
        sampleZone.setName("Cairo");
        sampleZone.setDefaultFee(new BigDecimal("50.00"));

        // MasterDataController.getCurrentUser does: (User) authentication.getPrincipal()
        ownerAuth = new UsernamePasswordAuthenticationToken(
                ownerUser, null, List.of(new SimpleGrantedAuthority("ROLE_OWNER")));
    }

    // ======== GET /api/master/users ========

    @Test
    @DisplayName("GET /api/master/users — يجب إرجاع قائمة المستخدمين")
    void getAllUsers_owner() throws Exception {
        Page<User> page = new PageImpl<>(List.of(ownerUser), PageRequest.of(0, 10, Sort.by("id").descending()), 1);
        when(userRepository.findAllNonDeleted(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/master/users").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    // ======== POST /api/master/users ========

    @Test
    @DisplayName("POST /api/master/users — يجب إنشاء مستخدم جديد")
    void createUser_success() throws Exception {
        Role courierRole = new Role("COURIER");
        courierRole.setId(4L);
        User newUser = new User();
        newUser.setName("New Courier");
        newUser.setPhone("0508888888");
        newUser.setPassword("pass123");
        newUser.setRole(courierRole);

        User saved = new User();
        saved.setId(10L);
        saved.setName("New Courier");
        saved.setPhone("0508888888");
        saved.setRole(courierRole);

        when(passwordEncoder.encode("pass123")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        mockMvc.perform(post("/api/master/users")
                        .with(authentication(ownerAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    // ======== DELETE /api/master/users/{id} ========

    @Test
    @DisplayName("DELETE /api/master/users/{id} — يجب حذف مستخدم (soft delete)")
    void deleteUser_success() throws Exception {
        User target = new User();
        target.setId(5L);
        target.setName("Target");
        when(userRepository.findById(5L)).thenReturn(Optional.of(target));
        when(userRepository.save(any(User.class))).thenReturn(target);

        mockMvc.perform(delete("/api/master/users/5").with(authentication(ownerAuth)).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/master/users/{id} — يجب إرجاع 404 عند عدم وجود المستخدم")
    void deleteUser_notFound() throws Exception {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(delete("/api/master/users/999").with(authentication(ownerAuth)).with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ======== GET /api/master/zones ========

    @Test
    @DisplayName("GET /api/master/zones — يجب إرجاع جميع المناطق للمالك")
    void getAllZones_owner() throws Exception {
        when(zoneRepository.findAll()).thenReturn(List.of(sampleZone));

        mockMvc.perform(get("/api/master/zones").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Cairo"));
    }

    // ======== POST /api/master/zones ========

    @Test
    @DisplayName("POST /api/master/zones — يجب إنشاء منطقة جديدة")
    void createZone_success() throws Exception {
        Zone newZone = new Zone();
        newZone.setName("Alexandria");
        newZone.setDefaultFee(new BigDecimal("60.00"));

        Zone saved = new Zone();
        saved.setId(2L);
        saved.setName("Alexandria");
        saved.setDefaultFee(new BigDecimal("60.00"));

        when(zoneRepository.save(any(Zone.class))).thenReturn(saved);

        mockMvc.perform(post("/api/master/zones")
                        .with(authentication(ownerAuth))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newZone)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    // ======== GET /api/master/pricing ========

    @Test
    @DisplayName("GET /api/master/pricing — يجب إرجاع جميع الأسعار")
    void getAllPricing_success() throws Exception {
        when(deliveryPricingRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/master/pricing").with(authentication(ownerAuth)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
