package com.twsela.web;

import com.twsela.domain.Role;
import com.twsela.domain.User;
import com.twsela.domain.UserStatus;
import com.twsela.security.JwtService;
import com.twsela.service.OtpService;
import com.twsela.service.SmsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = SmsController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(SmsControllerTest.TestMethodSecurityConfig.class)
class SmsControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private SmsService smsService;
    @MockBean private OtpService otpService;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

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
    @DisplayName("GET /api/sms/test — يجب إرجاع حالة الخدمة بنجاح")
    void testSmsService_success() throws Exception {
        mockMvc.perform(get("/api/sms/test").with(authentication(createAuth("OWNER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Available"));
    }

    @Test
    @DisplayName("POST /api/sms/send — يجب إرسال SMS بنجاح")
    void sendSms_success() throws Exception {
        when(smsService.sendSms(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/sms/send")
                        .with(authentication(createAuth("OWNER")))
                        .with(csrf())
                        .param("phoneNumber", "0501234567")
                        .param("message", "Test message"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/sms/send — يجب رفض الوصول للتاجر")
    void sendSms_forbidden_forMerchant() throws Exception {
        mockMvc.perform(post("/api/sms/send")
                        .with(authentication(createAuth("MERCHANT")))
                        .with(csrf())
                        .param("phoneNumber", "0501234567")
                        .param("message", "Test message"))
                .andExpect(status().isForbidden());
    }
}
