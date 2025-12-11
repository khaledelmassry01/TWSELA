package com.twsela.web;

import com.twsela.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@PreAuthorize("hasRole('OWNER') or hasRole('ADMIN')")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendSms(
            @RequestParam String phoneNumber,
            @RequestParam String message) {
        
        try {
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
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "SMS sending failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, Object>> sendOtp(
            @RequestParam String phoneNumber) {
        
        try {
            // Generate 6-digit OTP
            String otp = String.format("%06d", (int) (Math.random() * 1000000));
            String message = "رمز التحقق الخاص بك هو: " + otp + ". صالح لمدة 5 دقائق.";
            
            boolean success = smsService.sendSms(phoneNumber, message);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("phoneNumber", phoneNumber);
            response.put("otp", otp); // In production, don't return OTP in response
            response.put("timestamp", java.time.Instant.now());
            
            if (success) {
                response.put("status", "OTP sent successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "Failed to send OTP");
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "OTP sending failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    @PostMapping("/send-notification")
    public ResponseEntity<Map<String, Object>> sendNotification(
            @RequestParam String phoneNumber,
            @RequestParam String notificationType,
            @RequestParam(required = false) String trackingNumber) {
        
        try {
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
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Notification sending failed");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

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
