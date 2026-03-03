# TWSELA API Documentation (Arabic)

تاريخ التحديث: 2026-03-03

## 1) معلومات عامة

- **Base URL (Local):** `http://localhost:8000`
- **API Prefix:** أغلب المسارات تحت `/api`
- **Swagger UI:** `/swagger-ui.html`
- **OpenAPI JSON:** `/api-docs` أو `/v3/api-docs`

### Authentication

- النظام يستخدم **JWT Bearer Token**.
- Endpoint تسجيل الدخول يعطي token.
- أرسل التوكن في header:

```http
Authorization: Bearer <JWT_TOKEN>
```

### تنسيق الاستجابات

```json
{
  "success": true,
  "message": "...",
  "data": {}
}
```

### إحصائيات API

| المقياس | القيمة |
|---------|--------|
| إجمالي المتحكمات (Controllers) | 72 |
| إجمالي نقاط API (REST Endpoints) | ~350+ |
| WebSocket Endpoints | 4 |
| Flyway Migrations | V1–V37 |
| الكيانات (Entities) | 110 |
| الخدمات (Services) | 110 |

---

## 2) الأدوار والصلاحيات

الأدوار المستخدمة:

- `OWNER` — مالك المنصة (صلاحيات كاملة)
- `ADMIN` — مسؤول النظام
- `MERCHANT` — تاجر (إنشاء شحنات، تتبع، مدفوعات)
- `COURIER` — مندوب توصيل
- `WAREHOUSE_MANAGER` — مدير مستودع

> ملاحظة: الصلاحيات تُطبّق على مستوى `SecurityConfig` وعلى مستوى `@PreAuthorize` داخل كل endpoint.

---

## 3) DTOs / Payloads شائعة

### 3.1 LoginRequest

```json
{
  "phone": "+201234567890",
  "password": "123456"
}
```

### 3.2 PasswordResetRequest

```json
{
  "phone": "+201234567890",
  "otp": "123456",
  "newPassword": "newpassword123",
  "confirmPassword": "newpassword123"
}
```

---

## 4) Authentication APIs

### 4.1 Login
- **POST** `/api/auth/login`
- **Auth:** Public
- **Body:** `LoginRequest`
- **200:** token + user + role
- **401:** بيانات دخول غير صحيحة

### 4.2 Current User
- **GET** `/api/auth/me`
- **Auth:** Required
- **200:** `User`
- **401/404**

### 4.3 Auth Health
- **GET** `/api/auth/health`
- **Auth:** Public

### 4.4 Logout
- **POST** `/api/auth/logout`
- **Auth:** Required
- **200:**
```json
{
  "success": true,
  "message": "تم تسجيل الخروج بنجاح"
}
```

### 4.5 Change Password
- **POST** `/api/auth/change-password`
- **Auth:** Required
- **Body:**
```json
{
  "currentPassword": "...",
  "newPassword": "...",
  "confirmPassword": "..."
}
```
- **200:**
```json
{
  "success": true,
  "message": "تم تغيير كلمة المرور بنجاح"
}
```

### 4.6 Refresh Token
- **POST** `/api/auth/refresh`
- **Auth:** Required (Bearer Token)
- **200:**
```json
{
  "success": true,
  "token": "<NEW_JWT_TOKEN>"
}
```

---

## 5) Health API

### 5.1 App Health
- **GET** `/api/health`
- **Auth:** Public
- **200:**
```json
{
  "status": "UP",
  "version": "1.0.0",
  "environment": "local",
  "components": {
    "database": "UP",
    "redis": "UP"
  }
}
```

---

## 6) Public APIs

### 6.1 Track Shipment
- **GET** `/api/public/track/{trackingNumber}`
- **Auth:** Public
- **200:** قائمة `ShipmentStatusHistory`

### 6.2 Submit Feedback
- **POST** `/api/public/feedback/{trackingNumber}`
- **Auth:** Public
- **Body:** `ServiceFeedback`

### 6.3 Forgot Password
- **POST** `/api/public/forgot-password`
- **Auth:** Public
- **Body:** `PasswordResetRequest` (phone)

### 6.4 Send OTP
- **POST** `/api/public/send-otp`
- **Auth:** Public
- **Body:** `PasswordResetRequest` (phone)

### 6.5 Reset Password
- **POST** `/api/public/reset-password`
- **Auth:** Public
- **Body:** `PasswordResetRequest`

### 6.6 Contact Form
- **POST** `/api/public/contact`
- **Auth:** Public
- **Body:**
```json
{
  "firstName": "...",
  "lastName": "...",
  "email": "...",
  "subject": "...",
  "message": "..."
}
```

### 6.7 Office Locations
- **GET** `/api/public/contact/offices`
- **Auth:** Public

### 6.8 Public Tracking (Enhanced)
- **GET** `/api/public/tracking/{trackingNumber}` — تتبع محسّن مع تفاصيل كاملة
- **GET** `/api/public/tracking/{trackingNumber}/eta` — تقدير وقت الوصول
- **Auth:** Public

### 6.9 Public Branding
- **GET** `/api/public/branding/{slug}` — تنسيق CSS لعلامة المستأجر التجارية
- **Auth:** Public

### 6.10 Subscription Plans
- **GET** `/api/subscriptions/plans` — عرض خطط الاشتراك المتاحة
- **Auth:** Public

### 6.11 Knowledge Base (Public)
- **GET** `/api/support/articles` — مقالات قاعدة المعرفة
- **GET** `/api/support/articles/search?q=...` — بحث في المقالات
- **GET** `/api/support/articles/{id}` — مقال محدد
- **Auth:** Public

---

## 7) User APIs (`UserController`)

Base: `/api`

### 7.1 Users CRUD
- **GET** `/api/users` — قائمة المستخدمين (OWNER/ADMIN)
- **POST** `/api/users` — إنشاء مستخدم (OWNER/ADMIN)
- **PUT** `/api/users/{id}` — تحديث مستخدم (OWNER/ADMIN)
- **DELETE** `/api/users/{id}` — حذف مستخدم (OWNER/ADMIN)

#### Create user body
```json
{
  "name": "...",
  "phone": "...",
  "password": "...",
  "role": "OWNER|ADMIN|MERCHANT|COURIER|WAREHOUSE_MANAGER",
  "active": true
}
```

