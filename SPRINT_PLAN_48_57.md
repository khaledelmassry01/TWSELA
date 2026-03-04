# Twsela — Sprint Plan 48–57

> **Generated:** 2026-03-04  
> **Baseline:** Sprint 47 complete — 41 HTML pages, 39 JS handlers, ~162 api_service methods, V58 migration  
> **Goal:** Achieve 100% backend controller coverage, full platform functionality  
> **Approach:** Sprints 48–51 follow the original plan; Sprints 52–57 cover remaining controllers + new capabilities

---

## Current State (Post Sprint 47)

| Metric | Value |
|--------|-------|
| Backend Controllers | 87 |
| HTML Pages | 41 |
| JS Page Handlers | 39 |
| `api_service.js` methods | ~162 |
| Latest Flyway migration | V58 |
| Controllers with frontend coverage | ~35 (40%) |
| Controllers with zero frontend | ~52 (60%) |

---

## Controller Coverage Map

### Already Covered (Post Sprint 47 — ~35 controllers)

| Controller | Frontend Page |
|-----------|---------------|
| AuthController | login.html |
| DashboardController | */dashboard.html (×5 roles) |
| ShipmentController | merchant/shipments, create-shipment, shipment-details, owner/shipments |
| DeliveryController | courier/delivery.html |
| ManifestController | courier/manifest.html |
| PickupScheduleController | merchant/pickups, courier/pickups |
| RouteController | courier/route.html |
| LabelController | merchant/labels.html |
| BulkUploadController | merchant/bulk-upload.html |
| InvoiceController | merchant/invoices.html |
| RecipientController | merchant/recipients.html |
| ReturnController | merchant/returns, owner/returns |
| WalletController | wallet.html, owner/wallets |
| NotificationController | notifications.html |
| NotificationPreferenceController | (embedded in notifications) |
| UserController | owner/employees |
| SettingsController | settings.html, owner/settings |
| AnalyticsController | owner/analytics |
| ReportsController | owner/reports, reports/* |
| HealthController | (system endpoint) |
| PublicController | index.html, contact.html |
| PublicTrackingController | tracking.html |
| MasterDataController | (embedded in owner/zones, pricing) |
| CourierLocationController | (embedded in courier dashboard) |

### Planned Coverage (Sprints 48–57)

| Sprint | Controllers Covered |
|--------|-------------------|
| 48 | WarehouseZoneController, FulfillmentController, SupportController |
| 49 | AuditController, SecurityEventController, SettlementController, ContractController, ContractSlaController, ContractPricingController, NotificationTemplateController, LiveTrackingController |
| 50 | ChatController, RatingController, GamificationController, FleetController, ECommerceController, ECommerceWebhookController, WebhookController, ApiKeyController |
| 51 | SystemHealthController, BackupController, SmartAssignmentController, AutomationRuleController, SubscriptionController, CustomReportController, BIDashboardController, ScheduledTaskController, AsyncJobController |
| 52 | TenantController, TenantUserController, TenantQuotaController, TenantBrandingController |
| 53 | PaymentIntentController, PaymentRefundController, PaymentCallbackController, EInvoiceController, TaxController, FinancialController |
| 54 | DocumentController, ComplianceController, WorkflowDefinitionController, WorkflowExecutionController, ReportExportController |
| 55 | CarrierController, ThirdPartyPartnerController, DemandController, DataPipelineController, DeadLetterController, TelemetryController |
| 56 | DeviceMobileController, OfflineSyncController, SignatureController, DeliveryExperienceController, LiveNotificationController |
| 57 | LoyaltyController, CountryController, CurrencyController, IpManagementController, PlatformConfigController, PlatformOpsController, EventController, SmsController, WebSocketMessageController |

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
getAdminInvoicesAll(params), refundInvoice(id), getInvoiceStats(),
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
- Admin sidebar: add Users, Audit, Security, Notification Templates links
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
| 8 | Owner Merchants Mgmt | Owner | `owner/merchants-manage.html` | `owner-merchants-page.js` | `GET /api/merchants`, `POST /api/merchants`, `PUT /api/merchants/{id}` |

### API Methods to Add (~24)
```
createChatRoom(data), getMyChatRooms(), sendChatMessage(roomId, data), getRoomMessages(roomId, params),
submitRating(data), getCourierRatings(courierId), getShipmentRating(shipmentId), getMyRatings(),
getLeaderboard(params), getGamificationProfile(userId), getAchievements(), claimReward(rewardId),
createVehicle(data), getVehicles(params), updateVehicle(id, data), createFleetAssignment(data), logMaintenance(data),
connectEcommerce(data), getEcommerceConnections(), getEcommerceOrders(connectionId, params), disconnectEcommerce(id),
createWebhook(data), getWebhooks(), deleteWebhook(id), testWebhook(id),
createApiKey(data), getApiKeys(), revokeApiKey(id),
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
| 6 | Custom Report Builder | Owner | `owner/report-builder.html` | `owner-report-builder-page.js` | `GET /api/reports/custom`, `POST /api/reports/custom`, `PUT /api/reports/custom/{id}`, `POST /api/reports/custom/{id}/run` |
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

## Sprint 52 — Multi-Tenancy UI

> **Theme:** Full multi-tenant management — onboarding, branding, quotas, user control  
> **Migration:** V63 · **New Pages:** 8 · **New API Methods:** ~26

### Frontend Pages

| # | Page | Role | HTML | JS Handler | Key Endpoints |
|---|------|------|------|------------|---------------|
| 1 | Tenant Management | Admin | `admin/tenants.html` | `admin-tenants-page.js` | `GET /api/tenants`, `POST /api/tenants`, `PUT /api/tenants/{id}`, `DELETE /api/tenants/{id}` |
| 2 | Tenant Onboarding Wizard | Admin | `admin/tenant-onboard.html` | `admin-tenant-onboard-page.js` | `POST /api/tenants/onboard`, `GET /api/tenants/{id}/setup-status` |
| 3 | Tenant Branding | Admin | `admin/tenant-branding.html` | `admin-tenant-branding-page.js` | `GET /api/tenants/{id}/branding`, `PUT /api/tenants/{id}/branding`, `POST /api/tenants/{id}/branding/logo` |
| 4 | Tenant Quotas & Limits | Admin | `admin/tenant-quotas.html` | `admin-tenant-quotas-page.js` | `GET /api/tenants/{id}/quotas`, `PUT /api/tenants/{id}/quotas`, `GET /api/tenants/{id}/usage` |
| 5 | Tenant User Management | Admin | `admin/tenant-users.html` | `admin-tenant-users-page.js` | `GET /api/tenants/{id}/users`, `POST /api/tenants/{id}/users`, `PUT /api/tenants/{id}/users/{userId}`, `DELETE /api/tenants/{id}/users/{userId}` |
| 6 | Tenant Billing | Admin | `admin/tenant-billing.html` | `admin-tenant-billing-page.js` | `GET /api/tenants/{id}/billing`, `GET /api/tenants/{id}/invoices`, `POST /api/tenants/{id}/billing/charge` |
| 7 | Tenant Comparison | Admin | `admin/tenant-comparison.html` | `admin-tenant-comparison-page.js` | `GET /api/tenants/stats`, `GET /api/tenants/comparison`, `GET /api/tenants/growth` |
| 8 | My Tenant Settings | Owner | `owner/tenant-settings.html` | `owner-tenant-settings-page.js` | `GET /api/tenants/my`, `PUT /api/tenants/my/branding`, `GET /api/tenants/my/usage` |

### API Methods to Add (~26)
```
getTenants(params), createTenant(data), updateTenant(id, data), deleteTenant(id),
onboardTenant(data), getTenantSetupStatus(id),
getTenantBranding(id), updateTenantBranding(id, data), uploadTenantLogo(id, file),
getTenantQuotas(id), updateTenantQuotas(id, data), getTenantUsage(id),
getTenantUsers(id, params), createTenantUser(id, data), updateTenantUser(id, userId, data), deleteTenantUser(id, userId),
getTenantBilling(id), getTenantInvoices(id), chargeTenant(id, data),
getTenantStats(), getTenantComparison(), getTenantGrowth(),
getMyTenant(), updateMyTenantBranding(data), getMyTenantUsage()
```

### Database — V63
- Seed **tenant statuses**: `PENDING`, `ACTIVE`, `SUSPENDED`, `CANCELLED`
- Seed **tenant plan types**: `FREE`, `STARTER`, `PROFESSIONAL`, `ENTERPRISE`
- Seed **quota types**: `SHIPMENTS_PER_MONTH`, `USERS`, `WAREHOUSES`, `API_CALLS_PER_DAY`, `STORAGE_MB`
- Seed **default quotas** per plan type

### Sidebar Updates
- Admin sidebar: add Tenants section (Tenants, Onboarding, Branding, Quotas, Users, Billing, Comparison)

---

## Sprint 53 — Payments, E-Invoice & Tax

> **Theme:** Complete financial stack — payment lifecycle, electronic invoicing, tax compliance  
> **Migration:** V64 · **New Pages:** 8 · **New API Methods:** ~28

### Frontend Pages

| # | Page | Role | HTML | JS Handler | Key Endpoints |
|---|------|------|------|------------|---------------|
| 1 | Payment Intents | Owner | `owner/payment-intents.html` | `owner-payment-intents-page.js` | `GET /api/payments/intents`, `POST /api/payments/intents`, `GET /api/payments/intents/{id}`, `POST /api/payments/intents/{id}/capture` |
| 2 | Refund Management | Owner | `owner/refunds.html` | `owner-refunds-page.js` | `GET /api/payments/refunds`, `POST /api/payments/refunds`, `GET /api/payments/refunds/{id}`, `PUT /api/payments/refunds/{id}/approve` |
| 3 | Payment Callbacks Log | Admin | `admin/payment-callbacks.html` | `admin-payment-callbacks-page.js` | `GET /api/payments/callbacks`, `GET /api/payments/callbacks/{id}`, `POST /api/payments/callbacks/{id}/retry` |
| 4 | E-Invoice Dashboard | Owner | `owner/e-invoices.html` | `owner-e-invoices-page.js` | `GET /api/e-invoices`, `POST /api/e-invoices`, `POST /api/e-invoices/{id}/submit`, `GET /api/e-invoices/{id}/status` |
| 5 | Tax Configuration | Admin | `admin/tax.html` | `admin-tax-page.js` | `GET /api/tax/rules`, `POST /api/tax/rules`, `PUT /api/tax/rules/{id}`, `GET /api/tax/rates/{countryCode}` |
| 6 | Tax Reports | Owner | `owner/tax-reports.html` | `owner-tax-reports-page.js` | `GET /api/tax/reports`, `POST /api/tax/reports/generate`, `GET /api/tax/reports/{id}/download` |
| 7 | Financial Dashboard | Owner | `owner/financial.html` | `owner-financial-page.js` | `GET /api/financial/summary`, `GET /api/financial/revenue`, `GET /api/financial/expenses`, `GET /api/financial/profit-loss` |
| 8 | COD Reconciliation | Owner | `owner/cod-reconciliation.html` | `owner-cod-reconciliation-page.js` | `GET /api/financial/cod`, `GET /api/financial/cod/unreconciled`, `POST /api/financial/cod/reconcile` |

### API Methods to Add (~28)
```
getPaymentIntents(params), createPaymentIntent(data), getPaymentIntent(id), capturePayment(id),
getRefunds(params), createRefund(data), getRefund(id), approveRefund(id), rejectRefund(id),
getPaymentCallbacks(params), getPaymentCallback(id), retryCallback(id),
getEInvoices(params), createEInvoice(data), submitEInvoice(id), getEInvoiceStatus(id), downloadEInvoice(id),
getTaxRules(params), createTaxRule(data), updateTaxRule(id, data), deleteTaxRule(id), getTaxRates(countryCode),
getTaxReports(params), generateTaxReport(data), downloadTaxReport(id),
getFinancialSummary(params), getRevenue(params), getExpenses(params), getProfitLoss(params),
getCODSummary(params), getUnreconciledCOD(params), reconcileCOD(data)
```

### Database — V64
- Seed **payment intent statuses**: `CREATED`, `PROCESSING`, `SUCCEEDED`, `FAILED`, `CANCELLED`, `REFUNDED`
- Seed **refund statuses**: `PENDING`, `APPROVED`, `REJECTED`, `PROCESSED`
- Seed **e-invoice statuses**: `DRAFT`, `SUBMITTED`, `ACCEPTED`, `REJECTED`, `CANCELLED`
- Seed **tax types**: `VAT`, `SALES_TAX`, `SERVICE_TAX`, `EXEMPT`
- Seed **default VAT rates**: Egypt 14%, Saudi 15%, UAE 5%
- Seed **financial report types**: `PROFIT_LOSS`, `REVENUE`, `EXPENSE`, `COD_SUMMARY`

### Sidebar Updates
- Owner sidebar: add Financial, E-Invoices, Tax Reports, Payments, Refunds, COD Reconciliation links
- Admin sidebar: add Payment Callbacks, Tax Configuration links

---

## Sprint 54 — Documents, Compliance & Workflows

> **Theme:** Document management, regulatory compliance engine, workflow automation builder  
> **Migration:** V65 · **New Pages:** 8 · **New API Methods:** ~26

### Frontend Pages

| # | Page | Role | HTML | JS Handler | Key Endpoints |
|---|------|------|------|------------|---------------|
| 1 | Document Templates | Admin | `admin/documents.html` | `admin-documents-page.js` | `GET /api/documents/templates`, `POST /api/documents/templates`, `PUT /api/documents/templates/{id}`, `POST /api/documents/templates/{id}/generate` |
| 2 | Document Gallery | Shared | `documents.html` | `documents-page.js` | `GET /api/documents/my`, `GET /api/documents/{id}`, `GET /api/documents/{id}/download` |
| 3 | Compliance Dashboard | Admin | `admin/compliance.html` | `admin-compliance-page.js` | `GET /api/compliance/checks`, `POST /api/compliance/checks/run`, `GET /api/compliance/status`, `GET /api/compliance/violations` |
| 4 | Compliance Rules | Admin | `admin/compliance-rules.html` | `admin-compliance-rules-page.js` | `GET /api/compliance/rules`, `POST /api/compliance/rules`, `PUT /api/compliance/rules/{id}`, `PUT /api/compliance/rules/{id}/toggle` |
| 5 | Workflow Builder | Admin | `admin/workflows.html` | `admin-workflows-page.js` | `GET /api/workflows`, `POST /api/workflows`, `PUT /api/workflows/{id}`, `PUT /api/workflows/{id}/publish` |
| 6 | Workflow Executions | Admin | `admin/workflow-runs.html` | `admin-workflow-runs-page.js` | `GET /api/workflows/executions`, `GET /api/workflows/executions/{id}`, `POST /api/workflows/executions/{id}/retry`, `POST /api/workflows/executions/{id}/cancel` |
| 7 | Report Export Center | Shared | `report-exports.html` | `report-exports-page.js` | `GET /api/reports/exports`, `POST /api/reports/exports`, `GET /api/reports/exports/{id}/download`, `GET /api/reports/exports/{id}/status` |
| 8 | SLA Monitoring | Owner | `owner/sla-monitoring.html` | `owner-sla-monitoring-page.js` | `GET /api/contracts/sla/status`, `GET /api/contracts/sla/breaches`, `GET /api/contracts/sla/{id}/history` |

### API Methods to Add (~26)
```
getDocumentTemplates(params), createDocumentTemplate(data), updateDocumentTemplate(id, data), generateDocument(templateId, data),
getMyDocuments(params), getDocument(id), downloadDocument(id), deleteDocument(id),
getComplianceChecks(params), runComplianceCheck(data), getComplianceStatus(), getComplianceViolations(params),
getComplianceRules(params), createComplianceRule(data), updateComplianceRule(id, data), toggleComplianceRule(id),
getWorkflows(params), createWorkflow(data), updateWorkflow(id, data), publishWorkflow(id), deleteWorkflow(id),
getWorkflowExecutions(params), getWorkflowExecution(id), retryWorkflowExecution(id), cancelWorkflowExecution(id),
getReportExports(params), createReportExport(data), downloadReportExport(id), getReportExportStatus(id),
getSLAStatus(params), getSLABreaches(params), getSLAHistory(id)
```

### Database — V65
- Seed **document template types**: `AWB`, `INVOICE`, `CONTRACT`, `RECEIPT`, `MANIFEST`, `CUSTOMS_DECLARATION`
- Seed **compliance check types**: `DOCUMENT_EXPIRY`, `LICENSE_VALIDITY`, `INSURANCE_CHECK`, `KYC_VERIFICATION`, `DATA_PRIVACY`
- Seed **workflow step types**: `CONDITION`, `ACTION`, `DELAY`, `SPLIT`, `MERGE`, `NOTIFICATION`
- Seed **report export formats**: `PDF`, `EXCEL`, `CSV`, `JSON`
- Seed **SLA metric types**: `PICKUP_TIME`, `DELIVERY_TIME`, `FIRST_ATTEMPT_RATE`, `DAMAGE_RATE`, `RESPONSE_TIME`

---

## Sprint 55 — Carrier Partners, Demand & Telemetry

> **Theme:** Third-party carrier integration, demand forecasting, system telemetry & data pipelines  
> **Migration:** V66 · **New Pages:** 8 · **New API Methods:** ~28

### Frontend Pages

| # | Page | Role | HTML | JS Handler | Key Endpoints |
|---|------|------|------|------------|---------------|
| 1 | Carrier Management | Owner | `owner/carriers.html` | `owner-carriers-page.js` | `GET /api/carriers`, `POST /api/carriers`, `PUT /api/carriers/{id}`, `PUT /api/carriers/{id}/toggle` |
| 2 | Carrier Rate Cards | Owner | `owner/carrier-rates.html` | `owner-carrier-rates-page.js` | `GET /api/carriers/{id}/rates`, `POST /api/carriers/{id}/rates`, `PUT /api/carriers/{id}/rates/{rateId}` |
| 3 | Third-Party Partners | Owner | `owner/partners.html` | `owner-partners-page.js` | `GET /api/partners`, `POST /api/partners`, `PUT /api/partners/{id}`, `GET /api/partners/{id}/performance` |
| 4 | Demand Forecasting | Owner | `owner/demand.html` | `owner-demand-page.js` | `GET /api/demand/forecast`, `POST /api/demand/forecast/generate`, `GET /api/demand/trends`, `GET /api/demand/seasonality` |
| 5 | Data Pipeline Monitor | Admin | `admin/data-pipelines.html` | `admin-data-pipelines-page.js` | `GET /api/data-pipelines`, `POST /api/data-pipelines`, `PUT /api/data-pipelines/{id}`, `POST /api/data-pipelines/{id}/run` |
| 6 | Dead Letter Queue | Admin | `admin/dead-letters.html` | `admin-dead-letters-page.js` | `GET /api/dead-letters`, `GET /api/dead-letters/{id}`, `POST /api/dead-letters/{id}/retry`, `DELETE /api/dead-letters/{id}` |
| 7 | Telemetry Dashboard | Admin | `admin/telemetry.html` | `admin-telemetry-page.js` | `GET /api/telemetry/metrics`, `GET /api/telemetry/traces`, `GET /api/telemetry/errors`, `GET /api/telemetry/performance` |
| 8 | Multi-Carrier Shipping | Merchant | `merchant/multi-carrier.html` | `merchant-multi-carrier-page.js` | `GET /api/carriers/quotes`, `POST /api/carriers/book`, `GET /api/carriers/tracking/{shipmentId}` |

### API Methods to Add (~28)
```
getCarriers(params), createCarrier(data), updateCarrier(id, data), toggleCarrier(id),
getCarrierRates(carrierId), createCarrierRate(carrierId, data), updateCarrierRate(carrierId, rateId, data), deleteCarrierRate(carrierId, rateId),
getPartners(params), createPartner(data), updatePartner(id, data), getPartnerPerformance(id),
getDemandForecast(params), generateDemandForecast(data), getDemandTrends(params), getDemandSeasonality(params),
getDataPipelines(params), createDataPipeline(data), updateDataPipeline(id, data), runDataPipeline(id), getDataPipelineStatus(id),
getDeadLetters(params), getDeadLetter(id), retryDeadLetter(id), deleteDeadLetter(id), bulkRetryDeadLetters(ids),
getTelemetryMetrics(params), getTelemetryTraces(params), getTelemetryErrors(params), getTelemetryPerformance(params),
getCarrierQuotes(data), bookCarrier(data), getCarrierTracking(shipmentId)
```

### Database — V66
- Seed **carrier types**: `INTERNAL`, `EXTERNAL`, `PARTNER`, `FREELANCE`
- Seed **carrier statuses**: `ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING_VERIFICATION`
- Seed **demand forecast horizons**: `DAILY`, `WEEKLY`, `MONTHLY`, `QUARTERLY`
- Seed **pipeline statuses**: `IDLE`, `RUNNING`, `COMPLETED`, `FAILED`, `PAUSED`
- Seed **dead letter categories**: `NOTIFICATION_FAILED`, `WEBHOOK_FAILED`, `PAYMENT_FAILED`, `SYNC_FAILED`
- Seed **telemetry metric types**: `REQUEST_LATENCY`, `ERROR_RATE`, `THROUGHPUT`, `CPU_USAGE`, `MEMORY_USAGE`

### Sidebar Updates
- Owner sidebar: add Carriers, Partners, Demand links
- Admin sidebar: add Data Pipelines, Dead Letters, Telemetry links
- Merchant sidebar: add Multi-Carrier Shipping link

---

## Sprint 56 — Mobile PWA + Offline + Signatures + Delivery Experience

> **Theme:** Progressive Web App features, offline support, digital signatures, delivery UX  
> **Migration:** V67 · **New Pages:** 8 · **New API Methods:** ~24

### Frontend Pages

| # | Page | Role | HTML | JS Handler | Key Endpoints |
|---|------|------|------|------------|---------------|
| 1 | Mobile Device Manager | Admin | `admin/devices.html` | `admin-devices-page.js` | `GET /api/devices`, `POST /api/devices`, `PUT /api/devices/{id}`, `DELETE /api/devices/{id}`, `POST /api/devices/{id}/wipe` |
| 2 | Offline Sync Dashboard | Admin | `admin/offline-sync.html` | `admin-offline-sync-page.js` | `GET /api/offline/sync-status`, `GET /api/offline/conflicts`, `POST /api/offline/conflicts/{id}/resolve`, `GET /api/offline/queue` |
| 3 | Digital Signature Pad | Courier | `courier/signature.html` | `courier-signature-page.js` | `POST /api/signatures`, `GET /api/signatures/{shipmentId}`, `GET /api/signatures/{id}/image` |
| 4 | Delivery Experience Config | Admin | `admin/delivery-experience.html` | `admin-delivery-experience-page.js` | `GET /api/delivery-experience/config`, `PUT /api/delivery-experience/config`, `GET /api/delivery-experience/templates` |
| 5 | Customer Delivery Page | Public | `delivery-feedback.html` | `delivery-feedback-page.js` | `GET /api/delivery-experience/{token}`, `POST /api/delivery-experience/{token}/feedback`, `POST /api/delivery-experience/{token}/reschedule` |
| 6 | Live Notification Center | Admin | `admin/live-notifications.html` | `admin-live-notifications-page.js` | `GET /api/notifications/live/connections`, `POST /api/notifications/live/broadcast`, `GET /api/notifications/live/stats` |
| 7 | PWA Install & Settings | Shared | `pwa-settings.html` | `pwa-settings-page.js` | `GET /api/devices/my`, `PUT /api/devices/my/preferences`, `POST /api/devices/my/push-token` |
| 8 | Courier Mobile Dashboard | Courier | `courier/mobile-dashboard.html` | `courier-mobile-dashboard-page.js` | `GET /api/couriers/me/today`, `GET /api/couriers/me/earnings`, `POST /api/couriers/me/status` |

### API Methods to Add (~24)
```
getDevices(params), createDevice(data), updateDevice(id, data), deleteDevice(id), wipeDevice(id),
getOfflineSyncStatus(), getOfflineConflicts(params), resolveConflict(id, data), getOfflineQueue(),
createSignature(data), getShipmentSignature(shipmentId), getSignatureImage(id),
getDeliveryExperienceConfig(), updateDeliveryExperienceConfig(data), getDeliveryExperienceTemplates(),
getDeliveryExperiencePage(token), submitDeliveryFeedback(token, data), rescheduleDelivery(token, data),
getLiveNotificationConnections(), broadcastNotification(data), getLiveNotificationStats(),
getMyDevice(), updateMyDevicePreferences(data), registerPushToken(data),
getCourierToday(), getCourierEarnings(params), updateCourierStatus(status)
```

### Database — V67
- Seed **device types**: `ANDROID`, `IOS`, `WEB_PWA`, `DESKTOP`
- Seed **device statuses**: `ACTIVE`, `INACTIVE`, `LOST`, `WIPED`
- Seed **sync conflict resolution strategies**: `SERVER_WINS`, `CLIENT_WINS`, `MANUAL`, `MERGE`
- Seed **signature types**: `RECIPIENT`, `COURIER`, `WAREHOUSE_STAFF`
- Seed **delivery experience templates**: `STANDARD`, `PREMIUM`, `EXPRESS`
- Seed **courier online statuses**: `ONLINE`, `OFFLINE`, `ON_BREAK`, `ON_DELIVERY`

### Technical
- Canvas API integration for digital signature capture
- Service Worker for offline-first PWA capability
- Push notification subscription via Web Push API

---

## Sprint 57 — Loyalty, Localization & Platform Polish

> **Theme:** Loyalty programs, multi-country/currency support, IP management, platform config, final controller coverage  
> **Migration:** V68 · **New Pages:** 8 · **New API Methods:** ~30

### Frontend Pages

| # | Page | Role | HTML | JS Handler | Key Endpoints |
|---|------|------|------|------------|---------------|
| 1 | Loyalty Programs | Owner | `owner/loyalty.html` | `owner-loyalty-page.js` | `GET /api/loyalty/programs`, `POST /api/loyalty/programs`, `PUT /api/loyalty/programs/{id}`, `GET /api/loyalty/programs/{id}/members` |
| 2 | Loyalty Rewards Store | Shared | `loyalty.html` | `loyalty-page.js` | `GET /api/loyalty/my`, `GET /api/loyalty/rewards`, `POST /api/loyalty/rewards/{id}/redeem`, `GET /api/loyalty/history` |
| 3 | Country & Currency Config | Admin | `admin/localization.html` | `admin-localization-page.js` | `GET /api/countries`, `POST /api/countries`, `GET /api/currencies`, `POST /api/currencies`, `PUT /api/currencies/{id}/rates` |
| 4 | IP Management & ACL | Admin | `admin/ip-management.html` | `admin-ip-management-page.js` | `GET /api/security/ip-whitelist`, `POST /api/security/ip-whitelist`, `DELETE /api/security/ip-whitelist/{id}`, `GET /api/security/ip-blacklist` |
| 5 | Platform Configuration | Admin | `admin/platform-config.html` | `admin-platform-config-page.js` | `GET /api/platform/config`, `PUT /api/platform/config`, `GET /api/platform/config/categories`, `POST /api/platform/config/reset` |
| 6 | Platform Operations | Admin | `admin/platform-ops.html` | `admin-platform-ops-page.js` | `GET /api/platform/ops/status`, `POST /api/platform/ops/maintenance-mode`, `POST /api/platform/ops/cache/clear`, `POST /api/platform/ops/reindex` |
| 7 | Event Log Viewer | Admin | `admin/events.html` | `admin-events-page.js` | `GET /api/events`, `GET /api/events/{id}`, `GET /api/events/stats`, `GET /api/events/stream` |
| 8 | SMS & Communication Log | Admin | `admin/communications.html` | `admin-communications-page.js` | `GET /api/sms/logs`, `POST /api/sms/send`, `GET /api/sms/stats`, `GET /api/sms/templates` |

### API Methods to Add (~30)
```
getLoyaltyPrograms(params), createLoyaltyProgram(data), updateLoyaltyProgram(id, data), getLoyaltyMembers(id, params),
getMyLoyalty(), getLoyaltyRewards(params), redeemLoyaltyReward(id), getLoyaltyHistory(params),
getCountries(params), createCountry(data), updateCountry(id, data),
getCurrencies(params), createCurrency(data), updateCurrencyRates(id, data),
getIPWhitelist(params), addToIPWhitelist(data), removeFromIPWhitelist(id),
getIPBlacklist(params), addToIPBlacklist(data), removeFromIPBlacklist(id),
getPlatformConfig(params), updatePlatformConfig(data), getPlatformConfigCategories(), resetPlatformConfig(category),
getPlatformOpsStatus(), toggleMaintenanceMode(data), clearPlatformCache(), reindexData(),
getEvents(params), getEvent(id), getEventStats(params),
getSMSLogs(params), sendSMS(data), getSMSStats(params), getSMSTemplates()
```

### Database — V68
- Seed **loyalty program types**: `POINTS`, `TIERS`, `CASHBACK`, `STAMPS`
- Seed **loyalty tiers**: `BRONZE`, `SILVER`, `GOLD`, `PLATINUM`
- Seed **supported countries**: Egypt, Saudi Arabia, UAE, Kuwait, Qatar, Oman, Bahrain, Jordan
- Seed **currencies**: EGP, SAR, AED, KWD, QAR, OMR, BHD, JOD (with default rates)
- Seed **platform config categories**: `GENERAL`, `SECURITY`, `NOTIFICATIONS`, `PAYMENTS`, `SHIPPING`, `APPEARANCE`
- Seed **SMS template types**: `OTP`, `SHIPMENT_UPDATE`, `DELIVERY_CONFIRMATION`, `PAYMENT_RECEIVED`, `MARKETING`

### Sidebar Updates
- Owner sidebar: add Loyalty link
- Admin sidebar: add Localization, IP Management, Platform Config, Platform Ops, Events, Communications links
- All role sidebars: add Loyalty Rewards link

---

## Summary — Sprints 48–57 at a Glance

| Sprint | Theme | Pages | JS Handlers | API Methods | Migration |
|--------|-------|-------|-------------|-------------|-----------|
| **47** ✅ | Merchant & Courier Tools | 8 | 8 | ~22 | V58 |
| **48** | Warehouse Ops + Support | 8 | 8 | ~24 | V59 |
| **49** | Admin Panel + Financial | 8 | 8 | ~26 | V60 |
| **50** | Chat, Ratings, Gamification | 8 | 8 | ~24 | V61 |
| **51** | System Admin + Advanced | 8 | 8 | ~28 | V62 |
| **52** | Multi-Tenancy UI | 8 | 8 | ~26 | V63 |
| **53** | Payments, E-Invoice & Tax | 8 | 8 | ~28 | V64 |
| **54** | Documents, Compliance, Workflows | 8 | 8 | ~26 | V65 |
| **55** | Carriers, Demand & Telemetry | 8 | 8 | ~28 | V66 |
| **56** | Mobile PWA + Signatures | 8 | 8 | ~24 | V67 |
| **57** | Loyalty, Localization, Polish | 8 | 8 | ~30 | V68 |
| **TOTAL (48-57)** | | **80** | **80** | **~264** | **10** |

---

## Projected State After Sprint 57

| Metric | Post S47 | Post S51 | Post S57 | Total Change |
|--------|----------|----------|----------|-------------|
| HTML Pages | 41 | 73 | **121** | +80 |
| JS Page Handlers | 39 | 71 | **119** | +80 |
| `api_service.js` methods | ~162 | ~286 | **~426** | +264 |
| Backend coverage | ~40% | ~72% | **~100%** | +60% |
| Flyway migrations | V58 | V62 | **V68** | +10 |

---

## Execution Order Priority

| Priority | Sprints | Rationale |
|----------|---------|-----------|
| **P0 — Critical** | 48, 49 | Warehouse ops (revenue-blocking), Admin panel (operations) |
| **P1 — High** | 50, 51 | Social features drive engagement, System tools prevent downtime |
| **P2 — Medium** | 52, 53 | Multi-tenancy enables SaaS model, Payments complete financial stack |
| **P3 — Standard** | 54, 55 | Documents/compliance for enterprise, Carriers for multi-carrier |
| **P4 — Enhancement** | 56, 57 | Mobile PWA / Offline for field ops, Loyalty/localization for growth |

---

## Sprint Execution Checklist (Per Sprint)

For each sprint, follow this implementation order:

1. **Database** — Create Flyway migration (Vxx) with seed data
2. **API Layer** — Add new methods to `api_service.js` before `searchData()`
3. **HTML Pages** — Create page files (RTL Arabic, Bootstrap 5.3.2, role-specific sidebar)
4. **JS Handlers** — Create page handler extending `BasePageHandler`
5. **Routing** — Register pages in `app.js` (`getCurrentPage()` + `isPageHandlingAuth()`)
6. **Navigation** — Update sidebar links on all existing pages for affected roles
7. **Build & Test** — `mvn compile`, verify all files exist, count totals

---

## Risk Register

| Risk | Impact | Mitigation |
|------|--------|-----------|
| `api_service.js` grows too large (~400+ methods) | Maintenance | Consider splitting into domain-specific service files in Sprint 54+ |
| `app.js` routing becomes unwieldy | Bugs | Refactor to map-based auto-routing after Sprint 51 |
| Sidebar navigation grows long | UX | Add collapsible sections/groups after Sprint 50 |
| WebSocket complexity (Chat, Live Tracking) | Stability | Implement reconnection logic + fallback polling |
| Multi-tenancy data isolation | Security | Thorough testing with tenant-scoped queries |
| Offline sync conflicts | Data integrity | Conservative "server-wins" default strategy |
