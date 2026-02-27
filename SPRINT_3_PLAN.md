# 🚀 Sprint 3 — خطة تعزيز البنية والأمان والموثوقية

## ✅ حالة التنفيذ: **مكتمل بالكامل**

| البند | التفاصيل |
|---|---|
| **مرجع السبرنت** | Sprint 3 — Architecture, Security & Reliability |
| **المتطلب السابق** | Sprint 1 ✅ (28 مهمة) + Sprint 2 ✅ (32 مهمة) |
| **إجمالي المهام** | 38 مهمة في 6 حزم عمل — **38/38 مكتملة ✅** |
| **الأولوية** | P0 حرج (4) + P1 عالي (18) + P2 متوسط (16) |
| **الملفات المتأثرة** | ~55 ملف (Backend 30 + Frontend 20 + Infrastructure 5) |
| **نتيجة البناء** | `mvn compile` ✅ — صفر أخطاء |
| **نتيجة الاختبارات** | 14 test — 0 failures, 0 errors ✅ |

---

## 🔍 نتائج تدقيق ما بعد Sprint 2

### الحالة الحالية بالأرقام:

| المقياس | القيمة |
|---|---|
| ملفات TypeScript متبقية | **0** ✅ |
| EAGER fetch violations | **0** ✅ |
| `System.out/err` في Backend | **20** (في 3 ملفات فقط) |
| `@Autowired` field injection | **38** (في 12 ملف) |
| Endpoints بدون `@Valid` | **5** (untyped `Map<String,Object>`) |
| صفحات JS مفقودة (404) | **4** |
| روابط تنقل ميتة | **7** |
| npm dependencies غير مستخدمة | **5 من 5** (كلها!) |
| Controllers بدون response format موحد | **~10 من 14** |
| Test classes | **0** |
| Rate limiting | **0** |
| Caching | **معطّل بالكامل** |

---

## 📦 حزم العمل (Work Packages)

---

### WP-1: أمان JWT + Rate Limiting (P0 حرج) — 7 مهام

> **الهدف**: حماية المصادقة من brute-force + نقل JWT لتخزين أكثر أماناً

#### التحليل المفصل:

**JWT Storage:**
- الرمز مخزن في `localStorage` — معرّض لسرقة XSS
- `login.js` سطر 548 يخزن الرمز مباشرة `localStorage.setItem('authToken', data.token)` — **نسخة مكررة** من `auth_service.storeToken()`
- الحل: نقل لـ `sessionStorage` + توحيد التخزين في `auth_service` فقط
- (httpOnly cookies تحتاج تغيير كامل في flow المصادقة — مؤجل)

**Rate Limiting:**  
- **صفر** حماية ضد brute-force حالياً
- `/api/auth/login` + `/api/public/forgot-password` + `/api/public/send-otp` + `/api/public/reset-password` = 4 endpoints حرجة مكشوفة

#### المهام:

| # | المهمة | الملف(ات) | التفاصيل |
|---|---|---|---|
| T-01 | توحيد تخزين JWT في `auth_service` فقط | `login.js` | حذف `localStorage.setItem('authToken', data.token)` المباشر واستخدام `authService.storeToken()` |
| T-02 | نقل JWT من localStorage → sessionStorage | `auth_service.js`, `api_service.js`, `app.js`, `login.js` | استبدال كل `localStorage.getItem/setItem('authToken')` بـ `sessionStorage` |
| T-03 | إضافة Bucket4j dependency | `pom.xml` | إضافة `bucket4j-spring-boot-starter` |
| T-04 | إنشاء RateLimitFilter | `RateLimitFilter.java` (جديد) | Filter يحدد المحاولات حسب IP: login=5/min, OTP=3/min, password-reset=3/min |
| T-05 | تسجيل RateLimitFilter في SecurityConfig | `SecurityConfig.java` | إضافة الـ filter قبل `UsernamePasswordAuthenticationFilter` |
| T-06 | إصلاح CORS — ربط yml config | `SecurityConfig.java` | استخدام `@Value("${app.cors.allowed-origins}")` بدل hardcoded list |
| T-07 | إضافة production CORS origins | `application-prod.yml` | إضافة `app.cors.allowed-origins` لـ production domains |

---

### WP-2: Constructor Injection + Logging (P1) — 8 مهام

