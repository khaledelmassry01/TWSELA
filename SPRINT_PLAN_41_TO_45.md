# TWSELA Sprint Plan: Sprints 41–45

## ملخص عام

| Sprint | الموضوع | الكيانات الجديدة | Controllers | Tests (تقديري) |
|--------|---------|-----------------|-------------|----------------|
| 41 | Multi-Carrier & 3PL Integration | 8 | 4 | ~40 |
| 42 | Document Management & E-Signatures | 7 | 3 | ~35 |
| 43 | Mobile Offline Sync & Courier App | 7 | 4 | ~35 |
| 44 | Advanced Reporting & Data Pipeline | 8 | 4 | ~40 |
| 45 | Platform Ops, Archival & Auto-Scaling | 7 | 3 | ~35 |

**الهدف بعد Sprint 45:**
- ~178 entity
- ~178 service
- ~106 controller
- ~1,325 test
- Flyway V1–V55

---

## Sprint 41: Multi-Carrier & 3PL Integration

### الهدف
ربط المنصة مع شركات شحن خارجية (Aramex, DHL, FedEx, etc.) وإدارة شركاء توصيل الميل الأخير (3PL). تمكين التاجر والمالك من اختيار أفضل ناقل لكل شحنة بناءً على السعر والوقت والمنطقة.

### الكيانات الجديدة (8)

| Entity | الوصف |
|--------|-------|
| `Carrier` | شركة شحن خارجية (name, code, type, apiEndpoint, apiKey, status, supportedCountries) |
| `CarrierZoneMapping` | ربط مناطق المنصة بمناطق الناقل (carrier, zone, carrierZoneCode, deliveryDays) |
| `CarrierRate` | أسعار الناقل حسب المنطقة والوزن (carrier, zoneMapping, minWeight, maxWeight, basePrice, perKgPrice) |
| `CarrierShipment` | شحنة تم إنشاؤها عند ناقل خارجي (shipment, carrier, externalTrackingNumber, externalStatus, labelUrl) |
| `CarrierWebhookLog` | سجل webhooks من الناقلين (carrier, eventType, payload, processed, error) |
| `ThirdPartyPartner` | شريك 3PL (name, contactPhone, serviceArea, commissionRate, status) |
| `PartnerHandoff` | تسليم الشحنة لشريك 3PL (shipment, partner, handoffDate, status, partnerTrackingNumber) |
| `CarrierSelectionRule` | قاعدة اختيار الناقل الأمثل (priority, zoneId, weightRange, preferredCarrier, fallbackCarrier, criteria) |

### الخدمات (8)
- `CarrierService` — CRUD + integration status management
- `CarrierZoneMappingService` — Zone mapping logic
- `CarrierRateService` — Rate calculation + comparison
- `CarrierShipmentService` — Create/track/cancel external shipments
- `CarrierWebhookService` — Process inbound carrier webhooks
- `ThirdPartyPartnerService` — 3PL partner management
- `PartnerHandoffService` — Handoff lifecycle management
- `CarrierSelectionService` — Rule-based optimal carrier selection

### المتحكمات (4)

**CarrierController** (`/api/admin/carriers`)
- `POST /api/admin/carriers` — إضافة ناقل
- `GET /api/admin/carriers` — قائمة الناقلين
- `GET /api/admin/carriers/{id}` — تفاصيل
- `PUT /api/admin/carriers/{id}` — تحديث
- `PATCH /api/admin/carriers/{id}/toggle` — تفعيل/تعطيل
- `GET /api/admin/carriers/{id}/zones` — مناطق الناقل
- `POST /api/admin/carriers/{id}/zones` — ربط منطقة
- `GET /api/admin/carriers/{id}/rates` — أسعار الناقل
- `POST /api/admin/carriers/{id}/rates` — إضافة سعر
- `PUT /api/admin/carriers/rates/{rateId}` — تحديث سعر

**CarrierShipmentController** (`/api/carrier-shipments`)
- `POST /api/carrier-shipments` — إنشاء شحنة خارجية
- `GET /api/carrier-shipments/{id}` — تفاصيل
- `GET /api/carrier-shipments/{id}/track` — تتبع من الناقل
- `POST /api/carrier-shipments/{id}/cancel` — إلغاء
- `GET /api/carrier-shipments/{id}/label` — تحميل بوليصة الشحن
- `POST /api/carrier-shipments/compare?shipmentId=...` — مقارنة أسعار الناقلين
- `GET /api/carrier-shipments/selection-rules` — قواعد الاختيار
- `PUT /api/carrier-shipments/selection-rules` — تحديث قاعدة

