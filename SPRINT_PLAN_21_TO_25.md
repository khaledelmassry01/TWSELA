# خطة السبرنتات 21–25 — نظام Twsela

> **تاريخ الإعداد:** 2 مارس 2026  
> **الحالة:** Sprints 1–20 مكتملة ✅ (330 اختبار ناجح)  
> **الهدف:** التوسع التجاري والذكاء التشغيلي — المرحلة 2 و 3 من خريطة الطريق

---

## ملخص تنفيذي

| السبرنت | العنوان | ملفات Backend | ملفات Frontend | اختبارات جديدة | إجمالي الاختبارات |
|---------|---------|---------------|----------------|----------------|-------------------|
| **21** | خطط الاشتراك والفواتير وبوابة الدفع | ~16 | ~4 | ~35 | ~365 |
| **22** | إدارة الأسطول وتتبع المركبات | ~14 | ~4 | ~32 | ~397 |
| **23** | نظام التذاكر والدعم الفني و SLA | ~16 | ~5 | ~35 | ~432 |
| **24** | محرك التوزيع الذكي وتحسين المسارات | ~12 | ~4 | ~30 | ~462 |
| **25** | دعم متعدد الدول والعملات والضرائب | ~16 | ~4 | ~32 | ~494 |

**المحصلة النهائية بعد Sprint 25:**
- ~494 اختبار ناجح (من 330 حالياً)
- ~74 ملف Backend جديد
- ~21 ملف Frontend جديد
- ~12 Flyway migration جديدة (V8–V19)
- ~50 API endpoint جديد

---

## Sprint 21 — خطط الاشتراك والفواتير وبوابة الدفع

> **المدة:** 2 أسابيع  
> **الأولوية:** عالية — مصدر الدخل الأساسي للمنصة  
> **المتطلبات المسبقة:** WalletService ✅ (Sprint 18)، FinancialService ✅

### 21.0 نظرة عامة

```
التاجر يختار خطة:
  FREE (50 شحنة/شهر) → BASIC (500 شحنة/شهر) → PRO (5000 شحنة/شهر) → ENTERPRISE (بلا حدود)

دورة حياة الاشتراك:
  TRIAL → ACTIVE → PAST_DUE → EXPIRED → CANCELLED
                  → UPGRADED / DOWNGRADED

الفوترة التلقائية:
  نهاية كل شهر → إنشاء Invoice → محاولة تحصيل → نجاح/فشل
  فشل × 3 → تعليق الحساب → إشعار التاجر

بوابة الدفع:
  PaymentGateway (Interface)
  ├── PaymobGateway (مصر — بطاقات + محافظ)
  ├── StripeGateway (عالمي — بطاقات)
  └── FawryGateway (مصر — نقاط دفع)
```

