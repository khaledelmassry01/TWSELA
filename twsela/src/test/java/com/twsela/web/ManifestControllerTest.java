package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.Role;
import com.twsela.domain.ShipmentManifest;
import com.twsela.domain.User;
import com.twsela.domain.UserStatus;
import com.twsela.repository.ShipmentManifestRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.security.JwtService;
import com.twsela.web.dto.CreateManifestRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ManifestController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(ManifestControllerTest.TestMethodSecurityConfig.class)
class ManifestControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ShipmentManifestRepository shipmentManifestRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private ShipmentRepository shipmentRepository;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        org.mockito.Mockito.when(authHelper.getCurrentUser(org.mockito.ArgumentMatchers.any(org.springframework.security.core.Authentication.class)))
                .thenAnswer(inv -> (User) ((org.springframework.security.core.Authentication) inv.getArgument(0)).getPrincipal());
    }

    private User createUser(String roleName, Long id) {
        Role role = new Role(roleName);
        role.setId(id);
        UserStatus activeStatus = new UserStatus("ACTIVE");
        activeStatus.setId(1L);
        User user = new User();
        user.setId(id);
        user.setName("Test " + roleName);
        user.setPhone("050123456" + id);
        user.setRole(role);
        user.setStatus(activeStatus);
        user.setIsDeleted(false);
        return user;
    }

    private Authentication createAuth(String roleName) {
        User user = createUser(roleName, 1L);
        return new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_" + roleName)));
    }

    @Test
    @DisplayName("GET /api/manifests — يجب إرجاع جميع المانيفست للمالك")
    void getAllManifests_success_owner() throws Exception {
        when(shipmentManifestRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/manifests").with(authentication(createAuth("OWNER"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/manifests/{id} — يجب إرجاع 404 عند عدم وجود المانيفست")
    void getManifestById_notFound() throws Exception {
        when(shipmentManifestRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/manifests/999").with(authentication(createAuth("OWNER"))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/manifests — يجب رفض الوصول للتاجر")
    void createManifest_forbidden_forMerchant() throws Exception {
        CreateManifestRequest request = new CreateManifestRequest(1L);

        mockMvc.perform(post("/api/manifests")
                        .with(authentication(createAuth("MERCHANT")))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/manifests/{id} — يجب إرجاع المانيفست عند وجوده")
    void getManifestById_success() throws Exception {
        User courier = createUser("COURIER", 2L);
        ShipmentManifest manifest = new ShipmentManifest(courier, "MAN-001");
        manifest.setId(1L);
        manifest.setStatus(ShipmentManifest.ManifestStatus.CREATED);
        manifest.setCreatedAt(Instant.now());

        when(shipmentManifestRepository.findById(1L)).thenReturn(Optional.of(manifest));

        mockMvc.perform(get("/api/manifests/1").with(authentication(createAuth("OWNER"))))
                .andExpect(status().isOk());
    }
}
