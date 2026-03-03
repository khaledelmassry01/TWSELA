package com.twsela.service;

import com.twsela.domain.IpBlacklist;
import com.twsela.domain.SecurityEvent;
import com.twsela.domain.User;
import com.twsela.repository.IpBlacklistRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpBlockingServiceTest {

    @Mock
    private IpBlacklistRepository ipBlacklistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityEventService securityEventService;

    @InjectMocks
    private IpBlockingService ipBlockingService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setName("مدير");
        adminUser.setPhone("01099999999");
    }

    @Test
    @DisplayName("حظر عنوان IP يدوياً")
    void blockIp_success() {
        when(ipBlacklistRepository.findByIpAddress("10.0.0.99")).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(ipBlacklistRepository.save(any(IpBlacklist.class))).thenAnswer(inv -> {
            IpBlacklist entry = inv.getArgument(0);
            entry.setId(1L);
            return entry;
        });
        when(securityEventService.recordEvent(any(), eq(SecurityEvent.EventType.IP_BLOCKED),
                anyString(), any(), anyString(), eq(SecurityEvent.Severity.HIGH)))
                .thenReturn(new SecurityEvent());

        IpBlacklist result = ipBlockingService.blockIp("10.0.0.99", "سبام", 1L, true);

        assertNotNull(result);
        assertEquals("10.0.0.99", result.getIpAddress());
        assertTrue(result.isPermanent());
        verify(securityEventService).recordEvent(any(), eq(SecurityEvent.EventType.IP_BLOCKED),
                eq("10.0.0.99"), any(), anyString(), eq(SecurityEvent.Severity.HIGH));
    }

    @Test
    @DisplayName("حظر عنوان IP موجود بالفعل — خطأ")
    void blockIp_duplicate_throwsException() {
        IpBlacklist existing = new IpBlacklist();
        existing.setIpAddress("10.0.0.99");
        when(ipBlacklistRepository.findByIpAddress("10.0.0.99")).thenReturn(Optional.of(existing));

        assertThrows(BusinessRuleException.class,
                () -> ipBlockingService.blockIp("10.0.0.99", "duplicate", 1L, false));
    }

    @Test
    @DisplayName("حظر تلقائي للـ brute force")
    void autoBlockBruteForce_newBlock() {
        when(ipBlacklistRepository.findByIpAddress("192.168.1.100")).thenReturn(Optional.empty());
        when(ipBlacklistRepository.save(any(IpBlacklist.class))).thenAnswer(inv -> {
            IpBlacklist entry = inv.getArgument(0);
            entry.setId(2L);
            return entry;
        });
        when(securityEventService.recordEvent(any(), eq(SecurityEvent.EventType.BRUTE_FORCE_DETECTED),
                anyString(), any(), anyString(), eq(SecurityEvent.Severity.CRITICAL)))
                .thenReturn(new SecurityEvent());

        IpBlacklist result = ipBlockingService.autoBlockBruteForce("192.168.1.100");

        assertNotNull(result);
        assertFalse(result.isPermanent());
        assertNotNull(result.getExpiresAt());
    }

    @Test
    @DisplayName("فحص IP محظور دائماً")
    void isBlocked_permanent_returnsTrue() {
        IpBlacklist entry = new IpBlacklist();
        entry.setIpAddress("10.0.0.1");
        entry.setPermanent(true);
        when(ipBlacklistRepository.findByIpAddress("10.0.0.1")).thenReturn(Optional.of(entry));

        assertTrue(ipBlockingService.isBlocked("10.0.0.1"));
    }

    @Test
    @DisplayName("فحص IP منتهي الصلاحية — غير محظور")
    void isBlocked_expired_returnsFalse() {
        IpBlacklist entry = new IpBlacklist();
        entry.setIpAddress("10.0.0.2");
        entry.setPermanent(false);
        entry.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
        when(ipBlacklistRepository.findByIpAddress("10.0.0.2")).thenReturn(Optional.of(entry));

        assertFalse(ipBlockingService.isBlocked("10.0.0.2"));
    }

    @Test
    @DisplayName("رفع حظر عنوان IP")
    void unblockIp_success() {
        IpBlacklist entry = new IpBlacklist();
        entry.setId(1L);
        entry.setIpAddress("10.0.0.1");
        when(ipBlacklistRepository.findById(1L)).thenReturn(Optional.of(entry));

        ipBlockingService.unblockIp(1L);

        verify(ipBlacklistRepository).delete(entry);
    }

    @Test
    @DisplayName("القائمة السوداء النشطة")
    void getActiveBlacklist_returnsList() {
        when(ipBlacklistRepository.findNonExpired(any(Instant.class)))
                .thenReturn(List.of(new IpBlacklist()));

        List<IpBlacklist> result = ipBlockingService.getActiveBlacklist();
        assertEquals(1, result.size());
    }
}