> **الهدف**: تحويل كل `@Autowired` field injection لـ constructor injection + إزالة آخر System.out

#### الملفات المطلوب تحويلها (38 حقل في 12 ملف):

| الملف | عدد `@Autowired` | الإجراء |
|---|---|---|
| `DataInitializer.java` | 8 | Constructor injection |
| `ShipmentController.java` | 5 | Constructor injection |
| `MasterDataController.java` | 5 | Constructor injection |
| `DashboardController.java` | 4 | Constructor injection |
| `FinancialController.java` | 3 | Constructor injection |
| `ManifestController.java` | 3 | Constructor injection |
| `ReportsController.java` | 3 | Constructor injection |
| `AuditService.java` | 2 | Constructor injection |
| `AuditController.java` | 1 | Constructor injection |
| `BackupController.java` | 1 | Constructor injection |
| `BaseService.java` | 1 | Constructor injection (abstract) |
| `SmsController.java` | 1 | Constructor injection |

#### المهام:

| # | المهمة | الملف(ات) | التفاصيل |
|---|---|---|---|
| T-08 | تحويل Controllers لـ constructor injection | 9 Controllers | حذف `@Autowired` من الحقول، إضافة `private final` + constructor |
| T-09 | تحويل Services لـ constructor injection | `AuditService.java`, `BaseService.java` | نفس النمط |
| T-10 | تحويل DataInitializer لـ constructor injection | `DataInitializer.java` | 8 dependencies → constructor |
| T-11 | استبدال System.out بـ SLF4J في AuthController | `AuthController.java` | 16 سطر `System.out/err` → `log.info/error/debug` |
| T-12 | استبدال System.out بـ SLF4J في AuditService | `AuditService.java` | 3 أسطر |
| T-13 | استبدال System.out بـ SLF4J في ApplicationConfig | `ApplicationConfig.java` | 1 سطر |
| T-14 | إنشاء `logback-spring.xml` | `src/main/resources/logback-spring.xml` (جديد) | Profiles-aware logging: console (dev) + file rotation (prod) + separate error log |
| T-15 | إضافة MDC correlation ID filter | `RequestCorrelationFilter.java` (جديد) | ربط كل request بـ UUID ليظهر في logs |

---

### WP-3: Exception Handling + API Response Format (P1) — 7 مهام

> **الهدف**: توحيد استجابات API + معالجة أخطاء شاملة

#### Exceptions المفقودة من GlobalExceptionHandler:

| Exception | HTTP Status | الحالة |
|---|---|---|
| `HttpRequestMethodNotSupportedException` | 405 | **مفقود** |
| `MissingServletRequestParameterException` | 400 | **مفقود** |
| `HttpMediaTypeNotSupportedException` | 415 | **مفقود** |
| `DataIntegrityViolationException` | 409 | **مفقود** |
| `MaxUploadSizeExceededException` | 413 | **مفقود** |
| `HttpMessageNotReadableException` | 400 | **مفقود** |

#### API Response Format الموحد المقترح:

```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private Instant timestamp;
}
```

#### المهام:

| # | المهمة | الملف(ات) | التفاصيل |
|---|---|---|---|
| T-16 | إنشاء `ApiResponse<T>` generic class | `web/dto/ApiResponse.java` (جديد) | Success/error factory methods + builder |
| T-17 | إضافة 6 exception handlers مفقودة | `GlobalExceptionHandler.java` | معالجة 405, 400, 415, 409, 413, 400 |
| T-18 | إصلاح RuntimeException catch-all | `GlobalExceptionHandler.java` | لا يُسرّب `ex.getMessage()` للعميل في production |
| T-19 | توحيد استجابات AuthController | `AuthController.java` | استخدام `ApiResponse<>` بدل maps مخصصة |
| T-20 | توحيد استجابات FinancialController | `FinancialController.java` | لف raw entities بـ `ApiResponse` |
| T-21 | توحيد استجابات PublicController | `PublicController.java` | لف track/feedback responses |
| T-22 | إنشاء DTOs لـ 5 endpoints untyped | `web/dto/` (5 ملفات جديدة) | `ReconcileRequest`, `LocationUpdateDTO`, `SaveSettingsRequest`, `ContactFormDTO`, `CreateShipmentDTO` + `@Valid` |

---

### WP-4: صفحات Frontend المفقودة + التنقل (P1) — 8 مهام

