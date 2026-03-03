package com.twsela.service;

import com.twsela.domain.ComplianceReport;
import com.twsela.domain.ComplianceRule;
import com.twsela.domain.User;
import com.twsela.repository.ComplianceReportRepository;
import com.twsela.repository.ComplianceRuleRepository;
import com.twsela.repository.UserRepository;
import com.twsela.web.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplianceServiceTest {

    @Mock
    private ComplianceRuleRepository complianceRuleRepository;

    @Mock
    private ComplianceReportRepository complianceReportRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ComplianceService complianceService;

    private User adminUser;
    private ComplianceRule sampleRule;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setName("مدير");
        adminUser.setPhone("01099999999");

        sampleRule = new ComplianceRule();
        sampleRule.setId(1L);
        sampleRule.setName("ENFORCE_HTTPS");
        sampleRule.setCategory(ComplianceRule.Category.ENCRYPTION);
        sampleRule.setCheckExpression("ENFORCE_HTTPS");
        sampleRule.setEnabled(true);
    }

    @Test
    @DisplayName("تشغيل فحص الامتثال — إنشاء تقرير")
    void runComplianceCheck_createsReport() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(complianceRuleRepository.findByEnabledTrue()).thenReturn(List.of(sampleRule));
        when(complianceRuleRepository.save(any(ComplianceRule.class))).thenAnswer(inv -> inv.getArgument(0));
        when(complianceReportRepository.save(any(ComplianceReport.class))).thenAnswer(inv -> {
            ComplianceReport r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        ComplianceReport result = complianceService.runComplianceCheck(1L);

        assertNotNull(result);
        assertEquals(1, result.getTotalRules());
        assertEquals(1, result.getPassedRules());
        assertEquals(0, result.getFailedRules());
        assertEquals(ComplianceReport.ReportStatus.FINAL, result.getStatus());
        verify(complianceReportRepository).save(any(ComplianceReport.class));
    }

    @Test
    @DisplayName("فحص الامتثال — مستخدم غير موجود")
    void runComplianceCheck_userNotFound_throwsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> complianceService.runComplianceCheck(999L));
    }

    @Test
    @DisplayName("الحصول على جميع القواعد")
    void getAllRules_returnsList() {
        when(complianceRuleRepository.findByEnabledTrue()).thenReturn(List.of(sampleRule));

        List<ComplianceRule> rules = complianceService.getAllRules();
        assertEquals(1, rules.size());
        assertEquals("ENFORCE_HTTPS", rules.get(0).getName());
    }

    @Test
    @DisplayName("حالة الامتثال — نسبة النجاح")
    void getComplianceStatus_returnsStatusMap() {
        sampleRule.setLastResult(ComplianceRule.CheckResult.PASS);
        when(complianceRuleRepository.findByEnabledTrue()).thenReturn(List.of(sampleRule));

        Map<String, Object> status = complianceService.getComplianceStatus();

        assertEquals(1, status.get("totalRules"));
        assertEquals(1L, status.get("passed"));
        assertEquals(0L, status.get("failed"));
        assertEquals(100L, status.get("complianceScore"));
    }

    @Test
    @DisplayName("تقرير حسب المعرف")
    void getReportById_found() {
        ComplianceReport report = new ComplianceReport();
        report.setId(5L);
        report.setTotalRules(10);
        when(complianceReportRepository.findById(5L)).thenReturn(Optional.of(report));

        ComplianceReport result = complianceService.getReportById(5L);
        assertEquals(5L, result.getId());
    }

    @Test
    @DisplayName("أحدث تقرير")
    void getLatestReport_returnsReport() {
        ComplianceReport report = new ComplianceReport();
        report.setId(10L);
        when(complianceReportRepository.findLatest()).thenReturn(Optional.of(report));

        ComplianceReport result = complianceService.getLatestReport();
        assertNotNull(result);
        assertEquals(10L, result.getId());
    }
}