### 21.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **SubscriptionPlan.java** | `domain/` | Entity: `id`, `name` (FREE/BASIC/PRO/ENTERPRISE), `displayNameAr`, `monthlyPrice`, `annualPrice`, `maxShipmentsPerMonth`, `maxWebhooks`, `features` (JSON), `apiRateLimit`, `isActive`, `sortOrder` |
| 2 | **MerchantSubscription.java** | `domain/` | Entity: `id`, `merchant` (User), `plan` (SubscriptionPlan), `status` (enum: TRIAL/ACTIVE/PAST_DUE/EXPIRED/CANCELLED), `billingCycle` (MONTHLY/ANNUAL), `currentPeriodStart`, `currentPeriodEnd`, `trialEndsAt`, `cancelledAt`, `paymentMethodId`, `autoRenew` |
| 3 | **Invoice.java** | `domain/` | Entity: `id`, `invoiceNumber` (unique TWS-INV-XXXXXXXX), `subscription` (MerchantSubscription), `amount`, `tax`, `totalAmount`, `status` (DRAFT/PENDING/PAID/OVERDUE/CANCELLED/REFUNDED), `dueDate`, `paidAt`, `paymentGateway`, `paymentTransactionId`, `items` (OneToMany) |
| 4 | **InvoiceItem.java** | `domain/` | Entity: `id`, `invoice`, `description`, `quantity`, `unitPrice`, `totalPrice` |
| 5 | **PaymentTransaction.java** | `domain/` | Entity: `id`, `externalId`, `gateway` (PAYMOB/STRIPE/FAWRY/MANUAL), `type` (CHARGE/REFUND), `amount`, `currency`, `status` (PENDING/SUCCESS/FAILED/REFUNDED), `merchantId`, `invoiceId`, `metadata` (JSON), `errorMessage` |
| 6 | **UsageTracking.java** | `domain/` | Entity: `id`, `merchantId`, `period` (YYYY-MM), `shipmentsCreated`, `apiCalls`, `webhookEvents`, `lastUpdated` |
| 7 | **SubscriptionPlanRepository.java** | `repository/` | `findByName()`, `findByIsActiveTrue()` |
| 8 | **MerchantSubscriptionRepository.java** | `repository/` | `findByMerchantId()`, `findByStatusIn()`, `findByCurrentPeriodEndBefore()`, `findExpiring(days)` |
| 9 | **InvoiceRepository.java** | `repository/` | `findBySubscriptionId()`, `findByStatus()`, `findOverdue()`, `generateInvoiceNumber()` |
| 10 | **PaymentTransactionRepository.java** | `repository/` | `findByMerchantId()`, `findByInvoiceId()`, `findByExternalId()` |
| 11 | **UsageTrackingRepository.java** | `repository/` | `findByMerchantIdAndPeriod()`, `incrementShipments()`, `incrementApiCalls()` |
| 12 | **SubscriptionService.java** | `service/` | `createSubscription()`, `upgradeplan()`, `downgradePlan()`, `cancelSubscription()`, `renewSubscription()`, `checkUsageLimit()`, `getActivePlan()`, `processExpiredSubscriptions()` (Scheduled), `processTrialExpirations()` |
| 13 | **InvoiceService.java** | `service/` | `generateInvoice()`, `processPayment()`, `markAsPaid()`, `refundInvoice()`, `getInvoicesByMerchant()`, `getOverdueInvoices()`, `generateMonthlyInvoices()` (Scheduled) |
| 14 | **PaymentGateway.java** | `service/` | **Interface**: `charge(amount, currency, paymentMethod)`, `refund(transactionId)`, `getTransaction(externalId)`, `createPaymentIntent()`, `verifyWebhook(payload, signature)` |
| 15 | **PaymentGatewayFactory.java** | `service/` | Factory pattern: `getGateway(gatewayType)` → returns appropriate implementation |
| 16 | **PaymobGateway.java** | `service/` | Implements PaymentGateway for Paymob (Egyptian market) — auth token, payment key, iframe URL |
| 17 | **StripeGateway.java** | `service/` | Implements PaymentGateway for Stripe — PaymentIntent API |
| 18 | **UsageTrackingService.java** | `service/` | `trackShipmentCreation()`, `trackApiCall()`, `isWithinLimit()`, `getUsageSummary()`, `resetMonthlyCounters()` (Scheduled) |
| 19 | **SubscriptionController.java** | `web/` | `GET /api/subscriptions/plans` (عام)، `POST /api/subscriptions` (اشتراك جديد)، `PUT /api/subscriptions/upgrade` (ترقية)، `PUT /api/subscriptions/cancel` (إلغاء)، `GET /api/subscriptions/my` (اشتراكي الحالي)، `GET /api/subscriptions/usage` (استهلاكي) |
| 20 | **InvoiceController.java** | `web/` | `GET /api/invoices` (فواتيري)، `GET /api/invoices/{id}` (تفاصيل)، `POST /api/invoices/{id}/pay` (دفع)، `GET /api/admin/invoices` (كل الفواتير — OWNER)، `POST /api/admin/invoices/{id}/refund` (استرداد) |
| 21 | **PaymentWebhookController.java** | `web/` | `POST /api/payment/webhook/paymob` (callback)، `POST /api/payment/webhook/stripe` (callback) — بدون JWT |
| 22 | **SubscriptionDTO.java** | `web/dto/` | `PlanDTO`, `SubscriptionDTO`, `UsageDTO`, `InvoiceDTO` (nested inner classes) |
| 23 | **V8__create_subscription_tables.sql** | `db/migration/` | `subscription_plans`, `merchant_subscriptions`, `invoices`, `invoice_items`, `payment_transactions`, `usage_tracking` + seed plans |
| 24 | **V9__seed_subscription_plans.sql** | `db/migration/` | Insert FREE/BASIC/PRO/ENTERPRISE plans with Arabic names and pricing |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | السماح لـ `GET /api/subscriptions/plans` و `/api/payment/webhook/**` بدون JWT. `/api/subscriptions/**` و `/api/invoices/**` authenticated. `/api/admin/invoices/**` OWNER/ADMIN |
| 2 | **ShipmentService.java** | عند `createShipment()`: استدعاء `usageTrackingService.trackShipmentCreation()` + `checkUsageLimit()` |
| 3 | **RateLimitFilter.java** | تعديل ليقرأ الحد من `SubscriptionPlan.apiRateLimit` بدلاً من قيمة ثابتة |
| 4 | **WebhookService.java** | عند `subscribe()`: التحقق من حد الـ webhooks حسب الخطة |

### 21.2 Frontend

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **pricing.html** | `frontend/` | صفحة الأسعار العامة — 4 أعمدة (Free/Basic/Pro/Enterprise) مع مقارنة الميزات |
| 2 | **pricing-page.js** | `frontend/src/js/pages/` | يجلب الخطط من API، يعرض المقارنة، أزرار CTA للاشتراك |
| 3 | **merchant-subscription-page.js** | `frontend/src/js/pages/` | إدارة اشتراك التاجر — الخطة الحالية، الاستهلاك، الفواتير، ترقية/إلغاء |
| 4 | **merchant/subscription.html** | `frontend/merchant/` | صفحة إدارة الاشتراك للتاجر |

### 21.3 اختبارات (~35)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **SubscriptionServiceTest.java** | 12 | createSubscription (trial), activate, upgrade (pro→enterprise), downgrade, cancel, renew, checkLimit (within, exceeded), processExpired, processTrialExpiration, duplicateSubscription, invalidPlan |
| 2 | **InvoiceServiceTest.java** | 8 | generateInvoice, processPayment (success, failure), markAsPaid, refund, getByMerchant, getOverdue, generateMonthly |
| 3 | **UsageTrackingServiceTest.java** | 5 | trackShipment, trackApiCall, isWithinLimit (true, false), resetCounters |
| 4 | **SubscriptionControllerTest.java** | 5 | getPlans, subscribe, getMySubscription, upgrade, cancel |
| 5 | **InvoiceControllerTest.java** | 5 | getInvoices, getById, pay, adminGetAll, adminRefund |

---

## Sprint 22 — إدارة الأسطول وتتبع المركبات

> **المدة:** 2 أسابيع  
> **الأولوية:** عالية — إدارة الموارد المادية الأساسية  
> **المتطلبات المسبقة:** CourierDetails ✅، CourierLocationService ✅ (Sprint 16)

### 22.0 نظرة عامة

```
إدارة المركبات:
  Vehicle (دراجة/موتوسيكل/سيارة/فان/شاحنة)
  ├── تسجيل: نوع، موديل، لوحة، رخصة، تأمين
  ├── تعيين: مندوب ↔ مركبة (1:1 active assignment)
  ├── صيانة: جدول دوري + طوارئ + تذكيرات
  ├── وقود: تسجيل يومي + تقارير استهلاك
  └── حالة: ACTIVE / MAINTENANCE / RETIRED

نموذج التعيين:
  Courier ←→ Vehicle (ManyToOne active)
  عند تعيين مندوب جديد → إنهاء التعيين السابق تلقائياً

لوحة تحكم الأسطول:
  - إجمالي المركبات حسب النوع والحالة
  - مركبات تحتاج صيانة (تنبيه)
  - معدل استهلاك الوقود
  - خريطة حية لمواقع المركبات
  - تقارير الاستخدام والتكلفة
```

