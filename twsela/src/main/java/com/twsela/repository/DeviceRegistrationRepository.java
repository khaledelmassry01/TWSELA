package com.twsela.repository;

import com.twsela.domain.DeviceRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRegistrationRepository extends JpaRepository<DeviceRegistration, Long> {
    List<DeviceRegistration> findByUserId(Long userId);
    Optional<DeviceRegistration> findByUserIdAndDeviceId(Long userId, String deviceId);
    List<DeviceRegistration> findByPlatform(String platform);
    List<DeviceRegistration> findByTenantId(Long tenantId);
}