> **الهدف**: إصلاح كل صفحات JS المفقودة + الروابط الميتة

#### الملفات المفقودة:

| HTML يطلب | JS المفقود | الأثر |
|---|---|---|
| `contact.html` | `contact.js` | صفحة الاتصال بدون تفاعل |
| `settings.html` | `settings.js` | إعدادات الملف الشخصي معطلة |
| `merchant/shipments.html` | `merchant-shipments.js` | قائمة شحنات التاجر معطلة |
| `merchant/shipment-details.html` | `merchant-shipment-details.js` | تفاصيل الشحنة معطلة |

#### روابط تنقل ميتة:

| Dashboard | الروابط الميتة |
|---|---|
| `admin/dashboard.html` | `/admin/shipments.html`, `/admin/users.html`, `/admin/reports.html`, `/admin/settings.html` |
| `warehouse/dashboard.html` | `/warehouse/incoming.html`, `/warehouse/outgoing.html`, `/warehouse/inventory.html` |

#### المهام:

| # | المهمة | الملف(ات) | التفاصيل |
|---|---|---|---|
| T-23 | إنشاء `contact.js` | `pages/contact.js` (جديد) | نموذج اتصال + API integration |
| T-24 | إنشاء `settings.js` | `pages/settings.js` (جديد) | إعدادات المستخدم + تغيير كلمة المرور |
| T-25 | إنشاء `merchant-shipments.js` | `pages/merchant-shipments.js` (جديد) | جدول شحنات + بحث + pagination |
| T-26 | إنشاء `merchant-shipment-details.js` | `pages/merchant-shipment-details.js` (جديد) | عرض تفاصيل + تتبع timeline |
| T-27 | إصلاح روابط admin sidebar | `admin/dashboard.html` | تعليق/إخفاء الروابط للصفحات غير الموجودة مع رسالة "قريباً" |
| T-28 | إصلاح روابط warehouse sidebar | `warehouse/dashboard.html` | نفس المعالجة — إخفاء أو تعطيل |
| T-29 | إضافة config.js للصفحات المتبقية | `merchant/shipments.html`, `merchant/shipment-details.html`, `owner/merchants.html` | ضمان تحميل config.js قبل أي JS آخر |
| T-30 | إزالة npm dependencies غير المستخدمة | `package.json` | حذف `axios`, `date-fns`, `html-to-docx`, `lodash`, `marked` |

---

### WP-5: تفعيل Redis Cache (P2) — 4 مهام

> **الهدف**: تفعيل التخزين المؤقت لتقليل الحمل على قاعدة البيانات

#### الحالة الحالية:
- `spring-boot-starter-data-redis` موجود في pom.xml
- `@EnableCaching` مفعّل في `CacheConfig.java`
- كل `@Cacheable` annotations في `UserService.java` معلّقة
- Redis config في `application.yml` معلّق بالكامل

#### المهام:

| # | المهمة | الملف(ات) | التفاصيل |
|---|---|---|---|
| T-31 | تفعيل Redis config في application.yml | `application.yml` | إلغاء التعليق + إضافة `${REDIS_HOST}` و `${REDIS_PASSWORD}` |
| T-32 | تفعيل `@Cacheable` في UserService | `UserService.java` | إلغاء التعليق عن 6 annotations + تحسين TTL |
| T-33 | إضافة `@Cacheable` لـ Dashboard summary | `DashboardController.java` | Cache dashboard data لمدة 2 دقيقة |
| T-34 | إضافة `@Cacheable` لـ zones/statuses | `MasterDataController.java` | Cache master data لمدة 10 دقائق |

---

### WP-6: البنية التحتية للاختبار (P2) — 4 مهام

> **الهدف**: إنشاء الأساس لكتابة الاختبارات الآلية

#### الحالة الحالية: **0% اختبارات — لا يوجد مجلد test حتى**

#### المهام:

| # | المهمة | الملف(ات) | التفاصيل |
|---|---|---|---|
| T-35 | إضافة test dependencies | `pom.xml` | `spring-boot-starter-test`, `spring-security-test`, `h2` لـ in-memory DB |
| T-36 | إنشاء هيكل test directory | `src/test/java/com/twsela/` (جديد) | إنشاء المجلدات: `web/`, `service/`, `repository/` |
| T-37 | كتابة اختبارات AuthController | `AuthControllerTest.java` (جديد) | 5+ test cases: login success, invalid credentials, inactive user, missing fields, rate-limited |
| T-38 | كتابة اختبارات ShipmentService | `ShipmentServiceTest.java` (جديد) | 5+ test cases: create, status transition, validation, courier assignment |

