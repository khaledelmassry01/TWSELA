package com.twsela.web;

import com.twsela.domain.ServiceFeedback;
import com.twsela.domain.Shipment;
import com.twsela.domain.ShipmentStatusHistory;
import com.twsela.domain.User;
import com.twsela.repository.ServiceFeedbackRepository;
import com.twsela.repository.ShipmentRepository;
import com.twsela.repository.UserRepository;
import com.twsela.service.OtpService;
import com.twsela.service.SmsService;
import com.twsela.util.AppUtils;
import com.twsela.web.dto.ContactFormRequest;
import com.twsela.web.dto.PasswordResetRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Public APIs", description = "الخدمات العامة المتاحة بدون مصادقة")
public class PublicController {

    private static final Logger log = LoggerFactory.getLogger(PublicController.class);

    @Value("${app.password.generation.length:12}")
    private int passwordGenerationLength;

    @Value("${app.password.generation.chars:ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%}")
    private String passwordGenerationChars;

    @Value("${app.contact.inquiry-prefix:INQ-}")
    private String inquiryPrefix;

    @Value("${app.office.default-hours:الأحد - الخميس: 8:00 ص - 6:00 م}")
    private String officeDefaultHours;

    @Value("${app.office.email-domain:localhost}")
    private String officeEmailDomain;

    private final ShipmentRepository shipmentRepository;
    private final ServiceFeedbackRepository feedbackRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final SmsService smsService;

    public PublicController(ShipmentRepository shipmentRepository, ServiceFeedbackRepository feedbackRepository, 
                          UserRepository userRepository, PasswordEncoder passwordEncoder, 
                          OtpService otpService, SmsService smsService) {
        this.shipmentRepository = shipmentRepository;
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.smsService = smsService;
    }

