package com.twsela.web;

import com.twsela.domain.DeadLetterEvent;
import com.twsela.domain.DomainEvent;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.DeadLetterService;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DeadLetterController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(DeadLetterControllerTest.TestMethodSecurityConfig.class)
@DisplayName("اختبارات وحدة التحكم بالأحداث الميتة")
class DeadLetterControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @MockBean private DeadLetterService deadLetterService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("GET /api/events/dead-letter — قائمة الأحداث غير المحلولة")
    void getUnresolved_shouldReturnList() throws Exception {
        DomainEvent orig = new DomainEvent();
        orig.setId(1L);
        orig.setEventId("uuid-orig");
        orig.setEventType("SHIPMENT_STATUS_CHANGED");

        DeadLetterEvent dle = new DeadLetterEvent();
        dle.setId(1L);
        dle.setOriginalEvent(orig);
        dle.setFailureReason("Processing failed");
        dle.setResolved(false);

        when(deadLetterService.getUnresolved()).thenReturn(List.of(dle));

        mockMvc.perform(get("/api/events/dead-letter")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].failureReason").value("Processing failed"));
    }

    @Test
    @DisplayName("POST /api/events/dead-letter/{id}/retry — إعادة محاولة")
    void retry_shouldRetry() throws Exception {
        DeadLetterEvent retried = new DeadLetterEvent();
        retried.setId(1L);
        retried.setFailureCount(2);
        retried.setResolved(false);

        when(deadLetterService.retry(1L)).thenReturn(retried);

        mockMvc.perform(post("/api/events/dead-letter/1/retry")
                        .with(user("01000000000").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/events/dead-letter/stats — إحصائيات")
    void getStats_shouldReturn() throws Exception {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("unresolvedCount", 3L);
        stats.put("resolvedCount", 7L);
        stats.put("totalCount", 10L);

        when(deadLetterService.getStats()).thenReturn(stats);

        mockMvc.perform(get("/api/events/dead-letter/stats")
                        .with(user("01000000000").roles("OWNER"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.unresolvedCount").value(3));
    }
}
