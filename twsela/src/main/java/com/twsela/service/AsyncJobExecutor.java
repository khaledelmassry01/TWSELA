package com.twsela.service;

import com.twsela.domain.AsyncJob;
import com.twsela.repository.AsyncJobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * منفذ المهام غير المتزامنة — يسحب من القائمة وينفذ.
 */
@Service
public class AsyncJobExecutor {

    private static final Logger log = LoggerFactory.getLogger(AsyncJobExecutor.class);

    private final AsyncJobRepository asyncJobRepository;
    private final AsyncJobService asyncJobService;

    public AsyncJobExecutor(AsyncJobRepository asyncJobRepository,
                             AsyncJobService asyncJobService) {
        this.asyncJobRepository = asyncJobRepository;
        this.asyncJobService = asyncJobService;
    }

    /**
     * تنفيذ المهام المجدولة.
     */
    @Async
    @Transactional
    public void executeQueuedJobs() {
        List<AsyncJob> queued = asyncJobRepository.findByStatusAndScheduledAtBefore(
                AsyncJob.JobStatus.QUEUED, Instant.now());

        for (AsyncJob job : queued) {
            try {
                asyncJobService.startJob(job.getId());
                executeJob(job);
                asyncJobService.completeJob(job.getId(), "{\"status\":\"success\"}");
            } catch (Exception e) {
                log.error("Job execution failed: jobId={}, error={}", job.getJobId(), e.getMessage());
                asyncJobService.failJob(job.getId(), e.getMessage());
            }
        }
    }

    /**
     * تنفيذ مهمة واحدة حسب النوع.
     */
    void executeJob(AsyncJob job) {
        log.info("Executing job: type={}, jobId={}", job.getJobType(), job.getJobId());
        switch (job.getJobType()) {
            case "BULK_SHIPMENT_PROCESS" -> processBulkShipments(job);
            case "REPORT_GENERATION" -> generateReport(job);
            case "SETTLEMENT_CALCULATION" -> calculateSettlements(job);
            default -> log.info("Generic job execution for type: {}", job.getJobType());
        }
    }

    private void processBulkShipments(AsyncJob job) {
        log.info("Processing bulk shipments: {}", job.getPayload());
    }

    private void generateReport(AsyncJob job) {
        log.info("Generating report: {}", job.getPayload());
    }

    private void calculateSettlements(AsyncJob job) {
        log.info("Calculating settlements: {}", job.getPayload());
    }
}
