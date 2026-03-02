package com.twsela.repository;

import com.twsela.domain.AssignmentRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRuleRepository extends JpaRepository<AssignmentRule, Long> {

    Optional<AssignmentRule> findByRuleKey(String ruleKey);

    List<AssignmentRule> findByActiveTrue();
}
