package com.twsela.service;

import com.twsela.domain.AccountLockout;
import com.twsela.domain.User;
import com.twsela.repository.AccountLockoutRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * خدمة قفل وفتح الحسابات.
 */
@Service
@Transactional
public class AccountLockoutService {

    private static final Logger log = LoggerFactory.getLogger(AccountLockoutService.class);
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;

    private final AccountLockoutRepository accountLockoutRepository;
    private final UserRepository userRepository;

    public AccountLockoutService(AccountLockoutRepository accountLockoutRepository,
                                  UserRepository userRepository) {
        this.accountLockoutRepository = accountLockoutRepository;
        this.userRepository = userRepository;
    }

    /**
     * تسجيل محاولة فاشلة — يقفل الحساب بعد 5 محاولات.
     */
    public AccountLockout recordFailedAttempt(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<AccountLockout> existingOpt = accountLockoutRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        AccountLockout lockout;

        if (existingOpt.isPresent() && existingOpt.get().getUnlockedAt() == null
                && existingOpt.get().getFailedAttempts() < MAX_FAILED_ATTEMPTS) {
            lockout = existingOpt.get();
        } else {
            lockout = new AccountLockout();
            lockout.setUser(user);
        }

        lockout.setFailedAttempts(lockout.getFailedAttempts() + 1);
        lockout.setUpdatedAt(Instant.now());

        if (lockout.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
            Instant now = Instant.now();
            lockout.setLockoutStart(now);
            lockout.setLockoutEnd(now.plus(LOCKOUT_DURATION_MINUTES, ChronoUnit.MINUTES));
            lockout.setAutoUnlockAt(now.plus(LOCKOUT_DURATION_MINUTES, ChronoUnit.MINUTES));
            lockout.setLockoutReason("تجاوز الحد الأقصى للمحاولات الفاشلة: " + MAX_FAILED_ATTEMPTS);
            log.warn("Account locked for user {} — {} failed attempts", userId, lockout.getFailedAttempts());
        }

        return accountLockoutRepository.save(lockout);
    }

    /**
     * فحص هل الحساب مقفل حالياً.
     */
    @Transactional(readOnly = true)
    public boolean isLocked(Long userId) {
        Optional<AccountLockout> lockout = accountLockoutRepository.findActiveByUserId(userId, Instant.now());
        return lockout.isPresent();
    }

    /**
     * فتح حساب يدوياً (من الإدارة).
     */
    public AccountLockout manualUnlock(Long userId, Long unlockedByUserId) {
        AccountLockout lockout = accountLockoutRepository.findActiveByUserId(userId, Instant.now())
                .orElseThrow(() -> new BusinessRuleException("الحساب غير مقفل حالياً"));

        User unlocker = userRepository.findById(unlockedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", unlockedByUserId));

        lockout.setUnlockedBy(unlocker);
        lockout.setUnlockedAt(Instant.now());
        lockout.setUpdatedAt(Instant.now());

        log.info("Account {} manually unlocked by user {}", userId, unlockedByUserId);
        return accountLockoutRepository.save(lockout);
    }

    /**
     * إعادة تعيين عداد المحاولات الفاشلة.
     */
    public void resetFailedAttempts(Long userId) {
        Optional<AccountLockout> lockoutOpt = accountLockoutRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        lockoutOpt.ifPresent(lockout -> {
            lockout.setFailedAttempts(0);
            lockout.setUpdatedAt(Instant.now());
            accountLockoutRepository.save(lockout);
        });
    }

    /**
     * الحسابات المقفلة حالياً.
     */
    @Transactional(readOnly = true)
    public List<AccountLockout> getActiveLockouts() {
        return accountLockoutRepository.findActiveLockouts(Instant.now());
    }
}
