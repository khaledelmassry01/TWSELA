# خطة السبرنتات 26–30 — نظام Twsela

> **تاريخ الإعداد:** 2 مارس 2026  
> **الحالة:** Sprints 1–25 مكتملة ✅ (430 اختبار ناجح)  
> **الهدف:** إكمال الميزات التجارية الأساسية + تحليلات الأعمال + انطلاق النظام البيئي

---

## ملخص تنفيذي

| السبرنت | العنوان | ملفات Backend | ملفات Frontend | اختبارات جديدة | إجمالي الاختبارات |
|---------|---------|---------------|----------------|----------------|-------------------|
| **26** | إثبات التسليم (POD) وجدولة الاستلام | ~18 | ~4 | ~35 | ~465 |
| **27** | نظام الإشعارات المتقدم متعدد القنوات | ~20 | ~4 | ~38 | ~503 |
| **28** | لوحة ذكاء الأعمال والتحليلات المتقدمة | ~16 | ~6 | ~32 | ~535 |
| **29** | إدارة العقود والفواتير الإلكترونية المتقدمة | ~18 | ~4 | ~30 | ~565 |
| **30** | منصة API المفتوحة وتكامل منصات التجارة | ~22 | ~5 | ~35 | ~600 |

**المحصلة النهائية بعد Sprint 30:**
- ~600 اختبار ناجح (من 430 حالياً)
- ~94 ملف Backend جديد
- ~23 ملف Frontend جديد
- ~8 Flyway migration جديدة (V17–V24)
- ~65 API endpoint جديد

---

## ما تم إنجازه حتى الآن

### Sprints 1–6: إصلاح الأساسات ✅
أمان JWT، كاش Redis، قاعدة الاختبارات، حماية CSRF، BCrypt

### Sprints 7–11: بناء الـ API وتجربة المستخدم ✅
Swagger، إعدادات النظام، إعادة بناء الواجهة، توسيع الاختبارات

### Sprints 12–15: توحيد ونضج ✅
ApiResponse<T>، DTOs، WebSocket، رفع مجمع، باركود، تقييمات، DevOps

### Sprints 16–20: ميزات تجارية أساسية ✅
تتبع حي، مرتجعات، محافظ مالية، Webhooks، تقارير متقدمة

### Sprints 21–25: التوسع التجاري والذكاء ✅
اشتراكات وفواتير، أسطول، تذاكر دعم/SLA، توزيع ذكي، تعدد الدول

### الميزات المتبقية من المرحلة 1 (خريطة الطريق)

| # | الميزة | الحالة | Sprint المقترح |
|---|--------|--------|----------------|
| 1.2 | نظام الإشعارات المتقدم | ❌ جزئي (SMS فقط) | **Sprint 27** |
| 1.5 | إثبات التسليم (POD) | ❌ غير موجود | **Sprint 26** |
| 1.7 | لوحة تحكم متقدمة | ❌ جزئي | **Sprint 28** |
| 1.11 | جدولة الاستلام | ❌ غير موجود | **Sprint 26** |
| 1.14 | نظام الفروع | ❌ مؤجل | Sprint 31+ |
| 1.17 | تقارير PDF متقدمة | ❌ جزئي | **Sprint 28** |
| 1.19 | إدارة المناطق المتقدمة | ❌ مؤجل | Sprint 31+ |
| 1.20 | إعدادات متقدمة للنظام | ❌ جزئي | Sprint 31+ |

### الميزات المتبقية من المرحلة 2

| # | الميزة | الحالة | Sprint المقترح |
|---|--------|--------|----------------|
| 2.4 | إدارة العقود والاتفاقيات | ❌ غير موجود | **Sprint 29** |
| 2.13 | تكامل منصات التجارة | ❌ غير موجود | **Sprint 30** |
| 2.14 | API عامة للمطورين | ❌ جزئي (Swagger فقط) | **Sprint 30** |
| 2.10 | نظام الحوافز | ❌ غير موجود | Sprint 31+ |
| 2.11 | برنامج إحالة | ❌ غير موجود | Sprint 31+ |
| 2.12 | نظام العروض والخصومات | ❌ غير موجود | Sprint 31+ |

---

## Sprint 26 — إثبات التسليم (POD) وجدولة الاستلام

> **المدة:** 2 أسابيع  
> **الأولوية:** عالية جداً — تقليل النزاعات وإكمال دورة حياة الشحنة  
> **المتطلبات المسبقة:** ShipmentService ✅، CourierLocationService ✅ (Sprint 16)، ReturnService ✅ (Sprint 17)

### 26.0 نظرة عامة

```
إثبات التسليم (Proof of Delivery):
  عند تسليم شحنة:
  ├── صورة التسليم (التقاط من كاميرا المندوب)
  ├── توقيع رقمي من المستلم (Canvas signature)
  ├── إحداثيات GPS لموقع التسليم الفعلي
  ├── اسم المستلم الفعلي (إذا كان شخص مختلف)
  ├── ملاحظات التسليم
  ├── وقت التسليم الدقيق
  └── تخزين على خادم الملفات / S3

محاولة تسليم فاشلة:
  ├── سبب الفشل (CUSTOMER_ABSENT / WRONG_ADDRESS / REFUSED / PHONE_OFF / OTHER)
  ├── صورة (اختيارية — باب مغلق مثلاً)
  ├── إحداثيات GPS لموقع المحاولة
  ├── عدد المحاولات (max 3 قبل الإرجاع التلقائي)
  └── جدولة إعادة المحاولة (تلقائي أو يدوي)

جدولة الاستلام:
  PickupSchedule
  ├── التاجر يختار: التاريخ + الفترة الزمنية (صباح/ظهر/مساء)
  ├── العنوان (من عناوين التاجر المحفوظة أو عنوان جديد)
  ├── عدد الشحنات التقريبي
  ├── ملاحظات خاصة
  ├── التعيين التلقائي لمندوب (SmartAssignmentService)
  └── الحالات: SCHEDULED → ASSIGNED → IN_PROGRESS → COMPLETED → CANCELLED

النتيجة:
  - كل شحنة مسلّمة لها سجل POD كامل
  - التاجر يشاهد صورة التسليم والتوقيع
  - تقليل نزاعات "لم يتم التسليم" بنسبة 80%+
  - التاجر يجدول الاستلام بسهولة بدلاً من الاتصال
```

