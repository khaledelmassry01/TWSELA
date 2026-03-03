package com.twsela.repository;

import com.twsela.domain.ComplianceReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComplianceReportRepository extends JpaRepository<ComplianceReport, Long> {

    @Query("SELECT r FROM ComplianceReport r ORDER BY r.createdAt DESC LIMIT 1")
    Optional<ComplianceReport> findLatest();

    @Query("SELECT r FROM ComplianceReport r WHERE r.reportDate BETWEEN :start AND :end ORDER BY r.reportDate DESC")
    List<ComplianceReport> findByReportDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    List<ComplianceReport> findByStatus(ComplianceReport.ReportStatus status);
}
