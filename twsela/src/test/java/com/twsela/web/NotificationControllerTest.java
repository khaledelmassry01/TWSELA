package com.twsela.web;

import com.twsela.domain.Notification;
import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationType;
import com.twsela.domain.Role;
import com.twsela.domain.User;
import com.twsela.domain.UserStatus;
import com.twsela.security.JwtService;
import com.twsela.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(NotificationControllerTest.TestMethodSecurityConfig.class)
class NotificationControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private NotificationService notificationService;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        Mockito.when(authHelper.getCurrentUser(any(Authentication.class)))
                .thenAnswer(inv -> (User) ((Authentication) inv.getArgument(0)).getPrincipal());
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

    private Notification createNotification(Long id, Long userId) {
        Notification n = new Notification(userId, NotificationType.SHIPMENT_STATUS, "عنوان", "رسالة");
        n.setId(id);
        n.setChannel(NotificationChannel.IN_APP);
        n.setCreatedAt(Instant.now());
        return n;
    }

    @Test
    @DisplayName("GET /api/notifications — returns paginated notifications")
    void getNotifications_success() throws Exception {
        Notification n = createNotification(1L, 1L);
        when(notificationService.getAll(eq(1L), any()))
                .thenReturn(new PageImpl<>(List.of(n), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/api/notifications").with(authentication(createAuth("OWNER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    @DisplayName("GET /api/notifications/unread — returns unread count and list")
    void getUnread_success() throws Exception {
        Notification n = createNotification(1L, 1L);
        when(notificationService.getUnread(1L)).thenReturn(List.of(n));
        when(notificationService.getUnreadCount(1L)).thenReturn(1L);

        mockMvc.perform(get("/api/notifications/unread").with(authentication(createAuth("OWNER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.count").value(1));
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read — marks as read")
    void markAsRead_success() throws Exception {
        when(notificationService.markAsRead(1L, 1L)).thenReturn(true);

        mockMvc.perform(put("/api/notifications/1/read")
                        .with(authentication(createAuth("OWNER")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/read — returns 404 for missing notification")
    void markAsRead_notFound() throws Exception {
        when(notificationService.markAsRead(999L, 1L)).thenReturn(false);

        mockMvc.perform(put("/api/notifications/999/read")
                        .with(authentication(createAuth("OWNER")))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/notifications/read-all — marks all as read")
    void markAllAsRead_success() throws Exception {
        when(notificationService.markAllAsRead(1L)).thenReturn(3);

        mockMvc.perform(put("/api/notifications/read-all")
                        .with(authentication(createAuth("OWNER")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