### 7.2 Update Profile
- **PUT** `/api/users/profile`
- **Auth:** Required (أي مستخدم مسجّل)
- **Body:**
```json
{
  "name": "...",
  "phone": "...",
  "email": "..."
}
```

### 7.3 Role-based Listings
- **GET** `/api/couriers?page=0&limit=20`
- **GET** `/api/merchants?page=0&limit=20`
- **GET** `/api/employees?page=0&size=10`
- **POST** `/api/employees`

### 7.4 Courier CRUD
- **GET** `/api/couriers/{id}` — جلب بيانات مندوب محدد
- **POST** `/api/couriers` — إنشاء مندوب جديد
- **PUT** `/api/couriers/{id}` — تحديث بيانات مندوب
- **DELETE** `/api/couriers/{id}` — حذف مندوب
- **Auth:** OWNER/ADMIN

### 7.5 Courier Location (Enhanced)
- **POST** `/api/couriers/location` — تسجيل موقع المندوب الحالي (COURIER)
- **GET** `/api/couriers/{id}/location` — جلب آخر موقع للمندوب (OWNER/ADMIN/COURIER)
- **GET** `/api/couriers/{id}/location/history` — سجل مواقع المندوب (OWNER/ADMIN/COURIER)

### 7.6 Merchant CRUD
- **GET** `/api/merchants/{id}` — جلب بيانات تاجر محدد
- **POST** `/api/merchants` — إنشاء تاجر جديد
- **PUT** `/api/merchants/{id}` — تحديث بيانات تاجر
- **Auth:** OWNER/ADMIN

### 7.7 Employee Management
- **GET** `/api/employees/{id}` — جلب بيانات موظف محدد
- **PUT** `/api/employees/{id}` — تحديث بيانات موظف
- **Auth:** OWNER/ADMIN

---

## 8) Shipment APIs (`ShipmentController`)

Base: `/api/shipments`

### 8.1 Core Shipment Operations
- **GET** `/api/shipments` — قائمة الشحنات (page, size, sortBy, sortDir)
- **GET** `/api/shipments/{id}` — تفاصيل شحنة
- **GET** `/api/shipments/count` — عدد الشحنات
- **POST** `/api/shipments` — إنشاء شحنة (OWNER/ADMIN/MERCHANT)
- **GET** `/api/shipments/list` — قائمة مبسّطة

```json
{
  "recipientName": "...",
  "recipientPhone": "...",
  "recipientAddress": "...",
  "packageDescription": "...",
  "packageWeight": 1.5,
  "zoneId": 1,
  "priority": "STANDARD|EXPRESS|ECONOMY",
  "shippingFeePaidBy": "MERCHANT|RECIPIENT|PREPAID",
  "itemValue": 0,
  "codAmount": 0,
  "specialInstructions": "..."
}
```

### 8.2 Warehouse Operations
- **POST** `/api/shipments/warehouse/receive` — استلام شحنات
- **GET** `/api/shipments/warehouse/inventory` — مخزون المستودع
- **POST** `/api/shipments/warehouse/dispatch/{courierId}` — إرسال للمندوب
- **POST** `/api/shipments/warehouse/reconcile/courier/{courierId}` — تسوية المندوب
- **GET** `/api/shipments/warehouse/couriers` — قائمة المناديب
- **GET** `/api/shipments/warehouse/courier/{courierId}/shipments` — شحنات المندوب
- **GET** `/api/shipments/warehouse/stats` — إحصائيات المستودع

### 8.3 Return & Status
- **POST** `/api/shipments/{id}/return-request` — طلب مرتجع
- **PUT** `/api/shipments/courier/location/update` — تحديث موقع المندوب

### 8.4 Labels & Documents
- **GET** `/api/shipments/{id}/label` — تحميل ملصق الشحنة (PDF)
- **POST** `/api/shipments/labels/bulk` — طباعة ملصقات جماعية (PDF)
- **GET** `/api/shipments/{id}/barcode` — باركود الشحنة (PNG)
- **GET** `/api/shipments/{id}/qrcode` — QR Code (PNG)
- **POST** `/api/shipments/{id}/pod` — رفع إثبات التوصيل (multipart)
- **GET** `/api/shipments/{id}/pod` — جلب إثبات التوصيل

### 8.5 Bulk Upload
- **POST** `/api/shipments/bulk` — رفع شحنات من ملف Excel (multipart)
- **GET** `/api/shipments/bulk/template` — تحميل قالب Excel
- **Auth:** OWNER/ADMIN/MERCHANT

---

## 9) Manifest APIs (`ManifestController`)

Base: `/api/manifests`

- **GET** `/api/manifests` — قائمة البيانات
- **POST** `/api/manifests` — إنشاء بيان (`{"courierId": 5}`)
- **GET** `/api/manifests/{manifestId}` — تفاصيل بيان
- **POST** `/api/manifests/{manifestId}/shipments` — إضافة شحنات
- **PUT** `/api/manifests/{manifestId}/status?status=IN_PROGRESS` — تحديث الحالة
- **POST** `/api/manifests/{manifestId}/assign` — تعيين بأرقام التتبع

---

## 10) Master Data APIs (`MasterDataController`)

Base: `/api/master`

### 10.1 Users
- **GET/POST/PUT/DELETE** `/api/master/users` — إدارة المستخدمين

### 10.2 Zones
- **GET/POST/PUT/DELETE** `/api/master/zones` — إدارة المناطق

### 10.3 Pricing
- **GET/POST/PUT/DELETE** `/api/master/pricing` — إدارة الأسعار

### 10.4 Telemetry Settings
- **GET/PUT/DELETE** `/api/master/telemetry` — إعدادات القياس

---

## 11) Dashboard APIs (`DashboardController`)

Base: `/api/dashboard`

- **GET** `/api/dashboard/summary` — ملخص عام
- **GET** `/api/dashboard/statistics` — إحصائيات
- **GET** `/api/dashboard/dashboard-stats` — إحصائيات اللوحة
- **GET** `/api/dashboard/revenue-chart` — مخطط الإيرادات
- **GET** `/api/dashboard/shipments-chart` — مخطط الشحنات

---

## 12) Financial APIs (`FinancialController`)

Base: `/api/financial`

