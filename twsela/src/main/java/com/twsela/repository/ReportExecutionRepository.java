package com.twsela.repository;

import com.twsela.domain.ReportExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportExecutionRepository extends JpaRepository<ReportExecution, Long> {
    List<ReportExecution> findByCustomReportId(Long customReportId);
    List<ReportExecution> findByScheduleId(Long scheduleId);
    List<ReportExecution> findByStatus(String status);
    List<ReportExecution> findByTenantId(Long tenantId);
}
