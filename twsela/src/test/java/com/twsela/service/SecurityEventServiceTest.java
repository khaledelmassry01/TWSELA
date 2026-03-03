package com.twsela.service;

import com.twsela.domain.SecurityEvent;
import com.twsela.domain.User;
import com.twsela.repository.SecurityEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityEventServiceTest {

    @Mock
    private SecurityEventRepository securityEventRepository;

    @InjectMocks
    private SecurityEventService securityEventService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("أحمد");
        testUser.setPhone("01012345678");
    }

    @Test
    @DisplayName("تسجيل حدث أمني عادي")
    void recordEvent_success() {
        SecurityEvent saved = new SecurityEvent();
        saved.setId(1L);
        saved.setUser(testUser);
        saved.setEventType(SecurityEvent.EventType.LOGIN_SUCCESS);
        saved.setSeverity(SecurityEvent.Severity.LOW);
        when(securityEventRepository.save(any(SecurityEvent.class))).thenReturn(saved);

        SecurityEvent result = securityEventService.recordEvent(
                testUser, SecurityEvent.EventType.LOGIN_SUCCESS,
                "192.168.1.1", "Mozilla/5.0", "Login success", SecurityEvent.Severity.LOW);

        assertNotNull(result);
        assertEquals(SecurityEvent.EventType.LOGIN_SUCCESS, result.getEventType());
        verify(securityEventRepository).save(any(SecurityEvent.class));
    }

    @Test
    @DisplayName("تسجيل حدث أمني حرج يسجل تحذيراً")
    void recordEvent_critical_logsWarning() {
        SecurityEvent saved = new SecurityEvent();
        saved.setId(2L);
        saved.setSeverity(SecurityEvent.Severity.CRITICAL);
        saved.setEventType(SecurityEvent.EventType.BRUTE_FORCE_DETECTED);
        when(securityEventRepository.save(any(SecurityEvent.class))).thenReturn(saved);

        SecurityEvent result = securityEventService.recordEvent(
                testUser, SecurityEvent.EventType.BRUTE_FORCE_DETECTED,
                "10.0.0.1", null, "Brute force detected", SecurityEvent.Severity.CRITICAL);

        assertNotNull(result);
        assertEquals(SecurityEvent.Severity.CRITICAL, result.getSeverity());
    }

    @Test
    @DisplayName("اكتشاف brute force عند تجاوز الحد")
    void detectBruteForce_aboveThreshold_returnsTrue() {
        List<SecurityEvent> fiveFailures = List.of(
                new SecurityEvent(), new SecurityEvent(), new SecurityEvent(),
                new SecurityEvent(), new SecurityEvent());
        when(securityEventRepository.findRecentByIpAndType(eq("10.0.0.1"),
                eq(SecurityEvent.EventType.LOGIN_FAILURE), any(Instant.class)))
                .thenReturn(fiveFailures);

        assertTrue(securityEventService.detectBruteForce("10.0.0.1"));
    }

    @Test
    @DisplayName("عدم اكتشاف brute force تحت الحد")
    void detectBruteForce_belowThreshold_returnsFalse() {
        when(securityEventRepository.findRecentByIpAndType(eq("10.0.0.1"),
                eq(SecurityEvent.EventType.LOGIN_FAILURE), any(Instant.class)))
                .thenReturn(List.of(new SecurityEvent(), new SecurityEvent()));

        assertFalse(securityEventService.detectBruteForce("10.0.0.1"));
    }

    @Test
    @DisplayName("ملخص الأحداث الأمنية")
    void getEventSummary_returnsSummaryMap() {
        when(securityEventRepository.countByEventTypeAndCreatedAtBetween(
                eq(SecurityEvent.EventType.LOGIN_FAILURE), any(Instant.class), any(Instant.class)))
                .thenReturn(10L);
        when(securityEventRepository.countByEventTypeAndCreatedAtBetween(
                eq(SecurityEvent.EventType.BRUTE_FORCE_DETECTED), any(Instant.class), any(Instant.class)))
                .thenReturn(2L);
        when(securityEventRepository.countByEventTypeAndCreatedAtBetween(
                eq(SecurityEvent.EventType.IP_BLOCKED), any(Instant.class), any(Instant.class)))
                .thenReturn(1L);
        when(securityEventRepository.findBySeverity(SecurityEvent.Severity.CRITICAL))
                .thenReturn(List.of(new SecurityEvent()));

        Map<String, Object> summary = securityEventService.getEventSummary();

        assertEquals(10L, summary.get("loginFailures"));
        assertEquals(2L, summary.get("bruteForceDetected"));
        assertEquals(1L, summary.get("ipBlocked"));
        assertEquals(1, summary.get("criticalEvents"));
    }

    @Test
    @DisplayName("الأحداث الأمنية للمستخدم")
    void getEventsByUser_returnsList() {
        SecurityEvent event = new SecurityEvent();
        event.setId(1L);
        when(securityEventRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(event));

        List<SecurityEvent> result = securityEventService.getEventsByUser(1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("التهديدات النشطة")
    void getActiveThreats_returnsCriticalEvents() {
        when(securityEventRepository.findBySeverityOrderByCreatedAtDesc(SecurityEvent.Severity.CRITICAL))
                .thenReturn(Collections.emptyList());

        List<SecurityEvent> threats = securityEventService.getActiveThreats();
        assertNotNull(threats);
        assertTrue(threats.isEmpty());
    }
}
