# خطة السبرنتات 31–35 — Twsela Courier Management System

> **تاريخ الإعداد:** 3 مارس 2026  
> **الأساس:** 600 اختبار ناجح | 329+ ملف Java | 24 Migration | 76 Entity | 67 Service | 48 Controller  
> **الهدف:** 5 سبرنتات كبرى تُحوّل Twsela من نظام إدارة شحنات إلى منصة لوجستية ذكية متكاملة

---

## الهيكل الحالي (بعد Sprint 30)

| القياس | القيمة |
|--------|--------|
| الكيانات (Entities) | 76 |
| الخدمات (Services) | 67 |
| المتحكمات (Controllers) | 48 |
| المستودعات (Repositories) | 68 |
| DTOs | 42 |
| Flyway Migrations | V1–V24 |
| الاختبارات | 600 |
| نقاط API | ~225 |

### ما تم بناؤه (Sprints 1–30)
- نظام المستخدمين والأدوار والصلاحيات
- دورة حياة الشحنة الكاملة (CREATED → DELIVERED/RETURNED)
- المالية: المحافظ، المدفوعات، الفواتير، التسويات
- الاشتراكات والخطط للتجار
- تتبع المندوبين GPS + إثبات التسليم (POD)
- إدارة المرتجعات والمستودعات
- نظام الإشعارات متعدد القنوات (SMS/Email/Push/WhatsApp)
- قوالب الإشعارات وتفضيلات المستخدم
- الباركود والتسميات وبوليصة الشحن (AWB)
- التقييمات والشارات وأداء المناديب
- لوحة BI + تحليلات الإيرادات/العمليات/المناديب/التجار
- العقود والتسعير المخصص و SLA
- الأسطول (المركبات والصيانة والوقود)
- نظام التذاكر والدعم الفني
- الضرائب والفواتير الإلكترونية (مصر/السعودية/الإمارات)
- الدول والعملات وأسعار الصرف
- التوزيع الذكي وتحسين المسارات
- التنبؤ بالطلب والتسعير الديناميكي
- API مفتوح + API Keys + E-Commerce Integrations
- Webhooks ونظام الأحداث

---

## نظرة عامة على السبرنتات 31–35

| Sprint | العنوان | التأثير الرئيسي |
|--------|---------|----------------|
| **31** | **Real-time WebSocket Layer + Live Tracking** | بنية تحتية كاملة للاتصال الفوري + تتبع حي على الخريطة |
| **32** | **Payment Gateway Integration + COD Digitization** | 4 بوابات دفع + تحصيل إلكتروني بدل COD + تسوية تلقائية |
| **33** | **Security Hardening & Compliance Engine** | إصلاح 12 ثغرة حرجة + محرك امتثال + تدقيق أمني كامل |
| **34** | **Event-Driven Architecture + Message Queue** | Kafka/RabbitMQ + Event Sourcing + CQRS + Async Processing |
| **35** | **Multi-Tenant Architecture + White-Label Platform** | نظام متعدد المستأجرين + Tenant Isolation + Branding Engine |

---

## Sprint 31 — Real-time WebSocket Layer + Live Tracking

> **الهدف:** بناء طبقة اتصال فورية كاملة تمكّن التتبع الحي والإشعارات اللحظية والتواصل المباشر

### المتطلبات

#### الكيانات الجديدة (5)

| الكيان | الوصف | الحقول الرئيسية |
|--------|-------|----------------|
| `TrackingSession` | جلسة تتبع حية لشحنة | shipment, courier, status(ACTIVE/PAUSED/ENDED), startedAt, lastPingAt, estimatedArrival, currentLat, currentLng |
| `LocationPing` | نقطة موقع من المندوب | trackingSession, lat, lng, accuracy, speed, heading, batteryLevel, timestamp |
| `LiveNotification` | إشعار فوري عبر WebSocket | user, type, title, body, payload(JSON), read, deliveredAt, readAt, createdAt |
| `ChatRoom` | غرفة محادثة بين الأطراف | shipment, participants(JSON), type(MERCHANT_COURIER/RECIPIENT_COURIER/SUPPORT), status, createdAt |
| `ChatMessage` | رسالة داخل غرفة | chatRoom, sender, messageType(TEXT/IMAGE/LOCATION/SYSTEM), content, readBy(JSON), sentAt |

#### المستودعات (5)
- `TrackingSessionRepository` — findByShipmentIdAndStatus, findByCourierIdAndStatus, findActiveByShipmentId
- `LocationPingRepository` — findByTrackingSessionIdOrderByTimestampDesc, countByTrackingSessionId, findLatestByCourierId
- `LiveNotificationRepository` — findByUserIdAndReadFalse, countByUserIdAndReadFalse, findByUserIdOrderByCreatedAtDesc
- `ChatRoomRepository` — findByShipmentId, findByParticipantsContaining, findActiveByUserId
- `ChatMessageRepository` — findByChatRoomIdOrderBySentAtAsc, countByChatRoomIdAndReadByNotContaining

#### الخدمات (6)

