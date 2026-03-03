# Sprint 6 — Security & Critical Fixes
**المهام: 30 | حزم العمل: 4**

---

## WP-1: إصلاحات أمان حرجة (P0)

### T-6.01: SmsController — إزالة OTP من الاستجابة
- إزالة `response.put("otp", otp)` من `SmsController.java` L62
- ربط SmsController بـ OtpService بدلاً من توليد OTP محلياً

### T-6.02: SmsController — استبدال Math.random بـ SecureRandom
- استبدال `Math.random()` بـ OtpService.generateOtp()

### T-6.03: PublicController — SecureRandom لتوليد كلمات المرور
- استبدال `java.util.Random` بـ `SecureRandom` في `generateRandomPassword()`
- تحسين تعقيد كلمة المرور (أحرف + أرقام + رموز)

### T-6.04: ShipmentController — SecureRandom لرقم التتبع
- استبدال `Math.random()` بـ `SecureRandom`
- إضافة فحص التكرار في قاعدة البيانات

### T-6.05: OtpService — مقارنة آمنة ضد timing attack
- استبدال `String.equals()` بـ `MessageDigest.isEqual()`

### T-6.06: PublicController — إصلاح forgot-password
- منع إعادة تعيين كلمة المرور بدون OTP
- توحيد مسار reset-password ليكون المسار الوحيد

### T-6.07: AuthController — DTO بدل User entity في login
- إنشاء `LoginResponseDTO` مع الحقول الآمنة فقط
- استبدال `body.put("user", user)` بالـ DTO

## WP-2: إصلاحات نموذج البيانات (P0)

### T-6.08: RecipientDetails — إزالة UNIQUE عن phone
- إزالة `unique = true` من index
- إضافة composite index على (phone + address) بدلاً

### T-6.09: ShipmentController — findOrCreate للـ recipient
- تعديل createShipment ليبحث عن recipient موجود قبل الإنشاء

### T-6.10: entities — equals/hashCode لـ 15 entity
- إضافة equals/hashCode يعتمد على ID لكل entity مفقودة

### T-6.11: entities — toString لـ 7 entities رئيسية
- User, Shipment, Zone, Manifest, RecipientDetails, MerchantDetails, CourierDetails

## WP-3: إصلاحات Frontend حرجة (P0)

### T-6.12: auth_service.js — إصلاح مسارات API خاطئة
- `/api/auth/forgot-password` → `/api/public/send-otp`
- `/api/auth/reset-password` → `/api/public/reset-password`

### T-6.13: auth_service.js — إصلاح network error handling
- `return true` → `return false` عند خطأ الشبكة

### T-6.14: api_service.js — إصلاح ReferenceError في catch blocks
- إصلاح `url` و `options` scope في `verifyToken()` و `getCurrentUser()`

### T-6.15: api_service.js — إصلاح updateSettings HTTP method
- PUT → POST ليتوافق مع SettingsController

### T-6.16-T-6.21: Frontend page crashes — undefined apiService methods
- owner-dashboard-page.js — ربط بـ apiService methods الصحيحة
- owner-reports-couriers-page.js — نفس الإصلاح
- owner-reports-merchants-page.js — نفس الإصلاح
- owner-reports-warehouse-page.js — نفس الإصلاح
- courier-manifest-page.js — نفس الإصلاح
- owner-settings-page.js — تصحيح مسار API

## WP-4: اختبارات Sprint 6

### T-6.22: اختبار OTP generation الآمن
### T-6.23: اختبار tracking number generation
### T-6.24: اختبار login response DTO
### T-6.25: اختبار forgot-password flow
### T-6.26: اختبار RecipientDetails بدون unique constraint violation
### T-6.27-T-6.30: اختبارات SmsController endpoints

---

## معايير القبول
- [ ] صفر Math.random() في كود الأمان
- [ ] OTP لا يُرجع في أي استجابة HTTP
- [ ] Login response لا يحتوي User entity كامل
- [ ] RecipientDetails يقبل نفس الهاتف لشحنات مختلفة
- [ ] كل صفحات Frontend تعمل بدون TypeError
- [ ] جميع الاختبارات السابقة + الجديدة تنجح
- [ ] BUILD SUCCESS
