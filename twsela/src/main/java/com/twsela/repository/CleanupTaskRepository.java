package com.twsela.repository;

import com.twsela.domain.CleanupTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CleanupTaskRepository extends JpaRepository<CleanupTask, Long> {
    List<CleanupTask> findByIsActiveTrue();
    List<CleanupTask> findByTargetTable(String targetTable);
    List<CleanupTask> findByTenantId(Long tenantId);
}