| الخدمة | المسؤوليات |
|--------|-----------|
| `TrackingSessionService` | بدء/إيقاف/إنهاء جلسات التتبع، تسجيل نقاط الموقع، حساب ETA باستخدام Haversine formula، اكتشاف التوقف غير الطبيعي |
| `LiveTrackingService` | استقبال ونشر تحديثات الموقع عبر WebSocket، إدارة الاشتراكات في التتبع، تجميع وضغط النقاط قبل الإرسال |
| `LiveNotificationService` | إرسال إشعارات فورية عبر WebSocket، إدارة حالة الاتصال (online/offline)، تخزين مؤقت للإشعارات غير المُسلّمة |
| `ChatService` | إنشاء غرف محادثة، إرسال/استقبال رسائل، إدارة حالة القراءة، حذف وأرشفة المحادثات |
| `PresenceService` | تتبع حالة اتصال المستخدمين (online/offline/away)، آخر نشاط، عدد المتصلين الحاليين |
| `ETACalculationService` | حساب وقت الوصول المتوقع بناءً على المسافة والسرعة والمسار، تحديث ETA ديناميكياً |

#### المتحكمات (3)

| المتحكم | المسارات | الوصف |
|---------|---------|-------|
| `LiveTrackingController` | `GET /api/tracking/sessions/{shipmentId}` — جلسة التتبع الحالية | REST endpoints لإدارة التتبع |
| | `POST /api/tracking/sessions/{shipmentId}/start` — بدء التتبع | |
| | `POST /api/tracking/sessions/{shipmentId}/stop` — إيقاف التتبع | |
| | `GET /api/tracking/sessions/{shipmentId}/eta` — وقت الوصول المتوقع | |
| | `GET /api/tracking/sessions/{shipmentId}/history` — سجل المواقع | |
| `ChatController` | `POST /api/chat/rooms` — إنشاء غرفة | REST endpoints للمحادثات |
| | `GET /api/chat/rooms` — غرف المستخدم | |
| | `GET /api/chat/rooms/{roomId}/messages` — رسائل الغرفة | |
| | `POST /api/chat/rooms/{roomId}/messages` — إرسال رسالة | |
| | `PUT /api/chat/rooms/{roomId}/read` — تحديث حالة القراءة | |
| `WebSocketMessageController` | `@MessageMapping("/tracking.ping")` — استقبال نقطة موقع | STOMP message handlers |
| | `@MessageMapping("/tracking.subscribe")` — الاشتراك في تتبع | |
| | `@MessageMapping("/chat.send")` — إرسال رسالة chat | |
| | `@MessageMapping("/presence.heartbeat")` — نبضة حياة | |

#### البنية التحتية (تحديثات)
- تحديث `WebSocketConfig` الحالي — إضافة message broker وauthorization
- تحديث `WebSocketAuthInterceptor` الحالي — JWT validation للـ WebSocket handshake
- تحديث `SecurityConfig` — مسارات `/ws/**`, `/api/tracking/**`, `/api/chat/**`

#### Flyway Migrations (2)
- `V25__create_tracking_session_tables.sql` — tracking_sessions + location_pings + indexes
- `V26__create_chat_tables.sql` — live_notifications + chat_rooms + chat_messages + indexes

#### DTOs (2)
- `TrackingDTO` — TrackingSessionResponse, LocationPingRequest, ETAResponse, TrackingHistoryResponse
- `ChatDTO` — CreateRoomRequest, ChatRoomResponse, ChatMessageResponse, SendMessageRequest

#### الاختبارات (~40 اختبار جديد)
- `TrackingSessionServiceTest` — 8 اختبارات (start/stop/end session, register ping, calculate ETA, detect stale)
- `LiveTrackingServiceTest` — 6 اختبارات (publish location, subscribe, unsubscribe, compress pings)
- `LiveNotificationServiceTest` — 5 اختبارات (send, mark read, count unread, offline queue)
- `ChatServiceTest` — 8 اختبارات (create room, send message, mark read, archive, list rooms)
- `ETACalculationServiceTest` — 5 اختبارات (haversine distance, speed-based ETA, dynamic update)
- `LiveTrackingControllerTest` — 4 اختبارات (get session, start, stop, get ETA)
- `ChatControllerTest` — 4 اختبارات (create room, get rooms, send message, mark read)

---

## Sprint 32 — Payment Gateway Integration + COD Digitization

> **الهدف:** تحويل نظام الدفع من COD فقط إلى منصة دفع متكاملة تدعم Paymob + Tap + Stripe + Fawry مع تسوية تلقائية

### المتطلبات

#### الكيانات الجديدة (6)

| الكيان | الوصف | الحقول الرئيسية |
|--------|-------|----------------|
| `PaymentMethod` | وسيلة دفع مسجلة | user, type(CARD/WALLET/BANK_ACCOUNT/FAWRY), provider(PAYMOB/TAP/STRIPE/FAWRY), last4, brand, isDefault, active, metadata(JSON), tokenizedRef |
| `PaymentIntent` | نية دفع (قبل الإتمام) | shipment, amount, currency, status(PENDING/PROCESSING/SUCCEEDED/FAILED/REFUNDED), provider, providerRef, paymentMethod, metadata, attempts, expiresAt |
| `PaymentRefund` | طلب استرداد | paymentTransaction, amount, reason, status(PENDING/APPROVED/PROCESSED/REJECTED), providerRef, processedAt |
| `SettlementBatch` | دفعة تسوية | settlementNumber, period(DAILY/WEEKLY/BIWEEKLY/MONTHLY), startDate, endDate, totalTransactions, totalAmount, totalFees, netAmount, status(DRAFT/PENDING/PROCESSING/COMPLETED/FAILED), processedAt |
| `SettlementItem` | بند تسوية | batch, paymentTransaction, shipment, merchant, amount, fee, netAmount, type(COD/DELIVERY_FEE/REFUND/ADJUSTMENT) |
| `PaymentWebhookLog` | سجل webhooks من بوابات الدفع | provider, eventType, payload(TEXT), signature, verified, processedAt, error |

