package com.twsela.web;

import com.twsela.domain.User;
import com.twsela.repository.UserRepository;
import com.twsela.security.AuthenticationHelper;
import com.twsela.security.JwtService;
import com.twsela.security.TokenBlacklistService;
import com.twsela.service.AuditService;
import com.twsela.service.MetricsService;
import com.twsela.web.dto.ChangePasswordRequest;
import com.twsela.web.dto.DtoMapper;
import com.twsela.web.dto.LoginRequest;
import com.twsela.web.dto.LoginResponseDTO;
import com.twsela.web.dto.UserResponseDTO;
import com.twsela.web.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "إدارة المصادقة وتسجيل الدخول")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final MetricsService metricsService;
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthenticationHelper authHelper;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserRepository userRepository,
                          AuditService auditService,
                          MetricsService metricsService,
                          TokenBlacklistService tokenBlacklistService,
                          AuthenticationHelper authHelper) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.metricsService = metricsService;
        this.tokenBlacklistService = tokenBlacklistService;
        this.authHelper = authHelper;
    }

    @Operation(summary = "تسجيل الدخول", description = "تسجيل دخول المستخدم والحصول على JWT Token")
    @PostMapping("/login")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Map<String, Object>>> login(
            @Parameter(description = "بيانات تسجيل الدخول", required = true)
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        try {
            String phone = loginRequest.getPhone();
            String password = loginRequest.getPassword();

            log.info("AuthController: Login attempt for phone: {}", phone);
            metricsService.recordLoginAttempt();

            User user = userRepository.findByPhoneWithRoleAndStatus(phone).orElse(null);
            if (user == null) {
                log.warn("AuthController: User not found for phone: {}", phone);
                metricsService.recordLoginFailure();
                auditService.logAuthentication("LOGIN_FAILED", phone,
                        getClientIpAddress(request), request.getHeader("User-Agent"), false, "User not found");
                return ResponseEntity.status(401)
                        .body(com.twsela.web.dto.ApiResponse.error("Invalid phone number or password"));
            }

            // Account lockout check
            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(java.time.Instant.now())) {
                log.warn("AuthController: Account locked for phone: {}", phone);
                metricsService.recordLoginFailure();
                return ResponseEntity.status(401)
                        .body(com.twsela.web.dto.ApiResponse.error("الحساب مقفل مؤقتاً. حاول مرة أخرى لاحقاً"));
            }

            if (!user.isActive()) {
                log.warn("AuthController: User inactive - Status: {}, Deleted: {}", user.getStatus().getName(), user.getIsDeleted());
                metricsService.recordLoginFailure();
                auditService.logAuthentication("LOGIN_FAILED", phone,
                        getClientIpAddress(request), request.getHeader("User-Agent"), false, "User inactive");
                return ResponseEntity.status(401)
                        .body(com.twsela.web.dto.ApiResponse.error("User account is inactive"));
            }

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(phone, password));

            // Reset failed attempts on successful login
            if (user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                user.setLockedUntil(null);
                userRepository.save(user);
            }

            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "ROLE_" + user.getRole().getName().toUpperCase());
            String token = jwtService.generateToken(user.getPhone(), claims);

            metricsService.recordLoginSuccess();
            auditService.logAuthentication("LOGIN_SUCCESS", phone,
                    getClientIpAddress(request), request.getHeader("User-Agent"), true, null);

            Map<String, Object> loginData = new HashMap<>();
            loginData.put("token", token);
            loginData.put("user", DtoMapper.toLoginDTO(user));
            loginData.put("role", user.getRole().getName());

            log.info("AuthController: Login successful for user: {}", user.getName());
            return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(loginData, "Login successful"));

        } catch (org.springframework.security.core.AuthenticationException e) {
            metricsService.recordLoginFailure();
            log.warn("AuthController: Authentication failed for phone: {} - {}", loginRequest.getPhone(), e.getMessage());
            auditService.logAuthentication("LOGIN_FAILED", loginRequest.getPhone(),
                    getClientIpAddress(request), request.getHeader("User-Agent"), false, e.getMessage());

            // Increment failed attempts
            incrementFailedAttempts(loginRequest.getPhone());

            return ResponseEntity.status(401)
                    .body(com.twsela.web.dto.ApiResponse.error("Invalid phone number or password"));
        } catch (Exception e) {
            log.error("AuthController: Unexpected error during login for phone: {} - {}", loginRequest.getPhone(), e.getMessage(), e);
            metricsService.recordLoginFailure();
            auditService.logAuthentication("LOGIN_FAILED", loginRequest.getPhone(),
                    getClientIpAddress(request), request.getHeader("User-Agent"), false, e.getMessage());
            return ResponseEntity.status(401)
                    .body(com.twsela.web.dto.ApiResponse.error("Invalid phone number or password"));
        }
    }

    @Operation(summary = "الحصول على بيانات المستخدم الحالي")
    @GetMapping("/me")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<UserResponseDTO>> getCurrentUser(Authentication authentication) {
        User user = authHelper.getCurrentUser(authentication);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(DtoMapper.toUserDTO(user)));
    }

    @Operation(summary = "فحص حالة خدمة المصادقة")
    @GetMapping("/health")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Map<String, Object>>> authHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("version", "1.0.0");
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(health, "Auth service is running"));
    }

    @Operation(summary = "تسجيل الخروج", description = "تسجيل خروج المستخدم (إبطال التوكن)")
    @PostMapping("/logout")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Void>> logout(
            Authentication authentication,
            HttpServletRequest request) {
        String phone = authentication != null ? authentication.getName() : "unknown";
        log.info("AuthController: Logout requested by {}", phone);

        // Blacklist the current token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            tokenBlacklistService.blacklist(authHeader.substring(7));
        }

        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok("Logged out successfully"));
    }

    @Operation(summary = "تغيير كلمة المرور")
    @PostMapping("/change-password")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            Authentication authentication) {

        User user = authHelper.getCurrentUser(authentication);

        // Verify old password
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getPhone(), req.getOldPassword()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(com.twsela.web.dto.ApiResponse.error("كلمة المرور الحالية غير صحيحة"));
        }

        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(req.getNewPassword()));
        userRepository.save(user);

        log.info("AuthController: Password changed for user {}", user.getPhone());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok("تم تغيير كلمة المرور بنجاح"));
    }

    @Operation(summary = "تجديد التوكن")
    @PostMapping("/refresh")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Map<String, String>>> refreshToken(Authentication authentication) {
        User user = authHelper.getCurrentUser(authentication);

        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_" + user.getRole().getName().toUpperCase());
        String newToken = jwtService.generateToken(user.getPhone(), claims);

        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(
                Map.of("token", newToken), "Token refreshed successfully"));
    }

    // ── Private helpers ──────────────────────────────────────────

    private void incrementFailedAttempts(String phone) {
        userRepository.findByPhone(phone).ifPresent(user -> {
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 5) {
                user.setLockedUntil(java.time.Instant.now().plusSeconds(15 * 60)); // 15 minutes
                log.warn("Account locked for user {} after {} failed attempts", phone, attempts);
            }
            userRepository.save(user);
        });
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}



