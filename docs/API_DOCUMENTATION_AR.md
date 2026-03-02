# TWSELA API Documentation (Arabic)

تاريخ التحديث: 2026-02-28

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

لا يوجد Wrapper واحد في كل المسارات، لكن الأكثر شيوعًا:

```json
{
  "success": true,
  "message": "...",
  "data": {}
}
```

---

## 2) الأدوار والصلاحيات

الأدوار المستخدمة:

- `OWNER`
- `ADMIN`
- `MERCHANT`
- `COURIER`
- `WAREHOUSE_MANAGER`

> ملاحظة: الصلاحيات قد تُطبّق على مستوى `SecurityConfig` وعلى مستوى `@PreAuthorize` داخل كل endpoint.

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
- **وصف:** تسجيل خروج المستخدم الحالي وإبطال التوكن
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
- **400:** كلمة المرور الحالية غير صحيحة أو كلمة المرور الجديدة لا تتطابق

### 4.6 Refresh Token
- **POST** `/api/auth/refresh`
- **Auth:** Required (Bearer Token)
- **وصف:** تجديد التوكن قبل انتهاء صلاحيته
- **200:**
```json
{
  "success": true,
  "token": "<NEW_JWT_TOKEN>"
}
```
- **401:** التوكن منتهي الصلاحية أو غير صالح

---

## 5) Health API

### 5.1 App Health
- **GET** `/api/health`
- **Auth:** Public
- **وصف:** فحص صحة التطبيق مع حالة قاعدة البيانات و Redis (محدّث في Sprint 10)
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

---

## 7) User APIs (`UserController`)

Base: `/api`

### 7.1 Users CRUD
- **GET** `/api/users` (OWNER/ADMIN)
- **POST** `/api/users` (OWNER/ADMIN)
- **PUT** `/api/users/{id}` (OWNER/ADMIN)
- **DELETE** `/api/users/{id}` (OWNER/ADMIN)

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
- **وصف:** تحديث بيانات الملف الشخصي للمستخدم الحالي
- **Body:**
```json
{
  "name": "...",
  "phone": "...",
  "email": "..."
}
```
- **200:**
```json
{
  "success": true,
  "message": "تم تحديث الملف الشخصي",
  "data": { "id": 1, "name": "...", "phone": "..." }
}
```

### 7.3 Role-based listings
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

#### Create/Update Courier Body
```json
{
  "name": "...",
  "phone": "...",
  "password": "...",
  "active": true,
  "vehicleType": "MOTORCYCLE|CAR|VAN",
  "zoneId": 1
}
```

### 7.5 Courier Location
- **GET** `/api/couriers/{id}/location` — جلب آخر موقع للمندوب
- **PUT** `/api/couriers/{id}/location` — تحديث موقع المندوب
- **Auth:** OWNER/ADMIN/COURIER (المندوب نفسه فقط للتحديث)
- **Body (PUT):**
```json
{
  "latitude": 30.0444,
  "longitude": 31.2357
}
```
- **200 (GET):**
```json
{
  "courierId": 5,
  "latitude": 30.0444,
  "longitude": 31.2357,
  "updatedAt": "2026-02-28T12:00:00"
}
```

### 7.6 Merchant CRUD
- **GET** `/api/merchants/{id}` — جلب بيانات تاجر محدد
- **POST** `/api/merchants` — إنشاء تاجر جديد
- **PUT** `/api/merchants/{id}` — تحديث بيانات تاجر
- **Auth:** OWNER/ADMIN

#### Create/Update Merchant Body
```json
{
  "name": "...",
  "phone": "...",
  "password": "...",
  "businessName": "...",
  "address": "...",
  "active": true
}
```

### 7.7 Employee Management
- **GET** `/api/employees/{id}` — جلب بيانات موظف محدد
- **PUT** `/api/employees/{id}` — تحديث بيانات موظف
- **Auth:** OWNER/ADMIN

---

## 8) Shipment APIs (`ShipmentController`)

Base: `/api/shipments`

### 8.1 Core Shipment Operations