#### المستودعات (6)
- `PaymentMethodRepository` — findByUserId, findByUserIdAndIsDefaultTrue, findByTokenizedRef
- `PaymentIntentRepository` — findByShipmentId, findByStatus, findByProviderRef, findExpired
- `PaymentRefundRepository` — findByPaymentTransactionId, findByStatus, sumAmountByStatus
- `SettlementBatchRepository` — findByStatus, findByPeriodAndDate, findLatestByMerchant
- `SettlementItemRepository` — findByBatchId, sumAmountByBatchId, findByMerchantId
- `PaymentWebhookLogRepository` — findByProviderAndEventType, findUnprocessed

#### الخدمات (8)

| الخدمة | المسؤوليات |
|--------|-----------|
| `PaymobGateway` (implements PaymentGateway) | تنفيذ بوابة Paymob — authentication, payment keys, iframe URL, callback verification |
| `TapGateway` (implements PaymentGateway) | تنفيذ بوابة Tap Payments — charge creation, source management, webhook validation |
| `StripeGateway` (implements PaymentGateway) | تنفيذ بوابة Stripe — payment intents, webhook signature verification, refund processing |
| `FawryGateway` (implements PaymentGateway) | تنفيذ بوابة Fawry — reference number generation, status inquiry, HMAC validation |
| `PaymentIntentService` | إنشاء وإدارة نوايا الدفع، اختيار البوابة المناسبة، إعادة المحاولة عند الفشل |
| `PaymentRefundService` | معالجة طلبات الاسترداد، التحقق من الأهلية، إرسال الاسترداد للبوابة |
| `SettlementService` | إنشاء دفعات التسوية، حساب الصافي بعد العمولات، جدولة تلقائية (يومي/أسبوعي) |
| `PaymentWebhookProcessor` | معالجة callbacks من جميع البوابات، التحقق من التوقيع، تحديث حالة المعاملات |

#### المتحكمات (4)

| المتحكم | المسارات |
|---------|---------|
| `PaymentIntentController` | `POST /api/payments/intents` — إنشاء نية دفع |
| | `GET /api/payments/intents/{id}` — حالة النية |
| | `POST /api/payments/intents/{id}/confirm` — تأكيد الدفع |
| | `POST /api/payments/intents/{id}/cancel` — إلغاء |
| | `GET /api/payments/methods` — وسائل الدفع المحفوظة |
| | `POST /api/payments/methods` — إضافة وسيلة دفع |
| | `DELETE /api/payments/methods/{id}` — حذف وسيلة |
| `PaymentRefundController` | `POST /api/payments/refunds` — طلب استرداد |
| | `GET /api/payments/refunds/{id}` — حالة الاسترداد |
| | `GET /api/payments/refunds` — قائمة الاستردادات |
| | `POST /api/payments/refunds/{id}/approve` — الموافقة (OWNER/ADMIN) |
| `SettlementController` | `GET /api/settlements` — قائمة التسويات |
| | `GET /api/settlements/{id}` — تفاصيل التسوية |
| | `POST /api/settlements/generate` — إنشاء تسوية يدوية |
| | `GET /api/settlements/{id}/items` — بنود التسوية |
| | `POST /api/settlements/{id}/process` — تنفيذ التسوية |
| `PaymentCallbackController` | `POST /api/payments/callback/paymob` — Paymob webhook |
| | `POST /api/payments/callback/tap` — Tap webhook |
| | `POST /api/payments/callback/stripe` — Stripe webhook |
| | `POST /api/payments/callback/fawry` — Fawry webhook |

#### Flyway Migrations (2)
- `V27__create_payment_gateway_tables.sql` — payment_methods + payment_intents + payment_refunds + payment_webhook_logs + indexes
- `V28__create_settlement_tables.sql` — settlement_batches + settlement_items + indexes

#### DTOs (3)
- `PaymentIntentDTO` — CreateIntentRequest, IntentResponse, PaymentMethodRequest, PaymentMethodResponse
- `PaymentRefundDTO` — RefundRequest, RefundResponse, ApproveRefundRequest
- `SettlementDTO` — SettlementResponse, SettlementItemResponse, GenerateSettlementRequest

#### الاختبارات (~45 اختبار جديد)
- `PaymobGatewayTest` — 6 اختبارات (authenticate, create payment, verify callback, invalid signature)
- `TapGatewayTest` — 5 اختبارات (create charge, verify webhook, refund)
- `StripeGatewayTest` — 6 اختبارات (create intent, webhook signature, refund, cancel)
- `FawryGatewayTest` — 5 اختبارات (generate ref, status inquiry, HMAC validation)
- `PaymentIntentServiceTest` — 8 اختبارات (create, confirm, cancel, expired, retry, gateway selection)
- `PaymentRefundServiceTest` — 5 اختبارات (create, approve, reject, process, ineligible)
- `SettlementServiceTest` — 6 اختبارات (generate batch, calculate net, process, daily schedule)
- `PaymentIntentControllerTest` — 4 اختبارات (create, get, confirm, add method)

