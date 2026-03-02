# خطة السبرنتات 16–20 — نظام Twsela

> **تاريخ الإعداد:** 2 مارس 2026  
> **الحالة:** Sprints 1–15 مكتملة ✅ (245 اختبار ناجح)  
> **الهدف:** إضافة الميزات التجارية الأساسية من المرحلة 1 في خريطة الطريق

---

## ملخص تنفيذي

| السبرنت | العنوان | ملفات Backend | ملفات Frontend | اختبارات جديدة | إجمالي الاختبارات |
|---------|---------|---------------|----------------|----------------|-------------------|
| **16** | تتبع الموقع الحي + صفحة تتبع عامة | ~8 | ~3 | ~20 | ~265 |
| **17** | نظام المرتجعات | ~7 | ~2 | ~18 | ~283 |
| **18** | نظام المحافظ والتسوية المالية | ~8 | ~2 | ~18 | ~301 |
| **19** | نظام Webhooks والأحداث | ~6 | ~1 | ~15 | ~316 |
| **20** | التقارير المتقدمة + تقوية الواجهة الأمامية | ~4 | ~5 | ~15 | ~331 |

---

## Sprint 16 — تتبع الموقع الحي والتتبع العام

> **المدة:** 1 أسبوع  
> **الأولوية:** عالية — أكثر ميزة مطلوبة من العملاء حسب خريطة الطريق  
> **المتطلبات المسبقة:** WebSocket (Sprint 13) ✅، CourierLocationHistory entity ✅

### 16.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **CourierLocationService.java** | `service/` | حفظ موقع GPS → `CourierLocationHistory`، حساب ETA بالمسافة الخطية، أقرب مندوب لإحداثيات، آخر موقع معروف |
| 2 | **CourierLocationController.java** | `web/` | `POST /api/couriers/location` (تحديث الموقع — من JWT)، `GET /api/couriers/{id}/location` (آخر موقع)، `GET /api/couriers/{id}/location/history` (سجل اليوم) |
| 3 | **PublicTrackingController.java** | `web/` | `GET /api/public/track/{trackingNumber}` (حالة الشحنة + timeline + آخر موقع المندوب إن كان في الطريق)، `GET /api/public/track/{trackingNumber}/eta` (تقدير الوصول) |
| 4 | **TrackingResponseDTO.java** | `web/dto/` | بيانات التتبع العام: `trackingNumber`, `status`, `statusHistory[]`, `courierName`, `lastLocation`, `eta`, `podData` |
| 5 | **LocationDTO.java** | `web/dto/` | `latitude`, `longitude`, `timestamp`, `accuracy` |
| 6 | **V4__add_location_tracking_indexes.sql** | `db/migration/` | `ALTER TABLE courier_location_history ADD INDEX idx_courier_latest (courier_id, recorded_at DESC)` + أي أعمدة مفقودة |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | السماح لـ `GET /api/public/track/**` بدون تسجيل دخول |
| 2 | **CourierLocationHistory.java** | التحقق من وجود حقول: `courierId`, `latitude`, `longitude`, `recordedAt`, `accuracy` — إضافة `accuracy` لو غير موجودة |
| 3 | **ShipmentService.java** | إضافة `findByTrackingNumber()` إذا غير موجود |

### 16.2 Frontend

| # | الملف | الوصف |
|---|-------|-------|
| 1 | **tracking.html** | صفحة تتبع عامة (بدون تسجيل دخول) — حقل إدخال رقم التتبع + عرض timeline + خريطة بسيطة |
| 2 | **tracking-page.js** | `frontend/src/js/pages/` — يقرأ tracking number من URL param، يعرض الحالات بتسلسل زمني، يعرض ETA |
| 3 | **tracking-page.css** | `frontend/src/css/pages/` — تنسيق صفحة التتبع + timeline UI |