### 26.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **DeliveryProof.java** | `domain/` | Entity: `id`, `shipment` (OneToOne), `photoUrl` (String — path on storage), `signatureUrl` (String — encoded image), `latitude` (Double), `longitude` (Double), `recipientName` (String — الاسم الفعلي), `notes` (TEXT), `deliveredAt` (Instant), `capturedByUser` (User — المندوب) |
| 2 | **DeliveryAttempt.java** | `domain/` | Entity: `id`, `shipment` (ManyToOne), `attemptNumber` (int 1–3), `status` (FAILED/SUCCESS), `failureReason` (enum: CUSTOMER_ABSENT/WRONG_ADDRESS/REFUSED/PHONE_OFF/DAMAGED/OTHER), `photoUrl` (nullable), `latitude`, `longitude`, `notes`, `attemptedAt` (Instant), `nextAttemptDate` (LocalDate nullable), `courier` (User) |
| 3 | **PickupSchedule.java** | `domain/` | Entity: `id`, `merchant` (User), `pickupDate` (LocalDate), `timeSlot` (enum: MORNING_9_12/AFTERNOON_12_3/EVENING_3_6), `address` (String TEXT), `latitude`, `longitude`, `estimatedShipments` (int), `notes`, `assignedCourier` (User nullable), `status` (enum: SCHEDULED/ASSIGNED/IN_PROGRESS/COMPLETED/CANCELLED), `completedAt`, `createdAt` |
| 4 | **DeliveryProofRepository.java** | `repository/` | `findByShipmentId()`, `existsByShipmentId()` |
| 5 | **DeliveryAttemptRepository.java** | `repository/` | `findByShipmentId()`, `findByShipmentIdOrderByAttemptNumberDesc()`, `countByShipmentId()`, `findByCourierIdAndAttemptedAtBetween()` |
| 6 | **PickupScheduleRepository.java** | `repository/` | `findByMerchantIdAndStatus()`, `findByAssignedCourierIdAndPickupDate()`, `findByPickupDateAndStatus()`, `findByStatusAndPickupDateBefore()` (overdue) |
| 7 | **DeliveryProofService.java** | `service/` | `submitProof(shipmentId, photo, signature, lat, lng, recipientName, notes)` — يحفظ POD + يحدث حالة الشحنة إلى DELIVERED, `getProof(shipmentId)` → DeliveryProof, `getProofPhoto(shipmentId)` → byte[], `getSignature(shipmentId)` → byte[], `validateProofLocation(pod, shipment)` — مقارنة GPS مع عنوان التسليم |
| 8 | **DeliveryAttemptService.java** | `service/` | `recordAttempt(shipmentId, reason, photo, lat, lng, notes)` — يسجل محاولة فاشلة, `getAttempts(shipmentId)` → List, `scheduleRetry(attemptId, nextDate)`, `autoReturnAfterMaxAttempts(shipmentId)` → إرجاع تلقائي بعد 3 محاولات, `getFailureReport(dateRange)` → إحصائيات أسباب الفشل |
| 9 | **FileStorageService.java** | `service/` | `storeFile(MultipartFile, directory)` → String path, `getFile(path)` → byte[], `deleteFile(path)`, `generateUniqueFilename(originalName)` — تخزين محلي (قابل للاستبدال بـ S3 لاحقاً) |
| 10 | **PickupScheduleService.java** | `service/` | `schedulePickup(merchantId, date, timeSlot, address, lat, lng, estimatedShipments, notes)`, `assignCourier(pickupId, courierId)`, `autoAssignPickups()` (@Scheduled 6AM — يعين مناديب للمواعيد), `startPickup(pickupId)`, `completePickup(pickupId)`, `cancelPickup(pickupId)`, `getMerchantPickups(merchantId)`, `getCourierPickups(courierId, date)`, `getOverduePickups()` |
| 11 | **DeliveryProofController.java** | `web/` | `POST /api/delivery/{shipmentId}/proof` (رفع POD — multipart: photo + signature + JSON metadata), `GET /api/delivery/{shipmentId}/proof` (بيانات POD), `GET /api/delivery/{shipmentId}/proof/photo` (الصورة), `GET /api/delivery/{shipmentId}/proof/signature` (التوقيع) |
| 12 | **DeliveryAttemptController.java** | `web/` | `POST /api/delivery/{shipmentId}/attempt` (تسجيل محاولة فاشلة), `GET /api/delivery/{shipmentId}/attempts` (كل المحاولات), `PUT /api/delivery/attempt/{id}/retry` (جدولة إعادة محاولة), `GET /api/admin/delivery/failures` (تقرير الفشل — OWNER/ADMIN) |
| 13 | **PickupScheduleController.java** | `web/` | `POST /api/pickups` (جدولة استلام — MERCHANT), `GET /api/pickups/my` (مواعيدي — MERCHANT), `GET /api/pickups/today` (مواعيد اليوم — COURIER), `PUT /api/pickups/{id}/assign` (تعيين مندوب — ADMIN), `PUT /api/pickups/{id}/start` (بدء — COURIER), `PUT /api/pickups/{id}/complete` (إتمام — COURIER), `PUT /api/pickups/{id}/cancel` (إلغاء — MERCHANT/ADMIN), `GET /api/admin/pickups` (كل المواعيد + فلترة — OWNER/ADMIN) |
| 14 | **DeliveryDTO.java** | `web/dto/` | Records: `SubmitProofRequest(recipientName, latitude, longitude, notes)`, `ProofResponse(id, shipmentId, photoUrl, signatureUrl, recipientName, lat, lng, notes, deliveredAt, courierName)`, `RecordAttemptRequest(failureReason, latitude, longitude, notes)`, `AttemptResponse(id, shipmentId, attemptNumber, status, failureReason, photoUrl, lat, lng, notes, attemptedAt, nextAttemptDate)`, `FailureReportResponse(totalAttempts, failuresByReason Map, failuresByZone Map, avgAttemptsPerShipment)` |
| 15 | **PickupDTO.java** | `web/dto/` | Records: `SchedulePickupRequest(pickupDate, timeSlot, address, latitude, longitude, estimatedShipments, notes)`, `PickupResponse(id, merchantId, merchantName, pickupDate, timeSlot, address, estimatedShipments, status, assignedCourierId, assignedCourierName, completedAt, createdAt)` |
| 16 | **V17__create_delivery_proof_tables.sql** | `db/migration/` | `delivery_proofs` (shipment_id UNIQUE FK, photo_url, signature_url, latitude, longitude, recipient_name, notes, delivered_at, captured_by_user_id FK), `delivery_attempts` (shipment_id FK, attempt_number, status, failure_reason, photo_url, latitude, longitude, notes, attempted_at, next_attempt_date, courier_id FK), UNIQUE(shipment_id, attempt_number) |
| 17 | **V18__create_pickup_schedule_tables.sql** | `db/migration/` | `pickup_schedules` (merchant_id FK, pickup_date, time_slot, address TEXT, latitude, longitude, estimated_shipments, notes, assigned_courier_id FK nullable, status, completed_at, created_at) + indexes on (merchant_id, status), (assigned_courier_id, pickup_date), (status, pickup_date) |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | `/api/delivery/**` → authenticated (COURIER for POST proof/attempt, MERCHANT/ADMIN for GET). `/api/pickups/**` → authenticated (MERCHANT for POST/GET my, COURIER for today/start/complete, ADMIN for assign/admin). `/api/admin/delivery/**` → OWNER/ADMIN |
| 2 | **ShipmentService.java** | عند `updateStatus(DELIVERED)` → يتحقق من وجود DeliveryProof (إلزامي). إضافة `maxAttempts` setting من SystemSetting |
| 3 | **SmartAssignmentService.java** | إضافة `findBestCourierForPickup(pickupSchedule)` — يراعي موقع التاجر وحمل المندوب |
| 4 | **NotificationService.java** | إشعار عند: (1) جدولة استلام ناجحة، (2) تعيين مندوب للاستلام، (3) فشل محاولة تسليم + إعادة جدولة |

### 26.2 Frontend

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **merchant/pickups.html** | `frontend/merchant/` | صفحة إدارة مواعيد الاستلام — جدولة جديدة + قائمة المواعيد بالحالات |
| 2 | **merchant-pickups-page.js** | `frontend/src/js/pages/` | جدولة استلام + عرض المواعيد + إلغاء + عرض تفاصيل POD للشحنات المسلّمة |
| 3 | **owner/delivery-report.html** | `frontend/owner/` | تقرير التسليم — نسبة النجاح/الفشل + أسباب الفشل + POD viewer |
| 4 | **owner-delivery-report-page.js** | `frontend/src/js/pages/` | إحصائيات التسليم + عرض POD (صورة + توقيع) + فلترة حسب الفترة/المنطقة |

### 26.3 اختبارات (~35)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **DeliveryProofServiceTest.java** | 8 | submitProof (success, missingPhoto, invalidLocation), getProof (found, notFound), validateLocation (within range, too far), duplicateProof |
| 2 | **DeliveryAttemptServiceTest.java** | 8 | recordAttempt (1st, 2nd, 3rd → autoReturn), getAttempts, scheduleRetry, autoReturn, failureReport, maxAttemptsExceeded, invalidReason |
| 3 | **FileStorageServiceTest.java** | 5 | storeFile (success, invalidType, tooLarge), getFile, deleteFile |
| 4 | **PickupScheduleServiceTest.java** | 8 | schedulePickup, assignCourier, autoAssign, startPickup, completePickup, cancelPickup (merchant, admin), overduePickups |
| 5 | **DeliveryProofControllerTest.java** | 3 | submitProof, getProof, getPhoto |
| 6 | **PickupScheduleControllerTest.java** | 3 | schedulePickup, getMyPickups, getCourierToday |

---

## Sprint 27 — نظام الإشعارات المتقدم متعدد القنوات

> **المدة:** 2 أسابيع  
> **الأولوية:** عالية — تجربة المستخدم الأساسية  
> **المتطلبات المسبقة:** NotificationService ✅ (Sprint 8)، TwilioSmsService ✅، ShipmentStatusHistory ✅

### 27.0 نظرة عامة