1. **GET** `/api/shipments`
   - Query: `page`, `size`, `sortBy`, `sortDir`
   - Roles: OWNER/ADMIN/MERCHANT

2. **GET** `/api/shipments/{id}`
   - Roles: OWNER/ADMIN/MERCHANT/COURIER/WAREHOUSE_MANAGER

3. **GET** `/api/shipments/count`

4. **POST** `/api/shipments`
   - Roles: OWNER/ADMIN/MERCHANT
   - Body required fields:
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
  "alternatePhone": "...",
  "specialInstructions": "..."
}
```

5. **GET** `/api/shipments/list`
   - Query: `limit`, `courierId`, `deliveryDate`, `page`, `size`
   - ملاحظة: حاليًا endpoint placeholder (يرجع قائمة فارغة).

### 8.2 Warehouse Operations

1. **POST** `/api/shipments/warehouse/receive`
```json
{ "trackingNumbers": ["TS...", "TS..."] }
```

2. **GET** `/api/shipments/warehouse/inventory?page=0&size=20&sortBy=createdAt&sortDir=desc`

3. **POST** `/api/shipments/warehouse/dispatch/{courierId}`
```json
{ "shipmentIds": [1,2,3] }
```

4. **POST** `/api/shipments/warehouse/reconcile/courier/{courierId}`
```json
{
  "cash_confirmed_shipment_ids": [1,2],
  "returned_shipment_ids": [3]
}
```

5. **GET** `/api/shipments/warehouse/couriers`

6. **GET** `/api/shipments/warehouse/courier/{courierId}/shipments`

7. **GET** `/api/shipments/warehouse/stats`

### 8.3 Return & Courier Location

1. **POST** `/api/shipments/{id}/return-request`
```json
{ "reason": "..." }
```

2. **PUT** `/api/shipments/courier/location/update`
```json
{ "latitude": 30.0444, "longitude": 31.2357 }
```

---

## 9) Manifest APIs (`ManifestController`)

Base: `/api/manifests`

1. **GET** `/api/manifests`
2. **POST** `/api/manifests`
```json
{ "courierId": 5 }
```
3. **GET** `/api/manifests/{manifestId}`
4. **POST** `/api/manifests/{manifestId}/shipments`
```json
[101, 102, 103]
```
5. **PUT** `/api/manifests/{manifestId}/status?status=IN_PROGRESS`
6. **POST** `/api/manifests/{manifestId}/assign`
```json
{ "trackingNumbers": ["TS...", "TS..."] }
```

---

## 10) Master Data APIs (`MasterDataController`)

Base: `/api/master`

### 10.1 Users
- **GET** `/api/master/users?page=0&size=10&sortBy=id&sortDir=desc`
- **POST** `/api/master/users`
- **PUT** `/api/master/users/{id}`
- **DELETE** `/api/master/users/{id}`

### 10.2 Zones
- **GET** `/api/master/zones`
- **POST** `/api/master/zones`
- **PUT** `/api/master/zones/{id}`
- **DELETE** `/api/master/zones/{id}`

### 10.3 Pricing
- **GET** `/api/master/pricing`
- **POST** `/api/master/pricing`
- **PUT** `/api/master/pricing/{id}`
- **DELETE** `/api/master/pricing/{id}`

### 10.4 Telemetry Settings
- **GET** `/api/master/telemetry`
- **PUT** `/api/master/telemetry`
- **GET** `/api/master/telemetry/{key}`
- **DELETE** `/api/master/telemetry/{key}`

---

## 11) Dashboard APIs (`DashboardController`)

Base: `/api/dashboard`

1. **GET** `/api/dashboard/summary`
2. **GET** `/api/dashboard/statistics`
3. **GET** `/api/dashboard/dashboard-stats`
4. **GET** `/api/dashboard/revenue-chart`
5. **GET** `/api/dashboard/shipments-chart`

---

## 12) Financial APIs (`FinancialController`)

Base: `/api/financial`

1. **GET** `/api/financial/payouts`
2. **POST** `/api/financial/payouts`
```json
{
  "userId": 10,
  "payoutType": "COURIER|MERCHANT",
  "startDate": "2026-02-01",
  "endDate": "2026-02-15"
}
```
3. **GET** `/api/financial/payouts/{payoutId}`
4. **PUT** `/api/financial/payouts/{payoutId}/status?status=PAID`
5. **GET** `/api/financial/payouts/pending`
6. **GET** `/api/financial/payouts/user/{userId}`
7. **GET** `/api/financial/payouts/{payoutId}/items`

---

## 13) Reports APIs (`ReportsController`)

Base: `/api/reports`

> أغلب endpoints تتطلب `startDate` و `endDate` بصيغة `YYYY-MM-DD`

1. **GET** `/api/reports/shipments?startDate=2026-02-01&endDate=2026-02-25`
2. **GET** `/api/reports/couriers?startDate=...&endDate=...`
3. **GET** `/api/reports/merchants?startDate=...&endDate=...`
4. **GET** `/api/reports/warehouse?startDate=...&endDate=...`
5. **GET** `/api/reports/dashboard`
   - **Auth:** OWNER/ADMIN
   - **وصف:** ملخص شامل للوحة التحكم يشمل إحصائيات عامة
   - **200:**
```json
{
  "totalShipments": 1250,
  "deliveredToday": 45,
  "pendingShipments": 120,
  "activeCouriers": 18,
  "revenue": 25000.00
}
```

---

## 14) Settings APIs (`SettingsController`)

Base: `/api/settings`

> **تحديث Sprint 8:** الإعدادات تُحفظ الآن في قاعدة البيانات عبر `SystemSetting` entity بدلاً من الذاكرة فقط.

1. **GET** `/api/settings`
   - **Auth:** OWNER/ADMIN
   - **وصف:** جلب جميع إعدادات النظام من قاعدة البيانات
   - **200:**
```json
{
  "success": true,
  "data": {
    "companyName": "Twsela",
    "defaultCurrency": "EGP",
    "smsEnabled": true,
    "maxDeliveryAttempts": 3
  }
}
```

2. **POST** `/api/settings`
   - **Auth:** OWNER/ADMIN
   - **وصف:** حفظ/تحديث إعدادات النظام (تُحفظ في DB)
   - **Body:**
```json
{
  "companyName": "Twsela",
  "defaultCurrency": "EGP",
  "smsEnabled": true
}
```

3. **POST** `/api/settings/reset`
   - **Auth:** OWNER
   - **وصف:** إعادة تعيين الإعدادات للقيم الافتراضية

---

## 15) SMS APIs (`SmsController`)

Base: `/api/sms`

1. **POST** `/api/sms/send?phoneNumber=...&message=...`
2. **POST** `/api/sms/send-otp?phoneNumber=...`
3. **POST** `/api/sms/send-notification?phoneNumber=...&notificationType=...&trackingNumber=...`
4. **GET** `/api/sms/test`

---

## 16) Audit APIs (`AuditController`)

Base: `/api/audit`

1. **GET** `/api/audit/logs`
   - Query optional:
     - `startDate` (ISO DateTime)
     - `endDate` (ISO DateTime)
     - `action`
     - `userId`
2. **GET** `/api/audit/entity/{entityType}/{entityId}`
3. **GET** `/api/audit/user/{userId}`

---

## 17) Backup APIs (`BackupController`)

Base: `/api/backup`

1. **POST** `/api/backup/create`
2. **POST** `/api/backup/restore?backupFilePath=...`
3. **GET** `/api/backup/status`
4. **GET** `/api/backup/test`

---

## 18) Notification APIs

Base: `/api/notifications`

1. **GET** `/api/notifications`
   - **Auth:** Required
   - **وصف:** جلب جميع إشعارات المستخدم الحالي
   - **200:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "message": "تم تسليم الشحنة TS123456",
      "type": "SHIPMENT_DELIVERED",
      "read": false,
      "createdAt": "2026-02-28T10:00:00"
    }
  ]
}
```

