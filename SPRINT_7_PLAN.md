# Sprint 7 — Performance & Missing Backend Endpoints
**المهام: 30 | حزم العمل: 4**

---

## WP-1: إصلاحات أداء حرجة

### T-7.01: ReportsController — إصلاح N+1 في getMerchantReport
- إنشاء repository query مع date filter + GROUP BY
- حذف stream().filter() في Java

### T-7.02: ReportsController — إصلاح N+1 في getCourierReport
- نفس النمط — repository query بدل in-memory

### T-7.03: ReportsController — إصلاح getWarehouseReport
- استعلامات فعلية بـ status filter بدل count() × 3

### T-7.04: UserController — DB pagination بدل subList()
- استخدام Pageable في Repository بدل in-memory pagination

### T-7.05: Missing DB indexes
- Warehouse, CourierDetails, MerchantDetails

## WP-2: Backend endpoints مفقودة — Auth & User

### T-7.06: POST /api/auth/logout
- إنشاء endpoint (JWT invalidation أو مجرد confirmation)

### T-7.07: POST /api/auth/change-password
- إنشاء endpoint في AuthController

### T-7.08: POST /api/auth/refresh
- إنشاء refresh token endpoint

### T-7.09: PUT /api/users/profile
- إنشاء endpoint لتحديث الملف الشخصي

## WP-3: Backend endpoints مفقودة — CRUD

### T-7.10-T-7.16: Courier CRUD endpoints
- GET /api/couriers (list with pagination)
- GET /api/couriers/{id}
- POST /api/couriers
- PUT /api/couriers/{id}
- DELETE /api/couriers/{id}
- GET /api/couriers/{id}/location
- PUT /api/couriers/{id}/location

### T-7.17-T-7.20: Merchant CRUD endpoints
- GET /api/merchants (list with pagination)
- GET /api/merchants/{id}
- POST /api/merchants
- PUT /api/merchants/{id}

### T-7.21-T-7.24: Employee CRUD endpoints
- GET /api/employees (list with pagination)
- GET /api/employees/{id}
- POST /api/employees
- PUT /api/employees/{id}

## WP-4: Endpoints مفقودة — Notifications & Reports & Telemetry

### T-7.25: GET /api/notifications
### T-7.26: PUT /api/notifications/{id}/read
### T-7.27: PUT /api/notifications/read-all
### T-7.28: GET /api/reports/dashboard
### T-7.29: POST /api/telemetry
### T-7.30: GET /api/telemetry

---

## معايير القبول
- [ ] ReportsController: 0 N+1 queries
- [ ] كل endpoints في api_service.js لها backend مقابل
- [ ] Pagination عبر DB في كل القوائم
- [ ] جميع الاختبارات تنجح
- [ ] BUILD SUCCESS
