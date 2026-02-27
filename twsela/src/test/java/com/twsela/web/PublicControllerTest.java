package com.twsela.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.Role;
import com.twsela.domain.User;
import com.twsela.domain.UserStatus;
import com.twsela.repository.ServiceFeedbackRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.security.JwtService;
import com.twsela.service.OtpService;
import com.twsela.service.SmsService;
import com.twsela.web.dto.PasswordResetRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicController.class)
@AutoConfigureMockMvc(addFilters = false)
class PublicControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private ShipmentRepository shipmentRepository;
    @MockBean private ServiceFeedbackRepository feedbackRepository;
    @MockBean private JwtService jwtService;
    @MockBean private UserRepository userRepository;
    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private OtpService otpService;
    @MockBean private SmsService smsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        Role role = new Role("MERCHANT");
        role.setId(1L);
        UserStatus active = new UserStatus();
        active.setId(1L);
        active.setName("ACTIVE");

        testUser = new User();
        testUser.setId(1L);
        testUser.setName("أحمد");
        testUser.setPhone("01012345678");
        testUser.setPassword("$2a$10$hashed");
        testUser.setRole(role);
        testUser.setStatus(active);
    }

    // ======== POST /api/public/forgot-password ========

    @Test
    @DisplayName("POST /forgot-password — يجب إرسال OTP بدلاً من إعادة تعيين مباشرة")
    void forgotPassword_success() throws Exception {
        when(userRepository.findByPhone("01012345678")).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp("01012345678")).thenReturn("123456");
        when(smsService.sendOtp("01012345678", "123456")).thenReturn(true);

        String body = objectMapper.writeValueAsString(new PasswordResetRequest("01012345678", null, null, null));

        mockMvc.perform(post("/api/public/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify OTP was generated instead of direct password reset
        verify(otpService).generateOtp("01012345678");
        verify(smsService).sendOtp("01012345678", "123456");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("POST /forgot-password — يجب إرجاع 400 إذا المستخدم غير موجود")
    void forgotPassword_userNotFound() throws Exception {
        when(userRepository.findByPhone("09999999999")).thenReturn(Optional.empty());

        String body = objectMapper.writeValueAsString(new PasswordResetRequest("09999999999", null, null, null));

        mockMvc.perform(post("/api/public/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ======== POST /api/public/send-otp ========

    @Test
    @DisplayName("POST /send-otp — يجب إرسال رمز التحقق بنجاح")
    void sendOtp_success() throws Exception {
        when(userRepository.findByPhone("01012345678")).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp("01012345678")).thenReturn("123456");
        when(smsService.sendOtp("01012345678", "123456")).thenReturn(true);

        String body = objectMapper.writeValueAsString(new PasswordResetRequest("01012345678", null, null, null));

        mockMvc.perform(post("/api/public/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /send-otp — يجب إرجاع 400 إذا المستخدم غير موجود")
    void sendOtp_userNotFound() throws Exception {
        when(userRepository.findByPhone("09999999999")).thenReturn(Optional.empty());

        String body = objectMapper.writeValueAsString(new PasswordResetRequest("09999999999", null, null, null));

        mockMvc.perform(post("/api/public/send-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ======== POST /api/public/reset-password ========

    @Test
    @DisplayName("POST /reset-password — يجب إعادة تعيين كلمة المرور عبر OTP بنجاح")
    void resetPassword_success() throws Exception {
        when(userRepository.findByPhone("01012345678")).thenReturn(Optional.of(testUser));
        when(otpService.verifyOtp("01012345678", "123456")).thenReturn(true);
        when(passwordEncoder.encode("newpass123")).thenReturn("$2a$10$encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String body = objectMapper.writeValueAsString(
                new PasswordResetRequest("01012345678", "123456", "newpass123", "newpass123"));

        mockMvc.perform(post("/api/public/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /reset-password — يجب رفض الطلب إذا كلمتا المرور غير متطابقتين")
    void resetPassword_mismatch() throws Exception {
        String body = objectMapper.writeValueAsString(
                new PasswordResetRequest("01012345678", "123456", "pass1", "pass2"));

        mockMvc.perform(post("/api/public/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /reset-password — يجب رفض OTP غير صحيح")
    void resetPassword_invalidOtp() throws Exception {
        when(userRepository.findByPhone("01012345678")).thenReturn(Optional.of(testUser));
        when(otpService.verifyOtp("01012345678", "000000")).thenReturn(false);

        String body = objectMapper.writeValueAsString(
                new PasswordResetRequest("01012345678", "000000", "newpass", "newpass"));

        mockMvc.perform(post("/api/public/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ======== GET /api/public/contact/offices ========

    @Test
    @DisplayName("GET /contact/offices — يجب إرجاع مواقع المكاتب")
    void getOfficeLocations_success() throws Exception {
        mockMvc.perform(get("/api/public/contact/offices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.offices").isNotEmpty());
    }
}
