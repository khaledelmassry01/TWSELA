# Sprint 8 — Backend Quality & API Documentation
**المهام: 30 | حزم العمل: 4**

---

## WP-1: DTOs + @Operation Swagger

### T-8.01-T-8.08: @Operation annotations لـ 8 controllers بدون توثيق
- FinancialController, ManifestController, ReportsController, SettingsController
- AuditController, BackupController, HealthController, SmsController

### T-8.09-T-8.14: Response DTOs لاستبدال Map<String,Object>
- ShipmentResponseDTO, DashboardStatsDTO, ReportDTO
- UserResponseDTO, ManifestResponseDTO, FinancialResponseDTO

## WP-2: Stub implementations → Real implementations

### T-8.15: SettingsController — persistence في DB
- إنشاء SystemSettings entity + repository
- تعديل GET/POST endpoints لحفظ واسترجاع فعلي

### T-8.16: HealthController — externalize version
- من hardcoded version → Maven property أو env var

### T-8.17: BackupController — real implementation audit
- مراجعة وتحسين backup functionality

### T-8.18: Temporal type consistency
- توحيد Instant vs LocalDateTime عبر كل الـ entities

## WP-3: Code Quality

### T-8.19: Hardcoded values extraction
- PublicController office locations → DB or config
- DashboardController default values → config

### T-8.20: Service layer refactoring
- نقل business logic من Controllers إلى Services (ShipmentController 900+ lines)

### T-8.21: Exception messages i18n ready
- MessageSource integration للرسائل

### T-8.22-T-8.24: Repository optimizations
- Custom queries بدل derived queries المعقدة
- @Query annotations مع JPQL
- Specification pattern لـ dynamic queries

## WP-4: اختبارات Sprint 8

### T-8.25: SettingsController tests
### T-8.26: HealthController tests
### T-8.27: ReportsController tests
### T-8.28: DTO serialization tests
### T-8.29: AuditController tests
### T-8.30: BackupController tests

---

## معايير القبول
- [ ] كل controller endpoint لها @Operation annotation
- [ ] صفر Map<String,Object> في responses الرئيسية
- [ ] Settings تُحفظ في DB فعلياً
- [ ] BUILD SUCCESS + جميع الاختبارات تنجح
