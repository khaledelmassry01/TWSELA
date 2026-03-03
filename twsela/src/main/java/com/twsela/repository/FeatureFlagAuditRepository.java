package com.twsela.repository;

import com.twsela.domain.FeatureFlagAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FeatureFlagAuditRepository extends JpaRepository<FeatureFlagAudit, Long> {
    List<FeatureFlagAudit> findByFeatureFlagIdOrderByCreatedAtDesc(Long featureFlagId);
}
