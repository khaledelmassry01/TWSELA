package com.twsela.service;

import com.twsela.domain.SecurityEvent;
import com.twsela.domain.User;
import com.twsela.repository.SecurityEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * خدمة تسجيل وتحليل الأحداث الأمنية.
 */
@Service
@Transactional
public class SecurityEventService {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventService.class);
    private static final int BRUTE_FORCE_THRESHOLD = 5;
    private static final int BRUTE_FORCE_WINDOW_MINUTES = 15;

    private final SecurityEventRepository securityEventRepository;

    public SecurityEventService(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    /**
     * تسجيل حدث أمني.
     */
    public SecurityEvent recordEvent(User user, SecurityEvent.EventType eventType,
                                      String ipAddress, String userAgent,
                                      String details, SecurityEvent.Severity severity) {
        SecurityEvent event = new SecurityEvent();
        event.setUser(user);
        event.setEventType(eventType);
        event.setIpAddress(ipAddress);
        event.setUserAgent(userAgent);
        event.setDetails(details);
        event.setSeverity(severity);

        SecurityEvent saved = securityEventRepository.save(event);

        if (severity == SecurityEvent.Severity.CRITICAL) {
            log.warn("CRITICAL security event: type={}, ip={}, user={}",
                    eventType, ipAddress, user != null ? user.getId() : "anonymous");
        }

        log.info("Security event recorded: type={}, severity={}, ip={}", eventType, severity, ipAddress);
        return saved;
    }

    /**
     * تحليل أنماط brute force لعنوان IP.
     */
    @Transactional(readOnly = true)
    public boolean detectBruteForce(String ipAddress) {
        Instant windowStart = Instant.now().minus(BRUTE_FORCE_WINDOW_MINUTES, ChronoUnit.MINUTES);
        List<SecurityEvent> recentFailures = securityEventRepository.findRecentByIpAndType(
                ipAddress, SecurityEvent.EventType.LOGIN_FAILURE, windowStart);
        return recentFailures.size() >= BRUTE_FORCE_THRESHOLD;
    }

    /**
     * ملخص الأحداث الأمنية.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEventSummary() {
        Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("loginFailures", securityEventRepository.countByEventTypeAndCreatedAtBetween(
                SecurityEvent.EventType.LOGIN_FAILURE, last24h, Instant.now()));
        summary.put("bruteForceDetected", securityEventRepository.countByEventTypeAndCreatedAtBetween(
                SecurityEvent.EventType.BRUTE_FORCE_DETECTED, last24h, Instant.now()));
        summary.put("ipBlocked", securityEventRepository.countByEventTypeAndCreatedAtBetween(
                SecurityEvent.EventType.IP_BLOCKED, last24h, Instant.now()));
        summary.put("criticalEvents", securityEventRepository.findBySeverity(SecurityEvent.Severity.CRITICAL).size());
        return summary;
    }

    /**
     * الأحداث الأمنية لمستخدم.
     */
    @Transactional(readOnly = true)
    public List<SecurityEvent> getEventsByUser(Long userId) {
        return securityEventRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * التهديدات النشطة (أحداث حرجة وعالية).
     */
    @Transactional(readOnly = true)
    public List<SecurityEvent> getActiveThreats() {
        return securityEventRepository.findBySeverityOrderByCreatedAtDesc(SecurityEvent.Severity.CRITICAL);
    }
}
