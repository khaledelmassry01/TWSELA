package com.twsela.service;

import com.twsela.domain.AsyncJob;
import com.twsela.repository.AsyncJobRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * خدمة إنشاء وإدارة المهام غير المتزامنة.
 */
@Service
@Transactional
public class AsyncJobService {

    private static final Logger log = LoggerFactory.getLogger(AsyncJobService.class);
    private static final int MAX_CONCURRENT_JOBS = 10;

    private final AsyncJobRepository asyncJobRepository;

    public AsyncJobService(AsyncJobRepository asyncJobRepository) {
        this.asyncJobRepository = asyncJobRepository;
    }

    /**
     * إنشاء مهمة جديدة.
     */
    public AsyncJob createJob(String jobType, String payload, int priority, int maxRetries) {
        AsyncJob job = new AsyncJob();
        job.setJobType(jobType);
        job.setPayload(payload);
        job.setPriority(priority);
        job.setMaxRetries(maxRetries);
        job.setStatus(AsyncJob.JobStatus.QUEUED);
        job.setScheduledAt(Instant.now());

        AsyncJob saved = asyncJobRepository.save(job);
        log.info("Async job created: type={}, jobId={}, priority={}", jobType, saved.getJobId(), priority);
        return saved;
    }

    /**
     * بدء تنفيذ مهمة.
     */
    public AsyncJob startJob(Long jobId) {
        AsyncJob job = asyncJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("AsyncJob", "id", jobId));

        if (job.getStatus() != AsyncJob.JobStatus.QUEUED) {
            throw new BusinessRuleException("المهمة ليست في قائمة الانتظار — الحالة الحالية: " + job.getStatus());
        }

        // Check concurrent job limit
        List<AsyncJob> running = asyncJobRepository.findRunningJobs();
        if (running.size() >= MAX_CONCURRENT_JOBS) {
            throw new BusinessRuleException("الحد الأقصى للمهام المتزامنة (" + MAX_CONCURRENT_JOBS + ") قد تم الوصول إليه");
        }

        job.setStatus(AsyncJob.JobStatus.RUNNING);
        job.setStartedAt(Instant.now());

        log.info("Job {} started: type={}", job.getJobId(), job.getJobType());
        return asyncJobRepository.save(job);
    }

    /**
     * إتمام مهمة بنجاح.
     */
    public AsyncJob completeJob(Long jobId, String result) {
        AsyncJob job = asyncJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("AsyncJob", "id", jobId));

        job.setStatus(AsyncJob.JobStatus.COMPLETED);
        job.setCompletedAt(Instant.now());
        job.setResult(result);

        log.info("Job {} completed successfully", job.getJobId());
        return asyncJobRepository.save(job);
    }

    /**
     * تسجيل فشل مهمة مع إعادة محاولة.
     */
    public AsyncJob failJob(Long jobId, String errorMessage) {
        AsyncJob job = asyncJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("AsyncJob", "id", jobId));

        job.setRetryCount(job.getRetryCount() + 1);
        job.setErrorMessage(errorMessage);

        if (job.getRetryCount() >= job.getMaxRetries()) {
            job.setStatus(AsyncJob.JobStatus.FAILED);
            job.setCompletedAt(Instant.now());
            log.error("Job {} failed permanently after {} retries", job.getJobId(), job.getRetryCount());
        } else {
            job.setStatus(AsyncJob.JobStatus.QUEUED);
            log.warn("Job {} failed — retry {}/{}", job.getJobId(), job.getRetryCount(), job.getMaxRetries());
        }

        return asyncJobRepository.save(job);
    }

    /**
     * إلغاء مهمة.
     */
    public AsyncJob cancelJob(Long jobId) {
        AsyncJob job = asyncJobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("AsyncJob", "id", jobId));

        if (job.getStatus() == AsyncJob.JobStatus.COMPLETED || job.getStatus() == AsyncJob.JobStatus.CANCELLED) {
            throw new BusinessRuleException("لا يمكن إلغاء مهمة في الحالة: " + job.getStatus());
        }

        job.setStatus(AsyncJob.JobStatus.CANCELLED);
        job.setCompletedAt(Instant.now());

        log.info("Job {} cancelled", job.getJobId());
        return asyncJobRepository.save(job);
    }

    /**
     * جلب مهمة حسب jobId (UUID).
     */
    @Transactional(readOnly = true)
    public AsyncJob getByJobId(String jobId) {
        return asyncJobRepository.findByJobId(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("AsyncJob", "jobId", jobId));
    }

    /**
     * جلب المهام حسب الحالة.
     */
    @Transactional(readOnly = true)
    public List<AsyncJob> getJobsByStatus(List<AsyncJob.JobStatus> statuses) {
        return asyncJobRepository.findByStatusIn(statuses);
    }

    /**
     * إحصائيات المهام.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("queued", asyncJobRepository.countByStatus(AsyncJob.JobStatus.QUEUED));
        stats.put("running", asyncJobRepository.countByStatus(AsyncJob.JobStatus.RUNNING));
        stats.put("completed", asyncJobRepository.countByStatus(AsyncJob.JobStatus.COMPLETED));
        stats.put("failed", asyncJobRepository.countByStatus(AsyncJob.JobStatus.FAILED));
        stats.put("cancelled", asyncJobRepository.countByStatus(AsyncJob.JobStatus.CANCELLED));
        stats.put("total", asyncJobRepository.count());
        return stats;
    }
}
