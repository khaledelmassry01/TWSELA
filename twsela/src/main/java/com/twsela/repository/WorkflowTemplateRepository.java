package com.twsela.repository;

import com.twsela.domain.WorkflowTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowTemplateRepository extends JpaRepository<WorkflowTemplate, Long> {

    List<WorkflowTemplate> findByCategory(WorkflowTemplate.TemplateCategory category);

    List<WorkflowTemplate> findByIsSystemTrue();

    boolean existsByName(String name);
}
