# Twsela — Next 5 Sprints Plan

> **Priority:** Create missing frontend pages with full backend integration and DB seed data  
> **Sprint Duration:** 1 week each  
> **Current State:** 28 HTML pages, 26 JS handlers, 87 backend controllers, ~50 features with NO frontend

---

## Current Coverage Matrix

| Role | Existing Pages | Missing Pages |
|------|---------------|---------------|
| **Owner** | dashboard, analytics, employees, merchants, payouts, pricing, reports(3), settings, shipments, zones | wallet, invoices, settlements, contracts, notifications, audit-logs, system-health |
| **Admin** | dashboard | users-management, notifications-management, security, returns, support-tickets, system-settings |
| **Merchant** | dashboard, create-shipment, shipments, shipment-details | wallet, invoices, pickup-schedule, bulk-upload, returns, support, recipients, ratings |
| **Courier** | dashboard, manifest | wallet, route-map, live-tracking, delivery-proof, pickup-tasks, ratings, gamification |
| **Warehouse** | dashboard | zones-bins, receiving, fulfillment, pick-waves, inventory, dispatch |
| **Shared** | login, profile, settings, tracking, contact, 404, index | notifications-center, chat, support |

---

## Sprint 46 — Core Operations Pages (Wallet + Returns + Notifications)

> **Theme:** Every user role needs wallet and notifications — highest daily-use gap

### Frontend (8 new pages)

| # | Page | Role | HTML File | JS Handler | Backend Endpoint |
|---|------|------|-----------|------------|-----------------|
| 1 | **Wallet** | Shared | `frontend/wallet.html` | `wallet-page.js` | `GET /api/wallet/balance`, `GET /api/wallet/transactions`, `POST /api/wallet/withdraw` |
| 2 | **Admin Wallets** | Owner | `frontend/owner/wallets.html` | `owner-wallets-page.js` | `GET /api/wallet/admin/all` |
| 3 | **Notifications Center** | Shared | `frontend/notifications.html` | `notifications-page.js` | `GET /api/notifications/unread`, `PUT /api/notifications/{id}/read`, `PUT /api/notifications/read-all` |
| 4 | **Notification Preferences** | Shared | (inside settings.html tab) | update `settings.js` | `GET /api/notifications/preferences`, `PUT /api/notifications/preferences` |
| 5 | **Returns Management** | Owner/Admin | `frontend/owner/returns.html` | `owner-returns-page.js` | `GET /api/returns`, `PUT /api/returns/{id}/status`, `PUT /api/returns/{id}/assign` |
| 6 | **Returns (Merchant)** | Merchant | `frontend/merchant/returns.html` | `merchant-returns-page.js` | `POST /api/returns`, `GET /api/returns` |
| 7 | **Owner Merchants Page** | Owner | — (exists: `owner/merchants.html`) | `owner-merchants-page.js` **(MISSING!)** | `GET /api/merchants`, `POST /api/merchants`, `PUT /api/merchants/{id}` |
| 8 | **Index/Home Page** | Public | — (exists: `index.html`) | `index-page.js` **(MISSING!)** | `GET /api/public/contact/offices` |

