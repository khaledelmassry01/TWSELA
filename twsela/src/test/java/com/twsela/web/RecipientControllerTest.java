package com.twsela.web;

import com.twsela.service.RecipientProfileService;
import com.twsela.service.DeliverySchedulingService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RecipientController.class, properties = {
        "app.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1qd3QtdG9rZW4tbXVzdC1iZS1sb25n",
        "app.security.jwt.expiration-ms=3600000"
})
@Import(RecipientControllerTest.TestMethodSecurityConfig.class)
class RecipientControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {}

    @Autowired private MockMvc mockMvc;
    @MockBean private RecipientProfileService profileService;
    @MockBean private DeliverySchedulingService schedulingService;
    @MockBean private JwtService jwtService;
    @MockBean private TokenBlacklistService tokenBlacklistService;
    @MockBean private AuthenticationHelper authHelper;
    @MockBean private UserDetailsService userDetailsService;

    @Test
    @DisplayName("يجب جلب ملف المستلم")
    void getProfile_success() throws Exception {
        var resp = new RecipientProfileResponse(1L, "01234567890", "أحمد", null, "AR", null, null, null, 5, Instant.now());
        when(profileService.getById(1L)).thenReturn(resp);

        mockMvc.perform(get("/api/recipients/1")
                        .with(user("admin").roles("OWNER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value("01234567890"));
    }

    @Test
    @DisplayName("يجب إنشاء ملف مستلم")
    void createProfile_success() throws Exception {
        var resp = new RecipientProfileResponse(1L, "01234567890", "أحمد", null, "AR", null, null, null, 0, Instant.now());
        when(profileService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/recipients")
                        .with(user("admin").roles("OWNER"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"01234567890\",\"name\":\"أحمد\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب جلب فترات التوصيل")
    void getSlots_success() throws Exception {
        var slot = new DeliveryTimeSlotResponse(1L, 1L, 1, LocalTime.of(9, 0), LocalTime.of(12, 0),
                50, 5, true, BigDecimal.ZERO, "صباحي", Instant.now());
        when(schedulingService.getSlotsByZone(1L)).thenReturn(List.of(slot));

        mockMvc.perform(get("/api/delivery/slots/zone/1")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].displayNameAr").value("صباحي"));
    }

    @Test
    @DisplayName("يجب إنشاء حجز توصيل")
    void createBooking_success() throws Exception {
        var resp = new DeliveryBookingResponse(1L, 1L, 1L, 1L, LocalDate.now(), "BOOKED", null, null, Instant.now(), Instant.now());
        when(schedulingService.createBooking(any())).thenReturn(resp);

        mockMvc.perform(post("/api/delivery/bookings")
                        .with(user("admin").roles("MERCHANT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"deliveryTimeSlotId\":1,\"selectedDate\":\"2025-06-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("يجب رفض الوصول لغير المصرح لهم")
    void getProfile_forbidden() throws Exception {
        mockMvc.perform(get("/api/recipients/1")
                        .with(user("courier").roles("COURIER")))
                .andExpect(status().isForbidden());
    }
}
