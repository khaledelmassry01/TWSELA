package com.twsela.repository;

import com.twsela.domain.ReportWidget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportWidgetRepository extends JpaRepository<ReportWidget, Long> {
    List<ReportWidget> findByDashboardIdOrderByDisplayOrderAsc(String dashboardId);
    List<ReportWidget> findByUserId(Long userId);
    List<ReportWidget> findByTenantId(Long tenantId);
}
