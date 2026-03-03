package com.twsela.service;

import com.twsela.domain.*;
import com.twsela.repository.*;
import com.twsela.web.dto.OfflineMobileDTO.*;
import com.twsela.web.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DeviceMobileService {

    private final DeviceRegistrationRepository deviceRepository;
    private final BatteryOptimizationConfigRepository batteryConfigRepository;
    private final DataUsageLogRepository dataUsageLogRepository;
    private final AppVersionConfigRepository appVersionConfigRepository;

    public DeviceMobileService(DeviceRegistrationRepository deviceRepository,
                               BatteryOptimizationConfigRepository batteryConfigRepository,
                               DataUsageLogRepository dataUsageLogRepository,
                               AppVersionConfigRepository appVersionConfigRepository) {
        this.deviceRepository = deviceRepository;
        this.batteryConfigRepository = batteryConfigRepository;
        this.dataUsageLogRepository = dataUsageLogRepository;
        this.appVersionConfigRepository = appVersionConfigRepository;
    }

    public DeviceRegistrationResponse registerDevice(RegisterDeviceRequest request, Long tenantId) {
        DeviceRegistration d = new DeviceRegistration();
        d.setUserId(request.userId());
        d.setDeviceId(request.deviceId());
        d.setPlatform(request.platform());
        d.setOsVersion(request.osVersion());
        d.setAppVersion(request.appVersion());
        d.setPushToken(request.pushToken());
        d.setTenantId(tenantId);
        d = deviceRepository.save(d);
        return toDeviceResponse(d);
    }

    @Transactional(readOnly = true)
    public List<DeviceRegistrationResponse> getDevicesByUser(Long userId) {
        return deviceRepository.findByUserId(userId).stream().map(this::toDeviceResponse).toList();
    }

    public BatteryConfigResponse createBatteryConfig(CreateBatteryConfigRequest request) {
        BatteryOptimizationConfig c = new BatteryOptimizationConfig();
        c.setName(request.name());
        if (request.batteryThreshold() != null) c.setBatteryThreshold(request.batteryThreshold());
        if (request.locationIntervalSeconds() != null) c.setLocationIntervalSeconds(request.locationIntervalSeconds());
        if (request.pingIntervalSeconds() != null) c.setPingIntervalSeconds(request.pingIntervalSeconds());
        if (request.syncIntervalSeconds() != null) c.setSyncIntervalSeconds(request.syncIntervalSeconds());
        c = batteryConfigRepository.save(c);
        return toBatteryResponse(c);
    }

    @Transactional(readOnly = true)
    public List<BatteryConfigResponse> getAllBatteryConfigs() {
        return batteryConfigRepository.findAll().stream().map(this::toBatteryResponse).toList();
    }

    public AppVersionConfigResponse createAppVersionConfig(CreateAppVersionConfigRequest request) {
        AppVersionConfig v = new AppVersionConfig();
        v.setPlatform(request.platform());
        v.setMinVersion(request.minVersion());
        v.setCurrentVersion(request.currentVersion());
        v.setUpdateUrl(request.updateUrl());
        if (request.forceUpdate() != null) v.setForceUpdate(request.forceUpdate());
        v.setReleaseNotes(request.releaseNotes());
        v = appVersionConfigRepository.save(v);
        return toVersionResponse(v);
    }

    @Transactional(readOnly = true)
    public List<AppVersionConfigResponse> getVersionsByPlatform(String platform) {
        return appVersionConfigRepository.findByPlatform(platform).stream()
                .map(this::toVersionResponse).toList();
    }

    private DeviceRegistrationResponse toDeviceResponse(DeviceRegistration d) {
        return new DeviceRegistrationResponse(d.getId(), d.getUserId(), d.getDeviceId(),
                d.getPlatform(), d.getOsVersion(), d.getAppVersion(), d.getPushToken(),
                d.getLastActiveAt(), d.getTenantId(), d.getCreatedAt());
    }

    private BatteryConfigResponse toBatteryResponse(BatteryOptimizationConfig c) {
        return new BatteryConfigResponse(c.getId(), c.getName(), c.getBatteryThreshold(),
                c.getLocationIntervalSeconds(), c.getPingIntervalSeconds(),
                c.getSyncIntervalSeconds(), c.getCreatedAt());
    }

    private AppVersionConfigResponse toVersionResponse(AppVersionConfig v) {
        return new AppVersionConfigResponse(v.getId(), v.getPlatform(), v.getMinVersion(),
                v.getCurrentVersion(), v.getUpdateUrl(), v.getForceUpdate(),
                v.getReleaseNotes(), v.getCreatedAt());
    }
}
