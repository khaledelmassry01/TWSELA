package com.twsela.repository;

import com.twsela.domain.ReportSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportScheduleRepository extends JpaRepository<ReportSchedule, Long> {
    List<ReportSchedule> findByCustomReportId(Long customReportId);
    List<ReportSchedule> findByEnabledTrue();
    List<ReportSchedule> findByTenantId(Long tenantId);
}