### Backend
| # | Task |
|---|------|
| 1 | Add `api_service.js` methods: `getWalletBalance()`, `getWalletTransactions()`, `requestWithdrawal()`, `getAdminWallets()` |
| 2 | Add `api_service.js` methods: `getNotifications()`, `markNotificationRead()`, `markAllNotificationsRead()`, `getNotificationPreferences()`, `updateNotificationPreferences()` |
| 3 | Add `api_service.js` methods: `getReturns()`, `createReturn()`, `updateReturnStatus()`, `assignReturn()` |
| 4 | Fix `/api/zones` 500 error (route doesn't exist — add redirect or alias to `/api/master/zones`) |
| 5 | Fix `/v3/api-docs` SpringDoc 500 error |

### Database
| # | Task |
|---|------|
| 1 | V57 migration: Seed **return statuses** (`REQUESTED`, `APPROVED`, `PICKED_UP`, `RETURNED_TO_HUB`, `RETURNED_TO_MERCHANT`, `REJECTED`, `CANCELLED`) |
| 2 | V57 migration: Seed **notification templates** for key shipment events (CREATED, PICKED_UP, IN_TRANSIT, DELIVERED, FAILED) |
| 3 | V57 migration: Seed **default pricing rules** for zone-pairs (at least Cairo/Giza/Alexandria combinations) |

### Deliverables
- [ ] 6 new HTML pages + 8 JS handlers (including 2 fixes for existing gaps)
- [ ] 10+ new `api_service.js` methods
- [ ] V57 Flyway migration
- [ ] 2 backend bug fixes

---

## Sprint 47 — Merchant & Courier Essential Tools

> **Theme:** Give merchants and couriers the tools they use daily

### Frontend (8 new pages)

| # | Page | Role | HTML File | JS Handler | Backend Endpoint |
|---|------|------|-----------|------------|-----------------|
| 1 | **Bulk Upload** | Merchant | `frontend/merchant/bulk-upload.html` | `merchant-bulk-upload-page.js` | `POST /api/shipments/bulk`, `GET /api/shipments/bulk/template` |
| 2 | **Pickup Schedule** | Merchant | `frontend/merchant/pickups.html` | `merchant-pickups-page.js` | `POST /api/pickups`, `GET /api/pickups/my` |
| 3 | **Invoices (Merchant)** | Merchant | `frontend/merchant/invoices.html` | `merchant-invoices-page.js` | `GET /api/invoices`, `GET /api/invoices/{id}`, `POST /api/invoices/{id}/pay` |
| 4 | **Recipient Address Book** | Merchant | `frontend/merchant/recipients.html` | `merchant-recipients-page.js` | `GET /api/recipients/{id}`, `POST /api/recipients`, `GET /api/recipients/{profileId}/addresses` |
| 5 | **Courier Route Map** | Courier | `frontend/courier/route.html` | `courier-route-page.js` | `GET /api/routes/{courierId}`, `POST /api/routes/optimize/{courierId}` |
| 6 | **Delivery Proof** | Courier | `frontend/courier/delivery.html` | `courier-delivery-page.js` | `POST /api/delivery/{shipmentId}/proof`, `POST /api/delivery/{shipmentId}/attempt`, `GET /api/delivery/{shipmentId}/attempts` |
| 7 | **Courier Pickup Tasks** | Courier | `frontend/courier/pickups.html` | `courier-pickups-page.js` | `GET /api/pickups/today`, `PUT /api/pickups/{id}/start`, `PUT /api/pickups/{id}/complete` |
| 8 | **Label Print Page** | Shared | `frontend/merchant/labels.html` | `merchant-labels-page.js` | `GET /api/shipments/{id}/label`, `POST /api/shipments/labels/bulk`, `GET /api/shipments/{id}/barcode`, `GET /api/shipments/{id}/qrcode` |

### Backend
| # | Task |
|---|------|
| 1 | Add `api_service.js` methods: `bulkUploadShipments()`, `downloadBulkTemplate()` |
| 2 | Add `api_service.js` methods: `createPickup()`, `getMyPickups()`, `getTodayPickups()`, `startPickup()`, `completePickup()`, `cancelPickup()` |
| 3 | Add `api_service.js` methods: `getInvoices()`, `getInvoice()`, `payInvoice()` |
| 4 | Add `api_service.js` methods: `getRecipient()`, `createRecipient()`, `getRecipientAddresses()`, `createRecipientAddress()` |
| 5 | Add `api_service.js` methods: `getRoute()`, `optimizeRoute()` |
| 6 | Add `api_service.js` methods: `submitDeliveryProof()`, `addDeliveryAttempt()`, `getDeliveryAttempts()`, `getShipmentLabel()`, `bulkLabels()`, `getBarcode()`, `getQrCode()` |

### Database
| # | Task |
|---|------|
| 1 | V58 migration: Seed **invoice statuses** (`DRAFT`, `PENDING`, `PAID`, `OVERDUE`, `CANCELLED`, `REFUNDED`) |
| 2 | V58 migration: Seed **pickup statuses** (`SCHEDULED`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, `OVERDUE`) |
| 3 | V58 migration: Seed **sample recipient profiles** for testing |
| 4 | V58 migration: Seed **delivery attempt reasons** (`NOT_HOME`, `WRONG_ADDRESS`, `REFUSED`, `DAMAGED`, `RESCHEDULED`) |

### Deliverables
- [ ] 8 new HTML pages + 8 JS handlers
- [ ] 18+ new `api_service.js` methods
- [ ] V58 Flyway migration
- [ ] Google Maps API integration for route page

---

## Sprint 48 — Warehouse Operations + Support System

> **Theme:** Unlock the full warehouse workflow and add support channel

### Frontend (8 new pages)

| # | Page | Role | HTML File | JS Handler | Backend Endpoint |
|---|------|------|-----------|------------|-----------------|
| 1 | **WH Zones & Bins** | Warehouse | `frontend/warehouse/zones.html` | `warehouse-zones-page.js` | `GET /api/warehouse/{id}/zones`, `POST /api/warehouse/zones`, `GET /api/warehouse/zones/{zoneId}/bins` |
| 2 | **WH Receiving** | Warehouse | `frontend/warehouse/receiving.html` | `warehouse-receiving-page.js` | `GET /api/warehouse/{id}/receiving`, `POST /api/warehouse/receiving`, `PATCH /api/warehouse/receiving/{id}/status` |
| 3 | **WH Fulfillment** | Warehouse | `frontend/warehouse/fulfillment.html` | `warehouse-fulfillment-page.js` | `GET /api/warehouse/{id}/fulfillment`, `POST /api/warehouse/fulfillment`, `PATCH /api/warehouse/fulfillment/{id}/status` |
| 4 | **WH Pick Waves** | Warehouse | `frontend/warehouse/pick-waves.html` | `warehouse-pickwaves-page.js` | `GET /api/warehouse/{id}/pick-waves`, `POST /api/warehouse/pick-waves` |
| 5 | **WH Inventory** | Warehouse | `frontend/warehouse/inventory.html` | `warehouse-inventory-page.js` | `GET /api/warehouse/{id}/movements`, `POST /api/warehouse/movements`, `GET /api/shipments/warehouse/inventory` |
| 6 | **Support Tickets** | Shared | `frontend/support.html` | `support-page.js` | `POST /api/support/tickets`, `GET /api/support/tickets/my`, `GET /api/support/tickets/{id}`, `POST /api/support/tickets/{id}/messages` |
| 7 | **Admin Support** | Admin/Owner | `frontend/admin/support.html` | `admin-support-page.js` | `GET /api/support/tickets/admin`, `PUT /api/support/tickets/{id}/assign/{assigneeId}`, `PUT /api/support/tickets/{id}/resolve` |
| 8 | **Knowledge Base** | Public | `frontend/help.html` | `help-page.js` | `GET /api/support/articles`, `GET /api/support/articles/search` |

### Backend
| # | Task |
|---|------|
| 1 | Add `api_service.js` methods: `getWarehouseZones()`, `createWarehouseZone()`, `getZoneBins()`, `createBin()` |
| 2 | Add `api_service.js` methods: `getReceivingOrders()`, `createReceivingOrder()`, `updateReceivingStatus()` |
| 3 | Add `api_service.js` methods: `getFulfillmentOrders()`, `createFulfillmentOrder()`, `updateFulfillmentStatus()`, `assignPicker()` |
| 4 | Add `api_service.js` methods: `getPickWaves()`, `createPickWave()`, `updatePickWaveStatus()` |
| 5 | Add `api_service.js` methods: `getInventoryMovements()`, `createInventoryMovement()` |
| 6 | Add `api_service.js` methods: `createTicket()`, `getMyTickets()`, `getTicket()`, `sendTicketMessage()`, `getAdminTickets()`, `assignTicket()`, `resolveTicket()` |
| 7 | Add `api_service.js` methods: `getArticles()`, `searchArticles()` |

### Database
| # | Task |
|---|------|
| 1 | V59 migration: Seed **support ticket statuses** (`OPEN`, `IN_PROGRESS`, `WAITING_CUSTOMER`, `RESOLVED`, `CLOSED`) |
| 2 | V59 migration: Seed **support ticket categories** (`SHIPMENT_ISSUE`, `PAYMENT_ISSUE`, `ACCOUNT_ISSUE`, `TECHNICAL_ISSUE`, `OTHER`) |
| 3 | V59 migration: Seed **warehouse zone types** (`RECEIVING`, `STORAGE`, `PACKING`, `DISPATCH`, `RETURNS`) |
| 4 | V59 migration: Seed **default knowledge base articles** (How to create shipment, How to track, etc.) |

### Deliverables
- [ ] 8 new HTML pages + 8 JS handlers
- [ ] 20+ new `api_service.js` methods
- [ ] V59 Flyway migration
- [ ] Complete warehouse workflow (receive → store → pick → fulfill → dispatch)

---

## Sprint 49 — Admin Panel + Financial Management

> **Theme:** Full admin control panel and financial operations

### Frontend (8 new pages)

| # | Page | Role | HTML File | JS Handler | Backend Endpoint |
|---|------|------|-----------|------------|-----------------|
| 1 | **Admin Users** | Admin | `frontend/admin/users.html` | `admin-users-page.js` | `GET /api/master/users`, `POST /api/master/users`, `PUT /api/master/users/{id}`, `DELETE /api/master/users/{id}` |
| 2 | **Admin Invoices** | Owner/Admin | `frontend/owner/invoices.html` | `owner-invoices-page.js` | `GET /api/invoices/admin`, `POST /api/invoices/admin/{id}/refund` |
| 3 | **Admin Settlements** | Owner | `frontend/owner/settlements.html` | `owner-settlements-page.js` | `GET /api/settlements`, `POST /api/settlements/generate`, `POST /api/settlements/{id}/process` |
| 4 | **Admin Contracts** | Owner | `frontend/owner/contracts.html` | `owner-contracts-page.js` | `POST /api/admin/contracts`, `GET /api/admin/contracts`, `PUT /api/admin/contracts/{id}` |
| 5 | **Admin Notifications** | Admin | `frontend/admin/notifications.html` | `admin-notifications-page.js` | `GET /api/admin/notifications/templates`, `PUT /api/admin/notifications/templates/{id}`, `GET /api/admin/notifications/analytics` |
| 6 | **Admin Audit Logs** | Owner/Admin | `frontend/admin/audit.html` | `admin-audit-page.js` | `GET /api/audit/logs`, `GET /api/audit/entity/{type}/{id}`, `GET /api/audit/user/{userId}` |
| 7 | **Admin Security** | Owner/Admin | `frontend/admin/security.html` | `admin-security-page.js` | `GET /api/security/events`, `GET /api/security/events/summary`, `GET /api/security/lockouts`, `POST /api/security/lockouts/{userId}/unlock` |
| 8 | **Live Courier Tracking** | Owner/Admin | `frontend/owner/live-tracking.html` | `owner-live-tracking-page.js` | `GET /api/couriers`, `GET /api/couriers/{id}/location`, `GET /api/tracking/sessions/courier` |

### Backend
| # | Task |
|---|------|
| 1 | Add `api_service.js` methods: `masterGetUsers()`, `masterCreateUser()`, `masterUpdateUser()`, `masterDeleteUser()` |
| 2 | Add `api_service.js` methods: `getAdminInvoices()`, `refundInvoice()` |
| 3 | Add `api_service.js` methods: `getSettlements()`, `generateSettlement()`, `processSettlement()`, `getSettlementItems()` |
| 4 | Add `api_service.js` methods: `createContract()`, `getContracts()`, `updateContract()`, `terminateContract()` |
| 5 | Add `api_service.js` methods: `getNotificationTemplates()`, `updateNotificationTemplate()`, `testNotificationTemplate()`, `getNotificationAnalytics()` |
| 6 | Add `api_service.js` methods: `getAuditLogs()`, `getEntityAudit()`, `getUserAudit()` |
| 7 | Add `api_service.js` methods: `getSecurityEvents()`, `getSecuritySummary()`, `getLockouts()`, `unlockUser()` |
| 8 | Add `api_service.js` methods: `getCourierLocation()`, `getCourierLocationHistory()` |

### Database
| # | Task |
|---|------|
| 1 | V60 migration: Seed **settlement statuses** (`PENDING`, `PROCESSING`, `COMPLETED`, `FAILED`, `DISPUTED`) |
| 2 | V60 migration: Seed **contract statuses** (`DRAFT`, `PENDING_SIGNATURE`, `ACTIVE`, `EXPIRED`, `TERMINATED`) |
| 3 | V60 migration: Seed **default notification templates** (SMS/Push for all 21 shipment statuses) |
| 4 | V60 migration: Seed **audit event types** (`USER_LOGIN`, `USER_LOGOUT`, `SHIPMENT_CREATED`, `STATUS_CHANGED`, `PAYMENT_RECEIVED`, etc.) |

### Deliverables
- [ ] 8 new HTML pages + 8 JS handlers
- [ ] 22+ new `api_service.js` methods
- [ ] V60 Flyway migration
- [ ] Google Maps API integration for live tracking page

---

## Sprint 50 — Advanced Features & Chat

> **Theme:** Chat system, ratings, gamification, and integration pages

### Frontend (8 new pages)

| # | Page | Role | HTML File | JS Handler | Backend Endpoint |
|---|------|------|-----------|------------|-----------------|
| 1 | **Chat** | Shared | `frontend/chat.html` | `chat-page.js` | `POST /api/chat/rooms`, `GET /api/chat/rooms/my`, `POST /api/chat/messages`, `GET /api/chat/rooms/{id}/messages` |
| 2 | **Ratings & Reviews** | Shared | `frontend/ratings.html` | `ratings-page.js` | `POST /api/ratings`, `GET /api/ratings/courier/{id}`, `GET /api/ratings/shipment/{id}` |
| 3 | **Courier Gamification** | Courier | `frontend/courier/leaderboard.html` | `courier-leaderboard-page.js` | `GET /api/gamification/leaderboard`, `GET /api/gamification/profiles/{userId}`, `GET /api/gamification/achievements` |
| 4 | **Fleet Management** | Owner/Admin | `frontend/owner/fleet.html` | `owner-fleet-page.js` | `POST /api/fleet/vehicles`, `GET /api/fleet/vehicles`, `POST /api/fleet/assignments`, `POST /api/fleet/maintenance` |
| 5 | **E-Commerce Integrations** | Owner | `frontend/owner/integrations.html` | `owner-integrations-page.js` | `POST /api/integrations/connect`, `GET /api/integrations/connections`, `GET /api/integrations/{id}/orders` |
| 6 | **Webhooks & API Keys** | Owner | `frontend/owner/developers.html` | `owner-developers-page.js` | `POST /api/webhooks`, `GET /api/webhooks`, `POST /api/developer/keys`, `GET /api/developer/keys` |
| 7 | **Promo Codes & Campaigns** | Owner | `frontend/owner/marketing.html` | `owner-marketing-page.js` | `POST /api/promo-codes`, `GET /api/promo-codes`, `POST /api/campaigns`, `GET /api/campaigns` |
| 8 | **System Health (Admin)** | Owner/Admin | `frontend/admin/system.html` | `admin-system-page.js` | `GET /api/system/health-checks`, `GET /api/platform/metrics`, `GET /api/platform/alerts` |

### Backend
| # | Task |
|---|------|
| 1 | Add `api_service.js` methods: `createChatRoom()`, `getMyChatRooms()`, `sendMessage()`, `getRoomMessages()` |
| 2 | Add `api_service.js` methods: `submitRating()`, `getCourierRatings()`, `getShipmentRatings()` |
| 3 | Add `api_service.js` methods: `getLeaderboard()`, `getGamificationProfile()`, `getAchievements()` |
| 4 | Add `api_service.js` methods: `createVehicle()`, `getVehicles()`, `createAssignment()`, `createMaintenance()`, `logFuel()` |
| 5 | Add `api_service.js` methods: `connectEcommerce()`, `getEcommerceConnections()`, `getEcommerceOrders()` |
| 6 | Add `api_service.js` methods: `createWebhook()`, `getWebhooks()`, `testWebhook()`, `createApiKey()`, `getApiKeys()`, `rotateApiKey()` |
| 7 | Add `api_service.js` methods: `createPromoCode()`, `getPromoCodes()`, `createCampaign()`, `getCampaigns()`, `launchCampaign()` |
| 8 | Add `api_service.js` methods: `getSystemHealthChecks()`, `getPlatformMetrics()`, `getPlatformAlerts()`, `acknowledgeAlert()` |
| 9 | Implement WebSocket connection in frontend for real-time chat messages |

### Database
| # | Task |
|---|------|
| 1 | V61 migration: Seed **rating categories** (`SPEED`, `POLITENESS`, `PACKAGE_CONDITION`, `COMMUNICATION`, `OVERALL`) |
| 2 | V61 migration: Seed **vehicle types** (`MOTORCYCLE`, `CAR`, `VAN`, `TRUCK`) |
| 3 | V61 migration: Seed **achievement definitions** (First Delivery, Speed Demon, 100 Deliveries, etc.) |
| 4 | V61 migration: Seed **e-commerce platform types** (`SHOPIFY`, `WOOCOMMERCE`, `SALLA`, `ZID`, `CUSTOM`) |
| 5 | V61 migration: Seed **default feature flags** (`CHAT_ENABLED`, `GAMIFICATION_ENABLED`, `FLEET_ENABLED`, `ECOMMERCE_ENABLED`) |

### Deliverables
- [ ] 8 new HTML pages + 8 JS handlers
- [ ] 25+ new `api_service.js` methods
- [ ] V61 Flyway migration
- [ ] WebSocket chat integration

---

## Summary — 5 Sprints at a Glance

| Sprint | Theme | New Pages | New JS Handlers | New API Methods | DB Migration |
|--------|-------|-----------|-----------------|-----------------|-------------|
| **46** | Wallet + Returns + Notifications | 6 + 2 fixes | 8 | ~10 | V57 |
| **47** | Merchant & Courier Tools | 8 | 8 | ~18 | V58 |
| **48** | Warehouse Ops + Support | 8 | 8 | ~20 | V59 |
| **49** | Admin Panel + Financial | 8 | 8 | ~22 | V60 |
| **50** | Chat + Ratings + Advanced | 8 | 8 | ~25 | V61 |
| **TOTAL** | | **38 + 2** | **40** | **~95** | **5 migrations** |

### After Sprint 50

| Metric | Before | After |
|--------|--------|-------|
| HTML Pages | 28 | **68** |
| JS Page Handlers | 26 | **66** |
| `api_service.js` methods | ~80 | **~175** |
| Backend features with frontend | ~40% | **~85%** |
| Flyway migrations | V56 | **V61** |

### Remaining after Sprint 50 (Future Sprints)
- Tenant/multi-tenancy management pages
- Workflow builder UI
- Automation rules UI
- Data pipeline/export UI
- Custom report builder
- Compliance dashboard
- Country/currency/tax admin pages
- BI analytics deep-dive pages
- Mobile app (PWA conversion)
- Smart assignment configuration UI
- Document template management
- Delivery experience (surveys/redirects)
