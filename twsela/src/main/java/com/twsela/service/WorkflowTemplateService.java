package com.twsela.service;

import com.twsela.domain.WorkflowTemplate;
import com.twsela.repository.WorkflowTemplateRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WorkflowTemplateService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowTemplateService.class);

    private final WorkflowTemplateRepository workflowTemplateRepository;

    public WorkflowTemplateService(WorkflowTemplateRepository workflowTemplateRepository) {
        this.workflowTemplateRepository = workflowTemplateRepository;
    }

    public WorkflowTemplate create(WorkflowTemplate template) {
        if (workflowTemplateRepository.existsByName(template.getName())) {
            throw new BusinessRuleException("يوجد قالب سلسلة عمل بنفس الاسم");
        }
        log.info("إنشاء قالب سلسلة عمل: {}", template.getName());
        return workflowTemplateRepository.save(template);
    }

    @Transactional(readOnly = true)
    public WorkflowTemplate findById(Long id) {
        return workflowTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WorkflowTemplate", "id", id));
    }

    @Transactional(readOnly = true)
    public List<WorkflowTemplate> findAll() {
        return workflowTemplateRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<WorkflowTemplate> findByCategory(WorkflowTemplate.TemplateCategory category) {
        return workflowTemplateRepository.findByCategory(category);
    }

    @Transactional(readOnly = true)
    public List<WorkflowTemplate> findSystemTemplates() {
        return workflowTemplateRepository.findByIsSystemTrue();
    }

    public WorkflowTemplate update(Long id, WorkflowTemplate updated) {
        WorkflowTemplate existing = findById(id);
        if (existing.isSystem()) {
            throw new BusinessRuleException("لا يمكن تعديل قوالب النظام");
        }
        existing.setName(updated.getName());
        existing.setNameAr(updated.getNameAr());
        existing.setDescription(updated.getDescription());
        existing.setDescriptionAr(updated.getDescriptionAr());
        existing.setCategory(updated.getCategory());
        existing.setTemplateDefinition(updated.getTemplateDefinition());
        log.info("تحديث قالب سلسلة عمل: {}", id);
        return workflowTemplateRepository.save(existing);
    }

    public WorkflowTemplate incrementUsage(Long id) {
        WorkflowTemplate template = findById(id);
        template.setUsageCount(template.getUsageCount() + 1);
        return workflowTemplateRepository.save(template);
    }

    public void delete(Long id) {
        WorkflowTemplate template = findById(id);
        if (template.isSystem()) {
            throw new BusinessRuleException("لا يمكن حذف قوالب النظام");
        }
        log.info("حذف قالب سلسلة عمل: {}", template.getName());
        workflowTemplateRepository.delete(template);
    }
}
