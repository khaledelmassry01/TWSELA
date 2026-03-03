package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.web.dto.ReportingAnalyticsDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class DataPipelineService {

    private final DataExportJobRepository dataExportJobRepository;
    private final DataPipelineConfigRepository dataPipelineConfigRepository;
    private final PipelineExecutionRepository pipelineExecutionRepository;
    private final ReportWidgetRepository reportWidgetRepository;

    public DataPipelineService(DataExportJobRepository dataExportJobRepository,
                               DataPipelineConfigRepository dataPipelineConfigRepository,
                               PipelineExecutionRepository pipelineExecutionRepository,
                               ReportWidgetRepository reportWidgetRepository) {
        this.dataExportJobRepository = dataExportJobRepository;
        this.dataPipelineConfigRepository = dataPipelineConfigRepository;
        this.pipelineExecutionRepository = pipelineExecutionRepository;
        this.reportWidgetRepository = reportWidgetRepository;
    }

    // ── Data Export Jobs ──

    @Transactional(readOnly = true)
    public List<DataExportJob> getAllExportJobs(Long tenantId) {
        if (tenantId != null) {
            return dataExportJobRepository.findByTenantId(tenantId);
        }
        return dataExportJobRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DataExportJob getExportJobById(Long id) {
        return dataExportJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Data export job not found with id: " + id));
    }

    public DataExportJob createExportJob(CreateDataExportJobRequest request, Long userId, Long tenantId) {
        DataExportJob job = new DataExportJob();
        job.setEntityType(request.entityType());
        job.setFilters(request.filters());
        job.setFormat(request.format() != null ? request.format() : "CSV");
        job.setRequestedById(userId);
        job.setTenantId(tenantId);
        return dataExportJobRepository.save(job);
    }

    // ── Data Pipeline Configs ──

    @Transactional(readOnly = true)
    public List<DataPipelineConfig> getAllPipelineConfigs(Long tenantId) {
        if (tenantId != null) {
            return dataPipelineConfigRepository.findByTenantId(tenantId);
        }
        return dataPipelineConfigRepository.findAll();
    }

    @Transactional(readOnly = true)
    public DataPipelineConfig getPipelineConfigById(Long id) {
        return dataPipelineConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Data pipeline config not found with id: " + id));
    }

    public DataPipelineConfig createPipelineConfig(CreateDataPipelineConfigRequest request, Long tenantId) {
        DataPipelineConfig config = new DataPipelineConfig();
        config.setName(request.name());
        config.setSourceType(request.sourceType());
        config.setSourceConfig(request.sourceConfig());
        config.setTransformRules(request.transformRules());
        config.setDestinationType(request.destinationType());
        config.setDestConfig(request.destConfig());
        config.setSchedule(request.schedule());
        config.setIsActive(request.isActive() != null ? request.isActive() : true);
        config.setTenantId(tenantId);
        return dataPipelineConfigRepository.save(config);
    }

    public void deletePipelineConfig(Long id) {
        if (!dataPipelineConfigRepository.existsById(id)) {
            throw new ResourceNotFoundException("Data pipeline config not found with id: " + id);
        }
        dataPipelineConfigRepository.deleteById(id);
    }

    // ── Pipeline Executions ──

    @Transactional(readOnly = true)
    public List<PipelineExecution> getExecutionsByPipeline(Long pipelineConfigId) {
        return pipelineExecutionRepository.findByPipelineConfigId(pipelineConfigId);
    }

    public PipelineExecution startExecution(Long pipelineConfigId, Long tenantId) {
        PipelineExecution execution = new PipelineExecution();
        execution.setPipelineConfigId(pipelineConfigId);
        execution.setStartedAt(LocalDateTime.now());
        execution.setTenantId(tenantId);
        return pipelineExecutionRepository.save(execution);
    }

    // ── Report Widgets ──

    @Transactional(readOnly = true)
    public List<ReportWidget> getWidgetsByDashboard(String dashboardId) {
        return reportWidgetRepository.findByDashboardIdOrderByDisplayOrderAsc(dashboardId);
    }

    @Transactional(readOnly = true)
    public List<ReportWidget> getWidgetsByUser(Long userId) {
        return reportWidgetRepository.findByUserId(userId);
    }

    public ReportWidget createWidget(CreateReportWidgetRequest request, Long userId, Long tenantId) {
        ReportWidget widget = new ReportWidget();
        widget.setName(request.name());
        widget.setReportType(request.reportType());
        widget.setChartType(request.chartType() != null ? request.chartType() : "BAR");
        widget.setQueryConfig(request.queryConfig());
        widget.setDisplayOrder(request.displayOrder() != null ? request.displayOrder() : 0);
        widget.setDashboardId(request.dashboardId());
        widget.setUserId(userId);
        widget.setTenantId(tenantId);
        return reportWidgetRepository.save(widget);
    }

    public void deleteWidget(Long id) {
        if (!reportWidgetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report widget not found with id: " + id);
        }
        reportWidgetRepository.deleteById(id);
    }
}
