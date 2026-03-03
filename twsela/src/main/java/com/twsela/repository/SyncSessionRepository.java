package com.twsela.repository;

import com.twsela.domain.SyncSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SyncSessionRepository extends JpaRepository<SyncSession, Long> {
    List<SyncSession> findByUserId(Long userId);
    List<SyncSession> findByStatus(String status);
    List<SyncSession> findByTenantId(Long tenantId);
}
