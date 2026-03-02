package com.twsela.repository;

import com.twsela.domain.SubscriptionPlan;
import com.twsela.domain.SubscriptionPlan.PlanName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {

    Optional<SubscriptionPlan> findByName(PlanName name);

    List<SubscriptionPlan> findByActiveTrueOrderBySortOrderAsc();
}