---

## Sprint 33 — Security Hardening & Compliance Engine

> **الهدف:** إصلاح جميع الثغرات الأمنية الحرجة (12 P0 + 15 P1) وبناء محرك امتثال أمني شامل

### المتطلبات

#### الكيانات الجديدة (5)

| الكيان | الوصف | الحقول الرئيسية |
|--------|-------|----------------|
| `SecurityEvent` | حدث أمني مسجل | user, eventType(LOGIN_SUCCESS/LOGIN_FAILURE/BRUTE_FORCE_DETECTED/TOKEN_REVOKED/PASSWORD_CHANGED/SUSPICIOUS_ACTIVITY/IP_BLOCKED), ipAddress, userAgent, details(JSON), severity(LOW/MEDIUM/HIGH/CRITICAL), createdAt |
| `AccountLockout` | قفل حساب بعد محاولات فاشلة | user, failedAttempts, lockoutStart, lockoutEnd, lockoutReason, autoUnlockAt, unlockedBy, unlockedAt |
| `IpBlacklist` | عناوين IP محظورة | ipAddress, reason, blockedBy, blockedAt, expiresAt, permanent |
| `ComplianceRule` | قاعدة امتثال | name, category(DATA_PROTECTION/ACCESS_CONTROL/ENCRYPTION/AUDIT/PASSWORD_POLICY), description, severity, checkExpression, enabled, lastCheckedAt, lastResult(PASS/FAIL/WARNING) |
| `ComplianceReport` | تقرير امتثال | generatedBy, reportDate, totalRules, passedRules, failedRules, warningRules, details(JSON), status(DRAFT/FINAL), createdAt |

#### المستودعات (5)
- `SecurityEventRepository` — findByUserIdOrderByCreatedAtDesc, countByEventTypeAndCreatedAtBetween, findByIpAddressAndEventType, findBySeverity
- `AccountLockoutRepository` — findByUserIdAndLockoutEndAfter, findActiveLockouts, findByUser
- `IpBlacklistRepository` — findByIpAddress, findByPermanentTrue, findNonExpired, findByBlockedBy
- `ComplianceRuleRepository` — findByCategoryAndEnabledTrue, findByEnabledTrue, findByLastResult
- `ComplianceReportRepository` — findLatest, findByReportDateBetween, findByStatus

#### الخدمات (8)

| الخدمة | المسؤوليات |
|--------|-----------|
| `SecurityEventService` | تسجيل الأحداث الأمنية، تحليل الأنماط المشبوهة، تنبيهات فورية عند حدث حرج |
| `AccountLockoutService` | قفل الحساب بعد 5 محاولات فاشلة، فتح تلقائي بعد 30 دقيقة، فتح يدوي من الإدارة |
| `IpBlockingService` | حظر عناوين IP، فحص كل طلب، حظر تلقائي عند brute force، قائمة بيضاء |
| `ComplianceService` | تشغيل فحوصات الامتثال، إنشاء تقارير، جدولة فحوصات دورية |
| `InputSanitizationService` | تنظيف المدخلات من XSS/SQL Injection، تطبيق على كل الطلبات |
| `SecureRandomService` | توليد OTP/كلمات مرور/tokens باستخدام SecureRandom، بديل لكل استخدامات Math.random و java.util.Random |
| `PasswordPolicyService` | فرض سياسة كلمات مرور قوية (حد أدنى 8 أحرف + أرقام + رموز)، تاريخ كلمات المرور، منع إعادة الاستخدام |
| `SecurityAuditService` | تدقيق أمني شامل لحالة النظام، فحص الإعدادات الخاطئة، تقرير أمني |

#### المتحكمات (3)

| المتحكم | المسارات |
|---------|---------|
| `SecurityEventController` | `GET /api/security/events` — قائمة الأحداث الأمنية |
| | `GET /api/security/events/summary` — ملخص الأحداث |
| | `GET /api/security/events/threats` — تهديدات نشطة |
| | `GET /api/security/lockouts` — الحسابات المقفلة |
| | `POST /api/security/lockouts/{userId}/unlock` — فتح حساب |
| `IpManagementController` | `GET /api/security/ip-blacklist` — القائمة السوداء |
| | `POST /api/security/ip-blacklist` — حظر IP |
| | `DELETE /api/security/ip-blacklist/{id}` — رفع الحظر |
| | `GET /api/security/ip-whitelist` — القائمة البيضاء |
| `ComplianceController` | `GET /api/compliance/rules` — قواعد الامتثال |
| | `POST /api/compliance/check` — تشغيل فحص |
| | `GET /api/compliance/reports` — تقارير الامتثال |
| | `GET /api/compliance/reports/{id}` — تقرير مفصل |
| | `GET /api/compliance/status` — حالة الامتثال العامة |

#### إصلاحات أمنية حرجة (تعديلات على ملفات موجودة)