### 22.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **Vehicle.java** | `domain/` | Entity: `id`, `vehicleType` (BICYCLE/MOTORCYCLE/CAR/VAN/TRUCK), `make`, `model`, `year`, `licensePlate` (unique), `registrationNumber`, `insuranceExpiry`, `status` (ACTIVE/MAINTENANCE/RETIRED), `maxWeightKg`, `maxVolumeCm3`, `fuelType` (GASOLINE/DIESEL/ELECTRIC/NONE), `currentOdometerKm`, `notes` |
| 2 | **VehicleAssignment.java** | `domain/` | Entity: `id`, `vehicle` (ManyToOne), `courier` (ManyToOne User), `assignedAt`, `unassignedAt` (nullable = active), `assignedBy` (User) |
| 3 | **VehicleMaintenance.java** | `domain/` | Entity: `id`, `vehicle` (ManyToOne), `maintenanceType` (SCHEDULED/EMERGENCY/INSPECTION), `description`, `cost`, `scheduledDate`, `completedDate`, `odometerAtService`, `nextServiceDate`, `serviceProvider`, `status` (SCHEDULED/IN_PROGRESS/COMPLETED/CANCELLED) |
| 4 | **FuelLog.java** | `domain/` | Entity: `id`, `vehicle` (ManyToOne), `courier` (ManyToOne), `fuelType`, `liters`, `costPerLiter`, `totalCost`, `odometerReading`, `fuelStation`, `logDate` |
| 5 | **VehicleRepository.java** | `repository/` | `findByStatus()`, `findByVehicleType()`, `findByLicensePlate()`, `findAvailableVehicles()` (ACTIVE + not assigned) |
| 6 | **VehicleAssignmentRepository.java** | `repository/` | `findActiveByCourierId()`, `findActiveByVehicleId()`, `findByVehicleId()`, `findByCourierId()` |
| 7 | **VehicleMaintenanceRepository.java** | `repository/` | `findByVehicleId()`, `findUpcoming(days)`, `findOverdue()`, `findByStatus()` |
| 8 | **FuelLogRepository.java** | `repository/` | `findByVehicleId()`, `findByCourierId()`, `sumCostByVehicleIdAndDateRange()`, `avgConsumptionByVehicleId()` |
| 9 | **VehicleService.java** | `service/` | `createVehicle()`, `updateVehicle()`, `retireVehicle()`, `getAvailableVehicles()`, `getVehicleById()`, `getAllVehicles(filter, page)` |
| 10 | **VehicleAssignmentService.java** | `service/` | `assignCourierToVehicle()` (auto-unassign previous), `unassignCourier()`, `getCurrentAssignment()`, `getAssignmentHistory()` |
| 11 | **MaintenanceService.java** | `service/` | `scheduleMaintenance()`, `completeMaintenance()`, `cancelMaintenance()`, `getUpcomingMaintenance()`, `getOverdueMaintenance()`, `sendMaintenanceReminders()` (@Scheduled daily) |
| 12 | **FuelService.java** | `service/` | `logFuel()`, `getFuelHistory()`, `getFuelCostReport()`, `getConsumptionReport()` |
| 13 | **FleetController.java** | `web/` | vehicles CRUD: `POST /api/fleet/vehicles`, `GET /api/fleet/vehicles`, `GET /api/fleet/vehicles/{id}`, `PUT /api/fleet/vehicles/{id}`, `DELETE /api/fleet/vehicles/{id}` (retire) |
| 14 | **FleetAssignmentController.java** | `web/` | `POST /api/fleet/assign`, `DELETE /api/fleet/assign/{id}`, `GET /api/fleet/assignments`, `GET /api/fleet/courier/{id}/vehicle` |
| 15 | **FleetMaintenanceController.java** | `web/` | `POST /api/fleet/maintenance`, `PUT /api/fleet/maintenance/{id}`, `GET /api/fleet/maintenance`, `GET /api/fleet/maintenance/upcoming`, `GET /api/fleet/maintenance/overdue` |
| 16 | **FleetFuelController.java** | `web/` | `POST /api/fleet/fuel`, `GET /api/fleet/fuel/vehicle/{id}`, `GET /api/fleet/fuel/report` |
| 17 | **FleetDTO.java** | `web/dto/` | `VehicleDTO`, `AssignmentDTO`, `MaintenanceDTO`, `FuelLogDTO`, `FleetDashboardDTO` (nested inner classes) |
| 18 | **V10__create_fleet_tables.sql** | `db/migration/` | `vehicles`, `vehicle_assignments`, `vehicle_maintenance`, `fuel_logs` + indexes |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | `/api/fleet/**` → OWNER, ADMIN. `/api/fleet/fuel` → + COURIER (POST only for logging own fuel) |
| 2 | **CourierDetails.java** | إضافة helper `getCurrentVehicle()` → lazy delegation |

### 22.2 Frontend

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **owner/fleet.html** | `frontend/owner/` | لوحة تحكم الأسطول — جدول المركبات + فلترة حسب النوع/الحالة + إحصائيات |
| 2 | **owner-fleet-page.js** | `frontend/src/js/pages/` | CRUD المركبات + التعيينات + رسم بياني لحالة الأسطول |
| 3 | **owner/fleet-maintenance.html** | `frontend/owner/` | إدارة الصيانة — تقويم + قائمة + تنبيهات |
| 4 | **owner-fleet-maintenance-page.js** | `frontend/src/js/pages/` | عرض الصيانة القادمة/المتأخرة + جدولة جديدة |