```
نظام الإشعارات المتقدم:
  قنوات الإرسال:
  ├── SMS (Twilio — موجود، يحتاج تطوير)
  ├── Email (SendGrid API — جديد)
  ├── Push Notifications (Firebase Cloud Messaging — جديد)
  ├── WhatsApp Business API (Twilio WhatsApp — جديد)
  ├── In-App Notifications (WebSocket — موجود Sprint 13)
  └── العميل يختار القنوات المفضلة

  قوالب الإشعارات (Notification Templates):
  ├── لكل حدث: createShipment, statusChange, deliveryAttempt, paymentReceived, etc.
  ├── لكل قناة: نص مختلف (SMS قصير، Email مفصل، Push مختصر)
  ├── دعم متغيرات: {{shipmentNumber}}, {{courierName}}, {{eta}}, etc.
  ├── دعم عربي/إنجليزي
  └── قابل للتخصيص من الإدارة

  تفضيلات المستخدم (Notification Preferences):
  ├── لكل مستخدم يختار: القنوات المفعلة لكل نوع حدث
  ├── وقت "عدم الإزعاج" (Quiet Hours)
  ├── تجميع الإشعارات (Digest — يومي/أسبوعي)
  └── إيقاف مؤقت لكل القنوات

  أحداث الإشعار التلقائية:
  ├── تغيير حالة الشحنة → إشعار للتاجر + المستلم
  ├── فشل محاولة تسليم → إشعار للتاجر
  ├── اقتراب وقت التسليم (ETA < 30 دقيقة) → إشعار للمستلم
  ├── فاتورة جديدة / دفعة ناجحة → إشعار للتاجر
  ├── تذكرة دعم: رد جديد / تصعيد → إشعار
  ├── اشتراك: تجديد / انتهاء قريب → إشعار
  └── مانيفست جديد / شحنة جديدة مُسندة → إشعار للمندوب

  سجل الإشعارات:
  ├── كل إشعار مُرسل يُسجل في notification_log
  ├── حالة الإرسال: SENT / DELIVERED / FAILED / BOUNCED
  ├── تتبع معدل الفتح (Open Rate) للإيميلات
  └── إعادة إرسال تلقائية عند الفشل (retry 3×)
```

### 27.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **NotificationTemplate.java** | `domain/` | Entity: `id`, `eventType` (enum: SHIPMENT_CREATED/STATUS_CHANGED/DELIVERY_ATTEMPT/PAYMENT_RECEIVED/PAYMENT_FAILED/INVOICE_GENERATED/SUBSCRIPTION_EXPIRING/TICKET_REPLY/MANIFEST_ASSIGNED/PICKUP_SCHEDULED/PICKUP_REMINDER), `channel` (enum: SMS/EMAIL/PUSH/WHATSAPP/IN_APP), `subjectTemplate` (String nullable — for email), `bodyTemplateAr` (TEXT), `bodyTemplateEn` (TEXT), `isActive` (boolean), UNIQUE(eventType, channel) |
| 2 | **NotificationPreference.java** | `domain/` | Entity: `id`, `user` (ManyToOne, unique), `enabledChannels` (JSON — Map<EventType, List<Channel>>), `quietHoursStart` (LocalTime nullable — e.g., 22:00), `quietHoursEnd` (LocalTime nullable — e.g., 07:00), `digestMode` (enum: NONE/DAILY/WEEKLY), `pausedUntil` (Instant nullable — إيقاف مؤقت) |
| 3 | **NotificationDeliveryLog.java** | `domain/` | Entity: `id`, `notificationId` (Long FK to Notification), `channel` (enum), `recipient` (String — phone/email/deviceToken), `status` (enum: PENDING/SENT/DELIVERED/FAILED/BOUNCED), `externalId` (String nullable — Twilio SID, SendGrid ID), `errorMessage` (String nullable), `sentAt` (Instant), `deliveredAt` (Instant nullable), `retryCount` (int default 0), `nextRetryAt` (Instant nullable) |
| 4 | **DeviceToken.java** | `domain/` | Entity: `id`, `user` (ManyToOne), `token` (String unique — FCM registration token), `platform` (enum: ANDROID/IOS/WEB), `isActive` (boolean default true), `createdAt`, `lastUsedAt` |
| 5 | **NotificationTemplateRepository.java** | `repository/` | `findByEventTypeAndChannel()`, `findByEventType()`, `findByIsActiveTrue()` |
| 6 | **NotificationPreferenceRepository.java** | `repository/` | `findByUserId()` |
| 7 | **NotificationDeliveryLogRepository.java** | `repository/` | `findByNotificationId()`, `findByStatusAndNextRetryAtBefore()` (for retry), `countByChannelAndStatusAndSentAtBetween()` (analytics), `findByRecipientAndSentAtBetween()` |
| 8 | **DeviceTokenRepository.java** | `repository/` | `findByUserIdAndIsActiveTrue()`, `findByToken()`, `deleteByUserIdAndToken()` |
| 9 | **NotificationDispatcher.java** | `service/` | المنسق الرئيسي: `dispatch(userId, eventType, templateVars Map<String, String>)` — يجلب التفضيلات → يحدد القنوات → يملأ القوالب → يرسل عبر كل قناة → يسجل في delivery_log. يراعي Quiet Hours و pausedUntil. |
| 10 | **TemplateEngine.java** | `service/` | `render(template, variables)` → String — يستبدل `{{var}}` بالقيم. `renderForChannel(eventType, channel, locale, variables)` → subject + body |
| 11 | **EmailNotificationService.java** | `service/` | `sendEmail(to, subject, htmlBody)` — يستخدم SendGrid API (HTTP client). `sendBulkEmail(recipients, subject, body)`. Retry on failure (3 attempts with exponential backoff) |
| 12 | **PushNotificationService.java** | `service/` | `sendPush(userId, title, body, data Map)` — يجلب device tokens → Firebase Admin SDK → FCM. `sendToTopic(topic, title, body)`. Handle token invalidation |
| 13 | **WhatsAppNotificationService.java** | `service/` | `sendWhatsApp(phone, templateName, parameters)` — Twilio WhatsApp API. Message template-based (WhatsApp policy). Fallback to SMS if WhatsApp fails |
| 14 | **NotificationRetryService.java** | `service/` | `retryFailedNotifications()` (@Scheduled every 5 min) — finds FAILED with retryCount < 3 and nextRetryAt < now → retry via appropriate channel. Exponential backoff: 5min, 15min, 60min |
| 15 | **NotificationAnalyticsService.java** | `service/` | `getDeliveryStats(dateRange)` → sent/delivered/failed/bounced per channel, `getOpenRate(dateRange)` → email open rate, `getChannelPerformance()` → latency/success per channel |
| 16 | **NotificationPreferenceController.java** | `web/` | `GET /api/notifications/preferences` (تفضيلاتي), `PUT /api/notifications/preferences` (تعديل), `PUT /api/notifications/preferences/pause` (إيقاف مؤقت), `POST /api/notifications/devices` (تسجيل device token — FCM), `DELETE /api/notifications/devices/{token}` (إلغاء تسجيل) |
| 17 | **NotificationTemplateController.java** | `web/` | `GET /api/admin/notifications/templates` (كل القوالب — OWNER/ADMIN), `GET /api/admin/notifications/templates/{eventType}` (قوالب حدث), `PUT /api/admin/notifications/templates/{id}` (تعديل قالب), `POST /api/admin/notifications/templates/{id}/test` (إرسال تجريبي), `GET /api/admin/notifications/analytics` (إحصائيات) |
| 18 | **NotificationDTO.java** | `web/dto/` | Records: `PreferenceRequest(enabledChannels Map, quietHoursStart, quietHoursEnd, digestMode)`, `PreferenceResponse(...)`, `RegisterDeviceRequest(token, platform)`, `TemplateResponse(id, eventType, channel, subjectTemplate, bodyTemplateAr, bodyTemplateEn, isActive)`, `UpdateTemplateRequest(subjectTemplate, bodyTemplateAr, bodyTemplateEn, isActive)`, `TestNotificationRequest(recipientUserId, templateVars Map)`, `DeliveryStatsResponse(totalSent, delivered, failed, bounced, channelBreakdown Map)` |
| 19 | **V19__create_notification_templates.sql** | `db/migration/` | `notification_templates` (event_type, channel, subject_template, body_template_ar, body_template_en, is_active, UNIQUE(event_type, channel)), `notification_preferences` (user_id UNIQUE FK, enabled_channels JSON, quiet_hours_start TIME, quiet_hours_end TIME, digest_mode, paused_until), `notification_delivery_log` (notification_id FK, channel, recipient, status, external_id, error_message, sent_at, delivered_at, retry_count, next_retry_at), `device_tokens` (user_id FK, token UNIQUE, platform, is_active, created_at, last_used_at) |
| 20 | **V20__seed_notification_templates.sql** | `db/migration/` | Seed templates for all event types × all channels (Arabic/English). ~55 records (11 events × 5 channels) |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **NotificationService.java** | إعادة هيكلة: `send()` يُوجّه الآن إلى `NotificationDispatcher.dispatch()` بدلاً من الإرسال المباشر. يحتفظ بالتوافق مع الـ API القديم |
| 2 | **ShipmentService.java** | عند كل `updateStatus()` → `notificationDispatcher.dispatch(merchantId, STATUS_CHANGED, vars)` + dispatch للمستلم (عبر هاتفه) |
| 3 | **SecurityConfig.java** | `/api/notifications/preferences/**` → authenticated. `/api/notifications/devices/**` → authenticated. `/api/admin/notifications/**` → OWNER/ADMIN |
| 4 | **pom.xml** | إضافة `com.sendgrid:sendgrid-java:4.10.1` + `com.google.firebase:firebase-admin:9.2.0` |