| # | الإصلاح | الملف | التغيير |
|---|---------|-------|---------|
| 1 | إزالة DebugController | `DebugController.java` | **حذف كامل** |
| 2 | `anyRequest().authenticated()` بدل `permitAll()` | `SecurityConfig.java` | تغيير آخر قاعدة |
| 3 | SecureRandom بدل Random | `OtpService.java`, `SmsController.java`, `PublicController.java` | استبدال java.util.Random |
| 4 | إزالة OTP من الاستجابة | `SmsController.java` | حذف `response.put("otp", otp)` |
| 5 | DTOs بدل Entity binding | `MasterDataController.java` | استخدام DTOs مع `@Valid` |
| 6 | تأمين BackupService | `BackupService.java` | استخدام `--defaults-file` بدل CLI args |
| 7 | إصلاح courierId المثبت | `ShipmentController.java` | استخراج من SecurityContext |
| 8 | تقييد Actuator | `SecurityConfig.java`, `application.yml` | فقط health + prometheus |
| 9 | إزالة hardcoded credentials | `DataInitializer.java`, `application.yml` | متغيرات البيئة |
| 10 | Path traversal protection | `BackupController.java` | التحقق من المسار |
| 11 | تأمين file upload | `FileUploadService.java` | حجم + نوع + مسار خارجي |
| 12 | إصلاح forgot password | `PublicController.java` | إزالة logging + SecureRandom |

#### Filters جديدة (2)
- `IpBlacklistFilter` — فحص كل طلب ضد القائمة السوداء (OncePerRequestFilter)
- `InputSanitizationFilter` — تنظيف المدخلات (OncePerRequestFilter, `@Autowired(required = false)`)

#### Flyway Migrations (2)
- `V29__create_security_event_tables.sql` — security_events + account_lockouts + ip_blacklist + indexes
- `V30__create_compliance_tables.sql` — compliance_rules + compliance_reports + seed default rules

#### DTOs (2)
- `SecurityDTO` — SecurityEventResponse, LockoutResponse, UnlockRequest, IpBlockRequest, IpBlockResponse
- `ComplianceDTO` — ComplianceRuleResponse, ComplianceReportResponse, ComplianceCheckRequest, ComplianceStatusResponse

#### الاختبارات (~45 اختبار جديد)
- `SecurityEventServiceTest` — 7 اختبارات (record event, analyze patterns, detect brute force, alert critical)
- `AccountLockoutServiceTest` — 7 اختبارات (lock after 5 failures, auto unlock, manual unlock, check locked, reset)
- `IpBlockingServiceTest` — 6 اختبارات (block IP, check blocked, auto block brute force, whitelist, expiry)
- `ComplianceServiceTest` — 6 اختبارات (run check, generate report, schedule check, pass/fail/warning rules)
- `SecureRandomServiceTest` — 4 اختبارات (generate OTP, generate password, generate token, uniqueness)
- `PasswordPolicyServiceTest` — 5 اختبارات (validate strong, reject weak, history check, expiry)
- `SecurityEventControllerTest` — 4 اختبارات (list events, summary, threats, unlock)
- `ComplianceControllerTest` — 4 اختبارات (list rules, run check, get report, status)
- `IpManagementControllerTest` — 3 اختبارات (block, unblock, list)

---

## Sprint 34 — Event-Driven Architecture + Message Queue

> **الهدف:** تحويل البنية إلى Event-Driven Architecture مع Message Queue لمعالجة غير متزامنة، CQRS partial، وفصل الخدمات

### المتطلبات

#### الكيانات الجديدة (5)

| الكيان | الوصف | الحقول الرئيسية |
|--------|-------|----------------|
| `DomainEvent` | حدث نطاق مسجل | eventId(UUID), eventType, aggregateType, aggregateId, payload(JSON), metadata(JSON), version, publishedAt, processedAt, status(PENDING/PUBLISHED/PROCESSED/FAILED) |
| `EventSubscription` | اشتراك في نوع حدث | subscriberName, eventType, handlerClass, filterExpression, active, retryPolicy(JSON), lastProcessedAt, failureCount |
| `DeadLetterEvent` | أحداث فاشلة | originalEvent(DomainEvent), failureReason, failureCount, lastAttemptAt, nextRetryAt, resolved, resolvedBy, resolvedAt |
| `AsyncJob` | مهمة غير متزامنة | jobId(UUID), jobType, payload(JSON), status(QUEUED/RUNNING/COMPLETED/FAILED/CANCELLED), priority(1-10), scheduledAt, startedAt, completedAt, result(JSON), retryCount, maxRetries, errorMessage |
| `OutboxMessage` | Transactional Outbox Pattern | aggregateType, aggregateId, eventType, payload(JSON), published, publishedAt, createdAt |

#### المستودعات (5)
- `DomainEventRepository` — findByAggregateTypeAndAggregateId, findByStatusAndPublishedAtBefore, findByEventType
- `EventSubscriptionRepository` — findByEventTypeAndActiveTrue, findBySubscriberName, findActiveSubscriptions
- `DeadLetterEventRepository` — findUnresolved, findByEventType, countByResolved
- `AsyncJobRepository` — findByStatusAndScheduledAtBefore, findByJobType, findByStatusIn, findRunningJobs
- `OutboxMessageRepository` — findByPublishedFalse, findByPublishedFalseOrderByCreatedAtAsc

