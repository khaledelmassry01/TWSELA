package com.twsela.web;

import com.twsela.domain.LiveNotification;
import com.twsela.security.AuthenticationHelper;
import com.twsela.service.LiveNotificationService;
import com.twsela.service.PresenceService;
import com.twsela.web.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/live-notifications")
@Tag(name = "Live Notifications", description = "الإشعارات الحية وحالة الاتصال")
public class LiveNotificationController {

    private static final Logger log = LoggerFactory.getLogger(LiveNotificationController.class);

    private final LiveNotificationService liveNotificationService;
    private final PresenceService presenceService;
    private final AuthenticationHelper authenticationHelper;

    public LiveNotificationController(LiveNotificationService liveNotificationService,
                                      PresenceService presenceService,
                                      AuthenticationHelper authenticationHelper) {
        this.liveNotificationService = liveNotificationService;
        this.presenceService = presenceService;
        this.authenticationHelper = authenticationHelper;
    }

    @Operation(summary = "الحصول على الإشعارات غير المقروءة")
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<LiveNotification>>> getUnread(
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        List<LiveNotification> unread = liveNotificationService.getUnread(userId);
        return ResponseEntity.ok(ApiResponse.ok(unread));
    }

    @Operation(summary = "عدد الإشعارات غير المقروءة")
    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUnreadCount(
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        long count = liveNotificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("count", count)));
    }

    @Operation(summary = "الحصول على كل الإشعارات")
    @GetMapping
    public ResponseEntity<ApiResponse<List<LiveNotification>>> getAll(
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        List<LiveNotification> all = liveNotificationService.getAll(userId);
        return ResponseEntity.ok(ApiResponse.ok(all));
    }

    @Operation(summary = "تعليم إشعار كمقروء")
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAsRead(
            @PathVariable Long notificationId) {
        LiveNotification notification = liveNotificationService.markAsRead(notificationId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "id", notification.getId(),
                "read", notification.isRead()
        ), "تم التعليم كمقروء"));
    }

    @Operation(summary = "تعليم كل الإشعارات كمقروءة")
    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAllAsRead(
            Authentication authentication) {
        Long userId = authenticationHelper.getCurrentUserId(authentication);
        liveNotificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of("marked", true), "تم تعليم كل الإشعارات كمقروءة"));
    }

    @Operation(summary = "الحصول على المستخدمين المتصلين")
    @GetMapping("/presence/online")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOnlineUsers() {
        Set<Long> online = presenceService.getOnlineUsers();
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "onlineUsers", online,
                "count", online.size()
        )));
    }

    @Operation(summary = "هل المستخدم متصل؟")
    @GetMapping("/presence/{userId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> isUserOnline(
            @PathVariable Long userId) {
        boolean online = presenceService.isOnline(userId);
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "userId", userId,
                "online", online
        )));
    }
}