    @Operation(
        summary = "تتبع الشحنة",
        description = "تتبع حالة الشحنة باستخدام رقم التتبع"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "تم الحصول على تاريخ الشحنة بنجاح",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = List.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "الشحنة غير موجودة"
        )
    })
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<List<ShipmentStatusHistory>> track(
        @Parameter(description = "رقم التتبع", example = "TS123456789", required = true)
        @PathVariable String trackingNumber) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber).orElseThrow();
        return ResponseEntity.ok(shipment.getStatusHistory().stream().toList());
    }

    @Operation(
        summary = "إرسال تقييم الخدمة",
        description = "إرسال تقييم للخدمة المقدمة للشحنة"
    )
    @ApiResponse(
        responseCode = "200",
        description = "تم إرسال التقييم بنجاح",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ServiceFeedback.class)
        )
    )
    @PostMapping("/feedback/{trackingNumber}")
    public ResponseEntity<ServiceFeedback> feedback(
        @Parameter(description = "رقم التتبع", example = "TS123456789", required = true)
        @PathVariable String trackingNumber, 
        @Parameter(description = "بيانات التقييم", required = true)
        @RequestBody ServiceFeedback feedback) {
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber).orElseThrow();
        feedback.setShipment(shipment);
        return ResponseEntity.ok(feedbackRepository.save(feedback));
    }

    @Operation(
        summary = "نسيان كلمة المرور",
        description = "إعادة تعيين كلمة المرور باستخدام رقم الهاتف"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "تم إرسال كلمة المرور الجديدة بنجاح",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Map.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                    {
                        "success": true,
                        "message": "تم إرسال كلمة المرور الجديدة إلى هاتفك"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "المستخدم غير موجود"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "خطأ في الخادم"
        )
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
        @Parameter(description = "بيانات إعادة تعيين كلمة المرور", required = true)
        @Valid @RequestBody PasswordResetRequest request) {
            String phone = request.getPhone();

            // Find user by phone
            Optional<User> userOpt = userRepository.findByPhone(phone);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", ErrorMessages.USER_NOT_FOUND_BY_PHONE
                ));
            }

            // SECURITY FIX: Don't reset password directly. Send OTP instead.
            String otp = otpService.generateOtp(phone);
            boolean smsSent = smsService.sendOtp(phone, otp);
            
            log.info("Password reset OTP sent for user: {}", phone);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", ErrorMessages.OTP_SENT_WITH_RESET_INSTRUCTIONS
            ));
    }

    private String generateRandomPassword() {
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < passwordGenerationLength; i++) {
            password.append(passwordGenerationChars.charAt(random.nextInt(passwordGenerationChars.length())));
        }
        return password.toString();
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(@Valid @RequestBody PasswordResetRequest request) {
            String phone = request.getPhone();

            // Find user by phone
            Optional<User> userOpt = userRepository.findByPhone(phone);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", ErrorMessages.USER_NOT_FOUND_BY_PHONE
                ));
            }

            // Generate OTP
            String otp = otpService.generateOtp(phone);
            
            // Send OTP via SMS
            boolean smsSent = smsService.sendOtp(phone, otp);
            
            if (smsSent) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", ErrorMessages.OTP_SENT
                ));
            } else {
                // Even if SMS failed, we still generated the OTP (for development)
                // In production, you might want to return an error here
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", ErrorMessages.OTP_SENT_DEV_MODE
                ));
            }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
            String phone = request.getPhone();
            String otp = request.getOtp();
            String newPassword = request.getNewPassword();
            String confirmPassword = request.getConfirmPassword();

            // Validate password confirmation
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", ErrorMessages.PASSWORD_MISMATCH
                ));
            }

            // Find user by phone
            Optional<User> userOpt = userRepository.findByPhone(phone);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", ErrorMessages.USER_NOT_FOUND_BY_PHONE
                ));
            }

            // Verify OTP
            boolean otpValid = otpService.verifyOtp(phone, otp);
            if (!otpValid) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", ErrorMessages.OTP_INVALID
                ));
            }

            User user = userOpt.get();
            
            // Hash and update password
            String hashedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            
            // Log success
            log.info("Password reset via OTP completed for user: {}", user.getPhone());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", ErrorMessages.PASSWORD_CHANGED_SUCCESS
            ));
    }

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> submitContactForm(@Valid @RequestBody ContactFormRequest contactData) {
        Map<String, Object> response = new HashMap<>();
        
            // In a real application, you would:
            // 1. Save the contact form to database
            // 2. Send email notification to admin
            // 3. Send auto-reply to customer
            // 4. Log the inquiry
            
            // For now, we'll just return success
            response.put("success", true);
            response.put("message", ErrorMessages.CONTACT_FORM_SUCCESS);
            response.put("inquiryId", inquiryPrefix + System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
    }

    @GetMapping("/contact/offices")
    public ResponseEntity<Map<String, Object>> getOfficeLocations() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> offices = new HashMap<>();
        
        Map<String, Object> cairoOffice = new HashMap<>();
        cairoOffice.put("name", "المكتب الرئيسي - القاهرة");
        cairoOffice.put("address", "شارع التحرير، القاهرة 11511");
        cairoOffice.put("phone", "+20 2 1234 5678");
        cairoOffice.put("email", "cairo@" + officeEmailDomain);
        cairoOffice.put("hours", officeDefaultHours);
        offices.put("cairo", cairoOffice);
        
        Map<String, Object> alexandriaOffice = new HashMap<>();
        alexandriaOffice.put("name", "فرع الإسكندرية");
        alexandriaOffice.put("address", "شارع سعد زغلول، الإسكندرية 21500");
        alexandriaOffice.put("phone", "+20 3 2345 6789");
        alexandriaOffice.put("email", "alexandria@" + officeEmailDomain);
        alexandriaOffice.put("hours", officeDefaultHours);
        offices.put("alexandria", alexandriaOffice);
        
        Map<String, Object> gizaOffice = new HashMap<>();
        gizaOffice.put("name", "فرع الجيزة");
        gizaOffice.put("address", "شارع الهرم، الجيزة 12511");
        gizaOffice.put("phone", "+20 2 3456 7890");
        gizaOffice.put("email", "giza@" + officeEmailDomain);
        gizaOffice.put("hours", officeDefaultHours);
        offices.put("giza", gizaOffice);
        
        response.put("success", true);
        response.put("offices", offices);
        
        return ResponseEntity.ok(response);
    }
}



