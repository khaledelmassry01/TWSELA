package com.twsela.repository;

import com.twsela.domain.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    Optional<FeatureFlag> findByFeatureKey(String featureKey);
    List<FeatureFlag> findByIsEnabledTrue();
    boolean existsByFeatureKey(String featureKey);
}
