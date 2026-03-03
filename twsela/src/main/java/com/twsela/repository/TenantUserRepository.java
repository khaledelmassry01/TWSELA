package com.twsela.repository;

import com.twsela.domain.TenantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, Long> {

    List<TenantUser> findByUserId(Long userId);

    List<TenantUser> findByTenantId(Long tenantId);

    Optional<TenantUser> findByUserIdAndTenantId(Long userId, Long tenantId);

    List<TenantUser> findByTenantIdAndRole(Long tenantId, TenantUser.TenantRole role);

    boolean existsByUserIdAndTenantId(Long userId, Long tenantId);
}
