package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.web.dto.PlatformOpsDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PlatformOpsService {

    private final PlatformMetricRepository platformMetricRepository;
    private final SystemAlertRepository systemAlertRepository;
    private final MaintenanceWindowRepository maintenanceWindowRepository;

    public PlatformOpsService(PlatformMetricRepository platformMetricRepository,
                              SystemAlertRepository systemAlertRepository,
                              MaintenanceWindowRepository maintenanceWindowRepository) {
        this.platformMetricRepository = platformMetricRepository;
        this.systemAlertRepository = systemAlertRepository;
        this.maintenanceWindowRepository = maintenanceWindowRepository;
    }

    // ── Platform Metrics ──

    @Transactional(readOnly = true)
    public List<PlatformMetric> getAllMetrics() {
        return platformMetricRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PlatformMetric> getMetricsByName(String metricName) {
        return platformMetricRepository.findByMetricName(metricName);
    }

    public PlatformMetric createMetric(CreatePlatformMetricRequest request) {
        PlatformMetric metric = new PlatformMetric();
        metric.setMetricName(request.metricName());
        metric.setMetricValue(request.metricValue());
        metric.setMetricType(request.metricType() != null ? request.metricType() : "GAUGE");
        metric.setLabels(request.labels());
        return platformMetricRepository.save(metric);
    }

    // ── System Alerts ──

    @Transactional(readOnly = true)
    public List<SystemAlert> getAllAlerts(Long tenantId) {
        if (tenantId != null) {
            return systemAlertRepository.findByTenantId(tenantId);
        }
        return systemAlertRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<SystemAlert> getUnacknowledgedAlerts() {
        return systemAlertRepository.findByAcknowledgedFalse();
    }

    @Transactional(readOnly = true)
    public SystemAlert getAlertById(Long id) {
        return systemAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("System alert not found with id: " + id));
    }

    public SystemAlert createAlert(CreateSystemAlertRequest request, Long tenantId) {
        SystemAlert alert = new SystemAlert();
        alert.setAlertType(request.alertType());
        alert.setSeverity(request.severity() != null ? request.severity() : "INFO");
        alert.setMessage(request.message());
        alert.setComponent(request.component());
        alert.setTenantId(tenantId);
        return systemAlertRepository.save(alert);
    }

    public SystemAlert acknowledgeAlert(Long id, Long userId) {
        SystemAlert alert = getAlertById(id);
        alert.setAcknowledged(true);
        alert.setAcknowledgedById(userId);
        alert.setAcknowledgedAt(LocalDateTime.now());
        return systemAlertRepository.save(alert);
    }

    // ── Maintenance Windows ──

    @Transactional(readOnly = true)
    public List<MaintenanceWindow> getAllMaintenanceWindows(Long tenantId) {
        if (tenantId != null) {
            return maintenanceWindowRepository.findByTenantId(tenantId);
        }
        return maintenanceWindowRepository.findAll();
    }

    @Transactional(readOnly = true)
    public MaintenanceWindow getMaintenanceWindowById(Long id) {
        return maintenanceWindowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance window not found with id: " + id));
    }

    public MaintenanceWindow createMaintenanceWindow(CreateMaintenanceWindowRequest request, Long userId, Long tenantId) {
        MaintenanceWindow window = new MaintenanceWindow();
        window.setTitle(request.title());
        window.setDescription(request.description());
        window.setStartAt(request.startAt());
        window.setEndAt(request.endAt());
        window.setAffectedComponents(request.affectedComponents());
        window.setCreatedById(userId);
        window.setTenantId(tenantId);
        return maintenanceWindowRepository.save(window);
    }

    public void deleteMaintenanceWindow(Long id) {
        if (!maintenanceWindowRepository.existsById(id)) {
            throw new ResourceNotFoundException("Maintenance window not found with id: " + id);
        }
        maintenanceWindowRepository.deleteById(id);
    }
}
