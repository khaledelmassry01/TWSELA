package com.twsela.service;

import com.twsela.domain.SystemAuditLog;
import com.twsela.domain.User;
import com.twsela.repository.SystemAuditLogRepository;
import com.twsela.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final SystemAuditLogRepository systemAuditLogRepository;
    private final UserRepository userRepository;

    public AuditService(SystemAuditLogRepository systemAuditLogRepository,
                        UserRepository userRepository) {
        this.systemAuditLogRepository = systemAuditLogRepository;
        this.userRepository = userRepository;
    }

    /**
     * Log a successful operation
     */
    public void logSuccess(String action, String entityType, Long entityId, String description, 
                          Authentication authentication, HttpServletRequest request) {
        try {
            SystemAuditLog auditLog = createAuditLog(action, entityType, entityId, description, authentication, request);
            systemAuditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Don't let audit logging break the main operation
            log.error("Failed to log audit: {}", e.getMessage(), e);
        }
    }

    /**
     * Log a failed operation
     */
    public void logFailure(String action, String entityType, Long entityId, String description, 
                          String errorMessage, Authentication authentication, HttpServletRequest request) {
        try {
            SystemAuditLog auditLog = createAuditLog(action, entityType, entityId, description, authentication, request);
            auditLog.setOldValues(errorMessage);
            systemAuditLogRepository.save(auditLog);
        } catch (Exception e) {
            // Don't let audit logging break the main operation
            log.error("Failed to log audit: {}", e.getMessage(), e);
        }
    }

    /**
     * Log authentication events
     */
    public void logAuthentication(String action, String phone, String ipAddress, String userAgent, boolean success, String errorMessage) {
        try {
            SystemAuditLog auditLog = new SystemAuditLog();
            auditLog.setActionType(action);
            auditLog.setEntityType("AUTHENTICATION");
            auditLog.setIpAddress(ipAddress);
            auditLog.setUserAgent(userAgent);
            auditLog.setCreatedAt(Instant.now());
            
            if (success) {
                auditLog.setNewValues("Authentication successful for: " + phone);
            } else {
                auditLog.setOldValues("Authentication failed for: " + phone + " - " + errorMessage);
            }
            
            systemAuditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to log authentication audit: {}", e.getMessage(), e);
        }
    }

    /**
     * Get audit logs for a specific user
     */
    @Transactional(readOnly = true)
    public List<SystemAuditLog> getUserAuditLogs(Long userId) {
        return systemAuditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get audit logs for a specific entity
     */
    @Transactional(readOnly = true)
    public List<SystemAuditLog> getEntityAuditLogs(String entityType, Long entityId) {
        return systemAuditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    /**
     * Get audit logs within a date range
     */
    @Transactional(readOnly = true)
    public List<SystemAuditLog> getAuditLogsByDateRange(Instant startDate, Instant endDate) {
        return systemAuditLogRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * Create audit log object
     */
    private SystemAuditLog createAuditLog(String action, String entityType, Long entityId, String description, 
                                   Authentication authentication, HttpServletRequest request) {
        SystemAuditLog auditLog = new SystemAuditLog();
        auditLog.setActionType(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setNewValues(description);
        auditLog.setCreatedAt(Instant.now());

        if (authentication != null && authentication.isAuthenticated()) {
            try {
                String phone = authentication.getName();
                User user = userRepository.findByPhone(phone).orElse(null);
                if (user != null) {
                    auditLog.setUserId(user.getId());
                }
            } catch (Exception e) {
                // User not found, continue without user info
            }
        }

        if (request != null) {
            auditLog.setIpAddress(getClientIpAddress(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
        }

        return auditLog;
    }

    /**
     * Get client IP address from request
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