- **GET** `/api/financial/payouts` — قائمة المدفوعات
- **POST** `/api/financial/payouts` — إنشاء عملية صرف
- **GET** `/api/financial/payouts/{payoutId}` — تفاصيل
- **PUT** `/api/financial/payouts/{payoutId}/status?status=PAID` — تحديث الحالة
- **GET** `/api/financial/payouts/pending` — المعلّقة
- **GET** `/api/financial/payouts/user/{userId}` — مدفوعات مستخدم
- **GET** `/api/financial/payouts/{payoutId}/items` — بنود المدفوعات

---

## 13) Reports APIs (`ReportsController`)

Base: `/api/reports`

- **GET** `/api/reports/shipments?startDate=...&endDate=...` — تقرير الشحنات
- **GET** `/api/reports/couriers?startDate=...&endDate=...` — تقرير المناديب
- **GET** `/api/reports/merchants?startDate=...&endDate=...` — تقرير التجار
- **GET** `/api/reports/warehouse?startDate=...&endDate=...` — تقرير المستودع
- **GET** `/api/reports/dashboard` — ملخص لوحة التحكم

### Report Export
- **GET** `/api/reports/export/{reportType}?format=pdf&from=...&to=...&locale=ar`
- **Auth:** OWNER/ADMIN
- **Formats:** PDF, Excel, CSV
- **ReportTypes:** shipments, couriers, merchants, warehouse

---

## 14) Settings APIs (`SettingsController`)

Base: `/api/settings`

- **GET** `/api/settings` — جلب الإعدادات (OWNER/ADMIN)
- **POST** `/api/settings` — حفظ/تحديث الإعدادات
- **POST** `/api/settings/reset` — إعادة التعيين (OWNER)

---

## 15) SMS APIs (`SmsController`)

Base: `/api/sms`

- **POST** `/api/sms/send?phoneNumber=...&message=...`
- **POST** `/api/sms/send-otp?phoneNumber=...`
- **POST** `/api/sms/send-notification?phoneNumber=...&notificationType=...&trackingNumber=...`
- **GET** `/api/sms/test`

---

## 16) Audit APIs (`AuditController`)

Base: `/api/audit`

- **GET** `/api/audit/logs` — سجل التدقيق (params: startDate, endDate, action, userId)
- **GET** `/api/audit/entity/{entityType}/{entityId}` — سجل كيان
- **GET** `/api/audit/user/{userId}` — سجل مستخدم

---

## 17) Backup APIs (`BackupController`)

Base: `/api/backup`

- **POST** `/api/backup/create` — إنشاء نسخة احتياطية
- **POST** `/api/backup/restore?backupFilePath=...` — استعادة
- **GET** `/api/backup/status` — حالة النسخ
- **GET** `/api/backup/test` — اختبار

---

## 18) Notification APIs

Base: `/api/notifications`

### 18.1 User Notifications
- **GET** `/api/notifications` — جلب الإشعارات
- **PUT** `/api/notifications/{id}/read` — تحديد كمقروء
- **PUT** `/api/notifications/read-all` — تحديد الكل كمقروء

### 18.2 Notification Preferences
- **GET** `/api/notifications/preferences` — جلب التفضيلات
- **PUT** `/api/notifications/preferences` — تحديث التفضيلات
- **PUT** `/api/notifications/preferences/pause` — إيقاف مؤقت

### 18.3 Device Tokens (Push Notifications)
- **POST** `/api/notifications/devices` — تسجيل جهاز
- **DELETE** `/api/notifications/devices/{token}` — إلغاء تسجيل

### 18.4 Notification Templates (Admin)
- **GET** `/api/admin/notifications/templates` — قائمة القوالب
- **GET** `/api/admin/notifications/templates/{eventType}` — قوالب حدث
- **PUT** `/api/admin/notifications/templates/{id}` — تحديث قالب
- **POST** `/api/admin/notifications/templates/{id}/test` — اختبار قالب
- **GET** `/api/admin/notifications/analytics` — تحليلات الإشعارات
- **Auth:** OWNER/ADMIN

---

## 19) Telemetry APIs

Base: `/api/telemetry`

- **POST** `/api/telemetry` — إرسال بيانات القياس
- **GET** `/api/telemetry?startDate=...&endDate=...` — جلب البيانات (OWNER/ADMIN)

---

## 20) Analytics APIs (`AnalyticsController`)

Base: `/api/analytics`
**Auth:** OWNER/ADMIN

- **GET** `/api/analytics/revenue?startDate=...&endDate=...` — تقرير الإيرادات
- **GET** `/api/analytics/status-distribution?startDate=...&endDate=...` — توزيع الحالات
- **GET** `/api/analytics/courier-ranking?startDate=...&endDate=...&limit=10` — ترتيب المناديب
- **GET** `/api/analytics/top-merchants?startDate=...&endDate=...&limit=10` — أفضل التجار

---

## 21) BI Dashboard APIs (`BIDashboardController`)

Base: `/api/bi-analytics`
**Auth:** OWNER/ADMIN

- **GET** `/api/bi-analytics/summary?from=...&to=...` — ملخص شامل
- **GET** `/api/bi-analytics/revenue?from=...&to=...&top=10` — تحليل الإيرادات
- **GET** `/api/bi-analytics/operations?from=...&to=...` — تحليل العمليات
- **GET** `/api/bi-analytics/couriers?from=...&to=...&top=10` — أداء المناديب
- **GET** `/api/bi-analytics/merchants?from=...&to=...&top=10` — أداء التجار
- **GET** `/api/bi-analytics/kpi/trends?metric=...&from=...&to=...` — اتجاهات مؤشرات الأداء

---

## 22) Multi-Tenant APIs

### 22.1 Tenant Management (`TenantController`)

Base: `/api/tenants`
**Auth:** OWNER/ADMIN

- **POST** `/api/tenants` — إنشاء مستأجر
- **GET** `/api/tenants?status=...` — قائمة المستأجرين
- **GET** `/api/tenants/{id}` — تفاصيل مستأجر
- **PUT** `/api/tenants/{id}` — تحديث بيانات
- **POST** `/api/tenants/{id}/suspend` — تعليق (OWNER فقط)
- **POST** `/api/tenants/{id}/activate` — تفعيل (OWNER فقط)

### 22.2 Tenant Users (`TenantUserController`)
- **GET** `/api/tenants/{tenantId}/users` — مستخدمو المستأجر
- **POST** `/api/tenants/{tenantId}/invitations` — إرسال دعوة
- **GET** `/api/tenants/{tenantId}/invitations?status=PENDING` — قائمة الدعوات
- **POST** `/api/invitations/{token}/accept` — قبول دعوة (أي مستخدم مسجّل)
- **PUT** `/api/tenants/{tenantId}/users/{userId}/role` — تغيير الدور
- **DELETE** `/api/tenants/{tenantId}/users/{userId}` — إزالة مستخدم

