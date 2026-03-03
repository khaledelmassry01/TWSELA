# 🚀 Sprint 2 — خطة التحسين الممنهجة الكاملة

## ملخص تنفيذي

| البند | التفاصيل |
|---|---|
| **مرجع السبرنت** | Sprint 2 — Quality, Performance & Cleanup |
| **المتطلب السابق** | Sprint 1 ✅ مكتمل (28 مهمة، 5 حزم عمل، 0 أخطاء) |
| **إجمالي المهام** | 32 مهمة في 5 حزم عمل |
| **الأولوية** | P0 حرج (2) + P1 عالي (22) + P2 متوسط (8) |
| **الملفات المتأثرة** | ~40 ملف (Backend 12 + Frontend 25 + Infrastructure 3) |

---

## 🔍 نتائج التدقيق التفصيلي (ما قبل السبرنت)

### المشاكل الحرجة المتبقية من Sprint 1:

| # | المشكلة | الخطورة | الأثر |
|---|---|---|---|
| P0-17 | **عدم تطابق حالات الشحنة** — الكود يستخدم 15+ حالة، DataInitializer يحتوي 8، قاعدة البيانات 12 | 🔴 حرج | كل عمليات تغيير الحالة تفشل بـ RuntimeException |
| P0-10 | JWT مخزن في localStorage | 🔴 حرج | مؤجل لـ Sprint 3 (يحتاج تغيير backend + frontend معاً) |

### المشاكل عالية الأولوية المكتشفة:

| # | المشكلة | الأثر |
|---|---|---|
| 1 | 4 علاقات EAGER في Shipment + 2 في User = N+1 كارثي | أداء |
| 2 | DashboardController يحمّل كل الشحنات في الذاكرة ثم يفلتر | أداء |
| 3 | بيانات وهمية hardcoded في 3 endpoints بالـ Dashboard | وظيفي |
| 4 | `User.setActive()` و `setDeleted()` فارغتان = no-op | منطقي |
| 5 | 100+ سطر `System.out.println` بدلاً من SLF4J Logger | جودة |
| 6 | 21 ملف TypeScript ميت بالكامل (لا يُستورد من أي مكان) | نظافة |
| 7 | zones.html يحتوي مستند HTML مكرر (سطر 565-1098) | وظيفي |
| 8 | `owner-settings.js` مرجع في HTML لكن الملف غير موجود | وظيفي |
| 9 | Global error handlers فارغة تبتلع الأخطاء بصمت | تتبع |
| 10 | Base URL مكرر يدوياً في 4 ملفات JS | صيانة |
| 11 | Vite لا يعرف عن صفحات HTML الفرعية (MPA config مفقود) | بناء |
| 12 | @Valid مفقود من معظم الـ Controllers (4 فقط من أصل ~50 endpoint) | أمان |

---

## 📦 حزم العمل (Work Packages)

---

### WP-1: مزامنة حالات الشحنة (P0 حرج) — 6 مهام

> **الهدف**: حل عدم التطابق الكارثي بين الحالات في الكود وقاعدة البيانات وDataInitializer

#### التحليل المفصل للفجوة:

| الحالة | DataInitializer | قاعدة البيانات | الكود Java | الإجراء |
|---|:---:|:---:|:---:|---|
| `PENDING` | ✅ | ✅ | ✅ | لا شيء |
| `PROCESSING` | ✅ | ❌ | ❌ | حذف من DataInitializer |
| `APPROVED` | ❌ | ✅ | ✅ | إضافة لـ DataInitializer |
| `PENDING_APPROVAL` | ❌ | ❌ | ✅ | إضافة للكل |
| `PICKED_UP` | ❌ | ✅ | ✅ | إضافة لـ DataInitializer |
| `RECEIVED_AT_HUB` | ❌ | ❌ | ✅ | إضافة للكل |
| `ASSIGNED_TO_COURIER` | ❌ | ❌ | ✅ | إضافة للكل |
| `IN_TRANSIT` | ❌ | ✅ | ✅ | إضافة لـ DataInitializer |
| `OUT_FOR_DELIVERY` | ✅ | ✅ | ✅ | لا شيء |
| `DELIVERED` | ✅ | ✅ | ✅ | لا شيء |
| `FAILED_DELIVERY` | ✅ | ✅ | ✅ | لا شيء |
| `FAILED_ATTEMPT` | ❌ | ❌ | ✅ | إضافة للكل |
| `RETURNED` | ✅ | ❌ | ❌ | استبدال بـ RETURNED_TO_ORIGIN |
| `RETURNED_TO_ORIGIN` | ❌ | ✅ | ✅ | إضافة لـ DataInitializer |
| `RETURNED_TO_HUB` | ❌ | ❌ | ✅ | إضافة للكل |
| `CANCELLED` | ✅ | ✅ | ✅ | لا شيء |
| `ON_HOLD` | ✅ | ✅ | ✅ | لا شيء |
| `PARTIALLY_DELIVERED` | ❌ | ✅ | ✅ | إضافة لـ DataInitializer |
| `RESCHEDULED` | ❌ | ✅ | ✅ | إضافة لـ DataInitializer |

