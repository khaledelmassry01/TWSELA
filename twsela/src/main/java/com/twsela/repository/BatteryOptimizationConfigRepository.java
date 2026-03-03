package com.twsela.repository;

import com.twsela.domain.BatteryOptimizationConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatteryOptimizationConfigRepository extends JpaRepository<BatteryOptimizationConfig, Long> {
}
