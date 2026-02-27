package com.twsela.web;

import com.twsela.domain.User;
import com.twsela.repository.UserRepository;
import com.twsela.security.JwtService;
import com.twsela.util.AppUtils;
import com.twsela.service.AuditService;
import com.twsela.service.MetricsService;
import com.twsela.web.dto.LoginRequest;
import com.twsela.web.dto.LoginResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UserRepository userRepository, AuditService auditService, MetricsService metricsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.metricsService = metricsService;
    }

    @Operation(
        summary = "تسجيل الدخول",
        description = "تسجيل دخول المستخدم باستخدام رقم الهاتف وكلمة المرور والحصول على JWT Token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "تم تسجيل الدخول بنجاح",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "success": true,
                        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "user": {
                            "id": 1,
                            "name": "أحمد محمد",
                            "phone": "+201234567890",
                            "role": "MERCHANT"
                        },
                        "role": "MERCHANT",
                        "message": "Login successful"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "فشل في المصادقة - بيانات الدخول غير صحيحة",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Error Response",
                    value = """
                    {
                        "success": false,
                        "error": "Authentication failed",
                        "message": "Invalid phone number or password"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
        @Parameter(description = "بيانات تسجيل الدخول", required = true)
        @Valid @RequestBody LoginRequest loginRequest, 
        HttpServletRequest request) {
        try {
            String phone = loginRequest.getPhone();
            String password = loginRequest.getPassword();
            
            log.info("AuthController: Login attempt for phone: {}", phone);
            
            // Record login attempt
            metricsService.recordLoginAttempt();
            
            // Check if user exists first with optimized query
            User user = userRepository.findByPhoneWithRoleAndStatus(phone).orElse(null);
            if (user == null) {
                log.warn("AuthController: User not found for phone: {}", phone);
                metricsService.recordLoginFailure();
                auditService.logAuthentication("LOGIN_FAILED", phone, 
                    getClientIpAddress(request), request.getHeader("User-Agent"), false, "User not found");
                
                return AppUtils.unauthorized("Invalid phone number or password");
            }
            
            log.info("AuthController: User found - {} (Role: {}, Status: {})", user.getName(), user.getRole().getName(), user.getStatus().getName());
            
            // Check if user is active
            if (!user.isActive()) {
                log.warn("AuthController: User inactive - Status: {}, Deleted: {}", user.getStatus().getName(), user.getIsDeleted());
                metricsService.recordLoginFailure();
                auditService.logAuthentication("LOGIN_FAILED", phone, 
                    getClientIpAddress(request), request.getHeader("User-Agent"), false, "User inactive");
                
                return AppUtils.unauthorized("User account is inactive");
            }
            
            log.debug("AuthController: Attempting authentication for user: {}", user.getName());
            
            // Authenticate user
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(phone, password));
            
            log.info("AuthController: Authentication successful for user: {}", user.getName());
            
            // Generate JWT token
            Map<String, Object> claims = new HashMap<>();
            claims.put("role", "ROLE_" + user.getRole().getName().toUpperCase());
            String token = jwtService.generateToken(user.getPhone(), claims);
            
            // Record successful login
            metricsService.recordLoginSuccess();
            
            // Log successful login
            auditService.logAuthentication("LOGIN_SUCCESS", phone, 
                getClientIpAddress(request), request.getHeader("User-Agent"), true, null);
            
            Map<String, Object> body = new HashMap<>();
            body.put("success", true);
            body.put("token", token);
            body.put("user", new LoginResponseDTO(
                user.getId(),
                user.getName(),
                user.getPhone(),
                user.getRole().getName(),
                user.getStatus().getName()
            ));
            body.put("role", user.getRole().getName());
            body.put("message", "Login successful");
            
            log.info("AuthController: Login successful for user: {}", user.getName());
            return ResponseEntity.ok(body);
            
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Record failed login
            metricsService.recordLoginFailure();
            
            log.warn("AuthController: Authentication failed for phone: {} - {}", loginRequest.getPhone(), e.getMessage());
            
            // Log failed login
            auditService.logAuthentication("LOGIN_FAILED", loginRequest.getPhone(), 
                getClientIpAddress(request), request.getHeader("User-Agent"), false, e.getMessage());
            
            return AppUtils.unauthorized("Invalid phone number or password");
        } catch (Exception e) {
            // Handle other exceptions - CRITICAL FIX: Never return 500 for authentication failures
            log.error("AuthController: Unexpected error during login for phone: {} - {}", loginRequest.getPhone(), e.getMessage(), e);
            
            metricsService.recordLoginFailure();
            auditService.logAuthentication("LOGIN_FAILED", loginRequest.getPhone(), 
                getClientIpAddress(request), request.getHeader("User-Agent"), false, e.getMessage());
            
            // CRITICAL FIX: Return 401 instead of 500 for authentication failures
            return AppUtils.unauthorized("Invalid phone number or password");
        }
    }

    @Operation(
        summary = "الحصول على بيانات المستخدم الحالي",
        description = "الحصول على بيانات المستخدم المسجل حالياً باستخدام JWT Token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "تم الحصول على بيانات المستخدم بنجاح",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = User.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "غير مصرح - Token غير صحيح أو منتهي الصلاحية"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "المستخدم غير موجود"
        )
    })
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        log.debug("AuthController: /me endpoint called");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("AuthController: Authentication object: {}", authentication);
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("AuthController: No authentication or not authenticated");
            return ResponseEntity.status(401).build();
        }
        
        String phone = authentication.getName();
        log.debug("AuthController: Phone from authentication: {}", phone);
        
        User user = userRepository.findByPhone(phone).orElse(null);
        if (user == null) {
            log.warn("AuthController: User not found for phone: {}", phone);
            return ResponseEntity.status(404).build();
        }
        
        log.info("AuthController: User found: {} ({})", user.getName(), user.getRole().getName());
        return ResponseEntity.ok(user);
    }

    @Operation(
        summary = "فحص حالة خدمة المصادقة",
        description = "فحص حالة خدمة المصادقة والتأكد من عملها بشكل صحيح"
    )
    @ApiResponse(
        responseCode = "200",
        description = "الخدمة تعمل بشكل طبيعي",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = Map.class),
            examples = @ExampleObject(
                name = "Health Check Response",
                value = """
                {
                    "status": "UP",
                    "message": "Auth service is running",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "version": "1.0.0"
                }
                """
            )
        )
    )
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> authHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Auth service is running");
        response.put("timestamp", java.time.Instant.now());
        response.put("version", "1.0.0");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "تسجيل الخروج", description = "تسجيل خروج المستخدم (إبطال الجلسة)")
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(Authentication authentication) {
        // Stateless JWT — client deletes token; server acknowledges
        String phone = authentication != null ? authentication.getName() : "unknown";
        log.info("AuthController: Logout requested by {}", phone);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Logged out successfully");
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "تغيير كلمة المرور", description = "تغيير كلمة المرور للمستخدم الحالي")
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> request,
            Authentication authentication) {
        
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        if (oldPassword == null || newPassword == null || newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "كلمة المرور الجديدة يجب أن تكون 6 أحرف على الأقل"
            ));
        }
        
        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "User not found"));
        }
        
        // Verify old password
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(phone, oldPassword));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "كلمة المرور الحالية غير صحيحة"
            ));
        }
        
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(newPassword));
        userRepository.save(user);
        
        log.info("AuthController: Password changed for user {}", phone);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "تم تغيير كلمة المرور بنجاح");
        return ResponseEntity.ok(body);
    }

    @Operation(summary = "تجديد التوكن", description = "الحصول على توكن جديد باستخدام التوكن الحالي")
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Not authenticated"));
        }
        
        String phone = authentication.getName();
        User user = userRepository.findByPhoneWithRoleAndStatus(phone).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "User not found"));
        }
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "ROLE_" + user.getRole().getName().toUpperCase());
        String newToken = jwtService.generateToken(user.getPhone(), claims);
        
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("token", newToken);
        body.put("message", "Token refreshed successfully");
        return ResponseEntity.ok(body);
    }

    /**
     * Get client IP address from request
     */
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



