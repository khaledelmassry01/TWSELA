package com.twsela.repository;

import com.twsela.domain.TenantInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantInvitationRepository extends JpaRepository<TenantInvitation, Long> {

    Optional<TenantInvitation> findByToken(String token);

    List<TenantInvitation> findByTenantIdAndStatus(Long tenantId, TenantInvitation.InvitationStatus status);

    @Query("SELECT i FROM TenantInvitation i WHERE i.status = 'PENDING' AND i.expiresAt < CURRENT_TIMESTAMP")
    List<TenantInvitation> findExpired();

    boolean existsByTenantIdAndPhoneAndStatus(Long tenantId, String phone,
                                               TenantInvitation.InvitationStatus status);
}
