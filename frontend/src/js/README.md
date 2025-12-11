# Twsela CMS - Unified JavaScript Architecture

## نظرة عامة

تم بناء نظام JavaScript الموحد لنظام Twsela CMS وفقاً لمبدأ DRY (Don't Repeat Yourself) مع فصل واضح للاهتمامات. تم توحيد جميع الخدمات المكررة في ملفات مركزية لتقليل التعقيد وتحسين الصيانة.

## البنية الموحدة

```
src/js/
├── app.js                        # التطبيق الرئيسي الموحد (دمج app.js + app_base.js)
├── services/                      # الخدمات الموحدة
│   ├── auth_service.js           # خدمة المصادقة الموحدة
│   └── api_service.js            # خدمة API الموحدة (80+ مسار)
├── shared/                       # الخدمات المشتركة الموحدة
│   └── utilities.js              # جميع الأدوات الموحدة (دمج utils.js + shared-services.js + notification-manager.js)
└── pages/                        # معالجات الصفحات
    ├── merchant-create-shipment.js  # إنشاء الشحنات
    ├── owner-zones-page.js         # إدارة المناطق
    ├── owner-payouts.js            # إدارة المدفوعات
    └── courier-manifest-page.js    # قائمة عمل السائقين
```

## الخدمات الموحدة

### 1. TwselaApp (app.js) - التطبيق الرئيسي الموحد
- **الوظيفة**: التهيئة الأساسية والـ UI المشترك (دمج app.js + app_base.js)
- **الدوال الأساسية**:
  - `initializePage()`: التحقق من المصادقة والتوجيه
  - `setupGlobalListeners()`: ربط الأحداث العامة
  - `loadUserData()`: تحميل بيانات المستخدم
  - `updateUserInterface()`: تحديث واجهة المستخدم

### 2. AuthService (services/auth_service.js) - خدمة المصادقة الموحدة
- **الوظيفة**: المصادقة والتوجيه (الملف الوحيد للمصادقة)
- **الدوال الأساسية**:
  - `login(credentials)`: تسجيل الدخول
  - `checkAuthStatus()`: التحقق من صحة JWT
  - `redirectToLogin()`: التوجيه لصفحة الدخول
  - `hasRole(role)`: فحص الأدوار
  - `hasPermission(permission)`: فحص الصلاحيات

### 3. ApiService (services/api_service.js) - خدمة API الموحدة
- **الوظيفة**: API الموحد
- **المسارات المدعومة**: 80+ مسار API
- **الفئات**:
  - Authentication (8 مسارات)
  - User Management (7 مسارات)
  - Shipment Management (8 مسارات)
  - Zone Management (5 مسارات)
  - Financial Management (7 مسارات)
  - Manifest Management (5 مسارات)
  - Courier Operations (4 مسارات)
  - Telemetry (2 مسار)
  - Reporting (5 مسارات)
  - Notifications (3 مسارات)
  - Settings (2 مسار)
  - File Upload (2 مسار)

### 4. Utilities (shared/utilities.js) - الخدمات المشتركة الموحدة
- **الوظيفة**: جميع الأدوات المشتركة (دمج utils.js + shared-services.js + notification-manager.js)
- **الفئات الرئيسية**:
  - `NotificationManager`: إدارة الإشعارات الموحدة
  - `UIUtils`: أدوات واجهة المستخدم الموحدة
  - `Utils`: الأدوات العامة الموحدة
- **المميزات**:
  - إدارة الإشعارات المتقدمة
  - تنسيق البيانات والعملات
  - التحقق من صحة البيانات
  - أدوات التطوير المساعدة

## معالجات الصفحات

### 1. MerchantCreateShipmentHandler
- **الملف**: `pages/merchant-create-shipment.js`
- **الوظيفة**: إنشاء الشحنات مع حساب التسعير
- **المميزات**:
  - جلب المناطق تلقائياً
  - حساب سعر التوصيل والـ COD
  - التحقق من صحة البيانات
  - ربط النموذج بـ API

### 2. OwnerZonesPageHandler
- **الملف**: `pages/owner-zones-page.js`
- **الوظيفة**: إدارة المناطق
- **المميزات**:
  - CRUD operations للمناطق
  - البحث والفلترة
  - التصدير
  - التصفح

### 3. OwnerPayoutsHandler
- **الملف**: `pages/owner-payouts.js`
- **الوظيفة**: إدارة المدفوعات
- **المميزات**:
  - عرض طلبات المدفوعات
  - الموافقة/الرفض
  - الإجراءات الجماعية
  - التقارير المالية

### 4. CourierManifestPageHandler
- **الملف**: `pages/courier-manifest-page.js`
- **الوظيفة**: قائمة عمل السائقين
- **المميزات**:
  - تحديث حالة المهام
  - تتبع الموقع
  - إدارة المانيفست
  - التليمتري

## المبادئ المتبعة

### 1. DRY (Don't Repeat Yourself)
- جميع الدوال المشتركة في الخدمات المركزية
- لا يوجد تكرار في الكود
- إعادة استخدام الكود بكفاءة

### 2. Separation of Concerns
- فصل واضح بين HTML, CSS, JavaScript
- فصل الخدمات عن منطق الصفحات
- فصل المصادقة عن منطق الأعمال

### 3. Error Handling
- معالجة شاملة للأخطاء
- رسائل خطأ باللغة العربية
- تسجيل الأخطاء في الكونسول

### 4. Performance
- تحميل الكود عند الحاجة فقط
- استخدام Debouncing للبحث
- تحسين استعلامات API

## الاستخدام

### 1. التهيئة التلقائية
```javascript
// يتم التهيئة تلقائياً عند تحميل الصفحة
document.addEventListener('DOMContentLoaded', () => {
    // TwselaApp يتم تهيئته تلقائياً
    // معالجات الصفحات يتم تهيئتها حسب الصفحة الحالية
});
```

### 2. استخدام الخدمات الموحدة
```javascript
// استخدام AuthService الموحد
const user = authService.getCurrentUser();
const isAuthenticated = authService.isAuthenticated();

// استخدام ApiService الموحد
const response = await apiService.getShipments();
const zones = await apiService.getZones();
```

### 3. استخدام الخدمات المشتركة الموحدة
```javascript
// إظهار الإشعارات (NotificationManager)
notificationManager.success('تم الحفظ بنجاح');
notificationManager.error('حدث خطأ');

// استخدام UIUtils الموحد
UIUtils.showSuccess('تم الحفظ بنجاح');
UIUtils.showError('حدث خطأ');

// تنسيق البيانات
const formattedAmount = UIUtils.formatCurrency(1000);
const formattedDate = UIUtils.formatDate(new Date());

// استخدام Utils الموحد
const isValidEmail = Utils.validateEmail('test@example.com');
const randomString = Utils.generateRandomString(10);
```

## الأمان

### 1. المصادقة
- JWT tokens محفوظة في localStorage
- التحقق من صحة التوكن مع كل طلب
- إعادة التوجيه التلقائي عند انتهاء الجلسة

### 2. التحقق من الصلاحيات
- فحص الأدوار قبل الوصول للصفحات
- التحقق من الصلاحيات قبل تنفيذ العمليات
- حماية المسارات الحساسة

### 3. التحقق من البيانات
- التحقق من صحة البيانات في الواجهة
- تنظيف البيانات قبل الإرسال
- منع حقن الكود الضار

## الدعم

### 1. المتصفحات المدعومة
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

### 2. الميزات المطلوبة
- ES6+ Support
- Fetch API
- Local Storage
- Geolocation API (للسائقين)

## التطوير

### 1. إضافة صفحة جديدة
1. إنشاء ملف في `pages/`
2. إنشاء كلاس معالج
3. ربط الملف في HTML مع المراجع الموحدة
4. إضافة المسارات المطلوبة في ApiService الموحد

### 2. إضافة API جديد
1. إضافة الدالة في ApiService الموحد
2. توثيق المعاملات
3. إضافة معالجة الأخطاء
4. اختبار الوظيفة

### 3. إضافة ميزة UI جديدة
1. إضافة الدالة في UIUtils الموحد (shared/utilities.js)
2. توثيق الاستخدام
3. إضافة الأنماط المطلوبة
4. اختبار الوظيفة

### 4. إضافة أداة مشتركة جديدة
1. إضافة الدالة في Utils الموحد (shared/utilities.js)
2. توثيق الاستخدام
3. إضافة الاختبارات
4. تحديث README

## التحديثات المستقبلية

- [x] توحيد الخدمات المكررة (مكتمل)
- [x] دمج ملفات JavaScript المتشابهة (مكتمل)
- [x] تحسين بنية الملفات (مكتمل)
- [ ] إضافة Service Worker للتخزين المؤقت
- [ ] تحسين أداء التحميل
- [ ] إضافة المزيد من التحقق من الأمان
- [ ] دعم PWA
- [ ] إضافة اختبارات تلقائية
- [ ] تحويل إلى نظام modules ES6
- [ ] إضافة TypeScript support
