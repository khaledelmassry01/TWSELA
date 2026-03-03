# Twsela — Complete Controller & Endpoint Audit

> **Generated:** 2025 | **Total Controllers:** 70 | **Total REST Endpoints:** ~350+ | **WebSocket Channels:** 4

---

## Table of Contents

1. [Summary](#summary)
2. [Previously Documented Controllers (16)](#previously-documented-controllers)
3. [NEW Controllers Added After Sprint 8 (53)](#new-controllers-added-after-sprint-8)
4. [WebSocket Controller (1)](#websocket-controller)
5. [Role Reference](#role-reference)
6. [Notes](#notes)

---

## Summary

| Category | Count |
|----------|-------|
| Total Controller Files | 70 |
| Previously Documented (Sprint ≤ 8) | 16 |
| **NEW (Post-Sprint 8)** | **53** |
| WebSocket (non-REST) | 1 |
| Missing from Docs (DebugController) | Not Found in Code |

### Roles Used

| Role | Code |
|------|------|
| Owner | `OWNER` |
| Admin | `ADMIN` |
| Merchant | `MERCHANT` |
| Courier | `COURIER` |
| Warehouse Manager | `WAREHOUSE_MANAGER` |

---

## Previously Documented Controllers

These 16 controllers were in the existing documentation (Sprint ≤ 8). New endpoints may have been added.

---

### 1. AuthController
- **File:** `AuthController.java`
- **Base Path:** `/api/auth`
- **Class-Level Role:** None (mixed public/authenticated)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/auth/login` | Public | Login | `LoginRequest` → token+user+role |
| 2 | `GET` | `/api/auth/me` | Authenticated | Get current user | → `UserResponseDTO` |
| 3 | `GET` | `/api/auth/health` | Authenticated | Auth health check | — |
| 4 | `POST` | `/api/auth/logout` | Authenticated | Logout (blacklist token) | — |
| 5 | `POST` | `/api/auth/change-password` | Authenticated | Change password | `ChangePasswordRequest` |
| 6 | `POST` | `/api/auth/refresh` | Authenticated | Refresh JWT token | — |

---

### 2. HealthController
- **File:** `HealthController.java`
- **Base Path:** `/api`
- **Class-Level Role:** None (public)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/health` | Public | App health check (DB + Redis status) | — |

---

### 3. PublicController
- **File:** `PublicController.java`
- **Base Path:** `/api/public`
- **Class-Level Role:** None (public)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/public/track/{trackingNumber}` | Public | Track shipment | — |
| 2 | `POST` | `/api/public/feedback/{trackingNumber}` | Public | Submit feedback | `ServiceFeedback` |
| 3 | `POST` | `/api/public/forgot-password` | Public | Forgot password / send OTP | `PasswordResetRequest` |
| 4 | `POST` | `/api/public/send-otp` | Public | Send OTP | — |
| 5 | `POST` | `/api/public/reset-password` | Public | Reset password with OTP | `PasswordResetRequest` |
| 6 | `POST` | `/api/public/contact` | Public | Submit contact form | `ContactFormRequest` |
| 7 | `GET` | `/api/public/contact/offices` | Public | Get office locations | — |

---

### 4. UserController
- **File:** `UserController.java`
- **Base Path:** `/api`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/users` | OWNER, ADMIN | All users | → `List<UserResponseDTO>` |
| 2 | `POST` | `/api/users` | OWNER, ADMIN | Create user | `CreateUserRequest` → `UserResponseDTO` |
| 3 | `PUT` | `/api/users/{id}` | OWNER, ADMIN | Update user | `UpdateUserRequest` → `UserResponseDTO` |
| 4 | `DELETE` | `/api/users/{id}` | OWNER, ADMIN | Delete user | — |
| 5 | `PUT` | `/api/users/profile` | Authenticated | Update my profile | Map body → `UserResponseDTO` |
| 6 | `GET` | `/api/couriers/{id}` | OWNER, ADMIN | Get courier by ID | → `UserResponseDTO` |
| 7 | `POST` | `/api/couriers` | OWNER, ADMIN | Create courier | `CreateUserRequest` → `UserResponseDTO` |
| 8 | `PUT` | `/api/couriers/{id}` | OWNER, ADMIN | Update courier | `UpdateUserRequest` → `UserResponseDTO` |
| 9 | `DELETE` | `/api/couriers/{id}` | OWNER, ADMIN | Delete courier | — |
| 10 | `GET` | `/api/couriers/{id}/location` | OWNER, ADMIN, COURIER | Get courier location | — |
| 11 | `PUT` | `/api/couriers/{id}/location` | COURIER | Update courier location | Map body |
| 12 | `GET` | `/api/merchants/{id}` | OWNER, ADMIN | Get merchant by ID | → `UserResponseDTO` |
| 13 | `POST` | `/api/merchants` | OWNER, ADMIN | Create merchant | `CreateUserRequest` → `UserResponseDTO` |
| 14 | `PUT` | `/api/merchants/{id}` | OWNER, ADMIN | Update merchant | `UpdateUserRequest` → `UserResponseDTO` |
| 15 | `GET` | `/api/employees/{id}` | OWNER, ADMIN | Get employee by ID | → `UserResponseDTO` |
| 16 | `PUT` | `/api/employees/{id}` | OWNER, ADMIN | Update employee | `UpdateUserRequest` → `UserResponseDTO` |
| 17 | `GET` | `/api/couriers` | OWNER, ADMIN | List couriers (paginated) | → `ApiPageResponse<UserResponseDTO>` |
| 18 | `GET` | `/api/merchants` | OWNER, ADMIN | List merchants (paginated) | → `ApiPageResponse<UserResponseDTO>` |
| 19 | `GET` | `/api/employees` | OWNER, ADMIN | List employees (paginated) | → `ApiPageResponse<UserResponseDTO>` |
| 20 | `POST` | `/api/employees` | OWNER, ADMIN | Create employee | `CreateUserRequest` → `UserResponseDTO` |

---

### 5. ShipmentController
- **File:** `ShipmentController.java` (915 lines)
- **Base Path:** `/api/shipments`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/shipments` | OWNER, ADMIN, MERCHANT | All shipments (paginated, role-filtered) | — |
| 2 | `GET` | `/api/shipments/{id}` | ALL roles | Shipment by ID | — |
| 3 | `GET` | `/api/shipments/count` | ALL roles | Total shipment count | — |
| 4 | `POST` | `/api/shipments` | OWNER, ADMIN, MERCHANT | Create shipment | `CreateShipmentRequest` |
| 5 | `POST` | `/api/shipments/warehouse/receive` | WAREHOUSE_MANAGER, OWNER | Receive shipments at warehouse | Map body (trackingNumbers) |
| 6 | `GET` | `/api/shipments/warehouse/inventory` | WAREHOUSE_MANAGER, OWNER | Warehouse inventory (paginated) | — |
| 7 | `POST` | `/api/shipments/warehouse/dispatch/{courierId}` | WAREHOUSE_MANAGER, OWNER | Dispatch to courier | Map body (shipmentIds) |
| 8 | `POST` | `/api/shipments/warehouse/reconcile/courier/{courierId}` | WAREHOUSE_MANAGER, OWNER | End-of-day reconciliation | `ReconcileRequest` |
| 9 | `GET` | `/api/shipments/warehouse/couriers` | WAREHOUSE_MANAGER, OWNER | Active couriers for dispatch | — |
| 10 | `GET` | `/api/shipments/warehouse/courier/{courierId}/shipments` | WAREHOUSE_MANAGER, OWNER | Courier's shipments for reconcile | — |
| 11 | `GET` | `/api/shipments/warehouse/stats` | WAREHOUSE_MANAGER, OWNER | Warehouse statistics | — |
| 12 | `POST` | `/api/shipments/{id}/return-request` | ALL roles | Request return to origin (RTO) | Map body (reason) |
| 13 | `PUT` | `/api/shipments/courier/location/update` | COURIER | Update courier location (web) | `LocationUpdateRequest` |
| 14 | `GET` | `/api/shipments/list` | OWNER, ADMIN, MERCHANT, COURIER | List shipments (alt endpoint) | — |

---

### 6. ManifestController
- **File:** `ManifestController.java`
- **Base Path:** `/api/manifests`
- **Class-Level Role:** OWNER, ADMIN, COURIER

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/manifests` | Class-level | All manifests (role-filtered) | — |
| 2 | `POST` | `/api/manifests` | OWNER, ADMIN | Create manifest | `CreateManifestRequest` |
| 3 | `GET` | `/api/manifests/{manifestId}` | Class-level | Manifest details | — |
| 4 | `POST` | `/api/manifests/{manifestId}/shipments` | OWNER, ADMIN | Assign shipments by ID | — |
| 5 | `PUT` | `/api/manifests/{manifestId}/status` | Class-level | Update manifest status | — |
| 6 | `POST` | `/api/manifests/{manifestId}/assign` | OWNER, ADMIN, WAREHOUSE_MANAGER | Assign by tracking number | — |

---

### 7. MasterDataController
- **File:** `MasterDataController.java`
- **Base Path:** `/api/master`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/master/users` | Class-level | All users (paginated) | — |
| 2 | `POST` | `/api/master/users` | OWNER | Create user | — |
| 3 | `PUT` | `/api/master/users/{id}` | OWNER | Update user | — |
| 4 | `DELETE` | `/api/master/users/{id}` | OWNER | Soft delete user | — |
| 5 | `GET` | `/api/master/zones` | Class-level | All zones (cached) | — |
| 6 | `POST` | `/api/master/zones` | OWNER | Create zone | — |
| 7 | `PUT` | `/api/master/zones/{id}` | OWNER | Update zone | — |
| 8 | `DELETE` | `/api/master/zones/{id}` | OWNER | Delete zone | — |
| 9 | `GET` | `/api/master/pricing` | Class-level | All pricing (cached) | — |
| 10 | `POST` | `/api/master/pricing` | OWNER | Create pricing | — |
| 11 | `PUT` | `/api/master/pricing/{id}` | OWNER | Update pricing | — |
| 12 | `DELETE` | `/api/master/pricing/{id}` | OWNER | Delete pricing | — |
| 13 | `GET` | `/api/master/telemetry` | OWNER | Telemetry settings | — |
| 14 | `PUT` | `/api/master/telemetry` | OWNER | Update telemetry setting | — |
| 15 | `GET` | `/api/master/telemetry/{key}` | OWNER | Telemetry by key | — |
| 16 | `DELETE` | `/api/master/telemetry/{key}` | OWNER | Delete telemetry setting | — |

---

### 8. DashboardController
- **File:** `DashboardController.java`
- **Base Path:** `/api/dashboard`
- **Class-Level Role:** ALL roles

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/dashboard/summary` | ALL roles | Dashboard summary (role-based) | — |
| 2 | `GET` | `/api/dashboard/statistics` | ALL roles | Global statistics (cached) | — |
| 3 | `GET` | `/api/dashboard/dashboard-stats` | ALL roles | Today/week/month stats | — |
| 4 | `GET` | `/api/dashboard/revenue-chart` | ALL roles | Revenue chart data | — |
| 5 | `GET` | `/api/dashboard/shipments-chart` | ALL roles | Shipments chart data | — |

---

### 9. FinancialController
- **File:** `FinancialController.java`
- **Base Path:** `/api/financial`
- **Class-Level Role:** OWNER, ADMIN, MERCHANT, COURIER

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/financial/payouts` | Class-level | All payouts (role-filtered) | — |
| 2 | `POST` | `/api/financial/payouts` | OWNER, ADMIN | Create payout | `CreatePayoutRequest` |
| 3 | `GET` | `/api/financial/payouts/{payoutId}` | Class-level | Payout details | — |
| 4 | `PUT` | `/api/financial/payouts/{payoutId}/status` | OWNER, ADMIN | Update payout status | — |
| 5 | `GET` | `/api/financial/payouts/pending` | OWNER, ADMIN | Pending payouts | — |
| 6 | `GET` | `/api/financial/payouts/user/{userId}` | Class-level | User's payouts | — |
| 7 | `GET` | `/api/financial/payouts/{payoutId}/items` | Class-level | Payout items | — |

---

### 10. ReportsController
- **File:** `ReportsController.java`
- **Base Path:** `/api/reports`
- **Class-Level Role:** OWNER, ADMIN, MERCHANT, COURIER

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/reports/shipments` | Class-level | Shipment report (role-filtered) | — |
| 2 | `GET` | `/api/reports/couriers` | OWNER, ADMIN | Courier report | — |
| 3 | `GET` | `/api/reports/merchants` | OWNER, ADMIN | Merchant report | — |
| 4 | `GET` | `/api/reports/warehouse` | OWNER, ADMIN, WAREHOUSE_MANAGER | Warehouse report | — |
| 5 | `GET` | `/api/reports/dashboard` | Class-level | Dashboard report | — |

---

### 11. SettingsController
- **File:** `SettingsController.java`
- **Base Path:** `/api/settings`
- **Class-Level Role:** ALL roles

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/settings` | ALL roles | Get user settings | — |
| 2 | `POST` | `/api/settings` | ALL roles | Save user settings | — |
| 3 | `POST` | `/api/settings/reset` | ALL roles | Reset to defaults | — |

---

### 12. SmsController
- **File:** `SmsController.java`
- **Base Path:** `/api/sms`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/sms/send` | Class-level | Send SMS | params: phoneNumber, message |
| 2 | `POST` | `/api/sms/send-otp` | Class-level | Send OTP | param: phoneNumber |
| 3 | `POST` | `/api/sms/send-notification` | Class-level | Send notification SMS | params: phoneNumber, notificationType, trackingNumber |
| 4 | `GET` | `/api/sms/test` | Class-level | Test SMS service | — |

---

### 13. AuditController
- **File:** `AuditController.java`
- **Base Path:** `/api/audit`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/audit/logs` | Class-level | Audit logs (filtered) | — |
| 2 | `GET` | `/api/audit/entity/{entityType}/{entityId}` | Class-level | Entity audit log | — |
| 3 | `GET` | `/api/audit/user/{userId}` | Class-level | User audit log | — |

---

### 14. BackupController
- **File:** `BackupController.java`
- **Base Path:** `/api/backup`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/backup/create` | Class-level | Create backup | — |
| 2 | `POST` | `/api/backup/restore` | Class-level | Restore backup | — |
| 3 | `GET` | `/api/backup/status` | Class-level | Backup status | — |
| 4 | `GET` | `/api/backup/test` | Class-level | Test backup service | — |

---

### 15. NotificationController
- **File:** `NotificationController.java`
- **Base Path:** `/api/notifications`
- **Class-Level Role:** Authenticated

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/notifications` | Authenticated | Paginated notifications | → `NotificationDTO` |
| 2 | `GET` | `/api/notifications/unread` | Authenticated | Unread notifications + count | — |
| 3 | `PUT` | `/api/notifications/{id}/read` | Authenticated | Mark as read | — |
| 4 | `PUT` | `/api/notifications/read-all` | Authenticated | Mark all as read | — |

---

### 16. TelemetryController
- **File:** `TelemetryController.java`
- **Base Path:** `/api/telemetry`
- **Class-Level Role:** None (mixed)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/telemetry` | No auth | Ingest telemetry event | Map body |
| 2 | `GET` | `/api/telemetry` | OWNER, ADMIN | Get recent telemetry events | — |

> **Note:** DebugController (listed in existing docs) was **NOT found** in the codebase.

---

## NEW Controllers Added After Sprint 8

The following **53 controllers** are NEW and not in existing documentation.

---

### 17. AnalyticsController ⭐ NEW
- **File:** `AnalyticsController.java`
- **Base Path:** `/api/analytics`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/analytics/revenue` | Class-level | Revenue report by period | → `AnalyticsDTO.RevenueReport` |
| 2 | `GET` | `/api/analytics/status-distribution` | Class-level | Shipment status distribution | → `List<AnalyticsDTO.StatusDistribution>` |
| 3 | `GET` | `/api/analytics/courier-ranking` | Class-level | Courier performance ranking | → `List<AnalyticsDTO.CourierPerformance>` |
| 4 | `GET` | `/api/analytics/top-merchants` | Class-level | Top merchants | → `List<AnalyticsDTO.TopMerchant>` |

---

### 18. ApiKeyController ⭐ NEW
- **File:** `ApiKeyController.java`
- **Base Path:** `/api/developer/keys`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/developer/keys` | MERCHANT, OWNER | Create API key | `CreateApiKeyRequest` → `ApiKeyCreatedResponse` |
| 2 | `GET` | `/api/developer/keys` | MERCHANT, OWNER | List my API keys | → `List<ApiKeyResponse>` |
| 3 | `PUT` | `/api/developer/keys/{id}/rotate` | MERCHANT, OWNER | Rotate API key | — |
| 4 | `DELETE` | `/api/developer/keys/{id}` | MERCHANT, OWNER | Revoke API key | — |
| 5 | `GET` | `/api/developer/keys/{id}/usage` | MERCHANT, OWNER | Key usage stats | — |

---

### 19. AsyncJobController ⭐ NEW
- **File:** `AsyncJobController.java`
- **Base Path:** `/api/jobs`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/jobs` | OWNER, ADMIN | List jobs | `AsyncJobDTO.AsyncJobRequest` |
| 2 | `POST` | `/api/jobs` | OWNER, ADMIN | Create job | — |
| 3 | `GET` | `/api/jobs/{jobId}` | OWNER, ADMIN | Job details | — |
| 4 | `POST` | `/api/jobs/{jobId}/cancel` | OWNER, ADMIN | Cancel job | — |
| 5 | `GET` | `/api/jobs/stats` | OWNER, ADMIN | Job statistics | — |

---

### 20. AutomationRuleController ⭐ NEW
- **File:** `AutomationRuleController.java`
- **Base Path:** `/api/automation-rules`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/automation-rules` | OWNER, ADMIN | Create rule | `AutomationRuleDto` |
| 2 | `GET` | `/api/automation-rules/{id}` | OWNER, ADMIN | Get rule | — |
| 3 | `GET` | `/api/automation-rules` | OWNER, ADMIN | List rules by tenant | — |
| 4 | `PUT` | `/api/automation-rules/{id}` | OWNER, ADMIN | Update rule | `AutomationRuleDto` |
| 5 | `PATCH` | `/api/automation-rules/{id}/activate` | OWNER, ADMIN | Activate | — |
| 6 | `PATCH` | `/api/automation-rules/{id}/deactivate` | OWNER, ADMIN | Deactivate | — |
| 7 | `DELETE` | `/api/automation-rules/{id}` | OWNER, ADMIN | Delete | — |

---

### 21. BIDashboardController ⭐ NEW
- **File:** `BIDashboardController.java`
- **Base Path:** `/api/bi-analytics`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/bi-analytics/summary` | Class-level | Executive summary KPIs | — |
| 2 | `GET` | `/api/bi-analytics/revenue` | Class-level | Revenue analytics by region/merchant | — |
| 3 | `GET` | `/api/bi-analytics/operations` | Class-level | Operations analytics | — |
| 4 | `GET` | `/api/bi-analytics/couriers` | Class-level | Courier analytics | — |
| 5 | `GET` | `/api/bi-analytics/merchants` | Class-level | Merchant analytics | — |
| 6 | `GET` | `/api/bi-analytics/kpi/trends` | Class-level | KPI daily trends | — |

---

### 22. BulkUploadController ⭐ NEW
- **File:** `BulkUploadController.java`
- **Base Path:** `/api/shipments/bulk`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/shipments/bulk` | OWNER, ADMIN, MERCHANT | Upload bulk shipments (Excel) | `MultipartFile` |
| 2 | `GET` | `/api/shipments/bulk/template` | OWNER, ADMIN, MERCHANT | Download Excel template | — |

---

### 23. ChatController ⭐ NEW
- **File:** `ChatController.java`
- **Base Path:** `/api/chat`
- **Class-Level Role:** Authenticated

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/chat/rooms` | Authenticated | Create chat room | `CreateChatRoomRequest` |
| 2 | `POST` | `/api/chat/messages` | Authenticated | Send message | `SendChatMessageRequest` |
| 3 | `GET` | `/api/chat/rooms/{roomId}/messages` | Authenticated | Room messages | — |
| 4 | `GET` | `/api/chat/rooms/shipment/{shipmentId}` | Authenticated | Rooms for shipment | — |
| 5 | `GET` | `/api/chat/rooms/my` | Authenticated | My chat rooms | — |
| 6 | `POST` | `/api/chat/rooms/{roomId}/archive` | Authenticated | Archive room | — |

---

### 24. ComplianceController ⭐ NEW
- **File:** `ComplianceController.java`
- **Base Path:** `/api/compliance`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/compliance/rules` | OWNER, ADMIN | Compliance rules | — |
| 2 | `POST` | `/api/compliance/check` | OWNER, ADMIN | Run compliance check | — |
| 3 | `GET` | `/api/compliance/reports/{id}` | OWNER, ADMIN | Get report | — |
| 4 | `GET` | `/api/compliance/status` | OWNER, ADMIN | Compliance status | — |
| 5 | `GET` | `/api/compliance/reports` | OWNER, ADMIN | Latest report | — |

---

### 25. ContractController ⭐ NEW
- **File:** `ContractController.java`
- **Base Path:** `/api`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/admin/contracts` | OWNER, ADMIN | Create contract | `CreateContractRequest` → `ContractResponse` |
| 2 | `GET` | `/api/admin/contracts` | OWNER, ADMIN | All contracts | → `List<ContractResponse>` |
| 3 | `GET` | `/api/admin/contracts/{id}` | OWNER, ADMIN | Contract details | → `ContractResponse` |
| 4 | `PUT` | `/api/admin/contracts/{id}` | OWNER, ADMIN | Update draft | `UpdateContractRequest` |
| 5 | `POST` | `/api/admin/contracts/{id}/send-signature` | OWNER, ADMIN | Send for e-signature | — |
| 6 | `PUT` | `/api/admin/contracts/{id}/terminate` | OWNER, ADMIN | Terminate contract | `TerminateContractRequest` |
| 7 | `GET` | `/api/admin/contracts/expiring` | OWNER, ADMIN | Expiring contracts | — |
| 8 | `POST` | `/api/contracts/{id}/sign` | Authenticated | Sign contract | `SignContractRequest` |
| 9 | `GET` | `/api/contracts/my` | Authenticated | My contracts | — |

---

### 26. ContractPricingController ⭐ NEW
- **File:** `ContractPricingController.java`
- **Base Path:** `/api`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/admin/contracts/{contractId}/pricing` | OWNER, ADMIN | Add pricing rule | `CreatePricingRuleRequest` → `PricingRuleResponse` |
| 2 | `GET` | `/api/admin/contracts/{contractId}/pricing` | OWNER, ADMIN | Get pricing rules | → `List<PricingRuleResponse>` |
| 3 | `PUT` | `/api/admin/contracts/pricing/{ruleId}` | OWNER, ADMIN | Update pricing rule | — |
| 4 | `GET` | `/api/pricing/calculate` | Authenticated | Calculate price | — |

---

### 27. ContractSlaController ⭐ NEW
- **File:** `ContractSlaController.java`
- **Base Path:** `/api/admin/contracts`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/admin/contracts/{contractId}/sla` | Class-level | Get SLA terms | → `SlaTermsResponse` |
| 2 | `PUT` | `/api/admin/contracts/{contractId}/sla` | Class-level | Update SLA | `SlaTermsRequest` |
| 3 | `GET` | `/api/admin/contracts/{contractId}/sla/compliance` | Class-level | SLA compliance report | — |
| 4 | `GET` | `/api/admin/contracts/{contractId}/sla/penalties` | Class-level | SLA penalties | — |

---

### 28. CountryController ⭐ NEW
- **File:** `CountryController.java`
- **Base Path:** None (method-level paths)
- **Class-Level Role:** None

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/countries` | Public/Authenticated | All active countries | — |
| 2 | `GET` | `/api/countries/{code}` | Public/Authenticated | Country by code | — |
| 3 | `POST` | `/api/admin/countries` | Admin | Create country | `CreateCountryRequest` |
| 4 | `PUT` | `/api/admin/countries/{code}` | Admin | Update country | — |
| 5 | `PATCH` | `/api/admin/countries/{code}/toggle` | Admin | Toggle active | — |

---

### 29. CourierLocationController ⭐ NEW
- **File:** `CourierLocationController.java`
- **Base Path:** `/api/couriers`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/couriers/location` | COURIER | Update location | body: lat/lng → `LocationDTO` |
| 2 | `GET` | `/api/couriers/{courierId}/location` | OWNER, ADMIN, COURIER | Last known location | — |
| 3 | `GET` | `/api/couriers/{courierId}/location/history` | OWNER, ADMIN, COURIER | Today's location history | — |

---

### 30. CurrencyController ⭐ NEW
- **File:** `CurrencyController.java`
- **Base Path:** None (method-level paths)
- **Class-Level Role:** None

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/currencies` | Public/Authenticated | All active currencies | — |
| 2 | `GET` | `/api/currencies/convert` | Public/Authenticated | Convert amount | — |
| 3 | `GET` | `/api/currencies/rate` | Public/Authenticated | Get exchange rate | — |
| 4 | `PUT` | `/api/admin/currencies/exchange-rate` | Admin | Update exchange rate | `UpdateExchangeRateRequest` |

---

### 31. DeadLetterController ⭐ NEW
- **File:** `DeadLetterController.java`
- **Base Path:** `/api/events/dead-letter`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/events/dead-letter` | OWNER, ADMIN | Unresolved dead letters | — |
| 2 | `POST` | `/api/events/dead-letter/{id}/retry` | OWNER, ADMIN | Retry failed event | — |
| 3 | `POST` | `/api/events/dead-letter/{id}/resolve` | OWNER, ADMIN | Resolve event | — |
| 4 | `GET` | `/api/events/dead-letter/stats` | OWNER, ADMIN | Dead letter stats | — |

---

### 32. DeliveryController ⭐ NEW
- **File:** `DeliveryController.java`
- **Base Path:** `/api/delivery`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/delivery/{shipmentId}/proof` | COURIER | Submit delivery proof (multipart) | → `DeliveryDTO.ProofResponse` |
| 2 | `GET` | `/api/delivery/{shipmentId}/proof` | Authenticated | Get delivery proof | — |
| 3 | `POST` | `/api/delivery/{shipmentId}/attempt` | COURIER | Record failed attempt | `RecordAttemptRequest` → `AttemptResponse` |
| 4 | `GET` | `/api/delivery/{shipmentId}/attempts` | Authenticated | Get delivery attempts | — |
| 5 | `GET` | `/api/delivery/admin/failures` | OWNER, ADMIN | Failure report | — |

---

### 33. DemandController ⭐ NEW
- **File:** `DemandController.java`
- **Base Path:** `/api/demand`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/demand/predict` | Class-level | Predict demand for zone | → `DemandPredictionResponse` |
| 2 | `GET` | `/api/demand/courier-need` | Class-level | Courier need prediction | — |
| 3 | `GET` | `/api/demand/patterns` | Class-level | Historical patterns | → `DemandPatternResponse` |

---

### 34. ECommerceController ⭐ NEW
- **File:** `ECommerceController.java`
- **Base Path:** `/api/integrations`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/integrations/connect` | MERCHANT, OWNER | Connect store | `ConnectStoreRequest` → `ConnectionResponse` |
| 2 | `GET` | `/api/integrations/connections` | MERCHANT, OWNER | My connections | — |
| 3 | `DELETE` | `/api/integrations/{id}` | MERCHANT, OWNER | Disconnect store | — |
| 4 | `GET` | `/api/integrations/{id}/orders` | MERCHANT, OWNER | Platform orders | — |
| 5 | `POST` | `/api/integrations/{id}/retry` | MERCHANT, OWNER | Retry failed orders | — |
| 6 | `GET` | `/api/integrations/{id}/stats` | MERCHANT, OWNER | Connection stats | — |

---

### 35. ECommerceWebhookController ⭐ NEW
- **File:** `ECommerceWebhookController.java`
- **Base Path:** `/api/ecommerce/webhook`
- **Class-Level Role:** permit-all (signature-verified)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/ecommerce/webhook/shopify/{connectionId}` | Public | Shopify webhook | — |
| 2 | `POST` | `/api/ecommerce/webhook/woocommerce/{connectionId}` | Public | WooCommerce webhook | — |
| 3 | `POST` | `/api/ecommerce/webhook/salla/{connectionId}` | Public | Salla webhook | — |
| 4 | `POST` | `/api/ecommerce/webhook/zid/{connectionId}` | Public | Zid webhook | — |

---

### 36. EInvoiceController ⭐ NEW
- **File:** `EInvoiceController.java`
- **Base Path:** `/api/admin/einvoice`
- **Class-Level Role:** None (implicit admin)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/admin/einvoice/generate/{invoiceId}` | Admin | Generate e-invoice | — |
| 2 | `GET` | `/api/admin/einvoice/{id}` | Admin | Get e-invoice | — |
| 3 | `POST` | `/api/admin/einvoice/{id}/submit` | Admin | Submit to government | — |
| 4 | `GET` | `/api/admin/einvoice/pending` | Admin | Pending e-invoices | — |

---

### 37. EventController ⭐ NEW
- **File:** `EventController.java`
- **Base Path:** `/api/events`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/events` | OWNER, ADMIN | List events | — |
| 2 | `GET` | `/api/events/{eventId}` | OWNER, ADMIN | Event details | — |
| 3 | `GET` | `/api/events/subscriptions` | OWNER, ADMIN | Active subscriptions | — |
| 4 | `POST` | `/api/events/subscriptions` | OWNER, ADMIN | Create subscription | `EventDTO.SubscriptionRequest` |
| 5 | `PUT` | `/api/events/subscriptions/{id}` | OWNER, ADMIN | Update subscription | — |

---

### 38. FleetController ⭐ NEW
- **File:** `FleetController.java`
- **Base Path:** `/api/fleet`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/fleet/vehicles` | OWNER, ADMIN | Add vehicle | `CreateVehicleRequest` → `VehicleResponse` |
| 2 | `GET` | `/api/fleet/vehicles` | OWNER, ADMIN | All vehicles | — |
| 3 | `GET` | `/api/fleet/vehicles/{id}` | OWNER, ADMIN | Vehicle details | — |
| 4 | `GET` | `/api/fleet/vehicles/available` | OWNER, ADMIN | Available vehicles | — |
| 5 | `PUT` | `/api/fleet/vehicles/{id}/retire` | OWNER, ADMIN | Retire vehicle | — |
| 6 | `POST` | `/api/fleet/assignments` | OWNER, ADMIN | Assign vehicle to courier | `AssignVehicleRequest` → `AssignmentResponse` |
| 7 | `PUT` | `/api/fleet/assignments/{id}/return` | OWNER, ADMIN, COURIER | Return vehicle | `ReturnVehicleRequest` |
| 8 | `POST` | `/api/fleet/maintenance` | OWNER, ADMIN | Schedule maintenance | `ScheduleMaintenanceRequest` → `MaintenanceResponse` |
| 9 | `PUT` | `/api/fleet/maintenance/{id}/complete` | OWNER, ADMIN | Complete maintenance | — |
| 10 | `GET` | `/api/fleet/vehicles/{vehicleId}/maintenance` | OWNER, ADMIN | Vehicle maintenance log | — |
| 11 | `POST` | `/api/fleet/fuel` | OWNER, ADMIN, COURIER | Add fuel log | `AddFuelLogRequest` → `FuelLogResponse` |
| 12 | `GET` | `/api/fleet/vehicles/{vehicleId}/fuel` | OWNER, ADMIN | Vehicle fuel log | — |

---

### 39. InvoiceController ⭐ NEW
- **File:** `InvoiceController.java`
- **Base Path:** `/api/invoices`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/invoices` | MERCHANT | My invoices (paginated) | → `Page<InvoiceResponse>` |
| 2 | `GET` | `/api/invoices/{id}` | MERCHANT, OWNER, ADMIN | Invoice details | — |
| 3 | `POST` | `/api/invoices/{id}/pay` | MERCHANT | Pay invoice | `PaymentRequest` |
| 4 | `GET` | `/api/invoices/admin` | OWNER, ADMIN | Invoices by status | — |
| 5 | `POST` | `/api/invoices/admin/{id}/refund` | OWNER | Refund invoice | — |

---

### 40. IpManagementController ⭐ NEW
- **File:** `IpManagementController.java`
- **Base Path:** `/api/security/ip-blacklist`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/security/ip-blacklist` | OWNER, ADMIN | IP blacklist | — |
| 2 | `POST` | `/api/security/ip-blacklist` | OWNER, ADMIN | Block IP | `SecurityDTO.IpBlockRequest` |
| 3 | `DELETE` | `/api/security/ip-blacklist/{id}` | OWNER, ADMIN | Unblock IP | — |

---

### 41. LabelController ⭐ NEW
- **File:** `LabelController.java`
- **Base Path:** `/api/shipments`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/shipments/{id}/label` | OWNER, ADMIN, MERCHANT, WAREHOUSE_MANAGER | Download label PDF | — |
| 2 | `POST` | `/api/shipments/labels/bulk` | OWNER, ADMIN, MERCHANT, WAREHOUSE_MANAGER | Bulk labels PDF | — |
| 3 | `GET` | `/api/shipments/{id}/barcode` | ALL roles | Generate barcode | — |
| 4 | `GET` | `/api/shipments/{id}/qrcode` | ALL roles | Generate QR code | — |
| 5 | `POST` | `/api/shipments/{id}/pod` | OWNER, ADMIN, COURIER | Upload POD image | `MultipartFile` |
| 6 | `GET` | `/api/shipments/{id}/pod` | ALL roles | Get POD | — |

---

### 42. LiveNotificationController ⭐ NEW
- **File:** `LiveNotificationController.java`
- **Base Path:** `/api/live-notifications`
- **Class-Level Role:** Authenticated

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/live-notifications/unread` | Authenticated | Unread notifications | — |
| 2 | `GET` | `/api/live-notifications/unread/count` | Authenticated | Unread count | — |
| 3 | `GET` | `/api/live-notifications` | Authenticated | All notifications | — |
| 4 | `POST` | `/api/live-notifications/{notificationId}/read` | Authenticated | Mark as read | — |
| 5 | `POST` | `/api/live-notifications/read-all` | Authenticated | Mark all as read | — |
| 6 | `GET` | `/api/live-notifications/presence/online` | Authenticated | Online users | — |
| 7 | `GET` | `/api/live-notifications/presence/{userId}` | Authenticated | User online status | — |

---

### 43. LiveTrackingController ⭐ NEW
- **File:** `LiveTrackingController.java`
- **Base Path:** `/api/tracking`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/tracking/sessions/start` | COURIER, OWNER, ADMIN | Start tracking session | `StartTrackingRequest` |
| 2 | `POST` | `/api/tracking/sessions/{sessionId}/pause` | COURIER, OWNER, ADMIN | Pause session | — |
| 3 | `POST` | `/api/tracking/sessions/{sessionId}/resume` | COURIER, OWNER, ADMIN | Resume session | — |
| 4 | `POST` | `/api/tracking/sessions/{sessionId}/end` | COURIER, OWNER, ADMIN | End session | — |
| 5 | `POST` | `/api/tracking/ping` | COURIER | Send GPS ping | `LocationPingRequest` |
| 6 | `GET` | `/api/tracking/sessions/shipment/{shipmentId}` | Authenticated | Active session for shipment | — |
| 7 | `GET` | `/api/tracking/sessions/courier` | COURIER, OWNER, ADMIN | Courier active sessions | — |
| 8 | `GET` | `/api/tracking/sessions/{sessionId}/pings` | Authenticated | Session pings | — |

---

### 44. NotificationPreferenceController ⭐ NEW
- **File:** `NotificationPreferenceController.java`
- **Base Path:** `/api/notifications`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/notifications/preferences` | Authenticated | Get preferences | → `PreferenceResponse` |
| 2 | `PUT` | `/api/notifications/preferences` | Authenticated | Update preferences | `PreferenceRequest` |
| 3 | `PUT` | `/api/notifications/preferences/pause` | Authenticated | Pause notifications | `PauseRequest` |
| 4 | `POST` | `/api/notifications/devices` | Authenticated | Register push device | `RegisterDeviceRequest` → `DeviceTokenResponse` |
| 5 | `DELETE` | `/api/notifications/devices/{token}` | Authenticated | Unregister device | — |

---

### 45. NotificationTemplateController ⭐ NEW
- **File:** `NotificationTemplateController.java`
- **Base Path:** `/api/admin/notifications`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/admin/notifications/templates` | Class-level | All templates | → `TemplateResponse` |
| 2 | `GET` | `/api/admin/notifications/templates/{eventType}` | Class-level | Templates by event | — |
| 3 | `PUT` | `/api/admin/notifications/templates/{id}` | Class-level | Update template | `UpdateTemplateRequest` |
| 4 | `POST` | `/api/admin/notifications/templates/{id}/test` | Class-level | Test notification | `TestNotificationRequest` |
| 5 | `GET` | `/api/admin/notifications/analytics` | Class-level | Delivery analytics | — |

---

### 46. PaymentCallbackController ⭐ NEW
- **File:** `PaymentCallbackController.java`
- **Base Path:** `/api/payments/callback`
- **Class-Level Role:** permit-all (webhook)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/payments/callback/paymob` | Public | Paymob callback | — |
| 2 | `POST` | `/api/payments/callback/tap` | Public | Tap Payments callback | — |
| 3 | `POST` | `/api/payments/callback/stripe` | Public | Stripe callback | — |
| 4 | `POST` | `/api/payments/callback/fawry` | Public | Fawry callback | — |

---

### 47. PaymentIntentController ⭐ NEW
- **File:** `PaymentIntentController.java`
- **Base Path:** `/api/payments`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/payments/intents` | MERCHANT, OWNER, ADMIN | Create payment intent | `CreatePaymentIntentRequest` |
| 2 | `GET` | `/api/payments/intents/{id}` | MERCHANT, OWNER, ADMIN | Get intent status | — |
| 3 | `POST` | `/api/payments/intents/{id}/confirm` | MERCHANT, OWNER, ADMIN | Confirm intent | — |
| 4 | `POST` | `/api/payments/intents/{id}/cancel` | MERCHANT, OWNER, ADMIN | Cancel intent | — |
| 5 | `GET` | `/api/payments/methods` | Authenticated | Saved payment methods | — |
| 6 | `POST` | `/api/payments/methods` | Authenticated | Add payment method | `AddPaymentMethodRequest` |
| 7 | `DELETE` | `/api/payments/methods/{id}` | Authenticated | Delete payment method | — |

---

### 48. PaymentRefundController ⭐ NEW
- **File:** `PaymentRefundController.java`
- **Base Path:** `/api/payments/refunds`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/payments/refunds` | MERCHANT, OWNER, ADMIN | Create refund | `CreateRefundRequest` |
| 2 | `GET` | `/api/payments/refunds/{id}` | MERCHANT, OWNER, ADMIN | Refund status | — |
| 3 | `GET` | `/api/payments/refunds` | OWNER, ADMIN | Pending refunds | — |
| 4 | `POST` | `/api/payments/refunds/{id}/approve` | OWNER, ADMIN | Approve refund | — |
| 5 | `POST` | `/api/payments/refunds/{id}/reject` | OWNER, ADMIN | Reject refund | — |

---

### 49. PickupScheduleController ⭐ NEW
- **File:** `PickupScheduleController.java`
- **Base Path:** `/api/pickups`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/pickups` | MERCHANT | Schedule pickup | `SchedulePickupRequest` → `PickupResponse` |
| 2 | `GET` | `/api/pickups/my` | MERCHANT | My pickups (paginated) | — |
| 3 | `GET` | `/api/pickups/today` | COURIER | Today's courier pickups | — |
| 4 | `PUT` | `/api/pickups/{id}/assign/{courierId}` | OWNER, ADMIN | Assign courier | — |
| 5 | `PUT` | `/api/pickups/{id}/start` | COURIER | Start pickup | — |
| 6 | `PUT` | `/api/pickups/{id}/complete` | COURIER | Complete pickup | — |
| 7 | `PUT` | `/api/pickups/{id}/cancel` | MERCHANT, OWNER, ADMIN | Cancel pickup | — |
| 8 | `GET` | `/api/pickups/admin` | OWNER, ADMIN | All pickups (paginated) | — |
| 9 | `GET` | `/api/pickups/admin/overdue` | OWNER, ADMIN | Overdue pickups | — |

---

### 50. PublicTrackingController ⭐ NEW
- **File:** `PublicTrackingController.java`
- **Base Path:** `/api/public/tracking`
- **Class-Level Role:** None (public)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/public/tracking/{trackingNumber}` | Public | Full tracking with timeline, courier location, ETA | → `TrackingResponseDTO` |
| 2 | `GET` | `/api/public/tracking/{trackingNumber}/eta` | Public | ETA only (lightweight) | — |

---

### 51. RatingController ⭐ NEW
- **File:** `RatingController.java`
- **Base Path:** `/api/ratings`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/ratings` | OWNER, ADMIN, MERCHANT | Submit courier rating | `CourierRatingRequest` → `CourierRatingDTO` |
| 2 | `GET` | `/api/ratings/courier/{courierId}` | OWNER, ADMIN | Courier ratings | — |
| 3 | `GET` | `/api/ratings/shipment/{shipmentId}` | OWNER, ADMIN, MERCHANT, COURIER | Shipment rating | — |

---

### 52. ReportExportController ⭐ NEW
- **File:** `ReportExportController.java`
- **Base Path:** `/api/reports/export`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/reports/export/{reportType}` | Class-level | Export report (PDF/Excel/CSV) | — |

---

### 53. ReturnController ⭐ NEW
- **File:** `ReturnController.java`
- **Base Path:** `/api/returns`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/returns` | OWNER, ADMIN, MERCHANT | Create return | `ReturnRequestDTO` → `ReturnResponseDTO` |
| 2 | `GET` | `/api/returns` | OWNER, ADMIN, MERCHANT, COURIER | List returns (role-filtered) | — |
| 3 | `GET` | `/api/returns/{id}` | ALL roles | Return details | — |
| 4 | `PUT` | `/api/returns/{id}/status` | OWNER, ADMIN, COURIER | Update return status | — |
| 5 | `PUT` | `/api/returns/{id}/assign` | OWNER, ADMIN | Assign courier | — |

---

### 54. RouteController ⭐ NEW
- **File:** `RouteController.java`
- **Base Path:** `/api/routes`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/routes/optimize/{courierId}` | Class-level | Optimize route | `OptimizeRouteRequest` → `OptimizedRouteResponse` |
| 2 | `GET` | `/api/routes/{courierId}` | Class-level | Current route | — |

---

### 55. ScheduledTaskController ⭐ NEW
- **File:** `ScheduledTaskController.java`
- **Base Path:** `/api/scheduled-tasks`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/scheduled-tasks` | OWNER, ADMIN | Create task | `ScheduledTaskDto` |
| 2 | `GET` | `/api/scheduled-tasks/{id}` | OWNER, ADMIN | Task details | — |
| 3 | `GET` | `/api/scheduled-tasks` | OWNER, ADMIN | List tasks by tenant | — |
| 4 | `PUT` | `/api/scheduled-tasks/{id}` | OWNER, ADMIN | Update task | `ScheduledTaskDto` |
| 5 | `PATCH` | `/api/scheduled-tasks/{id}/activate` | OWNER, ADMIN | Activate | — |
| 6 | `PATCH` | `/api/scheduled-tasks/{id}/deactivate` | OWNER, ADMIN | Deactivate | — |
| 7 | `DELETE` | `/api/scheduled-tasks/{id}` | OWNER, ADMIN | Delete | — |

---

### 56. SecurityEventController ⭐ NEW
- **File:** `SecurityEventController.java`
- **Base Path:** `/api/security`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/security/events` | OWNER, ADMIN | Security events | — |
| 2 | `GET` | `/api/security/events/summary` | OWNER, ADMIN | Event summary | — |
| 3 | `GET` | `/api/security/events/threats` | OWNER, ADMIN | Active threats | — |
| 4 | `GET` | `/api/security/lockouts` | OWNER, ADMIN | Locked accounts | — |
| 5 | `POST` | `/api/security/lockouts/{userId}/unlock` | OWNER, ADMIN | Unlock account | — |
| 6 | `GET` | `/api/security/audit` | OWNER, ADMIN | Security audit report | — |

---

### 57. SettlementController ⭐ NEW
- **File:** `SettlementController.java`
- **Base Path:** `/api/settlements`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/settlements` | OWNER, ADMIN | List settlement batches | → `List<SettlementBatch>` |
| 2 | `GET` | `/api/settlements/{id}` | OWNER, ADMIN | Settlement details | — |
| 3 | `POST` | `/api/settlements/generate` | OWNER, ADMIN | Generate manual settlement | `GenerateSettlementRequest` |
| 4 | `GET` | `/api/settlements/{id}/items` | OWNER, ADMIN | Settlement line items | → `List<SettlementItem>` |
| 5 | `POST` | `/api/settlements/{id}/process` | OWNER, ADMIN | Process/execute settlement | — |

---

### 58. SmartAssignmentController ⭐ NEW
- **File:** `SmartAssignmentController.java`
- **Base Path:** `/api/assignment`
- **Class-Level Role:** OWNER, ADMIN

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/assignment/suggest/{shipmentId}` | Class-level | Suggest best courier | → `SuggestionResponse` |
| 2 | `GET` | `/api/assignment/score/{shipmentId}` | Class-level | Score breakdown | → `List<ScoreBreakdownResponse>` |
| 3 | `GET` | `/api/assignment/rules` | Class-level | Get assignment rules | → `List<RuleResponse>` |
| 4 | `PUT` | `/api/assignment/rules` | Class-level | Update assignment rule | `UpdateRuleRequest` → `RuleResponse` |

---

### 59. SubscriptionController ⭐ NEW
- **File:** `SubscriptionController.java`
- **Base Path:** `/api/subscriptions`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/subscriptions/plans` | Public/Authenticated | All plans | → `List<PlanResponse>` |
| 2 | `POST` | `/api/subscriptions` | MERCHANT | Subscribe to plan | `SubscribeRequest` → `SubscriptionResponse` |
| 3 | `GET` | `/api/subscriptions/my` | MERCHANT | My subscription | → `SubscriptionResponse` |
| 4 | `PUT` | `/api/subscriptions/upgrade` | MERCHANT | Upgrade plan | `UpgradeRequest` → `SubscriptionResponse` |
| 5 | `PUT` | `/api/subscriptions/downgrade` | MERCHANT | Downgrade plan | `UpgradeRequest` → `SubscriptionResponse` |
| 6 | `PUT` | `/api/subscriptions/cancel` | MERCHANT | Cancel subscription | → `SubscriptionResponse` |
| 7 | `GET` | `/api/subscriptions/usage` | MERCHANT | Usage stats | → `UsageResponse` |

---

### 60. SupportController ⭐ NEW
- **File:** `SupportController.java`
- **Base Path:** `/api/support`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/support/tickets` | Authenticated | Create ticket | `CreateTicketRequest` → `TicketResponse` |
| 2 | `GET` | `/api/support/tickets/my` | Authenticated | My tickets (paginated) | — |
| 3 | `GET` | `/api/support/tickets/{id}` | Authenticated | Ticket details | → `TicketResponse` |
| 4 | `GET` | `/api/support/tickets/{id}/messages` | Authenticated | Ticket messages | → `List<MessageResponse>` |
| 5 | `POST` | `/api/support/tickets/{id}/messages` | Authenticated | Add message | `AddMessageRequest` |
| 6 | `PUT` | `/api/support/tickets/{id}/assign/{assigneeId}` | OWNER, ADMIN | Assign ticket | — |
| 7 | `PUT` | `/api/support/tickets/{id}/resolve` | OWNER, ADMIN | Resolve ticket | — |
| 8 | `PUT` | `/api/support/tickets/{id}/close` | OWNER, ADMIN | Close ticket | — |
| 9 | `GET` | `/api/support/tickets/admin` | OWNER, ADMIN | Tickets by status | — |
| 10 | `POST` | `/api/support/articles` | OWNER, ADMIN | Create KB article | `ArticleRequest` → `ArticleResponse` |
| 11 | `PUT` | `/api/support/articles/{id}/publish` | OWNER, ADMIN | Publish article | — |
| 12 | `GET` | `/api/support/articles` | Public/Authenticated | Published articles (paginated) | — |
| 13 | `GET` | `/api/support/articles/search` | Public/Authenticated | Search articles | — |
| 14 | `GET` | `/api/support/articles/{id}` | Public/Authenticated | View article | — |

---

### 61. TaxController ⭐ NEW
- **File:** `TaxController.java`
- **Base Path:** None (method-level paths)
- **Class-Level Role:** None

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/tax/calculate` | Authenticated | Calculate tax | params: amount, countryCode |
| 2 | `GET` | `/api/admin/tax/rules` | Admin | All tax rules | — |
| 3 | `GET` | `/api/admin/tax/rules/{countryCode}` | Admin | Tax rules by country | — |
| 4 | `POST` | `/api/admin/tax/rules` | Admin | Create tax rule | `CreateTaxRuleRequest` |
| 5 | `PUT` | `/api/admin/tax/rules/{id}` | Admin | Update tax rule | `CreateTaxRuleRequest` |

---

### 62. TenantBrandingController ⭐ NEW
- **File:** `TenantBrandingController.java`
- **Base Path:** None (method-level paths)
- **Class-Level Role:** None

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/tenants/{tenantId}/branding` | OWNER, ADMIN | Get branding | → `BrandingResponse` |
| 2 | `PUT` | `/api/tenants/{tenantId}/branding` | OWNER, ADMIN | Update branding | `BrandingRequest` → `BrandingResponse` |
| 3 | `POST` | `/api/tenants/{tenantId}/branding/logo` | OWNER, ADMIN | Upload logo | param: logoUrl |
| 4 | `GET` | `/api/public/branding/{slug}` | Public | Get tenant CSS theme | → text/css |

---

### 63. TenantController ⭐ NEW
- **File:** `TenantController.java`
- **Base Path:** `/api/tenants`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/tenants` | OWNER, ADMIN | Create tenant | `CreateTenantRequest` → `TenantResponse` |
| 2 | `GET` | `/api/tenants` | OWNER, ADMIN | All tenants | → `List<TenantSummaryResponse>` |
| 3 | `GET` | `/api/tenants/{id}` | OWNER, ADMIN | Tenant details | → `TenantResponse` |
| 4 | `PUT` | `/api/tenants/{id}` | OWNER, ADMIN | Update tenant | `UpdateTenantRequest` → `TenantResponse` |
| 5 | `POST` | `/api/tenants/{id}/suspend` | OWNER | Suspend tenant | — |
| 6 | `POST` | `/api/tenants/{id}/activate` | OWNER | Activate tenant | — |

---

### 64. TenantQuotaController ⭐ NEW
- **File:** `TenantQuotaController.java`
- **Base Path:** `/api/tenants/{tenantId}/quotas`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/tenants/{tenantId}/quotas` | OWNER, ADMIN | Get quotas | → `List<QuotaResponse>` |
| 2 | `PUT` | `/api/tenants/{tenantId}/quotas/{quotaType}` | OWNER | Update quota max | param: maxValue |
| 3 | `GET` | `/api/tenants/{tenantId}/quotas/usage` | OWNER, ADMIN | Quota usage stats | — |

---

### 65. TenantUserController ⭐ NEW
- **File:** `TenantUserController.java`
- **Base Path:** None (method-level paths)
- **Class-Level Role:** None

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/tenants/{tenantId}/users` | OWNER, ADMIN | Tenant users | → `List<TenantUserResponse>` |
| 2 | `POST` | `/api/tenants/{tenantId}/invitations` | OWNER, ADMIN | Send invitation | `InvitationRequest` → `InvitationResponse` |
| 3 | `GET` | `/api/tenants/{tenantId}/invitations` | OWNER, ADMIN | Tenant invitations | → `List<InvitationResponse>` |
| 4 | `POST` | `/api/invitations/{token}/accept` | Authenticated | Accept invitation | → `TenantUserResponse` |
| 5 | `PUT` | `/api/tenants/{tenantId}/users/{userId}/role` | OWNER, ADMIN | Change user role | `ChangeRoleRequest` |
| 6 | `DELETE` | `/api/tenants/{tenantId}/users/{userId}` | OWNER, ADMIN | Remove user from tenant | — |

---

### 66. WalletController ⭐ NEW
- **File:** `WalletController.java`
- **Base Path:** `/api/wallet`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/wallet` | Authenticated | My wallet info | → `WalletDTO` |
| 2 | `GET` | `/api/wallet/balance` | Authenticated | My balance | → `BigDecimal` |
| 3 | `GET` | `/api/wallet/transactions` | Authenticated | Transaction history (paginated) | → `List<TransactionDTO>` |
| 4 | `POST` | `/api/wallet/withdraw` | Authenticated | Request withdrawal | body: {amount} |
| 5 | `GET` | `/api/wallet/admin/all` | OWNER, ADMIN | All wallets | → `List<WalletDTO>` |

---

### 67. WebhookController ⭐ NEW
- **File:** `WebhookController.java`
- **Base Path:** `/api/webhooks`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/webhooks` | OWNER, ADMIN, MERCHANT | Create subscription | `CreateWebhookRequest` → `WebhookDTO` |
| 2 | `GET` | `/api/webhooks` | OWNER, ADMIN, MERCHANT | List my subscriptions | → `List<WebhookDTO>` |
| 3 | `GET` | `/api/webhooks/{id}` | OWNER, ADMIN, MERCHANT | Subscription details | → `WebhookDTO` |
| 4 | `DELETE` | `/api/webhooks/{id}` | OWNER, ADMIN, MERCHANT | Deactivate subscription | — |
| 5 | `GET` | `/api/webhooks/{id}/events` | OWNER, ADMIN, MERCHANT | Event log (paginated) | → `List<WebhookEventDTO>` |
| 6 | `POST` | `/api/webhooks/{id}/test` | OWNER, ADMIN, MERCHANT | Send test event | → `WebhookEventDTO` |
| 7 | `POST` | `/api/webhooks/retry` | OWNER, ADMIN | Retry all failed events | → `Integer` |

---

### 68. WorkflowDefinitionController ⭐ NEW
- **File:** `WorkflowDefinitionController.java`
- **Base Path:** `/api/workflows`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `POST` | `/api/workflows` | OWNER, ADMIN | Create workflow | `WorkflowDefinitionDto` |
| 2 | `GET` | `/api/workflows/{id}` | OWNER, ADMIN | Workflow details | — |
| 3 | `GET` | `/api/workflows` | OWNER, ADMIN | Workflows by tenant | — |
| 4 | `PUT` | `/api/workflows/{id}` | OWNER, ADMIN | Update workflow | `WorkflowDefinitionDto` |
| 5 | `PATCH` | `/api/workflows/{id}/activate` | OWNER, ADMIN | Activate | — |
| 6 | `PATCH` | `/api/workflows/{id}/deactivate` | OWNER, ADMIN | Deactivate | — |
| 7 | `DELETE` | `/api/workflows/{id}` | OWNER, ADMIN | Delete workflow | — |
| 8 | `GET` | `/api/workflows/{id}/steps` | OWNER, ADMIN | Workflow steps | — |

---

### 69. WorkflowExecutionController ⭐ NEW
- **File:** `WorkflowExecutionController.java`
- **Base Path:** `/api/workflow-executions`
- **Class-Level Role:** None (method-level)

| # | Method | Path | Roles | Description | DTOs |
|---|--------|------|-------|-------------|------|
| 1 | `GET` | `/api/workflow-executions/{id}` | OWNER, ADMIN | Execution details | — |
| 2 | `GET` | `/api/workflow-executions` | OWNER, ADMIN | Executions by status | — |
| 3 | `GET` | `/api/workflow-executions/by-definition/{definitionId}` | OWNER, ADMIN | Executions by definition | — |
| 4 | `PATCH` | `/api/workflow-executions/{id}/cancel` | OWNER, ADMIN | Cancel execution | — |
| 5 | `PATCH` | `/api/workflow-executions/{id}/pause` | OWNER, ADMIN | Pause execution | — |
| 6 | `GET` | `/api/workflow-executions/{id}/steps` | OWNER, ADMIN | Step executions | — |

---

## WebSocket Controller

### 70. WebSocketMessageController (STOMP — not REST)
- **File:** `WebSocketMessageController.java`
- **Annotation:** `@Controller` (not `@RestController`)
- **Protocol:** STOMP over WebSocket

| # | Destination | Description |
|---|-------------|-------------|
| 1 | `/app/tracking.ping` | GPS location ping from courier (sessionId, lat, lng, accuracy, speed, heading, batteryLevel) |
| 2 | `/app/chat.send` | Send chat message (roomId, senderId, content, messageType) |
| 3 | `/app/presence.connect` | User connected (userId) |
| 4 | `/app/presence.disconnect` | User disconnected (userId) |

---

## Role Reference

| Role | Endpoint Access Summary |
|------|------------------------|
| **OWNER** | Full access to all endpoints |
| **ADMIN** | Nearly full access (cannot suspend/activate tenants) |
| **MERCHANT** | Shipments (own), pickups, subscriptions, invoices, integrations, webhooks, contracts (sign), wallet |
| **COURIER** | Shipments (assigned), delivery proof, location, fleet (return vehicle + fuel), pickups (today), wallet |
| **WAREHOUSE_MANAGER** | Warehouse operations, labels, manifests (assign), reports (warehouse) |

---

## Notes

1. **DebugController** is referenced in existing docs but was **NOT found** in the codebase — likely removed.
2. **ShipmentController** is the largest (915 lines) with 14 endpoints including warehouse operations merged from a former WarehouseController.
3. **Multi-tenant architecture** is implemented via TenantController, TenantUserController, TenantQuotaController, TenantBrandingController, and TenantIsolationService.
4. **Payment providers supported:** Paymob, Tap Payments, Stripe, Fawry (via PaymentCallbackController).
5. **E-commerce platforms supported:** Shopify, WooCommerce, Salla, Zid (via ECommerceWebhookController).
6. All controllers use **Swagger/OpenAPI 3** annotations (`@Operation`, `@Tag`).
7. Arabic (ar) descriptions in `@Operation(summary=...)` throughout.
