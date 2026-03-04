# Twsela — Comprehensive Business Rules Extraction

> **Auto-generated from backend Java code analysis**
> Covers: entities, services, security, configuration, enums, validation, calculations, scheduled tasks

---

## Table of Contents

1. [Authentication & Account Security](#1-authentication--account-security)
2. [User Management & Validation](#2-user-management--validation)
3. [Role-Based Access Control (RBAC)](#3-role-based-access-control-rbac)
4. [Shipment Lifecycle](#4-shipment-lifecycle)
5. [Pricing & Fees](#5-pricing--fees)
6. [Wallet & Financial Transactions](#6-wallet--financial-transactions)
7. [Settlement & Payouts](#7-settlement--payouts)
8. [Smart Courier Assignment](#8-smart-courier-assignment)
9. [Returns (RTO)](#9-returns-rto)
10. [Delivery Attempts](#10-delivery-attempts)
11. [Subscriptions & Plans](#11-subscriptions--plans)
12. [Contracts](#12-contracts)
13. [Custom / Contract Pricing](#13-custom--contract-pricing)
14. [Tax Rules](#14-tax-rules)
15. [Promo Codes](#15-promo-codes)
16. [Payments & Gateways](#16-payments--gateways)
17. [Invoicing](#17-invoicing)
18. [Fleet Management](#18-fleet-management)
19. [Zone Management](#19-zone-management)
20. [Gamification](#20-gamification)
21. [Loyalty Programs](#21-loyalty-programs)
22. [Support Tickets & SLA](#22-support-tickets--sla)
23. [Notifications](#23-notifications)
24. [OTP & Verification](#24-otp--verification)
25. [Rate Limiting](#25-rate-limiting)
26. [IP Blocking](#26-ip-blocking)
27. [Input Sanitization](#27-input-sanitization)
28. [Password Policy](#28-password-policy)
29. [Multi-Tenancy & Quotas](#29-multi-tenancy--quotas)
30. [Feature Flags](#30-feature-flags)
31. [Scheduled Tasks & Cron Jobs](#31-scheduled-tasks--cron-jobs)
32. [System Configuration Defaults](#32-system-configuration-defaults)
33. [Seed Data & Initialization](#33-seed-data--initialization)

---

## 1. Authentication & Account Security

**Source:** `web/controller/AuthController.java`, `service/AccountLockoutService.java`, `security/JwtService.java`, `config/SecurityConfig.java`

### 1.1 Login Flow
| Rule | Value | Source |
|------|-------|--------|
| JWT algorithm | HMAC-SHA256 | `JwtService.java` |
| JWT token expiration | 86,400,000 ms (24 hours) | `application.yml` → `jwt.expiration` |
| JWT secret source | Environment variable `JWT_SECRET` (required) | `application.yml` |
| JWT claim for role | `"ROLE_" + roleName.toUpperCase()` | `AuthController.java` |
| Session policy | STATELESS (no server-side sessions) | `SecurityConfig.java` |
| CSRF | Disabled (stateless API) | `SecurityConfig.java` |
| Token blacklisting on logout | Yes — token added to blacklist | `AuthController.java` |

### 1.2 Account Lockout

**AuthController lockout (inline logic):**

| Rule | Value |
|------|-------|
| Failed login → increment `failedLoginAttempts` | Per user |
| Lock threshold | `failedLoginAttempts >= 5` |
| Lock duration | **15 minutes** (`lockedUntil = now + 15min`) |
| On successful login | Reset `failedLoginAttempts = 0`, clear `lockedUntil` |
| Pre-login check | If `lockedUntil > now` → reject with "Account locked" |
| User must be active | `status == ACTIVE && !isDeleted` |

**AccountLockoutService (standalone service — may differ):**

| Rule | Value |
|------|-------|
| `MAX_FAILED_ATTEMPTS` | 5 |
| `LOCKOUT_DURATION_MINUTES` | 30 |
| Admin can manually unlock | Yes — resets counter to 0 |

> ⚠️ **Discrepancy:** AuthController uses 15-min lockout; AccountLockoutService uses 30-min. The AuthController inline logic takes precedence at runtime.

---

## 2. User Management & Validation

**Source:** `domain/User.java`, `web/validation/PasswordValidator.java`

### 2.1 User Entity Validation Constraints

| Field | Constraint | Pattern/Value |
|-------|-----------|---------------|
| `phone` | `@NotBlank`, `@Pattern` | `^[0-9]{10,15}$` (10–15 digits only) |
| `name` | `@NotBlank`, `@Size(min=2, max=100)`, `@Pattern` | `^[a-zA-Z\u0600-\u06FF\s]+$` (English + Arabic letters + spaces) |
| `password` | `@NotBlank`, `@Size(min=6)` | BCrypt hash stored (max 72 chars raw) |

### 2.2 User Active Status (Derived)
```
isActive = (status.getName() == "ACTIVE") && (!isDeleted)
```

### 2.3 Soft Delete
- `isDeleted` boolean flag
- `deletedAt` timestamp set on soft delete
- Soft-deleted users are excluded from active queries

### 2.4 Password Validator (Annotation `@ValidPassword`)
| Rule | Value |
|------|-------|
| Minimum length | 6 characters |
| Must contain at least one letter | `Character.isLetter(c)` |
| Must contain at least one digit | `Character.isDigit(c)` |
| Null or blank → invalid | Yes |

### 2.5 User Statuses
`ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING_VERIFICATION`

---

## 3. Role-Based Access Control (RBAC)

**Source:** `security/PermissionService.java`, `config/SecurityConfig.java`, `security/Permission.java`

### 3.1 Roles
`OWNER`, `ADMIN`, `MERCHANT`, `COURIER`, `WAREHOUSE_MANAGER`

### 3.2 Permission Matrix

| Permission | OWNER | ADMIN | MERCHANT | COURIER | WAREHOUSE_MANAGER |
|-----------|:-----:|:-----:|:--------:|:-------:|:-----------------:|
| USER_VIEW | ✅ | ✅ | | | |
| USER_CREATE | ✅ | ✅ | | | |
| USER_UPDATE | ✅ | ✅ | | | |
| USER_DELETE | ✅ | | | | |
| SHIPMENT_VIEW | ✅ | ✅ | ✅ | ✅ | ✅ |
| SHIPMENT_CREATE | ✅ | ✅ | ✅ | | ✅ |
| SHIPMENT_UPDATE | ✅ | ✅ | ✅ | | ✅ |
| SHIPMENT_DELETE | ✅ | | | | |
| SHIPMENT_ASSIGN | ✅ | ✅ | | | ✅ |
| SHIPMENT_STATUS_UPDATE | ✅ | ✅ | | ✅ | ✅ |
| ZONE_VIEW | ✅ | ✅ | | | |
| ZONE_CREATE | ✅ | ✅ | | | |
| ZONE_UPDATE | ✅ | ✅ | | | |
| ZONE_DELETE | ✅ | | | | |
| ZONE_ASSIGN | ✅ | ✅ | | | |
| DASHBOARD_VIEW | ✅ | ✅ | ✅ | ✅ | ✅ |
| REPORTS_VIEW | ✅ | ✅ | ✅ | | ✅ |
| ANALYTICS_VIEW | ✅ | ✅ | | | |
| SYSTEM_CONFIG | ✅ | | | | |
| SYSTEM_LOGS | ✅ | | | | |

### 3.3 URL-Level Access Rules (SecurityConfig)

| Endpoint Pattern | Allowed Roles |
|-----------------|---------------|
| `/api/health`, `/api/auth/login`, `/api/public/**` | Public (unauthenticated) |
| `/api/couriers/location` | COURIER only |
| `/api/wallet/admin/**`, `/api/analytics/**`, `/api/bi-analytics/**` | OWNER, ADMIN |
| `/api/audit/**`, `/api/sms/**`, `/api/backup/**` | OWNER, ADMIN |
| `/api/users/**`, `/api/merchants/**`, `/api/master/**` | OWNER, ADMIN |
| `/api/assignment/**`, `/api/routes/**`, `/api/demand/**` | OWNER, ADMIN |
| `/api/security/**`, `/api/compliance/**`, `/api/events/**` | OWNER, ADMIN |
| `/api/jobs/**`, `/api/tenants/**`, `/api/workflows/**` | OWNER, ADMIN |
| `/api/automation-rules/**` | OWNER, ADMIN |
| `/api/shipments/**` | OWNER, ADMIN, MERCHANT, COURIER, WAREHOUSE_MANAGER |
| `/api/dashboard/**` | OWNER, ADMIN, MERCHANT, COURIER, WAREHOUSE_MANAGER |
| `/api/wallet/**`, `/api/notifications/**` | Any authenticated |
| `/api/support/tickets/**`, `/api/chat/**` | Any authenticated |
| Actuator (metrics/prometheus) | OWNER, ADMIN |
| Swagger/API docs | Public |
| WebSocket `/ws/**` | Public |

---

## 4. Shipment Lifecycle

**Source:** `domain/ShipmentStatusConstants.java`, `service/ShipmentService.java`, `domain/Shipment.java`

### 4.1 All Shipment Statuses (21 total)

| Status | Description |
|--------|-------------|
| `PENDING` | Initial fallback status |
| `PENDING_APPROVAL` | Default on creation — awaiting approval |
| `APPROVED` | Approved; ready for courier assignment |
| `PICKED_UP` | Courier picked up from merchant |
| `RECEIVED_AT_HUB` | Arrived at warehouse hub |
| `READY_FOR_DISPATCH` | Ready to be dispatched from hub |
| `ASSIGNED_TO_COURIER` | Assigned to a courier for delivery |
| `IN_TRANSIT` | En route to recipient |
| `OUT_FOR_DELIVERY` | Last-mile delivery in progress |
| `DELIVERED` | Successfully delivered (**terminal**) |
| `FAILED_DELIVERY` | Delivery attempt failed |
| `FAILED_ATTEMPT` | Specific failed attempt recorded |
| `POSTPONED` | Rescheduled by recipient/courier |
| `PENDING_UPDATE` | Needs address/phone update |
| `PENDING_RETURN` | Marked for return to origin |
| `RETURNED_TO_HUB` | Returned to warehouse hub |
| `RETURNED_TO_ORIGIN` | Returned to merchant (**terminal**) |
| `CANCELLED` | Cancelled (**terminal**) |
| `ON_HOLD` | Temporarily on hold |
| `RESCHEDULED` | Rescheduled for new delivery date |
| `PARTIALLY_DELIVERED` | Partial delivery completed |

### 4.2 Status Transition Rules (Happy Path)
```
PENDING → PENDING_APPROVAL → APPROVED → PICKED_UP → RECEIVED_AT_HUB
→ READY_FOR_DISPATCH → ASSIGNED_TO_COURIER → IN_TRANSIT
→ OUT_FOR_DELIVERY → DELIVERED
```

### 4.3 Failed Delivery Reason Mapping
When a delivery fails, the `failedNote` text is analyzed to determine next status:

| Keywords in Note | Next Status |
|-----------------|-------------|
| `reschedule`, `postpone`, `later`, `tomorrow` | `POSTPONED` |
| `address`, `phone`, `update`, `wrong` | `PENDING_UPDATE` |
| `return`, `refuse`, `reject`, `back` | `PENDING_RETURN` |
| *default (no match)* | `POSTPONED` |

### 4.4 Dispatch Validation Rules
| Scenario | Rule |
|----------|------|
| Assign to courier | Shipment must be in `APPROVED` status |
| Warehouse dispatch | Shipment must be in `RECEIVED_AT_HUB` or `RETURNED_TO_HUB` |
| RTO (Return to Origin) | Cannot return if status is `DELIVERED`, `CANCELLED`, or `RETURNED_TO_ORIGIN` |

### 4.5 Tracking Number Format
```
"TWS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
```
Example: `TWS-A1B2C3D4`

### 4.6 Manifest Number Format
```
"MAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
```

### 4.7 Shipment Entity Defaults & Constraints

| Field | Default | Constraint |
|-------|---------|-----------|
| `itemValue` | 0 (on create) | `@NotNull`, `@DecimalMin("0.0")` |
| `codAmount` | `BigDecimal.ZERO` | `@NotNull`, `@DecimalMin("0.0")` |
| `deliveryFee` | Calculated from pricing | `@NotNull`, `@DecimalMin("0.0")` |
| `shippingFeePaidBy` | `MERCHANT` | Enum: `MERCHANT`, `RECIPIENT`, `PREPAID` |
| `sourceType` | `MERCHANT` | Enum: `MERCHANT`, `THIRD_PARTY_LOGISTICS_PARTNER("3PL_PARTNER")` |
| `podType` | — | Enum: `OTP`, `PHOTO`, `SIGNATURE` |
| `cashReconciled` | `false` | Boolean |
| `initialStatus` | `PENDING_APPROVAL` (fallback `PENDING`) | Set at creation |

### 4.8 Manifest Statuses
`CREATED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`

### 4.9 Courier Location Update
- Only users with `COURIER` role can update location
- Validates role before accepting latitude/longitude

---

## 5. Pricing & Fees

**Source:** `service/ShipmentService.java`, `service/CustomPricingService.java`, `domain/DeliveryPricing.java`

### 5.1 Delivery Fee Calculation Hierarchy
Priority order (first match wins):

| Priority | Source | Lookup |
|----------|--------|--------|
| 1 (highest) | Merchant-Zone specific price | `DeliveryPricing` where `merchant_id + zone_id` match and `isActive=true` |
| 2 | Zone default fee | `zone.defaultFee` |
| 3 | System setting | `DEFAULT_DELIVERY_FEE` from telemetry settings |
| 4 (fallback) | Hardcoded | **50.00 EGP** |

### 5.2 Priority Multipliers
Applied on top of base delivery fee:

| Priority | Multiplier | Example (base=50 EGP) |
|----------|-----------|----------------------|
| `EXPRESS` | **1.5×** | 75.00 EGP |
| `STANDARD` | **1.0×** | 50.00 EGP |
| `ECONOMY` | **0.8×** | 40.00 EGP |

### 5.3 Custom/Contract Pricing Formula
```
finalPrice = baseFee + (perKgRate × weight) + codFeeAmount − volumeDiscount
```
- `codFeeAmount = codAmount × codFeePercentage`
- Subject to `minimumCharge` floor
- Volume discount applied if monthly shipment count ≥ `minMonthlyShipments` (30-day window)

### 5.4 Default Pricing (CustomPricingService)

| Parameter | Default Value |
|-----------|--------------|
| Base fee | **25.00 EGP** |
| Per-kg rate | **2.00 EGP** |
| COD fee | **2%** (0.02) |

### 5.5 DeliveryPricing Entity Constraints
- Unique constraint on `(merchant_id, zone_id)` — one price per merchant per zone
- `isActive` flag to enable/disable

---

## 6. Wallet & Financial Transactions

**Source:** `domain/Wallet.java`, `domain/WalletTransaction.java`, `service/WalletService.java`

### 6.1 Wallet Types
`MERCHANT`, `COURIER`, `COMPANY`

### 6.2 Defaults
| Field | Default |
|-------|---------|
| Currency | `"EGP"` |
| Balance | `BigDecimal.ZERO` |

### 6.3 Transaction Types & Reasons

**Types:** `CREDIT`, `DEBIT`

**Reasons:**
| Reason | Description |
|--------|-------------|
| `COD_COLLECTED` | Cash on delivery collected |
| `DELIVERY_FEE` | Delivery fee charged/paid |
| `COMMISSION` | Company commission |
| `WITHDRAWAL` | Cash withdrawal |
| `SETTLEMENT` | Settlement batch payment |
| `RETURN_FEE` | Return shipment fee |
| `ADJUSTMENT` | Manual adjustment |

### 6.4 Credit/Debit Rules

| Operation | Rule |
|-----------|------|
| Credit | Amount must be > 0 |
| Debit | Amount must be > 0 **AND** balance ≥ amount (sufficient funds) |
| Idempotency | Check `existsByWalletIdAndReferenceIdAndReason` before creating — prevents duplicate transactions |

### 6.5 COD Settlement on Delivery

When shipment status changes to `DELIVERED`:

| Step | Wallet | Type | Amount |
|------|--------|------|--------|
| 1 | Courier wallet | CREDIT | `codAmount` (full COD collected) |
| 2 | Merchant wallet | CREDIT | `codAmount − deliveryFee` (COD minus fee) |
| (implied) | Company | — | `deliveryFee` (commission) |

**Skip Condition:** If both `codAmount == 0` and `deliveryFee == 0` → skip settlement entirely.

### 6.6 Balance Tracking
Each transaction records:
- `balanceBefore` — wallet balance before transaction
- `balanceAfter` — wallet balance after transaction

---

## 7. Settlement & Payouts

**Source:** `service/SettlementService.java`, `domain/Payout.java`

### 7.1 Settlement Fee
| Rule | Value |
|------|-------|
| Default fee percentage | **2.5%** (0.025) |
| Fee exemptions | `REFUND` and `ADJUSTMENT` item types → fee = 0 |

### 7.2 Settlement Periods
`DAILY`, `WEEKLY`, `BIWEEKLY`, `MONTHLY`

### 7.3 Settlement Batch Statuses & Transitions
```
DRAFT → PENDING → PROCESSING → COMPLETED
                              → FAILED
```

| Rule | Detail |
|------|--------|
| Items can only be added to | `DRAFT` batches |
| Processing allowed from | `DRAFT` or `PENDING` status only |
| Duplicate prevention | Cannot create batch for same `period + startDate` combination |

### 7.4 Settlement Number Format
```
"STL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
```

### 7.5 Payout Statuses
`PENDING`, `PROCESSED`, `FAILED`, `CANCELLED`

---

## 8. Smart Courier Assignment

**Source:** `service/SmartAssignmentService.java`

### 8.1 Scoring Formula (Weighted)
```
Total Score = (0.40 × DistanceScore) + (0.25 × LoadScore) + (0.15 × RatingScore)
            + (0.10 × ZoneScore)     + (0.05 × VehicleScore) + (0.05 × HistoryScore)
```

### 8.2 Default Thresholds

| Threshold | Value |
|-----------|-------|
| `MAX_LOAD` | 30 shipments |
| `MAX_DISTANCE` | 50.0 km |
| `MIN_RATING` | 3.0 (out of 5.0) |
| `ZONE_REQUIRED` | `true` (courier must be in same zone) |

### 8.3 Individual Score Calculations

**Distance Score (weight: 0.40):**
- Uses **Haversine formula** (Earth radius = 6,371 km)
- `0.0` if distance > `maxDistanceKm`
- `0.5` if courier has no coordinates (fallback)
- Formula: `1.0 - (distance / maxDistanceKm)`

**Load Score (weight: 0.25):**
- `0.0` if `currentLoad >= maxLoad`
- Formula: `1.0 - (currentLoad / maxLoad)`

**Rating Score (weight: 0.15):**
- `0.5` for new couriers with no ratings
- `0.0` if average rating < `minRating`
- Formula: `averageRating / 5.0`

**Zone Score (weight: 0.10):**
- `1.0` if courier is assigned to shipment's zone
- `0.0` otherwise

**Vehicle Score (weight: 0.05):**
- `1.0` if courier has an `ACTIVE` vehicle assignment
- `0.0` otherwise

**History Score (weight: 0.05):**
- Ratio of `deliveredCount / totalCount` over last 30 days
- `0.5` for new couriers (no history)

### 8.4 Pre-filter Rejection Criteria
Before scoring, reject couriers who:
1. Have average rating < `minRating` (3.0)
2. Have `currentLoad >= maxLoad` (30)
3. Are not assigned to shipment's zone (if `ZONE_REQUIRED = true`)

---

## 9. Returns (RTO)

**Source:** `service/ReturnService.java`, `domain/ReturnShipment.java`

### 9.1 Return Status Transitions
```
RETURN_REQUESTED → RETURN_APPROVED → RETURN_PICKUP_ASSIGNED
                 → RETURN_REJECTED    → RETURN_PICKED_UP
                                        → RETURN_IN_WAREHOUSE
                                          → RETURN_DELIVERED_TO_MERCHANT
```

| From Status | Allowed Transitions |
|------------|-------------------|
| `RETURN_REQUESTED` | `RETURN_APPROVED`, `RETURN_REJECTED` |
| `RETURN_APPROVED` | `RETURN_PICKUP_ASSIGNED` |
| `RETURN_PICKUP_ASSIGNED` | `RETURN_PICKED_UP` |
| `RETURN_PICKED_UP` | `RETURN_IN_WAREHOUSE` |
| `RETURN_IN_WAREHOUSE` | `RETURN_DELIVERED_TO_MERCHANT` |

### 9.2 Return Eligibility
Original shipment must be in one of these statuses:
- `DELIVERED`
- `PARTIALLY_DELIVERED`
- `FAILED_DELIVERY`
- `FAILED_ATTEMPT`

### 9.3 Return Fee Calculation
```
returnFee = originalDeliveryFee × RETURN_FEE_RATE
RETURN_FEE_RATE = 0.50 (50% of original delivery fee)
```

### 9.4 Business Rules
| Rule | Detail |
|------|--------|
| Duplicate prevention | Cannot create return if active return already exists for shipment (status ≠ `RETURN_REJECTED`) |
| Courier assignment | Courier can only be assigned when return is in `RETURN_APPROVED` status |

---

## 10. Delivery Attempts

**Source:** `domain/DeliveryAttempt.java`

### 10.1 Attempt Statuses
`FAILED`, `SUCCESS`

### 10.2 Failure Reasons
| Reason | Description |
|--------|-------------|
| `CUSTOMER_ABSENT` | Customer not at delivery address |
| `WRONG_ADDRESS` | Incorrect delivery address |
| `REFUSED` | Customer refused to accept |
| `PHONE_OFF` | Customer's phone is off |
| `DAMAGED` | Package was damaged |
| `OTHER` | Other reason (see notes) |

### 10.3 Attempt Tracking
- Unique constraint on `(shipment_id, attempt_number)` — each attempt numbered sequentially
- Records: GPS coordinates (lat/lng), photo URL, notes, timestamp
- Can schedule `nextAttemptDate` for retry

---

## 11. Subscriptions & Plans

**Source:** `service/SubscriptionService.java`, `domain/SubscriptionPlan.java`, `domain/MerchantSubscription.java`

### 11.1 Plan Names
`FREE`, `BASIC`, `PRO`, `ENTERPRISE`

### 11.2 Plan Attributes

| Attribute | Description |
|-----------|-------------|
| `monthlyPrice` | Price per month (BigDecimal) |
| `annualPrice` | Price per year (BigDecimal) |
| `maxShipmentsPerMonth` | 0 or negative = unlimited; Free default = 50 |
| `maxWebhooks` | Maximum webhook integrations |
| `apiRateLimit` | API calls per window (default: 100) |
| `sortOrder` | Used for upgrade/downgrade comparison |

### 11.3 Subscription Lifecycle
```
TRIAL → ACTIVE → PAST_DUE → EXPIRED
                           → CANCELLED
```

### 11.4 Subscription Rules

| Rule | Detail |
|------|--------|
| Trial period | **14 days** (`TRIAL_DAYS = 14`) |
| Billing cycles | `MONTHLY` (30 days), `ANNUAL` (365 days) |
| Upgrade rule | New plan `sortOrder` must be **>** current plan `sortOrder` |
| Downgrade rule | New plan `sortOrder` must be **<** current plan `sortOrder` |
| Cancellation | Sets `autoRenew = false` (does not immediately terminate) |
| Cannot renew | Cancelled subscriptions |
| Usage limit check | Free plan default = 50 shipments/month; 0 or negative `maxShipmentsPerMonth` = unlimited |
| Expiry processing | Daily at **2:00 AM** — if `autoRenew` → `PAST_DUE`; if `!autoRenew` → `EXPIRED` |

---

## 12. Contracts

**Source:** `service/ContractService.java`, `domain/Contract.java`

### 12.1 Contract Types
- `MERCHANT_AGREEMENT`
- `COURIER_AGREEMENT`
- `PARTNER_AGREEMENT`

### 12.2 Contract Statuses & Transitions
```
DRAFT → PENDING_SIGNATURE → ACTIVE → EXPIRING_SOON → EXPIRED
                                    → TERMINATED
```

### 12.3 Contract Rules

| Rule | Detail |
|------|--------|
| Contract number format | `"TWS-CTR-" + 8-char UUID uppercase` |
| End date validation | Must be **after** start date |
| Send for signature | Only from `DRAFT` status |
| Signature via OTP | OTP sent to party's phone; verified to activate |
| Sign contract | Only from `PENDING_SIGNATURE` status |
| On valid OTP | Status → `ACTIVE`, `signedAt` set, OTP cleared |
| Terminate | Only `ACTIVE` or `EXPIRING_SOON` contracts |
| Renew | Only `ACTIVE`, `EXPIRING_SOON`, or `EXPIRED` contracts |
| Renewal extends by | Same duration as original (calculated from `startDate` to `endDate`) |
| Edit allowed | Only on `DRAFT` status contracts |
| Default `renewalNoticeDays` | 30 days |

### 12.4 Auto-Renewal (Scheduled)
- Runs daily at **1:00 AM** (Africa/Cairo timezone)
- Checks contracts with `autoRenew = true` expiring within **7 days**
- Automatically extends by original duration

### 12.5 Expiry Reminders (Scheduled)
- Runs daily at **8:00 AM** (Africa/Cairo timezone)
- Sends reminders at **30, 14, and 7 days** before expiry

---

## 13. Custom / Contract Pricing

**Source:** `service/CustomPricingService.java`

### 13.1 Pricing Priority
```
1. Contract-specific pricing  →  2. Volume discount  →  3. Default pricing
```

### 13.2 Default Pricing Parameters

| Parameter | Value |
|-----------|-------|
| Base fee | **25.00 EGP** |
| Per-kg rate | **2.00 EGP/kg** |
| COD fee percentage | **2%** (0.02) |

### 13.3 Contract Pricing Formula
```
price = baseFee + (perKgRate × weight) + (codAmount × codFeePercentage) − volumeDiscount
IF price < minimumCharge THEN price = minimumCharge
```

### 13.4 Volume Discount Eligibility
- Applied when merchant's monthly shipment count ≥ `minMonthlyShipments`
- Window: last 30 days
- Discount amount defined per contract

---

## 14. Tax Rules

**Source:** `domain/TaxRule.java`, `service/TaxService.java`

### 14.1 Tax Types
`VAT`, `SALES_TAX`, `CUSTOMS`

### 14.2 Tax Rule Properties
| Property | Description |
|----------|-------------|
| `rate` | Decimal (e.g., 0.14 for 14% VAT) |
| `validFrom` / `validTo` | Date range for rule applicability |
| `exemptCategories` | JSON array of exempt category names |
| `isPrimary` | Boolean — first applicable primary rule used |

### 14.3 Tax Calculation
- Find first applicable tax rule for the given category
- Check date validity (`validFrom ≤ now ≤ validTo`)
- Check category not in `exemptCategories`
- Apply: `taxAmount = baseAmount × rate`

---

## 15. Promo Codes

**Source:** `domain/PromoCode.java`, `service/PromoCodeService.java`

### 15.1 Promo Code Properties

| Property | Description |
|----------|-------------|
| `code` | Unique code string |
| `discountType` | Type of discount (PERCENTAGE / FIXED) |
| `discountValue` | Numeric discount value |
| `minOrderValue` | Minimum order value for eligibility |
| `maxDiscountAmount` | Cap on discount (for percentage type) |
| `maxUsageTotal` | Maximum total redemptions |
| `maxUsagePerUser` | Maximum per-user redemptions |
| `currentUsage` | Counter of total uses |
| `validFrom` / `validUntil` | Validity date range |
| `applicableZones` | JSON — restrict to specific zones |
| `applicablePlans` | JSON — restrict to specific subscription plans |
| `isActive` | Enable/disable flag |

### 15.2 Business Rules
| Rule | Detail |
|------|--------|
| Duplicate prevention | Code must be unique (`existsByCode` check) |
| Deactivation | Sets `isActive = false` (soft disable) |
| Tenant scoped | Promo codes are tenant-specific |

---

## 16. Payments & Gateways

**Source:** `service/PaymentService.java`, domain enums

### 16.1 Payment Gateway Types
`STRIPE`, `TAP`, `PAYMOB`, `FAWRY`

### 16.2 Payment Types
`CHARGE`, `REFUND`

### 16.3 Payment Statuses
`PENDING`, `SUCCESS`, `FAILED`, `REFUNDED`

### 16.4 Refund Rule
- Can only refund transactions with `SUCCESS` status

---

## 17. Invoicing

**Source:** `domain/Invoice.java`

### 17.1 Invoice Statuses
`DRAFT`, `PENDING`, `PAID`, `OVERDUE`, `CANCELLED`, `REFUNDED`

### 17.2 Invoice Defaults
| Field | Default |
|-------|---------|
| `tax` | `BigDecimal.ZERO` |
| `status` | `DRAFT` (entity default) or `PENDING` (constructor) |
| `totalAmount` | `amount + tax` (on creation, `totalAmount = amount` since tax starts at 0) |

### 17.3 Invoice Number Format
Unique, up to 30 characters (format determined by caller service)

---

## 18. Fleet Management

**Source:** `service/FleetService.java`, `domain/Vehicle.java`, `domain/VehicleAssignment.java`, `domain/VehicleMaintenance.java`

### 18.1 Vehicle Types
`MOTORCYCLE`, `CAR`, `VAN`, `TRUCK`

### 18.2 Vehicle Statuses & Transitions
```
AVAILABLE → IN_USE (assigned)        → AVAILABLE (returned)
         → MAINTENANCE (scheduled)   → AVAILABLE (completed)
         → RETIRED (permanent)
```

### 18.3 Fleet Business Rules

| Rule | Detail |
|------|--------|
| Plate number | Unique — duplicate check on creation |
| Initial status | `AVAILABLE` |
| Cannot retire | Vehicle in `IN_USE` status |
| Vehicle assignment | Vehicle must be `AVAILABLE`; sets to `IN_USE` |
| Courier can only have one vehicle | `existsByCourierIdAndStatus(ACTIVE)` check — prevent duplicate active assignments |
| Vehicle return | Sets vehicle back to `AVAILABLE`, records end mileage |
| Return validation | Assignment must be `ACTIVE` status |
| Maintenance completion | Vehicle returns to `AVAILABLE` if it was in `MAINTENANCE` |

### 18.4 Vehicle Assignment Statuses
`ACTIVE`, `COMPLETED` (plus others defined in enum)

### 18.5 Maintenance Types & Statuses
**Statuses:** `SCHEDULED`, `COMPLETED` (plus others)

### 18.6 Fuel Logging
```
totalCost = liters × costPerLiter
```
- Auto-updates vehicle's `currentMileage` if `mileageAtFill` is provided

---

## 19. Zone Management

**Source:** `domain/Zone.java`, `config/DataInitializer.java`

### 19.1 Zone Statuses
`ZONE_ACTIVE`, `ZONE_INACTIVE`, `ZONE_MAINTENANCE`

### 19.2 Default Zones (Seeded)

| Zone | Default Fee |
|------|-----------|
| CAIRO | 50.00 EGP |
| GIZA | 50.00 EGP |
| ALEXANDRIA | 50.00 EGP |
| SHARQIA | 50.00 EGP |
| DAKAHLEIA | 50.00 EGP |

---

## 20. Gamification

**Source:** `service/GamificationService.java`, `domain/GamificationProfile.java`, `domain/Achievement.java`

### 20.1 Gamification Profile Defaults

| Field | Default |
|-------|---------|
| `currentLevel` | 1 |
| `totalXp` | 0 |
| `currentLevelXp` | 0 |
| `xpToNextLevel` | **100** |
| `currentStreak` | 0 |
| `longestStreak` | 0 |
| `totalDeliveries` | 0 |
| `perfectDeliveries` | 0 |
| `tier` | `"BRONZE"` |
| `monthlyXp` | 0 |
| `weeklyXp` | 0 |

### 20.2 XP & Leveling Formula
```
When XP is added:
  totalXp += xpAmount
  currentLevelXp += xpAmount
  monthlyXp += xpAmount
  weeklyXp += xpAmount

  WHILE currentLevelXp >= xpToNextLevel:
    currentLevelXp -= xpToNextLevel
    currentLevel += 1
    xpToNextLevel = xpToNextLevel × 1.5   (50% increase per level)
```

**Level progression example:**
| Level | XP Required |
|-------|------------|
| 1 → 2 | 100 |
| 2 → 3 | 150 |
| 3 → 4 | 225 |
| 4 → 5 | 337 |
| 5 → 6 | 506 |

### 20.3 Achievement Properties
| Property | Detail |
|----------|--------|
| `code` | Unique achievement identifier |
| `xpReward` | XP granted on completion (default: 0) |
| `rarity` | Default: `"COMMON"` |
| `category` | Grouping category |
| `criteria` | JSON criteria for auto-unlock |
| `sortOrder` | Display ordering |

### 20.4 Leaderboard
- Top 10 per tenant by totalXp
- Filtered by `period` and `periodKey` (e.g., "WEEKLY" / "2024-W05")
- Tracks: `rankPosition`, `score`, `deliveryCount`, `avgRating`, `xpEarned`

### 20.5 Duplicate Prevention
- One gamification profile per user (enforced by `existsByUserId` check)

---

## 21. Loyalty Programs

**Source:** `service/LoyaltyService.java`, `domain/LoyaltyProgram.java`, `domain/LoyaltyTransaction.java`

### 21.1 Loyalty Program Defaults

| Field | Default |
|-------|---------|
| `currentPoints` | 0 |
| `lifetimePoints` | 0 |
| `tier` | `"MEMBER"` |
| `pointsExpiring` | 0 |

### 21.2 Points Logic
```
On EARN transaction (points > 0):
  currentPoints += points
  lifetimePoints += points

On REDEEM transaction (points < 0):
  currentPoints += points  (negative, so it subtracts)
  // lifetimePoints not affected

lastActivityAt = now
balanceAfter = currentPoints  (stored on transaction)
```

### 21.3 Business Rules
| Rule | Detail |
|------|--------|
| One loyalty program per merchant | Enforced by `existsByMerchantId` check |
| Points have expiry | `pointsExpiringAt` and `pointsExpiring` fields |
| Tier has expiry | `tierExpiresAt` field |
| Tenant-scoped | All data is tenant-specific |

---

## 22. Support Tickets & SLA

**Source:** `domain/SupportTicket.java`, `domain/SlaPolicy.java`

### 22.1 Ticket Priorities
`LOW`, `MEDIUM`, `HIGH`, `URGENT`

### 22.2 Ticket Statuses
`OPEN`, `IN_PROGRESS`, `WAITING_ON_CUSTOMER`, `RESOLVED`, `CLOSED`

### 22.3 Ticket Categories
`SHIPMENT`, `PAYMENT`, `ACCOUNT`, `TECHNICAL`, `OTHER`

### 22.4 Ticket Defaults
| Field | Default |
|-------|---------|
| `priority` | `MEDIUM` |
| `status` | `OPEN` |
| `category` | `OTHER` |

### 22.5 SLA Policy
| Field | Description |
|-------|-------------|
| `firstResponseHours` | Max hours for first response by priority |
| `resolutionHours` | Max hours for resolution by priority |

### 22.6 Ticket Lifecycle Tracking
| Timestamp | Set When |
|-----------|----------|
| `firstResponseAt` | First agent response |
| `resolvedAt` | Status → `RESOLVED` |
| `closedAt` | Status → `CLOSED` |

---

## 23. Notifications

**Source:** `domain/NotificationType.java`, `domain/NotificationChannel.java`

### 23.1 Notification Types (17 total)
| Type | Description |
|------|-------------|
| `SHIPMENT_CREATED` | New shipment created |
| `SHIPMENT_STATUS_CHANGED` | Shipment status updated |
| `SHIPMENT_ASSIGNED` | Courier assigned |
| `SHIPMENT_DELIVERED` | Delivery confirmed |
| `SHIPMENT_FAILED` | Delivery failed |
| `PAYMENT_RECEIVED` | Payment received |
| `PAYMENT_SENT` | Payment sent |
| `WALLET_CREDIT` | Wallet credited |
| `WALLET_DEBIT` | Wallet debited |
| `RETURN_REQUESTED` | Return initiated |
| `RETURN_COMPLETED` | Return completed |
| `SUPPORT_TICKET_UPDATE` | Ticket update |
| `SYSTEM_ANNOUNCEMENT` | System-wide announcement |
| `ACCOUNT_ALERT` | Account security alert |
| `SUBSCRIPTION_ALERT` | Subscription event |
| `PROMOTION` | Promotional notification |
| `CUSTOM` | Custom notification |

### 23.2 Notification Channels (5)
`IN_APP`, `EMAIL`, `SMS`, `PUSH`, `WHATSAPP`

---

## 24. OTP & Verification

**Source:** `service/OtpService.java`, `application.yml`

### 24.1 OTP Rules

| Rule | Value |
|------|-------|
| Format | 6-digit random number (`%06d`) |
| Validity | **5 minutes** (configurable) |
| Max attempts | **5** (configurable) |
| Primary storage | Redis (with TTL = validity period) |
| Fallback storage | In-memory `ConcurrentHashMap` |
| Cleanup | Scheduled every **60 seconds** |
| Comparison method | Constant-time (`MessageDigest.isEqual`) — prevents timing attacks |

---

## 25. Rate Limiting

**Source:** `security/RateLimitFilter.java`, `domain/RateLimitPolicy.java`

### 25.1 Endpoint-Specific Rate Limits

| Endpoint | Max Requests | Window |
|----------|-------------|--------|
| `/api/auth/login` | **5** | 60 seconds |
| `/api/public/send-otp` | **3** | 60 seconds |
| `/api/public/forgot-password` | **3** | 60 seconds |
| `/api/public/reset-password` | **3** | 60 seconds |
| `/api/public/track/*` | **10** | 60 seconds |
| `/api/public/feedback` | **5** | 60 seconds |
| `/api/public/contact` | **5** | 60 seconds |

### 25.2 Rate Limit Implementation
| Rule | Detail |
|------|--------|
| Scope | Per IP address |
| IP extraction order | `X-Forwarded-For` → `X-Real-IP` → `remoteAddr` |
| Stale bucket eviction | Every **5 minutes** |
| Token bucket | Sliding window counter |

### 25.3 Default Rate Limit Policy
| Field | Default |
|-------|---------|
| `maxRequests` | 100 |
| `windowSeconds` | 60 |

---

## 26. IP Blocking

**Source:** `service/IpBlockingService.java`

### 26.1 Auto-Block Rules

| Rule | Value |
|------|-------|
| Auto-block duration | **24 hours** |
| Trigger | Brute force detection |
| Security event severity | `CRITICAL` |

### 26.2 Manual Block Rules

| Rule | Value |
|------|-------|
| Default duration | **24 hours** |
| Can be permanent | Yes |
| Security event severity | `HIGH` |

---

## 27. Input Sanitization

**Source:** `service/InputSanitizationService.java`

### 27.1 XSS Pattern Detection
Detected patterns (case-insensitive):
```
<script, </script, javascript:, onerror=, onload=, onclick=,
onmouseover=, onfocus=, onblur=, <iframe, <object, <embed,
<form, expression(, eval(, alert(, document.cookie, document.location
```

### 27.2 SQL Injection Pattern Detection
Detected patterns (case-insensitive):
```
' OR , '; DROP, 1=1, ' UNION, SELECT * FROM, INSERT INTO,
DELETE FROM, UPDATE SET, EXEC(, xp_cmdshell, sp_executesql
```

### 27.3 XSS Sanitization (HTML Entity Encoding)
| Character | Encoded As |
|-----------|-----------|
| `&` | `&amp;` |
| `<` | `&lt;` |
| `>` | `&gt;` |
| `"` | `&quot;` |
| `'` | `&#x27;` |

### 27.4 Input Safety Check
```
isSafe(input) = !containsXss(input) && !containsSqlInjection(input)
```

---

## 28. Password Policy

**Source:** `service/PasswordPolicyService.java`, `web/validation/PasswordValidator.java`

### 28.1 PasswordPolicyService (Strict — for password changes)

| Rule | Value |
|------|-------|
| `MIN_LENGTH` | 8 |
| `MAX_LENGTH` | 128 |
| Must contain uppercase | `[A-Z]` |
| Must contain lowercase | `[a-z]` |
| Must contain digit | `[0-9]` |
| Must contain special char | `!@#$%^&*()` etc. |
| Password history | Prevents reuse of previous passwords |

### 28.2 Common Password Blacklist
```
password, 123456, 12345678, qwerty, abc123,
password1, admin123, letmein, welcome, monkey
```

### 28.3 PasswordValidator (Annotation — for entity validation)

| Rule | Value |
|------|-------|
| `MIN_LENGTH` | 6 |
| Must contain ≥1 letter | Yes |
| Must contain ≥1 digit | Yes |

> ⚠️ **Two-tier password policy:** Entity validation (`@ValidPassword`) requires only 6 chars + letter + digit. The `PasswordPolicyService` enforces stricter rules (8+ chars, upper+lower+digit+special). Both may apply depending on the flow.

---

## 29. Multi-Tenancy & Quotas

**Source:** `domain/Tenant.java`, `domain/TenantQuota.java`

### 29.1 Tenant Statuses
`ACTIVE`, `SUSPENDED`, `TRIAL`, `CANCELLED`

### 29.2 Tenant Plans
`FREE`, `BASIC`, `PRO`, `ENTERPRISE`

### 29.3 Tenant Defaults
| Field | Default |
|-------|---------|
| `status` | `TRIAL` |
| `plan` | `FREE` |
| `tenantId` | `UUID.randomUUID().toString()` (auto-generated) |

### 29.4 Tenant Quota Types
| Quota | Description |
|-------|-------------|
| `MAX_SHIPMENTS_MONTHLY` | Monthly shipment limit |
| `MAX_USERS` | Maximum users in tenant |
| `MAX_API_CALLS` | API call limit |
| `MAX_STORAGE_MB` | Storage limit in MB |
| `MAX_WEBHOOKS` | Maximum webhook integrations |

### 29.5 Quota Reset Periods
`MONTHLY`, `DAILY`, `NEVER`

### 29.6 Quota Exceeded Check
```
isExceeded() = (currentValue >= maxValue)
```

### 29.7 Unique Constraint
One quota per type per tenant: unique on `(tenant_id, quota_type)`

---

## 30. Feature Flags

**Source:** `domain/FeatureFlag.java`

### 30.1 Feature Flag Properties

| Property | Description |
|----------|-------------|
| `rolloutPercentage` | 0–100 — percentage of users who see the feature |
| `targetRoles` | JSON array — restrict to specific roles |
| `targetTenants` | JSON array — restrict to specific tenants |
| `startDate` / `endDate` | Time-bounded feature availability |
| `isActive` | Master enable/disable switch |

---

## 31. Scheduled Tasks & Cron Jobs

**Source:** Various service files, `application.yml`

| Task | Cron Expression | Timezone | Source |
|------|----------------|----------|--------|
| Backup | `0 0 2 * * ?` (2:00 AM daily) | — | `application.yml` |
| Subscription expiry processing | Daily at 2:00 AM | — | `SubscriptionService.java` |
| Contract auto-renewal | `0 0 1 * * *` (1:00 AM daily) | Africa/Cairo | `ContractService.java` |
| Contract expiry reminders | `0 0 8 * * *` (8:00 AM daily) | Africa/Cairo | `ContractService.java` |
| OTP cleanup | Every 60 seconds | — | `OtpService.java` |
| Rate limit bucket eviction | Every 5 minutes | — | `RateLimitFilter.java` |

---

## 32. System Configuration Defaults

**Source:** `application.yml`

### 32.1 Server Configuration

| Setting | Value |
|---------|-------|
| Server port | `8000` |
| Tomcat max-threads | 200 |
| Tomcat min-spare-threads | 20 |
| Tomcat accept-count | 100 |
| Database | MySQL 8 at `localhost:3306/twsela` |
| Hibernate ddl-auto | `validate` |
| Flyway migrations | Enabled |

### 32.2 Cache Configuration

| Setting | Value |
|---------|-------|
| Redis host | `localhost:6379` |
| Redis pool max-active | 16 |
| Cache TTL | 1,800,000 ms (30 minutes) |

### 32.3 SMS Configuration

| Setting | Value |
|---------|-------|
| Provider | Twilio |
| Retry attempts | 3 |
| Timeout | 30,000 ms |

### 32.4 Dashboard Defaults

| Setting | Value |
|---------|-------|
| Courier earnings days | 30 |
| Chart days | 7 |

### 32.5 Logging

| Setting | Value |
|---------|-------|
| Max log file size | 100 MB |
| Max log history | 30 days |

### 32.6 Actuator / Monitoring

| Setting | Value |
|---------|-------|
| Percentiles | 0.5, 0.95, 0.99 |
| SLO targets | 50ms, 100ms, 200ms, 500ms |

### 32.7 Contact / Inquiry

| Setting | Value |
|---------|-------|
| Inquiry prefix | `"INQ-"` |
| Office hours | الأحد - الخميس: 8:00 ص - 6:00 م (Sun–Thu, 8AM–6PM) |

### 32.8 Password Generation (auto-generated passwords)

| Setting | Value |
|---------|-------|
| Length | 12 characters |
| Excluded ambiguous chars | `I, l, O, 0, 1` |

---

## 33. Seed Data & Initialization

**Source:** `config/DataInitializer.java`

### 33.1 Seeded Roles
`OWNER`, `ADMIN`, `MERCHANT`, `COURIER`, `WAREHOUSE_MANAGER`

### 33.2 Seeded User Statuses
`ACTIVE`, `INACTIVE`, `SUSPENDED`, `PENDING_VERIFICATION`

### 33.3 Seeded Payout Statuses
`PENDING`, `PROCESSED`, `FAILED`, `CANCELLED`

### 33.4 Seeded Zones (all with defaultFee = 50.00 EGP)
`CAIRO`, `GIZA`, `ALEXANDRIA`, `SHARQIA`, `DAKAHLEIA`

### 33.5 Telemetry / System Settings

| Setting Key | Value |
|-------------|-------|
| `DEFAULT_SYSTEM_FEE` | 50.00 |
| `MAX_WEIGHT_PER_SHIPMENT` | 50.0 (kg) |
| `DEFAULT_PRIORITY` | STANDARD |
| `AUTO_ASSIGN_COURIER` | false |
| `SMS_ENABLED` | true |
| `EMAIL_ENABLED` | false |

### 33.6 Default Users

| User | Phone | Default Password | Role |
|------|-------|-----------------|------|
| Owner | From env var (fallback: `01000000000`) | `ChangeMe@2024!` | OWNER |
| Admin | `01000000001` | `ChangeMe@2024!` | ADMIN |

---

## Appendix A: Complete Enum Reference

### Domain Enums

| Enum | Values | Source File |
|------|--------|-------------|
| ShipmentStatus (21) | PENDING, PENDING_APPROVAL, APPROVED, PICKED_UP, RECEIVED_AT_HUB, READY_FOR_DISPATCH, ASSIGNED_TO_COURIER, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, FAILED_DELIVERY, FAILED_ATTEMPT, POSTPONED, PENDING_UPDATE, PENDING_RETURN, RETURNED_TO_HUB, RETURNED_TO_ORIGIN, CANCELLED, ON_HOLD, RESCHEDULED, PARTIALLY_DELIVERED | `ShipmentStatusConstants.java` |
| ShippingFeePaidBy | MERCHANT, RECIPIENT, PREPAID | `Shipment.java` |
| SourceType | MERCHANT, THIRD_PARTY_LOGISTICS_PARTNER("3PL_PARTNER") | `Shipment.java` |
| PodType | OTP, PHOTO, SIGNATURE | `Shipment.java` |
| WalletType | MERCHANT, COURIER, COMPANY | `Wallet.java` |
| TransactionType | CREDIT, DEBIT | `WalletTransaction.java` |
| TransactionReason | COD_COLLECTED, DELIVERY_FEE, COMMISSION, WITHDRAWAL, SETTLEMENT, RETURN_FEE, ADJUSTMENT | `WalletTransaction.java` |
| ManifestStatus | CREATED, IN_PROGRESS, COMPLETED, CANCELLED | `ShipmentManifest.java` |
| ZoneStatus | ZONE_ACTIVE, ZONE_INACTIVE, ZONE_MAINTENANCE | `Zone.java` |
| ReturnStatus | RETURN_REQUESTED, RETURN_APPROVED, RETURN_REJECTED, RETURN_PICKUP_ASSIGNED, RETURN_PICKED_UP, RETURN_IN_WAREHOUSE, RETURN_DELIVERED_TO_MERCHANT | `ReturnShipment.java` |
| ContractType | MERCHANT_AGREEMENT, COURIER_AGREEMENT, PARTNER_AGREEMENT | `Contract.java` |
| ContractStatus | DRAFT, PENDING_SIGNATURE, ACTIVE, EXPIRING_SOON, EXPIRED, TERMINATED | `Contract.java` |
| SubscriptionPlan.PlanName | FREE, BASIC, PRO, ENTERPRISE | `SubscriptionPlan.java` |
| SubscriptionStatus | TRIAL, ACTIVE, PAST_DUE, EXPIRED, CANCELLED | `MerchantSubscription.java` |
| BillingCycle | MONTHLY, ANNUAL | `MerchantSubscription.java` |
| SettlementPeriod | DAILY, WEEKLY, BIWEEKLY, MONTHLY | `SettlementBatch.java` |
| SettlementBatchStatus | DRAFT, PENDING, PROCESSING, COMPLETED, FAILED | `SettlementBatch.java` |
| PayoutStatus | PENDING, PROCESSED, FAILED, CANCELLED | `Payout.java` |
| PaymentGatewayType | STRIPE, TAP, PAYMOB, FAWRY | Payment service |
| PaymentType | CHARGE, REFUND | Payment service |
| PaymentStatus | PENDING, SUCCESS, FAILED, REFUNDED | Payment service |
| InvoiceStatus | DRAFT, PENDING, PAID, OVERDUE, CANCELLED, REFUNDED | `Invoice.java` |
| TaxType | VAT, SALES_TAX, CUSTOMS | `TaxRule.java` |
| VehicleType | MOTORCYCLE, CAR, VAN, TRUCK | `Vehicle.java` |
| VehicleStatus | AVAILABLE, IN_USE, MAINTENANCE, RETIRED | `Vehicle.java` |
| AssignmentStatus | ACTIVE, COMPLETED (+ others) | `VehicleAssignment.java` |
| MaintenanceStatus | SCHEDULED, COMPLETED (+ others) | `VehicleMaintenance.java` |
| CourierVehicleType | BICYCLE, CAR, MOTORCYCLE, VAN | `CourierDetails.java` |
| TenantStatus | ACTIVE, SUSPENDED, TRIAL, CANCELLED | `Tenant.java` |
| TenantPlan | FREE, BASIC, PRO, ENTERPRISE | `Tenant.java` |
| QuotaType | MAX_SHIPMENTS_MONTHLY, MAX_USERS, MAX_API_CALLS, MAX_STORAGE_MB, MAX_WEBHOOKS | `TenantQuota.java` |
| ResetPeriod | MONTHLY, DAILY, NEVER | `TenantQuota.java` |
| AttemptStatus | FAILED, SUCCESS | `DeliveryAttempt.java` |
| FailureReason | CUSTOMER_ABSENT, WRONG_ADDRESS, REFUSED, PHONE_OFF, DAMAGED, OTHER | `DeliveryAttempt.java` |
| TicketPriority | LOW, MEDIUM, HIGH, URGENT | `SupportTicket.java` |
| TicketStatus | OPEN, IN_PROGRESS, WAITING_ON_CUSTOMER, RESOLVED, CLOSED | `SupportTicket.java` |
| TicketCategory | SHIPMENT, PAYMENT, ACCOUNT, TECHNICAL, OTHER | `SupportTicket.java` |
| NotificationType (17) | SHIPMENT_CREATED, SHIPMENT_STATUS_CHANGED, SHIPMENT_ASSIGNED, SHIPMENT_DELIVERED, SHIPMENT_FAILED, PAYMENT_RECEIVED, PAYMENT_SENT, WALLET_CREDIT, WALLET_DEBIT, RETURN_REQUESTED, RETURN_COMPLETED, SUPPORT_TICKET_UPDATE, SYSTEM_ANNOUNCEMENT, ACCOUNT_ALERT, SUBSCRIPTION_ALERT, PROMOTION, CUSTOM | `NotificationType.java` |
| NotificationChannel (5) | IN_APP, EMAIL, SMS, PUSH, WHATSAPP | `NotificationChannel.java` |
| ChatMessage.MessageType | TEXT, IMAGE, LOCATION, SYSTEM | `ChatMessage.java` |
| ChatRoom.RoomType | MERCHANT_COURIER, RECIPIENT_COURIER, SUPPORT | `ChatRoom.java` |
| SecurityEvent severity levels | HIGH, CRITICAL (+ others) | `SecurityEvent.java` |

---

## Appendix B: Number Format Patterns

| Pattern | Format | Example |
|---------|--------|---------|
| Tracking number | `TWS-` + 8-char UUID (upper) | `TWS-A1B2C3D4` |
| Manifest number | `MAN-` + 8-char UUID (upper) | `MAN-E5F6G7H8` |
| Settlement number | `STL-` + 8-char UUID (upper) | `STL-I9J0K1L2` |
| Contract number | `TWS-CTR-` + 8-char UUID (upper) | `TWS-CTR-M3N4O5P6` |
| Inquiry prefix | `INQ-` | `INQ-...` |

---

## Appendix C: Key Business Constants Summary

| Constant | Value | Source |
|----------|-------|--------|
| Default delivery fee | 50.00 EGP | `DataInitializer` / `ShipmentService` |
| Default system fee | 50.00 EGP | `DataInitializer` |
| Max weight per shipment | 50.0 kg | `DataInitializer` |
| Express multiplier | 1.5× | `ShipmentService` |
| Economy multiplier | 0.8× | `ShipmentService` |
| Return fee rate | 50% | `ReturnService` |
| Settlement fee | 2.5% | `SettlementService` |
| Custom pricing base | 25.00 EGP | `CustomPricingService` |
| Custom pricing per-kg | 2.00 EGP | `CustomPricingService` |
| Custom pricing COD fee | 2% | `CustomPricingService` |
| JWT expiry | 24 hours | `application.yml` |
| OTP validity | 5 minutes | `application.yml` |
| OTP max attempts | 5 | `application.yml` |
| OTP digits | 6 | `OtpService` |
| Account lock threshold | 5 failed attempts | `AuthController` |
| Account lock duration | 15 min (AuthController) / 30 min (Service) | See note |
| Trial period | 14 days | `SubscriptionService` |
| Monthly billing cycle | 30 days | `SubscriptionService` |
| Annual billing cycle | 365 days | `SubscriptionService` |
| Free plan shipment limit | 50/month | `SubscriptionService` |
| Smart assign max load | 30 shipments | `SmartAssignmentService` |
| Smart assign max distance | 50 km | `SmartAssignmentService` |
| Smart assign min rating | 3.0 / 5.0 | `SmartAssignmentService` |
| XP to level 2 | 100 XP | `GamificationProfile` |
| XP growth factor | 1.5× per level | `GamificationService` |
| Initial gamification tier | BRONZE | `GamificationProfile` |
| Initial loyalty tier | MEMBER | `LoyaltyProgram` |
| IP auto-block duration | 24 hours | `IpBlockingService` |
| Backup retention | 30 days | `application.yml` |
| Redis cache TTL | 30 minutes | `application.yml` |
| Contract renewal notice | 30 days default | `Contract.java` |
| Contract expiry reminders | 30, 14, 7 days | `ContractService` |
| Password min (strict) | 8 chars | `PasswordPolicyService` |
| Password min (entity) | 6 chars | `PasswordValidator` |
| Password max | 128 chars | `PasswordPolicyService` |
| Phone format | 10–15 digits | `User.java` |
| Name length | 2–100 chars | `User.java` |
| SMS retry attempts | 3 | `application.yml` |
| SMS timeout | 30 seconds | `application.yml` |