### 16.3 اختبارات (~20)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **CourierLocationServiceTest.java** | 7 | saveLocation, getLastLocation, getHistory, calculateETA, nearestCourier, invalidCoords, emptyHistory |
| 2 | **CourierLocationControllerTest.java** | 7 | updateLocation (success, unauthorized), getLastLocation (found, notFound), getHistory (ok, empty), invalidInput |
| 3 | **PublicTrackingControllerTest.java** | 6 | trackByNumber (found, notFound), trackWithCourierLocation, trackETA, invalidTrackingNumber, deliveredNoETA |

---

## Sprint 17 — نظام إدارة المرتجعات

> **المدة:** 1 أسبوع  
> **الأولوية:** عالية — دورة حياة الشحنة غير مكتملة بدون المرتجعات  
> **المتطلبات المسبقة:** ReturnShipment entity موجود ✅، ShipmentService ✅

### الحالات (Return Lifecycle)

```
RETURN_REQUESTED → RETURN_APPROVED → RETURN_PICKUP_ASSIGNED → RETURN_PICKED_UP 
→ RETURN_IN_WAREHOUSE → RETURN_DELIVERED_TO_MERCHANT
                      → RETURN_REJECTED (بواسطة Admin)
```

### 17.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **ReturnService.java** | `service/` | إنشاء طلب إرجاع، الموافقة/الرفض، تعيين مندوب، تحديث حالة، تسلسل الحالات المسموح، حساب رسوم الإرجاع |
| 2 | **ReturnController.java** | `web/` | `POST /api/returns` (طلب إرجاع)، `GET /api/returns` (قائمة حسب الدور)، `GET /api/returns/{id}` (تفاصيل)، `PUT /api/returns/{id}/status` (تحديث حالة)، `PUT /api/returns/{id}/assign` (تعيين مندوب)، `GET /api/returns/merchant/{id}` |
| 3 | **ReturnRequestDTO.java** | `web/dto/` | `shipmentId`, `reason`, `notes`, `returnType` (FULL/PARTIAL) |
| 4 | **ReturnResponseDTO.java** | `web/dto/` | بيانات المرتجع كاملة مع بيانات الشحنة الأصلية |
| 5 | **ReturnStatus.java** | `domain/` | Enum بالحالات أعلاه (إذا لم يكن موجوداً) |
| 6 | **V5__add_return_management.sql** | `db/migration/` | إضافة أعمدة/فهارس على `return_shipments`: `status`, `reason`, `return_fee`, `assigned_courier_id`, `approved_at`, `picked_up_at`, `delivered_at` |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | إضافة `/api/returns/**` بأدوار OWNER, ADMIN, MERCHANT, COURIER |
| 2 | **ReturnShipment.java** | مراجعة الحقول والعلاقات — إضافة `status`, `assignedCourier`, `reason`, `returnFee` إذا ناقصة |
| 3 | **NotificationService.java** | إرسال إشعار عند تغيير حالة المرتجع |

### 17.2 Frontend

| # | الملف | الوصف |
|---|-------|-------|
| 1 | **merchant-dashboard-page.js** | إضافة قسم "طلبات الإرجاع" مع زر "طلب إرجاع" وجدول المرتجعات |
| 2 | **owner-shipments-page.js** | إضافة tab للمرتجعات مع إمكانية الموافقة/الرفض/التعيين |

### 17.3 اختبارات (~18)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **ReturnServiceTest.java** | 10 | createReturn, approveReturn, rejectReturn, assignCourier, updateStatus (valid transitions), invalidTransition, returnFeeCalc, duplicateReturn, returnForDeliveredOnly, partialReturn |
| 2 | **ReturnControllerTest.java** | 8 | createReturn (success, shipmentNotFound, alreadyReturned), getAll, getById (found, notFound), updateStatus, assignCourier |

---

## Sprint 18 — نظام المحافظ والتسوية المالية

> **المدة:** 1 أسبوع  
> **الأولوية:** عالية — التدفق المالي الأساسي للنظام  
> **المتطلبات المسبقة:** FinancialService ✅، Payout entity ✅

### نموذج المحفظة

