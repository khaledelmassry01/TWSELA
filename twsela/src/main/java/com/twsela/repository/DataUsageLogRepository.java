package com.twsela.repository;

import com.twsela.domain.DataUsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DataUsageLogRepository extends JpaRepository<DataUsageLog, Long> {
    List<DataUsageLog> findByUserId(Long userId);
    List<DataUsageLog> findByTenantId(Long tenantId);
}
