package com.twsela.repository;

import com.twsela.domain.CachePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CachePolicyRepository extends JpaRepository<CachePolicy, Long> {
    List<CachePolicy> findByIsActiveTrue();
    Optional<CachePolicy> findByCacheRegion(String cacheRegion);
}