```
كل مستخدم (Merchant / Courier / Company) ← Wallet
  └── WalletTransaction (CREDIT / DEBIT)
       ├── سبب: COD_COLLECTED, DELIVERY_FEE, COMMISSION, WITHDRAWAL, SETTLEMENT, ADJUSTMENT
       └── مرجع: shipmentId أو payoutId

التسوية: عند توصيل شحنة COD:
  1. محفظة المندوب ← CREDIT (مبلغ COD)
  2. محفظة الشركة ← CREDIT (عمولة الشحن)
  3. محفظة التاجر  ← CREDIT (COD − عمولة)
```

### 18.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **Wallet.java** | `domain/` | Entity: `id`, `userId`, `walletType` (MERCHANT/COURIER/COMPANY), `balance` (BigDecimal), `currency`, `createdAt`, `updatedAt` |
| 2 | **WalletTransaction.java** | `domain/` | Entity: `id`, `walletId`, `type` (CREDIT/DEBIT), `amount`, `reason` (enum), `referenceId`, `balanceBefore`, `balanceAfter`, `description`, `createdAt` |
| 3 | **WalletRepository.java** | `repository/` | `findByUserId()`, `findByWalletType()` |
| 4 | **WalletTransactionRepository.java** | `repository/` | `findByWalletIdOrderByCreatedAtDesc(Pageable)`, `sumByWalletIdAndType()` |
| 5 | **WalletService.java** | `service/` | `getOrCreateWallet()`, `credit()`, `debit()`, `getBalance()`, `getTransactions(Pageable)`, `settleShipment(shipmentId)` — تسوية COD عند التوصيل |
| 6 | **WalletController.java** | `web/` | `GET /api/wallet` (محفظتي)، `GET /api/wallet/transactions` (سجل المعاملات — paginated)، `GET /api/wallet/balance` (الرصيد)، `POST /api/wallet/withdraw` (طلب سحب)، `GET /api/admin/wallets` (كل المحافظ — OWNER only) |
| 7 | **WalletDTO.java** | `web/dto/` | `balance`, `currency`, `walletType`, `recentTransactions[]` |
| 8 | **V6__create_wallet_tables.sql** | `db/migration/` | `CREATE TABLE wallets (...)`, `CREATE TABLE wallet_transactions (...)` مع فهارس |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | إضافة `/api/wallet/**` لجميع الأدوار، `/api/admin/wallets/**` لـ OWNER, ADMIN |
| 2 | **ShipmentService.java** | استدعاء `walletService.settleShipment()` عند تغيير الحالة إلى DELIVERED |

### 18.2 Frontend

| # | الملف | الوصف |
|---|-------|-------|
| 1 | **merchant-dashboard-page.js** | إضافة ويدجت "رصيد المحفظة" + رابط سجل المعاملات |
| 2 | **courier-dashboard-page.js** | إضافة ويدجت "أرباحي" مع رصيد اليوم والشهر |

### 18.3 اختبارات (~18)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **WalletServiceTest.java** | 10 | getOrCreate (new, existing), credit, debit, insufficientBalance, getBalance, settle (COD delivered), settle (already settled), transactionHistory, concurrentCredit |
| 2 | **WalletControllerTest.java** | 8 | getMyWallet, getTransactions (paginated), getBalance, requestWithdraw, adminGetAllWallets, walletNotFound, unauthorized, invalidWithdraw |

---

## Sprint 19 — نظام Webhooks والأحداث

> **المدة:** 1 أسبوع  
> **الأولوية:** متوسطة-عالية — تمكين التكامل مع أنظمة التجار  
> **المتطلبات المسبقة:** ShipmentService ✅، NotificationService ✅

### آلية العمل

```
التاجر يسجل webhook:
  POST /api/webhooks { url: "https://merchant.com/hook", events: ["SHIPMENT_STATUS_CHANGED", "SHIPMENT_DELIVERED"] }

عند تغيير حالة الشحنة:
  ShipmentService → WebhookDispatcher → HTTP POST to merchant URL
    Body: { event: "SHIPMENT_STATUS_CHANGED", shipmentId, trackingNumber, oldStatus, newStatus, timestamp }
    Headers: X-Webhook-Secret (HMAC signature for verification)

إعادة المحاولة: 3 مرات (1min, 5min, 15min) باستخدام @Retryable
```

