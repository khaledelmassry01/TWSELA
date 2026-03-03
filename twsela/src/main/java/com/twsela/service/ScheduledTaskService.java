package com.twsela.service;

import com.twsela.domain.ScheduledTask;
import com.twsela.repository.ScheduledTaskRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class ScheduledTaskService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTaskService.class);

    private final ScheduledTaskRepository scheduledTaskRepository;

    public ScheduledTaskService(ScheduledTaskRepository scheduledTaskRepository) {
        this.scheduledTaskRepository = scheduledTaskRepository;
    }

    public ScheduledTask create(ScheduledTask task) {
        if (scheduledTaskRepository.existsByNameAndTenantId(task.getName(),
                task.getTenant() != null ? task.getTenant().getId() : null)) {
            throw new BusinessRuleException("يوجد مهمة مجدولة بنفس الاسم");
        }
        log.info("إنشاء مهمة مجدولة: {}", task.getName());
        return scheduledTaskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public ScheduledTask findById(Long id) {
        return scheduledTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ScheduledTask", "id", id));
    }

    @Transactional(readOnly = true)
    public List<ScheduledTask> findByTenantId(Long tenantId) {
        return scheduledTaskRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<ScheduledTask> findDueTasks() {
        return scheduledTaskRepository.findByIsActiveTrueAndNextRunAtBefore(Instant.now());
    }

    public ScheduledTask update(Long id, ScheduledTask updated) {
        ScheduledTask existing = findById(id);
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setTaskType(updated.getTaskType());
        existing.setCronExpression(updated.getCronExpression());
        existing.setConfiguration(updated.getConfiguration());
        existing.setNextRunAt(updated.getNextRunAt());
        log.info("تحديث مهمة مجدولة: {}", id);
        return scheduledTaskRepository.save(existing);
    }

    public ScheduledTask activate(Long id) {
        ScheduledTask task = findById(id);
        task.setActive(true);
        log.info("تفعيل مهمة مجدولة: {}", task.getName());
        return scheduledTaskRepository.save(task);
    }

    public ScheduledTask deactivate(Long id) {
        ScheduledTask task = findById(id);
        task.setActive(false);
        log.info("تعطيل مهمة مجدولة: {}", task.getName());
        return scheduledTaskRepository.save(task);
    }

    public ScheduledTask recordRun(Long id, ScheduledTask.TaskStatus status, long durationMs) {
        ScheduledTask task = findById(id);
        task.setLastRunAt(Instant.now());
        task.setLastRunStatus(status);
        task.setLastRunDurationMs(durationMs);
        log.info("تسجيل تشغيل مهمة: {} — حالة: {}", task.getName(), status);
        return scheduledTaskRepository.save(task);
    }

    public void delete(Long id) {
        ScheduledTask task = findById(id);
        log.info("حذف مهمة مجدولة: {}", task.getName());
        scheduledTaskRepository.delete(task);
    }
}