### 22.3 Tenant Quotas (`TenantQuotaController`)

Base: `/api/tenants/{tenantId}/quotas`

- **GET** `/api/tenants/{tenantId}/quotas` — حصص المستأجر (OWNER/ADMIN)
- **PUT** `/api/tenants/{tenantId}/quotas/{quotaType}?maxValue=...` — تعديل الحصة (OWNER)
- **GET** `/api/tenants/{tenantId}/quotas/usage` — استخدام الحصص

### 22.4 Tenant Branding (`TenantBrandingController`)
- **GET** `/api/tenants/{tenantId}/branding` — إعدادات العلامة التجارية
- **PUT** `/api/tenants/{tenantId}/branding` — تحديث العلامة
- **POST** `/api/tenants/{tenantId}/branding/logo?logoUrl=...` — تحديث الشعار

---

## 23) Payment APIs

### 23.1 Payment Intents (`PaymentIntentController`)

Base: `/api/payments`

- **POST** `/api/payments/intents` — إنشاء نية دفع (MERCHANT/OWNER/ADMIN)
- **GET** `/api/payments/intents/{id}` — تفاصيل نية الدفع
- **POST** `/api/payments/intents/{id}/confirm` — تأكيد الدفع
- **POST** `/api/payments/intents/{id}/cancel` — إلغاء الدفع
- **GET** `/api/payments/methods` — طرق الدفع المحفوظة
- **POST** `/api/payments/methods` — إضافة طريقة دفع
- **DELETE** `/api/payments/methods/{id}` — حذف طريقة دفع

### 23.2 Refunds (`PaymentRefundController`)

Base: `/api/payments/refunds`

- **POST** `/api/payments/refunds` — طلب استرداد (MERCHANT/OWNER/ADMIN)
- **GET** `/api/payments/refunds/{id}` — تفاصيل الاسترداد
- **GET** `/api/payments/refunds` — قائمة الاستردادات (OWNER/ADMIN)
- **POST** `/api/payments/refunds/{id}/approve` — موافقة (OWNER/ADMIN)
- **POST** `/api/payments/refunds/{id}/reject?reason=...` — رفض (OWNER/ADMIN)

### 23.3 Payment Callbacks (Webhooks)

Base: `/api/payments/callback`
**Auth:** Public (signature verification)

- **POST** `/api/payments/callback/paymob` — Paymob webhook
- **POST** `/api/payments/callback/tap` — Tap webhook
- **POST** `/api/payments/callback/stripe` — Stripe webhook
- **POST** `/api/payments/callback/fawry` — Fawry webhook

---

## 24) Wallet APIs (`WalletController`)

Base: `/api/wallet`

- **GET** `/api/wallet` — تفاصيل المحفظة
- **GET** `/api/wallet/balance` — الرصيد
- **GET** `/api/wallet/transactions?page=0&size=20` — حركات المحفظة
- **POST** `/api/wallet/withdraw` — طلب سحب (`{"amount": 500}`)
- **GET** `/api/wallet/admin/all` — جميع المحافظ (OWNER/ADMIN)

---

## 25) Invoice APIs (`InvoiceController`)

Base: `/api/invoices`

- **GET** `/api/invoices?page=0&size=20` — فواتيري (MERCHANT)
- **GET** `/api/invoices/{id}` — تفاصيل فاتورة
- **POST** `/api/invoices/{id}/pay` — دفع الفاتورة (MERCHANT)
- **GET** `/api/invoices/admin?status=...` — فواتير الإدارة (OWNER/ADMIN)
- **POST** `/api/invoices/admin/{id}/refund` — استرداد (OWNER)

### E-Invoice
- **POST** `/api/admin/einvoice/generate/{invoiceId}?countryCode=...` — توليد فاتورة إلكترونية
- **GET** `/api/admin/einvoice/{id}` — تفاصيل الفاتورة الإلكترونية
- **POST** `/api/admin/einvoice/{id}/submit` — تقديم للجهة الضريبية
- **GET** `/api/admin/einvoice/pending` — فواتير بانتظار التقديم

---

## 26) Settlement APIs (`SettlementController`)

Base: `/api/settlements`
**Auth:** OWNER/ADMIN

- **GET** `/api/settlements?status=DRAFT` — قائمة التسويات
- **GET** `/api/settlements/{id}` — تفاصيل التسوية
- **POST** `/api/settlements/generate` — توليد تسوية
```json
{
  "period": "...",
  "startDate": "2026-03-01",
  "endDate": "2026-03-15"
}
```
- **GET** `/api/settlements/{id}/items` — بنود التسوية
- **POST** `/api/settlements/{id}/process` — تنفيذ التسوية

---

## 27) Subscription APIs (`SubscriptionController`)

Base: `/api/subscriptions`

- **GET** `/api/subscriptions/plans` — الخطط المتاحة (Public)
- **POST** `/api/subscriptions` — اشتراك في خطة (MERCHANT)
- **GET** `/api/subscriptions/my` — اشتراكي الحالي (MERCHANT)
- **PUT** `/api/subscriptions/upgrade` — ترقية الخطة
- **PUT** `/api/subscriptions/downgrade` — تخفيض الخطة
- **PUT** `/api/subscriptions/cancel` — إلغاء الاشتراك
- **GET** `/api/subscriptions/usage` — استخدام الخطة

---

## 28) E-Commerce Integration APIs

### 28.1 Store Connections (`ECommerceController`)

Base: `/api/integrations`
**Auth:** MERCHANT/OWNER

- **POST** `/api/integrations/connect` — ربط متجر
```json
{
  "platform": "SHOPIFY|WOOCOMMERCE|SALLA|ZID",
  "storeUrl": "...",
  "storeName": "...",
  "accessToken": "...",
  "webhookSecret": "...",
  "defaultZoneId": 1
}
```
- **GET** `/api/integrations/connections` — المتاجر المربوطة
- **DELETE** `/api/integrations/{id}` — فصل متجر
- **GET** `/api/integrations/{id}/orders` — طلبات المتجر
- **POST** `/api/integrations/{id}/retry` — إعادة المحاولة
- **GET** `/api/integrations/{id}/stats` — إحصائيات

