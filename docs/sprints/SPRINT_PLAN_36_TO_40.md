# خطة السبرنتات 36–40 — Twsela Courier Management System

> **تاريخ الإعداد:** 3 مارس 2026  
> **الأساس:** 870 اختبار ناجح | 103 Entity | 103 Service | 68 Controller | V1–V35 Migrations  
> **الهدف:** 5 سبرنتات كبرى تُحوّل Twsela إلى منصة لوجستية مكتملة بميزات enterprise-grade

---

## الهيكل الحالي (بعد Sprint 35)

| القياس | القيمة |
|--------|--------|
| الكيانات (Entities) | 103 |
| الخدمات (Services) | 103 |
| المتحكمات (Controllers) | 68 |
| المستودعات (Repositories) | ~95 |
| DTOs | ~55 |
| Flyway Migrations | V1–V35 |
| الاختبارات | 870 |
| نقاط API | ~280+ |

### ما تم بناؤه (Sprints 1–35)
- نظام المستخدمين والأدوار والصلاحيات + Multi-Tenant Architecture
- دورة حياة الشحنة الكاملة + التتبع الحي GPS + WebSocket
- المالية: المحافظ، المدفوعات، 4 بوابات دفع، الفواتير، التسويات
- الاشتراكات والخطط + API Keys + E-Commerce Integrations
- إدارة المرتجعات والمستودعات والأسطول
- نظام الإشعارات متعدد القنوات + Chat + Presence
- تقييمات المناديب + BI Dashboard + تحليلات شاملة
- العقود و SLA + التسعير المخصص + الضرائب والفواتير الإلكترونية
- التوزيع الذكي + تحسين المسارات + التنبؤ بالطلب
- Security Hardening + Compliance Engine + IP Management
- Event-Driven Architecture + Async Jobs + Dead Letter Queue
- Webhooks + Event Subscriptions + Outbox Pattern
- التسعير الديناميكي + الدول والعملات + أسعار الصرف
- Tenant Isolation + White-Label Branding + Tenant Quotas

---

## نظرة عامة على السبرنتات 36–40

| Sprint | العنوان | التأثير الرئيسي |
|--------|---------|----------------|
| **36** | **Workflow Engine & Business Process Automation** | محرك أتمتة كامل — قواعد أعمال قابلة للتخصيص + مشغلات تلقائية + سلاسل عمل مرئية |
| **37** | **Advanced Warehouse & Fulfillment Center** | تحويل المستودع إلى مركز تنفيذ كامل — مناطق + رفوف + Wave Picking + Pick/Pack/Ship |
| **38** | **Customer Self-Service & Delivery Experience** | بوابة خدمة ذاتية للمستلم — جدولة التوصيل + إعادة التوجيه + تفضيلات + استطلاعات رضا |
| **39** | **Rate Limiting, Caching & Search Infrastructure** | بنية أداء enterprise — Token Bucket Rate Limiter + Redis Cache Layer + Full-Text Search + Feature Flags |
| **40** | **Gamification, Loyalty & Campaign Engine** | نظام تحفيز كامل — مستويات XP + إنجازات + لوحة متصدرين + برنامج ولاء + حملات ترويجية |

---

## Sprint 36 — Workflow Engine & Business Process Automation

> **الهدف:** بناء محرك أتمتة يسمح للمالك والتاجر بتعريف قواعد أعمال مخصصة تُنفّذ تلقائياً عند حدوث أحداث معينة — بمثابة "Zapier" داخلي للمنصة

### المتطلبات

#### الكيانات الجديدة (7)

| الكيان | الوصف | الحقول الرئيسية |
|--------|-------|----------------|
| `WorkflowDefinition` | تعريف سلسلة العمل | name, description, triggerEvent (enum: SHIPMENT_CREATED, STATUS_CHANGED, PAYMENT_RECEIVED, DELIVERY_FAILED, RETURN_REQUESTED, SLA_BREACHED, RATING_SUBMITTED, DAILY_SCHEDULE), isActive, version, tenant, createdBy(User), priority |
| `WorkflowStep` | خطوة ضمن السلسلة | workflowDefinition, stepOrder, stepType (enum: CONDITION, ACTION, DELAY, BRANCH, LOOP), configuration(TEXT/JSON — conditions & params), nextStepOnSuccess, nextStepOnFailure, timeout |
| `WorkflowExecution` | تنفيذ فعلي لسلسلة | workflowDefinition, triggerEntityType, triggerEntityId, status (enum: RUNNING, COMPLETED, FAILED, CANCELLED, PAUSED), startedAt, completedAt, context(TEXT/JSON), errorMessage |
| `WorkflowStepExecution` | تنفيذ خطوة واحدة | workflowExecution, workflowStep, status (enum: PENDING, RUNNING, COMPLETED, FAILED, SKIPPED), input(TEXT), output(TEXT), startedAt, completedAt, retryCount, errorMessage |
| `AutomationRule` | قاعدة أتمتة بسيطة (if/then) | name, description, triggerEvent, conditionExpression(TEXT — SpEL/JSON), actionType (enum: SEND_NOTIFICATION, CHANGE_STATUS, ASSIGN_COURIER, UPDATE_FIELD, CALL_WEBHOOK, CREATE_TICKET, ADD_TAG, SEND_SMS, SEND_EMAIL), actionConfig(TEXT/JSON), isActive, tenant, executionCount, lastTriggeredAt |
| `ScheduledTask` | مهمة مجدولة من المستخدم | name, description, taskType (enum: GENERATE_REPORT, EXPIRE_SHIPMENTS, SEND_REMINDERS, SYNC_INVENTORY, PROCESS_SETTLEMENTS, CLEANUP_DATA, CUSTOM_WEBHOOK), cronExpression, configuration(TEXT/JSON), isActive, tenant, lastRunAt, nextRunAt, lastRunStatus, lastRunDuration |
| `WorkflowTemplate` | قالب جاهز لسلاسل العمل | name, nameAr, description, descriptionAr, category (enum: SHIPMENT, PAYMENT, NOTIFICATION, ASSIGNMENT, RETURN, SUPPORT), templateDefinition(TEXT/JSON), isSystem(boolean), usageCount |

#### المستودعات (7)
- `WorkflowDefinitionRepository` — findByTenantIdAndIsActiveTrue, findByTriggerEvent, findByCreatedById
- `WorkflowStepRepository` — findByWorkflowDefinitionIdOrderByStepOrder, countByWorkflowDefinitionId
- `WorkflowExecutionRepository` — findByWorkflowDefinitionId, findByStatus, findByTriggerEntityTypeAndTriggerEntityId, countByStatusAndCreatedAtAfter
- `WorkflowStepExecutionRepository` — findByWorkflowExecutionIdOrderByStartedAtAsc, findByStatus
- `AutomationRuleRepository` — findByTriggerEventAndIsActiveTrue, findByTenantId, countByTenantIdAndIsActiveTrue
- `ScheduledTaskRepository` — findByIsActiveTrue, findByNextRunAtBeforeAndIsActiveTrue, findByTenantId
- `WorkflowTemplateRepository` — findByCategory, findByIsSystemTrue, findByIdIn

#### الخدمات (7)

