package com.twsela.repository;

import com.twsela.domain.PlatformMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatformMetricRepository extends JpaRepository<PlatformMetric, Long> {
    List<PlatformMetric> findByMetricName(String metricName);
    List<PlatformMetric> findByMetricType(String metricType);
}
