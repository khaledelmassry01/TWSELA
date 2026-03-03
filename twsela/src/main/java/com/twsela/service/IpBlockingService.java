package com.twsela.service;

import com.twsela.domain.IpBlacklist;
import com.twsela.domain.SecurityEvent;
import com.twsela.domain.User;
import com.twsela.repository.IpBlacklistRepository;
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
 * خدمة حظر وإدارة عناوين IP.
 */
@Service
@Transactional
public class IpBlockingService {

    private static final Logger log = LoggerFactory.getLogger(IpBlockingService.class);
    private static final int AUTO_BLOCK_DURATION_HOURS = 24;

    private final IpBlacklistRepository ipBlacklistRepository;
    private final UserRepository userRepository;
    private final SecurityEventService securityEventService;

    public IpBlockingService(IpBlacklistRepository ipBlacklistRepository,
                              UserRepository userRepository,
                              SecurityEventService securityEventService) {
        this.ipBlacklistRepository = ipBlacklistRepository;
        this.userRepository = userRepository;
        this.securityEventService = securityEventService;
    }

    /**
     * حظر عنوان IP يدوياً.
     */
    public IpBlacklist blockIp(String ipAddress, String reason, Long blockedByUserId, boolean permanent) {
        Optional<IpBlacklist> existing = ipBlacklistRepository.findByIpAddress(ipAddress);
        if (existing.isPresent()) {
            throw new BusinessRuleException("عنوان IP " + ipAddress + " محظور بالفعل");
        }

        User blocker = userRepository.findById(blockedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", blockedByUserId));

        IpBlacklist entry = new IpBlacklist();
        entry.setIpAddress(ipAddress);
        entry.setReason(reason);
        entry.setBlockedBy(blocker);
        entry.setPermanent(permanent);
        if (!permanent) {
            entry.setExpiresAt(Instant.now().plus(AUTO_BLOCK_DURATION_HOURS, ChronoUnit.HOURS));
        }

        IpBlacklist saved = ipBlacklistRepository.save(entry);

        securityEventService.recordEvent(null, SecurityEvent.EventType.IP_BLOCKED,
                ipAddress, null, "IP blocked: " + reason, SecurityEvent.Severity.HIGH);

        log.info("IP {} blocked by user {} — permanent={}, reason={}", ipAddress, blockedByUserId, permanent, reason);
        return saved;
    }

    /**
     * حظر تلقائي عند اكتشاف brute force.
     */
    public IpBlacklist autoBlockBruteForce(String ipAddress) {
        Optional<IpBlacklist> existing = ipBlacklistRepository.findByIpAddress(ipAddress);
        if (existing.isPresent()) {
            return existing.get();
        }

        IpBlacklist entry = new IpBlacklist();
        entry.setIpAddress(ipAddress);
        entry.setReason("حظر تلقائي — محاولات brute force");
        entry.setPermanent(false);
        entry.setExpiresAt(Instant.now().plus(AUTO_BLOCK_DURATION_HOURS, ChronoUnit.HOURS));

        IpBlacklist saved = ipBlacklistRepository.save(entry);

        securityEventService.recordEvent(null, SecurityEvent.EventType.BRUTE_FORCE_DETECTED,
                ipAddress, null, "Auto-blocked for brute force", SecurityEvent.Severity.CRITICAL);

        log.warn("IP {} auto-blocked for brute force", ipAddress);
        return saved;
    }

    /**
     * فحص هل عنوان IP محظور.
     */
    @Transactional(readOnly = true)
    public boolean isBlocked(String ipAddress) {
        Optional<IpBlacklist> entry = ipBlacklistRepository.findByIpAddress(ipAddress);
        if (entry.isEmpty()) return false;
        IpBlacklist blacklist = entry.get();
        if (blacklist.isPermanent()) return true;
        return !blacklist.isExpired();
    }

    /**
     * رفع حظر عنوان IP.
     */
    public void unblockIp(Long id) {
        IpBlacklist entry = ipBlacklistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IpBlacklist", "id", id));
        ipBlacklistRepository.delete(entry);
        log.info("IP {} unblocked", entry.getIpAddress());
    }

    /**
     * القائمة السوداء الحالية (غير المنتهية).
     */
    @Transactional(readOnly = true)
    public List<IpBlacklist> getActiveBlacklist() {
        return ipBlacklistRepository.findNonExpired(Instant.now());
    }
}
