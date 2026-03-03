package com.twsela.service;

import com.twsela.domain.TenantAuditLog;
import com.twsela.domain.TenantUser;
import com.twsela.repository.TenantAuditLogRepository;
import com.twsela.repository.TenantUserRepository;
import com.twsela.web.exception.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * خدمة عزل البيانات بين المستأجرين.
 * تعتمد على tenant_id column للعزل (discriminator-based isolation).
 */
@Service
@Transactional
public class TenantIsolationService {

    private static final Logger log = LoggerFactory.getLogger(TenantIsolationService.class);

    private final TenantContextService tenantContextService;
    private final TenantUserRepository tenantUserRepository;
    private final TenantAuditLogRepository auditLogRepository;

    public TenantIsolationService(TenantContextService tenantContextService,
                                   TenantUserRepository tenantUserRepository,
                                   TenantAuditLogRepository auditLogRepository) {
        this.tenantContextService = tenantContextService;
        this.tenantUserRepository = tenantUserRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * التحقق من أن المستخدم ينتمي للمستأجر الحالي.
     */
    @Transactional(readOnly = true)
    public boolean isUserInCurrentTenant(Long userId) {
        Long tenantId = tenantContextService.getCurrentTenantId();
        if (tenantId == null) {
            return true; // No tenant context = no isolation
        }
        return tenantUserRepository.existsByUserIdAndTenantId(userId, tenantId);
    }

    /**
     * فرض عزل المستأجر - رفض الوصول إذا لم ينتمي المستخدم للمستأجر.
     */
    @Transactional(readOnly = true)
    public void enforceIsolation(Long userId) {
        if (!isUserInCurrentTenant(userId)) {
            throw new BusinessRuleException("ليس لديك صلاحية الوصول لهذا المستأجر");
        }
    }

    /**
     * التحقق من أن كيان ينتمي للمستأجر الحالي.
     */
    @Transactional(readOnly = true)
    public boolean belongsToCurrentTenant(Long entityTenantId) {
        Long currentTenantId = tenantContextService.getCurrentTenantId();
        if (currentTenantId == null) {
            return true; // No tenant context = no isolation
        }
        if (entityTenantId == null) {
            return true; // Entity has no tenant = shared resource
        }
        return currentTenantId.equals(entityTenantId);
    }

    /**
     * فرض عزل الكيانات.
     */
    public void enforceEntityIsolation(Long entityTenantId) {
        if (!belongsToCurrentTenant(entityTenantId)) {
            throw new BusinessRuleException("لا يمكن الوصول لبيانات مستأجر آخر");
        }
    }

    /**
     * جلب مستخدمي المستأجر.
     */
    @Transactional(readOnly = true)
    public List<TenantUser> getTenantUsers(Long tenantId) {
        return tenantUserRepository.findByTenantId(tenantId);
    }

    /**
     * تغيير دور مستخدم في المستأجر.
     */
    public TenantUser changeUserRole(Long tenantId, Long userId, TenantUser.TenantRole newRole) {
        TenantUser tenantUser = tenantUserRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new BusinessRuleException("المستخدم ليس عضواً في هذا المستأجر"));

        TenantUser.TenantRole oldRole = tenantUser.getRole();
        tenantUser.setRole(newRole);

        log.info("User {} role changed in tenant {} from {} to {}", userId, tenantId, oldRole, newRole);
        return tenantUserRepository.save(tenantUser);
    }

    /**
     * إزالة مستخدم من المستأجر.
     */
    public void removeUserFromTenant(Long tenantId, Long userId) {
        TenantUser tenantUser = tenantUserRepository.findByUserIdAndTenantId(userId, tenantId)
                .orElseThrow(() -> new BusinessRuleException("المستخدم ليس عضواً في هذا المستأجر"));

        if (tenantUser.getRole() == TenantUser.TenantRole.TENANT_OWNER) {
            throw new BusinessRuleException("لا يمكن إزالة مالك المستأجر");
        }

        tenantUser.setActive(false);
        tenantUserRepository.save(tenantUser);
        log.info("User {} removed from tenant {}", userId, tenantId);
    }

    /**
     * تسجيل عملية في سجل التدقيق.
     */
    public void logAuditEvent(Long tenantId, Long userId, String action,
                               String entityType, Long entityId,
                               String oldValues, String newValues, String ipAddress) {
        TenantAuditLog auditLog = new TenantAuditLog();
        auditLog.setAction(action);
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setIpAddress(ipAddress);

        auditLogRepository.save(auditLog);
        log.debug("Audit log: tenant={}, user={}, action={}, entity={}#{}", tenantId, userId, action, entityType, entityId);
    }

    /**
     * جلب سجل التدقيق للمستأجر.
     */
    @Transactional(readOnly = true)
    public List<TenantAuditLog> getAuditLogs(Long tenantId) {
        return auditLogRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }
}