#### المهام:

| # | المهمة | الملف | التفاصيل |
|---|---|---|---|
| T-01 | تحديث DataInitializer بالقائمة الكاملة (17 حالة) | `DataInitializer.java` | استبدال القائمة الحالية (8) بالـ 17 حالة الموحدة |
| T-02 | إعادة تفعيل `@Component` على DataInitializer | `DataInitializer.java` | إزالة التعليق `//` ليعمل عند بدء التشغيل |
| T-03 | إنشاء SQL migration script | `V2__sync_shipment_statuses.sql` (جديد) | INSERT IF NOT EXISTS للحالات المفقودة |
| T-04 | إنشاء ShipmentStatusConstants enum | `ShipmentStatusConstants.java` (جديد) | enum مركزي لكل أسماء الحالات بدل string literals |
| T-05 | استبدال string literals في ShipmentService | `ShipmentService.java` | استخدام الـ enum بدل `"PENDING_APPROVAL"` إلخ |
| T-06 | استبدال string literals في ShipmentController | `ShipmentController.java` | استخدام الـ enum بدل الأسماء النصية |

---

### WP-2: تحسين أداء Backend — 8 مهام

> **الهدف**: إصلاح N+1 queries، إزالة findAll() من Dashboard، إصلاح الاستعلامات

#### المهام:

| # | المهمة | الملف | التفاصيل |
|---|---|---|---|
| T-07 | تحويل EAGER → LAZY في Shipment (4 علاقات) | `Shipment.java` | `merchant`, `zone`, `status`, `recipientDetails` → LAZY |
| T-08 | إضافة `@EntityGraph` للاستعلامات الحرجة | `ShipmentRepository.java` | Named entity graph لتحميل العلاقات المطلوبة فقط |
| T-09 | إضافة استعلامات Dashboard محسّنة للـ Repository | `ShipmentRepository.java` | `findTop10ByOrderByUpdatedAtDesc()`, `sumDeliveryFeeByStatus()`, `countByMerchantIdAndCreatedAtBetween()` |
| T-10 | إعادة كتابة `getOwnerDashboardSummary` | `DashboardController.java` | استبدال `findAll().stream().sorted().limit(10)` بـ `findTop10` |
| T-11 | إعادة كتابة `getAdminDashboardSummary` | `DashboardController.java` | نفس النمط — استعلامات مباشرة بدل in-memory filter |
| T-12 | إعادة كتابة `getMerchantDashboardSummary` | `DashboardController.java` | استبدال `findByMerchantId()` + stream بـ repository queries |
| T-13 | إعادة كتابة `getCourierDashboardSummary` | `DashboardController.java` | استبدال `findByCourierId()` + stream بـ repository queries |
| T-14 | إصلاح `getWarehouseDashboardSummary` + إزالة البيانات الوهمية | `DashboardController.java` | استبدال placeholder logic بـ استعلامات حقيقية، حذف `getDashboardStatistics` endpoint الوهمي |

---

### WP-3: جودة Backend — 7 مهام

> **الهدف**: إصلاح الثغرات المنطقية، تحسين الـ logging، تطبيق validation

#### المهام:

| # | المهمة | الملف | التفاصيل |
|---|---|---|---|
| T-15 | إصلاح `User.setActive()` و `setDeleted()` | `User.java` | تحويلهما إلى deprecated مع تعليق واضح OR ربطهما بـ `setStatus()` و `setIsDeleted()` |
| T-16 | استبدال System.out/err بـ SLF4J Logger | ملفات متعددة (~8) | `DashboardController`, `UserController`, `PublicController`, `MasterDataController`, `GlobalExceptionHandler`, `DataInitializer` |
| T-17 | إضافة `@Valid` للـ Controllers المفقودة | `ShipmentController.java`, `UserController.java`, `ManifestController.java` | إضافة `@Valid` لكل `@RequestBody` parameter |
| T-18 | إزالة password logging من PublicController | `PublicController.java` | حذف `System.out.println("New Password: " + newPassword)` — **ثغرة أمنية** |
| T-19 | إصلاح `getStatistics()` — كل القيم متطابقة | `DashboardController.java` | `activeShipments` و `deliveredShipments` كلاهما = `count()` — إصلاح ليعكس القيم الحقيقية |
| T-20 | حذف/إصلاح endpoints البيانات الوهمية | `DashboardController.java` | `getDashboardStatistics()` يرجع أرقام hardcoded، `getRevenueChart()` و `getShipmentsChart()` أيضاً وهمية |
| T-21 | إضافة `@Transactional(readOnly=true)` للقراءات | `DashboardController.java` | كل GET methods يجب أن تكون read-only transaction |

---

### WP-4: تنظيف Frontend — 7 مهام

> **الهدف**: حذف الكود الميت، إصلاح الروابط المكسورة، توحيد الإعدادات

#### المهام:

