# تقرير شامل لمشروع TWSELA

تاريخ التحديث: 2026-02-25

## 1) ملخص تنفيذي

مشروع **TWSELA** هو نظام إدارة شحنات متكامل (Courier Management System) مبني بهندسة **Full-Stack**:

- **Backend:** Spring Boot 3.3.3 + Java 17
- **Frontend:** Vanilla JavaScript (ES Modules) + Vite
- **Database:** MySQL (أساسي) مع دعم H2 للتطوير
- **Security:** JWT + Spring Security + RBAC
- **Observability:** Actuator + Prometheus + Grafana
- **Integrations:** Twilio SMS + Google Maps (حسب التهيئة)

النظام مصمم لسيناريوهات تشغيل متعددة الأدوار (Owner/Admin/Merchant/Courier/Warehouse Manager) مع دعم واجهات عربية وعمليات الشحن من الإنشاء حتى التسليم/الإرجاع.

---

## 2) هيكل المشروع (Workspace)

### الجذر
- `README.md`: توثيق عام طويل للمشروع.
- `docs/`: مستندات الأعمال والمشروع.
- `frontend/`: الواجهة الأمامية وصفحات الأدوار.
- `twsela/`: تطبيق Spring Boot (Backend).
- `tools/`: أدوات مساعدة للتصدير والسكربتات.

### Frontend
- صفحات HTML حسب الدور:
  - `frontend/admin/`
  - `frontend/owner/`
  - `frontend/merchant/`
  - `frontend/courier/`
  - `frontend/warehouse/`
- منطق JavaScript الموحد في:
  - `frontend/src/js/pages/`
  - `frontend/src/js/services/`
  - `frontend/src/js/shared/`
  - `frontend/src/js/store/`
- Styling في:
  - `frontend/src/css/`
  - `frontend/src/assets/css/`

### Backend
- `twsela/src/main/java/com/twsela/`:
  - `web/` Controllers (REST APIs)
  - `service/` Business Logic
  - `repository/` Data Access
  - `domain/` Entities
  - `security/` JWT/SecurityConfig
  - `config/` إعدادات إضافية
- `twsela/src/main/resources/application.yml`: الإعدادات الرئيسية.

---

## 3) التكنولوجيا والاعتمادات الأساسية

من `twsela/pom.xml`:

- Spring Boot starters: Web, Data JPA, Security, Validation, Actuator
- JWT: `io.jsonwebtoken` (jjwt)
- MySQL Driver + H2
- Redis + Spring Cache
- Micrometer Prometheus
- OpenAPI/Swagger: `springdoc-openapi-starter-webmvc-ui`
- Twilio SDK
- Apache POI + iText/OpenPDF + ICU4J (تقارير/PDF/Arabic support)

من `frontend/package.json`:

- Vite, ESLint, Prettier, Sass
- Axios
- date-fns
- lodash

---

## 4) إعدادات التشغيل الحالية

من `application.yml`:

- `server.port: 8000`
- Context path: `/`
- Swagger UI: `/swagger-ui.html`
- OpenAPI docs: `/api-docs` و `/v3/api-docs`
- JWT expiration: 24 ساعة
- قاعدة البيانات الافتراضية: MySQL `twsela`
- CORS مفعّل لمنافذ التطوير (5173/5174/8000/8080 وغيرها)

---

## 5) نموذج الصلاحيات (RBAC)

الأدوار المستخدمة في الكود:

- `OWNER`
- `ADMIN`
- `MERCHANT`
- `COURIER`
- `WAREHOUSE_MANAGER`

تطبيق الصلاحيات يتم بطريقتين:

1. **Global route security** داخل `SecurityConfig`
2. **Method-level security** عبر `@PreAuthorize` داخل الـ Controllers

> ملاحظة مهمة: في بعض المسارات يوجد اعتماد على cast مباشر لـ `authentication.getPrincipal()` إلى `User`، بينما مسارات أخرى تعتمد `authentication.getName()` (رقم الهاتف). هذا يفرض اتساقًا ضروريًا في طريقة بناء الـ Principal داخل JWT filter.

---

## 6) الوحدات الوظيفية الأساسية

### 6.1 Authentication
- Login/JWT issuance
- Current user endpoint
- Auth health endpoint

### 6.2 Shipment Lifecycle
- إنشاء شحنة
- استرجاع شحنات مع pagination/sorting
- عمليات مخزن (receive/dispatch/reconcile/inventory/stats)
- طلب إرجاع RTO
- تحديث موقع الكابتن

### 6.3 Manifest Management
- إنشاء مانيفست
- إسناد شحنات للمانيفست
- تحديث حالة المانيفست