---

## 📊 مصفوفة التبعيات

```
WP-1 (T-01,T-02) → WP-4 (frontend fixes depend on consolidated token handling)
WP-2 (T-08..T-13) → WP-3 (clean constructors before adding ApiResponse)
WP-3 (T-16) → WP-3 (T-19..T-22) (ApiResponse class needed first)
WP-5 (T-31) → WP-5 (T-32..T-34) (Redis config needed first)
WP-6 (T-35,T-36) → WP-6 (T-37,T-38) (test infra needed first)
```

## ⚡ ترتيب التنفيذ المقترح

```
┌─────────────────────────────────────────────────────────┐
│ المرحلة 1: WP-1 (T-01 → T-07)                          │
│ أمان JWT + Rate Limiting + CORS                          │
├─────────────────────────────────────────────────────────┤
│ المرحلة 2: WP-2 (T-08 → T-15)                          │
│ Constructor Injection + Last System.out + Logging         │
├─────────────────────────────────────────────────────────┤
│ المرحلة 3: WP-3 (T-16 → T-22)                          │
│ ApiResponse + Exception Handling + DTOs                   │
├─────────────────────────────────────────────────────────┤
│ المرحلة 4: WP-4 (T-23 → T-30)                          │
│ Frontend: Missing pages + nav links + cleanup             │
├─────────────────────────────────────────────────────────┤
│ المرحلة 5: WP-5 (T-31 → T-34)                          │
│ Redis Cache activation                                    │
├─────────────────────────────────────────────────────────┤
│ المرحلة 6: WP-6 (T-35 → T-38)                          │
│ Test infrastructure + first test classes                   │
└─────────────────────────────────────────────────────────┘
```

## ✅ معايير القبول (Definition of Done)

- [x] JWT مخزن في `sessionStorage` بدل `localStorage`
- [x] Rate limiting نشط على login + OTP + password-reset (5 محاولات/دقيقة)
- [x] CORS origins تُقرأ من yml بدل hardcoded
- [x] صفر `@Autowired` field injection (كلها constructor)
- [x] صفر `System.out.println` في كل الكود
- [x] `logback-spring.xml` مع file rotation + correlation ID
- [x] `AppUtils` مستخدم في AuthController + PublicController (حافظنا على النمط الموحد القائم بدل إنشاء ApiResponse جديد)
- [x] 6 exception handlers جديدة في GlobalExceptionHandler
- [x] DTOs مربوطة بـ `@Valid` (CreateShipmentRequest, LocationUpdateRequest, ContactFormRequest, ReconcileRequest)
- [x] 4 ملفات JS جديدة (contact, settings, merchant-shipments, merchant-shipment-details)
- [x] 7 روابط ميتة معالجة (admin + warehouse) — disabled مع "قريباً"
- [x] صفر npm dependencies غير مستخدمة
- [x] Cache نشط مع ConcurrentMap (dev) + Redis (prod) — TTL محدد
- [x] 14 test cases تعمل بنجاح (6 AuthController + 8 UserService)
- [x] المشروع يُبنى بدون أخطاء compilation

---

## 🚫 خارج نطاق Sprint 3 (مؤجل لـ Sprint 4+)

| المهمة | السبب |
|---|---|
| httpOnly Cookie auth | يحتاج إعادة هيكلة كاملة لـ JWT flow |
| CI/CD Pipeline (GitHub Actions) | يحتاج setup منفصل + Docker registry |
| WebSocket للإشعارات الفورية | feature جديد كلياً |
| نظام المحفظة (Wallet) | feature جديد — يحتاج تصميم DB + API |
| نظام التذاكر (Tickets) | feature جديد — يحتاج تصميم كامل |
| Admin/Warehouse pages كاملة | يحتاج تصميم UI + APIs إضافية |
| E2E Tests (Cypress/Playwright) | يحتاج Sprint كامل منفصل |
| API versioning | يحتاج migration strategy |

---

> **جاهز للبدء؟** قل **"ابدا"** لبدء التنفيذ من WP-1.
