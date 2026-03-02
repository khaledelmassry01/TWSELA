# Sprint 11 — تنظيف الواجهة الأمامية وإصلاح الأخطاء الحرجة
**المهام: 30 | حزم العمل: 4**

> **الهدف:** القضاء على جميع المشاكل الحرجة المتبقية في Frontend — XSS، كود ميت، أخطاء التهيئة، وتوحيد الأنماط.

---

## WP-1: إزالة الكود الميت وتنظيف البنية (8 مهام)

### T-11.01: حذف جميع ملفات TypeScript الميتة
- حذف كل ملفات `*.ts` في `frontend/src/js/` (~18 ملف، ~3000+ سطر)
- `services/api.ts`, `services/auth.ts`, `services/shipment.ts`, `services/courier.ts`, `services/merchant.ts`, `services/report.ts`, `services/zone.ts`
- `store/index.ts`, `types/index.ts`, `pages/base-page.ts`, `pages/index.ts`
- جميع ملفات `pages/courier/*.ts`, `pages/merchant/*.ts`, `pages/owner/*.ts`, `pages/warehouse/*.ts`
- حذف `tsconfig.json` + `.babelrc` + إزالة TypeScript dependencies من `package.json`

### T-11.02: تنظيف package.json
- إزالة npm dependencies غير المستخدمة: `axios`, `date-fns`, `html-to-docx`, `lodash`, `marked`
- إبقاء `vite` فقط + أي dev dependencies ضرورية

### T-11.03: حذف المجلد المكرر
- إزالة `frontend/frontend/` — مجلد مكرر يحتوي نسخة قديمة من الخطوط

### T-11.04: إزالة الملفات بدون امتداد
- حذف `frontend/owner/merchants` و `frontend/owner/zones` (ملفات بدون `.html`)

### T-11.05: إصلاح `\`n` artifacts في HTML meta tags
- فحص وإصلاح الـ backtick-n الظاهرة في: `merchant/create-shipment.html`, `404.html`, `contact.html`, `settings.html`
- فحص باقي HTML files لنفس المشكلة

### T-11.06: إصلاح الروابط المكسورة
- `settings.html` يشير لـ `/dashboard.html` → تصحيح حسب الدور
- `contact.html` يحمّل `contact.js` غير موجود → إنشاء أو إزالة
- توحيد روابط sidebar بإضافة `.html`

### T-11.07: إصلاح ملف `.htaccess` — تفعيل caching للملفات الثابتة
- تعديل `Cache-Control` ليسمح بـ caching لـ CSS/JS/Fonts مع max-age مناسب
- إبقاء `no-cache` لملفات HTML فقط

### T-11.08: تحديث سنة حقوق النشر
- تغيير `© 2024` إلى `© 2025-2026` في جميع ملفات HTML

---

## WP-2: إصلاح ثغرات XSS وأنماط غير آمنة (8 مهام)

### T-11.09: تطبيق `sanitizeHTML()` في `GlobalUIHandler.js`
- تطبيق `SharedDataUtils.sanitizeHTML()` على جميع نقاط `innerHTML` في:
  - `createShipmentRow()`, `createUserRow()`, `createPayoutRow()`, `createManifestRow()`
  - جميع دوال إنشاء صفوف الجداول

### T-11.10: تطبيق sanitization في صفحات Page Handlers
- `owner-employees-page.js` — sanitize employee names/phones في الجداول
- `owner-shipments-page.js` — sanitize shipping data
- `owner-zones-page.js` — sanitize zone names
- `courier-dashboard-page.js` — sanitize delivery data
- `merchant-dashboard-page.js` — sanitize shipment data

### T-11.11: استبدال `confirm()` و `prompt()` بمكوّنات مخصصة
- `owner-payouts.js` — استبدال `confirm()` للموافقة/الرفض بـ `UIUtils.confirm()`
- `owner-zones-page.js` — استبدال `confirm()` للحذف
- `owner-employees-page.js` — استبدال `confirm()` للحذف
- `courier-dashboard-page.js` — استبدال `confirm()` للتسليم
- إنشاء `UIUtils.prompt()` لحالة "سبب الرفض"

### T-11.12: استبدال `alert()` بـ `UIUtils.showError()/showSuccess()`
- `owner-employees-page.js` — كل `alert()` → `UIUtils.showWarning()`
- `profile.js` — كل `alert()` → `UIUtils.showError()/showSuccess()`

### T-11.13: استبدال inline `onclick` بـ Event Listeners
- `owner-employees-page.js` — `onclick="deleteEmployee(id)"` → `addEventListener`
- `owner-shipments-page.js` — inline handlers → proper delegation
- `courier-dashboard-page.js` — inline handlers → delegation