### 19.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **WebhookSubscription.java** | `domain/` | Entity: `id`, `merchantId`, `url`, `secret` (HMAC key)، `events[]` (JSON)، `active`, `createdAt` |
| 2 | **WebhookEvent.java** | `domain/` | Entity: `id`, `subscriptionId`, `eventType`, `payload` (JSON)، `status` (PENDING/SENT/FAILED)، `attempts`, `lastAttemptAt`, `responseCode` |
| 3 | **WebhookRepository.java** | `repository/` | `findByMerchantIdAndActiveTrue()`, `findByEventsContaining()` |
| 4 | **WebhookEventRepository.java** | `repository/` | `findBySubscriptionIdOrderByCreatedAtDesc(Pageable)`, `findByStatus()` |
| 5 | **WebhookService.java** | `service/` | `subscribe()`, `unsubscribe()`, `dispatch(eventType, payload)` — يبحث عن المشتركين ويرسل HTTP POST مع HMAC signature، `retryFailed()` |
| 6 | **WebhookController.java** | `web/` | `POST /api/webhooks` (إنشاء)، `GET /api/webhooks` (قائمة اشتراكاتي)، `DELETE /api/webhooks/{id}` (إلغاء)، `GET /api/webhooks/{id}/events` (سجل الأحداث)، `POST /api/webhooks/{id}/test` (إرسال حدث تجريبي) |
| 7 | **V7__create_webhook_tables.sql** | `db/migration/` | `CREATE TABLE webhook_subscriptions (...)`, `CREATE TABLE webhook_events (...)` |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | إضافة `/api/webhooks/**` لأدوار OWNER, ADMIN, MERCHANT |
| 2 | **ShipmentService.java** | عند `updateStatus()`: استدعاء `webhookService.dispatch("SHIPMENT_STATUS_CHANGED", payload)` |

### 19.2 Frontend

| # | الملف | الوصف |
|---|-------|-------|
| 1 | **merchant-dashboard-page.js** | إضافة قسم "إعدادات التكامل" → إدارة Webhooks (إضافة URL، اختيار الأحداث، عرض سجل) |

### 19.3 اختبارات (~15)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **WebhookServiceTest.java** | 8 | subscribe, unsubscribe, dispatch (matching events), dispatch (no subscribers), hmacSignature, retryFailed, maxAttempts, invalidUrl |
| 2 | **WebhookControllerTest.java** | 7 | create (success, invalidUrl, duplicateUrl), list, delete, getEvents, testWebhook |

---

## Sprint 20 — التقارير المتقدمة + تقوية الواجهة الأمامية

> **المدة:** 1 أسبوع  
> **الأولوية:** متوسطة — تحسين جودة النظام وتجربة المستخدم  
> **المتطلبات المسبقة:** ReportsController ✅ (بعض stubs)، PdfService ✅، ExcelService ✅

### 20.1 Backend — التقارير

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **AnalyticsService.java** | `service/` | استعلامات تجميعية: `getRevenueByPeriod(from, to)`, `getDeliveryRateByZone()`, `getSLACompliance()`, `getCourierPerformanceRanking()`, `getTopMerchants()`, `getStatusDistribution()` |
| 2 | **ReportExportService.java** | `service/` | تصدير أي تقرير إلى PDF (PdfService) أو Excel (ExcelService): `exportToPdf(reportType, params)`, `exportToExcel(reportType, params)` |
| 3 | **AnalyticsDTO.java** | `web/dto/` | DTOs للتقارير: `RevenueReportDTO`, `DeliveryRateDTO`, `CourierRankingDTO` |
| 4 | **V8__add_analytics_views.sql** | `db/migration/` | إنشاء VIEW أو indexes لتحسين أداء الاستعلامات التجميعية |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **ReportsController.java** | ملء الـ stubs الموجودة بالبيانات الحقيقية من AnalyticsService، إضافة `GET /api/reports/export/{type}?format=pdf|excel` |
| 2 | **DashboardController.java** | إضافة KPI endpoints تستخدم AnalyticsService |

