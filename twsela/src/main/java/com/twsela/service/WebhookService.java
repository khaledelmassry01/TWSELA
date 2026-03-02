package com.twsela.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twsela.domain.User;
import com.twsela.domain.WebhookEvent;
import com.twsela.domain.WebhookEvent.DeliveryStatus;
import com.twsela.domain.WebhookSubscription;
import com.twsela.repository.WebhookEventRepository;
import com.twsela.repository.WebhookSubscriptionRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages webhook subscriptions and dispatches events via HTTP POST.
 * Payloads are signed with HMAC-SHA256 (X-Webhook-Signature header).
 */
@Service
@Transactional
public class WebhookService {

    private static final Logger log = LoggerFactory.getLogger(WebhookService.class);
    private static final int MAX_ATTEMPTS = 5;
    private static final String HMAC_ALGO = "HmacSHA256";

    private final WebhookSubscriptionRepository subscriptionRepository;
    private final WebhookEventRepository eventRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public WebhookService(WebhookSubscriptionRepository subscriptionRepository,
                          WebhookEventRepository eventRepository,
                          ObjectMapper objectMapper) {
        this.subscriptionRepository = subscriptionRepository;
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // ── Subscription management ─────────────────────────────────

    public WebhookSubscription subscribe(User merchant, String url, List<String> eventTypes) {
        if (url == null || url.isBlank()) {
            throw new BusinessRuleException("عنوان URL مطلوب");
        }
        if (eventTypes == null || eventTypes.isEmpty()) {
            throw new BusinessRuleException("يجب تحديد حدث واحد على الأقل");
        }

        String secret = generateSecret();
        String events = String.join(",", eventTypes);
        WebhookSubscription sub = new WebhookSubscription(merchant, url, secret, events);
        log.info("New webhook subscription for merchant {} → {}", merchant.getId(), url);
        return subscriptionRepository.save(sub);
    }

    public void unsubscribe(Long subscriptionId, Long merchantId) {
        WebhookSubscription sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("WebhookSubscription", "id", subscriptionId));
        if (!sub.getMerchant().getId().equals(merchantId)) {
            throw new BusinessRuleException("لا يمكن حذف اشتراك تابع لتاجر آخر");
        }
        sub.setActive(false);
        sub.setUpdatedAt(Instant.now());
        subscriptionRepository.save(sub);
        log.info("Deactivated webhook subscription {}", subscriptionId);
    }

    @Transactional(readOnly = true)
    public List<WebhookSubscription> getSubscriptions(Long merchantId) {
        return subscriptionRepository.findByMerchantId(merchantId);
    }

    @Transactional(readOnly = true)
    public WebhookSubscription getSubscription(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WebhookSubscription", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<WebhookEvent> getEvents(Long subscriptionId, Pageable pageable) {
        return eventRepository.findBySubscriptionIdOrderByCreatedAtDesc(subscriptionId, pageable);
    }

    // ── Event dispatching ───────────────────────────────────────

    /**
     * Dispatch an event to all active subscribers of that event type.
     * Runs asynchronously so the main transaction is not slowed down.
     */
    @Async
    public void dispatch(String eventType, Map<String, Object> data) {
        List<WebhookSubscription> subscribers = subscriptionRepository.findActiveByEventType(eventType);
        if (subscribers.isEmpty()) {
            return;
        }

        String payload;
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("event", eventType);
            body.put("timestamp", Instant.now().toString());
            body.put("data", data);
            payload = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            log.error("Failed to serialize webhook payload for event {}", eventType, e);
            return;
        }

        for (WebhookSubscription sub : subscribers) {
            WebhookEvent evt = new WebhookEvent(sub, eventType, payload);
            evt = eventRepository.save(evt);
            deliverEvent(evt, sub);
        }
    }

    /**
     * Send test event to a subscription.
     */
    public WebhookEvent sendTestEvent(Long subscriptionId) {
        WebhookSubscription sub = getSubscription(subscriptionId);
        Map<String, Object> testData = Map.of("test", true, "message", "اختبار webhook");
        String payload;
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("event", "TEST");
            body.put("timestamp", Instant.now().toString());
            body.put("data", testData);
            payload = objectMapper.writeValueAsString(body);
        } catch (Exception e) {
            throw new BusinessRuleException("فشل في إنشاء حمولة الاختبار");
        }
        WebhookEvent evt = new WebhookEvent(sub, "TEST", payload);
        evt = eventRepository.save(evt);
        deliverEvent(evt, sub);
        return evt;
    }

    /**
     * Retry all failed events that have not exceeded MAX_ATTEMPTS.
     */
    public int retryFailed() {
        List<WebhookEvent> failed = eventRepository.findByStatusAndAttemptsLessThan(
                DeliveryStatus.FAILED, MAX_ATTEMPTS);
        int retried = 0;
        for (WebhookEvent evt : failed) {
            deliverEvent(evt, evt.getSubscription());
            retried++;
        }
        log.info("Retried {} failed webhook events", retried);
        return retried;
    }

    // ── Internal delivery ───────────────────────────────────────

    private void deliverEvent(WebhookEvent evt, WebhookSubscription sub) {
        try {
            String signature = computeHmac(evt.getPayload(), sub.getSecret());

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(sub.getUrl()))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .header("X-Webhook-Signature", signature)
                    .header("X-Webhook-Event", evt.getEventType())
                    .POST(HttpRequest.BodyPublishers.ofString(evt.getPayload()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            evt.setAttempts(evt.getAttempts() + 1);
            evt.setLastAttemptAt(Instant.now());
            evt.setResponseCode(response.statusCode());
            evt.setResponseBody(truncate(response.body(), 1000));

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                evt.setStatus(DeliveryStatus.SENT);
            } else {
                evt.setStatus(DeliveryStatus.FAILED);
                log.warn("Webhook {} returned HTTP {} for event {}", sub.getUrl(), response.statusCode(), evt.getId());
            }
        } catch (Exception e) {
            evt.setAttempts(evt.getAttempts() + 1);
            evt.setLastAttemptAt(Instant.now());
            evt.setStatus(DeliveryStatus.FAILED);
            evt.setResponseBody(truncate(e.getMessage(), 1000));
            log.error("Webhook delivery failed for event {} → {}", evt.getId(), sub.getUrl(), e);
        }
        eventRepository.save(evt);
    }

    // ── Helpers ─────────────────────────────────────────────────

    private String computeHmac(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGO);
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO);
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("HMAC computation failed", e);
        }
    }

    private String generateSecret() {
        byte[] bytes = new byte[32];
        new java.security.SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}