### 22.3 اختبارات (~32)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **VehicleServiceTest.java** | 8 | create, update, retire, getById, getAll (filtered), getAvailable, duplicatePlate, invalidType |
| 2 | **VehicleAssignmentServiceTest.java** | 7 | assign (new), assign (auto-unassign previous), unassign, getCurrentAssignment, getHistory, assignRetiredVehicle, assignInactiveCourier |
| 3 | **MaintenanceServiceTest.java** | 7 | schedule, complete, cancel, getUpcoming, getOverdue, completeNonExistent, doubleComplete |
| 4 | **FuelServiceTest.java** | 4 | logFuel, getHistory, getCostReport, getConsumptionReport |
| 5 | **FleetControllerTest.java** | 6 | createVehicle, getAll, getById, updateVehicle, retireVehicle, assignCourier |

---

## Sprint 23 — نظام التذاكر والدعم الفني و SLA

> **المدة:** 2 أسابيع  
> **الأولوية:** عالية — خدمة العملاء هي أساس الاستمرارية  
> **المتطلبات المسبقة:** NotificationService ✅ (Sprint 8-9)، User/Role system ✅

### 23.0 نظرة عامة

```
نظام الدعم الفني:
  SupportTicket (تذكرة)
  ├── الأولوية: LOW / MEDIUM / HIGH / CRITICAL
  ├── الفئة: SHIPMENT_ISSUE / PAYMENT / ACCOUNT / TECHNICAL / OTHER
  ├── الحالة: OPEN → IN_PROGRESS → WAITING_CUSTOMER → RESOLVED → CLOSED → REOPENED
  ├── المرسل: أي مستخدم (Merchant/Courier/Warehouse)
  ├── المُسند إليه: Admin/Owner
  └── التصعيد: تلقائي بعد انتهاء SLA

SLA Policy:
  ├── CRITICAL: استجابة أولى < 1 ساعة، حل < 4 ساعات
  ├── HIGH: استجابة أولى < 4 ساعات، حل < 24 ساعة
  ├── MEDIUM: استجابة أولى < 12 ساعة، حل < 48 ساعة
  └── LOW: استجابة أولى < 24 ساعة، حل < 72 ساعة

التواصل الداخلي:
  TicketMessage (رسائل على التذكرة)
  ├── نص + مرفقات
  ├── رسائل داخلية (internal notes — مرئية للفريق فقط)
  └── إشعار عند رد جديد

قاعدة المعرفة:
  KnowledgeArticle (مقالات المساعدة)
  ├── عنوان + محتوى (Markdown)
  ├── فئة + وسوم
  ├── ترتيب الشعبية
  └── متاح عام أو لدور محدد
```

### 23.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **SupportTicket.java** | `domain/` | Entity: `id`, `ticketNumber` (unique TWS-TKT-XXXXXXXX), `subject`, `description` (TEXT), `category` (enum), `priority` (enum), `status` (enum), `createdBy` (User), `assignedTo` (User nullable), `relatedShipmentId` (nullable), `slaResponseDeadline`, `slaResolutionDeadline`, `firstRespondedAt`, `resolvedAt`, `closedAt`, `satisfaction` (1-5 nullable), `tags` (comma-separated) |
| 2 | **TicketMessage.java** | `domain/` | Entity: `id`, `ticket` (ManyToOne), `sender` (User), `content` (TEXT), `isInternal` (boolean — internal notes), `attachmentUrl` (nullable) |
| 3 | **SlaPolicy.java** | `domain/` | Entity: `id`, `priority` (enum unique), `responseTimeMinutes`, `resolutionTimeMinutes`, `escalateToRole`, `isActive` |
| 4 | **KnowledgeArticle.java** | `domain/` | Entity: `id`, `title`, `titleAr`, `content` (TEXT Markdown), `contentAr`, `category`, `tags`, `viewCount`, `helpfulCount`, `isPublished`, `visibleToRoles` (comma-separated), `author` (User), `lastUpdatedBy` (User) |
| 5 | **EscalationRule.java** | `domain/` | Entity: `id`, `name`, `condition` (enum: SLA_BREACH/UNASSIGNED_TIMEOUT/REOPENED_COUNT), `thresholdMinutes`, `action` (ASSIGN_TO_ADMIN/NOTIFY_OWNER/CHANGE_PRIORITY), `isActive` |
| 6 | **SupportTicketRepository.java** | `repository/` | `findByCreatedById()`, `findByAssignedToId()`, `findByStatus()`, `findByStatusAndPriority()`, `findBreachingSla()`, `countByStatusGroupBy()`, `searchByKeyword()` |
| 7 | **TicketMessageRepository.java** | `repository/` | `findByTicketId()`, `findByTicketIdAndIsInternalFalse()`, `countByTicketId()` |
| 8 | **SlaPolicyRepository.java** | `repository/` | `findByPriority()`, `findByIsActiveTrue()` |
| 9 | **KnowledgeArticleRepository.java** | `repository/` | `findByIsPublishedTrue()`, `findByCategoryAndIsPublishedTrue()`, `searchByKeyword()`, `findTopByViewCount()` |
| 10 | **EscalationRuleRepository.java** | `repository/` | `findByIsActiveTrue()`, `findByCondition()` |
| 11 | **SupportTicketService.java** | `service/` | `createTicket()` (auto-assign SLA deadlines), `assignTicket()`, `updateStatus()` (valid transitions), `addMessage()`, `getTicketsByUser()`, `getTicketsByAssignee()`, `getAllTickets(filter, page)`, `getTicketById()`, `escalateBreachedTickets()` (@Scheduled every 15min), `closeResolvedTickets()` (@Scheduled daily — auto-close after 7 days), `rateTicket()` |
| 12 | **SlaService.java** | `service/` | `calculateDeadlines(priority)`, `checkSlaCompliance(ticket)`, `getSlaMetrics(dateRange)`, `getAverageResponseTime()`, `getAverageResolutionTime()` |
| 13 | **KnowledgeService.java** | `service/` | `createArticle()`, `updateArticle()`, `publishArticle()`, `getPublishedArticles(category)`, `searchArticles(keyword)`, `incrementViewCount()`, `markHelpful()`, `getPopularArticles()` |
| 14 | **EscalationService.java** | `service/` | `processEscalations()`, `applyRule(ticket, rule)`, `notifyEscalation()` |
| 15 | **SupportTicketController.java** | `web/` | `POST /api/support/tickets` (إنشاء)، `GET /api/support/tickets` (تذاكري)، `GET /api/support/tickets/{id}` (تفاصيل + رسائل)، `PUT /api/support/tickets/{id}/assign` (ADMIN)، `PUT /api/support/tickets/{id}/status`، `POST /api/support/tickets/{id}/messages` (إضافة رد)، `PUT /api/support/tickets/{id}/rate` (تقييم) |
| 16 | **SupportAdminController.java** | `web/` | `GET /api/admin/support/tickets` (كل التذاكر + فلترة)، `GET /api/admin/support/metrics` (SLA metrics)، `GET /api/admin/support/sla` (SLA policies)، `PUT /api/admin/support/sla/{id}` (تعديل)، `GET /api/admin/support/escalations` (التصعيدات) |
| 17 | **KnowledgeController.java** | `web/` | `GET /api/kb/articles` (عام)، `GET /api/kb/articles/{id}`، `GET /api/kb/search?q=`، `POST /api/admin/kb/articles` (ADMIN)، `PUT /api/admin/kb/articles/{id}`، `PUT /api/admin/kb/articles/{id}/publish` |
| 18 | **SupportDTO.java** | `web/dto/` | `CreateTicketDTO`, `TicketDTO`, `TicketMessageDTO`, `SlaMetricsDTO`, `KnowledgeArticleDTO` (nested inner classes) |
| 19 | **V11__create_support_tables.sql** | `db/migration/` | `support_tickets`, `ticket_messages`, `sla_policies`, `knowledge_articles`, `escalation_rules` + indexes |
| 20 | **V12__seed_sla_policies.sql** | `db/migration/` | Insert SLA policies for CRITICAL/HIGH/MEDIUM/LOW |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | `/api/support/tickets/**` authenticated. `/api/admin/support/**` OWNER/ADMIN. `/api/kb/articles/**` مفتوح (GET). `/api/admin/kb/**` OWNER/ADMIN |
| 2 | **NotificationService.java** | إشعار عند: تذكرة جديدة مسندة، رد جديد، تصعيد SLA |

