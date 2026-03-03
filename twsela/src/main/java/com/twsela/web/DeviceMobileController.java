package com.twsela.web;

import com.twsela.service.DeviceMobileService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.OfflineMobileDTO.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mobile")
@PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
public class DeviceMobileController {

    private final DeviceMobileService mobileService;

    public DeviceMobileController(DeviceMobileService mobileService) {
        this.mobileService = mobileService;
    }

    @PostMapping("/devices")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'COURIER')")
    public ResponseEntity<ApiResponse<DeviceRegistrationResponse>> registerDevice(
            @Valid @RequestBody RegisterDeviceRequest request,
            @RequestParam(required = false) Long tenantId) {
        return ResponseEntity.ok(ApiResponse.ok(mobileService.registerDevice(request, tenantId)));
    }

    @GetMapping("/devices/user/{userId}")
    public ResponseEntity<ApiResponse<List<DeviceRegistrationResponse>>> getDevicesByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(mobileService.getDevicesByUser(userId)));
    }

    @PostMapping("/battery-configs")
    public ResponseEntity<ApiResponse<BatteryConfigResponse>> createBatteryConfig(
            @Valid @RequestBody CreateBatteryConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(mobileService.createBatteryConfig(request)));
    }

    @GetMapping("/battery-configs")
    public ResponseEntity<ApiResponse<List<BatteryConfigResponse>>> getAllBatteryConfigs() {
        return ResponseEntity.ok(ApiResponse.ok(mobileService.getAllBatteryConfigs()));
    }

    @PostMapping("/app-versions")
    public ResponseEntity<ApiResponse<AppVersionConfigResponse>> createAppVersionConfig(
            @Valid @RequestBody CreateAppVersionConfigRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(mobileService.createAppVersionConfig(request)));
    }

    @GetMapping("/app-versions/{platform}")
    public ResponseEntity<ApiResponse<List<AppVersionConfigResponse>>> getVersionsByPlatform(
            @PathVariable String platform) {
        return ResponseEntity.ok(ApiResponse.ok(mobileService.getVersionsByPlatform(platform)));
    }
}
