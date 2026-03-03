package com.twsela.service;

import com.twsela.domain.AsyncJob;
import com.twsela.repository.AsyncJobRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncJobServiceTest {

    @Mock
    private AsyncJobRepository asyncJobRepository;

    @InjectMocks
    private AsyncJobService asyncJobService;

    private AsyncJob sampleJob;

    @BeforeEach
    void setUp() {
        sampleJob = new AsyncJob();
        sampleJob.setId(1L);
        sampleJob.setJobId("job-uuid-123");
        sampleJob.setJobType("BULK_SHIPMENT_PROCESS");
        sampleJob.setPayload("{\"shipmentIds\":[1,2,3]}");
        sampleJob.setStatus(AsyncJob.JobStatus.QUEUED);
        sampleJob.setPriority(5);
        sampleJob.setMaxRetries(3);
        sampleJob.setRetryCount(0);
        sampleJob.setScheduledAt(Instant.now());
    }

    @Test
    @DisplayName("إنشاء مهمة جديدة")
    void createJob_shouldCreateAndReturn() {
        when(asyncJobRepository.save(any(AsyncJob.class))).thenAnswer(inv -> {
            AsyncJob j = inv.getArgument(0);
            j.setId(1L);
            j.setJobId("job-uuid-new");
            return j;
        });

        AsyncJob result = asyncJobService.createJob("REPORT_GENERATION", "{}", 3, 5);

        assertThat(result.getJobType()).isEqualTo("REPORT_GENERATION");
        assertThat(result.getPriority()).isEqualTo(3);
        assertThat(result.getMaxRetries()).isEqualTo(5);
        assertThat(result.getStatus()).isEqualTo(AsyncJob.JobStatus.QUEUED);
    }

    @Test
    @DisplayName("بدء تنفيذ مهمة")
    void startJob_shouldSetRunning() {
        when(asyncJobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));
        when(asyncJobRepository.findRunningJobs()).thenReturn(Collections.emptyList());
        when(asyncJobRepository.save(any(AsyncJob.class))).thenAnswer(inv -> inv.getArgument(0));

        AsyncJob result = asyncJobService.startJob(1L);

        assertThat(result.getStatus()).isEqualTo(AsyncJob.JobStatus.RUNNING);
        assertThat(result.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("بدء مهمة غير في قائمة الانتظار")
    void startJob_notQueued_shouldThrow() {
        sampleJob.setStatus(AsyncJob.JobStatus.RUNNING);
        when(asyncJobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));

        assertThatThrownBy(() -> asyncJobService.startJob(1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("بدء مهمة مع تجاوز الحد الأقصى")
    void startJob_maxConcurrent_shouldThrow() {
        List<AsyncJob> runningJobs = Collections.nCopies(10, new AsyncJob());
        when(asyncJobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));
        when(asyncJobRepository.findRunningJobs()).thenReturn(runningJobs);

        assertThatThrownBy(() -> asyncJobService.startJob(1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("إتمام مهمة بنجاح")
    void completeJob_shouldSetCompleted() {
        when(asyncJobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));
        when(asyncJobRepository.save(any(AsyncJob.class))).thenAnswer(inv -> inv.getArgument(0));

        AsyncJob result = asyncJobService.completeJob(1L, "Done successfully");

        assertThat(result.getStatus()).isEqualTo(AsyncJob.JobStatus.COMPLETED);
        assertThat(result.getResult()).isEqualTo("Done successfully");
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("فشل مهمة مع إعادة محاولة")
    void failJob_withRetries_shouldRequeue() {
        sampleJob.setRetryCount(0);
        sampleJob.setMaxRetries(3);
        when(asyncJobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));
        when(asyncJobRepository.save(any(AsyncJob.class))).thenAnswer(inv -> inv.getArgument(0));

        AsyncJob result = asyncJobService.failJob(1L, "Temporary error");

        assertThat(result.getStatus()).isEqualTo(AsyncJob.JobStatus.QUEUED);
        assertThat(result.getRetryCount()).isEqualTo(1);
        assertThat(result.getErrorMessage()).isEqualTo("Temporary error");
    }

    @Test
    @DisplayName("فشل مهمة نهائياً")
    void failJob_maxRetries_shouldFail() {
        sampleJob.setRetryCount(2);
        sampleJob.setMaxRetries(3);
        when(asyncJobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));
        when(asyncJobRepository.save(any(AsyncJob.class))).thenAnswer(inv -> inv.getArgument(0));

        AsyncJob result = asyncJobService.failJob(1L, "Permanent error");

        assertThat(result.getStatus()).isEqualTo(AsyncJob.JobStatus.FAILED);
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("إلغاء مهمة")
    void cancelJob_shouldCancel() {
        when(asyncJobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));
        when(asyncJobRepository.save(any(AsyncJob.class))).thenAnswer(inv -> inv.getArgument(0));

        AsyncJob result = asyncJobService.cancelJob(1L);

        assertThat(result.getStatus()).isEqualTo(AsyncJob.JobStatus.CANCELLED);
        assertThat(result.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("إلغاء مهمة مكتملة — رفض")
    void cancelJob_completed_shouldThrow() {
        sampleJob.setStatus(AsyncJob.JobStatus.COMPLETED);
        when(asyncJobRepository.findById(1L)).thenReturn(Optional.of(sampleJob));

        assertThatThrownBy(() -> asyncJobService.cancelJob(1L))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("إحصائيات المهام")
    void getStats_shouldReturnCounts() {
        when(asyncJobRepository.countByStatus(AsyncJob.JobStatus.QUEUED)).thenReturn(5L);
        when(asyncJobRepository.countByStatus(AsyncJob.JobStatus.RUNNING)).thenReturn(2L);
        when(asyncJobRepository.countByStatus(AsyncJob.JobStatus.COMPLETED)).thenReturn(10L);
        when(asyncJobRepository.countByStatus(AsyncJob.JobStatus.FAILED)).thenReturn(1L);
        when(asyncJobRepository.countByStatus(AsyncJob.JobStatus.CANCELLED)).thenReturn(0L);
        when(asyncJobRepository.count()).thenReturn(18L);

        Map<String, Object> stats = asyncJobService.getStats();

        assertThat(stats.get("queued")).isEqualTo(5L);
        assertThat(stats.get("running")).isEqualTo(2L);
        assertThat(stats.get("completed")).isEqualTo(10L);
        assertThat(stats.get("total")).isEqualTo(18L);
    }
}