| الخدمة | المسؤوليات |
|--------|-----------|
| `WorkflowDefinitionService` | CRUD لسلاسل العمل، إضافة/ترتيب/حذف خطوات، تفعيل/تعطيل، استنساخ من قالب، التحقق من صحة التعريف (validation)، versioning |
| `WorkflowEngineService` | المحرك الرئيسي — استقبال الأحداث → مطابقة مع السلاسل النشطة → إنشاء Execution → تنفيذ الخطوات تسلسليًا → معالجة CONDITION/ACTION/DELAY/BRANCH → retry عند الفشل |
| `WorkflowActionExecutor` | تنفيذ الأفعال: إرسال إشعار، تغيير حالة شحنة، تعيين مندوب، استدعاء webhook، إنشاء تذكرة دعم، تحديث حقول، إرسال SMS/Email |
| `WorkflowConditionEvaluator` | تقييم الشروط: مقارنة حقول (equals/contains/gt/lt)، AND/OR/NOT logic، تعبيرات SpEL، فحص وقت اليوم/اليوم من الأسبوع |
| `AutomationRuleService` | CRUD لقواعد الأتمتة البسيطة، مطابقة الأحداث مع القواعد، تنفيذ الفعل، تحديث عداد التنفيذ، تسجيل النتائج |
| `ScheduledTaskService` | CRUD للمهام المجدولة، حساب nextRunAt من cronExpression، تنفيذ المهام المُستحقة (@Scheduled فحص كل دقيقة)، تسجيل مدة التشغيل والحالة |
| `WorkflowTemplateService` | CRUD للقوالب، قوالب نظام جاهزة (System Templates)، إنشاء سلسلة من قالب، تصنيف حسب الفئة |

#### DTOs (3)
- `WorkflowDTO` — CreateWorkflowRequest, AddStepRequest, WorkflowResponse (with steps), WorkflowExecutionResponse, StepExecutionResponse
- `AutomationRuleDTO` — CreateRuleRequest (@NotBlank name, @NotNull triggerEvent, actionType, conditionExpression, actionConfig), RuleResponse (with executionCount/lastTriggered)
- `ScheduledTaskDTO` — CreateTaskRequest (@NotBlank name, cronExpression, taskType, configuration), TaskResponse (with lastRun/nextRun/status)

#### المتحكمات (4)

| المتحكم | المسارات | الوصف |
|---------|---------|-------|
| `WorkflowController` | `POST /api/workflows` — إنشاء سلسلة عمل | CRUD + خطوات |
| | `GET /api/workflows` — قائمة السلاسل | |
| | `GET /api/workflows/{id}` — تفاصيل مع الخطوات | |
| | `PUT /api/workflows/{id}` — تحديث | |
| | `POST /api/workflows/{id}/steps` — إضافة خطوة | |
| | `POST /api/workflows/{id}/activate` — تفعيل | |
| | `POST /api/workflows/{id}/deactivate` — تعطيل | |
| `WorkflowExecutionController` | `GET /api/workflows/{id}/executions` — سجل التنفيذات | مراقبة التنفيذ |
| | `GET /api/workflow-executions/{id}` — تفاصيل التنفيذ | |
| | `POST /api/workflow-executions/{id}/cancel` — إلغاء تنفيذ جارٍ | |
| | `GET /api/workflow-executions/{id}/steps` — خطوات التنفيذ | |
| `AutomationRuleController` | `POST /api/automation-rules` — إنشاء قاعدة | CRUD قواعد أتمتة |
| | `GET /api/automation-rules` — قائمة القواعد | |
| | `PUT /api/automation-rules/{id}` — تحديث | |
| | `DELETE /api/automation-rules/{id}` — حذف | |
| | `POST /api/automation-rules/{id}/toggle` — تفعيل/تعطيل | |
| `ScheduledTaskController` | `POST /api/scheduled-tasks` — إنشاء مهمة | CRUD مهام مجدولة |
| | `GET /api/scheduled-tasks` — قائمة المهام | |
| | `PUT /api/scheduled-tasks/{id}` — تحديث | |
| | `POST /api/scheduled-tasks/{id}/run-now` — تشغيل فوري | |
| | `POST /api/scheduled-tasks/{id}/toggle` — تفعيل/تعطيل | |

#### Flyway Migrations (2)
- `V36__create_workflow_tables.sql` — workflow_definitions + workflow_steps + workflow_executions + workflow_step_executions + workflow_templates + indexes + FK constraints
- `V37__create_automation_tables.sql` — automation_rules + scheduled_tasks + indexes

#### الاختبارات (~55 اختبار جديد)
- `WorkflowDefinitionServiceTest` — 8 اختبارات (create, addStep, reorderSteps, activate, deactivate, clone from template, validate circular, findByTrigger)
- `WorkflowEngineServiceTest` — 10 اختبارات (trigger event match, execute sequential steps, evaluate condition branch, handle action, handle delay, retry on failure, cancel running, skip on condition false, nested branch, timeout)
- `WorkflowActionExecutorTest` — 7 اختبارات (send notification, change status, assign courier, call webhook, create ticket, send SMS, unknown action error)
- `WorkflowConditionEvaluatorTest` — 6 اختبارات (equals condition, greater than, AND logic, OR logic, NOT logic, time-based condition)
- `AutomationRuleServiceTest` — 7 اختبارات (create rule, match trigger event, execute action, toggle active, duplicate trigger, count by tenant, update config)
- `ScheduledTaskServiceTest` — 7 اختبارات (create task, calculate next run, execute due tasks, record duration, run now, toggle active, invalid cron error)
- `WorkflowControllerTest` — 5 اختبارات (create workflow, list, add step, activate, forbidden non-owner)
- `AutomationRuleControllerTest` — 3 اختبارات (create rule, list, toggle)
- `ScheduledTaskControllerTest` — 3 اختبارات (create task, run now, toggle)

---

## Sprint 37 — Advanced Warehouse & Fulfillment Center

> **الهدف:** تحويل المستودع من مخزن بسيط إلى مركز تنفيذ (Fulfillment Center) كامل — إدارة المناطق والرفوف، عمليات الاستلام والتخزين، Wave Picking، وسلسلة Pick/Pack/Ship

### المتطلبات

#### الكيانات الجديدة (8)

