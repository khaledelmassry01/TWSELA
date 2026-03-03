package com.twsela.repository;

import com.twsela.domain.CustomReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomReportRepository extends JpaRepository<CustomReport, Long> {
    List<CustomReport> findByTenantId(Long tenantId);
    List<CustomReport> findByCreatedById(Long createdById);
    List<CustomReport> findByReportType(String reportType);
    List<CustomReport> findByIsPublicTrue();
}