### 27.2 Frontend

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **settings/notifications.html** | `frontend/settings/` | صفحة تفضيلات الإشعارات — تبديل القنوات لكل حدث + ساعات الهدوء + digest |
| 2 | **settings-notifications-page.js** | `frontend/src/js/pages/` | واجهة تفاعلية لتعديل تفضيلات الإشعارات + تسجيل Push notification |
| 3 | **admin/notification-templates.html** | `frontend/admin/` | إدارة قوالب الإشعارات — تعديل النص + إرسال تجريبي + إحصائيات |
| 4 | **admin-notification-templates-page.js** | `frontend/src/js/pages/` | CRUD قوالب + معاينة + اختبار + رسوم بيانية لمعدل التسليم |

### 27.3 اختبارات (~38)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **NotificationDispatcherTest.java** | 10 | dispatch (all channels), quietHours (within, outside), pausedUser, noPreference (defaults), templateNotFound, multipleChannels, digestMode |
| 2 | **TemplateEngineTest.java** | 5 | render (simple, nested vars), renderForChannel (ar, en), missingVar (graceful), nullTemplate |
| 3 | **EmailNotificationServiceTest.java** | 5 | sendEmail (success, failure, retry), sendBulk, invalidEmail |
| 4 | **PushNotificationServiceTest.java** | 5 | sendPush (success, invalidToken → deactivate), noDeviceTokens, multipleDevices, topicSend |
| 5 | **WhatsAppNotificationServiceTest.java** | 4 | sendWhatsApp (success, failure → fallbackSMS), invalidPhone, templateFormat |
| 6 | **NotificationRetryServiceTest.java** | 4 | retryFailed (success, maxRetries), exponentialBackoff, noFailedNotifications |
| 7 | **NotificationPreferenceControllerTest.java** | 3 | getPreferences, updatePreferences, pauseNotifications |
| 8 | **NotificationTemplateControllerTest.java** | 2 | getTemplates, updateTemplate |

---

## Sprint 28 — لوحة ذكاء الأعمال والتحليلات المتقدمة

> **المدة:** 2 أسابيع  
> **الأولوية:** عالية — رؤية البيانات لاتخاذ قرارات مبنية على حقائق  
> **المتطلبات المسبقة:** AnalyticsService ✅ (Sprint 15)، FinancialService ✅، كل الخدمات السابقة

### 28.0 نظرة عامة

```
لوحة ذكاء الأعمال (Business Intelligence):
  4 محاور تحليلية رئيسية:

  1. Revenue Analytics (تحليلات الإيرادات):
     ├── إجمالي الإيرادات (يومي/أسبوعي/شهري/سنوي)
     ├── إيرادات حسب المنطقة / التاجر / بوابة الدفع
     ├── تحليل الربحية (Revenue - Costs = Profit Margin)
     ├── تكلفة التوصيل الواحد (Cost per Delivery)
     ├── متوسط قيمة الشحنة (Average Shipment Value)
     ├── COD Collection Rate
     └── مقارنة شهر-بشهر (MoM) + سنة-بسنة (YoY)

  2. Operations Analytics (تحليلات العمليات):
     ├── نسبة النجاح من أول محاولة (First Attempt Delivery Rate)
     ├── متوسط وقت التسليم (من الإنشاء → التسليم)
     ├── SLA Compliance Rate (حسب الأولوية)
     ├── معدل المرتجعات (Return Rate) + أسبابها
     ├── ساعات الذروة (Peak Hours)
     ├── أيام الذروة (Peak Days)
     ├── Throughput (شحنات/ساعة/يوم)
     └── Bottleneck Analysis (أين تتعطل الشحنات?)

  3. Courier Analytics (تحليلات المندوبين):
     ├── Utilization Rate (وقت التوصيل ÷ وقت العمل)
     ├── توزيع الأداء (Distribution: top/avg/low performers)
     ├── توزيع الأرباح (Earnings Distribution)
     ├── Leaderboard (ترتيب أفضل المناديب)
     ├── Attendance Patterns (أنماط الحضور)
     ├── شحنات/يوم لكل مندوب
     └── مقارنة أداء المناديب

  4. Merchant Analytics (تحليلات التجار):
     ├── Retention Rate (نسبة الاستمرارية)
     ├── أكبر التجار حسب الحجم / الإيرادات
     ├── Growth Rate (معدل النمو)
     ├── Churn Prediction (توقع الانسحاب)
     ├── متوسط الشحنات/شهر لكل تاجر
     ├── نسبة دفع الفواتير في الوقت (Payment Punctuality)
     └── Net Promoter Score (NPS) — من تقييمات الدعم

  تصدير التقارير:
  ├── PDF (iText — موجود)
  ├── Excel (Apache POI — موجود)
  ├── CSV
  └── JSON API (لتكامل أنظمة خارجية)
```

### 28.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **BIDashboardService.java** | `service/` | المنسق الرئيسي: `getRevenueAnalytics(dateRange, groupBy)`, `getOperationsAnalytics(dateRange)`, `getCourierAnalytics(dateRange, top)`, `getMerchantAnalytics(dateRange, top)`, `getExecutiveSummary(dateRange)` — ملخص شامل لكل المحاور |
| 2 | **RevenueAnalyticsService.java** | `service/` | `getTotalRevenue(dateRange)`, `getRevenueByZone(dateRange)`, `getRevenueByMerchant(dateRange, top)`, `getProfitMargin(dateRange)`, `getCostPerDelivery(dateRange)`, `getAverageShipmentValue(dateRange)`, `getCodCollectionRate(dateRange)`, `getRevenueComparison(period1, period2)` — مقارنة MoM/YoY |
| 3 | **OperationsAnalyticsService.java** | `service/` | `getFirstAttemptRate(dateRange)`, `getAverageDeliveryTime(dateRange)`, `getSlaComplianceRate(dateRange)`, `getReturnRate(dateRange)`, `getReturnReasonBreakdown(dateRange)`, `getPeakHours(dateRange)`, `getPeakDays(dateRange)`, `getThroughput(dateRange)`, `getBottleneckAnalysis(dateRange)` — يحدد أين تتعطل الشحنات بأطول وقت |
| 4 | **CourierAnalyticsService.java** | `service/` | `getUtilizationRate(dateRange)`, `getPerformanceDistribution(dateRange)`, `getLeaderboard(dateRange, top)`, `getEarningsDistribution(dateRange)`, `getShipmentsPerDay(dateRange)`, `getCourierComparison(courierId1, courierId2, dateRange)` |
| 5 | **MerchantAnalyticsService.java** | `service/` | `getRetentionRate(dateRange)`, `getTopMerchants(dateRange, metric, top)`, `getGrowthRate(dateRange)`, `getChurnRisk(dateRange)` — merchants approaching churn (falling shipment count), `getPaymentPunctuality(dateRange)`, `getNPS(dateRange)` — from SupportTicket satisfaction ratings |
| 6 | **ReportExportService.java** | `service/` | `exportToPdf(reportType, dateRange, locale)` → byte[], `exportToExcel(reportType, dateRange)` → byte[], `exportToCsv(reportType, dateRange)` → byte[]. يستخدم PdfService و ExcelService الموجودين |
| 7 | **KPISnapshot.java** | `domain/` | Entity: `id`, `snapshotDate` (LocalDate unique), `totalRevenue` (BigDecimal), `totalShipments` (int), `deliveredShipments` (int), `returnedShipments` (int), `firstAttemptRate` (double), `avgDeliveryHours` (double), `activeCouriers` (int), `activeMerchants` (int), `newMerchants` (int), `slaComplianceRate` (double), `createdAt` |
| 8 | **KPISnapshotRepository.java** | `repository/` | `findBySnapshotDate()`, `findBySnapshotDateBetween()`, `findLatest()` |
| 9 | **KPISnapshotService.java** | `service/` | `captureSnapshot()` (@Scheduled midnight — يحسب KPIs اليومية ويحفظها), `getSnapshot(date)`, `getSnapshots(dateRange)`, `getTrend(metric, dateRange)` |
| 10 | **BIDashboardController.java** | `web/` | `GET /api/analytics/summary` (ملخص تنفيذي), `GET /api/analytics/revenue` (إيرادات + params: from, to, groupBy), `GET /api/analytics/operations` (عمليات), `GET /api/analytics/couriers` (مناديب + params: top, sort), `GET /api/analytics/merchants` (تجار + params: top, sort), `GET /api/analytics/kpi/trends` (اتجاهات KPI) |
| 11 | **ReportExportController.java** | `web/` | `GET /api/reports/export/{type}` (params: format=pdf|excel|csv, from, to, locale=ar|en) — كل أنواع التقارير بكل الصيغ |
| 12 | **AnalyticsDTO.java** | `web/dto/` | Records: `ExecutiveSummary(totalRevenue, totalShipments, deliveryRate, returnRate, activeCouriers, activeMerchants, revenueChangePercent, shipmentsChangePercent)`, `RevenueReport(total, byZone List, byMerchant List, profitMargin, costPerDelivery, comparison)`, `OperationsReport(firstAttemptRate, avgDeliveryTimeHours, slaCompliance, returnRate, returnReasons Map, peakHours List, throughputPerDay)`, `CourierReport(utilization, leaderboard List, earningsDistribution, performanceDistribution)`, `MerchantReport(retentionRate, topMerchants List, growthRate, churnRisk List, nps)`, `KPITrend(metric, dataPoints List<DateValue>)` |
| 13 | **V21__create_kpi_snapshots.sql** | `db/migration/` | `kpi_snapshots` (snapshot_date DATE UNIQUE, total_revenue, total_shipments, delivered_shipments, returned_shipments, first_attempt_rate, avg_delivery_hours, active_couriers, active_merchants, new_merchants, sla_compliance_rate, created_at) |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | `/api/analytics/**` → OWNER/ADMIN. `/api/reports/export/**` → OWNER/ADMIN |
| 2 | **ShipmentRepository.java** | إضافة custom @Query methods للإحصائيات المعقدة: `countByStatusAndCreatedAtBetween()`, `avgDeliveryTimeByDateRange()`, `revenueByZoneAndDateRange()` |
| 3 | **WalletTransactionRepository.java** | إضافة: `sumAmountByTypeAndDateRange()` لحساب الإيرادات |

