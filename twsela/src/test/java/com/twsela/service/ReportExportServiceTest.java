package com.twsela.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportExportServiceTest {

    @Mock private BIDashboardService biService;

    @InjectMocks
    private ReportExportService reportExportService;

    private Instant from;
    private Instant to;

    @BeforeEach
    void setUp() {
        from = Instant.now().minus(30, ChronoUnit.DAYS);
        to = Instant.now();
    }

    @Nested
    @DisplayName("exportToPdf — تصدير PDF")
    class ExportPdfTests {

        @Test
        @DisplayName("يجب تصدير التقرير بصيغة PDF")
        void exportToPdf_success() {
            when(biService.getExecutiveSummary(from, to)).thenReturn(java.util.Map.of("totalRevenue", "5000"));

            byte[] result = reportExportService.exportToPdf("summary", from, to, "ar");

            assertThat(result).isNotEmpty();
            String content = new String(result);
            assertThat(content).contains("%PDF-1.4");
            assertThat(content).contains("summary");
        }
    }

    @Nested
    @DisplayName("exportToExcel — تصدير Excel")
    class ExportExcelTests {

        @Test
        @DisplayName("يجب تصدير التقرير بصيغة Excel")
        void exportToExcel_success() {
            when(biService.getExecutiveSummary(from, to)).thenReturn(java.util.Map.of("totalRevenue", "5000"));

            byte[] result = reportExportService.exportToExcel("summary", from, to);

            assertThat(result).isNotEmpty();
            String content = new String(result);
            assertThat(content).contains("Metric,Value");
        }
    }

    @Nested
    @DisplayName("exportToCsv — تصدير CSV")
    class ExportCsvTests {

        @Test
        @DisplayName("يجب تصدير التقرير بصيغة CSV")
        void exportToCsv_success() {
            when(biService.getExecutiveSummary(from, to)).thenReturn(java.util.Map.of("totalRevenue", "5000"));

            byte[] result = reportExportService.exportToCsv("summary", from, to);

            assertThat(result).isNotEmpty();
            String content = new String(result);
            assertThat(content).contains("metric,value");
            assertThat(content).contains("totalRevenue");
        }
    }

    @Nested
    @DisplayName("export with different report types — أنواع التقارير")
    class ReportTypeTests {

        @Test
        @DisplayName("يجب دعم تقرير الإيرادات")
        void exportRevenue_usesRevenueService() {
            when(biService.getRevenueAnalytics(from, to, 10)).thenReturn(java.util.Map.of("revenue", "1000"));

            byte[] result = reportExportService.exportToCsv("revenue", from, to);

            assertThat(result).isNotEmpty();
            verify(biService).getRevenueAnalytics(from, to, 10);
        }
    }
}
