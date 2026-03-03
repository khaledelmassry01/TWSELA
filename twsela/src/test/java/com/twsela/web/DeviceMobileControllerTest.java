package com.twsela.web;

import com.twsela.service.DeviceMobileService;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.security.AuthenticationHelper;
import com.twsela.web.dto.OfflineMobileDTO.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DeviceMobileController.class, properties = {
    "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
    "app.security.jwt.expiration-ms=3600000"
})
@Import(DeviceMobileControllerTest.TestMethodSecurityConfig.class)
class DeviceMobileControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private DeviceMobileService mobileService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authenticationHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    void registerDevice_shouldReturnCreated() throws Exception {
        var response = new DeviceRegistrationResponse(1L, 10L, "dev-abc", "ANDROID",
                "14", "1.0.0", "tok-123", null, 1L, LocalDateTime.now());
        when(mobileService.registerDevice(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/mobile/devices")
                        .with(user("courier").roles("COURIER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"userId":10,"deviceId":"dev-abc","platform":"ANDROID"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.platform").value("ANDROID"));
    }

    @Test
    void createAppVersionConfig_shouldReturnCreated() throws Exception {
        var response = new AppVersionConfigResponse(1L, "ANDROID", "1.0.0", "2.0.0",
                "https://play.google.com", false, "New features", LocalDateTime.now());
        when(mobileService.createAppVersionConfig(any())).thenReturn(response);

        mockMvc.perform(post("/api/mobile/app-versions")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"platform":"ANDROID","minVersion":"1.0.0","currentVersion":"2.0.0"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentVersion").value("2.0.0"));
    }

    @Test
    void getDevices_forbidden_forCourier() throws Exception {
        mockMvc.perform(get("/api/mobile/devices/user/10")
                        .with(user("courier").roles("COURIER")))
                .andExpect(status().isForbidden());
    }
}