### 23.2 Frontend

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **support.html** | `frontend/` | إنشاء تذكرة + عرض تذاكري (مشترك لجميع الأدوار) |
| 2 | **support-page.js** | `frontend/src/js/pages/` | إنشاء تذكرة، عرض القائمة، تفاصيل + رسائل، تقييم |
| 3 | **admin/support.html** | `frontend/admin/` | لوحة تحكم الدعم — كل التذاكر + SLA dashboard + تعيين |
| 4 | **admin-support-page.js** | `frontend/src/js/pages/` | إدارة التذاكر + metrics + تصعيدات |
| 5 | **kb.html** | `frontend/` | قاعدة المعرفة العامة — مقالات + بحث |
| 6 | **kb-page.js** | `frontend/src/js/pages/` | بحث + عرض مقالات + Markdown renderer |

### 23.3 اختبارات (~35)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **SupportTicketServiceTest.java** | 12 | createTicket (auto SLA), assignTicket, updateStatus (valid transitions, invalid transition), addMessage, addInternalNote, getByUser, getByAssignee, escalateBreached, closeResolved, rateTicket, duplicateRating |
| 2 | **SlaServiceTest.java** | 5 | calculateDeadlines (all priorities), checkCompliance (within, breached), getMetrics, averageResponseTime |
| 3 | **KnowledgeServiceTest.java** | 6 | createArticle, publish, search, getPopular, incrementView, markHelpful |
| 4 | **SupportTicketControllerTest.java** | 7 | createTicket, getMyTickets, getById, assign, updateStatus, addMessage, rateTicket |
| 5 | **KnowledgeControllerTest.java** | 5 | getArticles, getById, search, createArticle (admin), publishArticle |

---

## Sprint 24 — محرك التوزيع الذكي وتحسين المسارات

> **المدة:** 2 أسابيع  
> **الأولوية:** عالية — الميزة التنافسية الأهم  
> **المتطلبات المسبقة:** CourierLocationService ✅ (Sprint 16)، CourierZone ✅، CourierRating ✅ (Sprint 12)، Fleet ✅ (Sprint 22)

### 24.0 نظرة عامة

```
محرك التوزيع الذكي:
  عند إنشاء مانيفست أو توزيع شحنات:
  
  Score(courier, shipment) = Σ(weight_i × factor_i)
  ├── Distance Factor (40%): المسافة بين موقع المندوب وعنوان الاستلام
  ├── Load Factor (25%): عدد الشحنات الحالية vs السعة القصوى
  ├── Rating Factor (15%): متوسط تقييم المندوب
  ├── Zone Factor (10%): هل المندوب مخصص لهذه المنطقة؟
  ├── Vehicle Factor (5%): هل حجم/وزن الشحنة يناسب المركبة؟
  └── History Factor (5%): نسبة نجاح المندوب في المنطقة

تحسين المسارات (Route Optimization):
  بعد تعيين شحنات لمندوب:
  ├── Nearest Neighbor heuristic (سريع)
  ├── 2-opt improvement (تحسين)
  ├── اعتبار نوافذ التسليم الزمنية
  └── إعادة حساب عند إضافة شحنة جديدة

قواعد تلقائية:
  AssignmentRule (configurable)
  ├── MAX_LOAD_PER_COURIER: 30
  ├── MAX_DISTANCE_KM: 50
  ├── MIN_RATING: 3.0
  ├── REQUIRE_ZONE_MATCH: true
  └── AUTO_ASSIGN_ENABLED: true
```

