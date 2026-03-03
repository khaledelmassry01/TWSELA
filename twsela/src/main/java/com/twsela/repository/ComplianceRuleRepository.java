package com.twsela.repository;

import com.twsela.domain.ComplianceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplianceRuleRepository extends JpaRepository<ComplianceRule, Long> {

    List<ComplianceRule> findByCategoryAndEnabledTrue(ComplianceRule.Category category);

    List<ComplianceRule> findByEnabledTrue();

    List<ComplianceRule> findByLastResult(ComplianceRule.CheckResult lastResult);
}