### 28.2 Frontend

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **owner/analytics.html** | `frontend/owner/` | لوحة ذكاء الأعمال الرئيسية — 4 tabs (إيرادات، عمليات، مناديب، تجار) + KPI cards + رسوم بيانية |
| 2 | **owner-analytics-page.js** | `frontend/src/js/pages/` | رسم بيانية بـ Chart.js (مضمنة في HTML كـ CDN). فلترة حسب الفترة. تحديث تلقائي. تصدير التقارير |
| 3 | **owner/analytics-detail.html** | `frontend/owner/` | تفاصيل كل محور تحليلي — جداول كاملة + charts مفصلة + مقارنات |
| 4 | **owner-analytics-detail-page.js** | `frontend/src/js/pages/` | عرض تفصيلي مع drill-down (النقر على منطقة → تفاصيلها) |
| 5 | **merchant/analytics.html** | `frontend/merchant/` | تحليلات التاجر — شحناتي + إيراداتي + أدائي |
| 6 | **merchant-analytics-page.js** | `frontend/src/js/pages/` | رسوم بيانية لشحنات ونسبة نجاح التاجر |

### 28.3 اختبارات (~32)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **RevenueAnalyticsServiceTest.java** | 7 | totalRevenue, byZone, byMerchant, profitMargin, costPerDelivery, avgValue, comparison (MoM) |
| 2 | **OperationsAnalyticsServiceTest.java** | 7 | firstAttemptRate, avgDeliveryTime, slaCompliance, returnRate, returnReasons, peakHours, bottleneck |
| 3 | **CourierAnalyticsServiceTest.java** | 5 | utilization, leaderboard, earningsDistribution, shipmentsPerDay, courierComparison |
| 4 | **MerchantAnalyticsServiceTest.java** | 5 | retention, topMerchants, growthRate, churnRisk, nps |
| 5 | **KPISnapshotServiceTest.java** | 4 | captureSnapshot, getSnapshot, getTrend, noData |
| 6 | **ReportExportServiceTest.java** | 4 | exportPdf, exportExcel, exportCsv, invalidDateRange |

---

## Sprint 29 — إدارة العقود والفواتير الإلكترونية المتقدمة

> **المدة:** 2 أسابيع  
> **الأولوية:** عالية — إضفاء الطابع الرسمي على العلاقات التجارية  
> **المتطلبات المسبقة:** SubscriptionService ✅ (Sprint 21)، InvoiceService ✅ (Sprint 21)، EInvoiceService ✅ (Sprint 25)، TaxService ✅ (Sprint 25)

### 29.0 نظرة عامة

```
إدارة العقود (Contract Management):
  Contract (عقد)
  ├── نوع العقد: MERCHANT_AGREEMENT / COURIER_AGREEMENT / PARTNER_AGREEMENT
  ├── بيانات الطرفين: الشركة + التاجر/المندوب
  ├── شروط التسعير المخصصة:
  │   ├── CustomPricingRule: override per merchant
  │   ├── خصم على الكمية (Volume Discount)
  │   ├── أسعار خاصة بالمنطقة
  │   └── رسوم COD مخصصة
  ├── SLA المتفق عليه:
  │   ├── نسبة النجاح المستهدفة
  │   ├── وقت التسليم الأقصى
  │   └── غرامات التأخير
  ├── مدة العقد: startDate → endDate
  ├── تجديد تلقائي: autoRenew + renewalNoticeDays
  ├── الحالات: DRAFT → PENDING_SIGNATURE → ACTIVE → EXPIRING_SOON → EXPIRED → TERMINATED
  └── التوقيع الإلكتروني (OTP-based)

نظام التسعير المخصص:
  عند حساب سعر شحنة لتاجر:
  1. هل يوجد عقد نشط مع تسعير مخصص؟ → استخدمه
  2. هل يوجد خصم كمية (Volume Discount)? → طبّقه
  3. وإلا → استخدم DeliveryPricing العام
  
  الفوائد:
  - التجار الكبار يحصلون على أسعار تنافسية
  - مرونة في التفاوض
  - تتبع الالتزام بشروط العقد

تقارير العقود:
  - عقود تنتهي قريباً (< 30 يوم)
  - نسبة التجديد (Renewal Rate)
  - إيرادات حسب نوع العقد
  - مخالفات SLA التعاقدي
```

