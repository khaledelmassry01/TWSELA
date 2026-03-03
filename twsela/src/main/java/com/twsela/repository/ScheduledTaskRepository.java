package com.twsela.repository;

import com.twsela.domain.ScheduledTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {

    List<ScheduledTask> findByTenantId(Long tenantId);

    List<ScheduledTask> findByIsActiveTrueAndNextRunAtBefore(Instant now);

    List<ScheduledTask> findByTenantIdAndIsActiveTrue(Long tenantId);

    boolean existsByNameAndTenantId(String name, Long tenantId);
}
