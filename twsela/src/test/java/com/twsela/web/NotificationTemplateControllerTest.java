package com.twsela.web;

import com.twsela.domain.NotificationChannel;
import com.twsela.domain.NotificationTemplate;
import com.twsela.domain.NotificationType;
import com.twsela.repository.NotificationTemplateRepository;
import com.twsela.security.JwtService;
import com.twsela.service.NotificationAnalyticsService;
import com.twsela.service.NotificationDispatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = NotificationTemplateController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(NotificationTemplateControllerTest.TestMethodSecurityConfig.class)
class NotificationTemplateControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;

    @MockBean private NotificationTemplateRepository templateRepository;
    @MockBean private NotificationDispatcher dispatcher;
    @MockBean private NotificationAnalyticsService analyticsService;
    @MockBean private JwtService jwtService;
    @MockBean private com.twsela.security.TokenBlacklistService tokenBlacklistService;
    @MockBean private com.twsela.security.AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("يجب عرض جميع القوالب للمسؤول")
    void getAllTemplates_admin() throws Exception {
        NotificationTemplate template = new NotificationTemplate();
        template.setId(1L);
        template.setEventType(NotificationType.SHIPMENT_CREATED);
        template.setChannel(NotificationChannel.EMAIL);
        template.setSubjectTemplate("شحنة جديدة");
        template.setBodyTemplateAr("تم إنشاء شحنة جديدة");
        template.setBodyTemplateEn("New shipment created");
        template.setActive(true);
        template.setCreatedAt(Instant.now());
        template.setUpdatedAt(Instant.now());

        when(templateRepository.findAll()).thenReturn(List.of(template));

        mockMvc.perform(get("/api/admin/notifications/templates")
                        .with(user("admin@test.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].eventType").value("SHIPMENT_CREATED"));
    }

    @Test
    @DisplayName("يجب رفض الوصول لغير المسؤول")
    void getAllTemplates_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/notifications/templates")
                        .with(user("merchant@test.com").roles("MERCHANT")))
                .andExpect(status().isForbidden());
    }
}