2. **PUT** `/api/notifications/{id}/read`
   - **Auth:** Required
   - **وصف:** تحديد إشعار واحد كمقروء
   - **200:** `{ "success": true }`

3. **PUT** `/api/notifications/read-all`
   - **Auth:** Required
   - **وصف:** تحديد جميع الإشعارات كمقروءة
   - **200:** `{ "success": true }`

---

## 19) Telemetry APIs

Base: `/api/telemetry`

1. **POST** `/api/telemetry`
   - **Auth:** Required
   - **وصف:** إرسال بيانات القياس عن بُعد (telemetry)
   - **Body:**
```json
{
  "event": "page_view",
  "page": "/dashboard",
  "duration": 1500,
  "metadata": {}
}
```
   - **200:** `{ "success": true }`

2. **GET** `/api/telemetry`
   - **Auth:** OWNER/ADMIN
   - **وصف:** جلب بيانات القياس عن بُعد
   - **Query:** `startDate`, `endDate`

---

## 20) Debug APIs (`DebugController`) — Development Only

Base: `/api/debug`

1. **GET** `/api/debug/generate-hash?password=...`
2. **POST** `/api/debug/test-password`
```json
{ "password": "...", "hash": "$2a$..." }
```
3. **POST** `/api/debug/reset-test-passwords`

