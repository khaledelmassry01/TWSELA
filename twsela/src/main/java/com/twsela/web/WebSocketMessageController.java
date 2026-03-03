package com.twsela.web;

import com.twsela.service.LiveTrackingService;
import com.twsela.service.ChatService;
import com.twsela.service.PresenceService;
import com.twsela.domain.ChatMessage;
import com.twsela.domain.LocationPing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * معالج رسائل WebSocket — يستقبل البيانات عبر STOMP ويعالجها.
 */
@Controller
public class WebSocketMessageController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketMessageController.class);

    private final LiveTrackingService liveTrackingService;
    private final ChatService chatService;
    private final PresenceService presenceService;

    public WebSocketMessageController(LiveTrackingService liveTrackingService,
                                       ChatService chatService,
                                       PresenceService presenceService) {
        this.liveTrackingService = liveTrackingService;
        this.chatService = chatService;
        this.presenceService = presenceService;
    }

    /**
     * استقبال نقطة موقع عبر WebSocket.
     * Client sends to: /app/tracking.ping
     */
    @MessageMapping("/tracking.ping")
    public void handleLocationPing(@Payload Map<String, Object> payload) {
        try {
            Long sessionId = toLong(payload.get("sessionId"));
            Double lat = toDouble(payload.get("lat"));
            Double lng = toDouble(payload.get("lng"));
            Float accuracy = toFloat(payload.get("accuracy"));
            Float speed = toFloat(payload.get("speed"));
            Float heading = toFloat(payload.get("heading"));
            Integer batteryLevel = toInteger(payload.get("batteryLevel"));

            liveTrackingService.processPing(sessionId, lat, lng, accuracy, speed, heading, batteryLevel);
        } catch (Exception e) {
            log.error("Error processing location ping: {}", e.getMessage());
        }
    }

    /**
     * استقبال رسالة محادثة عبر WebSocket.
     * Client sends to: /app/chat.send
     */
    @MessageMapping("/chat.send")
    public void handleChatMessage(@Payload Map<String, Object> payload) {
        try {
            Long roomId = toLong(payload.get("roomId"));
            Long senderId = toLong(payload.get("senderId"));
            String content = (String) payload.get("content");
            String typeStr = (String) payload.getOrDefault("messageType", "TEXT");
            ChatMessage.MessageType messageType = ChatMessage.MessageType.valueOf(typeStr);

            chatService.sendMessage(roomId, senderId, content, messageType);
        } catch (Exception e) {
            log.error("Error processing chat message: {}", e.getMessage());
        }
    }

    /**
     * تسجيل حالة الاتصال.
     * Client sends to: /app/presence.connect
     */
    @MessageMapping("/presence.connect")
    public void handlePresenceConnect(@Payload Map<String, Object> payload) {
        try {
            Long userId = toLong(payload.get("userId"));
            presenceService.userConnected(userId);
        } catch (Exception e) {
            log.error("Error processing presence connect: {}", e.getMessage());
        }
    }

    /**
     * تسجيل انقطاع الاتصال.
     * Client sends to: /app/presence.disconnect
     */
    @MessageMapping("/presence.disconnect")
    public void handlePresenceDisconnect(@Payload Map<String, Object> payload) {
        try {
            Long userId = toLong(payload.get("userId"));
            presenceService.userDisconnected(userId);
        } catch (Exception e) {
            log.error("Error processing presence disconnect: {}", e.getMessage());
        }
    }

    // ── Type conversion helpers ──

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        return Long.parseLong(value.toString());
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        return Double.parseDouble(value.toString());
    }

    private Float toFloat(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).floatValue();
        return Float.parseFloat(value.toString());
    }

    private Integer toInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        return Integer.parseInt(value.toString());
    }
}