### 24.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **AssignmentRule.java** | `domain/` | Entity: `id`, `ruleKey` (unique), `ruleValue`, `description`, `isActive`. Seed: MAX_LOAD, MAX_DISTANCE, MIN_RATING, REQUIRE_ZONE, etc. |
| 2 | **AssignmentScore.java** | `domain/` | Entity: `id`, `shipmentId`, `courierId`, `totalScore`, `distanceScore`, `loadScore`, `ratingScore`, `zoneScore`, `vehicleScore`, `historyScore`, `calculatedAt` — for audit trail |
| 3 | **OptimizedRoute.java** | `domain/` | Entity: `id`, `courierId`, `manifestId`, `waypoints` (JSON array of [{shipmentId, lat, lng, order, estimatedArrival}]), `totalDistanceKm`, `estimatedDurationMinutes`, `optimizedAt` |
| 4 | **AssignmentRuleRepository.java** | `repository/` | `findByRuleKey()`, `findByIsActiveTrue()` |
| 5 | **AssignmentScoreRepository.java** | `repository/` | `findByShipmentId()`, `findTopByShipmentIdOrderByTotalScoreDesc()`, `deleteByCalculatedAtBefore()` (cleanup) |
| 6 | **OptimizedRouteRepository.java** | `repository/` | `findByCourierIdAndOptimizedAtAfter()`, `findByManifestId()` |
| 7 | **SmartAssignmentService.java** | `service/` | المحرك الرئيسي: `calculateScore(courier, shipment)` → AssignmentScore، `findBestCourier(shipment)` → أعلى score، `autoAssignShipments(List<Shipment>)` → تعيين ذكي لكل شحنة، `bulkAssign(manifestId)` → تجميع وتعيين، `getScoreBreakdown(shipmentId, courierId)` → تفاصيل النتيجة |
| 8 | **RouteOptimizationService.java** | `service/` | `optimizeRoute(courierId, shipments)` → أفضل ترتيب، `nearestNeighbor(points)` → heuristic أولي، `twoOptImprove(route)` → تحسين تكراري، `calculateTotalDistance(route)`, `recalculateRoute(courierId)` — عند إضافة/إزالة شحنة |
| 9 | **DemandPredictionService.java** | `service/` | `predictDailyDemand(zone, date)` → عدد شحنات متوقع (based on rolling average + day-of-week factor)، `predictCourierNeed(zone, date)` → عدد مناديب مطلوب، `getHistoricalPattern(zone, dayOfWeek)` |
| 10 | **SmartAssignmentController.java** | `web/` | `POST /api/assignment/auto` (تعيين ذكي لقائمة شحنات)، `GET /api/assignment/score/{shipmentId}` (تفاصيل النتائج)، `GET /api/assignment/suggest/{shipmentId}` (اقتراح أفضل مندوب)، `PUT /api/assignment/rules` (تعديل القواعد)، `GET /api/assignment/rules` |
| 11 | **RouteController.java** | `web/` | `POST /api/routes/optimize/{courierId}` (تحسين المسار)، `GET /api/routes/{courierId}` (المسار الحالي)، `GET /api/routes/{courierId}/map` (بيانات الخريطة) |
| 12 | **DemandController.java** | `web/` | `GET /api/demand/predict?zone=&date=` (توقع الطلب)، `GET /api/demand/courier-need?zone=&date=` (احتياج المناديب)، `GET /api/demand/patterns?zone=` (الأنماط التاريخية) |
| 13 | **AssignmentDTO.java** | `web/dto/` | `ScoreBreakdownDTO`, `SuggestionDTO`, `OptimizedRouteDTO`, `DemandPredictionDTO` |
| 14 | **V13__create_assignment_tables.sql** | `db/migration/` | `assignment_rules`, `assignment_scores`, `optimized_routes` + seed rules |
| 15 | **V14__seed_assignment_rules.sql** | `db/migration/` | Insert default rules (MAX_LOAD=30, MAX_DISTANCE_KM=50, etc.) |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | `/api/assignment/**` و `/api/routes/**` و `/api/demand/**` → OWNER/ADMIN |
| 2 | **ManifestController.java** | إضافة option `autoAssign=true` عند إنشاء مانيفست |
| 3 | **ShipmentService.java** | عند إنشاء شحنة → `smartAssignmentService.findBestCourier()` إذا AUTO_ASSIGN_ENABLED |

### 24.2 Frontend

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **owner/assignment.html** | `frontend/owner/` | لوحة التوزيع الذكي — قائمة شحنات بانتظار التعيين + زر "توزيع ذكي" + عرض scores |
| 2 | **owner-assignment-page.js** | `frontend/src/js/pages/` | عرض الاقتراحات، تأكيد/تعديل التعيين، إعدادات القواعد |
| 3 | **owner/routes.html** | `frontend/owner/` | عرض المسارات المحسّنة على خريطة بسيطة |
| 4 | **owner-routes-page.js** | `frontend/src/js/pages/` | خريطة المسار + قائمة waypoints + ETA |

### 24.3 اختبارات (~30)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **SmartAssignmentServiceTest.java** | 12 | calculateScore (all factors), findBestCourier (available, none available, zone restriction), autoAssign (multiple shipments), bulkAssign, scoreBreakdown, minRatingFilter, maxLoadFilter, maxDistanceFilter |
| 2 | **RouteOptimizationServiceTest.java** | 8 | nearestNeighbor (3 points, 10 points), twoOptImprove, optimizeRoute (empty, single, multiple), calculateTotalDistance, recalculateRoute |
| 3 | **DemandPredictionServiceTest.java** | 4 | predictDailyDemand, predictCourierNeed, getPattern (weekday, weekend) |
| 4 | **SmartAssignmentControllerTest.java** | 6 | autoAssign, getScore, suggest, getRules, updateRules, suggestNotFound |

---

## Sprint 25 — دعم متعدد الدول والعملات والضرائب

> **المدة:** 2 أسابيع  
> **الأولوية:** متوسطة-عالية — شرط التوسع الإقليمي  
> **المتطلبات المسبقة:** WalletService ✅ (Sprint 18)، SubscriptionService ✅ (Sprint 21)، InvoiceService ✅ (Sprint 21)

