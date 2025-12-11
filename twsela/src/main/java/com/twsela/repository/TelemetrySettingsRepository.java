package com.twsela.repository;

import com.twsela.domain.TelemetrySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TelemetrySettingsRepository extends JpaRepository<TelemetrySettings, Long> {
    
    Optional<TelemetrySettings> findBySettingKey(String settingKey);
    
    boolean existsBySettingKey(String settingKey);
}