### 28.2 E-Commerce Webhooks

Base: `/api/ecommerce/webhook`
**Auth:** Public (signature verification)

- **POST** `/api/ecommerce/webhook/shopify/{connectionId}` — Shopify
- **POST** `/api/ecommerce/webhook/woocommerce/{connectionId}` — WooCommerce
- **POST** `/api/ecommerce/webhook/salla/{connectionId}` — Salla
- **POST** `/api/ecommerce/webhook/zid/{connectionId}` — Zid

---

## 29) Fleet Management APIs (`FleetController`)

Base: `/api/fleet`
**Auth:** OWNER/ADMIN (+ COURIER for fuel/return)

### 29.1 Vehicles
- **POST** `/api/fleet/vehicles` — إضافة مركبة
- **GET** `/api/fleet/vehicles?status=...` — قائمة المركبات
- **GET** `/api/fleet/vehicles/{id}` — تفاصيل مركبة
- **GET** `/api/fleet/vehicles/available?type=...` — المركبات المتاحة
- **PUT** `/api/fleet/vehicles/{id}/retire` — إيقاف مركبة

### 29.2 Assignments
- **POST** `/api/fleet/assignments` — تعيين مركبة لمندوب
- **PUT** `/api/fleet/assignments/{id}/return` — إرجاع مركبة

### 29.3 Maintenance
- **POST** `/api/fleet/maintenance` — جدولة صيانة
- **PUT** `/api/fleet/maintenance/{id}/complete?cost=...` — إنهاء صيانة
- **GET** `/api/fleet/vehicles/{vehicleId}/maintenance` — سجل صيانة

### 29.4 Fuel
- **POST** `/api/fleet/fuel` — تسجيل تعبئة وقود
- **GET** `/api/fleet/vehicles/{vehicleId}/fuel` — سجل الوقود

---

## 30) Delivery Management APIs

### 30.1 Delivery Proof & Attempts (`DeliveryController`)

Base: `/api/delivery`

- **POST** `/api/delivery/{shipmentId}/proof` — رفع إثبات التوصيل (COURIER, multipart)
- **GET** `/api/delivery/{shipmentId}/proof` — جلب الإثبات
- **POST** `/api/delivery/{shipmentId}/attempt` — تسجيل محاولة توصيل (COURIER)
- **GET** `/api/delivery/{shipmentId}/attempts` — محاولات التوصيل
- **GET** `/api/delivery/admin/failures?from=...&to=...` — تقرير الفشل (OWNER/ADMIN)

### 30.2 Returns (`ReturnController`)

Base: `/api/returns`

- **POST** `/api/returns` — طلب مرتجع (OWNER/ADMIN/MERCHANT)
- **GET** `/api/returns` — قائمة المرتجعات
- **GET** `/api/returns/{id}` — تفاصيل مرتجع
- **PUT** `/api/returns/{id}/status` — تحديث الحالة
- **PUT** `/api/returns/{id}/assign` — تعيين مندوب (OWNER/ADMIN)

### 30.3 Pickup Scheduling (`PickupScheduleController`)

Base: `/api/pickups`

- **POST** `/api/pickups` — جدولة استلام (MERCHANT)
- **GET** `/api/pickups/my?page=0&size=20` — طلبات الاستلام (MERCHANT)
- **GET** `/api/pickups/today` — استلامات اليوم (COURIER)
- **PUT** `/api/pickups/{id}/assign/{courierId}` — تعيين مندوب (OWNER/ADMIN)
- **PUT** `/api/pickups/{id}/start` — بدء الاستلام (COURIER)
- **PUT** `/api/pickups/{id}/complete` — إكمال (COURIER)
- **PUT** `/api/pickups/{id}/cancel` — إلغاء
- **GET** `/api/pickups/admin?status=...&page=0&size=20` — إدارة (OWNER/ADMIN)
- **GET** `/api/pickups/admin/overdue` — المتأخرة

---

## 31) Live Tracking & Location APIs

### 31.1 Tracking Sessions (`LiveTrackingController`)

Base: `/api/tracking`

- **POST** `/api/tracking/sessions/start` — بدء جلسة تتبع (COURIER)
- **POST** `/api/tracking/sessions/{sessionId}/pause` — إيقاف مؤقت
- **POST** `/api/tracking/sessions/{sessionId}/resume` — استئناف
- **POST** `/api/tracking/sessions/{sessionId}/end` — إنهاء
- **POST** `/api/tracking/ping` — إرسال نبضة موقع (COURIER)
- **GET** `/api/tracking/sessions/shipment/{shipmentId}` — جلسات شحنة
- **GET** `/api/tracking/sessions/courier` — جلسات المندوب
- **GET** `/api/tracking/sessions/{sessionId}/pings?limit=10` — نبضات الموقع

### 31.2 Live Notifications (`LiveNotificationController`)

Base: `/api/live-notifications`

- **GET** `/api/live-notifications/unread` — إشعارات غير مقروءة
- **GET** `/api/live-notifications/unread/count` — عدد غير المقروء
- **GET** `/api/live-notifications` — جميع الإشعارات
- **POST** `/api/live-notifications/{notificationId}/read` — تحديد كمقروء
- **POST** `/api/live-notifications/read-all` — تحديد الكل
- **GET** `/api/live-notifications/presence/online` — المستخدمون المتصلون
- **GET** `/api/live-notifications/presence/{userId}` — حالة مستخدم

---

## 32) Smart Assignment & Route Optimization APIs

### 32.1 Smart Assignment (`SmartAssignmentController`)

Base: `/api/assignment`
**Auth:** OWNER/ADMIN

- **GET** `/api/assignment/suggest/{shipmentId}` — اقتراح مندوب مناسب
- **GET** `/api/assignment/score/{shipmentId}` — تفصيل النقاط
- **GET** `/api/assignment/rules` — قواعد التوزيع
- **PUT** `/api/assignment/rules` — تحديث قاعدة

### 32.2 Route Optimization (`RouteController`)

Base: `/api/routes`
**Auth:** OWNER/ADMIN

- **POST** `/api/routes/optimize/{courierId}` — تحسين مسار المندوب
- **GET** `/api/routes/{courierId}` — المسار الحالي

### 32.3 Demand Prediction (`DemandController`)