#### الخدمات (8)

| الخدمة | المسؤوليات |
|--------|-----------|
| `EventPublisher` | نشر أحداث النطاق، Transactional Outbox pattern — كتابة الحدث في الـ DB أولاً ثم نشره |
| `EventProcessor` | معالجة الأحداث من القائمة، توجيه لكل مشترك، إعادة المحاولة عند الفشل |
| `OutboxPoller` | @Scheduled polling للأحداث غير المنشورة، نشرها وتحديث الحالة |
| `DeadLetterService` | إدارة الأحداث الفاشلة، إعادة المعالجة يدوياً أو تلقائياً، تنبيهات |
| `AsyncJobService` | إنشاء وإدارة المهام غير المتزامنة، أولويات، حد أقصى للتشغيل المتزامن |
| `AsyncJobExecutor` | تنفيذ المهام — bulk shipment processing, report generation, settlement calculation, email campaigns |
| `ShipmentEventHandler` | معالجة أحداث الشحنات (STATUS_CHANGED, ASSIGNED, DELIVERED) — تشغيل الإشعارات، تحديث KPI، تسجيل التاريخ |
| `PaymentEventHandler` | معالجة أحداث الدفع (PAYMENT_RECEIVED, REFUND_PROCESSED) — تحديث المحافظ، إنشاء سجل مالي |

#### المتحكمات (3)

| المتحكم | المسارات |
|---------|---------|
| `EventController` | `GET /api/events` — قائمة الأحداث (مع فلاتر) |
| | `GET /api/events/{eventId}` — تفاصيل حدث |
| | `GET /api/events/subscriptions` — الاشتراكات النشطة |
| | `POST /api/events/subscriptions` — إنشاء اشتراك |
| | `PUT /api/events/subscriptions/{id}` — تعديل اشتراك |
| `DeadLetterController` | `GET /api/events/dead-letter` — الأحداث الفاشلة |
| | `POST /api/events/dead-letter/{id}/retry` — إعادة محاولة |
| | `POST /api/events/dead-letter/{id}/resolve` — تحديد كمحلول |
| | `GET /api/events/dead-letter/stats` — إحصائيات |
| `AsyncJobController` | `GET /api/jobs` — قائمة المهام |
| | `POST /api/jobs` — إنشاء مهمة |
| | `GET /api/jobs/{jobId}` — حالة المهمة |
| | `POST /api/jobs/{jobId}/cancel` — إلغاء مهمة |
| | `GET /api/jobs/stats` — إحصائيات المهام |

#### تعديلات على الخدمات الموجودة
- إضافة `eventPublisher.publish()` في: `ShipmentService` (عند تغيير الحالة)، `WalletService` (عند معاملة مالية)، `PaymentService` (عند دفع/استرداد)
- تحويل الإشعارات من متزامنة لغير متزامنة عبر: `NotificationDispatcher` → يستجيب لأحداث بدل استدعاء مباشر

#### Flyway Migrations (2)
- `V31__create_event_tables.sql` — domain_events + event_subscriptions + dead_letter_events + outbox_messages + indexes
- `V32__create_async_job_tables.sql` — async_jobs + indexes + seed event subscriptions

#### DTOs (2)
- `EventDTO` — DomainEventResponse, SubscriptionRequest, SubscriptionResponse, DeadLetterResponse
- `AsyncJobDTO` — AsyncJobRequest, AsyncJobResponse, JobStatsResponse

#### الاختبارات (~45 اختبار جديد)
- `EventPublisherTest` — 6 اختبارات (publish event, outbox write, idempotency, version increment)
- `EventProcessorTest` — 7 اختبارات (process event, route to subscribers, retry on failure, skip inactive)
- `OutboxPollerTest` — 5 اختبارات (poll unpublished, mark published, handle empty, batch processing)
- `DeadLetterServiceTest` — 5 اختبارات (move to DLQ, retry, resolve, stats, auto retry)
- `AsyncJobServiceTest` — 7 اختبارات (create job, execute, cancel, priority ordering, max concurrent, timeout)
- `ShipmentEventHandlerTest` — 5 اختبارات (status changed notification, KPI update, history record)
- `EventControllerTest` — 4 اختبارات (list events, get event, create subscription, list subscriptions)
- `AsyncJobControllerTest` — 4 اختبارات (create job, get status, cancel, list jobs)
- `DeadLetterControllerTest` — 3 اختبارات (list dead letters, retry, resolve)

---

## Sprint 35 — Multi-Tenant Architecture + White-Label Platform

> **الهدف:** تحويل النظام إلى منصة متعددة المستأجرين مع عزل كامل للبيانات ومحرك تخصيص العلامة التجارية

### المتطلبات

#### الكيانات الجديدة (7)