**ThirdPartyController** (`/api/admin/3pl`)
- `POST /api/admin/3pl/partners` — إضافة شريك
- `GET /api/admin/3pl/partners` — قائمة الشركاء
- `GET /api/admin/3pl/partners/{id}` — تفاصيل
- `PUT /api/admin/3pl/partners/{id}` — تحديث
- `POST /api/admin/3pl/handoff` — تسليم شحنة لشريك
- `GET /api/admin/3pl/handoffs?status=...` — قائمة التسليمات
- `PUT /api/admin/3pl/handoffs/{id}/complete` — تأكيد إتمام

**CarrierWebhookController** (`/api/carriers/webhook`)
- `POST /api/carriers/webhook/{carrierCode}` — استقبال webhook ناقل
- `GET /api/admin/carriers/webhook-logs?carrierId=...` — سجل الأحداث

### Flyway Migrations
- **V46** — Create `carrier`, `carrier_zone_mapping`, `carrier_rate`, `carrier_shipment` tables
- **V47** — Create `carrier_webhook_log`, `third_party_partner`, `partner_handoff`, `carrier_selection_rule` tables

### الاختبارات (~40 tests)
- CarrierControllerTest (10)
- CarrierShipmentControllerTest (10)
- ThirdPartyControllerTest (10)
- CarrierWebhookControllerTest (5)
- CarrierSelectionServiceTest (5)

---

## Sprint 42: Document Management & E-Signatures

### الهدف
نظام إدارة مستندات شامل يشمل توليد بوالص الشحن (AWB)، المستندات الجمركية، العقود الإلكترونية، والتوقيع الإلكتروني. يدعم قوالب متعددة قابلة للتخصيص حسب المستأجر.

### الكيانات الجديدة (7)

| Entity | الوصف |
|--------|-------|
| `DocumentTemplate` | قالب مستند (name, type, format, templateContent, version, tenantId, isDefault) |
| `GeneratedDocument` | مستند مُنشأ (template, shipment, documentType, fileUrl, fileSize, generatedAt, expiresAt) |
| `DocumentBatch` | دفعة مستندات (batchType, status, totalDocuments, completedDocuments, requestedBy, startedAt) |
| `SignatureRequest` | طلب توقيع إلكتروني (document, signerName, signerPhone, status, token, expiresAt) |
| `DigitalSignature` | التوقيع الفعلي (signatureRequest, signatureImageUrl, signedAt, ipAddress, deviceInfo) |
| `CustomsDocument` | مستند جمركي (shipment, documentType, hsCode, declaredValue, currency, originCountry, destinationCountry) |
| `DocumentAuditLog` | سجل تدقيق المستندات (document, action, performedBy, details, timestamp) |

### الخدمات (7)
- `DocumentTemplateService` — Template CRUD + versioning
- `DocumentGenerationService` — PDF/HTML generation from templates
- `DocumentBatchService` — Bulk document generation management
- `SignatureService` — E-signature request lifecycle
- `DigitalSignatureService` — Signature capture + verification
- `CustomsDocumentService` — Customs paperwork generation
- `DocumentAuditService` — Document trail tracking

### المتحكمات (3)

**DocumentController** (`/api/documents`)
- `POST /api/documents/generate` — توليد مستند (AWB/Invoice/Receipt)
- `GET /api/documents/{id}` — تفاصيل + تحميل
- `GET /api/documents/shipment/{shipmentId}` — مستندات شحنة
- `POST /api/documents/batch` — توليد دفعة
- `GET /api/documents/batch/{batchId}` — حالة الدفعة
- `GET /api/documents/batch/{batchId}/download` — تحميل الدفعة (ZIP)

**DocumentTemplateController** (`/api/admin/document-templates`)
- `POST /api/admin/document-templates` — إنشاء قالب
- `GET /api/admin/document-templates` — قائمة القوالب
- `GET /api/admin/document-templates/{id}` — تفاصيل
- `PUT /api/admin/document-templates/{id}` — تحديث
- `POST /api/admin/document-templates/{id}/preview` — معاينة
- `DELETE /api/admin/document-templates/{id}` — حذف

