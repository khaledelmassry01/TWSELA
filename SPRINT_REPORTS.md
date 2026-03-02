# تقارير السبرنتات — نظام Twsela

> **تاريخ الإعداد:** 2 مارس 2026  
> **نطاق التقرير:** Sprint 1 → Sprint 25 (20 مكتمل + 5 مخطط)  
> **الحالة الحالية:** 330 اختبار ناجح ✅ | BUILD SUCCESS

---

## الفهرس

| القسم | السبرنتات | الحالة |
|-------|----------|--------|
| [المرحلة 0 — إصلاح الأساسات](#المرحلة-0--إصلاح-الأساسات-sprints-1-6) | 1–6 | ✅ مكتمل |
| [المرحلة 0.5 — بناء الـ API وتجربة المستخدم](#المرحلة-05--بناء-الـ-api-وتجربة-المستخدم-sprints-7-11) | 7–11 | ✅ مكتمل |
| [المرحلة 1أ — توحيد ونضج](#المرحلة-1أ--توحيد-ونضج-sprints-12-15) | 12–15 | ✅ مكتمل |
| [المرحلة 1ب — ميزات تجارية أساسية](#المرحلة-1ب--ميزات-تجارية-أساسية-sprints-16-20) | 16–20 | ✅ مكتمل |
| [المرحلة 2 — التوسع التجاري والذكاء](#المرحلة-2--التوسع-التجاري-والذكاء-sprints-21-25) | 21–25 | 📋 مخطط |

---

## ملخص شامل — كل السبرنتات

| Sprint | العنوان | اختبارات جديدة | إجمالي الاختبارات | الحالة |
|--------|---------|----------------|-------------------|--------|
| **1** | إصلاحات أمنية حرجة | 0 | 0 | ✅ |
| **2** | مزامنة حالات الشحنات والأداء | 0 | 0 | ✅ |
| **3** | أمان JWT والكاش وأساس الاختبارات | 14 | 14 | ✅ |
| **4** | توسيع الاختبارات وجودة الكود | 75 | 89 | ✅ |
| **5** | أمان البنية التحتية وتوثيق API | 0 | 89 | ✅ |
| **6** | تقوية الأمان وإصلاح نموذج البيانات | 0 | 89 | ✅ |
| **7** | نقاط API جديدة: المصادقة، المستخدمين، الإشعارات | 11 | 100 | ✅ |
| **8** | Swagger Operations وإعدادات النظام | 11 | 111 | ✅ |
| **9** | إعادة بناء الواجهة الأمامية | 0 | 111 | ✅ |
| **10** | توسيع الاختبارات وترقية المكتبات | 12 | 123 | ✅ |
| **11** | تنظيف الواجهة وإصلاح XSS | 0 | 123 | ✅ |
| **12** | توحيد استجابة API والـ DTOs | 32 | 155 | ✅ |
| **13** | WebSocket والاتصال الحي والإشعارات | 39 | 194 | ✅ |
| **14** | الرفع المجمع والباركود والتقييمات | 38 | 232 | ✅ |
| **15** | DevOps والجاهزية للإنتاج | 13 | 245 | ✅ |
| **16** | تتبع الموقع الحي والتتبع العام | 22 | 267 | ✅ |
| **17** | نظام إدارة المرتجعات | 18 | 285 | ✅ |
| **18** | المحافظ والتسوية المالية | 19 | 304 | ✅ |
| **19** | نظام Webhooks والأحداث | 16 | 320 | ✅ |
| **20** | التقارير المتقدمة والواجهة الأمامية | 10 | 330 | ✅ |
| **21** | خطط الاشتراك والفواتير وبوابة الدفع | ~35 | ~365 | 📋 |
| **22** | إدارة الأسطول وتتبع المركبات | ~32 | ~397 | 📋 |
| **23** | نظام التذاكر والدعم الفني و SLA | ~35 | ~432 | 📋 |
| **24** | محرك التوزيع الذكي وتحسين المسارات | ~30 | ~462 | 📋 |
| **25** | دعم متعدد الدول والعملات والضرائب | ~32 | ~494 | 📋 |

---

# المرحلة 0 — إصلاح الأساسات (Sprints 1-6)

> **الهدف**: إصلاح المشاكل الأمنية الحرجة وتحسين جودة الكود الموجود  
> **المدة الفعلية**: 6 أسابيع  
> **النتيجة**: نظام آمن ومستقر جاهز للبناء عليه

---

## Sprint 1 — إصلاحات أمنية حرجة

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 1 |
| **العنوان** | Critical Security Fixes & Broken Functions |
| **المدة** | أسبوع واحد |
| **الأولوية** | P0 — طوارئ |
| **اختبارات جديدة** | 0 |
| **إجمالي الاختبارات** | 0 |

### الأهداف
- سد الثغرات الأمنية الحرجة في النظام
- إصلاح الوظائف المعطلة
- منع تسريب البيانات الحساسة

### ما تم إنجازه

#### الأمان (Backend)
| # | الإصلاح | الملف | الخطورة |
|---|---------|-------|---------|
| 1 | حذف `DebugController` الذي يسرب بيانات حساسة | `web/DebugController.java` | 🔴 حرج |
| 2 | إغلاق `SecurityConfig` (كان `.permitAll()` لكل الطلبات) | `security/SecurityConfig.java` | 🔴 حرج |
| 3 | إضافة BCrypt لتشفير كلمات المرور | `service/UserService.java` | 🔴 حرج |
| 4 | استبدال `Random` بـ `SecureRandom` لتوليد OTP | `service/OtpService.java` | 🟡 عالي |
| 5 | نقل بيانات قاعدة البيانات إلى متغيرات بيئة | `application.yml` | 🟡 عالي |
| 6 | إصلاح hardcoded `courierId` في كود الإسناد | `service/ShipmentService.java` | 🟡 عالي |
| 7 | إصلاح Path Traversal في رفع الملفات | `service/FileUploadService.java` | 🔴 حرج |
| 8 | تأمين كلمة مرور النسخ الاحتياطي | `service/BackupService.java` | 🟡 عالي |
| 9 | تفعيل Soft Delete للمستخدمين | `domain/User.java` | 🟢 متوسط |
| 10 | قفل Actuator endpoints | `SecurityConfig.java` | 🟡 عالي |

#### الأمان (Frontend)
| # | الإصلاح | الملفات |
|---|---------|--------|
| 1 | إصلاح تجاوز المصادقة (`return true` عند الخطأ) | `auth_service.js` |
| 2 | إضافة `escapeHtml()` لمنع XSS (~30 نقطة حقن) | 7 ملفات JS |
| 3 | إصلاح خلط GET/POST في طلبات API | `api_service.js` |

#### الوظائف المعطلة
| # | الإصلاح | الوصف |
|---|---------|-------|
| 1 | `/api/shipments/list` | كان يعيد قائمة فارغة دائماً |
| 2 | `assignShipmentsToManifest()` | لم يكن يعمل — إصلاح الربط |
| 3 | `updateCourierLocation()` | خطأ في حفظ الإحداثيات |
| 4 | `createReturnShipment()` | خطأ في إنشاء سجل المرتجع |

#### التنظيف
- حذف 55 `catch` block فارغ في 12 ملف
- حذف ملفات يتيمة وغير مستخدمة
- إنشاء `.dockerignore`
- إنشاء `application-prod.yml` للإنتاج

### الملفات المتأثرة
- **حُذفت**: `DebugController.java`، ملفات يتيمة
- **عُدّلت**: `SecurityConfig.java`، `UserService.java`، `OtpService.java`، `ShipmentService.java`، `FileUploadService.java`، `BackupService.java`، `application.yml`، 7 ملفات JS

### المخاطر والدروس المستفادة
- النظام كان **غير آمن تماماً** — كل endpoint مفتوح
- كلمات المرور كانت مخزنة بالـ plain text
- لا يوجد CSRF protection أو rate limiting

---

## Sprint 2 — مزامنة حالات الشحنات والأداء

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 2 |
| **العنوان** | Shipment Status Synchronization & Performance |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية |
| **اختبارات جديدة** | 0 |
| **إجمالي الاختبارات** | 0 |

### الأهداف
- توحيد حالات الشحنات (كانت متناثرة ومتضاربة)
- تحسين أداء الاستعلامات
- تنظيف الكود الميت

### ما تم إنجازه

#### مزامنة البيانات
| # | العمل | التفاصيل |
|---|-------|---------|
| 1 | إنشاء `ShipmentStatusConstants.java` | توحيد 20 حالة شحنة في مكان واحد |
| 2 | إنشاء `V2__sync_shipment_statuses.sql` | Flyway migration لمزامنة الحالات في قاعدة البيانات |
| 3 | تحديث `DataInitializer` | من 8 حالات بذرية إلى 17 حالة |

#### تحسين الأداء
| # | التحسين | التأثير |
|---|---------|--------|
| 1 | تحويل EAGER → LAZY loading | 4 علاقات في `Shipment`، 2 في `User` — تقليل الاستعلامات بـ 60% |
| 2 | إضافة `@EntityGraph` لـ `ShipmentRepository` | تحميل انتقائي عند الحاجة |
| 3 | تحويل 5 endpoints في Dashboard | من `findAll().stream()` إلى direct queries |

#### جودة الكود
| # | التحسين | التفاصيل |
|---|---------|---------|
| 1 | إصلاح `User.setActive()/setDeleted()` | كانتا فارغتين (لا تفعلان شيئاً) |
| 2 | استبدال 100+ `System.out.println` بـ SLF4J | في كل ملفات Backend |
| 3 | إضافة `@Valid` لكل `@RequestBody` | التحقق التلقائي من المدخلات |
| 4 | إضافة `@Transactional(readOnly=true)` | لكل عمليات القراءة |

#### تنظيف الواجهة الأمامية
- حذف 21 ملف TypeScript ميت
- إصلاح HTML مكرر في صفحة المناطق
- إنشاء `owner-settings-page.js` (كان مفقوداً)
- توحيد `config.js` المركزي
- إعداد Vite MPA لـ 12 entry point

#### البنية التحتية
- نقل credentials خاصة بـ Grafana/MySQL إلى متغيرات بيئة
- إنشاء `.env.example`

---

## Sprint 3 — أمان JWT والكاش وأساس الاختبارات

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 3 |
| **العنوان** | JWT Security, Rate Limiting, Cache & Test Foundation |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية |
| **اختبارات جديدة** | 14 |
| **إجمالي الاختبارات** | 14 |

### الأهداف
- تأمين JWT tokens
- إضافة rate limiting
- بناء بنية الاختبارات التحتية
- تفعيل Redis cache

### ما تم إنجازه

#### الأمان
| # | العمل | التفاصيل |
|---|-------|---------|
| 1 | نقل JWT من `localStorage` إلى `sessionStorage` | حماية من XSS persistence |
| 2 | نقل CORS config إلى `application.yml` | تكوين مركزي |

#### Rate Limiting
- تكامل Bucket4j: 5 محاولات/دقيقة على:
  - `/api/auth/login`
  - `/api/otp/verify`
  - `/api/auth/reset-password`

#### Dependency Injection Refactor
- تحويل 38 `@Autowired` field injection → constructor injection في 12 ملف
- نمط أنظف وأسهل في الاختبار

#### Logging
- إنشاء `logback-spring.xml` (profile-aware: dev/prod)
- إنشاء `RequestCorrelationFilter` (MDC correlation IDs لتتبع الطلبات)

#### معالجة الاستثناءات
- 6 معالجات جديدة في `GlobalExceptionHandler`: 405, 400, 415, 409, 413
- DTOs جديدة: `CreateShipmentRequest`, `LocationUpdateRequest`, `ContactFormRequest`, `ReconcileRequest`

#### Redis Cache
- تكوين Redis مع متغيرات بيئة
- `@Cacheable` على:
  - `UserService`: 6 methods
  - `DashboardController`: 2 methods
  - `MasterDataController`: 10 methods
- Fallback إلى `ConcurrentMapCacheManager` عند عدم توفر Redis

#### صفحات Frontend جديدة
- `contact.js` — صفحة التواصل
- `settings.js` — صفحة الإعدادات
- `merchant-shipments.js` — قائمة شحنات التاجر
- `merchant-shipment-details.js` — تفاصيل الشحنة

#### الاختبارات الأولى
| ملف | عدد | يغطي |
|-----|-----|------|
| `AuthControllerTest.java` | 6 | login, register, unauthorized, invalidInput, refreshToken, logout |
| `UserServiceTest.java` | 8 | createUser, findById, findAll, updateUser, deleteUser, duplicatePhone, changePassword, userNotFound |

---

## Sprint 4 — توسيع الاختبارات وجودة الكود

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 4 |
| **العنوان** | Extended Testing & Code Quality |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية |
| **اختبارات جديدة** | 75 |
| **إجمالي الاختبارات** | 89 |

### الأهداف
- تغطية اختبارية واسعة للخدمات والمتحكمات
- تحسين جودة الكود وأمانه

### ما تم إنجازه

#### الاختبارات (الإنجاز الأكبر)
| ملف | عدد | يغطي |
|-----|-----|------|
| `ShipmentServiceTest.java` | 22 | CRUD, status transitions, tracking, assignment, validation, concurrent ops |
| `ShipmentControllerTest.java` | 8 | create, getById, list, update, delete, search, bulkStatus, unauthorized |
| `PublicControllerTest.java` | 8 | contact, about, terms, privacyPolicy, search, publicStats, invalidInput, rateLimit |
| `FinancialServiceTest.java` | 8 | calculateFee, processPayment, getBalance, reconcile, payoutGenerate, refund, invalidAmount, overdueCheck |
| `DashboardControllerTest.java` | 5 | ownerDashboard, merchantDashboard, courierDashboard, adminDashboard, unauthorized |
| `FinancialControllerTest.java` | 7 | getFinancials, processReconcile, generatePayout, getPayouts, payoutDetails, markAsPaid, unauthorized |
| `MasterDataControllerTest.java` | 7 | getZones, getStatuses, getRoles, getCities, getSettings, cached, refresh |
| `OtpServiceTest.java` | 10 | generate, verify, expired, invalidCode, resend, maxAttempts, cleanup, rateLimited, phoneFormat, otpLength |

#### الأمان
- JWT secret يجب أن يأتي من env var (بدون fallback)
- إضافة CSP + Permissions-Policy headers في Nginx
- نقل OTP config إلى `application.yml`

#### جودة الكود
- إضافة DB indexes على 4 جداول: `NotificationLog`, `SystemAuditLog`, `FraudBlacklist`, `CashMovementLedger`
- إزالة 36 catch-all block فارغ

#### الواجهة الأمامية
- إنشاء `Logger.js` — بديل مركزي لـ `console.*`
- استبدال 219 `console.*` بـ Logger في 25 ملف
- استبدال inline CSS بـ Tailwind utility classes
- حذف مجلدات `store/` و `types/` الفارغة

---

## Sprint 5 — أمان البنية التحتية وتوثيق API

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 5 |
| **العنوان** | Infrastructure Security & API Documentation |
| **المدة** | أسبوع واحد |
| **الأولوية** | متوسطة-عالية |
| **اختبارات جديدة** | 0 |
| **إجمالي الاختبارات** | 89 |

### الأهداف
- تأمين البنية التحتية (Docker, Grafana, DB)
- توثيق API كامل
- تنظيف نهائي للكود

### ما تم إنجازه

#### أمان البنية التحتية
| # | العمل | التفاصيل |
|---|-------|---------|
| 1 | نقل Docker credentials إلى env vars | MySQL root password, Redis password |
| 2 | إخفاء ports الداخلية | MySQL 3306 + Redis 6379 — داخلي فقط |
| 3 | نقل كلمة مرور Grafana | من hardcoded إلى env var |
| 4 | SwaggerConfig server URLs | من `@Value` بدلاً من hardcoded |

#### توثيق API
- إضافة `@Tag` annotations لجميع 14 controller (كان 5/14 فقط)
- توثيق مجموعات API: Auth, Shipments, Users, Dashboard, Financial, Manifests, Reports, Settings, Notifications, SMS, Telemetry, Health, Public, Backup

#### جودة الكود
- إضافة SLF4J Logger لـ 9 خدمات كانت تستخدم `System.out`
- إضافة `@Column(length=)` constraints على `User` و `Zone`

#### تنظيف الواجهة الأمامية
- إزالة inline scripts من 7 ملفات HTML
- استبدال 4 ملفات JS stub بمحتوى حقيقي
- توحيد `getApiBaseUrl()` عبر `config.js`

---

## Sprint 6 — تقوية الأمان وإصلاح نموذج البيانات

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 6 |
| **العنوان** | Security Hardening & Data Model Fixes |
| **المدة** | أسبوع واحد |
| **الأولوية** | متوسطة |
| **اختبارات جديدة** | 0 |
| **إجمالي الاختبارات** | 89 |

### الأهداف
- تقوية حماية endpoints
- إصلاح العلاقات في نموذج البيانات
- تحسين validation المدخلات

### ما تم إنجازه

#### الأمان
- تقوية حماية endpoints (role-based access لكل مسار)
- تحسين input validation عبر Bean Validation annotations
- إضافة `@PreAuthorize` annotations حيث لزم

#### نموذج البيانات
- إصلاح entity relationships (orphan removal, cascade types)
- إضافة constraints مفقودة (unique, nullable, length)
- تحسين index strategy على الجداول الرئيسية

#### طبقة الخدمات
- تحسين error handling في الخدمات
- إضافة null checks وvalidation
- توحيد نمط الاستجابات

---

# المرحلة 0.5 — بناء الـ API وتجربة المستخدم (Sprints 7-11)

> **الهدف**: توسيع API endpoints وتحسين تجربة المستخدم  
> **المدة الفعلية**: 5 أسابيع  
> **النتيجة**: نظام متكامل مع 100+ test وواجهة أمامية محسنة

---

## Sprint 7 — نقاط API جديدة: المصادقة، المستخدمين، الإشعارات

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 7 |
| **العنوان** | New Endpoints: Auth, User/Courier/Merchant Management, Notifications |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية |
| **اختبارات جديدة** | 11 |
| **إجمالي الاختبارات** | 100 |

### الأهداف
- إضافة endpoints جديدة أساسية
- إكمال CRUD لكل الأدوار
- بناء نظام الإشعارات

### ما تم إنجازه

#### Endpoints جديدة — المصادقة
| Method | Path | الوصف |
|--------|------|-------|
| POST | `/api/auth/logout` | تسجيل خروج (blacklist token) |
| POST | `/api/auth/change-password` | تغيير كلمة المرور |
| POST | `/api/auth/refresh` | تجديد JWT token |

#### Endpoints جديدة — إدارة المستخدمين
| Method | Path | الوصف |
|--------|------|-------|
| PUT | `/api/users/profile` | تعديل الملف الشخصي |
| GET/POST/PUT/DELETE | `/api/couriers/{id}` | CRUD المناديب |
| GET/PUT | `/api/couriers/{id}/location` | موقع المندوب |
| GET/POST/PUT | `/api/merchants/{id}` | CRUD التجار |
| GET/PUT | `/api/employees/{id}` | إدارة الموظفين |

#### Endpoints جديدة — الإشعارات والتلمتري
| Method | Path | الوصف |
|--------|------|-------|
| GET | `/api/notifications` | قائمة الإشعارات |
| PUT | `/api/notifications/{id}/read` | تحديد كمقروء |
| PUT | `/api/notifications/read-all` | تحديد الكل كمقروء |
| POST/GET | `/api/telemetry` | إرسال/قراءة التلمتري |
| GET | `/api/reports/dashboard` | تقرير لوحة التحكم |

#### الكيانات والخدمات
- `TokenBlacklistService` — لتسجيل الخروج (blacklist JWT tokens)
- `Notification` entity + `NotificationType` enum
- `TelemetrySettings` entity

---

## Sprint 8 — Swagger Operations وإعدادات النظام

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 8 |
| **العنوان** | Swagger Operations, SystemSettings Persistence, Code Quality |
| **المدة** | أسبوع واحد |
| **الأولوية** | متوسطة |
| **اختبارات جديدة** | 11 |
| **إجمالي الاختبارات** | 111 |

### الأهداف
- توثيق كل endpoint بـ Swagger @Operation
- تخزين إعدادات النظام في DB
- إنشاء DTOs مفقودة

### ما تم إنجازه

#### توثيق API
- إضافة 32 `@Operation` annotation لـ 7 controllers كانت بدون توثيق
- الآن: 100% من الـ endpoints موثقة في Swagger UI

#### كيان جديد: إعدادات النظام
- `SystemSetting` entity + `SystemSettingRepository`
- `SettingsController` → الآن يقرأ/يكتب من DB بدلاً من ملف config

#### DTOs جديدة
| DTO | الاستخدام |
|-----|-----------|
| `CourierResponseDTO` | عرض بيانات المندوب |
| `MerchantResponseDTO` | عرض بيانات التاجر |
| `ShipmentResponseDTO` | عرض بيانات الشحنة |
| `DashboardStatsDTO` | إحصائيات لوحة التحكم |
| `SettingsResponseDTO` | إعدادات النظام |
| `HealthResponseDTO` | حالة النظام |

#### جودة الكود
- إنشاء `ErrorMessages.java` (constants للرسائل المشتركة)
- نقل القيم الثابتة إلى config files
- `HealthController` يقرأ version/profile من `application.yml`

#### الاختبارات
| ملف | عدد |
|-----|-----|
| `SettingsControllerTest.java` | 3 |
| `ReportsControllerTest.java` | 5 |
| `HealthControllerTest.java` | 3 |

---

## Sprint 9 — إعادة بناء الواجهة الأمامية

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 9 |
| **العنوان** | Comprehensive Frontend Overhaul |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية |
| **اختبارات جديدة** | 0 |
| **إجمالي الاختبارات** | 111 |

### الأهداف
- مطابقة الواجهة الأمامية مع Backend API
- إنشاء مكتبات مشتركة
- تحسين الوصولية (Accessibility)

### ما تم إنجازه

#### مطابقة API
- مراجعة جميع 131 method في `api_service.js`
- مطابقة المسارات مع Backend endpoints
- إصلاح parameters ومسارات خاطئة

#### مكتبات مشتركة جديدة
| المكتبة | الوظيفة |
|---------|---------|
| `UIUtils` | Toast notifications, loading states, empty states, network error overlay |
| `ErrorHandler` | معالجة أخطاء موحدة مع retry + user-friendly messages |
| `loading.css` | Spinner وskeleton loading animations |

#### تحديث صفحات Frontend
- إضافة try/catch + `UIUtils` + `ErrorHandler` لـ 10 ملفات صفحات
- تحويل `owner-pricing-page.js` للعمل مع API حقيقي بدلاً من بيانات وهمية

#### الوصولية (Accessibility)
| # | التحسين | النطاق |
|---|---------|-------|
| 1 | `dir="rtl"` + `lang="ar"` | جميع ملفات HTML |
| 2 | ARIA landmarks | 5 dashboards |
| 3 | Null-safety guards | 3 page scripts |

---

## Sprint 10 — توسيع الاختبارات وترقية المكتبات

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 10 |
| **العنوان** | Test Expansion, Dependency Upgrades, Infrastructure |
| **المدة** | أسبوع واحد |
| **الأولوية** | متوسطة |
| **اختبارات جديدة** | 12 |
| **إجمالي الاختبارات** | 123 |

### الأهداف
- توسيع التغطية الاختبارية للمتحكمات
- ترقية المكتبات الأساسية
- تحسين بنية Docker

### ما تم إنجازه

#### الاختبارات الجديدة
| ملف | عدد | يغطي |
|-----|-----|------|
| `SmsControllerTest.java` | 3 | sendSms, sendBulk, invalidPhone |
| `ManifestControllerTest.java` | 4 | createManifest, getById, assignShipments, completeManifest |
| `UserControllerTest.java` | 3 | getProfile, updateProfile, changeRole |
| `AuditControllerTest.java` | 3 | getAuditLogs, getByEntity, getByUser |
| `BackupControllerTest.java` | 3 | createBackup, restoreBackup, getStatus |
| `NotificationControllerTest.java` | 5 | getAll, getUnread, markAsRead, markAllRead, getCount |
| `TelemetryControllerTest.java` | 3 | postTelemetry, getTelemetry, updateSettings |

#### ترقية المكتبات
| المكتبة | من | إلى |
|---------|-----|-----|
| jjwt | 0.11.5 | 0.12.6 |
| springdoc-openapi | 2.2.0 | 2.7.0 |

#### البنية التحتية
- `OtpService` → Redis-backed مع memory fallback
- `@Scheduled` cleanup لـ OTPs منتهية الصلاحية
- `HealthController` يبلّغ عن حالة DB + Redis
- Dockerfile: multi-stage build, non-root user, healthcheck

---

## Sprint 11 — تنظيف الواجهة وإصلاح XSS

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 11 |
| **العنوان** | Frontend Cleanup & Critical Bug Fixes |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية |
| **اختبارات جديدة** | 0 |
| **إجمالي الاختبارات** | 123 |

### الأهداف
- إزالة الكود الميت نهائياً
- القضاء على XSS المتبقي
- استبدال كل حوارات المتصفح الأصلية

### ما تم إنجازه

#### تنظيف الكود الميت
- حذف مجلد `frontend/frontend/` المكرر
- حذف ملفات بدون extension
- إصلاح backtick-n artifacts في 22 ملف HTML
- تحديث حقوق النشر إلى 2025-2026

#### تقوية XSS
- تطبيق `escapeHtml()` عبر `_e()` shorthand في `GlobalUIHandler.js`
  - 4 دوال لعرض الصفوف
  - 4 دوال لعرض النوافذ المنبثقة
- تعقيم 4 page handlers إضافية

#### استبدال حوارات المتصفح
| قبل | بعد | العدد |
|------|------|-------|
| `confirm()` | `Swal.fire()` | 11 |
| `alert()` | `NotificationService.show()` | 8 |
| `prompt()` | `Swal.fire({input})` | 2 |

**النتيجة**: صفر حوارات متصفح أصلية في النظام

#### صفحات جديدة
- `warehouse-dashboard-page.js` (320 سطر) — لوحة تحكم المستودع
- `admin-dashboard-page.js` (290 سطر) — لوحة تحكم المدير

#### أنماط موحدة
- توحيد `window.apiService` في 3 ملفات (10 استدعاء)

---

# المرحلة 1أ — توحيد ونضج (Sprints 12-15)

> **الهدف**: توحيد نمط API وإضافة ميزات أساسية وتجهيز للإنتاج  
> **المدة الفعلية**: 4 أسابيع  
> **النتيجة**: 245 اختبار ناجح + نظام جاهز للإنتاج

---

## Sprint 12 — توحيد استجابة API والـ DTOs

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 12 |
| **العنوان** | Unified API Response, DTOs, Exception Handling |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية |
| **اختبارات جديدة** | 32 |
| **إجمالي الاختبارات** | 155 |

### الأهداف
- توحيد شكل استجابة API (نمط واحد لكل الـ endpoints)
- بناء طبقة DTOs متكاملة
- معالجة استثناءات شاملة

### ما تم إنجازه

#### `ApiResponse<T>` — نمط الاستجابة الموحد
```json
{
  "success": true,
  "message": "تمت العملية بنجاح",
  "data": { ... },
  "errors": [],
  "timestamp": "2026-03-01T10:30:00Z"
}
```
- `ApiResponse<T>` — للاستجابات العادية
- `ApiPageResponse<T>` — للاستجابات المُقسّمة (paginated)

#### `GlobalExceptionHandler` — معالجة الاستثناءات
| Exception | HTTP Status | الوصف |
|-----------|------------|-------|
| `EntityNotFoundException` | 404 | كيان غير موجود |
| `AccessDeniedException` | 403 | صلاحيات غير كافية |
| `MethodArgumentNotValidException` | 400 | مدخلات غير صالحة |
| `DataIntegrityViolationException` | 409 | تعارض في البيانات |
| `Exception` (catch-all) | 500 | خطأ داخلي |

#### استثناءات مخصصة
- `ResourceNotFoundException` — مورد غير موجود
- `BusinessRuleException` — انتهاك قاعدة عمل
- `DuplicateResourceException` — مورد مكرر
- `InvalidOperationException` — عملية غير صالحة

#### DTOs مُنشأة/مُنقلة
| DTO | الاستخدام |
|-----|-----------|
| `CreatePayoutRequest` | إنشاء دفعة |
| `CreateManifestRequest` | إنشاء مانيفست |
| `CreateUserRequest` | إنشاء مستخدم |
| `UpdateUserRequest` | تعديل مستخدم |
| `CreateShipmentRequest` | إنشاء شحنة |
| `ReturnRequest` / `ReturnResponseDTO` | المرتجعات |
| `ManifestResponseDTO` | عرض المانيفست |
| `FinancialResponseDTO` | البيانات المالية |
| `CourierRatingDTO` / `CourierRatingRequest` | التقييمات |
| `DtoMapper` | تحويل Entity ↔ DTO |

#### Bean Validation
- `@NotBlank`, `@Pattern`, `@Min`, `@Size` على كل DTOs
- أنماط عربية: `^[\\p{L}\\s]+$` للأسماء
- تنسيقات الهاتف: `^[0-9]{10,15}$`

#### تحديث كل Controllers
- جميع الـ controllers تعيد `ApiResponse<T>`
- كل `@RequestBody` مع `@Valid`

#### تحديث Frontend
- تعديل `api_service.js` → `handleResponse()` يقرأ `{success, message, data}`

#### الاختبارات
| ملف | عدد |
|-----|-----|
| `GlobalExceptionHandlerTest.java` | 9 |
| `DtoMapperTest.java` | 8 |
| `ApiResponseTest.java` | 6 |
| `PasswordValidatorTest.java` | 3 |
| `CourierRatingDTO tests` | 6 |

---

## Sprint 13 — WebSocket والاتصال الحي والإشعارات

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 13 |
| **العنوان** | WebSocket Real-Time Communication & Advanced Notifications |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية |
| **اختبارات جديدة** | 39 |
| **إجمالي الاختبارات** | 194 |

### الأهداف
- تفعيل الاتصال الحي عبر WebSocket
- بناء نظام إشعارات متقدم
- دمج SMS مع الإشعارات

### ما تم إنجازه

#### WebSocket Configuration
```java
// STOMP over SockJS
Endpoint: /ws
Topics:
  /topic/shipment/{id}     → تحديثات حالة الشحنة
  /topic/dashboard/{role}  → تحديثات لوحة التحكم
  /topic/courier/{id}      → إشعارات المندوب
```
- `WebSocketConfig.java` — STOMP + SockJS
- `WebSocketAuthInterceptor.java` — JWT authentication for WebSocket

#### Notification System
| المكون | الوظيفة |
|--------|---------|
| `Notification` entity | تخزين الإشعارات في DB |
| `NotificationType` enum | أنواع: SHIPMENT_UPDATE, SYSTEM, PAYMENT, RATING, etc. |
| `NotificationChannel` enum | قنوات: IN_APP, SMS, EMAIL, PUSH |
| `NotificationLog` | سجل الإرسال |
| `NotificationService` | إرسال + broadcast + CRUD |

#### NotificationService Methods
```
send(), getAll(), getUnread(), getUnreadCount()
markAsRead(), markAllAsRead()
broadcastShipmentUpdate() → WebSocket topic
broadcastDashboardUpdate() → WebSocket topic
```

#### Flyway Migration
- `V3__create_notifications_and_ratings.sql` — جدول `notifications` + `courier_ratings` مع indexes

#### Frontend
- `websocket_service.js` — auto-reconnect + exponential backoff
- `NotificationBell.js` — جرس في الـ navbar مع badge للعدد + dropdown

#### الاختبارات
| ملف | عدد | يغطي |
|-----|-----|------|
| `WebSocketConfigTest.java` | 3 | configExists, stompEndpoint, messageBroker |
| `NotificationServiceTest.java` | 13 | send, getAll, getUnread, count, markRead, markAllRead, broadcast, sendToMultiple, invalidUser, emptyNotifications |
| `NotificationControllerTest.java` | 5 | getAll, getUnread, markRead, markAllRead, count |
| `TokenBlacklistServiceTest.java` | 6 | blacklist, isBlacklisted, notBlacklisted, expiry, cleanup, concurrent |
| `AuthenticationHelperTest.java` | 11 | getCurrentUser, getRole, isOwner, isAdmin, isMerchant, isCourier, notAuthenticated, invalidToken, extractClaims |

---

## Sprint 14 — الرفع المجمع والباركود والتقييمات

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 14 |
| **العنوان** | Bulk Upload, Barcode/AWB, Courier Ratings, Label Printing |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية |
| **اختبارات جديدة** | 38 |
| **إجمالي الاختبارات** | 232 |

### الأهداف
- رفع شحنات مجمعة من Excel
- توليد باركود و AWB
- نظام تقييم المناديب
- طباعة الملصقات

### ما تم إنجازه

#### الرفع المجمع (Bulk Upload)
- `ExcelService` — Apache POI parser لملفات Excel
- **Endpoints**:
  - `POST /api/shipments/bulk` — رفع ملف Excel (حد أقصى 500 شحنة)
  - `GET /api/shipments/bulk/template` — تحميل قالب Excel

#### AWB Generation
- `AwbService` — توليد رقم بوليصة الشحن
  - Format: `TWS-YYYYMMDD-NNNNNN` مع checksum
  - Methods: `generateAwb()`, `isValidAwb()`, `extractDate()`

#### Barcode/QR Code
- `BarcodeService` — مكتبة ZXing
  - `generateBarcode()` → Code128 format
  - `generateQrCode()` → QR Code
- **Endpoints**:
  - `GET /api/shipments/{id}/barcode`
  - `GET /api/shipments/{id}/qrcode`

#### طباعة الملصقات
- `LabelController`:
  - `GET /api/shipments/{id}/label` — ملصق فردي
  - `POST /api/shipments/labels/bulk` — ملصقات مجمعة
  - `POST /api/shipments/{id}/pod/upload` — رفع إثبات التسليم
  - `GET /api/shipments/{id}/pod/download` — تحميل إثبات التسليم

#### تقييم المناديب
- `CourierRating` entity (1-5 نجوم + تعليق)
- `RatingController`:
  - `POST /api/ratings` — إضافة تقييم
  - `GET /api/ratings/courier/{courierId}` — تقييمات مندوب
  - `GET /api/ratings/shipment/{shipmentId}` — تقييم شحنة

#### الاختبارات
| ملف | عدد | يغطي |
|-----|-----|------|
| `AwbServiceTest.java` | 8 | generate, validate (valid, invalid), extractDate, format, uniqueness, checksum, batchGenerate |
| `BarcodeServiceTest.java` | 6 | generateBarcode, generateQr, invalidInput, imageFormat, dimensions, emptyData |
| `BulkUploadControllerTest.java` | 5 | upload (success, invalidFormat, tooMany), getTemplate, emptyFile |
| `LabelControllerTest.java` | 8 | getSingleLabel, getBulkLabels, uploadPod, downloadPod, invalidIdLabel, emptyBulk, podNotFound, invalidPodFormat |
| `RatingControllerTest.java` | 8 | createRating, getByCourier, getByShipment, duplicateRating, invalidRating, averageRating, ratingNotFound, updateRating |
| `CourierRatingDTO tests` | 3 | mapping, validation, nullHandling |

---

## Sprint 15 — DevOps والجاهزية للإنتاج

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 15 |
| **العنوان** | DevOps, E2E Tests, Production Readiness |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية |
| **اختبارات جديدة** | 13 |
| **إجمالي الاختبارات** | 245 |

### الأهداف
- إعداد Flyway migrations
- API versioning
- جاهزية الإنتاج (Monitoring, Backup, PDF)
- اختبارات تكاملية

### ما تم إنجازه

#### Flyway Migrations
| Migration | الوصف |
|-----------|-------|
| `V1__add_user_lockout_columns.sql` | أعمدة قفل المستخدم بعد محاولات فاشلة |
| `V2__sync_shipment_statuses.sql` | مزامنة حالات الشحنات |
| `V3__create_notifications_and_ratings.sql` | جداول الإشعارات والتقييمات |

#### API Versioning
- `ApiVersionFilter.java` — mapping `/api/v1/**` paths
- `RequestTracingFilter.java` — correlation IDs لكل طلب

#### المراقبة والمقاييس
- `MetricsService` — Micrometer/Prometheus metrics
- Actuator endpoints مفعلة: health, info, prometheus, metrics
- Prometheus config + Grafana dashboards

#### PDF Reports
- `PdfService` (iText) — توليد:
  - ملصقات شحن
  - تقارير مالية
  - تقارير الأداء

#### النسخ الاحتياطي
- `BackupService` + `BackupController`:
  - `POST /api/backup/create`
  - `POST /api/backup/restore`
  - `GET /api/backup/status`

#### الإنتاج
- Docker Compose production settings
- Nginx: SSL termination + gzip + rate limiting
- Documentation: DEPLOYMENT_RUNBOOK.md

#### الاختبارات
| ملف | عدد | يغطي |
|-----|-----|------|
| `ApiVersionFilterTest.java` | 3 | v1Path, nonVersionedPath, invalidVersion |
| `RequestTracingFilterTest.java` | 4 | addCorrelationId, propagateExisting, filterChain, headerPresent |
| `IntegrationTest.java` | 10 | fullShipmentLifecycle, authFlow, roleAccess, concurrentOperations, dataConsistency, cacheInvalidation |

### 📊 حالة النظام بعد Sprint 15
```
✅ 245 اختبار ناجح
✅ BUILD SUCCESS
✅ 26 domain entity
✅ 14 controller (~70 endpoint)
✅ 13 service
✅ 3 Flyway migrations
✅ Docker + Nginx + Prometheus + Grafana
✅ PDF + Excel + Barcode/QR
✅ WebSocket + Notifications
✅ Redis Cache
✅ JWT + BCrypt + Rate Limiting
```

---

# المرحلة 1ب — ميزات تجارية أساسية (Sprints 16-20)

> **الهدف**: إضافة الميزات التجارية الأساسية المطلوبة من العملاء  
> **المدة الفعلية**: 5 أسابيع  
> **النتيجة**: 330 اختبار ناجح + تتبع حي + مرتجعات + محافظ + webhooks + تقارير

---

## Sprint 16 — تتبع الموقع الحي والتتبع العام

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 16 |
| **العنوان** | Live Location Tracking & Public Tracking Page |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية — أكثر ميزة مطلوبة |
| **اختبارات جديدة** | 22 |
| **إجمالي الاختبارات** | 267 |

### الأهداف
- تتبع GPS حي لموقع المندوب
- صفحة تتبع عامة للمستلم (بدون تسجيل دخول)
- حساب ETA (وقت الوصول المتوقع)
- إيجاد أقرب مندوب

### ما تم إنجازه

#### Backend — ملفات جديدة
| الملف | الوصف |
|-------|-------|
| `CourierLocationService.java` | حفظ موقع GPS، حساب ETA (Haversine × 1.3 road factor ÷ 25 km/h)، أقرب مندوب |
| `CourierLocationController.java` | `POST /api/couriers/location` (COURIER), `GET /api/couriers/{id}/location`, `GET /api/couriers/{id}/location/history` |
| `PublicTrackingController.java` | `GET /api/public/tracking/{trackingNumber}` (full tracking + timeline + ETA), `GET /api/public/tracking/{trackingNumber}/eta` |
| `LocationDTO.java` | `latitude` (BigDecimal), `longitude` (BigDecimal), `timestamp` (Instant) |
| `TrackingResponseDTO.java` | `trackingNumber`, `currentStatus`, `courierName`, `lastCourierLocation`, `estimatedMinutesToDelivery`, `podType`, `statusTimeline` |
| `V4__add_location_tracking_indexes.sql` | Composite index on `courier_location_history` |

#### Validation
- Latitude: ±90° range
- Longitude: ±180° range
- Invalid coordinates → `IllegalArgumentException`

#### ETA Algorithm
```
distance = haversineDistance(courierLat, courierLng, deliveryLat, deliveryLng)
roadDistance = distance × 1.3 (road factor)
etaMinutes = (roadDistance ÷ 25 km/h) × 60
```

#### SecurityConfig Changes
```java
.requestMatchers("/api/couriers/location").hasRole("COURIER")
.requestMatchers("/api/couriers/*/location/**").hasAnyRole("OWNER", "ADMIN", "COURIER")
.requestMatchers("/api/public/tracking/**").permitAll()
```

#### Frontend
| الملف | الوصف |
|-------|-------|
| `tracking.html` | صفحة تتبع عامة — حقل رقم التتبع + timeline + ETA |
| `tracking-page.js` | IIFE pattern — يقرأ tracking number من URL + يعرض الحالات |
| `tracking-page.css` | تنسيق Timeline UI + المسافة المتبقية |

#### الاختبارات
| ملف | عدد | أمثلة |
|-----|-----|-------|
| `CourierLocationServiceTest.java` | 10 | saveLocation, getLastLocation, getHistory, calculateETA, nearestCourier, invalidCoords, outOfRangeLatitude, outOfRangeLongitude, emptyHistory, noManifestETA |
| `CourierLocationControllerTest.java` | 6 | updateLocation, getLastLocation, getHistory, invalidInput, notFound, unauthorized |
| `PublicTrackingControllerTest.java` | 6 | trackByNumber, trackNotFound, trackWithCourierLocation, trackETA, deliveredNoETA, invalidTrackingNumber |

---

## Sprint 17 — نظام إدارة المرتجعات

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 17 |
| **العنوان** | Returns Management System |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية — دورة حياة الشحنة غير مكتملة بدون المرتجعات |
| **اختبارات جديدة** | 18 |
| **إجمالي الاختبارات** | 285 |

### الأهداف
- دورة حياة كاملة للمرتجعات (7 حالات)
- تعيين مندوب لاستلام المرتجع
- حساب رسوم الإرجاع

### ما تم إنجازه

#### دورة حياة المرتجع
```
RETURN_REQUESTED → RETURN_APPROVED → RETURN_PICKUP_ASSIGNED → RETURN_PICKED_UP 
    ↓                                                              ↓
RETURN_REJECTED                              RETURN_IN_WAREHOUSE → RETURN_DELIVERED_TO_MERCHANT
```

#### Backend — ملفات جديدة
| الملف | الوصف |
|-------|-------|
| `ReturnShipment.java` (معدّل) | إضافة `ReturnStatusEnum` (7 حالات), `status`, `assignedCourier`, `returnFee`, `notes`, `approvedAt`, `pickedUpAt`, `deliveredAt` |
| `ReturnShipmentRepository.java` (معدّل) | `findByStatus()`, `findByMerchantId()` (JPQL), `findByAssignedCourierId()` (JPQL), `existsByOriginalShipmentIdAndStatusNot()` |
| `ReturnRequestDTO.java` | `shipmentId` (required), `reason` (required), `notes` |
| `ReturnResponseDTO.java` | Full return response with original shipment data |
| `ReturnService.java` | `VALID_TRANSITIONS` EnumMap, `RETURNABLE_STATUSES` set, `RETURN_FEE_RATE = 0.50` |
| `ReturnController.java` | 5 endpoints (see below) |
| `V5__add_return_management.sql` | DDL changes for return management |

#### API Endpoints
| Method | Path | الوصف | الأدوار |
|--------|------|-------|---------|
| POST | `/api/returns` | طلب إرجاع | MERCHANT |
| GET | `/api/returns` | قائمة المرتجعات (حسب الدور) | ALL |
| GET | `/api/returns/{id}` | تفاصيل المرتجع | ALL |
| PUT | `/api/returns/{id}/status` | تحديث الحالة | OWNER, ADMIN |
| PUT | `/api/returns/{id}/assign` | تعيين مندوب | OWNER, ADMIN |

#### قواعد العمل
- لا يمكن إنشاء مرتجع لشحنة غير مسلّمة (RETURNABLE_STATUSES)
- لا يمكن إنشاء مرتجع مكرر لنفس الشحنة
- تسلسل حالات صارم (VALID_TRANSITIONS)
- رسوم إرجاع = 50% من رسوم التوصيل

#### الاختبارات
| ملف | عدد | أمثلة |
|-----|-----|-------|
| `ReturnServiceTest.java` | 11 | createReturn, approveReturn, rejectReturn, assignCourier, updateStatus (valid), invalidTransition, returnFeeCalc, duplicateReturn, returnForDeliveredOnly, shipmentNotFound, getByMerchant |
| `ReturnControllerTest.java` | 7 | createReturn, getAllReturns, getById, updateStatus, assignCourier, returnNotFound, invalidRequest |

---

## Sprint 18 — المحافظ والتسوية المالية

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 18 |
| **العنوان** | Wallets & Financial Settlement |
| **المدة** | أسبوع واحد |
| **الأولوية** | عالية — التدفق المالي الأساسي |
| **اختبارات جديدة** | 19 |
| **إجمالي الاختبارات** | 304 |

### الأهداف
- محفظة رقمية لكل مستخدم
- تسوية تلقائية عند تسليم شحنة COD
- سجل معاملات مفصل

### ما تم إنجازه

#### نموذج المحفظة
```
كل مستخدم ← Wallet
  ├── WalletType: MERCHANT / COURIER / COMPANY
  ├── balance: BigDecimal (12,2)
  └── currency: "EGP" (default)

WalletTransaction
  ├── TransactionType: CREDIT / DEBIT
  ├── TransactionReason: COD_COLLECTED, DELIVERY_FEE, COMMISSION, WITHDRAWAL, SETTLEMENT, RETURN_FEE, ADJUSTMENT
  ├── balanceBefore / balanceAfter
  └── referenceId (shipmentId / payoutId)
```

#### Backend — ملفات جديدة
| الملف | الوصف |
|-------|-------|
| `Wallet.java` | Entity مع `OneToOne` User, `WalletType` enum |
| `WalletTransaction.java` | Entity مع `TransactionType`, `TransactionReason` enums |
| `WalletRepository.java` | `findByUserId()`, `findByWalletType()`, `existsByUserId()` |
| `WalletTransactionRepository.java` | `findByWalletIdOrderByCreatedAtDesc(Pageable)`, `sumByWalletIdAndType()`, `existsByWalletIdAndReferenceIdAndReason()` |
| `WalletService.java` | Core wallet operations |
| `WalletController.java` | 5 endpoints |
| `WalletDTO.java` | Nested `TransactionDTO` |
| `V6__create_wallet_tables.sql` | `wallets` + `wallet_transactions` |

#### WalletService Methods
| Method | الوصف |
|--------|-------|
| `getOrCreateWallet()` | إنشاء محفظة إذا لم تكن موجودة |
| `credit(walletId, amount, reason, refId)` | إضافة رصيد |
| `debit(walletId, amount, reason, refId)` | خصم رصيد (مع فحص الرصيد الكافي) |
| `getBalance(userId)` | الرصيد الحالي |
| `getTransactions(userId, Pageable)` | سجل المعاملات |
| `settleShipment(shipmentId)` | تسوية COD — idempotent |
| `getAllWallets()` | كل المحافظ (Admin) |

#### تسوية COD
```
عند توصيل شحنة COD:
  1. محفظة المندوب ← CREDIT (مبلغ COD) ← COD_COLLECTED
  2. Create transaction with reason=COD_COLLECTED
  3. Idempotent: existsByWalletIdAndReferenceIdAndReason check
```

#### API Endpoints
| Method | Path | الوصف |
|--------|------|-------|
| GET | `/api/wallet` | محفظتي |
| GET | `/api/wallet/balance` | رصيدي |
| GET | `/api/wallet/transactions` | سجل المعاملات (paginated) |
| POST | `/api/wallet/withdraw` | طلب سحب |
| GET | `/api/wallet/admin/all` | كل المحافظ (OWNER/ADMIN) |

#### الاختبارات
| ملف | عدد | أمثلة |
|-----|-----|-------|
| `WalletServiceTest.java` | 12 | getOrCreate (new, existing), credit, debit, insufficientBalance, getBalance, settle (COD), settle (already settled), transactionHistory, withdraw, creditZero, debitZero, getAllWallets |
| `WalletControllerTest.java` | 7 | getMyWallet, getBalance, getTransactions, withdraw, adminGetAll, walletNotFound, unauthorizedAdmin |

---

## Sprint 19 — نظام Webhooks والأحداث

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 19 |
| **العنوان** | Webhooks & Event System |
| **المدة** | أسبوع واحد |
| **الأولوية** | متوسطة-عالية — تكامل مع أنظمة التجار |
| **اختبارات جديدة** | 16 |
| **إجمالي الاختبارات** | 320 |

### الأهداف
- نظام webhook subscriptions للتجار
- HMAC-SHA256 توقيع رقمي للأمان
- إعادة محاولة تلقائية عند الفشل
- سجل أحداث كامل

### ما تم إنجازه

#### آلية العمل
```
التاجر يسجل webhook:
  POST /api/webhooks { url: "https://merchant.com/hook", events: ["SHIPMENT_STATUS_CHANGED"] }

عند تغيير حالة الشحنة:
  WebhookService.dispatch() → HTTP POST to merchant URL
    Body: { event, shipmentId, trackingNumber, oldStatus, newStatus, timestamp }
    Headers: X-Webhook-Signature (HMAC-SHA256 with merchant's secret)

إعادة المحاولة: MAX_ATTEMPTS = 5
```

#### Backend — ملفات جديدة
| الملف | الوصف |
|-------|-------|
| `WebhookSubscription.java` | Entity: merchant, url, secret (HMAC key), events (comma-separated), active |
| `WebhookEvent.java` | Entity: `DeliveryStatus` (PENDING/SENT/FAILED), subscription, eventType, payload (TEXT), attempts, responseCode |
| `WebhookSubscriptionRepository.java` | `findByMerchantIdAndActiveTrue()`, `findActiveByEventType()` (JPQL LIKE) |
| `WebhookEventRepository.java` | `findBySubscriptionIdOrderByCreatedAtDesc(Pageable)`, `findByStatusAndAttemptsLessThan()` |
| `WebhookDTO.java` | `CreateWebhookRequest` (validated), `WebhookEventDTO` |
| `WebhookService.java` | subscribe, unsubscribe, dispatch (@Async), sendTestEvent, retryFailed |
| `WebhookController.java` | 7 endpoints |
| `V7__create_webhook_tables.sql` | `webhook_subscriptions` + `webhook_events` |

#### HMAC-SHA256 Signing
```java
Mac mac = Mac.getInstance("HmacSHA256");
mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
String signature = Hex.encodeHexString(mac.doFinal(payload.getBytes()));
// Header: X-Webhook-Signature: sha256={signature}
```

#### HTTP Client
- `java.net.http.HttpClient` مع 15s timeout
- @Async للإرسال غير المتزامن
- MAX_ATTEMPTS = 5

#### API Endpoints
| Method | Path | الوصف | الأدوار |
|--------|------|-------|---------|
| POST | `/api/webhooks` | إنشاء اشتراك | MERCHANT |
| GET | `/api/webhooks` | قائمة اشتراكاتي | MERCHANT |
| GET | `/api/webhooks/{id}` | تفاصيل اشتراك | MERCHANT |
| DELETE | `/api/webhooks/{id}` | إلغاء اشتراك | MERCHANT |
| GET | `/api/webhooks/{id}/events` | سجل الأحداث | MERCHANT |
| POST | `/api/webhooks/{id}/test` | إرسال حدث تجريبي | MERCHANT |
| POST | `/api/webhooks/retry` | إعادة المحاولة | OWNER/ADMIN |

#### الاختبارات
| ملف | عدد | أمثلة |
|-----|-----|-------|
| `WebhookServiceTest.java` | 9 | subscribe, unsubscribe, dispatch (matching, noSubscribers), hmacSignature, retryFailed, maxAttempts, invalidUrl, testEvent |
| `WebhookControllerTest.java` | 7 | create, list, getById, delete, getEvents, testWebhook, retryAdmin |

---

## Sprint 20 — التقارير المتقدمة والواجهة الأمامية

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 20 |
| **العنوان** | Advanced Reports & Frontend Enhancement |
| **المدة** | أسبوع واحد |
| **الأولوية** | متوسطة |
| **اختبارات جديدة** | 10 |
| **إجمالي الاختبارات** | 330 |

### الأهداف
- تقارير تحليلية متقدمة (إيرادات، أداء، توزيع)
- تقوية أمان الواجهة الأمامية (XSS prevention)
- صفحة تحليلات للمالك

### ما تم إنجازه

#### Backend — التقارير
| الملف | الوصف |
|-------|-------|
| `AnalyticsDTO.java` | `RevenueReport` (مع `PeriodRevenue` شهري), `StatusDistribution`, `CourierPerformance`, `TopMerchant` |
| `AnalyticsService.java` | 4 methods أساسية |
| `AnalyticsController.java` | 4 endpoints (OWNER/ADMIN) |

#### AnalyticsService Methods
| Method | الوصف |
|--------|-------|
| `getRevenueByPeriod(from, to)` | إيرادات شهرية مع تفصيل كل فترة |
| `getStatusDistribution(from, to)` | توزيع الشحنات حسب الحالة (8 حالات) |
| `getCourierPerformanceRanking(from, to)` | ترتيب المناديب حسب التوصيلات |
| `getTopMerchants(from, to)` | أكثر التجار نشاطاً |

#### API Endpoints
| Method | Path | الوصف |
|--------|------|-------|
| GET | `/api/analytics/revenue?from=&to=` | تقرير الإيرادات |
| GET | `/api/analytics/status-distribution?from=&to=` | توزيع الحالات |
| GET | `/api/analytics/courier-ranking?from=&to=` | ترتيب المناديب |
| GET | `/api/analytics/top-merchants?from=&to=` | أكثر التجار نشاطاً |

#### Frontend — أمان XSS
| الملف | الوصف |
|-------|-------|
| `sanitizer.js` | `escapeHtml()`, `sanitizeObject()`, `safeText()`, `safeHtml()` — exposed as `window.Sanitizer` |

#### Frontend — صفحة التحليلات
| الملف | الوصف |
|-------|-------|
| `owner/analytics.html` | صفحة RTL Arabic مع Bootstrap 5.3, date pickers, 4 أقسام بطاقات |
| `owner-analytics-page.js` | IIFE — يجلب من `/api/analytics/*` ويعرض بطاقات + جداول |

#### الاختبارات
| ملف | عدد | أمثلة |
|-----|-----|-------|
| `AnalyticsServiceTest.java` | 5 | revenueByPeriod, statusDistribution, courierRanking, topMerchants, emptyDateRange |
| `AnalyticsControllerTest.java` | 4 | getRevenue, getStatusDistribution, getCourierRanking, getTopMerchants |

### 📊 حالة النظام بعد Sprint 20
```
✅ 330 اختبار ناجح
✅ BUILD SUCCESS
✅ 38 domain entity
✅ 22 controller (~110 endpoint)
✅ 22 service
✅ 7 Flyway migrations (V1-V7)
✅ 26 frontend page handlers
✅ Live location tracking + Public tracking
✅ Returns management (7 states)
✅ Digital wallets + COD settlement
✅ Webhook system with HMAC signing
✅ Analytics dashboard
```

---

# المرحلة 2 — التوسع التجاري والذكاء (Sprints 21-25)

> **الحالة**: 📋 مخطط — لم يبدأ التنفيذ بعد  
> **الهدف**: خطط الاشتراك، إدارة الأسطول، نظام الدعم، الذكاء التشغيلي، التوسع الإقليمي  
> **الاختبارات المتوقعة**: ~164 اختبار جديد (330 → ~494)

---

## Sprint 21 — خطط الاشتراك والفواتير وبوابة الدفع (مخطط)

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 21 |
| **العنوان** | Subscription Plans, Invoicing & Payment Gateway |
| **المدة المتوقعة** | 2 أسابيع |
| **الأولوية** | عالية — مصدر الدخل |
| **اختبارات متوقعة** | ~35 |
| **إجمالي متوقع** | ~365 |
| **الحالة** | 📋 مخطط |

### الهدف
بناء الطبقة المالية للمنصة: خطط اشتراك للتجار (Free/Basic/Pro/Enterprise)، فوترة تلقائية، تكامل مع بوابات دفع (Paymob/Stripe)، تتبع الاستهلاك.

### المخرجات المتوقعة

#### كيانات جديدة (6)
| Entity | الوصف |
|--------|-------|
| `SubscriptionPlan` | خطط الاشتراك (4 خطط + أسعار شهرية وسنوية) |
| `MerchantSubscription` | اشتراك التاجر مع حالة (TRIAL/ACTIVE/PAST_DUE/EXPIRED/CANCELLED) |
| `Invoice` | فواتير مع حالة (DRAFT/PENDING/PAID/OVERDUE/CANCELLED/REFUNDED) |
| `InvoiceItem` | بنود الفاتورة |
| `PaymentTransaction` | معاملات الدفع عبر البوابات |
| `UsageTracking` | تتبع استهلاك التاجر شهرياً |

#### خدمات جديدة (5)
| Service | الوصف |
|---------|-------|
| `SubscriptionService` | CRUD + upgrade/downgrade + renewal + expiration |
| `InvoiceService` | إنشاء + دفع + استرداد + فوترة شهرية تلقائية |
| `PaymentGateway` (Interface) | `charge()`, `refund()`, `getTransaction()`, `verifyWebhook()` |
| `PaymobGateway` / `StripeGateway` | implementations |
| `UsageTrackingService` | تتبع + حدود + إعادة تعيين شهرية |

#### API Endpoints جديدة (~12)
- Subscriptions: plans, subscribe, upgrade, cancel, usage
- Invoices: list, details, pay, admin list, admin refund
- Payment webhooks: Paymob, Stripe

#### Flyway Migrations
- `V8__create_subscription_tables.sql`
- `V9__seed_subscription_plans.sql`

---

## Sprint 22 — إدارة الأسطول وتتبع المركبات (مخطط)

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 22 |
| **العنوان** | Fleet Management & Vehicle Tracking |
| **المدة المتوقعة** | 2 أسابيع |
| **الأولوية** | عالية — إدارة الموارد المادية |
| **اختبارات متوقعة** | ~32 |
| **إجمالي متوقع** | ~397 |
| **الحالة** | 📋 مخطط |

### الهدف
إدارة شاملة لأسطول المركبات: تسجيل، تعيين مندوب-مركبة، صيانة دورية وطوارئ، تتبع الوقود، لوحة تحكم الأسطول.

### المخرجات المتوقعة

#### كيانات جديدة (4)
| Entity | الوصف |
|--------|-------|
| `Vehicle` | المركبة: type, make, model, licensePlate, status, fuelType, capacity |
| `VehicleAssignment` | تعيين مندوب-مركبة (1:1 active) |
| `VehicleMaintenance` | صيانة: scheduled/emergency/inspection + status + cost |
| `FuelLog` | سجل الوقود: liters, cost, odometer |

#### خدمات جديدة (4)
| Service | الوصف |
|---------|-------|
| `VehicleService` | CRUD + retire + available vehicles |
| `VehicleAssignmentService` | assign/unassign + auto-unassign previous |
| `MaintenanceService` | schedule/complete/cancel + reminders (@Scheduled) |
| `FuelService` | log + reports + consumption analysis |

#### API Endpoints جديدة (~16)
- Vehicles: CRUD (5)
- Assignments: assign, unassign, list, getByCorier (4)
- Maintenance: schedule, complete, list, upcoming, overdue (5)
- Fuel: log, history, report (3)

#### Flyway Migration
- `V10__create_fleet_tables.sql`

---

## Sprint 23 — نظام التذاكر والدعم الفني و SLA (مخطط)

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 23 |
| **العنوان** | Support Tickets, SLA & Communication Hub |
| **المدة المتوقعة** | 2 أسابيع |
| **الأولوية** | عالية — خدمة العملاء |
| **اختبارات متوقعة** | ~35 |
| **إجمالي متوقع** | ~432 |
| **الحالة** | 📋 مخطط |

### الهدف
نظام دعم فني متكامل: تذاكر مع أولويات، SLA مع تصعيد تلقائي، رسائل على التذاكر، قاعدة معرفة.

### المخرجات المتوقعة

#### كيانات جديدة (5)
| Entity | الوصف |
|--------|-------|
| `SupportTicket` | التذكرة: ticketNumber, subject, category, priority, status, SLA deadlines |
| `TicketMessage` | رسائل + internal notes + مرفقات |
| `SlaPolicy` | سياسة SLA لكل أولوية: response time, resolution time |
| `KnowledgeArticle` | مقالات مساعدة: Markdown content, categories, tags, view count |
| `EscalationRule` | قواعد تصعيد: SLA_BREACH, UNASSIGNED_TIMEOUT, REOPENED_COUNT |

#### خدمات جديدة (4)
| Service | الوصف |
|---------|-------|
| `SupportTicketService` | CRUD + assign + status transitions + @Scheduled escalation |
| `SlaService` | deadline calculation + compliance check + metrics |
| `KnowledgeService` | CRUD + publish + search + popularity |
| `EscalationService` | process escalations + apply rules + notify |

#### حالات التذكرة
```
OPEN → IN_PROGRESS → WAITING_CUSTOMER → RESOLVED → CLOSED
  ↓                                        ↓
REOPENED                                 (auto-close after 7 days)
```

#### SLA Policies
| الأولوية | استجابة أولى | حل |
|----------|-------------|-----|
| CRITICAL | < 1 ساعة | < 4 ساعات |
| HIGH | < 4 ساعات | < 24 ساعة |
| MEDIUM | < 12 ساعة | < 48 ساعة |
| LOW | < 24 ساعة | < 72 ساعة |

#### Flyway Migrations
- `V11__create_support_tables.sql`
- `V12__seed_sla_policies.sql`

---

## Sprint 24 — محرك التوزيع الذكي وتحسين المسارات (مخطط)

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 24 |
| **العنوان** | Smart Assignment Engine & Route Optimization |
| **المدة المتوقعة** | 2 أسابيع |
| **الأولوية** | عالية — الميزة التنافسية الأهم |
| **اختبارات متوقعة** | ~30 |
| **إجمالي متوقع** | ~462 |
| **الحالة** | 📋 مخطط |

### الهدف
محرك توزيع ذكي يحسب أفضل مندوب لكل شحنة، مع تحسين ترتيب التوصيل وتوقع الطلب.

### المخرجات المتوقعة

#### خوارزمية التوزيع الذكي
```
Score(courier, shipment) = Σ(weight × factor)
├── Distance (40%): المسافة — من CourierLocationService
├── Load (25%): عدد الشحنات الحالية vs السعة
├── Rating (15%): متوسط التقييم — من CourierRating
├── Zone (10%): هل المندوب في منطقة الشحنة؟ — من CourierZone
├── Vehicle (5%): هل المركبة مناسبة؟ — من Fleet (Sprint 22)
└── History (5%): نسبة النجاح في المنطقة
```

#### تحسين المسارات (Route Optimization)
```
1. Nearest Neighbor heuristic (O(n²)) — ترتيب أولي
2. 2-opt improvement — تحسين تكراري
3. اعتبار نوافذ التسليم الزمنية
4. إعادة حساب عند إضافة شحنة جديدة
```

#### كيانات جديدة (3)
| Entity | الوصف |
|--------|-------|
| `AssignmentRule` | قواعد قابلة للتعديل: MAX_LOAD, MAX_DISTANCE, MIN_RATING |
| `AssignmentScore` | سجل النتائج: totalScore + breakdown لكل عامل |
| `OptimizedRoute` | المسار المحسّن: waypoints JSON, totalDistanceKm, estimatedDuration |

#### خدمات جديدة (3)
| Service | الوصف |
|---------|-------|
| `SmartAssignmentService` | calculateScore, findBestCourier, autoAssign, bulkAssign |
| `RouteOptimizationService` | nearestNeighbor, twoOptImprove, calculateTotalDistance |
| `DemandPredictionService` | predictDailyDemand, predictCourierNeed (rolling average + day-of-week) |

#### Flyway Migrations
- `V13__create_assignment_tables.sql`
- `V14__seed_assignment_rules.sql`

---

## Sprint 25 — دعم متعدد الدول والعملات والضرائب (مخطط)

### معلومات عامة

| البند | القيمة |
|-------|--------|
| **الرقم** | Sprint 25 |
| **العنوان** | Multi-Country, Multi-Currency & Tax Compliance |
| **المدة المتوقعة** | 2 أسابيع |
| **الأولوية** | متوسطة-عالية — شرط التوسع الإقليمي |
| **اختبارات متوقعة** | ~32 |
| **إجمالي متوقع** | ~494 |
| **الحالة** | 📋 مخطط |

### الهدف
دعم تشغيل النظام في دول عربية متعددة: عملات، ضرائب، فواتير إلكترونية، تنسيقات عناوين.

### المخرجات المتوقعة

#### الدول المدعومة
| الدولة | العملة | ضريبة VAT | بوابة الدفع |
|--------|--------|----------|------------|
| 🇪🇬 مصر | EGP | 14% | Paymob |
| 🇸🇦 السعودية | SAR | 15% | Tap |
| 🇦🇪 الإمارات | AED | 5% | Stripe |
| 🇯🇴 الأردن | JOD | 16% | — |

#### كيانات جديدة (5)
| Entity | الوصف |
|--------|-------|
| `Country` | الدولة: code (ISO), name, currencyCode, phonePrefix, timeZone |
| `Currency` | العملة: code (ISO 4217), symbol, decimalPlaces |
| `ExchangeRate` | أسعار الصرف: base, target, rate, effectiveDate |
| `TaxRule` | قواعد الضرائب: country, taxType, rate, exemptions |
| `EInvoice` | الفاتورة الإلكترونية: format (ETA/ZATCA/FTA), signedPayload, qrCode |

#### خدمات جديدة (5)
| Service | الوصف |
|---------|-------|
| `CountryService` | CRUD + activate countries |
| `CurrencyService` | convert, getExchangeRate, fetchRatesFromApi (@Scheduled daily) |
| `TaxService` | calculateTax, getApplicableRules, isExempt |
| `EInvoiceService` | generate, submit, sign, generateQr |
| `LocalizationService` | formatAddress, formatPhone, formatCurrency per country |

#### الفاتورة الإلكترونية
```
مصر: ETA (Electronic Tax Authority) format
  └── XML payload + digital signature + QR code

السعودية: ZATCA (Fatoorah) format
  └── XML/JSON + CSID signing + QR code

الإمارات: FTA compliance
  └── Standard invoice format
```

#### Flyway Migrations
- `V15__create_country_currency_tables.sql`
- `V16__create_tax_tables.sql`
- `V17__seed_countries_and_currencies.sql`
- `V18__seed_tax_rules.sql`

---

# الملحقات

## إحصائيات شاملة

### تطور الاختبارات عبر السبرنتات

```
Sprint:  1   2   3   4   5   6   7   8   9  10  11  12  13  14  15  16  17  18  19  20
Tests:   0   0  14  89  89  89 100 111 111 123 123 155 194 232 245 267 285 304 320 330
         ─   ─  ██  ████████████████████████████████████████████████████████████████████
```

### تطور الكود

| المقياس | Sprint 1 | Sprint 10 | Sprint 15 | Sprint 20 | Sprint 25 (هدف) |
|---------|---------|-----------|-----------|-----------|-----------------|
| Domain Entities | 26 | 28 | 29 | 38 | 55+ |
| Controllers | 14 | 14 | 17 | 22 | 32+ |
| Services | 13 | 14 | 17 | 22 | 34+ |
| Test Files | 0 | 22 | 36 | 46 | 65+ |
| Total Tests | 0 | 123 | 245 | 330 | ~494 |
| Flyway Migrations | 0 | 2 | 3 | 7 | 19 |
| Frontend Pages | 18 | 22 | 24 | 26 | 37+ |
| API Endpoints | ~70 | ~80 | ~90 | ~110 | ~160 |

### مراحل المشروع

```
Phase 0: Foundation Repair (Sprint 1-6)
├── Security fixes (P0)
├── Performance optimization
├── Code quality + logging
├── Infrastructure security
└── Test foundation

Phase 0.5: API & UX (Sprint 7-11)
├── 15+ new endpoints
├── Swagger documentation
├── Frontend overhaul
├── Library upgrades
└── XSS elimination

Phase 1a: Maturity (Sprint 12-15)
├── Unified API response
├── WebSocket real-time
├── Bulk upload + barcodes
├── PDF reports + backup
└── Production readiness

Phase 1b: Core Features (Sprint 16-20)
├── Live location tracking
├── Returns management
├── Digital wallets
├── Webhook system
└── Analytics dashboard

Phase 2: Expansion (Sprint 21-25) ← PLANNED
├── Subscription monetization
├── Fleet management
├── Support & SLA
├── Smart assignment AI
└── Multi-country expansion
```

---

> **نهاية التقرير** — آخر تحديث: 2 مارس 2026
