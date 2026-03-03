# Entity ↔ Database Comparison Report

> **Generated**: June 2025  
> **Scope**: 72 MySQL tables vs 98 `@Entity` classes in `com.twsela.domain`  
> **Severity Levels**: 🔴 CRITICAL (runtime failure) | 🟠 HIGH (data loss/inconsistency) | 🟡 MEDIUM (potential issues) | 🔵 LOW (best practice)

---

## Executive Summary

| Metric | Count |
|--------|-------|
| DB Tables | 72 |
| Entity Classes (`@Entity`) | 98 |
| Matched (Entity ↔ Table) | 69 |
| **DB Tables WITHOUT Entity** | **1** |
| **Entities WITHOUT DB Table** | **29** |
| **Column Name Mismatches** | **5** |
| **Phantom Columns (entity has, DB doesn't)** | **9** |
| **Missing Columns (DB has, entity doesn't)** | **4** |
| **Missing JPA Relationships (plain Long FK)** | **7** |

---

## PART 1 — DB Table WITHOUT Matching Entity

### 🟠 `merchant_zone_pricing` — No Entity

| DB Column | Type | Nullable | Key |
|-----------|------|----------|-----|
| id | bigint | NO | PRI |
| merchant_id | bigint | NO | MUL (FK → users) |
| zone_id | bigint | NO | MUL (FK → zones) |
| delivery_fee | decimal | NO | |
| is_active | bit | YES | |
| created_at | datetime | YES | |
| updated_at | datetime | YES | |

**Note**: This table is almost identical to `delivery_pricing`. Both store per-merchant-per-zone delivery fees. `DeliveryPricing.java` maps to `delivery_pricing`, but **nothing maps to `merchant_zone_pricing`**. One of these tables is likely redundant and should be consolidated.

---

## PART 2 — Entities WITHOUT DB Table (29 entities)

These entity classes declare `@Table` names that **do not exist** in the database. Hibernate `ddl-auto=validate` would fail; if set to `update`, Hibernate will auto-create them.

### Group A — Multi-Tenant System (8 entities)

| Entity | @Table | Purpose |
|--------|--------|---------|
| `Tenant` | `tenants` | Core tenant record |
| `TenantUser` | `tenant_users` | User-tenant membership |
| `TenantBranding` | `tenant_branding` | UI customization |
| `TenantConfiguration` | `tenant_configurations` | Key/value settings |
| `TenantQuota` | `tenant_quotas` | Usage limits |
| `TenantInvitation` | `tenant_invitations` | Invite workflow |
| `TenantAuditLog` | `tenant_audit_logs` | Per-tenant audit trail |
| `SystemSetting` | `system_settings` | Global system settings |

> ⚠️ Additionally, several *existing* entities reference `tenantId` as a plain field (User, Shipment, Wallet, Contract, Zone, ApiKey), but **no `tenant_id` column exists in those DB tables**. See Part 4.

### Group B — Payment/Settlement System (6 entities)

| Entity | @Table | Purpose |
|--------|--------|---------|
| `PaymentMethod` | `payment_methods` | Stored payment instruments |
| `PaymentIntent` | `payment_intents` | Payment lifecycle tracking |
| `PaymentRefund` | `payment_refunds` | Refund management |
| `PaymentWebhookLog` | `payment_webhook_logs` | Incoming webhook logs |
| `SettlementBatch` | `settlement_batches` | Periodic settlement runs |
| `SettlementItem` | `settlement_items` | Individual settlement line items |

### Group C — Real-time/Chat System (4 entities)

| Entity | @Table | Purpose |
|--------|--------|---------|
| `LiveNotification` | `live_notifications` | WebSocket notifications |
| `ChatRoom` | `chat_rooms` | Shipment conversations |
| `ChatMessage` | `chat_messages` | Chat messages |
| `TrackingSession` | `tracking_sessions` | Live delivery tracking |
| `LocationPing` | `location_pings` | GPS pings within sessions |

### Group D — Event Sourcing / Async (4 entities)

| Entity | @Table | Purpose |
|--------|--------|---------|
| `DomainEvent` | `domain_events` | Event store |
| `EventSubscription` | `event_subscriptions` | Event handlers |
| `DeadLetterEvent` | `dead_letter_events` | Failed event retry |
| `OutboxMessage` | `outbox_messages` | Transactional outbox |
| `AsyncJob` | `async_jobs` | Background job queue |

### Group E — Security/Compliance (4 entities)

| Entity | @Table | Purpose |
|--------|--------|---------|
| `SecurityEvent` | `security_events` | Security incident log |
| `AccountLockout` | `account_lockouts` | Brute-force lockout tracking |
| `IpBlacklist` | `ip_blacklist` | IP blocking |
| `ComplianceRule` | `compliance_rules` | Compliance check definitions |
| `ComplianceReport` | `compliance_reports` | Compliance audit results |

---

## PART 3 — Column Name Mismatches (🔴 CRITICAL — Runtime Failures)

These will cause SQL errors at runtime because the `@Column(name=...)` in the entity does **not** match the actual DB column name.

### 3.1 `PaymentTransaction` → `payment_transactions`

| Entity Field | @Column name | Actual DB Column | Status |
|-------------|-------------|-----------------|--------|
| `gateway` | `gateway` | `gateway_type` | 🔴 **MISMATCH** |
| `type` | `type` | `payment_type` | 🔴 **MISMATCH** |

**Fix**: Change entity `@Column(name = "gateway")` → `@Column(name = "gateway_type")` and `@Column(name = "type")` → `@Column(name = "payment_type")`.

### 3.2 `TicketMessage` → `ticket_messages`

| Entity Field | @Column name | Actual DB Column | Status |
|-------------|-------------|-----------------|--------|
| `internal` | `is_internal` | `internal` | 🔴 **MISMATCH** |

**Fix**: Change `@Column(name = "is_internal")` → `@Column(name = "internal")`.

### 3.3 `SlaPolicy` → `sla_policies`

| Entity Field | @Column name | Actual DB Column | Status |
|-------------|-------------|-----------------|--------|
| `active` | `is_active` | `active` | 🔴 **MISMATCH** |

**Fix**: Change `@Column(name = "is_active")` → `@Column(name = "active")`, or rename DB column to `is_active`.

### 3.4 `NotificationPreference` → `notification_preferences`

| Entity Field | @Column name | Actual DB Column | Status |
|-------------|-------------|-----------------|--------|
| `enabledChannelsJson` | `enabled_channels` | `enabled_channels` | ✅ OK (explicit mapping present) |

*(This was suspicious but confirmed correct — entity has `@Column(name = "enabled_channels")`.)*

---

## PART 4 — Phantom Columns (Entity has field, DB does NOT have column) 🟠

These entity fields reference columns that do not exist in the database.

| Entity | Field | Expected DB Column | DB Table |
|--------|-------|--------------------|----------|
| `User` | `tenantId` | `tenant_id` | `users` |
| `Shipment` | `tenantId` | `tenant_id` | `shipments` |
| `Wallet` | `tenantId` | `tenant_id` | `wallets` |
| `Contract` | `tenantId` | `tenant_id` | `contracts` |
| `Zone` | `tenantId` | `tenant_id` | `zones` |
| `ApiKey` | `tenantId` | `tenant_id` | `api_keys` |
| `TaxRule` | `name` | `name` | `tax_rules` |
| `TaxRule` | `nameAr` | `name_ar` | `tax_rules` |
| `Invoice` | `paymentTransactionId` | `payment_transaction_id` | `invoices` |
| `SubscriptionPlan` | `displayNameAr` | `display_name_ar` | `subscription_plans` |

> With `ddl-auto=validate` these cause startup failure. With `ddl-auto=update`, Hibernate silently adds these columns.

---

## PART 5 — Missing Columns (DB has column, Entity does NOT) 🟡

| DB Table | DB Column | Type | Entity Class | Status |
|----------|-----------|------|-------------|--------|
| `currencies` | `updated_at` | timestamp | `Currency` | Missing field |
| `sla_policies` | `updated_at` | timestamp | `SlaPolicy` | Missing field |
| `subscription_plans` | `updated_at` | timestamp | `SubscriptionPlan` | Missing field |
| `ticket_messages` | `updated_at` | timestamp | `TicketMessage` | Missing field |

> These columns exist in the DB (often with `DEFAULT_GENERATED on update CURRENT_TIMESTAMP`) but the entity has no corresponding field. This means the entity cannot read/write these values. Since the DB auto-manages them, this is lower severity but the entity should still model them for audit completeness.

---

## PART 6 — Missing JPA Relationships (Plain `Long` instead of `@ManyToOne`) 🟡

These entities use a raw `Long` field for a foreign key instead of a proper `@ManyToOne` JPA relationship, even though the DB has a foreign key constraint.

| Entity | Field | DB FK Target | Should Be |
|--------|-------|-------------|-----------|
| `Notification` | `userId` (Long) | `users.id` | `@ManyToOne User user` |
| `NotificationDeliveryLog` | `notificationId` (Long) | `notifications.id` | `@ManyToOne Notification notification` |
| `ApiKeyUsageLog` | `apiKeyId` (Long) | `api_keys.id` | `@ManyToOne ApiKey apiKey` |
| `AssignmentScore` | `shipmentId` (Long) | `shipments.id` | `@ManyToOne Shipment shipment` |
| `AssignmentScore` | `courierId` (Long) | `users.id` | `@ManyToOne User courier` |
| `OptimizedRoute` | `courierId` (Long) | `users.id` | `@ManyToOne User courier` |
| `OptimizedRoute` | `manifestId` (Long) | `shipment_manifests.id` | `@ManyToOne ShipmentManifest manifest` |
| `UsageTracking` | `merchantId` (Long) | — (no FK in DB) | — (OK as-is) |
| `SystemAuditLog` | `userId` (Long) | — (no FK in DB) | — (OK as-is) |
| `CashMovementLedger` | `shipmentId` (Long) | — (no FK in DB) | — (OK as-is) |
| `SupportTicket` | `shipmentId` (Long) | `shipments.id` (FK exists) | `@ManyToOne Shipment shipment` |

**Impact**: No lazy loading, no cascading, no type safety, no navigation in JPQL joins. Queries require manual `JOIN` on ID instead of using relationship paths.

---

## PART 7 — Matched Entity ↔ Table: Column-Level Audit (69 matches)

✅ = fully matched | ⚠️ = minor issues noted above | ❌ = see Parts 3-6

| # | Entity | @Table | Match Status | Notes |
|---|--------|--------|-------------|-------|
| 1 | `ApiKey` | `api_keys` | ⚠️ | Phantom `tenantId` |
| 2 | `ApiKeyUsageLog` | `api_key_usage_log` | ⚠️ | Plain Long FK |
| 3 | `AssignmentRule` | `assignment_rules` | ✅ | |
| 4 | `AssignmentScore` | `assignment_scores` | ⚠️ | Plain Long FKs |
| 5 | `CashMovementLedger` | `cash_movement_ledger` | ✅ | |
| 6 | `Contract` | `contracts` | ⚠️ | Phantom `tenantId` |
| 7 | `ContractSlaTerms` | `contract_sla_terms` | ✅ | |
| 8 | `Country` | `countries` | ✅ | |
| 9 | `CourierDetails` | `courier_details` | ✅ | |
| 10 | `CourierLocationHistory` | `courier_location_history` | ✅ | |
| 11 | `CourierRating` | `courier_ratings` | ✅ | |
| 12 | `CourierZone` | `courier_zones` | ✅ | |
| 13 | `Currency` | `currencies` | ⚠️ | Missing `updated_at` |
| 14 | `CustomPricingRule` | `custom_pricing_rules` | ✅ | |
| 15 | `DeliveryAttempt` | `delivery_attempts` | ✅ | |
| 16 | `DeliveryPricing` | `delivery_pricing` | ✅ | |
| 17 | `DeliveryProof` | `delivery_proofs` | ✅ | |
| 18 | `DeviceToken` | `device_tokens` | ✅ | |
| 19 | `ECommerceConnection` | `ecommerce_connections` | ✅ | |
| 20 | `ECommerceOrder` | `ecommerce_orders` | ✅ | |
| 21 | `EInvoice` | `e_invoices` | ✅ | |
| 22 | `ExchangeRate` | `exchange_rates` | ✅ | |
| 23 | `FraudBlacklist` | `fraud_blacklist` | ✅ | |
| 24 | `FuelLog` | `fuel_logs` | ✅ | |
| 25 | `Invoice` | `invoices` | ⚠️ | Phantom `paymentTransactionId` |
| 26 | `InvoiceItem` | `invoice_items` | ✅ | |
| 27 | `KnowledgeArticle` | `knowledge_articles` | ✅ | |
| 28 | `KPISnapshot` | `kpi_snapshots` | ✅ | |
| 29 | `MerchantDetails` | `merchant_details` | ✅ | |
| 30 | `MerchantServiceFeedback` | `merchant_service_feedback` | ✅ | |
| 31 | `MerchantSubscription` | `merchant_subscriptions` | ✅ | |
| 32 | `Notification` | `notifications` | ⚠️ | Plain Long FK for user |
| 33 | `NotificationDeliveryLog` | `notification_delivery_log` | ⚠️ | Plain Long FK |
| 34 | `NotificationLog` | `notification_log` | ✅ | |
| 35 | `NotificationPreference` | `notification_preferences` | ✅ | |
| 36 | `NotificationTemplate` | `notification_templates` | ✅ | |
| 37 | `OptimizedRoute` | `optimized_routes` | ⚠️ | Plain Long FKs |
| 38 | `PaymentTransaction` | `payment_transactions` | ❌ | **Column name mismatches** |
| 39 | `Payout` | `payouts` | ✅ | |
| 40 | `PayoutItem` | `payout_items` | ✅ | |
| 41 | `PayoutStatus` | `payout_statuses` | ✅ | |
| 42 | `PickupSchedule` | `pickup_schedules` | ✅ | |
| 43 | `RecipientDetails` | `recipient_details` | ✅ | |
| 44 | `ReturnShipment` | `return_shipments` | ✅ | |
| 45 | `Role` | `roles` | ✅ | |
| 46 | `ServiceFeedback` | `service_feedback` | ✅ | |
| 47 | `Shipment` | `shipments` | ⚠️ | Phantom `tenantId` |
| 48 | `ShipmentManifest` | `shipment_manifests` | ✅ | |
| 49 | `ShipmentPackageDetails` | `shipment_package_details` | ✅ | |
| 50 | `ShipmentStatus` | `shipment_statuses` | ✅ | |
| 51 | `ShipmentStatusHistory` | `shipment_status_history` | ✅ | |
| 52 | `SlaPolicy` | `sla_policies` | ❌ | **Column name mismatch** (`is_active` vs `active`), missing `updated_at` |
| 53 | `SubscriptionPlan` | `subscription_plans` | ⚠️ | Phantom `displayNameAr`, missing `updated_at` |
| 54 | `SupportTicket` | `support_tickets` | ⚠️ | Plain Long FK for shipment |
| 55 | `SystemAuditLog` | `system_audit_log` | ✅ | |
| 56 | `TaxRule` | `tax_rules` | ⚠️ | Phantom `name`, `nameAr` |
| 57 | `TelemetrySettings` | `telemetry_settings` | ✅ | |
| 58 | `TicketMessage` | `ticket_messages` | ❌ | **Column name mismatch** (`is_internal` vs `internal`), missing `updated_at` |
| 59 | `UsageTracking` | `usage_tracking` | ✅ | |
| 60 | `User` | `users` | ⚠️ | Phantom `tenantId` |
| 61 | `UserStatus` | `user_statuses` | ✅ | |
| 62 | `Vehicle` | `vehicles` | ✅ | |
| 63 | `VehicleAssignment` | `vehicle_assignments` | ✅ | |
| 64 | `VehicleMaintenance` | `vehicle_maintenance` | ✅ | |
| 65 | `Wallet` | `wallets` | ⚠️ | Phantom `tenantId` |
| 66 | `WalletTransaction` | `wallet_transactions` | ✅ | |
| 67 | `Warehouse` | `warehouses` | ✅ | |
| 68 | `WarehouseInventory` | `warehouse_inventory` | ✅ | |
| 69 | `WebhookEvent` | `webhook_events` | ✅ | |
| 70 | `WebhookSubscription` | `webhook_subscriptions` | ✅ | |
| 71 | `Zone` | `zones` | ⚠️ | Phantom `tenantId` |

---

## PART 8 — Duplicate/Redundant Tables

| Table 1 | Table 2 | Columns | Verdict |
|---------|---------|---------|---------|
| `delivery_pricing` | `merchant_zone_pricing` | Identical schema (merchant_id, zone_id, delivery_fee, is_active, timestamps) | **One should be removed.** `delivery_pricing` has an entity; `merchant_zone_pricing` does not. |

---

## PART 9 — Priority Fix List

### 🔴 CRITICAL — Fix Immediately (runtime failures)

1. **`PaymentTransaction`**: Rename `@Column(name = "gateway")` → `"gateway_type"` and `@Column(name = "type")` → `"payment_type"`
2. **`TicketMessage`**: Rename `@Column(name = "is_internal")` → `"internal"`
3. **`SlaPolicy`**: Rename `@Column(name = "is_active")` → `"active"`

### 🟠 HIGH — Fix Before Next Deploy (data inconsistency)

4. **Phantom `tenantId` on 6 entities** (User, Shipment, Wallet, Contract, Zone, ApiKey): Either add `tenant_id` column to those 6 DB tables via migration, or remove the field from entities until multi-tenancy is implemented
5. **`TaxRule`** phantom fields `name`, `nameAr`: Add columns via migration or remove from entity
6. **`Invoice`** phantom `paymentTransactionId`: Add column or remove from entity
7. **`SubscriptionPlan`** phantom `displayNameAr`: Add `display_name_ar` column or remove from entity
8. **`merchant_zone_pricing`** table: Create entity or drop table (consolidate with `delivery_pricing`)

### 🟡 MEDIUM — Plan for Sprint

9. **Add `updatedAt` field** to `Currency`, `SlaPolicy`, `SubscriptionPlan`, `TicketMessage` entities
10. **Create 29 missing DB tables** for entities (tenants, payment_methods, chat system, event sourcing, security, etc.) — or mark these entities with `@Transient` / remove if they're not yet needed
11. **Convert plain Long FKs** to `@ManyToOne` in `Notification`, `NotificationDeliveryLog`, `ApiKeyUsageLog`, `AssignmentScore`, `OptimizedRoute`, `SupportTicket`

### 🔵 LOW — Technical Debt

12. Standardize boolean column naming: some use `is_active` (DB), some use `active` (DB). Pick one convention.
13. Standardize timestamp types: mixture of `datetime` and `timestamp` across DB tables for similar fields.
14. Add missing `@Index` annotations to match DB indexes where entity doesn't declare them.

---

## PART 10 — Non-Entity Domain Files

These files in `com.twsela.domain` are **not** `@Entity` classes:

| File | Type | Used By |
|------|------|---------|
| `NotificationChannel.java` | Enum | `Notification`, `NotificationDeliveryLog`, `NotificationTemplate` |
| `NotificationType.java` | Enum | `Notification`, `NotificationTemplate` |
| `ShipmentStatusConstants.java` | Constants | Shipment status name constants |
| `ZoneStatus.java` | Enum | `Zone` |

---

## PART 11 — FK Constraints Validation

### DB FK constraints without corresponding JPA `@ManyToOne`:

| DB FK | Table | Column | Target | Entity Uses |
|-------|-------|--------|--------|-------------|
| `fk_usage_apikey` | `api_key_usage_log` | `api_key_id` | `api_keys` | Plain `Long apiKeyId` |
| `fk_score_shipment` | `assignment_scores` | `shipment_id` | `shipments` | Plain `Long shipmentId` |
| `fk_score_courier` | `assignment_scores` | `courier_id` | `users` | Plain `Long courierId` |
| `fk_route_courier` | `optimized_routes` | `courier_id` | `users` | Plain `Long courierId` |
| `fk_route_manifest` | `optimized_routes` | `manifest_id` | `shipment_manifests` | Plain `Long manifestId` |
| `fk_ndl_notification` | `notification_delivery_log` | `notification_id` | `notifications` | Plain `Long notificationId` |
| FK on `notifications` | `notifications` | `user_id` | `users` | Plain `Long userId` |
| `fk_ticket_shipment` | `support_tickets` | `shipment_id` | `shipments` | Plain `Long shipmentId` |

### JPA `@ManyToOne` without DB FK constraint:

All JPA relationships in matched entities have corresponding DB FK constraints. ✅

---

*End of Report*