**SignatureController** (`/api/signatures`)
- `POST /api/signatures/request` — إرسال طلب توقيع
- `GET /api/signatures/verify/{token}` — التحقق من صلاحية التوقيع
- `POST /api/signatures/sign/{token}` — تنفيذ التوقيع (multipart)
- `GET /api/signatures/{id}` — تفاصيل التوقيع
- `GET /api/signatures/document/{documentId}` — توقيعات مستند

**CustomsController** (`/api/customs`) [ضمن DocumentController]
- `POST /api/customs/generate/{shipmentId}` — توليد مستند جمركي
- `GET /api/customs/shipment/{shipmentId}` — مستندات جمركية

### Flyway Migrations
- **V48** — Create `document_template`, `generated_document`, `document_batch` tables
- **V49** — Create `signature_request`, `digital_signature`, `customs_document`, `document_audit_log` tables

### الاختبارات (~35 tests)
- DocumentControllerTest (10)
- DocumentTemplateControllerTest (8)
- SignatureControllerTest (8)
- DocumentGenerationServiceTest (5)
- SignatureServiceTest (4)

---

## Sprint 43: Mobile Offline Sync & Courier App Optimization

### الهدف
بنية تحتية كاملة لدعم العمل بدون اتصال (offline-first) لتطبيق المندوب. تتضمن قائمة انتظار للعمليات، محرك مزامنة، تحسين استهلاك البطارية والبيانات، وAPIs مخصصة للهاتف.

### الكيانات الجديدة (7)

| Entity | الوصف |
|--------|-------|
| `OfflineQueue` | عملية مؤجلة بانتظار المزامنة (userId, operationType, payload, priority, status, createdOfflineAt, syncedAt) |
| `SyncSession` | جلسة مزامنة (userId, deviceId, startedAt, completedAt, itemsSynced, itemsFailed, status) |
| `SyncConflict` | تعارض بيانات (syncSession, entityType, entityId, localData, serverData, resolution, resolvedAt) |
| `DeviceRegistration` | تسجيل جهاز المندوب (userId, deviceId, platform, osVersion, appVersion, pushToken, lastActiveAt) |
| `BatteryOptimizationConfig` | إعدادات توفير البطارية (name, batteryThreshold, locationInterval, pingInterval, syncInterval) |
| `DataUsageLog` | سجل استهلاك البيانات (userId, deviceId, endpoint, bytesUp, bytesDown, timestamp) |
| `AppVersionConfig` | إعدادات إصدار التطبيق (platform, minVersion, currentVersion, updateUrl, forceUpdate, releaseNotes) |

### الخدمات (7)
- `OfflineQueueService` — Queue management + conflict detection
- `SyncService` — Bidirectional sync engine
- `SyncConflictService` — Conflict resolution (auto + manual)
- `DeviceRegistrationService` — Device management + push token refresh
- `BatteryOptimizationService` — Dynamic config based on battery level
- `DataUsageService` — Usage tracking + compression optimization
- `AppVersionService` — Version management + forced update logic

### المتحكمات (4)

**SyncController** (`/api/mobile/sync`)
- `POST /api/mobile/sync/push` — رفع العمليات المؤجلة للخادم
- `POST /api/mobile/sync/pull?since=...` — جلب التحديثات من الخادم
- `GET /api/mobile/sync/status` — حالة المزامنة
- `GET /api/mobile/sync/conflicts` — التعارضات المفتوحة
- `POST /api/mobile/sync/conflicts/{id}/resolve` — حل تعارض
- `POST /api/mobile/sync/full` — مزامنة كاملة (reset)

**DeviceController** (`/api/mobile/devices`)
- `POST /api/mobile/devices/register` — تسجيل جهاز
- `PUT /api/mobile/devices/{deviceId}/token` — تحديث push token
- `GET /api/mobile/devices/my` — أجهزتي
- `DELETE /api/mobile/devices/{deviceId}` — إلغاء تسجيل

**MobileConfigController** (`/api/mobile/config`)
- `GET /api/mobile/config/battery?level=...` — إعدادات توفير البطارية المناسبة
- `GET /api/mobile/config/app-version?platform=...` — معلومات الإصدار
- `GET /api/mobile/config/sync-settings` — إعدادات المزامنة

