package com.twsela.repository;

import com.twsela.domain.OfflineQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OfflineQueueRepository extends JpaRepository<OfflineQueue, Long> {
    List<OfflineQueue> findByUserIdAndStatusOrderByPriorityAsc(Long userId, String status);
    List<OfflineQueue> findByStatus(String status);
    List<OfflineQueue> findByTenantId(Long tenantId);
}