Base: `/api/demand`
**Auth:** OWNER/ADMIN

- **GET** `/api/demand/predict?zoneId=...&date=...` — توقع الطلب
- **GET** `/api/demand/courier-need?zoneId=...&date=...` — الحاجة للمناديب
- **GET** `/api/demand/patterns?zoneId=...` — أنماط الطلب

---

## 33) Contract & Pricing APIs

### 33.1 Contract Management (`ContractController`)

Base: `/api/admin/contracts` (OWNER/ADMIN)

- **POST** `/api/admin/contracts` — إنشاء عقد
- **GET** `/api/admin/contracts` — قائمة العقود
- **GET** `/api/admin/contracts/{id}` — تفاصيل عقد
- **PUT** `/api/admin/contracts/{id}` — تحديث عقد
- **POST** `/api/admin/contracts/{id}/send-signature` — إرسال للتوقيع
- **PUT** `/api/admin/contracts/{id}/terminate` — إنهاء عقد
- **GET** `/api/admin/contracts/expiring?days=30` — عقود تنتهي قريبًا

#### Merchant Contract Actions
- **POST** `/api/contracts/{id}/sign` — توقيع العقد (أي مستخدم مسجّل)
- **GET** `/api/contracts/my` — عقودي

### 33.2 Contract Pricing (`ContractPricingController`)
- **POST** `/api/admin/contracts/{contractId}/pricing` — إضافة قاعدة تسعير
- **GET** `/api/admin/contracts/{contractId}/pricing` — قواعد التسعير
- **PUT** `/api/admin/contracts/pricing/{ruleId}` — تحديث قاعدة
- **GET** `/api/pricing/calculate?merchantId=...&zoneFromId=...&zoneToId=...&weightKg=1.0&codAmount=...` — حساب السعر

### 33.3 SLA Management (`ContractSlaController`)
- **GET** `/api/admin/contracts/{contractId}/sla` — شروط SLA
- **PUT** `/api/admin/contracts/{contractId}/sla` — تحديث SLA
- **GET** `/api/admin/contracts/{contractId}/sla/compliance?from=...&to=...` — امتثال SLA
- **GET** `/api/admin/contracts/{contractId}/sla/penalties?from=...&to=...` — غرامات

---

## 34) Support & Knowledge Base APIs (`SupportController`)

Base: `/api/support`

### 34.1 Tickets
- **POST** `/api/support/tickets` — إنشاء تذكرة (أي مستخدم)
- **GET** `/api/support/tickets/my?page=0&size=20` — تذاكري
- **GET** `/api/support/tickets/{id}` — تفاصيل تذكرة
- **GET** `/api/support/tickets/{id}/messages` — رسائل التذكرة
- **POST** `/api/support/tickets/{id}/messages` — إضافة رسالة

### 34.2 Ticket Administration
- **PUT** `/api/support/tickets/{id}/assign/{assigneeId}` — تعيين (OWNER/ADMIN)
- **PUT** `/api/support/tickets/{id}/resolve` — حل (OWNER/ADMIN)
- **PUT** `/api/support/tickets/{id}/close` — إغلاق (OWNER/ADMIN)
- **GET** `/api/support/tickets/admin?status=...&page=0&size=20` — إدارة

### 34.3 Knowledge Base
- **POST** `/api/support/articles` — إنشاء مقال (OWNER/ADMIN)
- **PUT** `/api/support/articles/{id}/publish` — نشر مقال (OWNER/ADMIN)
- **GET** `/api/support/articles?page=0&size=20` — مقالات (Public)
- **GET** `/api/support/articles/search?q=...` — بحث (Public)
- **GET** `/api/support/articles/{id}` — مقال (Public)

---

## 35) Rating APIs (`RatingController`)

Base: `/api/ratings`

- **POST** `/api/ratings` — تقييم مندوب (OWNER/ADMIN/MERCHANT)
- **GET** `/api/ratings/courier/{courierId}` — تقييمات مندوب (OWNER/ADMIN)
- **GET** `/api/ratings/shipment/{shipmentId}` — تقييم شحنة

---

## 36) Chat APIs (`ChatController`)

Base: `/api/chat`
**Auth:** Required (أي مستخدم مسجّل)

- **POST** `/api/chat/rooms` — إنشاء غرفة محادثة
- **POST** `/api/chat/messages` — إرسال رسالة
- **GET** `/api/chat/rooms/{roomId}/messages` — رسائل الغرفة
- **GET** `/api/chat/rooms/shipment/{shipmentId}` — غرف شحنة
- **GET** `/api/chat/rooms/my` — غرفي
- **POST** `/api/chat/rooms/{roomId}/archive` — أرشفة

---

## 37) API Key Management (`ApiKeyController`)

Base: `/api/developer/keys`
**Auth:** MERCHANT/OWNER

- **POST** `/api/developer/keys` — إنشاء مفتاح API
- **GET** `/api/developer/keys` — مفاتيحي
- **PUT** `/api/developer/keys/{id}/rotate` — تدوير المفتاح
- **DELETE** `/api/developer/keys/{id}` — حذف
- **GET** `/api/developer/keys/{id}/usage?days=30` — استخدام المفتاح

---

## 38) Webhook APIs (`WebhookController`)

Base: `/api/webhooks`
**Auth:** OWNER/ADMIN/MERCHANT

- **POST** `/api/webhooks` — إنشاء webhook
- **GET** `/api/webhooks` — قائمة webhooks
- **GET** `/api/webhooks/{id}` — تفاصيل
- **DELETE** `/api/webhooks/{id}` — حذف
- **GET** `/api/webhooks/{id}/events?page=0&size=20` — أحداث webhook
- **POST** `/api/webhooks/{id}/test` — اختبار
- **POST** `/api/webhooks/retry` — إعادة المحاولة (OWNER/ADMIN)

---

## 39) Event System APIs

### 39.1 Domain Events (`EventController`)

Base: `/api/events`
**Auth:** OWNER/ADMIN

- **GET** `/api/events?eventType=...` — قائمة الأحداث
- **GET** `/api/events/{eventId}` — تفاصيل حدث
- **GET** `/api/events/subscriptions` — اشتراكات الأحداث
- **POST** `/api/events/subscriptions` — إنشاء اشتراك
- **PUT** `/api/events/subscriptions/{id}` — تحديث

### 39.2 Dead Letter Queue (`DeadLetterController`)

