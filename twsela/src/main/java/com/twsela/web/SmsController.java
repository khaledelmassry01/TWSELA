package com.twsela.web;

import com.twsela.service.OtpService;
import com.twsela.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/sms")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
@Tag(name = "SMS", description = "إدارة الرسائل النصية القصيرة")
public class SmsController {

    private static final Logger log = LoggerFactory.getLogger(SmsController.class);
    private final SmsService smsService;
    private final OtpService otpService;

    public SmsController(SmsService smsService, OtpService otpService) {
        this.smsService = smsService;
        this.otpService = otpService;
    }

    @Operation(summary = "إرسال رسالة", description = "إرسال رسالة SMS لرقم هاتف")
    @ApiResponse(responseCode = "200", description = "تم الإرسال")
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendSms(
            @RequestParam String phoneNumber,
            @RequestParam String message) {
        
        boolean success = smsService.sendSms(phoneNumber, message);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("phoneNumber", phoneNumber);
        response.put("message", message);
        response.put("timestamp", java.time.Instant.now());
        
        if (success) {
            response.put("status", "SMS sent successfully");
            return ResponseEntity.ok(response);
        } else {
            response.put("status", "Failed to send SMS");
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "إرسال OTP", description = "إرسال رمز التحقق OTP")
    @ApiResponse(responseCode = "200", description = "تم الإرسال")
    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(
            @RequestParam String phoneNumber) {
        
        // Generate secure OTP via OtpService (uses SecureRandom)
        String otp = otpService.generateOtp(phoneNumber);
        
        // Send OTP via SMS
        boolean success = smsService.sendOtp(phoneNumber, otp);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("phoneNumber", phoneNumber);
        // SECURITY: OTP is never returned in response
        response.put("timestamp", java.time.Instant.now());
        
        if (success) {
            log.info("OTP sent successfully to phone: {}", phoneNumber);
            response.put("status", "OTP sent successfully");
            return ResponseEntity.ok(response);
        } else {
            log.warn("Failed to send OTP to phone: {}", phoneNumber);
            response.put("status", "Failed to send OTP");
            return ResponseEntity.status(500).body(response);
        }
    }

    @Operation(summary = "إرسال إشعار", description = "إرسال إشعار SMS حسب النوع")
    @ApiResponse(responseCode = "200", description = "تم الإرسال")
    @PostMapping("/send-notification")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @RequestParam String phoneNumber,
            @RequestParam String notificationType,
            @RequestParam(required = false) String trackingNumber) {
        
            String message;
            
            switch (notificationType.toLowerCase()) {
                case "shipment_created":
                    message = "تم إنشاء شحنتك بنجاح. رقم التتبع: " + trackingNumber;
                    break;
                case "shipment_picked_up":
                    message = "تم استلام شحنتك من المتجر. رقم التتبع: " + trackingNumber;
                    break;
                case "shipment_in_transit":
                    message = "شحنتك في الطريق إليك. رقم التتبع: " + trackingNumber;
                    break;
                case "shipment_delivered":
                    message = "تم تسليم شحنتك بنجاح. رقم التتبع: " + trackingNumber;
                    break;
                case "shipment_delayed":
                    message = "تأخرت شحنتك. سنقوم بتحديثك قريباً. رقم التتبع: " + trackingNumber;
                    break;
                default:
                    message = "إشعار من تطبيق تسيلا";
            }
            
            boolean success = smsService.sendSms(phoneNumber, message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("phoneNumber", phoneNumber);
            response.put("notificationType", notificationType);
            response.put("message", message);
            response.put("timestamp", java.time.Instant.now());
            
            if (success) {
                response.put("status", "Notification sent successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "Failed to send notification");
                return ResponseEntity.status(500).body(response);
            }
    }

    @Operation(summary = "اختبار خدمة SMS", description = "اختبار الاتصال بخدمة الرسائل")
    @ApiResponse(responseCode = "200", description = "تم الاختبار")
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testSmsService() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "SMS Service Test");
        response.put("status", "Available");
        response.put("timestamp", java.time.Instant.now());
        response.put("endpoints", new String[]{
            "POST /api/sms/send",
            "POST /api/sms/send-otp", 
            "POST /api/sms/send-notification"
        });
        
        return ResponseEntity.ok(response);
    }
}
