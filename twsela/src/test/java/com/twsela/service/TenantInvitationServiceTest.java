package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.TenantInvitationRepository;
import com.twsela.repository.TenantRepository;
import com.twsela.repository.TenantUserRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantInvitationServiceTest {

    @Mock private TenantInvitationRepository invitationRepository;
    @Mock private TenantUserRepository tenantUserRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private TenantInvitationService invitationService;

    private Tenant tenant;
    private User user;
    private TenantInvitation invitation;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);
        tenant.setName("شركة التوصيل");
        tenant.setSlug("delivery");

        user = new User();
        user.setId(10L);
        user.setName("محمد علي");
        user.setPhone("01000000001");

        invitation = new TenantInvitation();
        invitation.setId(1L);
        invitation.setTenant(tenant);
        invitation.setPhone("01222222222");
        invitation.setRole(TenantUser.TenantRole.TENANT_USER);
        invitation.setStatus(TenantInvitation.InvitationStatus.PENDING);
        invitation.setExpiresAt(Instant.now().plus(7, ChronoUnit.DAYS));
    }

    @Test
    @DisplayName("إنشاء دعوة بنجاح")
    void createInvitation_success() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(invitationRepository.existsByTenantIdAndPhoneAndStatus(1L, "01222222222",
                TenantInvitation.InvitationStatus.PENDING)).thenReturn(false);
        when(userRepository.findByPhone("01222222222")).thenReturn(Optional.empty());
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(invitationRepository.save(any(TenantInvitation.class))).thenAnswer(inv -> {
            TenantInvitation i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        TenantInvitation result = invitationService.createInvitation(1L, "01222222222",
                TenantUser.TenantRole.TENANT_USER, 10L);

        assertThat(result).isNotNull();
        assertThat(result.getPhone()).isEqualTo("01222222222");
    }

    @Test
    @DisplayName("رفض دعوة مكررة")
    void createInvitation_duplicate_throwsException() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(invitationRepository.existsByTenantIdAndPhoneAndStatus(1L, "01222222222",
                TenantInvitation.InvitationStatus.PENDING)).thenReturn(true);

        assertThatThrownBy(() -> invitationService.createInvitation(1L, "01222222222",
                TenantUser.TenantRole.TENANT_USER, 10L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("قبول دعوة بنجاح")
    void acceptInvitation_success() {
        when(invitationRepository.findByToken("test-token")).thenReturn(Optional.of(invitation));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(tenantUserRepository.existsByUserIdAndTenantId(10L, 1L)).thenReturn(false);
        when(tenantUserRepository.save(any(TenantUser.class))).thenAnswer(inv -> {
            TenantUser tu = inv.getArgument(0);
            tu.setId(1L);
            return tu;
        });
        when(invitationRepository.save(any(TenantInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        TenantUser result = invitationService.acceptInvitation("test-token", 10L);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(TenantUser.TenantRole.TENANT_USER);
        assertThat(invitation.getStatus()).isEqualTo(TenantInvitation.InvitationStatus.ACCEPTED);
    }

    @Test
    @DisplayName("رفض قبول دعوة منتهية")
    void acceptInvitation_expired_throwsException() {
        invitation.setExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS));
        when(invitationRepository.findByToken("expired-token")).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(TenantInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> invitationService.acceptInvitation("expired-token", 10L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("إلغاء دعوة")
    void cancelInvitation_success() {
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(invitation));
        when(invitationRepository.save(any(TenantInvitation.class))).thenAnswer(inv -> inv.getArgument(0));

        invitationService.cancelInvitation(1L);

        assertThat(invitation.getStatus()).isEqualTo(TenantInvitation.InvitationStatus.CANCELLED);
    }
}