### 6.4 Financial & Payouts
- إدارة دفعات merchant/courier
- تغيير حالة الدفعة
- استعراض العناصر المرتبطة بالدفعة

### 6.5 Reports & Dashboards
- تقارير shipments/couriers/merchants/warehouse
- ملخصات وإحصائيات Dashboard حسب الدور

### 6.6 Master Data
- إدارة users/zones/pricing/telemetry settings

### 6.7 Public APIs
- تتبع شحنة
- feedback
- forgot/reset password + OTP
- contact form + office locations

### 6.8 Ops / Admin Utilities
- Audit logs
- Backup create/restore/status
- SMS test/send
- Debug endpoints (development)

---

## 7) قاعدة البيانات (نظرة سريعة)

Entities محورية:

- `User`, `Role`, `UserStatus`
- `Shipment`, `ShipmentStatus`, `ShipmentStatusHistory`
- `ShipmentManifest`
- `Payout`, `PayoutItem`, `PayoutStatus`
- `Zone`, `DeliveryPricing`
- `SystemAuditLog`, `TelemetrySettings`

علاقات مهمة:

- الشحنة ترتبط بـ merchant + zone + status + recipient details
- المانيفست يرتبط بـ courier ويضم مجموعة شحنات
- الـ payout يرتبط بمستخدم وبنود payoutItems وقد يرتبط بالشحنات

---

## 8) حالة التشغيل الحالية (حسب سياق الجلسة)

- `npm install` في `frontend/` نجح.
- `mvn compile` في `twsela/` نجح.
- كان هناك فشل سابق في `npm run dev` و `mvn spring-boot:run` (مطلوب فحص logs إذا أردت).
- نداء login API تم بنجاح من PowerShell.

---

## 9) ملاحظات تقنية مهمة (Observed)

1. **تعارضات محتملة في الأمان/السلوك**
   - `SecurityConfig` يسمح `"/api/auth/**"` بشكل عام، مع استثناء `"/api/auth/me"` كـ authenticated. يلزم التحقق من ترتيب الـ matchers في runtime behavior (عادةً أول matcher يطابق يربح).

2. **Debug endpoints مفتوحة (`/api/debug/**`)**
   - مناسبة للتطوير فقط، وخطرة في الإنتاج لأنها تشمل توليد hash/اختبار كلمات المرور وتعديل passwords.

3. **بعض endpoints تعيد بيانات hardcoded أو placeholder**
   - مثال: أجزاء من dashboard charts والإحصائيات.
   - مثال: `GET /api/shipments/list` يعيد قائمة فارغة placeholder.

4. **اتساق الاستجابة**
   - رغم وجود نمط شائع (`success/message/data`)، بعض endpoints ترجع domain مباشرة أو بنية مختلفة. يمكن توحيدها عبر Response wrapper موحد.

5. **CORS origins ثابتة في SecurityConfig**
   - جيد للتطوير، لكن يفضل externalize بالكامل عبر config في production.

---

## 10) خطة استعادة فهم المشروع بسرعة (Recommended)

1. تشغيل backend وفتح Swagger:
   - `http://localhost:8000/swagger-ui.html`
2. تنفيذ smoke test للـ APIs الرئيسية:
   - Auth → Dashboard → Shipments → Master Data
3. مراجعة frontend flow من:
   - `frontend/src/js/services/api_service.js`
   - `frontend/src/js/services/auth_service.js`
4. تثبيت baseline:
   - Postman Collection
   - Test users مع roles
5. إغلاق endpoints التطوير قبل أي بيئة Production.

---

## 11) ملفات يجب قراءتها أولًا عند العودة للمشروع

- `README.md`
- `twsela/src/main/resources/application.yml`
- `twsela/src/main/java/com/twsela/security/SecurityConfig.java`
- `twsela/src/main/java/com/twsela/web/*.java`
- `frontend/src/js/README.md`
- `frontend/src/js/services/api_service.js`

---

## 12) الخلاصة

المشروع متقدم فعليًا من ناحية البنية ويغطي نطاق تشغيلي واسع (Operations + Finance + Warehouse + Reporting). أقوى نقطة فيه هي تقسيم الأدوار وتكامل الـ APIs. أهم ما يحتاجه عند استئناف العمل هو:

- تثبيت baseline تشغيل واضح
- توحيد تنسيق الاستجابات
- قفل debug endpoints للإنتاج
- مراجعة دقيقة لتفاصيل authorization matcher order

---

إذا أردت، أقدر في الخطوة التالية أعمل لك:
- **Postman Collection جاهزة** من التوثيق الحالي
- أو **OpenAPI YAML يدوي محسّن** مرتب على Modules.
