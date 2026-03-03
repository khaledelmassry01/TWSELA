package com.twsela.service;

import com.twsela.domain.ScheduledTask;
import com.twsela.domain.Tenant;
import com.twsela.repository.ScheduledTaskRepository;
import com.twsela.web.exception.BusinessRuleException;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledTaskServiceTest {

    @Mock
    private ScheduledTaskRepository repository;

    @InjectMocks
    private ScheduledTaskService service;

    private ScheduledTask task;
    private Tenant tenant;

    @BeforeEach
    void setUp() {
        tenant = new Tenant();
        tenant.setId(1L);

        task = new ScheduledTask();
        task.setId(1L);
        task.setName("تقرير يومي");
        task.setTaskType(ScheduledTask.TaskType.GENERATE_REPORT);
        task.setCronExpression("0 0 6 * * ?");
        task.setTenant(tenant);
    }

    @Test
    @DisplayName("إنشاء مهمة مجدولة بنجاح")
    void create_success() {
        when(repository.existsByNameAndTenantId("تقرير يومي", 1L)).thenReturn(false);
        when(repository.save(any())).thenReturn(task);
        ScheduledTask result = service.create(task);
        assertThat(result.getName()).isEqualTo("تقرير يومي");
    }

    @Test
    @DisplayName("رفض إنشاء مهمة مكررة")
    void create_duplicate_throws() {
        when(repository.existsByNameAndTenantId("تقرير يومي", 1L)).thenReturn(true);
        assertThatThrownBy(() -> service.create(task))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    @DisplayName("تفعيل مهمة")
    void activate() {
        task.setActive(false);
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(repository.save(any())).thenReturn(task);
        service.activate(1L);
        assertThat(task.isActive()).isTrue();
    }

    @Test
    @DisplayName("تسجيل تشغيل مهمة")
    void recordRun() {
        when(repository.findById(1L)).thenReturn(Optional.of(task));
        when(repository.save(any())).thenReturn(task);
        service.recordRun(1L, ScheduledTask.TaskStatus.SUCCESS, 1500L);
        assertThat(task.getLastRunStatus()).isEqualTo(ScheduledTask.TaskStatus.SUCCESS);
        assertThat(task.getLastRunDurationMs()).isEqualTo(1500L);
        assertThat(task.getLastRunAt()).isNotNull();
    }

    @Test
    @DisplayName("بحث بمعرّف غير موجود")
    void findById_notFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("جلب مهام المستأجر")
    void findByTenantId() {
        when(repository.findByTenantId(1L)).thenReturn(List.of(task));
        assertThat(service.findByTenantId(1L)).hasSize(1);
    }
}
