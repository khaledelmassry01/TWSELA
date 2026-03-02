package com.twsela.repository;

import com.twsela.domain.OptimizedRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OptimizedRouteRepository extends JpaRepository<OptimizedRoute, Long> {

    List<OptimizedRoute> findByCourierIdAndOptimizedAtAfterOrderByOptimizedAtDesc(Long courierId, Instant after);

    Optional<OptimizedRoute> findByManifestId(Long manifestId);

    Optional<OptimizedRoute> findTopByCourierIdOrderByOptimizedAtDesc(Long courierId);
}