| الكيان | الوصف | الحقول الرئيسية |
|--------|-------|----------------|
| `WarehouseZone` | منطقة داخل المستودع | warehouse, name, code(unique per warehouse), zoneType (enum: RECEIVING, STORAGE, PICKING, PACKING, SHIPPING, RETURNS, COLD_STORAGE, HAZMAT), capacity, currentOccupancy, isActive, sortOrder |
| `StorageBin` | رف/موقع تخزين محدد | warehouseZone, binCode(unique per warehouse), aisle, rack, shelf, position, binType (enum: STANDARD, LARGE, SMALL, FLOOR, PALLET), maxWeight, maxItems, currentItems, isOccupied, isActive |
| `ReceivingOrder` | أمر استلام بضاعة | warehouse, merchant, referenceNumber(unique), status (enum: EXPECTED, ARRIVED, INSPECTING, COMPLETED, REJECTED), expectedDate, arrivedAt, completedAt, totalExpectedItems, totalReceivedItems, notes, receivedBy(User) |
| `ReceivingOrderItem` | بند استلام | receivingOrder, productSku, productName, expectedQuantity, receivedQuantity, damagedQuantity, assignedBin(StorageBin), inspectionNotes, status (enum: PENDING, RECEIVED, PARTIAL, DAMAGED, MISSING) |
| `FulfillmentOrder` | أمر تنفيذ (Pick/Pack/Ship) | warehouse, shipment, merchant, orderNumber(unique), status (enum: PENDING, WAVE_ASSIGNED, PICKING, PICKED, PACKING, PACKED, READY_TO_SHIP, SHIPPED, CANCELLED), priority (enum: STANDARD, EXPRESS, URGENT), assignedPicker(User), assignedPacker(User), pickedAt, packedAt, shippedAt |
| `FulfillmentOrderItem` | بند تنفيذ | fulfillmentOrder, productSku, productName, quantity, pickedQuantity, sourceBin(StorageBin), pickSequence, isPicked |
| `PickWave` | موجة انتقاء جماعية | warehouse, waveNumber(unique), status (enum: CREATED, IN_PROGRESS, COMPLETED, CANCELLED), strategy (enum: SINGLE_ORDER, BATCH, ZONE, CLUSTER), totalOrders, completedOrders, assignedPicker(User), startedAt, completedAt |
| `InventoryMovement` | حركة مخزون | warehouse, storageBin, productSku, movementType (enum: RECEIVE, PICK, PUT_AWAY, TRANSFER, ADJUST_IN, ADJUST_OUT, RETURN_IN, DAMAGE_OUT), quantity, referenceType, referenceId, performedBy(User), notes |

#### المستودعات (8)
- `WarehouseZoneRepository` — findByWarehouseId, findByWarehouseIdAndZoneType, findByWarehouseIdAndIsActiveTrue
- `StorageBinRepository` — findByWarehouseZoneId, findByBinCodeAndWarehouseZoneWarehouseId, findAvailableBins(@Query where isOccupied=false and isActive=true), findByAisleAndWarehouseZoneWarehouseId
- `ReceivingOrderRepository` — findByWarehouseId, findByMerchantId, findByStatus, findByReferenceNumber
- `ReceivingOrderItemRepository` — findByReceivingOrderId, findByStatus, countByReceivingOrderIdAndStatus
- `FulfillmentOrderRepository` — findByWarehouseId, findByShipmentId, findByStatus, findByAssignedPickerId, findPendingByWarehouseId(@Query status=PENDING ordered by priority)
- `FulfillmentOrderItemRepository` — findByFulfillmentOrderId, findUnpickedByFulfillmentOrderId(@Query isPicked=false)
- `PickWaveRepository` — findByWarehouseIdAndStatus, findByAssignedPickerId, findLatestByWarehouseId
- `InventoryMovementRepository` — findByWarehouseIdAndProductSku, findByMovementType, findByStorageBinId, findByPerformedByIdAndCreatedAtBetween

#### الخدمات (7)

| الخدمة | المسؤوليات |
|--------|-----------|
| `WarehouseZoneService` | CRUD للمناطق، حساب الإشغال، نقل بين المناطق، تقرير استخدام المناطق |
| `StorageBinService` | CRUD للرفوف، تخصيص رف لبضاعة، تحرير رف، البحث عن رف فارغ مناسب (حسب النوع والسعة)، خريطة المستودع |
| `ReceivingService` | إنشاء أمر استلام، تسجيل وصول البضاعة، فحص وتسجيل التالف/الناقص، تخصيص رفوف تلقائي (Put-Away)، إغلاق أمر الاستلام، تسجيل حركة مخزون RECEIVE |
| `FulfillmentService` | إنشاء أمر تنفيذ من شحنة، تعيين Picker/Packer، تتبع حالة Pick/Pack/Ship، تسجيل الانتقاء لكل بند، التحقق من اكتمال الانتقاء، تسجيل حركة مخزون PICK |
| `PickWaveService` | إنشاء موجة انتقاء (تجميع أوامر حسب الاستراتيجية)، تعيين منتقي، تتبع تقدم الموجة، إغلاق الموجة، تقرير كفاءة الانتقاء |
| `InventoryMovementService` | تسجيل جميع الحركات، تعديل الكميات (Adjust In/Out)، نقل بين رفوف (Transfer)، استعلام حركات منتج أو رف، تقرير حركات يومي |
| `WarehouseAnalyticsService` | إحصائيات المستودع: نسبة الإشغال، متوسط وقت التنفيذ، معدل دقة الانتقاء، أداء المنتقين، تقرير البضائع البطيئة/السريعة |

#### DTOs (3)
- `WarehouseZoneDTO` — CreateZoneRequest, BinRequest, ZoneResponse (with bins count), BinResponse
- `ReceivingDTO` — CreateReceivingOrderRequest (with items list), ReceiveItemRequest (receivedQty, damagedQty, binCode), ReceivingOrderResponse, ReceivingItemResponse
- `FulfillmentDTO` — FulfillmentOrderResponse (with items), PickItemRequest (fulfillmentOrderItemId, pickedQty), CreateWaveRequest (strategy, orderIds), WaveResponse, MovementResponse

#### المتحكمات (4)

| المتحكم | المسارات | الوصف |
|---------|---------|-------|
| `WarehouseZoneController` | `POST /api/warehouses/{warehouseId}/zones` — إنشاء منطقة | إدارة المناطق والرفوف |
| | `GET /api/warehouses/{warehouseId}/zones` — قائمة المناطق | |
| | `POST /api/warehouses/{warehouseId}/zones/{zoneId}/bins` — إضافة رف | |
| | `GET /api/warehouses/{warehouseId}/zones/{zoneId}/bins` — رفوف المنطقة | |
| | `GET /api/warehouses/{warehouseId}/bins/available` — الرفوف الفارغة | |
| `ReceivingController` | `POST /api/warehouses/{warehouseId}/receiving` — أمر استلام | عمليات الاستلام |
| | `GET /api/warehouses/{warehouseId}/receiving` — قائمة الأوامر | |
| | `POST /api/receiving/{id}/arrive` — تسجيل وصول | |
| | `POST /api/receiving/{id}/items/{itemId}/receive` — تسجيل بند | |
| | `POST /api/receiving/{id}/complete` — إغلاق الأمر | |
| `FulfillmentController` | `POST /api/warehouses/{warehouseId}/fulfillment` — أمر تنفيذ | Pick/Pack/Ship |
| | `GET /api/warehouses/{warehouseId}/fulfillment` — قائمة الأوامر | |
| | `POST /api/fulfillment/{id}/pick` — تسجيل انتقاء | |
| | `POST /api/fulfillment/{id}/pack` — تأكيد التغليف | |
| | `POST /api/fulfillment/{id}/ship` — تأكيد الشحن | |
| | `POST /api/warehouses/{warehouseId}/pick-waves` — إنشاء موجة | |
| | `GET /api/warehouses/{warehouseId}/pick-waves` — قائمة الموجات | |
| `WarehouseAnalyticsController` | `GET /api/warehouses/{warehouseId}/analytics/occupancy` — الإشغال | تحليلات المستودع |
| | `GET /api/warehouses/{warehouseId}/analytics/fulfillment` — أداء التنفيذ | |
| | `GET /api/warehouses/{warehouseId}/analytics/movements` — حركات المخزون | |
| | `GET /api/warehouses/{warehouseId}/analytics/picker-performance` — أداء المنتقين | |

