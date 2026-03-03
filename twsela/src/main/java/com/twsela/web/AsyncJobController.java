package com.twsela.web;

import com.twsela.domain.AsyncJob;
import com.twsela.service.AsyncJobService;
import com.twsela.web.dto.ApiResponse;
import com.twsela.web.dto.AsyncJobDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * متحكم المهام غير المتزامنة.
 */
@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Async Jobs", description = "إدارة المهام غير المتزامنة")
public class AsyncJobController {

    private final AsyncJobService asyncJobService;

    public AsyncJobController(AsyncJobService asyncJobService) {
        this.asyncJobService = asyncJobService;
    }

    @GetMapping
    @Operation(summary = "قائمة المهام")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<AsyncJob>>> getJobs(
            @RequestParam(required = false) List<AsyncJob.JobStatus> statuses) {
        List<AsyncJob> jobs;
        if (statuses != null && !statuses.isEmpty()) {
            jobs = asyncJobService.getJobsByStatus(statuses);
        } else {
            jobs = asyncJobService.getJobsByStatus(
                    List.of(AsyncJob.JobStatus.QUEUED, AsyncJob.JobStatus.RUNNING,
                            AsyncJob.JobStatus.COMPLETED, AsyncJob.JobStatus.FAILED));
        }
        return ResponseEntity.ok(ApiResponse.ok(jobs));
    }

    @PostMapping
    @Operation(summary = "إنشاء مهمة")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AsyncJob>> createJob(
            @Valid @RequestBody AsyncJobDTO.AsyncJobRequest request) {
        AsyncJob job = asyncJobService.createJob(
                request.getJobType(),
                request.getPayload(),
                request.getPriority(),
                request.getMaxRetries());
        return ResponseEntity.ok(ApiResponse.ok(job, "تم إنشاء المهمة بنجاح"));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "تفاصيل مهمة")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AsyncJob>> getJob(@PathVariable String jobId) {
        AsyncJob job = asyncJobService.getByJobId(jobId);
        return ResponseEntity.ok(ApiResponse.ok(job));
    }

    @PostMapping("/{jobId}/cancel")
    @Operation(summary = "إلغاء مهمة")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<AsyncJob>> cancelJob(@PathVariable String jobId) {
        AsyncJob job = asyncJobService.getByJobId(jobId);
        AsyncJob cancelled = asyncJobService.cancelJob(job.getId());
        return ResponseEntity.ok(ApiResponse.ok(cancelled, "تم إلغاء المهمة"));
    }

    @GetMapping("/stats")
    @Operation(summary = "إحصائيات المهام")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = asyncJobService.getStats();
        return ResponseEntity.ok(ApiResponse.ok(stats));
    }
}
