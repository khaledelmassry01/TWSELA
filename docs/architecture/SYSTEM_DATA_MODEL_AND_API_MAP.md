# Twsela System — Comprehensive Data Model & API Surface Map

> **Generated from full source-code audit of `twsela/src/main/java/com/twsela/`**
> Covers: 26 domain entities, 14 controllers (~70 endpoints), 13 services, 26 repositories, 9 DTOs, security layer, and configuration.

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Data Model — All 26 Entities](#2-data-model--all-26-entities)
3. [Entity Relationship Diagram (Textual)](#3-entity-relationship-diagram-textual)
4. [API Surface — All Endpoints](#4-api-surface--all-endpoints)
5. [Business Logic Layer — Services](#5-business-logic-layer--services)
6. [Security & Authentication](#6-security--authentication)
7. [Roles, Permissions & Access Control](#7-roles-permissions--access-control)
8. [Shipment Lifecycle & Status Machine](#8-shipment-lifecycle--status-machine)
9. [Repository Layer — Custom Queries](#9-repository-layer--custom-queries)
10. [DTOs — Request Objects](#10-dtos--request-objects)
11. [Configuration & Infrastructure](#11-configuration--infrastructure)
12. [Appendix: File Index](#12-appendix-file-index)

---

## 1. Architecture Overview

| Layer | Package | Count | Purpose |
|-------|---------|-------|---------|
| Domain Entities | `com.twsela.domain` | 26 | JPA entities & enums |
| Controllers | `com.twsela.web` | 14 | REST API endpoints |
| DTOs | `com.twsela.web.dto` | 9 | Request/response objects |
| Services | `com.twsela.service` | 13 | Business logic |
| Repositories | `com.twsela.repository` | 26 | Data access (Spring Data JPA) |
| Security | `com.twsela.security` | 5 | JWT, RBAC, permissions |
| Config | `com.twsela.config` | 4 | App config, caching, seeding |

**Tech Stack**: Spring Boot 3 · Jakarta Persistence (JPA) · MySQL 8 · JWT (HS256) · Redis (optional cache) · Twilio SMS · iText PDF · Apache POI Excel · Micrometer/Prometheus · Swagger/OpenAPI 3

---

## 2. Data Model — All 26 Entities

### 2.1 User (Core)

**Table**: `users`

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | `Long` | PK, auto-generated | |
| `name` | `String` | `@NotBlank`, 2-100 chars, pattern: `^[\\p{L}\\s]+$` (Arabic+Latin) | |
| `phone` | `String` | `@NotBlank`, unique, pattern: `^[0-9]{10,15}$` | Primary login identifier |
| `password` | `String` | `@JsonIgnore`, min 6 chars | BCrypt hashed |
| `role` | `Role` | `@ManyToOne`, `@NotNull` | FK → `roles` |
| `status` | `UserStatus` | `@ManyToOne`, `@NotNull` | FK → `user_statuses` |
| `isDeleted` | `Boolean` | default `false` | Soft-delete flag |
| `deletedAt` | `Instant` | nullable | |
| `createdAt` | `Instant` | auto-set | |
| `updatedAt` | `Instant` | auto-set | |

**Relationships**:
- `@OneToOne(mappedBy)` → `MerchantDetails`
- `@OneToOne(mappedBy)` → `CourierDetails`
- `@OneToMany(mappedBy="merchant")` → `Shipment` (createdShipments)
- `@OneToMany(mappedBy="user")` → `Payout`

**Helper**: `isActive()` → `status.name == "ACTIVE" && !isDeleted`

---

### 2.2 Shipment (Core)

**Table**: `shipments`

| Field | Type | Constraints | Notes |
|-------|------|-------------|-------|
| `id` | `Long` | PK | |
| `trackingNumber` | `String` | unique | Format: `TWS-XXXXXXXX` |
| `merchant` | `User` | `@ManyToOne` | FK → `users` |
| `manifest` | `ShipmentManifest` | `@ManyToOne` | FK → `shipment_manifests` |
| `zone` | `Zone` | `@ManyToOne` | FK → `zones` |
| `status` | `ShipmentStatus` | `@ManyToOne` | FK → `shipment_statuses` |
| `recipientDetails` | `RecipientDetails` | `@ManyToOne` | FK → `recipient_details` |
| `deliveryLatitude` | `BigDecimal` | nullable | GPS coord |
| `deliveryLongitude` | `BigDecimal` | nullable | GPS coord |
| `itemValue` | `BigDecimal` | `@NotNull`, ≥ 0 | |
| `shippingFeePaidBy` | `Enum` | `MERCHANT` / `RECIPIENT` / `PREPAID` | |
| `codAmount` | `BigDecimal` | `@NotNull`, ≥ 0 | Cash on delivery |
| `deliveryFee` | `BigDecimal` | `@NotNull`, ≥ 0 | Calculated |
| `sourceType` | `Enum` | `MERCHANT` / `THIRD_PARTY_LOGISTICS_PARTNER` | |
| `externalTrackingNumber` | `String` | nullable | For 3PL |
| `cashReconciled` | `Boolean` | default `false` | |
| `payout` | `Payout` | `@ManyToOne` | FK → `payouts` |
| `podType` | `Enum` | `OTP` / `PHOTO` / `SIGNATURE` | Proof of delivery |
| `podData` | `String` | nullable | POD content |
| `recipientNotes` | `String` | `@Column(TEXT)` | |
| `createdAt` | `Instant` | | |
| `updatedAt` | `Instant` | | |
| `deliveredAt` | `Instant` | nullable | |

**Relationships**:
- `@OneToMany(mappedBy)` → `ShipmentStatusHistory`
- `@OneToOne(mappedBy)` → `ShipmentPackageDetails`

**Helper**: `getCourier()` → `manifest.getCourier()`
**Transient helpers**: `getRecipientName()`, `getRecipientPhone()`, `getRecipientAddress()` delegate to `recipientDetails`

---

### 2.3 Role (Lookup)

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | `Long` | PK |
| `name` | `String` | unique, max 50 |
| `description` | `String` | |

**Seed values**: `OWNER`, `ADMIN`, `MERCHANT`, `COURIER`, `WAREHOUSE_MANAGER`

---

### 2.4 UserStatus (Lookup)

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | `Long` | PK |
| `name` | `String` | unique, max 50 |

**Seed values**: `ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING_VERIFICATION`

---

### 2.5 ShipmentStatus (Lookup)

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | `Long` | PK |
| `name` | `String` | unique, max 50 |
| `description` | `String` | |

**DataInitializer seed**: `PENDING`, `PROCESSING`, `OUT_FOR_DELIVERY`, `DELIVERED`, `FAILED_DELIVERY`, `RETURNED`, `CANCELLED`, `ON_HOLD`

**Additional statuses used in code** (created at runtime or via SQL): `PENDING_APPROVAL`, `APPROVED`, `RECEIVED_AT_HUB`, `READY_FOR_DISPATCH`, `ASSIGNED_TO_COURIER`, `FAILED_ATTEMPT`, `POSTPONED`, `PENDING_UPDATE`, `PENDING_RETURN`, `RETURNED_TO_HUB`, `RETURNED_TO_ORIGIN`

---

### 2.6 Zone

**Table**: `zones`

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | `Long` | PK |
| `name` | `String` | unique |
| `description` | `String` | |
| `centerLatitude` | `BigDecimal` | |
| `centerLongitude` | `BigDecimal` | |
| `defaultFee` | `BigDecimal` | |
| `status` | `ZoneStatus` (enum) | `ZONE_ACTIVE` / `ZONE_INACTIVE` / `ZONE_MAINTENANCE` |
| `createdAt` | `Instant` | |

**Seed zones**: `CAIRO`, `GIZA`, `ALEXANDRIA`, `SHARQIA`, `DAKAHLEIA` (default fee: 50.00 EGP)

---

### 2.7 ShipmentManifest

**Table**: `shipment_manifests`

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | `Long` | PK |
| `courier` | `User` | `@ManyToOne` → FK `users` |
| `manifestNumber` | `String` | unique |
| `status` | `Enum` | `CREATED` / `IN_PROGRESS` / `COMPLETED` / `CANCELLED` |
| `createdAt` | `Instant` | |
| `assignedAt` | `Instant` | |

**Relationships**: `@OneToMany(mappedBy)` → `Shipment`

---

### 2.8 ShipmentPackageDetails

**Table**: shares PK with `Shipment` (`@MapsId`)

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | = `shipment.id` |
| `packageType` | `String` | |
| `weightKg` | `BigDecimal` | |
| `lengthCm` | `BigDecimal` | |
| `widthCm` | `BigDecimal` | |
| `heightCm` | `BigDecimal` | |

---

### 2.9 ShipmentStatusHistory

**Table**: `shipment_status_history`

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | PK |
| `shipment` | `Shipment` | `@ManyToOne` |
| `status` | `ShipmentStatus` | `@ManyToOne` |
| `notes` | `String` | `@Column(TEXT)` |
| `createdAt` | `Instant` | |

---

### 2.10 RecipientDetails

**Table**: `recipient_details` (deduplicated by phone)

| Field | Type | Constraints |
|-------|------|-------------|
| `id` | `Long` | PK |
| `phone` | `String` | `@NotBlank`, unique, 10-15 digits |
| `name` | `String` | |
| `address` | `String` | |
| `alternatePhone` | `String` | |

**Relationships**: `@OneToMany(mappedBy)` → `Shipment`

---

### 2.11 Payout

**Table**: `payouts`

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | PK |
| `user` | `User` | `@ManyToOne` |
| `payoutType` | `Enum` | `COURIER_SETTLEMENT` / `MERCHANT_PAYOUT` / `WAREHOUSE_SETTLEMENT` |
| `status` | `PayoutStatus` | `@ManyToOne` |
| `payoutPeriodStart` | `LocalDate` | |
| `payoutPeriodEnd` | `LocalDate` | |
| `netAmount` | `BigDecimal` | |
| `paidAt` | `Instant` | |
| `description` | `String` | |
| `notes` | `String` | |
| `createdAt` | `Instant` | |

**Relationships**: `@OneToMany` → `PayoutItem`, `@OneToMany` → `Shipment`

---

### 2.12 PayoutItem

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | PK |
| `payout` | `Payout` | `@ManyToOne` |
| `sourceType` | `Enum` | `ADJUSTMENT` / `BONUS` / `EXPENSE` / `SHIPMENT` |
| `sourceId` | `Long` | |
| `amount` | `BigDecimal` | |
| `description` | `String` | |
| `createdAt` | `Instant` | |

---

### 2.13 PayoutStatus (Lookup)

| Field | Type |
|-------|------|
| `id` | `Long` |
| `name` | `String` (unique) |
| `description` | `String` |

**Seed values**: `PENDING`, `PROCESSED`, `FAILED`, `CANCELLED`

---

### 2.14 MerchantDetails

Shares PK with `User` (`@MapsId`)

| Field | Type | Notes |
|-------|------|-------|
| `businessName` | `String` | |
| `pickupAddress` | `String` | |
| `bankAccountDetails` | `String` | `@JsonIgnore` |

---

### 2.15 CourierDetails

Shares PK with `User` (`@MapsId`)

| Field | Type | Notes |
|-------|------|-------|
| `vehicleType` | `Enum` | `BICYCLE` / `CAR` / `MOTORCYCLE` / `VAN` |
| `licensePlateNumber` | `String` | |
| `onboardingDate` | `LocalDate` | |

---

### 2.16 DeliveryPricing

**Unique constraint**: `(merchant_id, zone_id)`

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | PK |
| `merchant` | `User` | `@ManyToOne` |
| `zone` | `Zone` | `@ManyToOne` |
| `deliveryFee` | `BigDecimal` | |
| `isActive` | `Boolean` | |
| `createdAt` | `Instant` | |
| `updatedAt` | `Instant` | |

---

### 2.17 CourierZone

Composite PK: `@EmbeddedId(courierId, zoneId)`

| Field | Type |
|-------|------|
| `courier` | `User` (`@ManyToOne`) |
| `zone` | `Zone` (`@ManyToOne`) |

---

### 2.18 ReturnShipment

| Field | Type |
|-------|------|
| `id` | `Long` |
| `originalShipment` | `Shipment` (`@ManyToOne`) |
| `returnShipment` | `Shipment` (`@ManyToOne`) |
| `reason` | `String` |
| `createdAt` | `Instant` |
| `createdBy` | `String` |

---

### 2.19 CashMovementLedger

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | PK |
| `user` | `User` | `@ManyToOne` |
| `transactionType` | `Enum` | `COLLECTION` / `DEPOSIT_TO_WAREHOUSE` / `DEPOSIT_TO_BANK` / `WITHDRAWAL` |
| `amount` | `BigDecimal` | |
| `shipmentId` | `Long` | |
| `description` | `String` | |
| `status` | `Enum` | `PENDING` / `VERIFIED` / `RECONCILED` |
| `createdAt` | `Instant` | |
| `reconciledAt` | `Instant` | |
| `immutable` | `Boolean` | default `true` |

---

### 2.20 SystemAuditLog

| Field | Type |
|-------|------|
| `id` | `Long` |
| `userId` | `Long` |
| `actionType` | `String` |
| `entityType` | `String` |
| `entityId` | `Long` |
| `oldValues` | `String` (TEXT) |
| `newValues` | `String` (TEXT) |
| `ipAddress` | `String` |
| `userAgent` | `String` |
| `createdAt` | `Instant` |

---

### 2.21 NotificationLog

| Field | Type |
|-------|------|
| `id` | `Long` |
| `recipientPhone` | `String` |
| `messageType` | `String` |
| `messageContent` | `String` (TEXT) |
| `sentAt` | `Instant` |
| `status` | `String` |
| `errorMessage` | `String` |

---

### 2.22 CourierLocationHistory

| Field | Type |
|-------|------|
| `id` | `Long` |
| `courier` | `User` (`@ManyToOne`) |
| `latitude` | `BigDecimal` |
| `longitude` | `BigDecimal` |
| `timestamp` | `Instant` |

---

### 2.23 ServiceFeedback

`@OneToOne` → `Shipment` (unique constraint on `shipment_id`)

| Field | Type |
|-------|------|
| `id` | `Long` |
| `shipment` | `Shipment` |
| `rating` | `short` |
| `comment` | `String` |
| `createdAt` | `Instant` |

---

### 2.24 MerchantServiceFeedback

| Field | Type | Notes |
|-------|------|-------|
| `id` | `Long` | |
| `merchant` | `User` | `@ManyToOne` |
| `courier` | `User` | `@ManyToOne` |
| `relatedShipment` | `Shipment` | `@ManyToOne` |
| `rating` | `Integer` | 1-5 |
| `comment` | `String` | |
| `createdAt` | `Instant` | |

---

### 2.25 FraudBlacklist

| Field | Type |
|-------|------|
| `id` | `Long` |
| `entityType` | `String` |
| `entityValue` | `String` |
| `reason` | `String` |
| `createdAt` | `Instant` |
| `createdBy` | `String` |
| `isActive` | `Boolean` |

---

### 2.26 Warehouse & WarehouseInventory

**Warehouse**:

| Field | Type |
|-------|------|
| `id` | `Long` |
| `name` | `String` (`@NotBlank`, max 100) |
| `address` | `String` (`@NotBlank`) |
| `latitude` | `BigDecimal` |
| `longitude` | `BigDecimal` |

`@OneToMany(mappedBy)` → `WarehouseInventory`

**WarehouseInventory** (`@MapsId` on Shipment — shared PK):

| Field | Type |
|-------|------|
| `shipment` | `Shipment` (`@OneToOne`) |
| `warehouse` | `Warehouse` (`@ManyToOne`) |
| `receivedBy` | `User` (`@ManyToOne`) |
| `receivedAt` | `Instant` |
| `dispatchedAt` | `Instant` |
| `status` | `Enum`: `RECEIVED` / `IN_STORAGE` / `DISPATCHED` / `PICKED_UP` |

---

### 2.27 TelemetrySettings (Key-Value Config)

| Field | Type |
|-------|------|
| `id` | `Long` |
| `settingKey` | `String` (unique) |
| `settingValue` | `String` |
| `description` | `String` |
| `createdAt` | `Instant` |
| `updatedAt` | `Instant` |

**Seed settings**: `DEFAULT_SYSTEM_FEE=50.00`, `MAX_WEIGHT_PER_SHIPMENT=50.0`, `DEFAULT_PRIORITY=STANDARD`, `SYSTEM_NAME=Twsela`, `SUPPORT_PHONE=01023782584`, `SMS_ENABLED=true`, `EMAIL_ENABLED=false`, `AUTO_ASSIGN_COURIER=false`

---

## 3. Entity Relationship Diagram (Textual)

```
User ──┬── 1:1 ── MerchantDetails
       ├── 1:1 ── CourierDetails
       ├── 1:N ── Shipment (as merchant)
       ├── 1:N ── Payout
       ├── 1:N ── CashMovementLedger
       ├── 1:N ── CourierLocationHistory
       ├── 1:N ── MerchantServiceFeedback (as merchant OR courier)
       ├── N:1 ── Role
       └── N:1 ── UserStatus

Shipment ──┬── N:1 ── User (merchant)
           ├── N:1 ── ShipmentManifest ── N:1 ── User (courier)
           ├── N:1 ── Zone
           ├── N:1 ── ShipmentStatus
           ├── N:1 ── RecipientDetails
           ├── N:1 ── Payout
           ├── 1:1 ── ShipmentPackageDetails
           ├── 1:1 ── ServiceFeedback
           ├── 1:1 ── WarehouseInventory ── N:1 ── Warehouse
           ├── 1:N ── ShipmentStatusHistory ── N:1 ── ShipmentStatus
           └── 1:N ── ReturnShipment (original/return links)

Zone ──┬── 1:N ── DeliveryPricing ── N:1 ── User (merchant)
       └── N:M ── User (courier) via CourierZone

Payout ──┬── 1:N ── PayoutItem
         ├── N:1 ── PayoutStatus
         └── N:1 ── User
```

---

## 4. API Surface — All Endpoints

### 4.1 AuthController — `/api/auth`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `POST` | `/api/auth/login` | None | Any | Login → JWT token + user info |
| `GET` | `/api/auth/me` | JWT | Any authenticated | Get current user profile |
| `GET` | `/api/auth/health` | None | Any | Auth service health check |

---

### 4.2 ShipmentController — `/api/shipments`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `GET` | `/api/shipments` | JWT | OWNER, ADMIN, MERCHANT | List shipments (paginated, sorted). MERCHANT sees own only |
| `GET` | `/api/shipments/{id}` | JWT | OWNER, ADMIN, MERCHANT, COURIER, WAREHOUSE_MANAGER | Get shipment by ID |
| `GET` | `/api/shipments/count` | JWT | OWNER, ADMIN, MERCHANT, COURIER, WAREHOUSE_MANAGER | Count shipments |
| `POST` | `/api/shipments` | JWT | OWNER, ADMIN, MERCHANT | Create shipment (via CreateShipmentRequest DTO) |
| `POST` | `/api/shipments/warehouse/receive` | JWT | WAREHOUSE_MANAGER, OWNER | Receive shipments at hub by tracking numbers |
| `GET` | `/api/shipments/warehouse/inventory` | JWT | WAREHOUSE_MANAGER, OWNER | View warehouse inventory (RECEIVED_AT_HUB/RETURNED_TO_HUB) |
| `POST` | `/api/shipments/warehouse/dispatch/{courierId}` | JWT | WAREHOUSE_MANAGER, OWNER | Dispatch shipments to courier (creates manifest) |
| `POST` | `/api/shipments/warehouse/reconcile/courier/{courierId}` | JWT | WAREHOUSE_MANAGER, OWNER | End-of-day cash reconciliation |
| `GET` | `/api/shipments/warehouse/couriers` | JWT | WAREHOUSE_MANAGER, OWNER | List active couriers for dispatch |
| `GET` | `/api/shipments/warehouse/courier/{courierId}/shipments` | JWT | WAREHOUSE_MANAGER, OWNER | Get courier's shipments for reconciliation |
| `GET` | `/api/shipments/warehouse/stats` | JWT | WAREHOUSE_MANAGER, OWNER | Warehouse statistics |
| `POST` | `/api/shipments/{id}/return-request` | JWT | All authenticated | Request return to origin |
| `PUT` | `/api/shipments/courier/location/update` | JWT | COURIER | Update courier GPS location |
| `GET` | `/api/shipments/list` | JWT | OWNER, ADMIN, MERCHANT, COURIER | Alternative list (stub — returns empty) |

---

### 4.3 UserController — `/api`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `GET` | `/api/users` | JWT | OWNER, ADMIN | List all users |
| `POST` | `/api/users` | JWT | OWNER, ADMIN | Create user |
| `PUT` | `/api/users/{id}` | JWT | OWNER, ADMIN | Update user |
| `DELETE` | `/api/users/{id}` | JWT | OWNER, ADMIN | Delete user |
| `GET` | `/api/couriers` | JWT | OWNER, ADMIN | List couriers (paginated) |
| `GET` | `/api/merchants` | JWT | OWNER, ADMIN | List merchants (paginated) |
| `GET` | `/api/employees` | JWT | OWNER, ADMIN | List employees (paginated) |
| `POST` | `/api/employees` | JWT | OWNER, ADMIN | Create employee |

---

### 4.4 ManifestController — `/api/manifests`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `GET` | `/api/manifests` | JWT | OWNER, ADMIN, COURIER | List manifests (COURIER sees own) |
| `POST` | `/api/manifests` | JWT | OWNER, ADMIN | Create manifest |
| `GET` | `/api/manifests/{manifestId}` | JWT | OWNER, ADMIN, COURIER | Get manifest by ID |
| `POST` | `/api/manifests/{manifestId}/shipments` | JWT | OWNER, ADMIN, COURIER | Assign shipments by IDs (TODO) |
| `PUT` | `/api/manifests/{manifestId}/status` | JWT | OWNER, ADMIN, COURIER | Update manifest status |
| `POST` | `/api/manifests/{manifestId}/assign` | JWT | OWNER, ADMIN, WAREHOUSE_MANAGER | Assign shipments by tracking numbers |

---

### 4.5 DashboardController — `/api/dashboard`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `GET` | `/api/dashboard/summary` | JWT | All authenticated | Role-based dashboard summary |
| `GET` | `/api/dashboard/statistics` | JWT | All authenticated | General statistics |

---

### 4.6 FinancialController — `/api/financial`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `GET` | `/api/financial/payouts` | JWT | OWNER, ADMIN, MERCHANT, COURIER | List payouts (own for MERCHANT/COURIER) |
| `POST` | `/api/financial/payouts` | JWT | OWNER, ADMIN | Create payout |
| `GET` | `/api/financial/payouts/{payoutId}` | JWT | OWNER, ADMIN, MERCHANT, COURIER | Get payout by ID |
| `PUT` | `/api/financial/payouts/{payoutId}/status` | JWT | OWNER, ADMIN | Update payout status |
| `GET` | `/api/financial/payouts/pending` | JWT | OWNER, ADMIN | List pending payouts |
| `GET` | `/api/financial/payouts/user/{userId}` | JWT | OWNER, ADMIN, MERCHANT, COURIER | Get payouts for user |
| `GET` | `/api/financial/payouts/{payoutId}/items` | JWT | OWNER, ADMIN, MERCHANT, COURIER | Get payout line items |

---

### 4.7 ReportsController — `/api/reports`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `GET` | `/api/reports/shipments?startDate&endDate` | JWT | OWNER, ADMIN, MERCHANT, COURIER | Shipment report (role-filtered) |
| `GET` | `/api/reports/couriers?startDate&endDate` | JWT | OWNER, ADMIN | Courier performance report |
| `GET` | `/api/reports/merchants?startDate&endDate` | JWT | OWNER, ADMIN | Merchant report |
| `GET` | `/api/reports/warehouse?startDate&endDate` | JWT | OWNER, ADMIN, WAREHOUSE_MANAGER | Warehouse report |

---

### 4.8 MasterDataController — `/api/master`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `GET` | `/api/master/users` | JWT | OWNER, ADMIN | List users |
| `POST` | `/api/master/users` | JWT | OWNER | Create user |
| `PUT` | `/api/master/users/{id}` | JWT | OWNER | Update user |
| `DELETE` | `/api/master/users/{id}` | JWT | OWNER | Delete user |
| `GET` | `/api/master/zones` | JWT | OWNER, ADMIN | List zones |
| `POST` | `/api/master/zones` | JWT | OWNER | Create zone |
| `PUT` | `/api/master/zones/{id}` | JWT | OWNER | Update zone |
| `DELETE` | `/api/master/zones/{id}` | JWT | OWNER | Delete zone |
| `GET` | `/api/master/pricing` | JWT | OWNER, ADMIN | List delivery pricing |
| `POST` | `/api/master/pricing` | JWT | OWNER | Create pricing |
| `PUT` | `/api/master/pricing/{id}` | JWT | OWNER | Update pricing |
| `DELETE` | `/api/master/pricing/{id}` | JWT | OWNER | Delete pricing |
| `GET` | `/api/master/telemetry` | JWT | OWNER | Get telemetry settings |
| `PUT` | `/api/master/telemetry` | JWT | OWNER | Update telemetry settings |

---

### 4.9 SmsController — `/api/sms`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `POST` | `/api/sms/send` | JWT | OWNER, ADMIN | Send SMS |
| `POST` | `/api/sms/send-otp` | JWT | OWNER, ADMIN | Send OTP via SMS |
| `POST` | `/api/sms/send-notification` | JWT | OWNER, ADMIN | Send shipment notification (type: created/picked_up/in_transit/delivered/delayed) |
| `GET` | `/api/sms/test` | JWT | OWNER, ADMIN | Test SMS service |

---

### 4.10 SettingsController — `/api/settings`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `GET` | `/api/settings` | JWT | All authenticated | Get settings (hardcoded: language=ar, timezone=Asia/Riyadh, currency=EGP) |
| `POST` | `/api/settings` | JWT | All authenticated | Save settings (stub — no persistence) |
| `POST` | `/api/settings/reset` | JWT | All authenticated | Reset to defaults (stub) |

---

### 4.11 HealthController — `/api/health`

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| `GET` | `/api/health` | None | Application health check |

---

### 4.12 DebugController — `/api/debug` (DEVELOPMENT ONLY)

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| `GET` | `/api/debug/generate-hash` | None | Generate BCrypt hash |
| `POST` | `/api/debug/test-password` | None | Test password match |
| `POST` | `/api/debug/reset-test-passwords` | None | Reset test user passwords |

**Test accounts** (from DebugController):
| Phone | Role |
|-------|------|
| `01023782584` | OWNER |
| `01023782585` | MERCHANT |
| `01023782586` | COURIER |
| `01023782588` | WAREHOUSE_MANAGER |
| `01126538767` | ADMIN |

---

### 4.13 BackupController — `/api/backup`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `POST` | `/api/backup/create` | JWT | OWNER, ADMIN | Create database backup |
| `POST` | `/api/backup/restore` | JWT | OWNER, ADMIN | Restore from backup |
| `GET` | `/api/backup/status` | JWT | OWNER, ADMIN | Get backup status |
| `GET` | `/api/backup/test` | JWT | OWNER, ADMIN | Test backup service |

---

### 4.14 AuditController — `/api/audit`

| Method | Path | Auth | Roles | Purpose |
|--------|------|------|-------|---------|
| `GET` | `/api/audit/logs` | JWT | OWNER, ADMIN | Get audit logs (filter: date, action, userId) |
| `GET` | `/api/audit/entity/{entityType}/{entityId}` | JWT | OWNER, ADMIN | Get entity audit logs |
| `GET` | `/api/audit/user/{userId}` | JWT | OWNER, ADMIN | Get user audit logs |

---

### 4.15 PublicController — `/api/public`

| Method | Path | Auth | Purpose |
|--------|------|------|---------|
| `GET` | `/api/public/track/{trackingNumber}` | None | Public shipment tracking |
| `POST` | `/api/public/feedback/{trackingNumber}` | None | Submit service feedback |
| `POST` | `/api/public/forgot-password` | None | Forgot password → generates random password, sends via SMS |
| `POST` | `/api/public/send-otp` | None | Send OTP for password reset |
| `POST` | `/api/public/reset-password` | None | Reset password with OTP verification |
| `POST` | `/api/public/contact` | None | Contact form submission (stub) |
| `GET` | `/api/public/contact/offices` | None | Get office locations (Cairo, Alexandria, Giza) |

---

## 5. Business Logic Layer — Services

### 5.1 ShipmentService (642 lines)

| Method | Purpose |
|--------|---------|
| `getAllShipments(Pageable)` | Paginated shipment list |
| `getShipmentById(id)` | Single shipment fetch |
| `getShipmentByTrackingNumber(tn)` | Lookup by tracking number |
| `createShipment(merchantId, shipment)` | Create with tracking number generation, fee calculation |
| `createShipmentFromExcel(merchantId, shipment)` | Bulk creation from Excel import |
| `updateShipment(id, updates)` | Update shipment fields |
| `updateShipmentStatus(id, statusName, notes)` | Change status + create history record |
| `deleteShipment(id)` | Hard delete |
| `getShipmentsByMerchant(merchantId, pageable)` | Merchant's shipments |
| `getShipmentsByCourier(courierId, pageable)` | Courier's shipments via manifest |
| `getUnassignedShipments()` | Shipments without manifest |
| `createReturnShipment(original, reason)` | Full RTO process |
| `updateCourierLocation(courierId, lat, lng)` | GPS tracking |
| `getAllStatuses()` / `createStatus()` / `updateStatus()` / `deleteStatus()` | Shipment status CRUD |

**Fee Calculation Hierarchy**:
1. `DeliveryPricing` for merchant+zone → if exists, use it
2. `Zone.defaultFee` → if exists, use it
3. TelemetrySettings `DEFAULT_SYSTEM_FEE` → if exists, use it
4. Hardcoded `50.00` EGP fallback

**Tracking Number Format**: `TWS-` + 8 random uppercase alphanumeric chars

**Failed Delivery Status Mapping** (`determineSpecificFailedStatus(reason)`):
- `postponed` / `not_available` → `POSTPONED`
- `wrong_address` / `wrong_phone` → `PENDING_UPDATE`
- `rejected` / `refused` → `PENDING_RETURN`
- default → `FAILED_ATTEMPT`

---

### 5.2 UserService

| Method | Purpose |
|--------|---------|
| `getAllUsers()` | List all users |
| `getUserById(id)` | Single user |
| `getUserByPhone(phone)` | Lookup by phone |
| `createUser(user)` | Create with BCrypt password, phone uniqueness check |
| `createMerchant(user, merchantDetails)` | Create user + MerchantDetails |
| `updateUser(id, updates)` | Update (name, phone, role, status) |
| `deleteUser(id)` | **Hard delete** (not soft-delete) |
| Roles CRUD | `getAllRoles()`, `createRole()`, `updateRole()`, `deleteRole()` |

---

### 5.3 FinancialService

| Method | Purpose |
|--------|---------|
| `createCourierPayout(courierId, start, end)` | Calculate courier settlement: **70% of delivery fees** for delivered shipments |
| `createMerchantPayout(merchantId, start, end)` | Calculate merchant payout: **100% of delivery fees** returned to merchant |
| `calculateRevenue(start, end)` | System revenue = total delivery fees - courier payouts |
| `updatePayoutStatus(payoutId, status)` | Change payout status |
| `getPayoutsForUser(userId)` | List payouts |
| `getPendingPayouts()` | List payouts with PENDING status |

**Key Business Rule**: Courier gets **70%** share of delivery fees.

---

### 5.4 AuditService

- `logAction(userId, actionType, entityType, entityId, oldValues, newValues, ipAddress, userAgent)` → Creates `SystemAuditLog`
- `logAuthentication(userId, actionType, ipAddress, userAgent)` → Auth-specific audit
- `getLogsByDateRange()`, `getLogsByEntity()`, `getLogsByUser()` → Query methods

---

### 5.5 SmsService (Interface) & TwilioSmsService (Implementation)

**Interface**: `sendSms(phone, message)`, `sendOtp(phone, otp)` (Arabic template: "رمز التحقق الخاص بك هو: {otp}")

**TwilioSmsService**:
- `@ConditionalOnProperty(name="sms.provider", havingValue="twilio", matchIfMissing=true)`
- Lazy initialization of Twilio client
- Retry with exponential backoff (max 3 attempts)
- Phone number formatting to E.164 (auto-detect Egypt +20, Saudi +966, UAE +971)
- Controlled by `sms.enabled` flag (default: `false`)

---

### 5.6 OtpService

- In-memory `ConcurrentHashMap` storage
- 6-digit random OTP
- 5-minute validity
- Max 5 verification attempts per OTP
- Methods: `generateOtp(phone)`, `verifyOtp(phone, otp)`, `invalidateOtp(phone)`

---

### 5.7 BackupService

- MySQL backup via `mysqldump` shell command
- Gzip compression
- Scheduled daily at **2:00 AM** (`@Scheduled(cron="0 0 2 * * *")`)
- Configurable retention policy (default: 30 days)
- Restore from `.sql.gz` file
- Controlled by `backup.enabled` flag

---

### 5.8 MetricsService

Prometheus/Micrometer metrics:
- **Counters**: `twsela.login.attempts`, `twsela.login.success`, `twsela.login.failures`, `twsela.shipments.created`, `twsela.shipments.delivered`
- **Timers**: `twsela.shipments.processing.time`, `twsela.database.query.time`
- **Gauges**: `twsela.users.active`, `twsela.shipments.active`
- **Tagged counters**: `twsela.api.calls` (endpoint, method, status), `twsela.cache.hits/misses`, `twsela.errors`

---

### 5.9 PdfService

- Generates shipment labels (half-A4 size) using **iText PDF**
- Barcode128 for tracking number
- Full Arabic RTL support via **ICU4J** (ArabicShaping/Bidi) + **Noto Sans Arabic** font
- `generateShipmentLabel(shipment)` → single label
- `generateBulkLabels(List<Shipment>)` → multi-page PDF
- Label content: company name ("توصيله"), barcode, recipient info, sender info, COD+fee total, customer service phone

---

### 5.10 ExcelService

- Processes bulk shipment import via **Apache POI** (`.xlsx`)
- Expected headers: `Recipient Name`, `Phone`, `Address`, `Zone Name`, `COD Amount`, `Package Size`, `Notes`
- Validates zone existence, phone format, amounts
- `generateTemplate()` → downloadable template workbook
- All error messages in Arabic

---

### 5.11 FileUploadService

- Uploads Proof of Delivery images
- Storage: `src/main/resources/static/uploads/pod/`
- Public path: `/uploads/pod/{trackingNumber}.{ext}`
- Validates: non-empty file, image MIME type
- `uploadPodImage(file, trackingNumber)`, `deletePodImage(path)`

---

### 5.12 BaseService (Abstract)

Shared utility methods for subclasses:
- `findUserByPhone(phone)` → User lookup with RuntimeException
- `validateUser(user)` → null + active check
- `validateUserRole(user, expectedRole)` → role assertion

---

### 5.13 AuthorizationService

Programmatic access control:
- `getCurrentUser()` → from SecurityContext
- `canAccessShipment(shipmentId)` → OWNER=all, MERCHANT=own, COURIER=assigned, WAREHOUSE_MANAGER=all
- `canModifyShipment(shipmentId)` → MERCHANT can modify only when not yet picked up
- `canAccessUser(userId)` → OWNER=all, others=self only
- `ensureCanAccess*()` / `ensureCanModify*()` → throws SecurityException

---

## 6. Security & Authentication

### 6.1 JWT Configuration

| Parameter | Value |
|-----------|-------|
| Algorithm | HMAC-SHA256 |
| Token expiration | 24 hours (`86400000 ms`) |
| Subject | User's phone number |
| Extra claims | `role` (role name string) |
| Secret | Configurable via `JWT_SECRET` env var |

### 6.2 JwtAuthenticationFilter

- Extracts `Bearer` token from `Authorization` header
- Skips: `/api/auth/login`, `/api/auth/register`, `/api/auth/forgot-password`, `/api/auth/reset-password`
- Extracts role from JWT claims → creates `SimpleGrantedAuthority`
- Sets authentication in `SecurityContextHolder`

### 6.3 SecurityConfig — Route Protection

| Pattern | Access |
|---------|--------|
| `/api/health` | PUBLIC |
| `/api/auth/login`, `/api/public/**`, `/api/debug/**` | PUBLIC |
| `/swagger-ui/**`, `/api-docs/**`, `/v3/api-docs/**` | PUBLIC |
| Static files (`*.html`, `*.css`, `*.js`, etc.) | PUBLIC |
| `/actuator/health,info,metrics,prometheus` | PUBLIC |
| `/api/auth/me` | AUTHENTICATED |
| `/api/shipments/**` | OWNER, ADMIN, MERCHANT, COURIER |
| `/api/master/**` | OWNER, ADMIN |
| `/api/dashboard/**` | ALL roles |
| `/api/manifests/**` | OWNER, ADMIN, COURIER |
| `/api/financial/**` | OWNER, ADMIN, MERCHANT, COURIER |
| `/api/reports/**` | ALL roles |
| `/api/warehouse/**` | OWNER, ADMIN, WAREHOUSE_MANAGER |
| `/api/audit/**` | OWNER, ADMIN |
| `/api/sms/**` | OWNER, ADMIN |
| `/api/backup/**` | OWNER, ADMIN |
| `/api/users/**`, `/api/merchants/**` | OWNER, ADMIN |
| All other `/api/**` | AUTHENTICATED |

**Session**: Stateless (no server-side sessions)
**CSRF**: Disabled
**CORS**: Allows `localhost:5173/5174/8000/8080` origins with credentials

### 6.4 Caching

Redis cache with per-cache TTLs:
| Cache | TTL |
|-------|-----|
| `users` | 30 min |
| `roles` | 1 hour |
| `zones` | 1 hour |
| `shipmentStatuses` | 1 hour |
| `pricing` | 2 hours |
| `dashboard` | 5 min |
| `statistics` | 10 min |

Fallback: `ConcurrentMapCacheManager` when `spring.cache.type=simple`

---

## 7. Roles, Permissions & Access Control

### 7.1 Roles

| Role | Description |
|------|-------------|
| `OWNER` | System owner — full access |
| `ADMIN` | Administrator — full operational access, no user delete or system config |
| `MERCHANT` | Merchant — shipment creation, own data view, reports |
| `COURIER` | Courier — shipment status updates, own manifests, dashboards |
| `WAREHOUSE_MANAGER` | Warehouse — shipment receive/dispatch, reports |

### 7.2 Permission Enum & Mapping

```
Permission                   OWNER  ADMIN  MERCHANT  COURIER  WAREHOUSE
──────────────────────────────────────────────────────────────────────────
user:view                      ✓      ✓
user:create                    ✓      ✓
user:update                    ✓      ✓
user:delete                    ✓
shipment:view                  ✓      ✓       ✓        ✓         ✓
shipment:create                ✓      ✓       ✓                  ✓
shipment:update                ✓      ✓       ✓                  ✓
shipment:delete                ✓
shipment:assign                ✓      ✓                          ✓
shipment:status:update         ✓      ✓                ✓         ✓
zone:view                      ✓      ✓
zone:create                    ✓      ✓
zone:update                    ✓      ✓
zone:delete                    ✓
zone:assign                    ✓      ✓
dashboard:view                 ✓      ✓       ✓        ✓         ✓
reports:view                   ✓      ✓       ✓                  ✓
analytics:view                 ✓      ✓
system:config                  ✓
system:logs                    ✓
```

---

## 8. Shipment Lifecycle & Status Machine

### 8.1 Complete Status Set (from code analysis)

```
PENDING ───────────────┐
                       ▼
               PENDING_APPROVAL
                       │
                       ▼
                   APPROVED
                       │
                       ▼
              RECEIVED_AT_HUB ◄─────── (warehouse/receive)
                       │
                       ▼
             READY_FOR_DISPATCH
                       │
                       ▼
           ASSIGNED_TO_COURIER ◄─────── (manifest assignment)
                       │
                       ▼
             OUT_FOR_DELIVERY ◄──────── (courier starts delivery)
                       │
              ┌────────┼────────┐
              ▼        ▼        ▼
          DELIVERED  FAILED   POSTPONED
              │     ATTEMPT      │
              │        │         │
              │   ┌────┼────┐    │
              │   ▼    ▼    ▼    │
              │  PENDING PENDING │
              │  _UPDATE _RETURN │
              │         │        │
              │         ▼        │
              │   RETURNED_TO_HUB◄┘
              │         │
              │         ▼
              │  RETURNED_TO_ORIGIN
              │
              ▼
          CANCELLED (can occur at any stage)
          ON_HOLD   (can occur at any stage)
```

### 8.2 Status Transitions (from code)

| From | To | Trigger |
|------|----|---------|
| (new) | `PENDING` | Shipment created by merchant |
| `PENDING` | `PENDING_APPROVAL` | Awaiting admin approval |
| `PENDING_APPROVAL` | `APPROVED` | Admin approves |
| `APPROVED` | `RECEIVED_AT_HUB` | Warehouse receives shipment (`POST /shipments/warehouse/receive`) |
| `RECEIVED_AT_HUB` | `READY_FOR_DISPATCH` | Warehouse marks ready |
| `READY_FOR_DISPATCH` | `ASSIGNED_TO_COURIER` | Added to manifest (`POST /manifests/{id}/assign`) |
| `ASSIGNED_TO_COURIER` | `OUT_FOR_DELIVERY` | Courier starts delivery |
| `OUT_FOR_DELIVERY` | `DELIVERED` | Successful delivery with POD |
| `OUT_FOR_DELIVERY` | `FAILED_ATTEMPT` | Generic delivery failure |
| `OUT_FOR_DELIVERY` | `POSTPONED` | Recipient not available / postponed |
| `OUT_FOR_DELIVERY` | `PENDING_UPDATE` | Wrong address / wrong phone |
| `OUT_FOR_DELIVERY` | `PENDING_RETURN` | Rejected / refused by recipient |
| `FAILED_ATTEMPT`/`POSTPONED` | `OUT_FOR_DELIVERY` | Re-attempt |
| `PENDING_RETURN` | `RETURNED_TO_HUB` | Courier returns to warehouse |
| `RETURNED_TO_HUB` | `RETURNED_TO_ORIGIN` | Returned to merchant |
| Any | `CANCELLED` | Cancellation |
| Any | `ON_HOLD` | Hold |

### 8.3 Key Business Events on Status Change

- **DELIVERED**: Sets `deliveredAt` timestamp, triggers `MetricsService.recordShipmentDelivered()`
- **RECEIVED_AT_HUB**: Creates `WarehouseInventory` record
- **Return Request**: Creates new `ReturnShipment` entity linking original → return shipment with new tracking number
- Every status change creates a `ShipmentStatusHistory` record

---

## 9. Repository Layer — Custom Queries

### ShipmentRepository (26 custom methods)

| Method | Query Type |
|--------|-----------|
| `findByTrackingNumber(tn)` | Derived |
| `findByCourierId(id)` | `@Query` JOIN FETCH manifest |
| `searchShipments(tn, name, phone)` | `@Query` LIKE search across tracking/recipient |
| `findByCourierIsNull()` | `@Query` WHERE manifest IS NULL |
| `findByCourierIdAndStatusIn(id, statuses)` | `@Query` JOIN FETCH |
| `findByStatusAndZoneIdIn(status, zoneIds)` | Derived |
| `findByTrackingNumberIn(tns)` | Derived (bulk lookup) |
| `findByManifestId(id)` | Derived |
| `findByPayoutId(id)` | Derived |
| `countByStatus/countByCourierId/countByMerchantId` | Aggregation |
| `findByCourierIdAndStatusNameAndCashReconciledFalse(id, status)` | `@Query` for reconciliation |
| `findByMerchantIdAndStatusNameAndPayoutIsNull(id, status)` | `@Query` for payout calculation |
| `findUnreconciledDeliveredShipments()` | `@Query` delivered + not reconciled |
| `findByRecipientPhone(phone)` | `@Query` through RecipientDetails |

### UserRepository

| Method | Notes |
|--------|-------|
| `findByPhone(phone)` | Primary auth lookup |
| `findByPhoneWithRoleAndStatus(phone)` | `@Query` JOIN FETCH for auth (avoids N+1) |
| `findByRoleName(roleName)` | `@Query` JOIN FETCH role |
| `findActiveUsersByRole(roleName)` | `@Query` active + not deleted |
| `countActiveUsers()` | Aggregation |

### Other Notable Repositories

| Repository | Key Methods |
|------------|-------------|
| `DeliveryPricingRepository` | `findByMerchantIdAndZoneId`, `findByIsActiveTrue` |
| `CourierZoneRepository` | `findByCourierId`, `findByZoneId` (composite PK queries) |
| `PayoutRepository` | `findActivePayoutsForUser(userId, date)`, `findPayoutStatusByName` |
| `ZoneRepository` | `findByNameIgnoreCase`, `findAllActiveZonesOrdered`, `countActiveZones` |
| `SystemAuditLogRepository` | `findByCreatedAtBetween`, `findByEntityTypeAndEntityId` |
| `ShipmentManifestRepository` | `findByManifestNumber`, `findByCourierIdAndStatus` |

---

## 10. DTOs — Request Objects

| DTO | Fields | Used By |
|-----|--------|---------|
| `LoginRequest` | `phone` (@NotBlank, phone pattern), `password` (@NotBlank, min 6) | `POST /api/auth/login` |
| `CreateShipmentRequest` | `recipientName`, `recipientPhone`, `alternatePhone`, `recipientAddress`, `packageDescription`, `packageWeight`, `itemValue`, `codAmount`, `zoneId`, `priority` (STANDARD/EXPRESS/ECONOMY), `shippingFeePaidBy` (MERCHANT/RECIPIENT), `specialInstructions` | `POST /api/shipments` |
| `CreateManifestRequest` | `courierId` (@NotNull) | `POST /api/manifests` |
| `AssignShipmentsRequest` | `trackingNumbers` (List<String>, @NotEmpty) | `POST /api/manifests/{id}/assign` |
| `CreatePayoutRequest` | `userId`, `payoutType` (COURIER/MERCHANT), `startDate`, `endDate` (LocalDate) | `POST /api/financial/payouts` |
| `PasswordResetRequest` | `phone`, `otp`, `newPassword`, `confirmPassword` | `POST /api/public/reset-password` |
| `ReturnRequest` | `reason` (@NotBlank) | `POST /api/shipments/{id}/return-request` |
| `LocationUpdateRequest` | `latitude`, `longitude` (Double, @NotNull) | `PUT /api/shipments/courier/location/update` |
| `ContactFormRequest` | `firstName`, `lastName`, `email` (@Email), `subject`, `message` | `POST /api/public/contact` |

**Inline DTOs** (inner classes in UserController):
- `CreateUserRequest`: `name`, `phone`, `password`, `role`, `vehicleType`, `licensePlateNumber`, `businessName`, `pickupAddress`
- `UpdateUserRequest`: `name`, `phone`, `role`, `status`

---

## 11. Configuration & Infrastructure

### 11.1 application.yml Summary

| Setting | Value |
|---------|-------|
| Database | MySQL 8 at `localhost:3306/twsela` |
| DDL mode | `validate` (production) |
| Server port | `8000` |
| SSL | Disabled for dev (`SERVER_SSL_ENABLED=false`) |
| JWT expiration | 24 hours |
| Logging | File: `/var/log/twsela/twsela.log`, max 100MB, 30-day history |
| Actuator | Exposed: health, info, metrics, prometheus |
| Cache | Redis (commented out in dev) |

### 11.2 DataInitializer Seed Data

Disabled (`@Component` commented out). When enabled, seeds:
- 5 Roles: OWNER, ADMIN, MERCHANT, COURIER, WAREHOUSE_MANAGER
- 4 User Statuses: ACTIVE, INACTIVE, SUSPENDED, PENDING_VERIFICATION
- 8 Shipment Statuses: PENDING, PROCESSING, OUT_FOR_DELIVERY, DELIVERED, FAILED_DELIVERY, RETURNED, CANCELLED, ON_HOLD
- 4 Payout Statuses: PENDING, PROCESSED, FAILED, CANCELLED
- 5 Zones: CAIRO, GIZA, ALEXANDRIA, SHARQIA, DAKAHLEIA (default fee: 50 EGP)
- 8 Telemetry Settings
- 2 Users: Owner (01023782584) + Admin (01023782585)

### 11.3 Infrastructure Components

| Component | Purpose |
|-----------|---------|
| Nginx (`twsela/nginx.conf`) | Reverse proxy |
| Docker (`Dockerfile`, `docker-compose.*.yml`) | Containerization |
| Prometheus (`monitoring/prometheus.yml`) | Metrics collection |
| Grafana (`monitoring/grafana/`) | Dashboards |
| Backup script (`backup/backup-script.sh`) | Database backup |

---

## 12. Appendix: File Index

### Domain (26 files)
`User`, `Shipment`, `Role`, `UserStatus`, `ShipmentStatus`, `Zone`, `ZoneStatus` (enum), `ShipmentManifest`, `ShipmentPackageDetails`, `ShipmentStatusHistory`, `RecipientDetails`, `Payout`, `PayoutItem`, `PayoutStatus`, `MerchantDetails`, `CourierDetails`, `DeliveryPricing`, `CourierZone`, `ReturnShipment`, `CashMovementLedger`, `SystemAuditLog`, `NotificationLog`, `CourierLocationHistory`, `ServiceFeedback`, `MerchantServiceFeedback`, `FraudBlacklist`, `Warehouse`, `WarehouseInventory`, `TelemetrySettings`

### Controllers (14 files)
`AuthController`, `ShipmentController`, `UserController`, `ManifestController`, `DashboardController`, `FinancialController`, `ReportsController`, `MasterDataController`, `SmsController`, `SettingsController`, `HealthController`, `DebugController`, `BackupController`, `AuditController`, `PublicController`

### Services (13 files)
`ShipmentService`, `UserService`, `FinancialService`, `AuditService`, `SmsService` (interface), `TwilioSmsService`, `OtpService`, `BackupService`, `MetricsService`, `PdfService`, `ExcelService`, `FileUploadService`, `BaseService` (abstract), `AuthorizationService`

### Repositories (26 files)
One per entity — all extend `JpaRepository<Entity, ID>`

### Security (5 files)
`SecurityConfig`, `JwtService`, `JwtAuthenticationFilter`, `Permission` (enum), `PermissionService`, `SecurityExceptionHandler`

### Config (4 files)
`DataInitializer`, `CacheConfig`, `SwaggerConfig`, `JacksonConfig`

### DTOs (9 files)
`LoginRequest`, `CreateShipmentRequest`, `CreateManifestRequest`, `AssignShipmentsRequest`, `CreatePayoutRequest`, `PasswordResetRequest`, `ReturnRequest`, `LocationUpdateRequest`, `ContactFormRequest`

---

*End of comprehensive system map.*
