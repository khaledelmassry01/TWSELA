package com.twsela.repository;

import com.twsela.domain.SystemAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SystemAuditLogRepository extends JpaRepository<SystemAuditLog, Long> {
    
    List<SystemAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<SystemAuditLog> findByActionTypeOrderByCreatedAtDesc(String actionType);
    
    List<SystemAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);
    
    @Query("SELECT s FROM SystemAuditLog s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<SystemAuditLog> findByCreatedAtBetween(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}
