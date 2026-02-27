package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.Role;
import com.twsela.domain.User;
import com.twsela.domain.UserStatus;
import com.twsela.repository.UserRepository;
import com.twsela.security.JwtService;
import com.twsela.service.AuditService;
import com.twsela.service.MetricsService;
import com.twsela.web.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private AuditService auditService;

    @MockBean
    private MetricsService metricsService;

    private User testUser;
    private Role testRole;
    private UserStatus activeStatus;

    @BeforeEach
    void setUp() {
        testRole = new Role("MERCHANT");
        testRole.setId(1L);

        activeStatus = new UserStatus();
        activeStatus.setId(1L);
        activeStatus.setName("ACTIVE");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setPhone("0501234567");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setRole(testRole);
        testUser.setStatus(activeStatus);
        testUser.setIsDeleted(false);
    }

    @Test
    @DisplayName("POST /api/auth/login - successful login returns token")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("0501234567", "password123");

        when(userRepository.findByPhoneWithRoleAndStatus("0501234567"))
                .thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(testUser.getPhone(), null));
        when(jwtService.generateToken(anyString(), any(Map.class)))
                .thenReturn("test.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("test.jwt.token"))
                .andExpect(jsonPath("$.role").value("MERCHANT"));

        verify(metricsService).recordLoginAttempt();
    }

    @Test
    @DisplayName("POST /api/auth/login - user not found returns 401")
    void login_UserNotFound() throws Exception {
        LoginRequest request = new LoginRequest("0509999999", "password123");

        when(userRepository.findByPhoneWithRoleAndStatus("0509999999"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login - inactive user returns 401")
    void login_InactiveUser() throws Exception {
        UserStatus inactiveStatus = new UserStatus();
        inactiveStatus.setId(2L);
        inactiveStatus.setName("INACTIVE");
        testUser.setStatus(inactiveStatus);

        LoginRequest request = new LoginRequest("0501234567", "password123");

        when(userRepository.findByPhoneWithRoleAndStatus("0501234567"))
                .thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login - bad credentials returns 401")
    void login_BadCredentials() throws Exception {
        LoginRequest request = new LoginRequest("0501234567", "wrongpassword");

        when(userRepository.findByPhoneWithRoleAndStatus("0501234567"))
                .thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/auth/login - missing phone returns 400")
    void login_MissingPhone() throws Exception {
        String body = "{\"password\": \"password123\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/auth/health - returns ok")
    void health_ReturnsOk() throws Exception {
        mockMvc.perform(get("/api/auth/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