### T-11.14: إصلاح CSP headers
- تحديث CSP في HTML files لإزالة `unsafe-inline` حيث أمكن
- إضافة nonce-based script loading أو نقل inline scripts لملفات خارجية

### T-11.15: إصلاح localStorage → sessionStorage
- التأكد أن جميع بيانات Auth تستخدم `sessionStorage` فقط
- عدم تخزين JWT tokens في `localStorage`

### T-11.16: إصلاح auth error returns
- `auth_service.js` — `checkAuthStatus()` catch block: `return false` بدل `return true`
- إضافة retry mechanism مع timestamp للحالات المؤقتة

---

## WP-3: توحيد أنماط الكود (7 مهام)

### T-11.17: توحيد نمط الوصول لـ API Service
- توحيد استخدام `window.apiService` في جميع الصفحات
- إزالة أنماط `this.services.api` المتضاربة
- توثيق النمط المعتمد

### T-11.18: توحيد initialization pattern
- إزالة تضارب auth checking:
  - `app.js` 100ms delay check
  - `BasePageHandler` constructor check
  - `owner-zones-page.js` own `waitForAppInitialization()`
- إنشاء نمط initialization واحد واضح

### T-11.19: توحيد phone validation
- إنشاء `ValidationUtils.validatePhone(phone, country)` مشترك
- دعم أرقام مصرية وسعودية
- استبدال الـ regex المتناثرة في 3+ ملفات

### T-11.20: إصلاح "جنيه سعودي" → "ريال سعودي"
- تصحيح في `SharedDataUtils.js` وأي ملفات أخرى

### T-11.21: توحيد Console Logging
- إزالة/تعليق console.log المفصل من `owner-zones-page.js` و `app.js`
- فقط الأخطاء (`console.error`) تبقى في production
- إنشاء `Logger` utility مبسط (debug/info/warn/error) مع flag للبيئة

### T-11.22: إصلاح `ProfilePageHandler` لامتداد `BasePageHandler`
- تحويل `profile.js` لاستخدام `extends BasePageHandler`
- توحيد auth checking

### T-11.23: تنظيف CSS المكرر
- إزالة utility classes المكررة من `twsela-design.css` (المتوفرة حالياً في Bootstrap)
- توحيد CSS variables في ملف واحد `_variables.css`
- إزالة التعريفات المكررة من `styles.css` و `twsela-design.css`

---

## WP-4: إكمال الصفحات الهيكلية (7 مهام)

### T-11.24: إكمال `owner-pricing-page.js` (42 سطر حالياً)
- تطبيق كامل لعرض/إنشاء/تعديل/حذف الأسعار
- ربط مع `/api/pricing` endpoints

### T-11.25: إكمال `owner-reports-couriers-page.js` (39 سطر)
- جدول بيانات المناديب + أداء
- رسوم بيانية Chart.js
- ربط مع `/api/reports/couriers`

### T-11.26: إكمال `owner-reports-merchants-page.js` (39 سطر)
- جدول بيانات التجار + إيرادات
- رسوم بيانية
- ربط مع `/api/reports/merchants`

### T-11.27: إكمال `owner-reports-warehouse-page.js` (39 سطر)
- إحصائيات المستودع + عمليات
- ربط مع `/api/reports/warehouse`

### T-11.28: إكمال `courier-manifest-page.js` (102 سطر stubs)
- عرض بيان الاستلام الحالي
- قائمة الشحنات في البيان
- تحديث حالة الشحنات
- ربط مع `/api/manifests`

### T-11.29: إنشاء `warehouse-dashboard-page.js`
- لوحة تحكم المستودع كاملة
- إحصائيات المخزون الحالي
- شحنات منتظرة + مرسلة
- ربط مع `/api/reports/warehouse` + `/api/shipments`

### T-11.30: إنشاء `admin-dashboard-page.js`
- لوحة تحكم المشرف
- إحصائيات النظام
- آخر سجلات التدقيق
- ربط مع `/api/dashboard` + `/api/audit`

---

## معايير القبول
- [ ] صفر ملفات TypeScript ميتة
- [ ] صفر `alert()`/`confirm()`/`prompt()` أصلية
- [ ] `sanitizeHTML()` مُطبقة على كل إدخال innerHTML ديناميكي
- [ ] جميع الصفحات الهيكلية مكتملة وتعمل مع API
- [ ] صفر `\`n` artifacts في HTML
- [ ] BUILD SUCCESS للـ Backend (123 tests, 0 failures)
