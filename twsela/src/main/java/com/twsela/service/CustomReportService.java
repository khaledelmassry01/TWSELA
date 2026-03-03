package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.web.dto.ReportingAnalyticsDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CustomReportService {

    private final CustomReportRepository customReportRepository;
    private final ReportScheduleRepository reportScheduleRepository;
    private final ReportExecutionRepository reportExecutionRepository;
    private final SavedFilterRepository savedFilterRepository;

    public CustomReportService(CustomReportRepository customReportRepository,
                               ReportScheduleRepository reportScheduleRepository,
                               ReportExecutionRepository reportExecutionRepository,
                               SavedFilterRepository savedFilterRepository) {
        this.customReportRepository = customReportRepository;
        this.reportScheduleRepository = reportScheduleRepository;
        this.reportExecutionRepository = reportExecutionRepository;
        this.savedFilterRepository = savedFilterRepository;
    }

    // ── Custom Reports ──

    @Transactional(readOnly = true)
    public List<CustomReport> getAllReports(Long tenantId) {
        if (tenantId != null) {
            return customReportRepository.findByTenantId(tenantId);
        }
        return customReportRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CustomReport getReportById(Long id) {
        return customReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Custom report not found with id: " + id));
    }

    public CustomReport createReport(CreateCustomReportRequest request, Long userId, Long tenantId) {
        CustomReport report = new CustomReport();
        report.setName(request.name());
        report.setDescription(request.description());
        report.setReportType(request.reportType());
        report.setQueryConfig(request.queryConfig());
        report.setColumns(request.columns());
        report.setFilters(request.filters());
        report.setIsPublic(request.isPublic() != null ? request.isPublic() : false);
        report.setCreatedById(userId);
        report.setTenantId(tenantId);
        return customReportRepository.save(report);
    }

    public CustomReport updateReport(Long id, UpdateCustomReportRequest request) {
        CustomReport report = getReportById(id);
        if (request.name() != null) report.setName(request.name());
        if (request.description() != null) report.setDescription(request.description());
        if (request.queryConfig() != null) report.setQueryConfig(request.queryConfig());
        if (request.columns() != null) report.setColumns(request.columns());
        if (request.filters() != null) report.setFilters(request.filters());
        if (request.isPublic() != null) report.setIsPublic(request.isPublic());
        return customReportRepository.save(report);
    }

    public void deleteReport(Long id) {
        if (!customReportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Custom report not found with id: " + id);
        }
        customReportRepository.deleteById(id);
    }

    // ── Report Schedules ──

    @Transactional(readOnly = true)
    public List<ReportSchedule> getSchedulesByReport(Long customReportId) {
        return reportScheduleRepository.findByCustomReportId(customReportId);
    }

    public ReportSchedule createSchedule(CreateReportScheduleRequest request, Long tenantId) {
        ReportSchedule schedule = new ReportSchedule();
        schedule.setCustomReportId(request.customReportId());
        schedule.setCronExpression(request.cronExpression());
        schedule.setFormat(request.format() != null ? request.format() : "PDF");
        schedule.setRecipients(request.recipients());
        schedule.setEnabled(request.enabled() != null ? request.enabled() : true);
        schedule.setTenantId(tenantId);
        return reportScheduleRepository.save(schedule);
    }

    public void deleteSchedule(Long id) {
        if (!reportScheduleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report schedule not found with id: " + id);
        }
        reportScheduleRepository.deleteById(id);
    }

    // ── Report Executions ──

    @Transactional(readOnly = true)
    public List<ReportExecution> getExecutionsByReport(Long customReportId) {
        return reportExecutionRepository.findByCustomReportId(customReportId);
    }

    public ReportExecution createExecution(CreateReportExecutionRequest request, Long tenantId) {
        ReportExecution execution = new ReportExecution();
        execution.setCustomReportId(request.customReportId());
        execution.setScheduleId(request.scheduleId());
        execution.setTenantId(tenantId);
        return reportExecutionRepository.save(execution);
    }

    // ── Saved Filters ──

    @Transactional(readOnly = true)
    public List<SavedFilter> getFiltersByUser(Long userId) {
        return savedFilterRepository.findByUserId(userId);
    }

    public SavedFilter createFilter(CreateSavedFilterRequest request, Long userId, Long tenantId) {
        SavedFilter filter = new SavedFilter();
        filter.setName(request.name());
        filter.setEntityType(request.entityType());
        filter.setFilterConfig(request.filterConfig());
        filter.setIsDefault(request.isDefault() != null ? request.isDefault() : false);
        filter.setUserId(userId);
        filter.setTenantId(tenantId);
        return savedFilterRepository.save(filter);
    }

    public void deleteFilter(Long id) {
        if (!savedFilterRepository.existsById(id)) {
            throw new ResourceNotFoundException("Saved filter not found with id: " + id);
        }
        savedFilterRepository.deleteById(id);
    }
}
