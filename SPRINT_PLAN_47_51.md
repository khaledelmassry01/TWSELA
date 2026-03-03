# Twsela — Sprint Plan 47–51

> **Generated:** 2026-03-04  
> **Baseline:** Sprint 46 complete — 33 HTML pages, 31 JS handlers, 140 api_service methods, V57 migration  
> **Goal:** Close the frontend gap — bring coverage from 19.5% to ~75% of 87 backend controllers  
> **Priority:** Daily-use merchant/courier tools first, then admin/warehouse, then advanced features

---

## Current State (Post Sprint 46)

| Metric | Value |
|--------|-------|
| Backend Controllers | 87 |
| Controllers with dedicated frontend page | 17 (19.5%) |
| Controllers with partial/embedded coverage | 12 (13.8%) |
| Controllers with zero frontend | **52 (59.8%)** |
| HTML Pages | 33 |
| JS Page Handlers | 31 |
| `api_service.js` methods | ~140 |
| Latest Flyway migration | V57 |

---

## Sprint 47 — Merchant & Courier Daily Tools

> **Theme:** The features merchants and couriers use every single day  
> **Migration:** V58 · **New Pages:** 8 · **New API Methods:** ~22

### Frontend Pages

| # | Page | Role | HTML | JS Handler | Key Endpoints |
|---|------|------|------|------------|---------------|
| 1 | Bulk Shipment Upload | Merchant | `merchant/bulk-upload.html` | `merchant-bulk-upload-page.js` | `POST /api/shipments/bulk`, `GET /api/shipments/bulk/template` |
| 2 | Pickup Schedule | Merchant | `merchant/pickups.html` | `merchant-pickups-page.js` | `POST /api/pickups`, `GET /api/pickups/my`, `PUT /api/pickups/{id}/cancel` |
| 3 | Merchant Invoices | Merchant | `merchant/invoices.html` | `merchant-invoices-page.js` | `GET /api/invoices`, `GET /api/invoices/{id}`, `POST /api/invoices/{id}/pay` |
| 4 | Recipient Address Book | Merchant | `merchant/recipients.html` | `merchant-recipients-page.js` | `GET /api/recipients/{id}`, `POST /api/recipients`, `GET /api/recipients/{profileId}/addresses` |
| 5 | Courier Route Map | Courier | `courier/route.html` | `courier-route-page.js` | `GET /api/routes/{courierId}`, `POST /api/routes/optimize/{courierId}` |
| 6 | Delivery Proof & Attempts | Courier | `courier/delivery.html` | `courier-delivery-page.js` | `POST /api/delivery/{id}/proof`, `POST /api/delivery/{id}/attempt`, `GET /api/delivery/{id}/attempts` |
| 7 | Courier Pickup Tasks | Courier | `courier/pickups.html` | `courier-pickups-page.js` | `GET /api/pickups/today`, `PUT /api/pickups/{id}/start`, `PUT /api/pickups/{id}/complete` |
| 8 | Label/AWB Print | Merchant | `merchant/labels.html` | `merchant-labels-page.js` | `GET /api/shipments/{id}/label`, `POST /api/shipments/labels/bulk`, `GET /api/shipments/{id}/barcode` |

### API Methods to Add (~22)
```
bulkUploadShipments(file), downloadBulkTemplate(),
createPickup(data), getMyPickups(), getTodayPickups(), startPickup(id), completePickup(id), cancelPickup(id),
getInvoices(params), getInvoice(id), payInvoice(id), downloadInvoicePdf(id),
getRecipient(id), createRecipient(data), getRecipientAddresses(profileId), createRecipientAddress(profileId, data),
getCourierRoute(courierId), optimizeRoute(courierId),
submitDeliveryProof(shipmentId, data), addDeliveryAttempt(shipmentId, data), getDeliveryAttempts(shipmentId),
getShipmentLabel(id)
```

### Database — V58
- Seed **invoice statuses**: `DRAFT`, `PENDING`, `PAID`, `OVERDUE`, `CANCELLED`, `REFUNDED`
- Seed **pickup statuses**: `SCHEDULED`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
- Seed **delivery attempt reasons**: `NOT_HOME`, `WRONG_ADDRESS`, `REFUSED`, `DAMAGED`, `RESCHEDULED`
- Seed **label templates** config data

### Sidebar Updates
- Merchant sidebar: add Bulk Upload, Pickups, Invoices, Recipients, Labels links
- Courier sidebar: add Route, Delivery, Pickups links

---

## Sprint 48 — Warehouse Operations + Support System