#### Flyway Migrations (2)
- `V38__create_warehouse_zone_tables.sql` — warehouse_zones + storage_bins + inventory_movements + indexes + FK constraints
- `V39__create_fulfillment_tables.sql` — receiving_orders + receiving_order_items + fulfillment_orders + fulfillment_order_items + pick_waves + indexes

#### الاختبارات (~55 اختبار جديد)
- `WarehouseZoneServiceTest` — 6 اختبارات (create zone, findByWarehouse, calculate occupancy, deactivate, duplicate code error, zone types)
- `StorageBinServiceTest` — 7 اختبارات (create bin, find available, assign item, release bin, capacity exceeded error, find by aisle, warehouse map)
- `ReceivingServiceTest` — 8 اختبارات (create order, mark arrived, receive item full, receive partial, receive damaged, auto put-away, complete order, reject order)
- `FulfillmentServiceTest` — 9 اختبارات (create from shipment, assign picker, pick item, pick all complete, pack, ship, cancel, status transitions, express priority)
- `PickWaveServiceTest` — 6 اختبارات (create wave single-order, create wave batch, assign picker, complete wave, cancel wave, efficiency report)
- `InventoryMovementServiceTest` — 6 اختبارات (record receive, record pick, adjust in, adjust out, transfer between bins, daily report)
- `WarehouseAnalyticsServiceTest` — 4 اختبارات (occupancy stats, fulfillment time, picker performance, slow-moving items)
- `WarehouseZoneControllerTest` — 4 اختبارات (create zone, list zones, create bin, available bins)
- `FulfillmentControllerTest` — 3 اختبارات (create order, pick item, forbidden merchant)
- `ReceivingControllerTest` — 3 اختبارات (create receiving, receive item, complete)

---

## Sprint 38 — Customer Self-Service & Delivery Experience

> **الهدف:** بناء منظومة خدمة ذاتية شاملة للمستلم النهائي — جدولة التوصيل، إدارة العناوين، إعادة التوجيه، تعليمات التوصيل، تتبع محسّن، واستطلاعات رضا

### المتطلبات

#### الكيانات الجديدة (7)

| الكيان | الوصف | الحقول الرئيسية |
|--------|-------|----------------|
| `RecipientProfile` | ملف المستلم | phone(unique), name, email, preferredLanguage (AR/EN), defaultAddress(RecipientAddress), preferredTimeSlot, deliveryInstructions, totalDeliveries, createdAt |
| `RecipientAddress` | دفتر عناوين المستلم | recipientProfile, label(HOME/WORK/OTHER/custom), addressLine1, addressLine2, city, district, postalCode, latitude, longitude, isDefault, notes |
| `DeliveryTimeSlot` | فترة توصيل متاحة | zone, dayOfWeek (0-6), startTime, endTime, maxCapacity, currentBookings, isActive, surchargeAmount, displayNameAr |
| `DeliveryBooking` | حجز فترة توصيل | shipment, deliveryTimeSlot, recipientProfile, selectedDate, status (enum: BOOKED, CONFIRMED, RESCHEDULED, CANCELLED), rescheduledFrom(self-referencing), rescheduledReason, bookedAt |
| `DeliveryRedirect` | طلب إعادة توجيه | shipment, recipientProfile, redirectType (enum: CHANGE_ADDRESS, HOLD_AT_WAREHOUSE, REDIRECT_TO_NEIGHBOR, RESCHEDULE, RETURN_TO_SENDER), newAddress(RecipientAddress nullable), holdUntilDate, neighborName, neighborPhone, status (enum: REQUESTED, APPROVED, REJECTED, APPLIED), reason, requestedAt, processedAt, processedBy(User) |
| `DeliveryPreference` | تفضيلات توصيل عامة | recipientProfile, preferSafePlace(boolean), safePlaceDescription, allowNeighborDelivery(boolean), requireSignature(boolean), requireOtp(boolean), preferContactless(boolean), smsBeforeDelivery(boolean), smsMinutesBefore |
| `SatisfactionSurvey` | استطلاع رضا | shipment, recipientProfile, overallRating(1-5), deliverySpeedRating(1-5), courierBehaviorRating(1-5), packagingRating(1-5), comment(TEXT), wouldRecommend(boolean), submittedAt, feedbackTags(TEXT — comma-separated: FAST, POLITE, CAREFUL, LATE, RUDE, DAMAGED) |

#### المستودعات (7)
- `RecipientProfileRepository` — findByPhone, existsByPhone
- `RecipientAddressRepository` — findByRecipientProfileId, findByRecipientProfileIdAndIsDefaultTrue, countByRecipientProfileId
- `DeliveryTimeSlotRepository` — findByZoneIdAndDayOfWeek, findAvailableSlots(@Query currentBookings < maxCapacity and isActive), findByZoneIdAndIsActiveTrue
- `DeliveryBookingRepository` — findByShipmentId, findByRecipientProfileIdAndSelectedDateAfter, findByDeliveryTimeSlotIdAndSelectedDate, countByDeliveryTimeSlotIdAndSelectedDate
- `DeliveryRedirectRepository` — findByShipmentId, findByStatus, findByRecipientProfileId, countByShipmentId
- `DeliveryPreferenceRepository` — findByRecipientProfileId
- `SatisfactionSurveyRepository` — findByShipmentId, findByRecipientProfileId, averageRatingByDateRange(@Query AVG), findByOverallRatingLessThan, countBySubmittedAtBetween

#### الخدمات (7)

| الخدمة | المسؤوليات |
|--------|-----------|
| `RecipientProfileService` | إنشاء/تحديث ملف المستلم تلقائياً من بيانات الشحنة (getOrCreate by phone)، إدارة العناوين (CRUD + set default)، تفضيلات اللغة، إحصائيات المستلم |
| `DeliveryTimeSlotService` | CRUD للفترات الزمنية حسب المنطقة، التحقق من التوفر (availability check)، تطبيق رسوم إضافية للفترات المميزة، إدارة السعة القصوى |
| `DeliveryBookingService` | حجز فترة توصيل لشحنة، إعادة جدولة (مع تتبع السبب والمصدر)، إلغاء حجز، التحقق من عدم تجاوز السعة، إرسال إشعار تأكيد |
| `DeliveryRedirectService` | طلب إعادة توجيه (5 أنواع)، موافقة/رفض (OWNER/ADMIN)، تطبيق التغيير على الشحنة، التحقق من أهلية الإعادة (لا يمكن بعد التوصل)، إشعار المندوب |
| `DeliveryPreferenceService` | CRUD لتفضيلات التوصيل، تطبيق التفضيلات تلقائياً على الشحنات الجديدة، التحقق من OTP إذا مطلوب |
| `SatisfactionSurveyService` | إنشاء/إرسال استطلاع (تلقائياً بعد التوصيل)، تسجيل الإجابة، حساب المتوسطات والإحصائيات، تنبيه عند تقييم سلبي (< 3)، تقارير NPS (Net Promoter Score) |
| `RecipientExperienceService` | خدمة شاملة للتتبع المحسّن: timeline كاملة بالعربي، تقدير وقت الوصول، خريطة المندوب، حالة مفصلة (مثل "المندوب على بعد 2 كم")، رابط تتبع عام |

