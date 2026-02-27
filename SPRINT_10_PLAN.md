# Sprint 10 — Tests, Upgrades & Infrastructure
**المهام: 30 | حزم العمل: 4**

---

## WP-1: توسيع اختبارات (9 services + 8 controllers مفقودة)

### T-10.01-T-10.05: Service tests
- AuditServiceTest
- SmsServiceTest
- ExcelServiceTest
- FileUploadServiceTest
- MetricsServiceTest

### T-10.06-T-10.10: Controller tests
- SmsControllerTest
- ManifestControllerTest
- ReportsControllerTest
- UserControllerTest
- SettingsControllerTest

## WP-2: ترقيات مؤجلة من Sprint 4

### T-10.11: jjwt upgrade (0.11.5 → 0.12.6)
- تحديث pom.xml
- تحديث JwtService لـ API الجديد
- اختبار كامل

### T-10.12: springdoc upgrade (2.2.0 → 2.7.0)
- تحديث pom.xml
- مراجعة SwaggerConfig
- اختبار Swagger UI

### T-10.13: Spring Boot dependency audit
- مراجعة وتحديث dependencies أخرى

## WP-3: بنية تحتية

### T-10.14: OTP storage → Redis
- نقل من ConcurrentHashMap إلى Redis
- TTL تلقائي بدل cleanup يدوي

### T-10.15: Background OTP cleanup task
- @Scheduled task لتنظيف OTPs المنتهية

### T-10.16: Health check improvements
- Actuator endpoints مع DB + Redis status

### T-10.17: Docker optimization
- Multi-stage build
- Layer caching
- Health check in Dockerfile

### T-10.18-T-10.20: Monitoring improvements
- Grafana dashboards update
- Prometheus metrics for new endpoints
- Alert rules

## WP-4: Documentation & CI Foundation

### T-10.21: API Documentation update
- تحديث API_DOCUMENTATION_AR.md مع كل endpoints الجديدة

### T-10.22: README update
- تحديث README مع الـ 10 sprints

### T-10.23: Flyway migration
- تنظيم schema migrations

### T-10.24-T-10.26: Database schema validation
- Validate all entity-to-table mappings
- Check all indexes
- Verify all foreign keys

### T-10.27-T-10.30: Final integration testing
- End-to-end flow tests
- Cross-browser verification
- Performance benchmarks
- Security scan

---

## معايير القبول
- [ ] Test coverage > 60% of services/controllers
- [ ] jjwt + springdoc upgraded successfully
- [ ] OTP يُخزن في Redis في production
- [ ] BUILD SUCCESS + كل الاختبارات تنجح
- [ ] توثيق API كامل ومحدث
