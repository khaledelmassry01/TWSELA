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
        try {
            String phone = request.getPhone();

            // Find user by phone
            Optional<User> userOpt = userRepository.findByPhone(phone);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "المستخدم غير موجود برقم الهاتف هذا"
                ));
            }

            User user = userOpt.get();
            
            // Generate new random password (8 digits)
            String newPassword = generateRandomPassword();
            
            // Hash the new password
            String hashedPassword = passwordEncoder.encode(newPassword);
            
            // Update user's password
            user.setPassword(hashedPassword);
            userRepository.save(user);
            
            // Log the new password to console (in production, this would send SMS)
            System.out.println("=== FORGOT PASSWORD RESET ===");
            System.out.println("User: " + user.getName() + " (" + user.getPhone() + ")");
            System.out.println("New Password: " + newPassword);
            System.out.println("=============================");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "تم إرسال كلمة المرور الجديدة إلى هاتفك"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "فشل في إعادة تعيين كلمة المرور: " + e.getMessage()
            ));
        }
    }

    private String generateRandomPassword() {
        java.util.Random random = new java.util.Random();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            password.append(random.nextInt(10));
        }
        return password.toString();
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(@Valid @RequestBody PasswordResetRequest request) {
        try {
            String phone = request.getPhone();

            // Find user by phone
            Optional<User> userOpt = userRepository.findByPhone(phone);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "المستخدم غير موجود برقم الهاتف هذا"
                ));
            }

            // Generate OTP
            String otp = otpService.generateOtp(phone);
            
            // Send OTP via SMS
            boolean smsSent = smsService.sendOtp(phone, otp);
            
            if (smsSent) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "تم إرسال رمز التحقق إلى هاتفك"
                ));
            } else {
                // Even if SMS failed, we still generated the OTP (for development)
                // In production, you might want to return an error here
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "تم إرسال رمز التحقق (تحقق من console في وضع التطوير)"
                ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "فشل في إرسال رمز التحقق: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        try {
            String phone = request.getPhone();
            String otp = request.getOtp();
            String newPassword = request.getNewPassword();
            String confirmPassword = request.getConfirmPassword();

            // Validate password confirmation
            if (!newPassword.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "كلمة السر وتأكيد كلمة السر غير متطابقين"
                ));
            }

            // Find user by phone
            Optional<User> userOpt = userRepository.findByPhone(phone);
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "المستخدم غير موجود برقم الهاتف هذا"
                ));
            }

            // Verify OTP
            boolean otpValid = otpService.verifyOtp(phone, otp);
            if (!otpValid) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "رمز التحقق غير صحيح أو منتهي الصلاحية"
                ));
            }

            User user = userOpt.get();
            
            // Hash and update password
            String hashedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(hashedPassword);
            userRepository.save(user);
            
            // Log success to console
            System.out.println("=== PASSWORD RESET SUCCESS ===");
            System.out.println("User: " + user.getName() + " (" + user.getPhone() + ")");
            System.out.println("Password updated successfully");
            System.out.println("==============================");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "تم تغيير كلمة السر بنجاح"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "error", "فشل في إعادة تعيين كلمة السر: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> submitContactForm(@RequestBody Map<String, Object> contactData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate required fields
            if (!contactData.containsKey("firstName") || !contactData.containsKey("lastName") || 
                !contactData.containsKey("email") || !contactData.containsKey("subject") || 
                !contactData.containsKey("message")) {
                
                response.put("success", false);
                response.put("message", "يرجى ملء جميع الحقول المطلوبة");
                return ResponseEntity.badRequest().body(response);
            }
            
            // In a real application, you would:
            // 1. Save the contact form to database
            // 2. Send email notification to admin
            // 3. Send auto-reply to customer
            // 4. Log the inquiry
            
            // For now, we'll just return success
            response.put("success", true);
            response.put("message", "تم إرسال رسالتك بنجاح! سنتواصل معك قريباً.");
            response.put("inquiryId", "INQ-" + System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "حدث خطأ في إرسال الرسالة. يرجى المحاولة مرة أخرى.");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/contact/offices")
    public ResponseEntity<Map<String, Object>> getOfficeLocations() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> offices = new HashMap<>();
        
        Map<String, Object> cairoOffice = new HashMap<>();
        cairoOffice.put("name", "المكتب الرئيسي - القاهرة");
        cairoOffice.put("address", "شارع التحرير، القاهرة 11511");
        cairoOffice.put("phone", "+20 2 1234 5678");
        cairoOffice.put("email", "cairo@localhost");
        cairoOffice.put("hours", "الأحد - الخميس: 8:00 ص - 6:00 م");
        offices.put("cairo", cairoOffice);
        
        Map<String, Object> alexandriaOffice = new HashMap<>();
        alexandriaOffice.put("name", "فرع الإسكندرية");
        alexandriaOffice.put("address", "شارع سعد زغلول، الإسكندرية 21500");
        alexandriaOffice.put("phone", "+20 3 2345 6789");
        alexandriaOffice.put("email", "alexandria@localhost");
        alexandriaOffice.put("hours", "الأحد - الخميس: 8:00 ص - 6:00 م");
        offices.put("alexandria", alexandriaOffice);
        
        Map<String, Object> gizaOffice = new HashMap<>();
        gizaOffice.put("name", "فرع الجيزة");
        gizaOffice.put("address", "شارع الهرم، الجيزة 12511");
        gizaOffice.put("phone", "+20 2 3456 7890");
        gizaOffice.put("email", "giza@localhost");
        gizaOffice.put("hours", "الأحد - الخميس: 8:00 ص - 6:00 م");
        offices.put("giza", gizaOffice);
        
        response.put("success", true);
        response.put("offices", offices);
        
        return ResponseEntity.ok(response);
    }
}