#### DTOs (3)
- `RecipientDTO` — RecipientProfileResponse (with addresses & preferences), AddressRequest (@NotBlank addressLine1, city), AddressResponse, PreferenceRequest
- `DeliverySlotDTO` — TimeSlotResponse (with availability count), BookingRequest (@NotNull slotId, date), BookingResponse, RescheduleRequest (newSlotId, newDate, reason)
- `DeliveryExperienceDTO` — RedirectRequest (@NotNull redirectType, optional newAddress/holdDate/neighborInfo), RedirectResponse, SurveyRequest (ratings 1-5, comment, tags), SurveyResponse, TrackingTimelineResponse (list of events with Arabic descriptions), ExperienceStatsResponse

#### المتحكمات (4)

| المتحكم | المسارات | الوصف |
|---------|---------|-------|
| `RecipientController` | `GET /api/recipients/me` — ملفي (بالهاتف من JWT) | الملف والعناوين |
| | `PUT /api/recipients/me` — تحديث بياناتي | |
| | `POST /api/recipients/me/addresses` — إضافة عنوان | |
| | `GET /api/recipients/me/addresses` — عناويني | |
| | `PUT /api/recipients/me/addresses/{id}/default` — تعيين افتراضي | |
| | `PUT /api/recipients/me/preferences` — تفضيلات التوصيل | |
| | `GET /api/recipients/me/deliveries` — شحناتي السابقة | |
| `DeliverySlotController` | `GET /api/zones/{zoneId}/delivery-slots` — الفترات المتاحة | فترات وحجوزات التوصيل |
| | `POST /api/delivery-slots` — إنشاء فترة (OWNER/ADMIN) | |
| | `POST /api/shipments/{shipmentId}/book-slot` — حجز فترة | |
| | `POST /api/shipments/{shipmentId}/reschedule` — إعادة جدولة | |
| | `DELETE /api/shipments/{shipmentId}/booking` — إلغاء حجز | |
| `DeliveryRedirectController` | `POST /api/shipments/{shipmentId}/redirect` — طلب إعادة توجيه | إعادة التوجيه |
| | `GET /api/delivery-redirects` — قائمة الطلبات (OWNER/ADMIN) | |
| | `POST /api/delivery-redirects/{id}/approve` — موافقة | |
| | `POST /api/delivery-redirects/{id}/reject` — رفض | |
| `SatisfactionController` | `POST /api/shipments/{shipmentId}/survey` — إرسال استطلاع | الاستطلاعات والتقييم |
| | `GET /api/satisfaction/stats` — إحصائيات عامة (OWNER/ADMIN) | |
| | `GET /api/satisfaction/nps` — Net Promoter Score | |
| | `GET /api/satisfaction/low-ratings` — تقييمات سلبية | |

#### Flyway Migrations (2)
- `V40__create_recipient_tables.sql` — recipient_profiles + recipient_addresses + delivery_preferences + delivery_time_slots + delivery_bookings + indexes
- `V41__create_delivery_experience_tables.sql` — delivery_redirects + satisfaction_surveys + indexes

#### الاختبارات (~50 اختبار جديد)
- `RecipientProfileServiceTest` — 7 اختبارات (getOrCreate new, getOrCreate existing, addAddress, setDefault, updateProfile, getStats, deleteAddress)
- `DeliveryTimeSlotServiceTest` — 5 اختبارات (createSlot, findAvailable, capacity full, surcharge, deactivate)
- `DeliveryBookingServiceTest` — 7 اختبارات (book success, capacity exceeded error, reschedule, cancel booking, duplicate booking error, confirm, notification sent)
- `DeliveryRedirectServiceTest` — 7 اختبارات (request change address, request hold, request neighbor, approve redirect, reject redirect, already delivered error, apply to shipment)
- `SatisfactionSurveyServiceTest` — 6 اختبارات (submit survey, calculate average, NPS score, low rating alert, duplicate survey error, stats by date range)
- `RecipientExperienceServiceTest` — 5 اختبارات (build timeline Arabic, estimate ETA, nearby courier message, generate tracking link, full experience)
- `RecipientControllerTest` — 4 اختبارات (get profile, add address, update preferences, get deliveries)
- `DeliverySlotControllerTest` — 4 اختبارات (get available slots, book slot, reschedule, forbidden)
- `SatisfactionControllerTest` — 3 اختبارات (submit survey, get stats, get NPS)
- `DeliveryRedirectControllerTest` — 3 اختبارات (request redirect, approve, forbidden courier)

---

## Sprint 39 — Rate Limiting, Caching & Search Infrastructure

> **الهدف:** بنية أداء enterprise-grade — محرك تحديد معدل الطلبات (Token Bucket)، طبقة تخزين مؤقت Redis شاملة، محرك بحث نص كامل، ونظام Feature Flags

### المتطلبات

#### الكيانات الجديدة (7)

| الكيان | الوصف | الحقول الرئيسية |
|--------|-------|----------------|
| `RateLimitPolicy` | سياسة تحديد المعدل | name, policyType (enum: API_KEY, USER, IP, TENANT, GLOBAL), maxRequests, windowSeconds, burstLimit, cooldownSeconds, isActive, appliesTo(TEXT — path patterns), description |
| `RateLimitOverride` | استثناء لسياسة | rateLimitPolicy, overrideType (enum: API_KEY, USER, IP, TENANT), overrideValue (the key/userId/IP/tenantId), customMaxRequests, customWindowSeconds, reason, expiresAt, createdBy(User) |
| `RateLimitViolation` | سجل انتهاك | rateLimitPolicy, violatorType, violatorValue, requestPath, requestMethod, requestCount, windowStart, blockedAt, unblockedAt |
| `CachePolicy` | سياسة تخزين مؤقت | name, cacheRegion (enum: SHIPMENTS, USERS, ZONES, PRICING, ANALYTICS, CONFIG, TENANTS), ttlSeconds, maxEntries, evictionStrategy (enum: LRU, LFU, TTL), isActive, description |
| `SearchIndex` | فهرس بحث | name, entityType (enum: SHIPMENT, USER, MERCHANT, ZONE, TICKET, ARTICLE), fields(TEXT — JSON array of indexed fields), language (AR/EN/BOTH), isActive, lastRebuiltAt, documentCount, rebuildCronExpression |
| `FeatureFlag` | علم ميزة | featureKey(unique), name, description, isEnabled, rolloutPercentage(0-100), targetRoles(TEXT — comma-separated), targetTenants(TEXT — comma-separated), startDate, endDate, createdBy(User), metadata(TEXT/JSON) |
| `FeatureFlagAudit` | سجل تغييرات الأعلام | featureFlag, action (enum: CREATED, ENABLED, DISABLED, UPDATED, ROLLED_OUT, ROLLED_BACK), previousValue(TEXT), newValue(TEXT), changedBy(User), reason |

#### المستودعات (7)
- `RateLimitPolicyRepository` — findByPolicyTypeAndIsActiveTrue, findByAppliesToContaining, findByIsActiveTrue
- `RateLimitOverrideRepository` — findByOverrideTypeAndOverrideValue, findByRateLimitPolicyId, findExpired(@Query expiresAt < now), deleteExpired
- `RateLimitViolationRepository` — findByViolatorTypeAndViolatorValue, countByBlockedAtAfter, findRecentByViolatorValue(@Query last 24h)
- `CachePolicyRepository` — findByCacheRegion, findByIsActiveTrue
- `SearchIndexRepository` — findByEntityType, findByIsActiveTrue, findByName
- `FeatureFlagRepository` — findByFeatureKey, findByIsEnabledTrue, findByTargetRolesContaining, findByTargetTenantsContaining
- `FeatureFlagAuditRepository` — findByFeatureFlagIdOrderByCreatedAtDesc, findByChangedById

