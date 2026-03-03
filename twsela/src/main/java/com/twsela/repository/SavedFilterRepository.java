package com.twsela.repository;

import com.twsela.domain.SavedFilter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedFilterRepository extends JpaRepository<SavedFilter, Long> {
    List<SavedFilter> findByUserId(Long userId);
    List<SavedFilter> findByEntityType(String entityType);
    List<SavedFilter> findByTenantId(Long tenantId);
}
