package com.twsela.repository;

import com.twsela.domain.RateLimitViolation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RateLimitViolationRepository extends JpaRepository<RateLimitViolation, Long> {
    List<RateLimitViolation> findByViolatorTypeAndViolatorValueOrderByBlockedAtDesc(String type, String value);
    List<RateLimitViolation> findByRateLimitPolicyIdOrderByBlockedAtDesc(Long policyId);
}