**MobileOptimizedController** (`/api/mobile/compact`)
- `GET /api/mobile/compact/dashboard` — لوحة مبسّطة (بيانات مضغوطة)
- `GET /api/mobile/compact/shipments?since=...` — شحنات مضغوطة (delta)
- `POST /api/mobile/compact/batch-update` — تحديث شحنات متعددة (single request)
- `GET /api/mobile/compact/manifest/{manifestId}` — بيان مضغوط
- `POST /api/mobile/compact/location-batch` — رفع مواقع GPS مجمّعة

### إعدادات إدارية
- `GET /api/admin/mobile/battery-configs` — قائمة إعدادات البطارية
- `POST /api/admin/mobile/battery-configs` — إضافة إعداد
- `GET /api/admin/mobile/data-usage?userId=...` — استهلاك البيانات
- `POST /api/admin/mobile/app-versions` — إضافة إصدار

### Flyway Migrations
- **V50** — Create `offline_queue`, `sync_session`, `sync_conflict` tables
- **V51** — Create `device_registration`, `battery_optimization_config`, `data_usage_log`, `app_version_config` tables

### الاختبارات (~35 tests)
- SyncControllerTest (10)
- DeviceControllerTest (6)
- MobileConfigControllerTest (5)
- MobileOptimizedControllerTest (8)
- SyncServiceTest (6)

---

## Sprint 44: Advanced Reporting & Data Pipeline

### الهدف
منصة تقارير متقدمة تسمح بإنشاء تقارير مخصصة، جدولة التقارير، تصدير البيانات بصيغ متعددة، وبناء ETL pipeline للتحليلات المتقدمة. يتضمن report builder مرئي.

### الكيانات الجديدة (8)

| Entity | الوصف |
|--------|-------|
| `CustomReport` | تقرير مخصص (name, description, reportType, queryConfig, columns, filters, createdBy, isPublic) |
| `ReportSchedule` | جدولة التقرير (customReport, cronExpression, format, recipients, enabled, lastRunAt, nextRunAt) |
| `ReportExecution` | تنفيذ تقرير (customReport, schedule, status, startedAt, completedAt, fileUrl, fileSize, rowCount) |
| `DataExportJob` | مهمة تصدير بيانات (entityType, filters, format, status, requestedBy, fileUrl, expiresAt) |
| `DataPipelineConfig` | إعداد ETL (name, sourceType, sourceConfig, transformRules, destinationType, destConfig, schedule) |
| `PipelineExecution` | تنفيذ pipeline (pipelineConfig, status, recordsProcessed, recordsFailed, startedAt, completedAt) |
| `SavedFilter` | فلتر محفوظ (name, entityType, filterConfig, userId, isDefault) |
| `ReportWidget` | عنصر لوحة مخصصة (name, reportType, chartType, queryConfig, displayOrder, dashboardId) |

### الخدمات (8)
- `CustomReportService` — Report CRUD + dynamic query building
- `ReportScheduleService` — Scheduling + cron management
- `ReportExecutionService` — Async report generation
- `DataExportService` — Export to CSV/Excel/PDF/JSON
- `DataPipelineService` — ETL pipeline management
- `PipelineExecutionService` — Pipeline run + monitoring
- `SavedFilterService` — User filter management
- `ReportWidgetService` — Dashboard widget management

### المتحكمات (4)

**CustomReportController** (`/api/custom-reports`)
- `POST /api/custom-reports` — إنشاء تقرير مخصص
- `GET /api/custom-reports` — قائمة تقاريري
- `GET /api/custom-reports/{id}` — تفاصيل
- `PUT /api/custom-reports/{id}` — تحديث
- `DELETE /api/custom-reports/{id}` — حذف
- `POST /api/custom-reports/{id}/execute` — تنفيذ التقرير
- `GET /api/custom-reports/{id}/executions` — تنفيذات سابقة
- `GET /api/custom-reports/{id}/preview` — معاينة (أول 50 سجل)
- `POST /api/custom-reports/{id}/schedule` — جدولة
- `GET /api/custom-reports/public` — التقارير العامة

**DataExportController** (`/api/data-export`)
- `POST /api/data-export` — طلب تصدير
- `GET /api/data-export/{id}` — حالة + تحميل
- `GET /api/data-export/my` — طلبات التصدير
- `GET /api/data-export/formats` — الصيغ المتاحة