### 20.2 Frontend — تقوية الأمان والوصولية

| # | الملف | الوصف |
|---|-------|-------|
| 1 | **sanitizer.js** | `shared/` — `escapeHtml()` utility لمنع XSS في template literals. كل الـ `innerHTML` و `textContent` تستخدمه |
| 2 | **تحديث كل page JS files** | استبدال `innerHTML += \`...\`` بإدخال نص مُعقَّم عبر `sanitizer.escapeHtml()` |
| 3 | **تحديث كل HTML files** | إضافة `aria-label` للأزرار، `<label>` مرتبط بالنماذج، `scope` للجداول، `skip-nav` link |
| 4 | **owner-reports-page.js** | تحديث لعرض التقارير الجديدة مع أزرار تصدير PDF/Excel |
| 5 | **main.css** | إضافة `:focus-visible` styles و skip-nav styles |

### 20.3 اختبارات (~15)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **AnalyticsServiceTest.java** | 7 | revenueByPeriod, deliveryRateByZone, slaCompliance, courierRanking, topMerchants, statusDistribution, emptyDateRange |
| 2 | **ReportExportServiceTest.java** | 4 | exportPdf, exportExcel, invalidReportType, emptyData |
| 3 | **ReportsControllerTest (additions)** | 4 | getRevenueReport, getDeliveryReport, exportPdf, exportExcel |

---

## ملخص التبعيات بين السبرنتات

```
Sprint 16 (Tracking)
  └── مستقل — يعتمد فقط على Sprint 13 (WebSocket) ✅

Sprint 17 (Returns)
  └── مستقل — يعتمد على ReturnShipment entity الموجود ✅

Sprint 18 (Wallets)
  ├── يستفيد من Sprint 17 (رسوم الإرجاع → خصم من المحفظة)
  └── يتكامل مع ShipmentService (تسوية عند التوصيل)

Sprint 19 (Webhooks)
  ├── يتكامل مع Sprint 17 (حدث RETURN_STATUS_CHANGED)
  └── يتكامل مع Sprint 18 (حدث SETTLEMENT_COMPLETED)

Sprint 20 (Reports + Frontend)
  └── يستفيد من كل البيانات المتراكمة من 16-19
```

```
الترتيب المُوصى به:
16 → 17 → 18 → 19 → 20
(كل سبرنت يبني على السابق لكن يمكن تنفيذه بشكل مستقل)
```

---

## المخاطر والتخفيف

| المخاطر | الاحتمال | التخفيف |
|---------|---------|---------|
| `CourierLocationHistory` entity ناقصة الحقول | متوسط | Sprint 16 يتحقق ويضيف ما ينقص |
| `ReturnShipment` entity قد يحتاج إعادة تصميم | متوسط | Sprint 17 يراجع ويعدّل حسب الحاجة |
| أداء الاستعلامات التجميعية للتقارير | متوسط | Sprint 20 يضيف database views + indexes |
| Webhook dispatch يؤثر على أداء الـ main thread | منخفض | Sprint 19 يستخدم `@Async` + `@Retryable` |
| تعارض المحافظ عند التعاملات المتزامنة | منخفض | Sprint 18 يستخدم `@Transactional` + pessimistic lock |

---

## المقاييس المستهدفة بعد Sprint 20

| المقياس | القيمة الحالية (Sprint 15) | المستهدف (Sprint 20) |
|---------|---------------------------|---------------------|
| **اختبارات ناجحة** | 245 | 330+ |
| **ملفات Java Backend** | ~155 | ~185 |
| **ملفات Frontend JS** | 35 | 40+ |
| **Flyway Migrations** | 3 | 8 |
| **API Endpoints** | ~80 | ~110 |
| **ميزات المرحلة 1 المكتملة** | 7/20 | 12/20 |

---

> **الخطوة التالية:** تنفيذ Sprint 16 — تتبع الموقع الحي والتتبع العام