### 25.0 نظرة عامة

```
الدعم المتعدد:
  Country (دولة)
  ├── مصر (EG): EGP، ضريبة 14% VAT، شكل عنوان مصري، Paymob
  ├── السعودية (SA): SAR، ضريبة 15% VAT + ZATCA e-invoice, Tap
  ├── الإمارات (AE): AED، ضريبة 5% VAT، FTA compliance
  └── الأردن (JO): JOD، ضريبة 16% sales tax

العملات وأسعار الصرف:
  ExchangeRate (يومي — من API خارجي أو يدوي)
  ├── EGP/USD, SAR/USD, AED/USD
  └── تحويل تلقائي عند العمليات العابرة

الضرائب:
  TaxRule (لكل دولة)
  ├── النوع: VAT / SALES_TAX / CUSTOM
  ├── النسبة
  ├── الفئات المعفاة
  └── فترة الصلاحية (from/to)

الفاتورة الإلكترونية:
  EInvoice
  ├── مصر: ETA (Electronic Tax Authority) format
  ├── السعودية: ZATCA (Fatoorah) format
  └── التوقيع الرقمي + QR code
```

### 25.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **Country.java** | `domain/` | Entity: `id`, `code` (ISO 3166-1 alpha-2, unique), `nameEn`, `nameAr`, `currencyCode`, `phonePrefix`, `addressFormat`, `timeZone`, `isActive`, `defaultPaymentGateway` |
| 2 | **Currency.java** | `domain/` | Entity: `id`, `code` (ISO 4217, unique), `nameEn`, `nameAr`, `symbol`, `decimalPlaces`, `isActive` |
| 3 | **ExchangeRate.java** | `domain/` | Entity: `id`, `baseCurrency` (String), `targetCurrency`, `rate` (BigDecimal 10,6), `effectiveDate`, `source` (API/MANUAL), `createdAt` |
| 4 | **TaxRule.java** | `domain/` | Entity: `id`, `country` (ManyToOne), `taxType` (VAT/SALES_TAX/CUSTOMS), `name`, `nameAr`, `rate` (BigDecimal), `exemptCategories` (JSON array), `validFrom`, `validTo`, `isActive` |
| 5 | **EInvoice.java** | `domain/` | Entity: `id`, `invoice` (OneToOne), `country`, `format` (ETA/ZATCA/FTA), `serialNumber`, `signedPayload` (TEXT), `qrCode` (TEXT — base64), `submissionId` (from government API), `status` (DRAFT/SUBMITTED/ACCEPTED/REJECTED), `submittedAt`, `responseData` (TEXT) |
| 6 | **CountryRepository.java** | `repository/` | `findByCode()`, `findByIsActiveTrue()` |
| 7 | **CurrencyRepository.java** | `repository/` | `findByCode()`, `findByIsActiveTrue()` |
| 8 | **ExchangeRateRepository.java** | `repository/` | `findByBaseCurrencyAndTargetCurrencyAndEffectiveDate()`, `findLatestRate(base, target)` |
| 9 | **TaxRuleRepository.java** | `repository/` | `findByCountryCodeAndIsActiveTrue()`, `findByCountryCodeAndTaxType()`, `findApplicable(countryCode, date)` |
| 10 | **EInvoiceRepository.java** | `repository/` | `findByInvoiceId()`, `findByStatus()`, `findByCountryAndSubmittedAtBetween()` |
| 11 | **CountryService.java** | `service/` | `getAllCountries()`, `getCountryByCode()`, `createCountry()`, `updateCountry()`, `activateCountry()` |
| 12 | **CurrencyService.java** | `service/` | `convert(amount, fromCurrency, toCurrency)`, `getExchangeRate(from, to)`, `updateExchangeRate()`, `fetchExchangeRatesFromApi()` (@Scheduled daily), `getAllCurrencies()` |
| 13 | **TaxService.java** | `service/` | `calculateTax(amount, countryCode)`, `getApplicableTaxRules(countryCode)`, `createTaxRule()`, `updateTaxRule()`, `isExempt(category, countryCode)` |
| 14 | **EInvoiceService.java** | `service/` | `generateEInvoice(invoiceId)` → format-specific, `submitToGovernment(eInvoiceId)`, `signPayload(data, format)`, `generateQrCode(data)`, `getEInvoiceByInvoice(invoiceId)`, `retryRejected()` (@Scheduled) |
| 15 | **LocalizationService.java** | `service/` | `getLocalizedMessage(key, locale)`, `formatAddress(address, countryCode)`, `formatPhone(phone, countryCode)`, `formatCurrency(amount, currencyCode)` |
| 16 | **CountryController.java** | `web/` | `GET /api/countries` (عام)، `GET /api/countries/{code}`، `POST /api/admin/countries` (ADMIN)، `PUT /api/admin/countries/{code}` |
| 17 | **CurrencyController.java** | `web/` | `GET /api/currencies`، `GET /api/currencies/convert?amount=&from=&to=`، `PUT /api/admin/currencies/exchange-rate` |
| 18 | **TaxController.java** | `web/` | `GET /api/tax/calculate?amount=&country=`، `GET /api/admin/tax/rules`، `POST /api/admin/tax/rules`، `PUT /api/admin/tax/rules/{id}` |
| 19 | **EInvoiceController.java** | `web/` | `POST /api/admin/einvoice/generate/{invoiceId}`، `GET /api/admin/einvoice/{id}`، `POST /api/admin/einvoice/{id}/submit`، `GET /api/admin/einvoice/pending` |
| 20 | **CountryDTO.java** | `web/dto/` | `CountryDTO`, `CurrencyDTO`, `ExchangeRateDTO`, `TaxRuleDTO`, `EInvoiceDTO` |
| 21 | **V15__create_country_currency_tables.sql** | `db/migration/` | `countries`, `currencies`, `exchange_rates` + indexes |
| 22 | **V16__create_tax_tables.sql** | `db/migration/` | `tax_rules`, `e_invoices` + indexes |
| 23 | **V17__seed_countries_and_currencies.sql** | `db/migration/` | Insert EG/SA/AE/JO + EGP/SAR/AED/JOD/USD |
| 24 | **V18__seed_tax_rules.sql** | `db/migration/` | Insert VAT rules for each country |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | `/api/countries` و `/api/currencies` → GET مفتوح. `/api/admin/countries/**` و `/api/admin/currencies/**` و `/api/admin/tax/**` و `/api/admin/einvoice/**` → OWNER/ADMIN |
| 2 | **Wallet.java** | إضافة `currency` field (default EGP)، دعم عمليات متعددة العملات |
| 3 | **InvoiceService.java** | عند إنشاء فاتورة → حساب الضريبة من `TaxService`، إنشاء EInvoice تلقائياً |
| 4 | **User.java** | إضافة `countryCode` (default "EG") |
| 5 | **Zone.java** | إضافة `countryCode` لدعم المناطق في دول مختلفة |