**DataPipelineController** (`/api/admin/pipelines`)
- `POST /api/admin/pipelines` — إنشاء pipeline
- `GET /api/admin/pipelines` — قائمة pipelines
- `GET /api/admin/pipelines/{id}` — تفاصيل
- `PUT /api/admin/pipelines/{id}` — تحديث
- `POST /api/admin/pipelines/{id}/run` — تشغيل يدوي
- `GET /api/admin/pipelines/{id}/executions` — تنفيذات
- `POST /api/admin/pipelines/{id}/validate` — التحقق من الإعداد

**ReportWidgetController** (`/api/report-widgets`)
- `POST /api/report-widgets` — إنشاء widget
- `GET /api/report-widgets/dashboard` — widgets لوحتي
- `PUT /api/report-widgets/{id}` — تحديث
- `DELETE /api/report-widgets/{id}` — حذف
- `PUT /api/report-widgets/reorder` — إعادة ترتيب
- `GET /api/report-widgets/{id}/data` — بيانات Widget

### إعدادات إضافية
- `POST /api/saved-filters` — حفظ فلتر
- `GET /api/saved-filters?entityType=...` — قائمة الفلاتر
- `DELETE /api/saved-filters/{id}` — حذف فلتر

### Flyway Migrations
- **V52** — Create `custom_report`, `report_schedule`, `report_execution`, `saved_filter` tables
- **V53** — Create `data_export_job`, `data_pipeline_config`, `pipeline_execution`, `report_widget` tables

### الاختبارات (~40 tests)
- CustomReportControllerTest (12)
- DataExportControllerTest (6)
- DataPipelineControllerTest (10)
- ReportWidgetControllerTest (7)
- CustomReportServiceTest (5)

---

## Sprint 45: Platform Ops, Archival & Auto-Scaling

### الهدف
بنية تحتية لعمليات المنصة: إدارة صحة النظام، أرشفة البيانات القديمة، تنظيف تلقائي، إعدادات قابلة للتوسيع، ومراقبة الأداء التفصيلية. يُعتبر الأساس لاستقرار المنصة على المدى الطويل.

### الكيانات الجديدة (7)

| Entity | الوصف |
|--------|-------|
| `SystemHealthCheck` | فحص صحة النظام (component, status, responseTimeMs, details, checkedAt) |
| `ArchivePolicy` | سياسة أرشفة (entityType, retentionDays, archiveStrategy, compressionEnabled, lastRunAt) |
| `ArchivedRecord` | سجل مؤرشف (originalTable, originalId, archivedData, archivedAt, archivePolicy, expiresAt) |
| `CleanupTask` | مهمة تنظيف (name, targetTable, condition, dryRun, deletedCount, lastRunAt, schedule) |
| `PlatformMetric` | مقياس أداء المنصة (metricName, metricValue, metricType, labels, recordedAt) |
| `SystemAlert` | تنبيه نظام (alertType, severity, message, component, acknowledged, acknowledgedBy, resolvedAt) |
| `MaintenanceWindow` | نافذة صيانة (title, description, startAt, endAt, affectedComponents, status, createdBy) |

### الخدمات (7)
- `SystemHealthService` — Health check orchestration
- `ArchiveService` — Data archival + retrieval
- `ArchivedRecordService` — Archived data query + restore
- `CleanupService` — Scheduled cleanup tasks
- `PlatformMetricService` — Custom metrics collection + exposure to Prometheus
- `SystemAlertService` — Alert creation + routing + acknowledgment
- `MaintenanceWindowService` — Maintenance scheduling + notification

### المتحكمات (3)

**PlatformOpsController** (`/api/admin/platform`)
- `GET /api/admin/platform/health` — فحص شامل لصحة النظام
- `GET /api/admin/platform/health/{component}` — صحة مكوّن محدد
- `GET /api/admin/platform/health/history?hours=24` — سجل الصحة
- `GET /api/admin/platform/metrics?from=...&to=...` — مقاييس الأداء
- `GET /api/admin/platform/metrics/summary` — ملخص الأداء
- `GET /api/admin/platform/disk-usage` — استخدام القرص
- `GET /api/admin/platform/connections` — حالة الاتصالات (DB, Redis, etc.)