Base: `/api/events/dead-letter`
**Auth:** OWNER/ADMIN

- **GET** `/api/events/dead-letter` — الأحداث الفاشلة
- **POST** `/api/events/dead-letter/{id}/retry` — إعادة محاولة
- **POST** `/api/events/dead-letter/{id}/resolve` — حل
- **GET** `/api/events/dead-letter/stats` — إحصائيات

### 39.3 Async Jobs (`AsyncJobController`)

Base: `/api/jobs`
**Auth:** OWNER/ADMIN

- **GET** `/api/jobs?statuses=...` — قائمة المهام
- **POST** `/api/jobs` — إنشاء مهمة
- **GET** `/api/jobs/{jobId}` — تفاصيل
- **POST** `/api/jobs/{jobId}/cancel` — إلغاء
- **GET** `/api/jobs/stats` — إحصائيات

---

## 40) Security APIs

### 40.1 Security Events (`SecurityEventController`)

Base: `/api/security`
**Auth:** OWNER/ADMIN

- **GET** `/api/security/events?userId=...` — سجل الأحداث الأمنية
- **GET** `/api/security/events/summary` — ملخص أمني
- **GET** `/api/security/events/threats` — التهديدات
- **GET** `/api/security/lockouts` — الحسابات المقفلة
- **POST** `/api/security/lockouts/{userId}/unlock` — فتح حساب
- **GET** `/api/security/audit` — تدقيق أمني

### 40.2 IP Management (`IpManagementController`)

Base: `/api/security/ip-blacklist`
**Auth:** OWNER/ADMIN

- **GET** `/api/security/ip-blacklist` — قائمة IPs المحظورة
- **POST** `/api/security/ip-blacklist` — حظر IP
- **DELETE** `/api/security/ip-blacklist/{id}` — إلغاء الحظر

### 40.3 Compliance (`ComplianceController`)

Base: `/api/compliance`
**Auth:** OWNER/ADMIN

- **GET** `/api/compliance/rules` — قواعد الامتثال
- **POST** `/api/compliance/check` — فحص الامتثال
- **GET** `/api/compliance/reports/{id}` — تقرير امتثال
- **GET** `/api/compliance/status` — حالة الامتثال
- **GET** `/api/compliance/reports` — تقارير الامتثال

---

## 41) Internationalization APIs

### 41.1 Countries (`CountryController`)
- **GET** `/api/countries` — قائمة الدول
- **GET** `/api/countries/{code}` — تفاصيل دولة
- **POST** `/api/admin/countries` — إضافة دولة
- **PUT** `/api/admin/countries/{code}` — تحديث
- **PATCH** `/api/admin/countries/{code}/toggle?active=true` — تفعيل/تعطيل

### 41.2 Currency (`CurrencyController`)
- **GET** `/api/currencies` — قائمة العملات
- **GET** `/api/currencies/convert?amount=...&from=...&to=...` — تحويل عملات
- **GET** `/api/currencies/rate?base=...&target=...` — سعر الصرف
- **PUT** `/api/admin/currencies/exchange-rate` — تحديث سعر الصرف

### 41.3 Tax (`TaxController`)
- **GET** `/api/tax/calculate?amount=...&countryCode=...` — حساب الضريبة
- **GET** `/api/admin/tax/rules` — قواعد ضريبية
- **GET** `/api/admin/tax/rules/{countryCode}` — قواعد دولة
- **POST** `/api/admin/tax/rules` — إضافة قاعدة
- **PUT** `/api/admin/tax/rules/{id}` — تحديث

---

## 42) Workflow & Automation APIs (Sprint 36)

### 42.1 Workflow Definitions (`WorkflowDefinitionController`)

Base: `/api/workflows`
**Auth:** OWNER/ADMIN

- **POST** `/api/workflows` — إنشاء سلسلة عمل
- **GET** `/api/workflows` — قائمة سلاسل العمل
- **GET** `/api/workflows/{id}` — تفاصيل سلسلة
- **PUT** `/api/workflows/{id}` — تحديث
- **PATCH** `/api/workflows/{id}/activate` — تفعيل
- **PATCH** `/api/workflows/{id}/deactivate` — تعطيل
- **DELETE** `/api/workflows/{id}` — حذف
- **GET** `/api/workflows/{id}/steps` — خطوات السلسلة

```json
{
  "name": "إشعار عند تأخر التوصيل",
  "description": "...",
  "triggerEvent": "DELIVERY_FAILED",
  "priority": 1,
  "steps": [
    {
      "stepOrder": 1,
      "stepType": "CONDITION",
      "configuration": "{\"field\": \"attemptCount\", \"operator\": \"gte\", \"value\": 2}"
    },
    {
      "stepOrder": 2,
      "stepType": "ACTION",
      "configuration": "{\"action\": \"SEND_NOTIFICATION\", \"target\": \"merchant\"}"
    }
  ]
}
```

### 42.2 Workflow Executions (`WorkflowExecutionController`)

Base: `/api/workflow-executions`
**Auth:** OWNER/ADMIN

- **GET** `/api/workflow-executions/{id}` — تفاصيل التنفيذ
- **GET** `/api/workflow-executions?status=RUNNING` — قائمة التنفيذات
- **GET** `/api/workflow-executions/by-definition/{definitionId}` — تنفيذات سلسلة
- **PATCH** `/api/workflow-executions/{id}/cancel` — إلغاء
- **PATCH** `/api/workflow-executions/{id}/pause` — إيقاف
- **GET** `/api/workflow-executions/{id}/steps` — خطوات التنفيذ

### 42.3 Automation Rules (`AutomationRuleController`)

Base: `/api/automation-rules`
**Auth:** OWNER/ADMIN

- **POST** `/api/automation-rules` — إنشاء قاعدة
- **GET** `/api/automation-rules` — قائمة القواعد
- **GET** `/api/automation-rules/{id}` — تفاصيل
- **PUT** `/api/automation-rules/{id}` — تحديث
- **PATCH** `/api/automation-rules/{id}/activate` — تفعيل
- **PATCH** `/api/automation-rules/{id}/deactivate` — تعطيل
- **DELETE** `/api/automation-rules/{id}` — حذف

```json
{
  "name": "تعيين مندوب تلقائي",
  "description": "...",
  "triggerEvent": "SHIPMENT_CREATED",
  "conditionExpression": "{\"field\": \"priority\", \"equals\": \"EXPRESS\"}",
  "actionType": "ASSIGN_COURIER",
  "actionConfig": "{\"strategy\": \"nearest\"}"
}
```

