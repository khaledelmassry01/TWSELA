package com.twsela.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Rate limiting filter for sensitive endpoints.
 * Uses a sliding-window counter per client IP.
 * <p>
 * Limits:
 * <ul>
 *   <li>/api/auth/login — 5 requests per minute</li>
 *   <li>/api/public/send-otp — 3 requests per minute</li>
 *   <li>/api/public/forgot-password — 3 requests per minute</li>
 *   <li>/api/public/reset-password — 3 requests per minute</li>
 *   <li>/api/public/track/* — 10 requests per minute</li>
 *   <li>/api/public/feedback — 5 requests per minute</li>
 *   <li>/api/public/contact — 5 requests per minute</li>
 * </ul>
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    /** Map of "IP:endpoint" → bucket */
    private final ConcurrentHashMap<String, RateBucket> buckets = new ConcurrentHashMap<>();

    /** Rate limit rules: URI prefix → max requests per window */
    private static final Map<String, Integer> RATE_LIMITS = Map.of(
            "/api/auth/login", 5,
            "/api/public/send-otp", 3,
            "/api/public/forgot-password", 3,
            "/api/public/reset-password", 3,
            "/api/public/track", 10,
            "/api/public/feedback", 5,
            "/api/public/contact", 5
    );

    /** Window size in seconds */
    private static final long WINDOW_SECONDS = 60;

    /** Eviction interval — clean stale buckets every 5 minutes */
    private static final long EVICTION_INTERVAL_MS = 5 * 60 * 1000L;
    private volatile long lastEviction = System.currentTimeMillis();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        Integer maxRequests = resolveLimit(uri);

        if (maxRequests == null) {
            // No rate-limit rule for this URI
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        String bucketKey = clientIp + ":" + normalizeUri(uri);

        evictStaleEntries();

        RateBucket bucket = buckets.computeIfAbsent(bucketKey, k -> new RateBucket());

        if (!bucket.tryConsume(maxRequests, WINDOW_SECONDS)) {
            log.warn("Rate limit exceeded for IP={} on endpoint={}", clientIp, uri);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"تم تجاوز الحد المسموح من الطلبات. حاول مرة أخرى بعد دقيقة.\",\"errors\":[\"RATE_LIMIT_EXCEEDED\"]}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Match the request URI against the rate-limit rules.
     * Uses startsWith so /api/public/track/ABC123 matches /api/public/track.
     */
    private Integer resolveLimit(String uri) {
        for (Map.Entry<String, Integer> entry : RATE_LIMITS.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /** Normalize URI for bucket key (strip path params after the matched prefix) */
    private String normalizeUri(String uri) {
        for (String prefix : RATE_LIMITS.keySet()) {
            if (uri.startsWith(prefix)) {
                return prefix;
            }
        }
        return uri;
    }

    /** Extract the real client IP, respecting X-Forwarded-For from reverse proxy */
    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }

    /** Periodically remove stale buckets to prevent memory leaks */
    private void evictStaleEntries() {
        long now = System.currentTimeMillis();
        if (now - lastEviction < EVICTION_INTERVAL_MS) {
            return;
        }
        lastEviction = now;
        long cutoff = Instant.now().getEpochSecond() - WINDOW_SECONDS * 2;
        buckets.entrySet().removeIf(e -> e.getValue().windowStart < cutoff);
    }

    /**
     * Simple sliding-window rate bucket.
     * Thread-safe via AtomicInteger + volatile.
     */
    static class RateBucket {
        volatile long windowStart;
        final AtomicInteger count;

        RateBucket() {
            this.windowStart = Instant.now().getEpochSecond();
            this.count = new AtomicInteger(0);
        }

        /**
         * Try to consume one request token.
         * @return true if allowed, false if rate limit exceeded
         */
        boolean tryConsume(int maxRequests, long windowSeconds) {
            long now = Instant.now().getEpochSecond();
            if (now - windowStart >= windowSeconds) {
                // Reset the window
                synchronized (this) {
                    if (now - windowStart >= windowSeconds) {
                        windowStart = now;
                        count.set(0);
                    }
                }
            }
            return count.incrementAndGet() <= maxRequests;
        }
    }
}