| # | المهمة | الملف(ات) | التفاصيل |
|---|---|---|---|
| T-22 | حذف 21 ملف TypeScript ميت | 21 ملف `.ts` | كل الملفات تحت `frontend/src/js/` بامتداد `.ts` — لا يستوردها أي ملف |
| T-23 | إصلاح zones.html — حذف المحتوى المكرر | `frontend/owner/zones.html` | حذف الأسطر 565-1098 (مستند HTML مكرر كامل) |
| T-24 | إنشاء `owner-settings-page.js` | `frontend/src/js/pages/owner-settings-page.js` (جديد) | stub أساسي يمنع 404 error |
| T-25 | تحديث settings.html للملف الجديد | `frontend/owner/settings.html` | تغيير `owner-settings.js` → `owner-settings-page.js` |
| T-26 | توحيد Base URL في ملف واحد | `frontend/src/js/shared/config.js` (جديد) + 4 ملفات | إنشاء `config.js` مركزي واستيراده من `app.js`, `api_service.js`, `auth_service.js`, `login.js` |
| T-27 | إصلاح Global Error Handlers | `frontend/src/js/app.js` | إضافة `console.error` logging + optional user notification |
| T-28 | إصلاح Vite MPA Config | `frontend/vite.config.js` | إضافة `rollupOptions.input` لكل ملفات HTML (12 entry point) |

---

### WP-5: بنية تحتية — 4 مهام

> **الهدف**: تأمين كلمات المرور الافتراضية، توحيد Docker Compose

#### المهام:

| # | المهمة | الملف | التفاصيل |
|---|---|---|---|
| T-29 | تأمين Grafana password | `docker-compose.monitoring.yml` | استبدال `admin123` بـ `${GRAFANA_PASSWORD}` |
| T-30 | تأمين MySQL credentials | `docker-compose.monitoring.yml` | استبدال `root`/`twsela123` بـ `${MYSQL_ROOT_PASSWORD}` و `${MYSQL_PASSWORD}` |
| T-31 | إنشاء `.env.example` للـ Docker | `.env.example` (جديد) | نموذج لجميع متغيرات البيئة المطلوبة |
| T-32 | إزالة port binding لـ MySQL | `docker-compose.monitoring.yml` | إزالة `ports: "3306:3306"` — MySQL يجب ألا يكون مكشوفاً للخارج |

---

## 📊 مصفوفة التبعيات

```
T-01 ──→ T-02 ──→ T-03       (WP-1: statuses أولاً)
T-04 ──→ T-05 ──→ T-06       (WP-1: enum بعد القائمة)
T-07 ──→ T-08                 (WP-2: LAZY أولاً ثم EntityGraph)
T-09 ──→ T-10..T-14           (WP-2: queries أولاً ثم Dashboard)
T-22..T-23 ──→ T-28           (WP-4: تنظيف أولاً ثم Vite)
T-24 ──→ T-25                 (WP-4: إنشاء الملف ثم تحديث HTML)
```

## ⚡ ترتيب التنفيذ المقترح

```
┌─────────────────────────────────────────────────────┐
│ المرحلة 1: WP-1 (T-01 → T-06)                      │
│ إصلاح حالات الشحنة — كل شيء يعتمد عليها            │
├─────────────────────────────────────────────────────┤
│ المرحلة 2: WP-2 (T-07 → T-14)                      │
│ أداء Backend — EAGER→LAZY + Dashboard queries        │
├─────────────────────────────────────────────────────┤
│ المرحلة 3: WP-3 (T-15 → T-21)                      │
│ جودة Backend — Validation + Logging + Security       │
├─────────────────────────────────────────────────────┤
│ المرحلة 4: WP-4 (T-22 → T-28)                      │
│ تنظيف Frontend — Delete dead code + Fix links        │
├─────────────────────────────────────────────────────┤
│ المرحلة 5: WP-5 (T-29 → T-32)                      │
│ بنية تحتية — Docker security                         │
└─────────────────────────────────────────────────────┘
```

## ✅ معايير القبول (Definition of Done)

- [ ] كل الـ 17 حالة شحنة موجودة في DataInitializer + SQL migration
- [ ] لا يوجد أي `findAll()` بدون pagination في DashboardController
- [ ] كل EAGER relationships في Shipment/User تحولت لـ LAZY
- [ ] صفر `System.out.println` في production code
- [ ] صفر ملفات TypeScript متبقية
- [ ] zones.html بدون HTML مكرر
- [ ] Vite build ينجح لكل صفحات HTML
- [ ] لا كلمات مرور hardcoded في Docker Compose
- [ ] المشروع يُبنى بدون أخطاء compilation

---

## 🚫 خارج نطاق Sprint 2 (مؤجل لـ Sprint 3)

| المهمة | السبب |
|---|---|
| JWT من localStorage → httpOnly cookie | يحتاج تغيير backend authentication flow كامل |
| Rate Limiting (Bucket4j) | يحتاج dependency جديدة + تصميم سياسات |
| Redis caching enablement | يحتاج تصميم cache strategy كامل |
| CI/CD Pipeline | يحتاج GitHub Actions setup منفصل |
| Constructor Injection migration | يحتاج refactoring واسع لكل Services |
| DTO migration لـ Map<String,Object> endpoints | يحتاج تصميم DTOs جديدة + تعديل frontend |

---

> **جاهز للبدء؟** قل **"ابدا"** لبدء التنفيذ من WP-1.
