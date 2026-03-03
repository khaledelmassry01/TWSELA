package com.twsela.repository;

import com.twsela.domain.RateLimitOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RateLimitOverrideRepository extends JpaRepository<RateLimitOverride, Long> {
    List<RateLimitOverride> findByRateLimitPolicyId(Long policyId);
    List<RateLimitOverride> findByOverrideTypeAndOverrideValue(String type, String value);
}