#### الخدمات (8)

| الخدمة | المسؤوليات |
|--------|-----------|
| `RateLimitService` | التحقق من حد المعدل (Token Bucket algorithm باستخدام Redis INCR + EXPIRE)، فحص الطلب قبل التنفيذ، تسجيل الانتهاكات، دعم Override للاستثناءات، الحظر المؤقت عند الانتهاك المتكرر |
| `RateLimitPolicyService` | CRUD للسياسات، CRUD للاستثناءات، تنظيف الاستثناءات المنتهية، تقرير الانتهاكات، إحصائيات الاستخدام |
| `CacheService` | طبقة تخزين مؤقت موحدة — `get(region, key)`, `put(region, key, value, ttl)`, `evict(region, key)`, `evictAll(region)`, إحصائيات cache hit/miss ratio، تنظيف دوري |
| `CachePolicyService` | CRUD لسياسات التخزين المؤقت، تطبيق TTL و eviction strategy، warm-up cache عند البدء |
| `SearchService` | بحث نص كامل: بناء فهرس من الكيان، بحث بالكلمات المفتاحية (عربي + إنجليزي)، ترتيب النتائج (relevance scoring)، بحث مع فلاتر (status, date range, zone)، إعادة بناء الفهرس |
| `SearchIndexService` | CRUD للفهارس، بناء/إعادة بناء الفهرس من البيانات، جدولة إعادة البناء الدوري، مراقبة حجم الفهرس وعدد المستندات |
| `FeatureFlagService` | isEnabled(featureKey) — التحقق مع rollout percentage وtarget roles/tenants، CRUD للأعلام، تفعيل/تعطيل، rollout تدريجي، جلب الأعلام النشطة لمستخدم/دور/tenant |
| `FeatureFlagAuditService` | تسجيل كل تغيير، سجل التدقيق للعلم، rollback لآخر حالة |

#### Filters / Interceptors (2)
- `RateLimitFilter` — @Component @Order(0) OncePerRequestFilter — يستخرج identifier (API key → USER → IP → TENANT) → يستدعي RateLimitService.isAllowed() → إرجاع 429 Too Many Requests مع Retry-After header
- `FeatureFlagInterceptor` — HandlerInterceptor — يفحص @RequiresFeature annotation على المتحكمات → يتحقق من FeatureFlagService.isEnabled() → يرجع 404 إذا الميزة معطلة

#### Custom Annotation (1)
- `@RequiresFeature(value = "feature-key")` — annotation لوسم endpoints بشرط تفعيل feature flag

#### DTOs (3)
- `RateLimitDTO` — CreatePolicyRequest (@NotBlank name, @NotNull policyType, maxRequests, windowSeconds), OverrideRequest, PolicyResponse, ViolationResponse, RateLimitStatsResponse
- `SearchDTO` — SearchRequest (query, entityType, filters Map, page, size), SearchResponse<T> (results, totalHits, took, facets), SearchSuggestionResponse
- `FeatureFlagDTO` — CreateFlagRequest (@NotBlank featureKey, name), UpdateFlagRequest, FlagResponse (with audit trail count), FlagStatusResponse (for client evaluation)

#### المتحكمات (4)

| المتحكم | المسارات | الوصف |
|---------|---------|-------|
| `RateLimitController` | `POST /api/rate-limits/policies` — إنشاء سياسة | إدارة حدود المعدل |
| | `GET /api/rate-limits/policies` — قائمة السياسات | |
| | `PUT /api/rate-limits/policies/{id}` — تحديث | |
| | `POST /api/rate-limits/overrides` — إنشاء استثناء | |
| | `GET /api/rate-limits/violations` — سجل الانتهاكات | |
| | `GET /api/rate-limits/stats` — إحصائيات | |
| `CacheController` | `GET /api/cache/stats` — إحصائيات التخزين المؤقت | إدارة Cache |
| | `DELETE /api/cache/{region}` — تفريغ منطقة | |
| | `DELETE /api/cache` — تفريغ الكل | |
| | `GET /api/cache/policies` — سياسات التخزين | |
| | `PUT /api/cache/policies/{id}` — تحديث سياسة | |
| `SearchController` | `POST /api/search` — بحث شامل | البحث الموحد |
| | `GET /api/search/suggestions` — اقتراحات بحث | |
| | `GET /api/search/indexes` — قائمة الفهارس | |
| | `POST /api/search/indexes/{id}/rebuild` — إعادة بناء فهرس | |
| `FeatureFlagController` | `POST /api/feature-flags` — إنشاء علم | أعلام الميزات |
| | `GET /api/feature-flags` — قائمة الأعلام | |
| | `PUT /api/feature-flags/{id}` — تحديث | |
| | `POST /api/feature-flags/{id}/enable` — تفعيل | |
| | `POST /api/feature-flags/{id}/disable` — تعطيل | |
| | `GET /api/feature-flags/evaluate` — تقييم الأعلام للمستخدم الحالي | |
| | `GET /api/feature-flags/{id}/audit` — سجل التغييرات | |

#### Flyway Migrations (2)
- `V42__create_rate_limit_tables.sql` — rate_limit_policies + rate_limit_overrides + rate_limit_violations + cache_policies + indexes
- `V43__create_search_and_feature_flag_tables.sql` — search_indexes + feature_flags + feature_flag_audits + indexes

#### الاختبارات (~55 اختبار جديد)
- `RateLimitServiceTest` — 8 اختبارات (allow within limit, block exceeded, token bucket refill, burst limit, override allows more, cooldown period, IP rate limit, tenant rate limit)
- `RateLimitPolicyServiceTest` — 5 اختبارات (create policy, create override, cleanup expired, violation report, stats)
- `CacheServiceTest` — 7 اختبارات (get cache miss, get cache hit, put and get, evict key, evict region, TTL expiry, hit ratio stats)
- `SearchServiceTest` — 8 اختبارات (search shipment by tracking number, search Arabic text, search with filters, search with pagination, relevance scoring, rebuild index, empty results, suggestions)
- `FeatureFlagServiceTest` — 8 اختبارات (is enabled true, is disabled, rollout percentage, target role match, target tenant match, date range active, date range expired, rollback)
- `FeatureFlagAuditServiceTest` — 4 اختبارات (log creation, log enable, log disable, get audit trail)
- `RateLimitControllerTest` — 4 اختبارات (create policy, list policies, get violations, forbidden non-admin)
- `SearchControllerTest` — 4 اختبارات (search shipments, suggestions, rebuild index, forbidden)
- `FeatureFlagControllerTest` — 4 اختبارات (create flag, enable, evaluate for user, get audit)
- `CacheControllerTest` — 3 اختبارات (get stats, evict region, forbidden)

---

## Sprint 40 — Gamification, Loyalty & Campaign Engine

> **الهدف:** بناء نظام تحفيز شامل — مستويات وXP للمناديب، إنجازات ولوحة متصدرين، برنامج ولاء للتجار، نظام إحالات، أكواد خصم وعروض، وحملات تسويقية

