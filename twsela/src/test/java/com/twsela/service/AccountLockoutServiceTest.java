package com.twsela.service;

import com.twsela.domain.AccountLockout;
import com.twsela.domain.User;
import com.twsela.repository.AccountLockoutRepository;
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
class AccountLockoutServiceTest {

    @Mock
    private AccountLockoutRepository accountLockoutRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountLockoutService accountLockoutService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("كريم");
        testUser.setPhone("01011111111");

        adminUser = new User();
        adminUser.setId(99L);
        adminUser.setName("مدير النظام");
        adminUser.setPhone("01099999999");
    }

    @Test
    @DisplayName("تسجيل محاولة فاشلة — إنشاء سجل جديد")
    void recordFailedAttempt_newLockout() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountLockoutRepository.findTopByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.empty());
        when(accountLockoutRepository.save(any(AccountLockout.class))).thenAnswer(inv -> {
            AccountLockout l = inv.getArgument(0);
            l.setId(1L);
            return l;
        });

        AccountLockout result = accountLockoutService.recordFailedAttempt(1L);

        assertNotNull(result);
        assertEquals(1, result.getFailedAttempts());
        assertNull(result.getLockoutStart()); // not locked yet (< 5)
        verify(accountLockoutRepository).save(any(AccountLockout.class));
    }

    @Test
    @DisplayName("قفل الحساب بعد 5 محاولات فاشلة")
    void recordFailedAttempt_locksAfterFiveAttempts() {
        AccountLockout existing = new AccountLockout();
        existing.setId(1L);
        existing.setUser(testUser);
        existing.setFailedAttempts(4); // will become 5

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountLockoutRepository.findTopByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(existing));
        when(accountLockoutRepository.save(any(AccountLockout.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountLockout result = accountLockoutService.recordFailedAttempt(1L);

        assertEquals(5, result.getFailedAttempts());
        assertNotNull(result.getLockoutStart());
        assertNotNull(result.getLockoutEnd());
        assertNotNull(result.getAutoUnlockAt());
    }

    @Test
    @DisplayName("فحص حساب مقفل")
    void isLocked_activelyLocked_returnsTrue() {
        AccountLockout lockout = new AccountLockout();
        lockout.setId(1L);
        when(accountLockoutRepository.findActiveByUserId(eq(1L), any(Instant.class)))
                .thenReturn(Optional.of(lockout));

        assertTrue(accountLockoutService.isLocked(1L));
    }

    @Test
    @DisplayName("فحص حساب غير مقفل")
    void isLocked_notLocked_returnsFalse() {
        when(accountLockoutRepository.findActiveByUserId(eq(1L), any(Instant.class)))
                .thenReturn(Optional.empty());

        assertFalse(accountLockoutService.isLocked(1L));
    }

    @Test
    @DisplayName("فتح حساب يدوياً")
    void manualUnlock_success() {
        AccountLockout lockout = new AccountLockout();
        lockout.setId(1L);
        lockout.setUser(testUser);
        lockout.setLockoutStart(Instant.now().minus(10, ChronoUnit.MINUTES));

        when(accountLockoutRepository.findActiveByUserId(eq(1L), any(Instant.class)))
                .thenReturn(Optional.of(lockout));
        when(userRepository.findById(99L)).thenReturn(Optional.of(adminUser));
        when(accountLockoutRepository.save(any(AccountLockout.class))).thenAnswer(inv -> inv.getArgument(0));

        AccountLockout result = accountLockoutService.manualUnlock(1L, 99L);

        assertNotNull(result.getUnlockedAt());
        assertEquals(adminUser, result.getUnlockedBy());
    }

    @Test
    @DisplayName("محاولة فتح حساب غير مقفل — خطأ")
    void manualUnlock_notLocked_throwsException() {
        when(accountLockoutRepository.findActiveByUserId(eq(1L), any(Instant.class)))
                .thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class,
                () -> accountLockoutService.manualUnlock(1L, 99L));
    }

    @Test
    @DisplayName("إعادة تعيين المحاولات الفاشلة")
    void resetFailedAttempts_success() {
        AccountLockout lockout = new AccountLockout();
        lockout.setId(1L);
        lockout.setFailedAttempts(3);
        when(accountLockoutRepository.findTopByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Optional.of(lockout));
        when(accountLockoutRepository.save(any(AccountLockout.class))).thenAnswer(inv -> inv.getArgument(0));

        accountLockoutService.resetFailedAttempts(1L);

        verify(accountLockoutRepository).save(argThat(l -> l.getFailedAttempts() == 0));
    }

    @Test
    @DisplayName("قائمة الحسابات المقفلة")
    void getActiveLockouts_returnsList() {
        when(accountLockoutRepository.findActiveLockouts(any(Instant.class)))
                .thenReturn(List.of(new AccountLockout()));

        List<AccountLockout> result = accountLockoutService.getActiveLockouts();
        assertEquals(1, result.size());
    }
}