> **Theme:** Full warehouse workflow + customer support channel  
> **Migration:** V59 · **New Pages:** 8 · **New API Methods:** ~24

### Frontend Pages

| # | Page | Role | HTML | JS Handler | Key Endpoints |
|---|------|------|------|------------|---------------|
| 1 | WH Zones & Bins | Warehouse | `warehouse/zones.html` | `warehouse-zones-page.js` | `GET /api/warehouse/{id}/zones`, `POST /api/warehouse/zones`, `GET /api/warehouse/zones/{zoneId}/bins` |
| 2 | WH Receiving | Warehouse | `warehouse/receiving.html` | `warehouse-receiving-page.js` | `GET /api/warehouse/{id}/receiving`, `POST /api/warehouse/receiving`, `PATCH /api/warehouse/receiving/{id}/status` |
| 3 | WH Fulfillment | Warehouse | `warehouse/fulfillment.html` | `warehouse-fulfillment-page.js` | `GET /api/warehouse/{id}/fulfillment`, `POST /api/warehouse/fulfillment`, `PATCH /api/warehouse/fulfillment/{id}/status` |
| 4 | WH Pick Waves | Warehouse | `warehouse/pick-waves.html` | `warehouse-pickwaves-page.js` | `GET /api/warehouse/{id}/pick-waves`, `POST /api/warehouse/pick-waves` |
| 5 | WH Inventory | Warehouse | `warehouse/inventory.html` | `warehouse-inventory-page.js` | `GET /api/warehouse/{id}/movements`, `POST /api/warehouse/movements` |
| 6 | Support Tickets (User) | Shared | `support.html` | `support-page.js` | `POST /api/support/tickets`, `GET /api/support/tickets/my`, `POST /api/support/tickets/{id}/messages` |
| 7 | Support Admin | Admin | `admin/support.html` | `admin-support-page.js` | `GET /api/support/tickets/admin`, `PUT /api/support/tickets/{id}/assign/{assigneeId}`, `PUT /api/support/tickets/{id}/resolve` |
| 8 | Knowledge Base / Help | Public | `help.html` | `help-page.js` | `GET /api/support/articles`, `GET /api/support/articles/search` |

### API Methods to Add (~24)
```
getWarehouseZones(warehouseId), createWarehouseZone(data), getZoneBins(zoneId), createBin(data),
getReceivingOrders(warehouseId), createReceivingOrder(data), updateReceivingStatus(id, status),
getFulfillmentOrders(warehouseId), createFulfillmentOrder(data), updateFulfillmentStatus(id, status), assignPicker(id, pickerId),
getPickWaves(warehouseId), createPickWave(data), startPickWave(id), completePickWave(id),
getInventoryMovements(warehouseId), createInventoryMovement(data),
createSupportTicket(data), getMyTickets(), getTicket(id), sendTicketMessage(id, data),
getAdminTickets(), assignTicket(id, assigneeId), resolveTicket(id),
getHelpArticles(), searchHelpArticles(query)
```

### Database — V59
- Seed **support ticket statuses**: `OPEN`, `IN_PROGRESS`, `WAITING_CUSTOMER`, `RESOLVED`, `CLOSED`
- Seed **support categories**: `SHIPMENT_ISSUE`, `PAYMENT_ISSUE`, `ACCOUNT_ISSUE`, `TECHNICAL_ISSUE`, `OTHER`
- Seed **warehouse zone types**: `RECEIVING`, `STORAGE`, `PACKING`, `DISPATCH`, `RETURNS`
- Seed **sample help articles** (5-10 basic how-to articles in Arabic)

### Sidebar Updates
- Warehouse sidebar: add Zones, Receiving, Fulfillment, Pick Waves, Inventory links
- All role sidebars: add Support/Help link

---

## Sprint 49 — Admin Panel + Financial Management

> **Theme:** Admin control center with full audit, security, and financial tools  
> **Migration:** V60 · **New Pages:** 8 · **New API Methods:** ~26

### Frontend Pages