### المتطلبات

#### الكيانات الجديدة (8)

| الكيان | الوصف | الحقول الرئيسية |
|--------|-------|----------------|
| `GamificationProfile` | ملف اللعب للمندوب | user(unique), currentLevel, totalXp, currentLevelXp, xpToNextLevel, currentStreak, longestStreak, totalDeliveries, perfectDeliveries, tier (enum: BRONZE, SILVER, GOLD, PLATINUM, DIAMOND), monthlyXp, weeklyXp |
| `Achievement` | تعريف إنجاز | code(unique), name, nameAr, description, descriptionAr, iconUrl, category (enum: DELIVERY, SPEED, RATING, STREAK, MILESTONE, SPECIAL), xpReward, criteria(TEXT/JSON — conditions), isActive, sortOrder, rarity (enum: COMMON, UNCOMMON, RARE, EPIC, LEGENDARY) |
| `UserAchievement` | إنجاز محقق | user, achievement, unlockedAt, progress(0-100), isCompleted, xpAwarded |
| `LeaderboardEntry` | مدخل في لوحة المتصدرين | user, period (enum: DAILY, WEEKLY, MONTHLY, ALL_TIME), periodKey (e.g. "2026-W10", "2026-03"), rank, score, deliveryCount, avgRating, xpEarned, calculatedAt |
| `LoyaltyProgram` | برنامج ولاء للتاجر | merchant(User), currentPoints, lifetimePoints, tier (enum: MEMBER, SILVER, GOLD, VIP), tierExpiresAt, pointsExpiringAt, pointsExpiring, lastActivityAt |
| `LoyaltyTransaction` | حركة نقاط ولاء | loyaltyProgram, transactionType (enum: EARN_SHIPMENT, EARN_REFERRAL, EARN_BONUS, REDEEM_DISCOUNT, REDEEM_FREE_SHIPMENT, EXPIRE, ADJUST), points, balanceAfter, referenceType, referenceId, description, expiresAt |
| `PromoCode` | كود خصم/عرض | code(unique), name, nameAr, discountType (enum: PERCENTAGE, FIXED_AMOUNT, FREE_SHIPMENT), discountValue, minOrderValue, maxDiscountAmount, maxUsageTotal, maxUsagePerUser, currentUsage, validFrom, validUntil, applicableZones(TEXT), applicablePlans(TEXT), isActive, createdBy(User) |
| `Campaign` | حملة تسويقية | name, nameAr, description, campaignType (enum: WELCOME, REACTIVATION, UPSELL, SEASONAL, REFERRAL, LOYALTY_BONUS), targetAudience (enum: ALL_MERCHANTS, NEW_MERCHANTS, INACTIVE_MERCHANTS, HIGH_VALUE, COURIERS, SPECIFIC_ZONE), targetCriteria(TEXT/JSON), promoCode(nullable), message(TEXT), messageAr(TEXT), channel (enum: SMS, EMAIL, PUSH, IN_APP, ALL), status (enum: DRAFT, SCHEDULED, ACTIVE, PAUSED, COMPLETED, CANCELLED), scheduledAt, startedAt, completedAt, totalTargets, totalSent, totalOpened, totalConverted |

#### المستودعات (8)
- `GamificationProfileRepository` — findByUserId, findTopByOrderByTotalXpDesc(@Param limit), findByTier, findByCurrentLevelGreaterThanEqual
- `AchievementRepository` — findByCategory, findByIsActiveTrue, findByCode, findByRarity
- `UserAchievementRepository` — findByUserId, findByUserIdAndIsCompletedTrue, findByAchievementId, existsByUserIdAndAchievementIdAndIsCompletedTrue
- `LeaderboardEntryRepository` — findByPeriodAndPeriodKeyOrderByRankAsc, findByUserIdAndPeriod, deleteByPeriodAndPeriodKey
- `LoyaltyProgramRepository` — findByMerchantId, findByTier, findByPointsExpiringAtBefore
- `LoyaltyTransactionRepository` — findByLoyaltyProgramIdOrderByCreatedAtDesc, sumPointsByLoyaltyProgramIdAndTransactionType, findByExpiresAtBeforeAndTransactionType
- `PromoCodeRepository` — findByCode, findByIsActiveTrueAndValidUntilAfter, findByCreatedById
- `CampaignRepository` — findByStatus, findByScheduledAtBeforeAndStatus(@Query SCHEDULED and past), findByCampaignType

#### الخدمات (8)

| الخدمة | المسؤوليات |
|--------|-----------|
| `GamificationService` | awardXp (حساب XP + ترقية المستوى/الدرجة تلقائياً)، updateStreak (يومي — تسجيل أو كسر)، calculateLevel (XP thresholds: 0-100 L1, 100-300 L2, 300-600 L3...)، getTier (من المستوى)، getProfile و getStats |
| `AchievementService` | CRUD للإنجازات، checkAndAward (فحص معايير الإنجاز بعد كل حدث)، updateProgress (تقدم تدريجي 0-100%)، getUnlocked و getLocked لمستخدم، findNextAchievements (الأقرب للفتح) |
| `LeaderboardService` | calculateLeaderboard (يومي/أسبوعي/شهري/عام — @Scheduled)، getLeaderboard (with rank for current user)، getUserRank، getTopPerformers، تنظيف الفترات القديمة |
| `LoyaltyService` | enrollMerchant (تسجيل في البرنامج)، earnPoints (من شحنة/إحالة/مكافأة)، redeemPoints (خصم أو شحنة مجانية)، calculateTier (MEMBER→SILVER→GOLD→VIP حسب النقاط)، expireOldPoints (@Scheduled)، getStatement |
| `PromoCodeService` | CRUD، validateCode (التحقق من الصلاحية + الحد الأقصى + المنطقة + الخطة)، applyDiscount (حساب الخصم على الشحنة)، incrementUsage، generateBulkCodes (إنشاء أكواد جماعية)، getUsageStats |
| `CampaignService` | CRUD حملات، جدولة إرسال، تشغيل الحملة (تحديد الجمهور → إرسال عبر القناة)، إيقاف/استئناف، تتبع معدلات الفتح والتحويل، تقرير أداء الحملة |
| `ReferralService` | إنشاء رابط/كود إحالة فريد لكل تاجر، تتبع الإحالات، منح نقاط ولاء عند الإحالة الناجحة، شروط الأهلية (التاجر المُحال يجب أن يكمل 5 شحنات)، تقرير الإحالات |
| `RewardService` | خدمة موحدة للمكافآت: منح XP للمندوب + نقاط للتاجر عند اكتمال الشحنة، منح مكافآت خاصة (أعياد/مناسبات)، تطبيق كود خصم، حساب المكافأة النهائية مع جميع العوامل |

#### DTOs (3)
- `GamificationDTO` — GamificationProfileResponse (level, xp, tier, streak, achievements count), AchievementResponse (with user progress), LeaderboardResponse (rank, user summary, score, period), AwardXpRequest
- `LoyaltyDTO` — LoyaltyProgramResponse (points, tier, expiring), LoyaltyTransactionResponse, RedeemRequest (@Min(1) points, redeemType), LoyaltyStatsResponse
- `CampaignDTO` — CreatePromoCodeRequest (@NotBlank code, @NotNull discountType, discountValue), PromoCodeResponse, ValidatePromoResponse (isValid, discountAmount, reason), CreateCampaignRequest (@NotBlank name, campaignType, targetAudience, channel, message), CampaignResponse (with stats), CampaignStatsResponse (sent, opened, converted rates)