| الكيان | الوصف | الحقول الرئيسية |
|--------|-------|----------------|
| `Tenant` | مستأجر (شركة شحن) | tenantId(UUID), name, slug(unique), domain(unique), status(ACTIVE/SUSPENDED/TRIAL/CANCELLED), plan(FREE/BASIC/PRO/ENTERPRISE), contactName, contactPhone, contactEmail, settings(JSON), createdAt |
| `TenantBranding` | تخصيص العلامة التجارية | tenant, logoUrl, faviconUrl, primaryColor, secondaryColor, accentColor, fontFamily, companyNameAr, companyNameEn, taglineAr, taglineEn, footerText, customCSS, emailTemplate(JSON) |
| `TenantConfiguration` | إعدادات المستأجر | tenant, configKey, configValue, category(GENERAL/SHIPMENT/PAYMENT/NOTIFICATION/SMS/SECURITY), encrypted, description |
| `TenantUser` | ربط مستخدم بمستأجر | user, tenant, role(TENANT_OWNER/TENANT_ADMIN/TENANT_USER), active, joinedAt |
| `TenantQuota` | حصص وحدود المستأجر | tenant, quotaType(MAX_SHIPMENTS_MONTHLY/MAX_USERS/MAX_API_CALLS/MAX_STORAGE_MB/MAX_WEBHOOKS), maxValue, currentValue, resetPeriod(MONTHLY/DAILY/NEVER), lastResetAt |
| `TenantInvitation` | دعوة مستخدم لمستأجر | tenant, email, phone, role, invitedBy, token(UUID), status(PENDING/ACCEPTED/EXPIRED/CANCELLED), expiresAt |
| `TenantAuditLog` | سجل تدقيق خاص بالمستأجر | tenant, user, action, entityType, entityId, oldValues(JSON), newValues(JSON), ipAddress, createdAt |

#### المستودعات (7)
- `TenantRepository` — findBySlug, findByDomain, findByStatus, findByPlan
- `TenantBrandingRepository` — findByTenantId
- `TenantConfigurationRepository` — findByTenantIdAndConfigKey, findByTenantIdAndCategory
- `TenantUserRepository` — findByUserId, findByTenantId, findByUserIdAndTenantId, findByTenantIdAndRole
- `TenantQuotaRepository` — findByTenantIdAndQuotaType, findByTenantId, findExceeded
- `TenantInvitationRepository` — findByToken, findByTenantIdAndStatus, findExpired
- `TenantAuditLogRepository` — findByTenantIdOrderByCreatedAtDesc, findByTenantIdAndAction

#### الخدمات (8)

| الخدمة | المسؤوليات |
|--------|-----------|
| `TenantService` | CRUD للمستأجرين، تفعيل/تعليق، ترقية/تخفيض الخطة |
| `TenantBrandingService` | إدارة العلامة التجارية، رفع الشعار، إنشاء ثيم CSS ديناميكي |
| `TenantConfigService` | إدارة إعدادات المستأجر، تشفير القيم الحساسة (API keys, credentials)، توريث الإعدادات الافتراضية |
| `TenantContextService` | Tenant Resolution — تحديد المستأجر من: subdomain, custom domain, header (X-Tenant-ID), JWT claim. يوفر `ThreadLocal<Tenant>` |
| `TenantQuotaService` | فحص وتتبع الحصص، رفض العمليات عند تجاوز الحد، إعادة تعيين دورية |
| `TenantInvitationService` | إنشاء وإرسال الدعوات، قبول/رفض/انتهاء المدة |
| `TenantIsolationService` | عزل البيانات — Discriminator-based isolation (tenant_id column على كل جدول)، فحص كل query، منع الوصول عبر المستأجرين |
| `TenantMigrationService` | أدوات لنقل بيانات من نظام أحادي لنظام متعدد المستأجرين، إنشاء مستأجر من بيانات موجودة |

#### المتحكمات (4)

| المتحكم | المسارات |
|---------|---------|
| `TenantController` | `POST /api/tenants` — إنشاء مستأجر |
| | `GET /api/tenants` — قائمة المستأجرين (SUPER_ADMIN) |
| | `GET /api/tenants/{id}` — تفاصيل المستأجر |
| | `PUT /api/tenants/{id}` — تعديل |
| | `POST /api/tenants/{id}/suspend` — تعليق |
| | `POST /api/tenants/{id}/activate` — تفعيل |
| `TenantBrandingController` | `GET /api/tenants/{id}/branding` — الحصول على العلامة التجارية |
| | `PUT /api/tenants/{id}/branding` — تحديث العلامة التجارية |
| | `POST /api/tenants/{id}/branding/logo` — رفع شعار |
| | `GET /api/public/branding/{slug}` — العلامة التجارية العامة (بدون مصادقة) |
| `TenantUserController` | `GET /api/tenants/{id}/users` — مستخدمو المستأجر |
| | `POST /api/tenants/{id}/invitations` — دعوة مستخدم |
| | `GET /api/tenants/{id}/invitations` — الدعوات |
| | `POST /api/invitations/{token}/accept` — قبول دعوة |
| | `PUT /api/tenants/{id}/users/{userId}/role` — تغيير الدور |
| | `DELETE /api/tenants/{id}/users/{userId}` — إزالة مستخدم |
| `TenantQuotaController` | `GET /api/tenants/{id}/quotas` — الحصص الحالية |
| | `PUT /api/tenants/{id}/quotas/{type}` — تعديل حصة |
| | `GET /api/tenants/{id}/quotas/usage` — استخدام الحصص |

#### البنية التحتية