### 25.2 Frontend

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **owner/countries.html** | `frontend/owner/` | إدارة الدول — تفعيل/تعطيل + إعدادات كل دولة |
| 2 | **owner-countries-page.js** | `frontend/src/js/pages/` | CRUD دول + عملات + أسعار صرف |
| 3 | **owner/tax.html** | `frontend/owner/` | إدارة قواعد الضرائب — لكل دولة + فواتير إلكترونية |
| 4 | **owner-tax-page.js** | `frontend/src/js/pages/` | قواعد الضرائب + عرض EInvoice + إعادة إرسال |

### 25.3 اختبارات (~32)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **CountryServiceTest.java** | 5 | getAll, getByCode, create, update, activate |
| 2 | **CurrencyServiceTest.java** | 6 | convert (same currency, different), getRate (exists, notFound), updateRate, fetchFromApi |
| 3 | **TaxServiceTest.java** | 6 | calculateTax (EG 14%, SA 15%), getApplicable, isExempt (yes, no), createRule, expiredRule |
| 4 | **EInvoiceServiceTest.java** | 5 | generate (ETA format), submit (success, rejected), signPayload, generateQr |
| 5 | **LocalizationServiceTest.java** | 4 | formatAddress (EG, SA), formatPhone, formatCurrency |
| 6 | **CountryControllerTest.java** | 6 | getCountries, getByCode, adminCreate, adminUpdate, convertCurrency, calculateTax |

---

## ملخص التبعيات بين السبرنتات

```
Sprint 21 (Subscriptions & Payment)
  └── يعتمد على: WalletService ✅ (Sprint 18)
  └── يوفر: UsageTracking, PaymentGateway, InvoiceService

Sprint 22 (Fleet Management)
  └── يعتمد على: CourierDetails ✅, CourierLocationService ✅ (Sprint 16)
  └── يوفر: Vehicle entity, capacity data for Sprint 24

Sprint 23 (Support & SLA)
  └── مستقل — يعتمد فقط على User/Role ✅, NotificationService ✅
  └── يوفر: SLA framework, customer satisfaction data

Sprint 24 (Smart Assignment)
  ├── يعتمد على: Sprint 16 (locations), Sprint 22 (vehicle capacity)
  ├── يستفيد من: Sprint 12 (ratings), CourierZone ✅
  └── يوفر: Auto-assignment, route optimization

Sprint 25 (Multi-Country)
  ├── يعتمد على: Sprint 21 (invoices, payment gateways)
  ├── يعدّل: Sprint 18 (wallet multi-currency)
  └── يوفر: Foundation for regional expansion
```

```
الترتيب المُوصى به:
21 → 22 → 23 → 24 → 25
(التسلسل مهم: 24 يحتاج 22 للمركبات، 25 يحتاج 21 للفواتير)
```

---

## المخاطر والتخفيف

| المخاطر | الاحتمال | التخفيف |
|---------|---------|---------|
| تعقيد بوابات الدفع (APIs خارجية) | عالي | Sprint 21 يستخدم Interface pattern + mock implementations قابلة للاستبدال |
| أداء محرك التوزيع الذكي | متوسط | Sprint 24 يبدأ بـ Nearest Neighbor ثم يحسّن بـ 2-opt — O(n²) مقبول لـ <100 شحنة |
| تعقيد الضرائب والفواتير الإلكترونية | متوسط | Sprint 25 يبدأ بمصر فقط ثم يتوسع — adapter pattern per country |
| SLA escalation storms | منخفض | Sprint 23 يضع حد أقصى للتصعيدات + تبريد (cooldown period) |
| Route optimization accuracy | متوسط | Sprint 24 يستخدم heuristic بسيط أولاً — يمكن استبداله بـ OR-Tools لاحقاً |
| تزامن عمليات المحفظة متعددة العملات | منخفض | Sprint 25 يستخدم @Transactional + optimistic locking |

---

## المقاييس المستهدفة بعد Sprint 25

| المقياس | القيمة الحالية (Sprint 20) | المستهدف (Sprint 25) |
|---------|---------------------------|---------------------|
| **اختبارات ناجحة** | 330 | ~494 |
| **ملفات Java Backend** | ~185 | ~260 |
| **ملفات Frontend JS** | 26 | 37+ |
| **Flyway Migrations** | 7 (V1-V7) | 19 (V1-V18) |
| **API Endpoints** | ~110 | ~160 |
| **Domain Entities** | 38 | 55+ |
| **ميزات المرحلة 1 المكتملة** | 12/20 | 15/20 |
| **ميزات المرحلة 2 المكتملة** | 0/22 | 5/22 |
| **ميزات المرحلة 3 المكتملة** | 0/18 | 4/18 |

---

> **الخطوة التالية:** تنفيذ Sprint 21 — خطط الاشتراك والفواتير وبوابة الدفع
