package com.twsela.service;

import com.twsela.domain.Tenant;
import com.twsela.domain.TenantInvitation;
import com.twsela.domain.TenantUser;
import com.twsela.domain.User;
import com.twsela.repository.TenantInvitationRepository;
import com.twsela.repository.TenantRepository;
import com.twsela.repository.TenantUserRepository;
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

/**
 * خدمة إدارة دعوات المستأجر.
 */
@Service
@Transactional
public class TenantInvitationService {

    private static final Logger log = LoggerFactory.getLogger(TenantInvitationService.class);
    private static final int INVITATION_EXPIRY_DAYS = 7;

    private final TenantInvitationRepository invitationRepository;
    private final TenantUserRepository tenantUserRepository;
    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;

    public TenantInvitationService(TenantInvitationRepository invitationRepository,
                                    TenantUserRepository tenantUserRepository,
                                    TenantRepository tenantRepository,
                                    UserRepository userRepository) {
        this.invitationRepository = invitationRepository;
        this.tenantUserRepository = tenantUserRepository;
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
    }

    /**
     * إنشاء دعوة جديدة.
     */
    public TenantInvitation createInvitation(Long tenantId, String phone, TenantUser.TenantRole role, Long invitedByUserId) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant", "id", tenantId));

        // Check if already invited
        if (invitationRepository.existsByTenantIdAndPhoneAndStatus(tenantId, phone, TenantInvitation.InvitationStatus.PENDING)) {
            throw new BusinessRuleException("يوجد دعوة معلقة بالفعل لهذا الرقم");
        }

        // Check if already a member
        User user = userRepository.findByPhone(phone).orElse(null);
        if (user != null && tenantUserRepository.existsByUserIdAndTenantId(user.getId(), tenantId)) {
            throw new BusinessRuleException("المستخدم عضو بالفعل في هذا المستأجر");
        }

        User invitedBy = userRepository.findById(invitedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", invitedByUserId));

        TenantInvitation invitation = new TenantInvitation();
        invitation.setTenant(tenant);
        invitation.setPhone(phone);
        invitation.setRole(role);
        invitation.setInvitedBy(invitedBy);
        invitation.setStatus(TenantInvitation.InvitationStatus.PENDING);
        invitation.setExpiresAt(Instant.now().plus(INVITATION_EXPIRY_DAYS, ChronoUnit.DAYS));

        log.info("Invitation created for phone {} to tenant {} with role {}", phone, tenantId, role);
        return invitationRepository.save(invitation);
    }

    /**
     * قبول دعوة.
     */
    public TenantUser acceptInvitation(String token, Long userId) {
        TenantInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("TenantInvitation", "token", token));

        if (invitation.getStatus() != TenantInvitation.InvitationStatus.PENDING) {
            throw new BusinessRuleException("هذه الدعوة غير صالحة");
        }

        if (invitation.isExpired()) {
            invitation.setStatus(TenantInvitation.InvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
            throw new BusinessRuleException("انتهت صلاحية هذه الدعوة");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check if already a member
        if (tenantUserRepository.existsByUserIdAndTenantId(userId, invitation.getTenant().getId())) {
            throw new BusinessRuleException("المستخدم عضو بالفعل في هذا المستأجر");
        }

        // Create tenant user
        TenantUser tenantUser = new TenantUser();
        tenantUser.setUser(user);
        tenantUser.setTenant(invitation.getTenant());
        tenantUser.setRole(invitation.getRole());
        tenantUser.setActive(true);
        tenantUser.setJoinedAt(Instant.now());

        // Update invitation status
        invitation.setStatus(TenantInvitation.InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);

        log.info("Invitation accepted: user {} joined tenant {} as {}", userId, invitation.getTenant().getId(), invitation.getRole());
        return tenantUserRepository.save(tenantUser);
    }

    /**
     * إلغاء دعوة.
     */
    public void cancelInvitation(Long invitationId) {
        TenantInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResourceNotFoundException("TenantInvitation", "id", invitationId));

        if (invitation.getStatus() != TenantInvitation.InvitationStatus.PENDING) {
            throw new BusinessRuleException("لا يمكن إلغاء دعوة غير معلقة");
        }

        invitation.setStatus(TenantInvitation.InvitationStatus.CANCELLED);
        invitationRepository.save(invitation);
        log.info("Invitation {} cancelled", invitationId);
    }

    /**
     * انتهاء صلاحية الدعوات القديمة.
     */
    public int expireOldInvitations() {
        List<TenantInvitation> expired = invitationRepository.findExpired();
        for (TenantInvitation inv : expired) {
            inv.setStatus(TenantInvitation.InvitationStatus.EXPIRED);
            invitationRepository.save(inv);
        }
        if (!expired.isEmpty()) {
            log.info("Expired {} invitations", expired.size());
        }
        return expired.size();
    }

    /**
     * جلب دعوات المستأجر.
     */
    @Transactional(readOnly = true)
    public List<TenantInvitation> getByTenantAndStatus(Long tenantId, TenantInvitation.InvitationStatus status) {
        return invitationRepository.findByTenantIdAndStatus(tenantId, status);
    }

    /**
     * جلب دعوة بالتوكن.
     */
    @Transactional(readOnly = true)
    public TenantInvitation getByToken(String token) {
        return invitationRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("TenantInvitation", "token", token));
    }
}