**Filters و Interceptors:**
- `TenantContextFilter` — `OncePerRequestFilter` مع `@Autowired(required = false)` يحدد المستأجر من كل طلب ويخزنه في `ThreadLocal`
- `TenantDataFilter` — Hibernate `@Filter` يضيف `WHERE tenant_id = :tenantId` تلقائياً على كل الاستعلامات

**تعديلات على الكيانات الموجودة:**
- إضافة `tenantId (Long)` لكل الكيانات الرئيسية: `User`, `Shipment`, `Zone`, `Wallet`, `Contract`, `ApiKey`
- إضافة `@FilterDef` و `@Filter` لعزل البيانات

#### Flyway Migrations (3)
- `V33__create_tenant_tables.sql` — tenants + tenant_branding + tenant_configurations + tenant_users + tenant_quotas + tenant_invitations + tenant_audit_logs + indexes
- `V34__add_tenant_id_to_existing_tables.sql` — إضافة `tenant_id` لكل الجداول الرئيسية + قيمة افتراضية للبيانات الموجودة + indexes
- `V35__seed_default_tenant.sql` — إنشاء مستأجر افتراضي (Default Tenant) وربط كل البيانات الموجودة به

#### DTOs (3)
- `TenantDTO` — CreateTenantRequest, TenantResponse, UpdateTenantRequest, TenantSummaryResponse
- `TenantBrandingDTO` — BrandingRequest, BrandingResponse
- `TenantUserDTO` — InvitationRequest, InvitationResponse, TenantUserResponse, ChangeRoleRequest, QuotaResponse, QuotaUsageResponse

#### الاختبارات (~50 اختبار جديد)
- `TenantServiceTest` — 8 اختبارات (create, activate, suspend, upgrade plan, find by slug/domain)
- `TenantBrandingServiceTest` — 5 اختبارات (update branding, upload logo, get by tenant, generate CSS)
- `TenantConfigServiceTest` — 6 اختبارات (set config, get config, encrypted value, category filter, default inheritance)
- `TenantContextServiceTest` — 7 اختبارات (resolve from subdomain, domain, header, JWT, unknown tenant, null)
- `TenantQuotaServiceTest` — 6 اختبارات (check quota, increment, exceed limit, reset monthly, multiple quotas)
- `TenantInvitationServiceTest` — 5 اختبارات (create invitation, accept, expire, cancel, duplicate)
- `TenantIsolationServiceTest` — 5 اختبارات (filter by tenant, cross-tenant blocked, super admin bypass)
- `TenantControllerTest` — 4 اختبارات (create, list, suspend, activate)
- `TenantBrandingControllerTest` — 3 اختبارات (get branding, update, public branding)
- `TenantUserControllerTest` — 4 اختبارات (invite, list users, accept invitation, change role)

---

## ملخص الأرقام المتوقعة بعد Sprint 35

| القياس | Sprint 30 (الحالي) | Sprint 35 (المتوقع) | الزيادة |
|--------|-------------------|--------------------|---------| 
| الكيانات | 76 | 104 (+28) | +37% |
| الخدمات | 67 | 105 (+38) | +57% |
| المتحكمات | 48 | 65 (+17) | +35% |
| المستودعات | 68 | 96 (+28) | +41% |
| DTOs | 42 | 54 (+12) | +29% |
| Migrations | V24 | V35 | +11 |
| الاختبارات | 600 | ~825 (+225) | +38% |
| نقاط API | ~225 | ~305 (+80) | +36% |

---

## خريطة التنفيذ

```
Sprint 31                Sprint 32                Sprint 33
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│  WebSocket +     │     │  Payment Gateway │     │  Security        │
│  Live Tracking   │────▶│  COD Digitize    │────▶│  Hardening +     │
│  + Chat          │     │  + Settlement    │     │  Compliance      │
│  ~40 tests       │     │  ~45 tests       │     │  ~45 tests       │
└──────────────────┘     └──────────────────┘     └──────────────────┘
                                                         │
                                                         ▼
                         Sprint 35                Sprint 34
                         ┌──────────────────┐     ┌──────────────────┐
                         │  Multi-Tenant +  │     │  Event-Driven    │
                         │  White-Label     │◀────│  Architecture +  │
                         │  Platform        │     │  Message Queue   │
                         │  ~50 tests       │     │  ~45 tests       │
                         └──────────────────┘     └──────────────────┘
```

### لماذا هذا الترتيب؟

1. **Sprint 31 (WebSocket)** — أول لأنه الأكثر طلباً من العملاء (تتبع حي) وأساس للـ Sprint 32 (إشعارات الدفع)
2. **Sprint 32 (Payment)** — يحتاج WebSocket لإشعارات الدفع الفورية + يُنتج إيرادات مباشرة
3. **Sprint 33 (Security)** — بعد Payment لأن بوابات الدفع تتطلب أمان عالي + compliance is mandatory
4. **Sprint 34 (Events)** — يحتاج Sprint 33 (الأمان) ليكون مكتملاً قبل بناء event system
5. **Sprint 35 (Multi-Tenant)** — آخر لأنه يعتمد على كل شيء ويُعيد هيكلة البنية

---

> **ملاحظة:** كل سبرنت هو تغيير كبير ومستقل يُضيف قدرة جديدة كاملة للنظام. الاختبارات ملزمة لكل سبرنت (لا يُعتبر مكتملاً بدون BUILD SUCCESS).