> يجب تعطيل هذه المسارات في بيئة الإنتاج.

---

## 21) DTOs الجديدة (Sprint 8)

تم إنشاء 6 DTOs جديدة لتوحيد استجابات الـ API:

### 21.1 CourierResponseDTO
```json
{
  "id": 5,
  "name": "أحمد",
  "phone": "01012345678",
  "active": true,
  "vehicleType": "MOTORCYCLE",
  "currentZone": "القاهرة"
}
```

### 21.2 MerchantResponseDTO
```json
{
  "id": 10,
  "name": "محل الأمانة",
  "phone": "01098765432",
  "businessName": "محل الأمانة للملابس",
  "totalShipments": 230
}
```

### 21.3 ShipmentResponseDTO
```json
{
  "id": 100,
  "trackingNumber": "TS20260228001",
  "status": "IN_TRANSIT",
  "recipientName": "محمد",
  "createdAt": "2026-02-28T08:00:00"
}
```

### 21.4 DashboardStatsDTO
```json
{
  "totalShipments": 1250,
  "deliveredToday": 45,
  "activeCouriers": 18,
  "revenue": 25000.00
}
```

### 21.5 SettingsResponseDTO
```json
{
  "settings": { "key": "value" },
  "lastUpdated": "2026-02-28T12:00:00"
}
```

### 21.6 HealthResponseDTO
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

## 22) أكواد الأخطاء المتوقعة

- `200` نجاح
- `400` بيانات ناقصة/غير صحيحة
- `401` غير مصادق
- `403` غير مصرح بالدور الحالي
- `404` مورد غير موجود
- `500` خطأ داخلي

---

## 23) أمثلة عملية سريعة

### 20.1 Login

```bash
curl -X POST "http://localhost:8000/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"phone":"01023782584","password":"150620KkZz@#$"}'
```

### 20.2 Get Current User

```bash
curl "http://localhost:8000/api/auth/me" \
  -H "Authorization: Bearer <TOKEN>"
```

### 20.3 Create Shipment

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

---

## 24) ملاحظات جودة API (مهمة)

1. بعض endpoints تُرجع Domain مباشرة، وبعضها يُرجع wrapper object.
2. بعض endpoints حاليًا placeholder/hardcoded.
3. يفضل توحيد Error contract عبر `@ControllerAdvice` موحد.
4. يفضل إصدار API versioning موحد (مثل `/api/v1`) بدل الاستخدام الجزئي.

---

## 25) مسار التطوير المقترح لتوثيق احترافي أكثر

1. اعتماد OpenAPI annotations لكل endpoint (summary/description/request/response).
2. تعريف SecuritySchemes (Bearer JWT) في OpenAPI config.
3. توحيد examples بالعربي لكل endpoint.
4. توليد Postman collection تلقائيًا من OpenAPI.