### 42.4 Scheduled Tasks (`ScheduledTaskController`)

Base: `/api/scheduled-tasks`
**Auth:** OWNER/ADMIN

- **POST** `/api/scheduled-tasks` — إنشاء مهمة مجدولة
- **GET** `/api/scheduled-tasks` — قائمة المهام
- **GET** `/api/scheduled-tasks/{id}` — تفاصيل
- **PUT** `/api/scheduled-tasks/{id}` — تحديث
- **PATCH** `/api/scheduled-tasks/{id}/activate` — تفعيل
- **PATCH** `/api/scheduled-tasks/{id}/deactivate` — تعطيل
- **DELETE** `/api/scheduled-tasks/{id}` — حذف

```json
{
  "name": "تقرير يومي",
  "description": "...",
  "taskType": "GENERATE_REPORT",
  "cronExpression": "0 8 * * *",
  "configuration": "{\"reportType\": \"daily_summary\", \"recipients\": [\"owner\"]}"
}
```

---

## 43) WebSocket / STOMP APIs

### 43.1 WebSocket Connection
- **Endpoint:** `ws://localhost:8000/ws` (STOMP)
- **Broker prefixes:** `/topic`, `/queue`
- **App prefix:** `/app`

### 43.2 STOMP Destinations

| Destination | Payload | الوصف |
|------------|---------|-------|
| `/app/tracking.ping` | `{sessionId, lat, lng, accuracy, speed, heading, batteryLevel}` | إرسال نبضة GPS |
| `/app/chat.send` | `{roomId, senderId, content, messageType}` | إرسال رسالة محادثة |
| `/app/presence.connect` | `{userId}` | تسجيل الاتصال |
| `/app/presence.disconnect` | `{userId}` | تسجيل قطع الاتصال |

### 43.3 Subscription Topics
- `/topic/tracking/{shipmentId}` — تحديثات تتبع الشحنة
- `/topic/notifications/{userId}` — إشعارات المستخدم
- `/queue/chat/{roomId}` — رسائل المحادثة
- `/topic/presence` — حالات الاتصال

---

## 44) أكواد الأخطاء المتوقعة

| الكود | الوصف |
|-------|-------|
| `200` | نجاح |
| `201` | تم الإنشاء بنجاح |
| `400` | بيانات ناقصة/غير صحيحة |
| `401` | غير مصادق (token مفقود أو منتهي) |
| `403` | غير مصرح بالدور الحالي |
| `404` | مورد غير موجود |
| `409` | تعارض (مثل: بيانات مكررة) |
| `429` | تجاوز حد المعدل (Rate Limit) |
| `500` | خطأ داخلي في الخادم |

---

## 45) أمثلة عملية سريعة

### Login

```bash
curl -X POST "http://localhost:8000/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"phone":"01023782584","password":"150620KkZz@#$"}'
```

### Get Current User

```bash
curl "http://localhost:8000/api/auth/me" \
  -H "Authorization: Bearer <TOKEN>"
```

### Create Shipment

```bash
curl -X POST "http://localhost:8000/api/shipments" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "recipientName":"محمد أحمد",
    "recipientPhone":"01000000000",
    "recipientAddress":"القاهرة",
    "packageDescription":"مستندات",
    "packageWeight":1.2,
    "zoneId":1,
    "priority":"STANDARD",
    "shippingFeePaidBy":"MERCHANT",
    "itemValue":200,
    "codAmount":200
  }'
```

### Create Workflow

```bash
curl -X POST "http://localhost:8000/api/workflows" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "name":"إشعار التأخير",
    "triggerEvent":"DELIVERY_FAILED",
    "priority":1,
    "steps":[{"stepOrder":1,"stepType":"ACTION","configuration":"{\"action\":\"SEND_SMS\"}"}]
  }'
```

### Connect E-Commerce Store

```bash
curl -X POST "http://localhost:8000/api/integrations/connect" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "platform":"SHOPIFY",
    "storeUrl":"https://mystore.myshopify.com",
    "storeName":"متجري",
    "accessToken":"shpat_xxx",
    "webhookSecret":"whsec_xxx",
    "defaultZoneId":1
  }'
```

### Create Payment Intent

```bash
curl -X POST "http://localhost:8000/api/payments/intents" \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount":500.00,
    "currency":"EGP",
    "gateway":"PAYMOB",
    "shipmentId":123
  }'
```

---

## 46) ملخص نقاط API حسب المجال

| المجال | عدد Endpoints (تقريبي) |
|--------|----------------------|
| Authentication & Security | ~25 |
| Users, Couriers & Merchants | ~20 |
| Shipments & Labels | ~25 |
| Manifests | ~6 |
| Warehouse & Inventory | ~12 |
| Delivery & Returns & Pickups | ~25 |
| Tracking & Location | ~15 |
| Payments & Wallet & Invoices | ~25 |
| Settlements & Financial | ~15 |
| Notifications & Chat | ~20 |
| Subscriptions & API Keys | ~12 |
| E-Commerce Integrations | ~10 |
| Fleet Management | ~12 |
| Contracts & Pricing & SLA | ~15 |
| Analytics & BI & Reports | ~20 |
| Workflow & Automation | ~25 |
| Events & Jobs & Webhooks | ~20 |
| Multi-Tenant | ~15 |
| Settings & Master Data & Config | ~15 |
| Internationalization (Country/Currency/Tax) | ~12 |
| Support & Knowledge Base | ~15 |
| Compliance & Audit | ~10 |
| **الإجمالي** | **~350+** |

---

## 47) ملاحظات مهمة

1. جميع endpoints تحت `/api/**` تتطلب مصادقة JWT ما لم يُذكر أنها Public
2. DebugController تم إزالته من بيئة الإنتاج
3. Payment Callbacks و E-Commerce Webhooks عامة لكنها تتحقق من التوقيعات
4. WebSocket يستخدم STOMP protocol مع مصادقة JWT
5. الاستجابات API موحدة عبر `ApiResponse<T>` wrapper
6. جميع timestamps بتنسيق ISO 8601
7. Pagination تستخدم `page` (0-based) و `size` كمعاملات استعلام
8. النظام يدعم Multi-Tenant مع عزل كامل للبيانات