### 29.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **Contract.java** | `domain/` | Entity: `id`, `contractNumber` (unique TWS-CTR-XXXXXXXX), `contractType` (enum: MERCHANT_AGREEMENT/COURIER_AGREEMENT/PARTNER_AGREEMENT), `party` (User — التاجر/المندوب), `startDate` (LocalDate), `endDate` (LocalDate), `status` (enum: DRAFT/PENDING_SIGNATURE/ACTIVE/EXPIRING_SOON/EXPIRED/TERMINATED), `autoRenew` (boolean), `renewalNoticeDays` (int default 30), `signedAt` (Instant nullable), `signatureOtp` (String nullable), `termsDocument` (TEXT — Markdown), `notes`, `createdBy` (User), `createdAt`, `updatedAt` |
| 2 | **CustomPricingRule.java** | `domain/` | Entity: `id`, `contract` (ManyToOne), `zoneFrom` (Zone nullable), `zoneTo` (Zone nullable), `shipmentType` (String nullable — filter), `basePrice` (BigDecimal), `perKgPrice` (BigDecimal), `codFeePercent` (BigDecimal), `minimumCharge` (BigDecimal), `discountPercent` (BigDecimal nullable — volume discount), `minMonthlyShipments` (int — trigger for volume discount), `isActive` |
| 3 | **ContractSlaTerms.java** | `domain/` | Entity: `id`, `contract` (OneToOne), `targetDeliveryRate` (double — e.g., 0.95 for 95%), `maxDeliveryHours` (int — e.g., 48), `latePenaltyPerShipment` (BigDecimal — per late delivery), `lostPenaltyFixed` (BigDecimal — per lost shipment), `slaReviewPeriod` (enum: MONTHLY/QUARTERLY) |
| 4 | **ContractRepository.java** | `repository/` | `findByPartyId()`, `findByPartyIdAndStatus()`, `findActiveByPartyId(userId)` (@Query: status=ACTIVE and NOW between start/end), `findExpiringWithin(days)`, `findByStatus()`, `findByContractNumber()` |
| 5 | **CustomPricingRuleRepository.java** | `repository/` | `findByContractId()`, `findActiveByContractIdAndZones(contractId, zoneFromId, zoneToId)` |
| 6 | **ContractSlaTermsRepository.java** | `repository/` | `findByContractId()` |
| 7 | **ContractService.java** | `service/` | `createContract(type, partyId, startDate, endDate, terms)`, `sendForSignature(contractId)` → generates OTP to party's phone, `signContract(contractId, otp)` → verifies OTP → ACTIVE, `terminateContract(contractId, reason)`, `renewContract(contractId)`, `getContractsByParty(userId)`, `getExpiringContracts(days)`, `processAutoRenewals()` (@Scheduled daily — renew contracts with autoRenew=true and endDate approaching), `sendExpiryReminders()` (@Scheduled daily — notify parties 30/14/7 days before expiry) |
| 8 | **CustomPricingService.java** | `service/` | `calculatePrice(merchantId, zoneFrom, zoneTo, weightKg, codAmount)` — checks active contract pricing first → falls back to DeliveryPricing, `addPricingRule(contractId, rule)`, `updatePricingRule(ruleId, updates)`, `getEffectivePricing(merchantId, zoneFrom, zoneTo)` → returns active rule or default |
| 9 | **ContractSlaService.java** | `service/` | `checkSlaCompliance(contractId, dateRange)` → compares actual rates vs targets → returns violations, `calculatePenalties(contractId, dateRange)` → total penalties for SLA violations, `getSlaReport(contractId)` → full compliance report |
| 10 | **ContractController.java** | `web/` | `POST /api/admin/contracts` (إنشاء — OWNER/ADMIN), `GET /api/admin/contracts` (كل العقود — OWNER/ADMIN), `GET /api/admin/contracts/{id}` (تفاصيل), `PUT /api/admin/contracts/{id}` (تعديل draft), `POST /api/admin/contracts/{id}/send-signature` (إرسال OTP), `POST /api/contracts/{id}/sign` (توقيع — MERCHANT/COURIER), `PUT /api/admin/contracts/{id}/terminate` (إنهاء), `GET /api/admin/contracts/expiring` (تنتهي قريباً), `GET /api/contracts/my` (عقودي — authenticated) |
| 11 | **ContractPricingController.java** | `web/` | `POST /api/admin/contracts/{id}/pricing` (إضافة قاعدة تسعير), `GET /api/admin/contracts/{id}/pricing` (قواعد العقد), `PUT /api/admin/contracts/pricing/{ruleId}` (تعديل), `GET /api/pricing/calculate` (params: merchantId, zoneFrom, zoneTo, weightKg, codAmount — حساب السعر الفعلي) |
| 12 | **ContractSlaController.java** | `web/` | `GET /api/admin/contracts/{id}/sla` (شروط SLA), `PUT /api/admin/contracts/{id}/sla` (تعديل شروط), `GET /api/admin/contracts/{id}/sla/compliance` (params: from, to — تقرير الالتزام), `GET /api/admin/contracts/{id}/sla/penalties` (params: from, to — الغرامات) |
| 13 | **ContractDTO.java** | `web/dto/` | Records: `CreateContractRequest(contractType, partyId, startDate, endDate, autoRenew, renewalNoticeDays, termsDocument, notes)`, `ContractResponse(id, contractNumber, contractType, partyId, partyName, startDate, endDate, status, autoRenew, signedAt, pricingRulesCount, createdAt)`, `SignContractRequest(otp)`, `CreatePricingRuleRequest(zoneFromId, zoneToId, basePrice, perKgPrice, codFeePercent, minimumCharge, discountPercent, minMonthlyShipments)`, `PricingRuleResponse(...)`, `PricingCalculationResponse(basePrice, weightCharge, codFee, discount, totalPrice, source — CONTRACT/DEFAULT)`, `SlaTermsRequest(targetDeliveryRate, maxDeliveryHours, latePenalty, lostPenalty, reviewPeriod)`, `SlaComplianceReport(contractId, period, targetRate, actualRate, isCompliant, totalShipments, lateShipments, lostShipments, totalPenalties)` |
| 14 | **V22__create_contract_tables.sql** | `db/migration/` | `contracts` (contract_number UNIQUE, contract_type, party_id FK, start_date, end_date, status, auto_renew, renewal_notice_days, signed_at, signature_otp, terms_document TEXT, notes, created_by FK, created_at, updated_at), `custom_pricing_rules` (contract_id FK, zone_from_id FK nullable, zone_to_id FK nullable, shipment_type, base_price, per_kg_price, cod_fee_percent, minimum_charge, discount_percent, min_monthly_shipments, is_active), `contract_sla_terms` (contract_id FK UNIQUE, target_delivery_rate, max_delivery_hours, late_penalty_per_shipment, lost_penalty_fixed, sla_review_period) + indexes |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | `/api/admin/contracts/**` → OWNER/ADMIN. `/api/contracts/my` → authenticated. `/api/contracts/{id}/sign` → authenticated. `/api/pricing/calculate` → authenticated |
| 2 | **DeliveryPricingService.java** (أو FinancialService) | تعديل `calculateShipmentCost()` → يفحص CustomPricingService أولاً قبل السعر الافتراضي |
| 3 | **OtpService.java** | إعادة استخدام لتوليد/تحقق OTP لتوقيع العقود |

### 29.2 Frontend

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **owner/contracts.html** | `frontend/owner/` | إدارة العقود — قائمة + إنشاء + تعديل شروط التسعير + SLA |
| 2 | **owner-contracts-page.js** | `frontend/src/js/pages/` | CRUD عقود + إدارة قواعد التسعير + عرض تقرير SLA |
| 3 | **merchant/contract.html** | `frontend/merchant/` | عقودي — عرض العقد الحالي + التوقيع الإلكتروني (OTP) |
| 4 | **merchant-contract-page.js** | `frontend/src/js/pages/` | عرض تفاصيل العقد + عملية التوقيع + الأسعار المخصصة |

### 29.3 اختبارات (~30)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **ContractServiceTest.java** | 9 | createContract, sendForSignature, signContract (valid OTP, invalid OTP), terminateContract, renewContract, autoRenew (eligible, expired), sendExpiryReminder |
| 2 | **CustomPricingServiceTest.java** | 7 | calculatePrice (contract exists, no contract → default), addRule, updateRule, volumeDiscount (eligible, below threshold), zoneSpecificPricing |
| 3 | **ContractSlaServiceTest.java** | 6 | checkCompliance (compliant, violation), calculatePenalties (late, lost, both), getSlaReport, noSlaTerms |
| 4 | **ContractControllerTest.java** | 4 | createContract, getContracts, signContract, getMyContracts |
| 5 | **ContractPricingControllerTest.java** | 4 | addPricingRule, getPricingRules, calculatePrice, updateRule |

---

## Sprint 30 — منصة API المفتوحة وتكامل منصات التجارة

> **المدة:** 2 أسابيع  
> **الأولوية:** عالية — فتح النظام البيئي وزيادة قاعدة التجار  
> **المتطلبات المسبقة:** WebhookService ✅ (Sprint 19)، SubscriptionService ✅ (Sprint 21)، ShipmentService ✅

### 30.0 نظرة عامة

```
منصة API المفتوحة (Open API Platform):
  ├── API Key Management:
  │   ├── كل تاجر يحصل على API Key (TWS-KEY-xxxxx) + Secret
  │   ├── Scopes: shipments:read, shipments:write, tracking:read, webhooks:manage
  │   ├── Rate Limiting حسب خطة الاشتراك (Free: 100/hr, Basic: 1000/hr, Pro: 10000/hr)
  │   ├── تدوير المفاتيح (Key Rotation)
  │   └── لوحة إحصائيات الاستخدام
  │
  ├── API Versioning:
  │   ├── /api/v2/* — النسخة الجديدة (SDK-friendly)
  │   ├── /api/v1/* — التوافقية مع القديم
  │   └── Deprecation notices في Headers
  │
  └── Developer Documentation:
      ├── Swagger/OpenAPI spec (موجود — يحتاج تطوير)
      ├── نماذج كود (Code Samples) لكل endpoint
      └── Sandbox environment (test mode)

تكامل منصات التجارة (E-Commerce Integrations):
  ├── Shopify:
  │   ├── Webhook receiver (order.created → auto-create shipment)
  │   ├── Fulfillment API (update Shopify when delivered)
  │   └── Product sync (لحساب الوزن والأبعاد)
  │
  ├── WooCommerce:
  │   ├── REST API integration (order webhook → shipment)
  │   ├── Status sync (Twsela status → WooCommerce order status)
  │   └── Shipping Calculator plugin
  │
  ├── Salla (منصة سلة — السعودية):
  │   ├── Webhook receiver (order events)
  │   ├── Fulfillment update
  │   └── COD support
  │
  └── Zid (منصة زد — السعودية):
      ├── Webhook receiver
      ├── Fulfillment sync
      └── Product weight/dimensions

نموذج التكامل الموحد:
  ECommerceIntegration (Interface)
  ├── parseOrder(payload) → ShipmentCreateRequest
  ├── updateFulfillment(shipmentId, status)
  ├── validateWebhook(payload, signature)
  └── getOrderDetails(externalOrderId)

  عند وصول webhook من Shopify/Salla/etc:
  1. التحقق من التوقيع
  2. تحويل الطلب إلى شحنة Twsela
  3. إنشاء الشحنة تلقائياً
  4. عند تغيير حالة الشحنة → تحديث المنصة المصدرية
```

