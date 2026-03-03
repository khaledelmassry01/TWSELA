package com.twsela.web;

import com.twsela.domain.*;
import com.twsela.domain.NotificationPreference.DigestMode;
import com.twsela.repository.NotificationPreferenceRepository;
import com.twsela.repository.UserRepository;
import com.twsela.security.JwtService;
import com.twsela.service.PushNotificationService;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationPreferenceController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(NotificationPreferenceControllerTest.TestMethodSecurityConfig.class)
class NotificationPreferenceControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private NotificationPreferenceRepository preferenceRepository;
    @MockBean private UserRepository userRepository;
    @MockBean private PushNotificationService pushService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        when(authHelper.getCurrentUserId(any(Authentication.class))).thenReturn(1L);
    }

    private Authentication createAuth() {
        Role role = new Role("MERCHANT");
        role.setId(2L);
        UserStatus active = new UserStatus("ACTIVE");
        active.setId(1L);
        User user = new User();
        user.setId(1L);
        user.setName("Test Merchant");
        user.setPhone("0501234567");
        user.setRole(role);
        user.setStatus(active);
        user.setIsDeleted(false);
        return new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_MERCHANT")));
    }

    @Test
    @DisplayName("يجب الحصول على التفضيلات الافتراضية عند عدم وجود تفضيلات محفوظة")
    void getPreferences_defaultWhenNotFound() throws Exception {
        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/notifications/preferences")
                        .with(user("0501234567").roles("MERCHANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب الحصول على التفضيلات المحفوظة")
    void getPreferences_existing() throws Exception {
        User testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test");
        testUser.setPhone("0501234567");

        NotificationPreference pref = new NotificationPreference();
        pref.setId(1L);
        pref.setUser(testUser);
        pref.setEnabledChannelsJson("{\"SHIPMENT_CREATED\":[\"EMAIL\",\"PUSH\"]}");
        pref.setDigestMode(DigestMode.DAILY);

        when(preferenceRepository.findByUserId(1L)).thenReturn(Optional.of(pref));

        mockMvc.perform(get("/api/notifications/preferences")
                        .with(user("0501234567").roles("MERCHANT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.digestMode").value("DAILY"));
    }

    @Test
    @DisplayName("يجب تسجيل جهاز جديد بنجاح")
    void registerDevice_success() throws Exception {
        User testUser = new User();
        testUser.setId(1L);

        DeviceToken dt = new DeviceToken();
        dt.setId(1L);
        dt.setToken("fcm-token-123");
        dt.setPlatform(DeviceToken.Platform.ANDROID);
        dt.setActive(true);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(pushService.registerToken(eq(1L), eq("fcm-token-123"), eq(DeviceToken.Platform.ANDROID), any(User.class)))
                .thenReturn(dt);

        mockMvc.perform(post("/api/notifications/devices")
                        .with(user("0501234567").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"fcm-token-123\",\"platform\":\"ANDROID\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("fcm-token-123"));
    }
}