**ArchiveController** (`/api/admin/archive`)
- `GET /api/admin/archive/policies` — سياسات الأرشفة
- `POST /api/admin/archive/policies` — إنشاء سياسة
- `PUT /api/admin/archive/policies/{id}` — تحديث
- `POST /api/admin/archive/policies/{id}/run` — تشغيل يدوي
- `GET /api/admin/archive/records?originalTable=...&page=0&size=20` — البيانات المؤرشفة
- `POST /api/admin/archive/records/{id}/restore` — استعادة سجل
- `GET /api/admin/archive/stats` — إحصائيات الأرشفة
- `GET /api/admin/cleanup/tasks` — مهام التنظيف
- `POST /api/admin/cleanup/tasks` — إنشاء مهمة تنظيف
- `POST /api/admin/cleanup/tasks/{id}/run?dryRun=true` — تنفيذ (أو محاكاة)

**AlertController** (`/api/admin/alerts`)
- `GET /api/admin/alerts?severity=...&acknowledged=false` — التنبيهات
- `GET /api/admin/alerts/{id}` — تفاصيل تنبيه
- `POST /api/admin/alerts/{id}/acknowledge` — تأكيد الاطلاع
- `POST /api/admin/alerts/{id}/resolve` — حل المشكلة
- `GET /api/admin/alerts/summary` — ملخص التنبيهات
- `POST /api/admin/maintenance` — إنشاء نافذة صيانة
- `GET /api/admin/maintenance?status=SCHEDULED` — نوافذ الصيانة
- `PUT /api/admin/maintenance/{id}` — تحديث
- `POST /api/admin/maintenance/{id}/start` — بدء الصيانة
- `POST /api/admin/maintenance/{id}/complete` — إنهاء

### Flyway Migrations
- **V54** — Create `system_health_check`, `archive_policy`, `archived_record`, `cleanup_task` tables
- **V55** — Create `platform_metric`, `system_alert`, `maintenance_window` tables

### الاختبارات (~35 tests)
- PlatformOpsControllerTest (10)
- ArchiveControllerTest (12)
- AlertControllerTest (8)
- ArchiveServiceTest (5)

---

## ملخص تراكمي

### بعد Sprint 45

| المقياس | القيمة |
|---------|--------|
| إجمالي الكيانات | ~178 |
| إجمالي الخدمات | ~178 |
| إجمالي المتحكمات | ~106 |
| إجمالي Repositories | ~140 |
| إجمالي DTOs | ~80+ |
| إجمالي الاختبارات | ~1,325 |
| Flyway Migrations | V1–V55 |
| REST Endpoints | ~550+ |
| WebSocket Endpoints | 4 |

### خريطة الاعتمادات

```
Sprint 41 (Multi-Carrier)    ← يعتمد على: Zone, Shipment, Webhook
Sprint 42 (Documents)        ← يعتمد على: Shipment, Contract, Tenant
Sprint 43 (Mobile/Offline)   ← يعتمد على: Shipment, Manifest, Tracking
Sprint 44 (Reporting)        ← يعتمد على: Analytics, BI Dashboard, Report Export
Sprint 45 (Platform Ops)     ← مستقل تقريبًا (infrastructure)
```

### أولويات التنفيذ

1. **Sprint 41** — Multi-Carrier ضروري لتوسيع التغطية الجغرافية
2. **Sprint 42** — Documents مطلوب للامتثال والعمليات الجمركية
3. **Sprint 43** — Mobile Offline حاسم لتجربة المندوب في المناطق ضعيفة الاتصال
4. **Sprint 44** — Reporting يرفع القيمة للمالك والتاجر
5. **Sprint 45** — Platform Ops يضمن استقرار المنصة على المدى الطويل

### ملاحظات فنية مهمة

1. **لا Lombok** — جميع الكيانات تتطلب getters/setters يدوية
2. **لا BaseEntity** — كل entity يعلن id, createdAt, updatedAt بنفسه
3. **Role و UserStatus كيانات** وليست enums
4. **User.tenantId** من نوع `Long` (وليس `@ManyToOne Tenant`)
5. **ApiResponse<T>** wrapper لجميع الاستجابات
6. **test pattern**: `@WebMvcTest` + `TestMethodSecurityConfig`
7. **packages**: controllers في `com.twsela.web`, services في `com.twsela.service`