### 30.1 Backend

#### ملفات جديدة

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **ApiKey.java** | `domain/` | Entity: `id`, `merchant` (ManyToOne User), `keyValue` (String unique — TWS-KEY-xxxxx), `secretHash` (String — BCrypt hashed), `name` (String — descriptive label), `scopes` (String — comma-separated: "shipments:read,shipments:write,tracking:read"), `rateLimit` (int — requests per hour, from subscription plan), `isActive` (boolean), `lastUsedAt` (Instant nullable), `requestCount` (long — total API calls), `expiresAt` (Instant nullable), `createdAt` |
| 2 | **ApiKeyUsageLog.java** | `domain/` | Entity: `id`, `apiKeyId` (Long), `endpoint` (String), `method` (String), `responseStatus` (int), `ipAddress` (String), `userAgent` (String), `requestedAt` (Instant) — مسجل لتحليل الاستخدام |
| 3 | **ECommerceConnection.java** | `domain/` | Entity: `id`, `merchant` (ManyToOne User), `platform` (enum: SHOPIFY/WOOCOMMERCE/SALLA/ZID), `storeName` (String), `storeUrl` (String), `accessToken` (String — encrypted), `webhookSecret` (String), `isActive` (boolean), `autoCreateShipments` (boolean default true), `defaultZoneId` (Zone nullable), `lastSyncAt` (Instant), `syncErrors` (int default 0), `createdAt` |
| 4 | **ECommerceOrder.java** | `domain/` | Entity: `id`, `connection` (ManyToOne ECommerceConnection), `externalOrderId` (String), `externalOrderNumber` (String), `shipment` (ManyToOne Shipment nullable), `platform` (enum), `status` (enum: RECEIVED/SHIPMENT_CREATED/FULFILLED/FAILED), `rawPayload` (TEXT — JSON), `errorMessage` (String nullable), `receivedAt`, `processedAt` |
| 5 | **ApiKeyRepository.java** | `repository/` | `findByKeyValue()`, `findByMerchantId()`, `findByMerchantIdAndIsActiveTrue()`, `findByIsActiveTrue()` |
| 6 | **ApiKeyUsageLogRepository.java** | `repository/` | `countByApiKeyIdAndRequestedAtBetween()`, `findByApiKeyIdOrderByRequestedAtDesc()` |
| 7 | **ECommerceConnectionRepository.java** | `repository/` | `findByMerchantId()`, `findByMerchantIdAndPlatform()`, `findByIsActiveTrue()`, `findByPlatformAndIsActiveTrue()` |
| 8 | **ECommerceOrderRepository.java** | `repository/` | `findByExternalOrderIdAndPlatform()`, `findByConnectionId()`, `findByStatus()`, `countByConnectionIdAndStatus()` |
| 9 | **ApiKeyService.java** | `service/` | `generateApiKey(merchantId, name, scopes)` → ApiKey (generates TWS-KEY-xxx + random secret → hash secret → return plain secret ONCE), `rotateKey(keyId)` → deactivate old + generate new, `revokeKey(keyId)`, `validateKey(keyValue, secret)` → ApiKey or null, `getKeysByMerchant(merchantId)`, `getUsageStats(keyId, dateRange)`, `enforceRateLimit(keyId)` → boolean (Redis-backed sliding window counter) |
| 10 | **ApiKeyAuthFilter.java** | `config/` | OncePerRequestFilter: intercepts `/api/v2/**` requests → reads `X-API-Key` + `X-API-Secret` headers → validates via ApiKeyService → sets SecurityContext → logs usage. Skips if JWT already present |
| 11 | **ECommerceIntegration.java** | `service/` | **Interface**: `parseOrder(String rawPayload)` → ShipmentCreateDTO, `updateFulfillment(Shipment shipment, String status)`, `validateWebhook(String payload, String signature, String secret)` → boolean, `getPlatform()` → ECommercePlatform |
| 12 | **ShopifyIntegration.java** | `service/` | Implements ECommerceIntegration for Shopify. `parseOrder()` → maps Shopify order JSON to ShipmentCreateDTO (recipient address, items, weight, COD). `updateFulfillment()` → POST to Shopify Fulfillment API. `validateWebhook()` → HMAC-SHA256 verification |
| 13 | **WooCommerceIntegration.java** | `service/` | Implements ECommerceIntegration for WooCommerce. Maps WC order to shipment. Status sync via WC REST API |
| 14 | **SallaIntegration.java** | `service/` | Implements ECommerceIntegration for Salla (Arabic market). Maps Salla order to shipment. OAuth token-based API |
| 15 | **ZidIntegration.java** | `service/` | Implements ECommerceIntegration for Zid (Saudi market). Maps Zid order to shipment |
| 16 | **ECommerceIntegrationFactory.java** | `service/` | Factory: `getIntegration(platform)` → returns appropriate implementation |
| 17 | **ECommerceService.java** | `service/` | `connectStore(merchantId, platform, storeUrl, accessToken)`, `disconnectStore(connectionId)`, `processIncomingOrder(connectionId, rawPayload, signature)` → validate → parse → create shipment → save ECommerceOrder, `syncFulfillment(shipmentId)` → sends status back to origin platform, `retryFailedOrders(connectionId)`, `getConnectionStats(connectionId)` |
| 18 | **ApiKeyController.java** | `web/` | `POST /api/developer/keys` (إنشاء — MERCHANT), `GET /api/developer/keys` (مفاتيحي), `PUT /api/developer/keys/{id}/rotate` (تدوير), `DELETE /api/developer/keys/{id}` (إلغاء), `GET /api/developer/keys/{id}/usage` (إحصائيات) |
| 19 | **ECommerceController.java** | `web/` | `POST /api/integrations/connect` (ربط متجر — MERCHANT), `GET /api/integrations/connections` (اتصالاتي), `DELETE /api/integrations/{id}` (فصل), `GET /api/integrations/{id}/orders` (طلبات المنصة), `POST /api/integrations/{id}/retry` (إعادة محاولة الفاشلة), `GET /api/integrations/{id}/stats` (إحصائيات) |
| 20 | **ECommerceWebhookController.java** | `web/` | `POST /api/ecommerce/webhook/shopify/{connectionId}` (بدون JWT — يتحقق بالتوقيع), `POST /api/ecommerce/webhook/woocommerce/{connectionId}`, `POST /api/ecommerce/webhook/salla/{connectionId}`, `POST /api/ecommerce/webhook/zid/{connectionId}` |
| 21 | **DeveloperDTO.java** | `web/dto/` | Records: `CreateApiKeyRequest(name, scopes)`, `ApiKeyResponse(id, name, keyValue, scopes, rateLimit, isActive, lastUsedAt, requestCount, createdAt)`, `ApiKeyCreatedResponse(id, keyValue, secret — shown ONCE)`, `ApiKeyUsageResponse(totalRequests, requestsByDay List, topEndpoints List, errorRate)`, `ConnectStoreRequest(platform, storeUrl, accessToken, webhookSecret, defaultZoneId)`, `ConnectionResponse(id, platform, storeName, storeUrl, isActive, autoCreate, lastSyncAt, syncErrors)`, `ECommerceOrderResponse(id, externalOrderId, platform, status, shipmentId, receivedAt, processedAt, errorMessage)` |
| 22 | **V23__create_api_key_tables.sql** | `db/migration/` | `api_keys` (merchant_id FK, key_value UNIQUE, secret_hash, name, scopes, rate_limit, is_active, last_used_at, request_count, expires_at, created_at), `api_key_usage_log` (api_key_id FK, endpoint, method, response_status, ip_address, user_agent, requested_at) + indexes |
| 23 | **V24__create_ecommerce_tables.sql** | `db/migration/` | `ecommerce_connections` (merchant_id FK, platform, store_name, store_url, access_token, webhook_secret, is_active, auto_create_shipments, default_zone_id FK nullable, last_sync_at, sync_errors, created_at, UNIQUE(merchant_id, platform)), `ecommerce_orders` (connection_id FK, external_order_id, external_order_number, shipment_id FK nullable, platform, status, raw_payload TEXT, error_message, received_at, processed_at) + indexes |

