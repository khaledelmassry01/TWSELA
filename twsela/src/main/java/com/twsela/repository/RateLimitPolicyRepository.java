package com.twsela.repository;

import com.twsela.domain.RateLimitPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RateLimitPolicyRepository extends JpaRepository<RateLimitPolicy, Long> {
    List<RateLimitPolicy> findByIsActiveTrue();
    List<RateLimitPolicy> findByPolicyType(String policyType);
}
