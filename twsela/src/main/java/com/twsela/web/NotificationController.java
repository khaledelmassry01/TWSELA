package com.twsela.web;

import com.twsela.domain.NotificationLog;
import com.twsela.domain.User;
import com.twsela.repository.NotificationLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "إدارة الإشعارات")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationLogRepository notificationLogRepository;

    public NotificationController(NotificationLogRepository notificationLogRepository) {
        this.notificationLogRepository = notificationLogRepository;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        String phone = currentUser.getPhone();

        List<NotificationLog> notifications = notificationLogRepository.findByRecipientPhoneOrderBySentAtDesc(phone);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", notifications);
        response.put("count", notifications.size());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id, Authentication authentication) {
        NotificationLog notification = notificationLogRepository.findById(id).orElse(null);
        if (notification == null) {
            return ResponseEntity.status(404).body(Map.of("success", false, "message", "Notification not found"));
        }

        notification.setStatus("READ");
        notificationLogRepository.save(notification);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "تم تحديد الإشعار كمقروء");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        String phone = currentUser.getPhone();

        List<NotificationLog> notifications = notificationLogRepository.findByRecipientPhoneOrderBySentAtDesc(phone);
        for (NotificationLog n : notifications) {
            if (!"READ".equals(n.getStatus())) {
                n.setStatus("READ");
                notificationLogRepository.save(n);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "تم تحديد جميع الإشعارات كمقروءة");
        return ResponseEntity.ok(response);
    }
}