#### تعديلات

| # | الملف | التعديل |
|---|-------|---------|
| 1 | **SecurityConfig.java** | `/api/v2/**` → يمر عبر ApiKeyAuthFilter (API Key OR JWT). `/api/developer/**` → authenticated (MERCHANT/OWNER). `/api/integrations/**` → authenticated (MERCHANT/OWNER). `/api/ecommerce/webhook/**` → permitAll (يتحقق داخلياً بالتوقيع) |
| 2 | **ShipmentService.java** | إضافة event عند تغيير حالة الشحنة → `eCommerceService.syncFulfillment()` إذا كانت الشحنة من منصة خارجية |
| 3 | **UsageTrackingService.java** | تتبع API calls عبر API Key |
| 4 | **pom.xml** | إضافة `commons-codec` لـ HMAC verification (إذا لم يكن موجوداً) |

### 30.2 Frontend

| # | الملف | المسار | الوصف |
|---|-------|--------|-------|
| 1 | **merchant/developer.html** | `frontend/merchant/` | بوابة المطور — إدارة API Keys + وثائق مبسطة + نماذج كود |
| 2 | **merchant-developer-page.js** | `frontend/src/js/pages/` | CRUD API Keys + عرض إحصائيات + نسخ المفتاح |
| 3 | **merchant/integrations.html** | `frontend/merchant/` | ربط المتاجر — Shopify/WooCommerce/Salla/Zid + حالة الاتصال + طلبات |
| 4 | **merchant-integrations-page.js** | `frontend/src/js/pages/` | ربط/فصل متجر + عرض الطلبات المستوردة + إعادة المحاولة للفاشلة |
| 5 | **admin/api-analytics.html** | `frontend/admin/` | إحصائيات API الشاملة — استخدام حسب التاجر/المفتاح + Rate Limit alerts |

### 30.3 اختبارات (~35)

| # | ملف الاختبار | عدد الاختبارات | يغطي |
|---|-------------|----------------|-------|
| 1 | **ApiKeyServiceTest.java** | 8 | generateKey, validateKey (valid, invalid, expired, inactive), rotateKey, revokeKey, enforceRateLimit (within, exceeded) |
| 2 | **ApiKeyAuthFilterTest.java** | 5 | validApiKey → authenticated, invalidKey → 401, missingHeaders → skip (JWT path), expiredKey → 401, rateLimitExceeded → 429 |
| 3 | **ShopifyIntegrationTest.java** | 5 | parseOrder (standard, withCOD, missingFields), validateWebhook (valid, invalid), updateFulfillment |
| 4 | **SallaIntegrationTest.java** | 3 | parseOrder, validateWebhook, updateFulfillment |
| 5 | **ECommerceServiceTest.java** | 8 | connectStore, processOrder (success, duplicate order, invalid payload, webhook invalid signature), syncFulfillment, retryFailed, disconnectStore |
| 6 | **ApiKeyControllerTest.java** | 3 | createKey, getMyKeys, rotateKey |
| 7 | **ECommerceControllerTest.java** | 3 | connectStore, getConnections, processWebhook |

---

## ملخص التبعيات بين السبرنتات

```
Sprint 26 (POD & Pickup Scheduling)
  ├── يعتمد على: ShipmentService ✅, CourierLocationService ✅ (Sprint 16)
  ├── يعتمد على: SmartAssignmentService ✅ (Sprint 24) — لتعيين المندوب للاستلام
  └── يوفر: DeliveryProof data, PickupSchedule, FileStorageService

Sprint 27 (Advanced Notifications)
  ├── يعتمد على: NotificationService ✅, TwilioSmsService ✅
  ├── يعتمد على: كل الخدمات (لإرسال إشعارات عند الأحداث)
  └── يوفر: NotificationDispatcher متعدد القنوات, templates, preferences

Sprint 28 (BI Dashboard & Analytics)
  ├── يعتمد على: كل الخدمات السابقة (يقرأ بيانات من الجميع)
  ├── يستفيد من: Sprint 26 POD (first attempt rate, failure reasons)
  └── يوفر: KPI snapshots, export reports, executive summary

Sprint 29 (Contract Management)
  ├── يعتمد على: Sprint 21 (Subscriptions), Sprint 25 (EInvoice, Tax)
  ├── يعتمد على: DeliveryPricing ✅, OtpService ✅
  └── يوفر: Custom pricing per merchant, SLA enforcement, penalties

Sprint 30 (API Platform & E-Commerce)
  ├── يعتمد على: Sprint 19 (Webhooks), Sprint 21 (Subscriptions — rate limit)
  ├── يعتمد على: ShipmentService ✅ (auto-create shipments)
  └── يوفر: API Key auth, e-commerce auto-imports, fulfillment sync
```

```
الترتيب المُوصى به:
26 → 27 → 28 → 29 → 30
(التسلسل منطقي: 26 يوفر بيانات POD لـ 28، 27 مستقل لكنه يُحسّن UX مبكراً)
```

---

## المخاطر والتخفيف

| المخاطر | الاحتمال | التخفيف |
|---------|---------|---------|
| حجم ملفات الصور/التوقيعات (Sprint 26) | متوسط | تحديد حد أقصى 5MB + ضغط الصور قبل التخزين + FileStorageService قابل للاستبدال بـ S3 |
| تعقيد تكامل SendGrid/FCM/WhatsApp (Sprint 27) | عالي | Interface pattern لكل قناة + Mock implementations للاختبارات + Retry mechanism |
| أداء الاستعلامات التحليلية (Sprint 28) | متوسط | KPI snapshots يومية (pre-computed) بدلاً من استعلامات حية + indexes على التواريخ |
| تعقيد التسعير المخصص — تضارب القواعد (Sprint 29) | متوسط | أولوية واضحة: Contract pricing > Volume discount > Default pricing. قواعد أكثر تحديداً (zone-specific) تأخذ أولوية |
| أمان API Keys — تسريب المفاتيح (Sprint 30) | عالي | Secret يُعرض مرة واحدة فقط، BCrypt hash مخزن، Rate limiting, IP whitelist (مستقبلاً), Key rotation |
| تنوع بنية Webhooks بين المنصات (Sprint 30) | متوسط | Adapter pattern — كل منصة لها Implementation مستقل، الحفظ الخام (rawPayload) للتشخيص |
| تجاوز Rate Limit بعدد كبير من API Keys (Sprint 30) | منخفض | Rate limit يُربط بالتاجر (كل مفاتيحه مجتمعة)، Redis sliding window counter |

---

## المقاييس المستهدفة بعد Sprint 30

| المقياس | القيمة الحالية (Sprint 25) | المستهدف (Sprint 30) |
|---------|---------------------------|---------------------|
| **اختبارات ناجحة** | 430 | ~600 |
| **ملفات Java Backend** | ~255 | ~350 |
| **ملفات Frontend JS** | ~30 | ~53 |
| **Flyway Migrations** | 16 (V1-V16) | 24 (V1-V24) |
| **API Endpoints** | ~160 | ~225 |
| **Domain Entities** | 61 | ~75 |
| **ميزات المرحلة 1 المكتملة** | 15/20 | 18/20 |
| **ميزات المرحلة 2 المكتملة** | 5/22 | 9/22 |
| **ميزات المرحلة 3 المكتملة** | 4/18 | 6/18 |
| **ميزات المرحلة 4 المكتملة** | 0/15 | 2/15 |

---

## ما بعد Sprint 30 — نظرة مستقبلية

| السبرنت | الموضوع المقترح |
|---------|----------------|
| **31** | نظام الحوافز والمكافآت (Incentives) + برنامج الإحالة (Referral) |
| **32** | نظام الفروع المتعددة (Branches) + إدارة المناطق المتقدمة (Polygon Zones) |
| **33** | Chatbot ذكي + OCR للعناوين + Geocoding |
| **34** | نظام الخصومات والكوبونات + Express/Same-Day Delivery |
| **35** | تطبيق المندوب (Flutter/React Native) + Offline Mode |

---

> **الخطوة التالية:** تنفيذ Sprint 26 — إثبات التسليم (POD) وجدولة الاستلام