| # | Page | Role | HTML | JS Handler | Key Endpoints |
|---|------|------|------|------------|---------------|
| 1 | Admin Users Management | Admin | `admin/users.html` | `admin-users-page.js` | `GET /api/master/users`, `POST /api/master/users`, `PUT /api/master/users/{id}`, `DELETE /api/master/users/{id}` |
| 2 | Admin Invoices | Owner | `owner/invoices.html` | `owner-invoices-page.js` | `GET /api/invoices/admin`, `POST /api/invoices/admin/{id}/refund` |
| 3 | Settlements | Owner | `owner/settlements.html` | `owner-settlements-page.js` | `GET /api/settlements`, `POST /api/settlements/generate`, `POST /api/settlements/{id}/process` |
| 4 | Contracts | Owner | `owner/contracts.html` | `owner-contracts-page.js` | `POST /api/admin/contracts`, `GET /api/admin/contracts`, `PUT /api/admin/contracts/{id}` |
| 5 | Audit Logs | Admin | `admin/audit.html` | `admin-audit-page.js` | `GET /api/audit/logs`, `GET /api/audit/entity/{type}/{id}`, `GET /api/audit/user/{userId}` |
| 6 | Security Dashboard | Admin | `admin/security.html` | `admin-security-page.js` | `GET /api/security/events`, `GET /api/security/events/summary`, `GET /api/security/lockouts`, `POST /api/security/lockouts/{userId}/unlock` |
| 7 | Notification Templates | Admin | `admin/notifications.html` | `admin-notifications-page.js` | `GET /api/admin/notifications/templates`, `PUT /api/admin/notifications/templates/{id}`, `GET /api/admin/notifications/analytics` |
| 8 | Live Courier Tracking | Owner | `owner/live-tracking.html` | `owner-live-tracking-page.js` | `GET /api/couriers`, `GET /api/couriers/{id}/location`, `GET /api/tracking/sessions/courier` |

### API Methods to Add (~26)
```
masterGetUsers(params), masterCreateUser(data), masterUpdateUser(id, data), masterDeleteUser(id),
getAdminInvoices(params), refundInvoice(id), getInvoiceStats(),
getSettlements(params), generateSettlement(data), processSettlement(id), getSettlementItems(id),
createContract(data), getContracts(params), updateContract(id, data), terminateContract(id), getContractSlas(id),
getSecurityEvents(params), getSecuritySummary(), getLockouts(), unlockUser(id),
getNotificationTemplates(), updateNotificationTemplate(id, data), testNotificationTemplate(id), getNotificationAnalytics(),
getCourierLocationHistory(courierId), getActiveTrackingSessions()
```

