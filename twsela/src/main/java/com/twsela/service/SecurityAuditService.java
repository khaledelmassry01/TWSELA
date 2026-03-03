package com.twsela.service;

import com.twsela.domain.ComplianceRule;
import com.twsela.domain.SecurityEvent;
import com.twsela.repository.ComplianceRuleRepository;
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
 * خدمة تدقيق أمني شامل لحالة النظام.
 */
@Service
@Transactional(readOnly = true)
public class SecurityAuditService {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditService.class);

    private final SecurityEventRepository securityEventRepository;
    private final ComplianceRuleRepository complianceRuleRepository;

    public SecurityAuditService(SecurityEventRepository securityEventRepository,
                                 ComplianceRuleRepository complianceRuleRepository) {
        this.securityEventRepository = securityEventRepository;
        this.complianceRuleRepository = complianceRuleRepository;
    }

    /**
     * تقرير أمني شامل.
     */
    public Map<String, Object> generateSecurityAudit() {
        Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);
        Instant last7d = Instant.now().minus(7, ChronoUnit.DAYS);

        Map<String, Object> audit = new LinkedHashMap<>();

        // Login statistics
        Map<String, Object> loginStats = new LinkedHashMap<>();
        loginStats.put("successLast24h", securityEventRepository.countByEventTypeAndCreatedAtBetween(
                SecurityEvent.EventType.LOGIN_SUCCESS, last24h, Instant.now()));
        loginStats.put("failureLast24h", securityEventRepository.countByEventTypeAndCreatedAtBetween(
                SecurityEvent.EventType.LOGIN_FAILURE, last24h, Instant.now()));
        loginStats.put("bruteForceDetected7d", securityEventRepository.countByEventTypeAndCreatedAtBetween(
                SecurityEvent.EventType.BRUTE_FORCE_DETECTED, last7d, Instant.now()));
        audit.put("loginStatistics", loginStats);

        // Compliance status
        List<ComplianceRule> rules = complianceRuleRepository.findByEnabledTrue();
        long passed = rules.stream().filter(r -> r.getLastResult() == ComplianceRule.CheckResult.PASS).count();
        long failed = rules.stream().filter(r -> r.getLastResult() == ComplianceRule.CheckResult.FAIL).count();

        Map<String, Object> compliance = new LinkedHashMap<>();
        compliance.put("totalRules", rules.size());
        compliance.put("passed", passed);
        compliance.put("failed", failed);
        compliance.put("score", rules.isEmpty() ? 100 : (passed * 100 / rules.size()));
        audit.put("complianceStatus", compliance);

        // Threat overview
        List<SecurityEvent> criticalEvents = securityEventRepository.findBySeverity(SecurityEvent.Severity.CRITICAL);
        audit.put("criticalEventsCount", criticalEvents.size());

        Map<String, Object> ipStats = new LinkedHashMap<>();
        ipStats.put("blockedLast24h", securityEventRepository.countByEventTypeAndCreatedAtBetween(
                SecurityEvent.EventType.IP_BLOCKED, last24h, Instant.now()));
        audit.put("ipStatistics", ipStats);

        log.info("Security audit generated — {} compliance rules, {} critical events", rules.size(), criticalEvents.size());
        return audit;
    }
}
