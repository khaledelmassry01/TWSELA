package com.twsela.repository;

import com.twsela.domain.DocumentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentTemplateRepository extends JpaRepository<DocumentTemplate, Long> {
    List<DocumentTemplate> findByType(String type);
    List<DocumentTemplate> findByTenantId(Long tenantId);
    List<DocumentTemplate> findByIsDefaultTrue();
}