### Database — V60
- Seed **settlement statuses**: `PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, `DISPUTED`
- Seed **contract statuses**: `DRAFT`, `PENDING_SIGNATURE`, `ACTIVE`, `EXPIRED`, `TERMINATED`
- Seed **audit event types**: `USER_LOGIN`, `USER_LOGOUT`, `SHIPMENT_CREATED`, `STATUS_CHANGED`, `PAYMENT_RECEIVED`, `SETTINGS_CHANGED`
- Seed **notification template defaults** for all 21 shipment statuses (SMS + Push)

### Sidebar Updates
- Admin sidebar: add Users, Audit, Security, Notification Templates, Support links
- Owner sidebar: add Invoices, Settlements, Contracts, Live Tracking links

---

## Sprint 50 — Chat, Ratings & Gamification

> **Theme:** Social features — real-time chat, rating system, courier gamification  
> **Migration:** V61 · **New Pages:** 8 · **New API Methods:** ~24

### Frontend Pages

| # | Page | Role | HTML | JS Handler | Key Endpoints |
|---|------|------|------|------------|---------------|
| 1 | Chat | Shared | `chat.html` | `chat-page.js` | `POST /api/chat/rooms`, `GET /api/chat/rooms/my`, `POST /api/chat/messages`, `GET /api/chat/rooms/{id}/messages` |
| 2 | Ratings & Reviews | Shared | `ratings.html` | `ratings-page.js` | `POST /api/ratings`, `GET /api/ratings/courier/{id}`, `GET /api/ratings/shipment/{id}` |
| 3 | Courier Leaderboard | Courier | `courier/leaderboard.html` | `courier-leaderboard-page.js` | `GET /api/gamification/leaderboard`, `GET /api/gamification/profiles/{userId}`, `GET /api/gamification/achievements` |
| 4 | Fleet Management | Owner | `owner/fleet.html` | `owner-fleet-page.js` | `POST /api/fleet/vehicles`, `GET /api/fleet/vehicles`, `POST /api/fleet/assignments`, `POST /api/fleet/maintenance` |
| 5 | E-Commerce Integrations | Owner | `owner/integrations.html` | `owner-integrations-page.js` | `POST /api/integrations/connect`, `GET /api/integrations/connections`, `GET /api/integrations/{id}/orders` |
| 6 | Webhooks & API Keys | Owner | `owner/developers.html` | `owner-developers-page.js` | `POST /api/webhooks`, `GET /api/webhooks`, `POST /api/developer/keys`, `GET /api/developer/keys` |
| 7 | Promo Codes & Campaigns | Owner | `owner/marketing.html` | `owner-marketing-page.js` | `POST /api/promo-codes`, `GET /api/promo-codes`, `POST /api/campaigns`, `GET /api/campaigns` |
| 8 | Owner Merchants Mgmt | Owner | `owner/merchants-manage.html` | `owner-merchants-page.js` | `GET /api/merchants`, `POST /api/merchants`, `PUT /api/merchants/{id}`, `DELETE /api/merchants/{id}` |

### API Methods to Add (~24)
```
createChatRoom(data), getMyChatRooms(), sendChatMessage(roomId, data), getRoomMessages(roomId, params),
submitRating(data), getCourierRatings(courierId), getShipmentRating(shipmentId), getMyRatings(),
getLeaderboard(params), getGamificationProfile(userId), getAchievements(), claimReward(rewardId),
createVehicle(data), getVehicles(params), updateVehicle(id, data), createFleetAssignment(data), logMaintenance(data),
connectEcommerce(data), getEcommerceConnections(), getEcommerceOrders(connectionId, params), disconnectEcommerce(id),
createWebhook(data), getWebhooks(), deleteWebhook(id), testWebhook(id),
createPromoCode(data), getPromoCodes(params)
```

### Database — V61
- Seed **rating categories**: `SPEED`, `POLITENESS`, `PACKAGE_CONDITION`, `COMMUNICATION`, `OVERALL`
- Seed **vehicle types**: `MOTORCYCLE`, `CAR`, `VAN`, `TRUCK`
- Seed **achievement definitions**: First Delivery, Speed Demon, 100 Deliveries, Perfect Week, etc.
- Seed **e-commerce platform types**: `SHOPIFY`, `WOOCOMMERCE`, `SALLA`, `ZID`, `CUSTOM`
- Seed **promo code types**: `PERCENTAGE`, `FIXED_AMOUNT`, `FREE_SHIPPING`

### Technical
- WebSocket integration for real-time chat
- Google Maps API for courier leaderboard map widget

---

## Sprint 51 — System Admin + Advanced Tools

> **Theme:** Platform operations, automation, subscriptions, and remaining admin tools  
> **Migration:** V62 · **New Pages:** 8 · **New API Methods:** ~28

### Frontend Pages

| # | Page | Role | HTML | JS Handler | Key Endpoints |
|---|------|------|------|------------|---------------|
| 1 | System Health Monitor | Admin | `admin/system.html` | `admin-system-page.js` | `GET /api/system/health-checks`, `GET /api/platform/metrics`, `GET /api/platform/alerts` |
| 2 | Backup Management | Admin | `admin/backups.html` | `admin-backups-page.js` | `GET /api/backup`, `POST /api/backup/create`, `GET /api/backup/{id}`, `DELETE /api/backup/{id}` |
| 3 | Smart Assignment Config | Admin | `admin/assignment.html` | `admin-assignment-page.js` | `GET /api/assignment/config`, `PUT /api/assignment/config`, `POST /api/assignment/run`, `GET /api/assignment/stats` |
| 4 | Automation Rules | Admin | `admin/automation.html` | `admin-automation-page.js` | `GET /api/automation-rules`, `POST /api/automation-rules`, `PUT /api/automation-rules/{id}`, `PUT /api/automation-rules/{id}/toggle` |
| 5 | Subscription Plans | Owner | `owner/subscriptions.html` | `owner-subscriptions-page.js` | `GET /api/subscriptions`, `POST /api/subscriptions`, `PUT /api/subscriptions/{id}`, `GET /api/subscriptions/stats` |
| 6 | Custom Report Builder | Owner | `owner/report-builder.html` | `owner-report-builder-page.js` | `GET /api/reports`, `POST /api/reports`, `PUT /api/reports/{id}`, `POST /api/reports/{id}/run` |
| 7 | BI Analytics Dashboard | Owner | `owner/bi-analytics.html` | `owner-bi-analytics-page.js` | `GET /api/bi-analytics/metrics`, `GET /api/bi-analytics/funnel`, `GET /api/bi-analytics/cohort` |
| 8 | Scheduled Tasks | Admin | `admin/tasks.html` | `admin-tasks-page.js` | `GET /api/scheduled-tasks`, `POST /api/scheduled-tasks`, `PUT /api/scheduled-tasks/{id}/toggle`, `GET /api/jobs` |

### API Methods to Add (~28)
```
getSystemHealth(), getPlatformMetrics(), getPlatformAlerts(), acknowledgeAlert(id),
getBackups(), createBackup(), getBackup(id), deleteBackup(id), restoreBackup(id),
getAssignmentConfig(), updateAssignmentConfig(data), runAssignment(data), getAssignmentStats(),
getAutomationRules(params), createAutomationRule(data), updateAutomationRule(id, data), toggleAutomationRule(id), deleteAutomationRule(id),
getSubscriptions(params), createSubscription(data), updateSubscription(id, data), getSubscriptionStats(),
getCustomReports(), createCustomReport(data), updateCustomReport(id, data), runCustomReport(id),
getBIMetrics(params), getBIFunnel(params), getBICohort(params),
getScheduledTasks(), createScheduledTask(data), toggleScheduledTask(id), getAsyncJobs(params)
```

### Database — V62
- Seed **automation trigger types**: `SHIPMENT_CREATED`, `STATUS_CHANGED`, `PAYMENT_RECEIVED`, `SLA_BREACH`, `SCHEDULE`
- Seed **automation action types**: `SEND_NOTIFICATION`, `ASSIGN_COURIER`, `UPDATE_STATUS`, `CREATE_INVOICE`, `WEBHOOK`
- Seed **subscription plan defaults**: Free, Starter, Business, Enterprise
- Seed **scheduled task types**: `SETTLEMENT_GENERATION`, `REPORT_EXPORT`, `DATA_CLEANUP`, `BACKUP`, `NOTIFICATION_DIGEST`
- Seed **BI metric definitions**: conversion_rate, avg_delivery_time, customer_retention, revenue_per_courier

---

## Summary — Sprints 47-51 at a Glance

| Sprint | Theme | Pages | JS Handlers | API Methods | Migration |
|--------|-------|-------|-------------|-------------|-----------|
| **47** | Merchant & Courier Tools | 8 | 8 | ~22 | V58 |
| **48** | Warehouse Ops + Support | 8 | 8 | ~24 | V59 |
| **49** | Admin Panel + Financial | 8 | 8 | ~26 | V60 |
| **50** | Chat, Ratings, Gamification | 8 | 8 | ~24 | V61 |
| **51** | System Admin + Advanced | 8 | 8 | ~28 | V62 |
| **TOTAL** | | **40** | **40** | **~124** | **5** |

---

## Projected State After Sprint 51

| Metric | Before (Post S46) | After S51 | Change |
|--------|-------------------|-----------|--------|
| HTML Pages | 33 | **73** | +40 |
| JS Page Handlers | 31 | **71** | +40 |
| `api_service.js` methods | ~140 | **~264** | +124 |
| Backend coverage (dedicated) | 19.5% | **~72%** | +52.5% |
| Flyway migrations | V57 | **V62** | +5 |

---

## Coverage After Sprint 51 — What's Left

### Remaining Uncovered Controllers (~11)
| Controller | Reason for Deferral |
|-----------|---------------------|
| `TenantController` / `TenantUserController` / `TenantQuotaController` / `TenantBrandingController` | Multi-tenancy — large scope, need separate sprint |
| `DeliveryExperienceController` | Customer-facing redirect/survey — low priority |
| `SignatureController` | Signature capture needs canvas integration — mobile-first |
| `LoyaltyController` | Loyalty programs — needs business rules definition first |
| `DeviceMobileController` / `OfflineSyncController` | Mobile-only — PWA conversion sprint |
| `DocumentController` | Document template editor — needs WYSIWYG integration |
| `DemandController` | Demand forecasting — needs ML pipeline first |

### Recommended Post-S51 Sprints
- **Sprint 52:** Multi-Tenancy UI (tenant CRUD, branding, quotas, user management)
- **Sprint 53:** Document Templates + Delivery Experience + Signature Capture
- **Sprint 54:** Mobile PWA Conversion + Offline Sync
- **Sprint 55:** Loyalty Program + Demand Forecasting + Remaining polish

---

## Sprint Execution Checklist (Per Sprint)

For each sprint, the implementation order is:

1. **Database** — Create Flyway migration with seed data
2. **API Layer** — Add new methods to `api_service.js`
3. **HTML Pages** — Create page files following existing patterns (RTL Arabic, Bootstrap 5.3.2, role-specific sidebar)
4. **JS Handlers** — Create page handler extending `BasePageHandler`
5. **Routing** — Register new pages in `app.js` (`getCurrentPage()` + `isPageHandlingAuth()`)
6. **Navigation** — Update sidebar links on existing pages for the affected roles
7. **Build & Test** — `mvn compile` backend, verify all files exist
