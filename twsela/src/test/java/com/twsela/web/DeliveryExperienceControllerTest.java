package com.twsela.web;

import com.twsela.service.DeliveryExperienceService;
import com.twsela.web.dto.RecipientExperienceDTO.*;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.security.AuthenticationHelper;
import org.junit.jupiter.api.DisplayName;
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

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DeliveryExperienceController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(DeliveryExperienceControllerTest.TestMethodSecurityConfig.class)
class DeliveryExperienceControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @MockBean private DeliveryExperienceService experienceService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("يجب جلب طلبات إعادة التوجيه")
    void getRedirects_success() throws Exception {
        var r = new DeliveryRedirectResponse(1L, 1L, 1L, "CHANGE_ADDRESS", 2L,
                null, null, null, "REQUESTED", "تغيير العنوان",
                Instant.now(), null, null, Instant.now());
        when(experienceService.getRedirectsByShipment(1L)).thenReturn(List.of(r));

        mockMvc.perform(get("/api/delivery/redirects/shipment/1")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].redirectType").value("CHANGE_ADDRESS"));
    }

    @Test
    @DisplayName("يجب إنشاء طلب إعادة توجيه")
    void createRedirect_success() throws Exception {
        var r = new DeliveryRedirectResponse(1L, 1L, null, "HOLD", null,
                LocalDate.now().plusDays(3), null, null, "REQUESTED", null,
                Instant.now(), null, null, Instant.now());
        when(experienceService.createRedirect(any())).thenReturn(r);

        mockMvc.perform(post("/api/delivery/redirects")
                        .with(user("merchant").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shipmentId\":1,\"redirectType\":\"HOLD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب تقديم استبيان رضا")
    void submitSurvey_success() throws Exception {
        var s = new SatisfactionSurveyResponse(1L, 1L, 1L, 5, 5, 5, 4, "ممتاز", true, "سريع", Instant.now());
        when(experienceService.submitSurvey(any())).thenReturn(s);

        mockMvc.perform(post("/api/delivery/surveys")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shipmentId\":1,\"overallRating\":5,\"comment\":\"ممتاز\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب رفض الوصول لغير المصرح لهم")
    void getRedirects_forbidden() throws Exception {
        mockMvc.perform(get("/api/delivery/redirects/shipment/1")
                        .with(user("courier").roles("COURIER")))
                .andExpect(status().isForbidden());
    }
}