#### المتحكمات (4)

| المتحكم | المسارات | الوصف |
|---------|---------|-------|
| `GamificationController` | `GET /api/gamification/profile` — ملفي | ملف اللاعب والإنجازات |
| | `GET /api/gamification/achievements` — جميع الإنجازات | |
| | `GET /api/gamification/achievements/mine` — إنجازاتي | |
| | `GET /api/gamification/leaderboard` — لوحة المتصدرين (param: period) | |
| | `GET /api/gamification/leaderboard/rank` — ترتيبي | |
| | `GET /api/gamification/stats` — إحصائيات عامة (OWNER/ADMIN) | |
| `LoyaltyController` | `GET /api/loyalty/program` — برنامجي (MERCHANT) | برنامج الولاء |
| | `GET /api/loyalty/transactions` — حركات النقاط | |
| | `POST /api/loyalty/redeem` — استبدال نقاط | |
| | `GET /api/loyalty/stats` — إحصائيات (OWNER/ADMIN) | |
| | `GET /api/loyalty/tiers` — شرح الدرجات | |
| `PromoCodeController` | `POST /api/promo-codes` — إنشاء كود (OWNER/ADMIN) | أكواد الخصم |
| | `GET /api/promo-codes` — قائمة الأكواد (OWNER/ADMIN) | |
| | `POST /api/promo-codes/validate` — التحقق من كود | |
| | `POST /api/promo-codes/apply/{shipmentId}` — تطبيق كود | |
| | `POST /api/promo-codes/bulk-generate` — إنشاء جماعي (OWNER) | |
| | `GET /api/promo-codes/{id}/stats` — إحصائيات الكود | |
| `CampaignController` | `POST /api/campaigns` — إنشاء حملة (OWNER/ADMIN) | الحملات التسويقية |
| | `GET /api/campaigns` — قائمة الحملات (OWNER/ADMIN) | |
| | `POST /api/campaigns/{id}/launch` — تشغيل | |
| | `POST /api/campaigns/{id}/pause` — إيقاف | |
| | `POST /api/campaigns/{id}/resume` — استئناف | |
| | `GET /api/campaigns/{id}/stats` — إحصائيات الأداء | |
| | `GET /api/campaigns/analytics` — تحليلات شاملة | |

#### Flyway Migrations (2)
- `V44__create_gamification_tables.sql` — gamification_profiles + achievements + user_achievements + leaderboard_entries + indexes
- `V45__create_loyalty_campaign_tables.sql` — loyalty_programs + loyalty_transactions + promo_codes + campaigns + indexes

#### الاختبارات (~55 اختبار جديد)
- `GamificationServiceTest` — 8 اختبارات (award xp, level up, tier upgrade bronze→silver, update streak, break streak, calculate level thresholds, get profile, award with achievement trigger)
- `AchievementServiceTest` — 7 اختبارات (check and award delivery milestone, check speed achievement, update progress, get unlocked, get locked, find next achievements, duplicate achievement ignored)
- `LeaderboardServiceTest` — 5 اختبارات (calculate daily, calculate weekly, get leaderboard, get user rank, cleanup old periods)
- `LoyaltyServiceTest` — 7 اختبارات (enroll merchant, earn points from shipment, redeem discount, redeem free shipment, insufficient points error, tier upgrade, expire old points)
- `PromoCodeServiceTest` — 7 اختبارات (create code, validate valid, validate expired, validate max usage, apply discount percentage, apply discount fixed, generate bulk codes)
- `CampaignServiceTest` — 6 اختبارات (create campaign, launch, pause resume, target audience filter, track conversion, campaign stats)
- `RewardServiceTest` — 4 اختبارات (complete shipment reward, holiday bonus, apply promo with loyalty, combined calculation)
- `GamificationControllerTest` — 4 اختبارات (get profile, get leaderboard, get achievements, forbidden non-courier for profile)
- `LoyaltyControllerTest` — 3 اختبارات (get program, redeem, get stats forbidden)
- `PromoCodeControllerTest` — 3 اختبارات (create code, validate, bulk generate)
- `CampaignControllerTest` — 3 اختبارات (create campaign, launch, get stats)

---

## ملخص التبعيات بين السبرنتات

```
Sprint 36 (Workflow Engine)
  ├── مستقل — لا يعتمد على 37-40
  ├── يستفيد من: Event System (Sprint 34), Notification System (Sprint 20)
  └── يوفر: automation foundation يمكن أن تستخدمه Sprint 38 (auto-survey) و Sprint 40 (auto-reward)

Sprint 37 (Warehouse & Fulfillment)
  ├── يعتمد على: Warehouse entity (Sprint 15), WarehouseInventory
  ├── يستفيد من: Shipment lifecycle, Zone system
  └── يوفر: fulfillment operations, inventory tracking

Sprint 38 (Customer Self-Service)
  ├── يعتمد على: Shipment, Zone, DeliveryProof, CourierLocation
  ├── يستفيد من: Notification System, Rating System
  └── يوفر: recipient experience, delivery scheduling

Sprint 39 (Infrastructure)
  ├── مستقل — بنية تحتية أفقية
  ├── يعزز: API Key system (Sprint 31), Tenant Quotas (Sprint 35)
  └── يوفر: rate limiting, caching, search for all modules

Sprint 40 (Gamification & Loyalty)
  ├── يعتمد على: CourierRating, Wallet, Notification
  ├── يستفيد من: Workflow Engine (Sprint 36) لأتمتة المكافآت
  └── يوفر: complete incentive ecosystem
```

```
الترتيب المُوصى به:
36 → 37 → 38 → 39 → 40
(متسلسل، لكن 36 و 37 و 39 مستقلة ويمكن تبادل ترتيبها)
```

---

## المقاييس المستهدفة بعد Sprint 40

| المقياس | القيمة الحالية (Sprint 35) | المستهدف (Sprint 40) |
|---------|---------------------------|---------------------|
| **اختبارات ناجحة** | 870 | ~1,140 |
| **الكيانات (Entities)** | 103 | ~140 |
| **الخدمات (Services)** | 103 | ~140 |
| **المتحكمات (Controllers)** | 68 | ~88 |
| **Flyway Migrations** | V1–V35 | V1–V45 |
| **نقاط API** | ~280 | ~370 |
| **DTOs** | ~55 | ~70 |
| **Filters/Interceptors** | ~8 | ~12 |

---

## مخطط التنفيذ

```
الأسبوع 1-2:  Sprint 36 — Workflow Engine & Automation
الأسبوع 3-4:  Sprint 37 — Warehouse & Fulfillment Center
الأسبوع 5-6:  Sprint 38 — Customer Self-Service Portal
الأسبوع 7-8:  Sprint 39 — Rate Limiting, Caching & Search
الأسبوع 9-10: Sprint 40 — Gamification, Loyalty & Campaigns
```

> **ملاحظة:** كل سبرنت يُختبر بالكامل قبل الانتقال للتالي. الهدف: 0 failures في كل سبرنت.
