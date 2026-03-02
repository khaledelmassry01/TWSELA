package com.twsela.web;

import com.twsela.domain.Notification;
import com.twsela.domain.User;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.NotificationService;
import com.twsela.web.dto.NotificationDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "إدارة الإشعارات")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;
    private final AuthenticationHelper authHelper;

    public NotificationController(NotificationService notificationService, AuthenticationHelper authHelper) {
        this.notificationService = notificationService;
        this.authHelper = authHelper;
    }

    @GetMapping
    @Operation(summary = "الحصول على الإشعارات", description = "قائمة الإشعارات مع تصفح الصفحات")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Page<NotificationDTO>>> getNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        User currentUser = authHelper.getCurrentUser(authentication);
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationDTO> notifications = notificationService.getAll(currentUser.getId(), pageable)
                .map(this::toDTO);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(notifications));
    }

    @GetMapping("/unread")
    @Operation(summary = "الإشعارات غير المقروءة", description = "عدد وقائمة الإشعارات غير المقروءة")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Map<String, Object>>> getUnread(Authentication authentication) {
        User currentUser = authHelper.getCurrentUser(authentication);
        Long userId = currentUser.getId();
        List<NotificationDTO> unread = notificationService.getUnread(userId).stream()
                .map(this::toDTO).toList();
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(
                Map.of("count", count, "notifications", unread)));
    }

    @PutMapping("/{id}/read")
    @Operation(summary = "تحديد كمقروء", description = "تحديد إشعار واحد كمقروء")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Void>> markAsRead(
            @PathVariable Long id, Authentication authentication) {
        User currentUser = authHelper.getCurrentUser(authentication);
        boolean success = notificationService.markAsRead(id, currentUser.getId());
        if (!success) {
            return ResponseEntity.status(404)
                    .body(com.twsela.web.dto.ApiResponse.error("الإشعار غير موجود"));
        }
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok("تم تحديد الإشعار كمقروء"));
    }

    @PutMapping("/read-all")
    @Operation(summary = "تحديد الكل كمقروء", description = "تحديد جميع الإشعارات كمقروءة")
    public ResponseEntity<com.twsela.web.dto.ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        User currentUser = authHelper.getCurrentUser(authentication);
        int count = notificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(com.twsela.web.dto.ApiResponse.ok(
                "تم تحديد " + count + " إشعار كمقروء"));
    }

    private NotificationDTO toDTO(Notification n) {
        return new NotificationDTO(
                n.getId(), n.getType().name(), n.getChannel().name(),
                n.getTitle(), n.getMessage(), n.getActionUrl(),
                n.isRead(), n.getCreatedAt(), n.getReadAt());
    }
}
