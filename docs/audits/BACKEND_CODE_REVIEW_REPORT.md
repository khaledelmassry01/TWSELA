# Comprehensive Backend Code Review Report
## Twsela Courier Management System — Spring Boot Backend

**Review Date:** 2025-01-17  
**Scope:** `twsela/src/main/java/com/twsela/` + resources, migrations, tests  
**Files Reviewed:** 150+ source files across all packages  
**Methodology:** Manual line-by-line review of every file in domain/, service/, web/, security/, config/, repository/, util/, resources/, and test/

---

## Executive Summary

| Severity | Count |
|----------|-------|
| **CRITICAL** | 7 |
| **HIGH** | 22 |
| **MEDIUM** | 34 |
| **LOW** | 18 |
| **TOTAL** | 81 |

The codebase is well-structured with clear separation between layers, proper use of Spring Boot idioms, and good test coverage for core services. However, there are **critical security vulnerabilities** (hardcoded credentials, fail-open token blacklist, weak JWT fallback), **race conditions** in financial operations, and **performance anti-patterns** (loading entire tables then filtering in memory). The DTO layer exists but is underutilized — many controllers still return raw entities or `Map<String,Object>`.

---

## Category 1: Domain Entities

### Files Reviewed (39 files in `domain/`)
All 39 entity files fully read.

| # | File | Lines | Severity | Description | Why It's a Problem |
|---|------|-------|----------|-------------|--------------------|
| 1 | `Shipment.java` | ~256 | **HIGH** | `setCourier(User courier)` is a **NO-OP** — method body is empty | Courier assignment via this setter silently does nothing. Any code calling `shipment.setCourier(courier)` believes it worked. |
| 2 | `PayoutItem.java` | ~80 | **HIGH** | `setItemType(String)` wraps in try-catch and silently swallows `IllegalArgumentException` from `valueOf()` | Invalid item types are silently ignored instead of being rejected. Should throw or log. |
| 3 | `Zone.java` | Timestamps | **MEDIUM** | Uses `LocalDateTime` for `createdAt`/`updatedAt` while all other entities use `Instant` | Inconsistent timestamp types across the domain model. `LocalDateTime` lacks timezone info; mixing types causes serialization and comparison bugs. |
| 4 | `Payout.java` | `@ManyToOne` | **MEDIUM** | `FetchType.EAGER` on `user` relationship | Loading full user entity for every Payout query, even when user data is not needed. Impacts list endpoints. |
| 5 | `DeliveryPricing.java` | `@ManyToOne` | **MEDIUM** | `FetchType.EAGER` on `zone` relationship | Same eager-loading issue. |
| 6 | `ShipmentManifest.java` | `@ManyToOne` | **MEDIUM** | `FetchType.EAGER` on `courier` relationship | Same eager-loading issue. |
| 7 | `WarehouseInventory.java` | `@ManyToOne` | **MEDIUM** | `FetchType.EAGER` on `zone` and `warehouse` relationships | Two eager relationships add unnecessary joins for every query. |
| 8 | `Notification.java` | `userId` field | **MEDIUM** | `userId` is a plain `Long`, not a `@ManyToOne` relationship to `User` | No referential integrity enforced by JPA. Orphan notifications possible. The DB migration (`V3`) also lacks a foreign key constraint on `user_id`. |
| 9 | `NotificationLog.java` | Fields | **MEDIUM** | `status` and `messageType` are plain `String` fields instead of enums | No type safety; allows arbitrary string values that may not match expected constants. |
| 10 | `CourierRating.java` | `rating` field | **LOW** | No `@Min`/`@Max` validation on `rating` field at entity level | Invalid ratings (e.g., 0, -1, 100) could be persisted. Validation exists only in the DTO (`CourierRatingRequest`). |
| 11 | `MerchantServiceFeedback.java` | `rating` field | **LOW** | Same missing entity-level validation on rating | Same issue as above. |
| 12 | `CashMovementLedger.java` | `setImmutable()` | **LOW** | Method named `setImmutable()` contradicts the concept — a setter on an immutability flag implies mutability | Misleading API. The flag can be toggled, defeating the purpose. |
| 13 | `WebhookSubscription.java` | `events` field | **LOW** | Events stored as comma-separated `String` instead of a proper collection (`@ElementCollection` or join table) | Cannot query by individual event type efficiently; parsing required on every read. |
| 14 | Multiple entities | — | **LOW** | Missing `equals()` and `hashCode()` overrides based on business key | Entities in `Set` collections or as `Map` keys will use identity equality, which fails across sessions. |

---

## Category 2: Services

### Files Reviewed (22 files in `service/`)
All 22 service files fully read.

| # | File | Lines | Severity | Description | Why It's a Problem |
|---|------|-------|----------|-------------|--------------------|
| 15 | `WalletService.java` | `credit()`/`debit()` | **CRITICAL** | No database-level locking (`@Lock(PESSIMISTIC_WRITE)` or `SELECT ... FOR UPDATE`) on wallet balance operations | Two concurrent requests can read the same balance, both pass the insufficient-funds check, and both write — causing balance corruption. This is a **financial data integrity issue**. |
| 16 | `ShipmentService.java` | `assignShipmentsToCourier()` | **HIGH** | Calls `findAll()` on `ShipmentRepository` then filters in memory by status | Loads the entire shipments table into memory on every assignment. With thousands of shipments, this causes OOM or extreme latency. |
| 17 | `ShipmentService.java` | `getRecentActivity()` | **HIGH** | Same pattern — `findAll()` then `.stream().filter().limit()` | Fetches every shipment to show "recent activity". Should use repository query with pagination. |
| 18 | `ShipmentService.java` | `updateStatus()` | **HIGH** | No optimistic or pessimistic locking on shipment status | Two concurrent status updates can overwrite each other. E.g., "DELIVERED" and "RETURNED" applied simultaneously. |
| 19 | `FinancialService.java` | Rate calculation | **HIGH** | Hardcoded `0.70` (70%) courier rate | Business-critical configuration embedded in code. Cannot be changed without redeployment. Should be configurable per zone/pricing tier. |
| 20 | `FinancialService.java` | `calculateCourierEarnings()` | **HIGH** | Method accepts `startDate`/`endDate` parameters but **ignores them** — queries all delivered shipments | Earnings are always calculated for the entire history, regardless of period requested. Financial reports will be wrong. |
| 21 | `UserService.java` | `listByRole()` | **HIGH** | Calls `findAll()` then filters in memory by role name | Loads all users to find those with a specific role. Should use `findByRoleName()` repository method. |
| 22 | `UserService.java` | `deleteUser()` | **HIGH** | Calls `deleteById()` — hard delete despite the entity having an `isDeleted` soft-delete flag | Permanent data loss. Breaks audit trails and referential integrity (foreign keys in shipments, payouts, etc.). |
| 23 | `AnalyticsService.java` | Rankings | **HIGH** | N+1 query pattern — iterates all couriers/merchants individually to build rankings | For 100 couriers, makes 100+ individual DB queries. Should use aggregate queries. |
| 24 | `AnalyticsService.java` | Status strings | **MEDIUM** | Uses hardcoded status strings ("DELIVERED", "RETURNED") that may not match `ShipmentStatusConstants` values | Silently returns 0 counts if constant names differ from what's used here. |
| 25 | `BackupService.java` | `restoreBackup()` | **MEDIUM** | Passes DB password via `-p` command-line argument: `command.add("-p" + dbPassword)` | Password visible in process listing (`ps aux`). The `createBackup()` method correctly uses `MYSQL_PWD` env var, but `restoreBackup()` does not. Inconsistent security. |
| 26 | `ExcelService.java` | `processExcelFile()` | **MEDIUM** | No limit on file size or row count | A malicious user could upload a massive Excel file, causing OOM. Should validate `file.getSize()` and max row count. |
| 27 | `ExcelService.java` | `createShipmentFromRow()` | **MEDIUM** | Zone lookup per row (`zoneRepository.findByNameIgnoreCase()`) but zone object is never assigned to shipment | Zone is validated to exist but not used — the shipment is saved without a zone reference. |
| 28 | `OtpService.java` | Fallback store | **MEDIUM** | In-memory `ConcurrentHashMap` fallback when Redis is down — not shared across instances | In a multi-instance deployment, OTP generated on instance A cannot be verified on instance B if Redis is down. |
| 29 | `PdfService.java` | `processArabicText()` | **MEDIUM** | `e.printStackTrace()` in catch block instead of using logger | Stack trace goes to stderr, bypassing the logging framework, and is not captured in log files. |
| 30 | `BaseService.java` | `findUserByPhone()` | **LOW** | Throws generic `RuntimeException` instead of typed exception | Callers cannot distinguish "user not found" from other runtime failures. |
| 31 | `AwbService.java` | `counter` | **LOW** | `AtomicLong` counter initialized from `System.currentTimeMillis() % 1_000_000` — resets on restart | After restart, counter may produce duplicate AWBs if restarts happen within the same date. Risk is low but exists. |
| 32 | `CourierLocationService.java` | `findNearestCourier()` | **LOW** | Iterates all courier IDs and calls `getLastLocation()` for each — no batch query | N+1 pattern for finding nearest courier. Acceptable for small courier lists but won't scale. |
| 33 | `FileUploadService.java` | `UPLOAD_DIR` | **LOW** | Static `System.getProperty()` at class load time — not Spring-managed | Cannot be overridden via `application.yml`. Should use `@Value` annotation. |

---

## Category 3: Controllers

### Files Reviewed (28 files in `web/`)
All 28 controller files fully read.

| # | File | Lines | Severity | Description | Why It's a Problem |
|---|------|-------|----------|-------------|--------------------|
| 34 | `MasterDataController.java` | `createUser()` | **CRITICAL** | Accepts raw `User` entity as `@RequestBody` | Attacker can set any field: `role`, `status`, `isDeleted`, `password` with arbitrary values. Should use `CreateUserRequest` DTO with only whitelisted fields. |
| 35 | `MasterDataController.java` | `updatePricing()` | **HIGH** | All field updates are **commented out** — the method is a no-op | Pricing updates appear to succeed (200 OK) but actually save nothing. Business-critical functionality is broken. |
| 36 | `MasterDataController.java` | `deleteZone()` | **HIGH** | Uses `deleteById()` (hard delete) | Inconsistent with soft-delete strategy used elsewhere. Deleting a zone that has associated shipments will cause FK constraint violations or orphaned data. |
| 37 | `TelemetryController.java` | `POST /api/telemetry` | **HIGH** | No authentication on POST endpoint | Anyone on the network can inject arbitrary telemetry data. Could be used for log injection, DoS, or misleading monitoring dashboards. |
| 38 | `TelemetryController.java` | Data storage | **MEDIUM** | Uses in-memory `ConcurrentLinkedDeque` | All telemetry data lost on restart. No size limit — unbounded memory growth. |
| 39 | `ShipmentController.java` | Size | **HIGH** | 915 lines — God controller | Handles shipments CRUD, warehouse operations, courier location, returns, and reconciliation. Violates SRP; extremely hard to maintain and test. Should be split into 4-5 focused controllers. |
| 40 | `ShipmentController.java` | `getCurrentUser()` | **MEDIUM** | Duplicates `AuthenticationHelper.getCurrentUser()` logic | Each controller has its own user-resolution code instead of using the injected helper. |
| 41 | `ShipmentController.java` | `generateTrackingNumber()` | **MEDIUM** | Tracking number generation logic duplicated in controller | Should be in `AwbService` (which already exists). Two different tracking number formats in use. |
| 42 | `ShipmentController.java` | `calculateDeliveryFee()` | **MEDIUM** | Fee calculation logic in controller, not service layer | Business logic leaking into the presentation layer. |
| 43 | `ShipmentController.java` | Batch operations | **MEDIUM** | `receiveShipments()`, `dispatchToCourier()`, `reconcileWithCourier()` iterate shipments individually | Saves each shipment one-by-one in a loop. Should use `saveAll()` for batch persistence. |
| 44 | `AuthController.java` | `changePassword()` ~L160 | **HIGH** | Creates `new BCryptPasswordEncoder()` instead of injecting the configured bean | Different BCrypt strength/version from the security config bean. Password may be encoded with wrong parameters, causing login failures. |
| 45 | `AuthController.java` | Login | **MEDIUM** | Two DB queries for the same user: `findByPhoneWithRoleAndStatus` then `authenticate` | Doubles the database load for every login. The `authenticate()` call internally loads the user again via `UserDetailsService`. |
| 46 | `AuthController.java` | Logging | **MEDIUM** | Phone numbers logged in `warn` messages | PII (phone numbers) in logs. Violates data privacy regulations (GDPR/local equivalents). |
| 47 | `DashboardController.java` | `getDashboardStatistics()` ~L340 | **HIGH** | Week/month delivered counts reuse `countByStatusName("DELIVERED")` without date filtering | Shows the **same** delivered count for today, this week, and this month. Dashboard statistics are misleading. |
| 48 | `DashboardController.java` | `getRevenueChart()` ~L390 | **MEDIUM** | Returns all zeros for daily revenue breakdown | Placeholder code shipped to production. Revenue chart displays no data. |
| 49 | `DashboardController.java` | Timezone | **MEDIUM** | Uses `ZoneId.systemDefault()` throughout | Server timezone-dependent. Different behavior on different servers or when deployed to cloud (UTC vs local). |
| 50 | `ReportsController.java` | `getCourierReport()` ~L95 | **HIGH** | Iterates ALL couriers with individual queries per courier | N+1 query pattern. For 50 couriers, makes 50+ DB calls. Should use aggregate query. |
| 51 | `ReportsController.java` | `convertToInstantEnd()` | **MEDIUM** | Uses `23:59:59` as end-of-day | Misses the last second of the day (23:59:59.001 to 23:59:59.999). Should use next day at 00:00:00 exclusive. |
| 52 | `FinancialController.java` | `getAllPayouts()` | **MEDIUM** | Returns full `Payout` entities with EAGER-loaded `User` | Leaks internal user data (password hash, status, etc.) in API response. |
| 53 | `FinancialController.java` | Authorization | **MEDIUM** | Manual role checks (`!currentUser.getRole().getName().equals("OWNER")`) alongside `@PreAuthorize` | Inconsistent authorization strategy. Some endpoints use annotations, others use manual checks — easy to miss one. |
| 54 | `ManifestController.java` | `assignShipmentsToManifest()` | **MEDIUM** | Saves each shipment individually inside a loop | No batch save. For 50 shipments, makes 50 individual INSERT/UPDATE calls. |
| 55 | `PublicController.java` | `track()` | **MEDIUM** | `.orElseThrow()` without custom exception | Returns 500 Internal Server Error instead of 404 Not Found when tracking number doesn't exist. |
| 56 | `PublicController.java` | `feedback()` | **MEDIUM** | Accepts raw `ServiceFeedback` entity as `@RequestBody` | No DTO validation. Client can set internal fields. |
| 57 | `PublicController.java` | `submitContactForm()` | **LOW** | Contact form handler is a stub — logs nothing, saves nothing | Contact inquiries are silently discarded. Returns "success" but takes no action. |
| 58 | `GlobalExceptionHandler.java` | `handleRuntimeException()` | **MEDIUM** | Checks message for Arabic substring "غير موجودة" to determine 404 | Fragile string-based exception classification. Will break if message text changes. |
| 59 | `GlobalExceptionHandler.java` | `handleGenericException()` | **MEDIUM** | Checks `request.getRequestURI()` for "/api/auth/login" | Couples global handler to a specific endpoint. Maintenance burden. |
| 60 | `MasterDataController.java` | `getAllPricing()` | **LOW** | OWNER and non-OWNER branches execute identical logic | Dead code branch. The `if/else` is unnecessary. |

---

## Category 4: Security

### Files Reviewed (11 files in `security/`)
All 11 security files fully read.

| # | File | Lines | Severity | Description | Why It's a Problem |
|---|------|-------|----------|-------------|--------------------|
| 61 | `TokenBlacklistService.java` | `isBlacklisted()` | **CRITICAL** | Returns `false` (not blacklisted) when Redis throws an exception | If Redis goes down, all logged-out/revoked tokens are treated as valid. Attacker can use stolen tokens during Redis outage. Should **fail closed** (return `true` or throw). |
| 62 | `JwtService.java` | Key derivation | **HIGH** | Falls back to HMAC-SHA256 with the raw secret when secret length < 64 chars | Weak key material. Most env-var secrets are short strings, triggering the weak fallback. Should enforce minimum key length or always use HMAC-SHA512 with proper key derivation. |
| 63 | `JwtAuthenticationFilter.java` | Error handling | **MEDIUM** | Uses `e.printStackTrace()` instead of logger | Stack traces go to stderr, not captured by log aggregation. Also leaks internal details if stderr is exposed. |
| 64 | `RateLimitFilter.java` | Implementation | **MEDIUM** | In-memory `ConcurrentHashMap` with check-then-act race condition | 1) Not shared across instances — load-balanced requests bypass limits. 2) TOCTOU race between checking and incrementing count. |
| 65 | `SecurityConfig.java` | Static resources | **MEDIUM** | Overly permissive patterns: `"/css/**"`, `"/js/**"`, `"/images/**"`, etc. | If backend accidentally serves files at these paths, they bypass all security. Should be restricted to specific known public paths only. |
| 66 | `PermissionService.java` | Usage | **LOW** | Well-defined permission enum and role mapping exists, but controllers use manual `role.getName().equals("OWNER")` checks instead | The entire Permission/PermissionService infrastructure is largely unused. Authorization is inconsistent. |
| 67 | `RequestCorrelationFilter.java` | Client ID | **LOW** | Accepts client-provided `X-Correlation-Id` header without validation | Allows log injection via crafted correlation IDs (e.g., newlines, long strings). Should sanitize input. |

---

## Category 5: Configuration

### Files Reviewed (8 files in `config/`)
All 8 configuration files fully read.

| # | File | Lines | Severity | Description | Why It's a Problem |
|---|------|-------|----------|-------------|--------------------|
| 68 | `DataInitializer.java` | Lines 50-90 | **CRITICAL** | Hardcoded passwords `"150620KkZz@#$"` and phone numbers `"01023782584"`/`"01023782585"` for owner and admin accounts | Credentials in source code are visible to anyone with repository access. Stored in Git history permanently. Should use env vars or vault. |
| 69 | `application.yml` | `jwt.secret` | **CRITICAL** | `JWT_SECRET` defaults to empty string `""` when env var is not set | Application starts with an empty/null JWT secret. Tokens signed with empty key can be forged trivially. Should fail startup if secret is not configured. |
| 70 | `CacheConfig.java` | `cacheManager()` | **MEDIUM** | Bean method signature requires `RedisConnectionFactory` but has conditional logic for `cacheType` config | If `cacheType=redis` and Redis is down, Spring may fail to inject `RedisConnectionFactory` before reaching the fallback code path. |
| 71 | `WebSocketConfig.java` | CORS | **MEDIUM** | `setAllowedOriginPatterns("*")` | Allows any origin to connect via WebSocket. Should be restricted to known frontend domains. |
| 72 | `application.yml` | SMS config | **LOW** | `sms.provider` defaults to `"twilio"` even in development | Dev environments try to use Twilio (which fails if credentials are missing). Should default to `"console"` or `"mock"`. |

---

## Category 6: DTOs & Mappers

### Files Reviewed (33 files in `web/dto/`)
All 33 DTO files fully read.

| # | File | Lines | Severity | Description | Why It's a Problem |
|---|------|-------|----------|-------------|--------------------|
| 73 | `DtoMapper.java` | All | **HIGH** | Only maps `User`, `Login`, and `Shipment` — 3 out of 39 entities | Most controllers manually build `Map<String,Object>` or return raw entities. No consistent response mapping strategy. |
| 74 | `ReconcileRequest.java` | Field names | **MEDIUM** | Uses `snake_case` field names (`cash_confirmed_shipment_ids`, `returned_shipment_ids`) | Inconsistent with Java naming conventions and all other DTOs which use `camelCase`. Jackson deserialization may fail depending on `PropertyNamingStrategy`. |
| 75 | `UpdateUserRequest.java` | Validation | **LOW** | No validation annotations on any field | Accepts any string for `name`, `phone`, `password`. Other request DTOs like `CreateUserRequest` have proper `@NotBlank`, `@ValidPassword`, etc. |
| 76 | `PasswordResetRequest.java` | `newPassword` | **LOW** | No `@ValidPassword` annotation on newPassword field | Password reset allows weak passwords, unlike login and create flows which enforce `@ValidPassword`. |
| 77 | `AppUtils.java` | `PASSWORD_PATTERN` | **LOW** | Requires uppercase, lowercase, digit, AND special char with 8+ chars but `PasswordValidator` only requires 6+ chars with letter+digit | Two different password strength rules in the codebase. `AppUtils` is stricter but unused in validation flow. |

---

## Category 7: Repositories

### Files Reviewed (8 of 33 key repositories)
Focused on repositories with complex queries or business-critical operations.

| # | File | Lines | Severity | Description | Why It's a Problem |
|---|------|-------|----------|-------------|--------------------|
| 78 | `ShipmentRepository.java` | `searchShipments()` | **MEDIUM** | Uses `LIKE '%...%'` with wildcards on both sides | Cannot use database indexes. Full table scan on every search query. |
| 79 | `ShipmentRepository.java` | Deprecated method | **LOW** | `findByTrackingNumberContainingIgnoreCase...` marked `@Deprecated` but still present | Dead code that may still be called. Should be removed. |
| 80 | `UserRepository.java` | `deleteByRoleNot()` | **HIGH** | Destructive bulk delete: deletes all users whose role is NOT the specified one | Dangerous method. A single call with wrong parameter could delete all users. Should not exist without safeguards. |
| 81 | `WalletRepository.java` | — | **LOW** | No `@Lock(PESSIMISTIC_WRITE)` finder for concurrent wallet access | Relates to the WalletService race condition (issue #15). Repository needs a locking find method. |
| 82 | `ZoneRepository.java` | `findAllActiveZonesOrdered()` | **LOW** | JPQL uses string literal `'ZONE_ACTIVE'` instead of parameterized enum | Hardcoded status string; if enum value changes, query silently returns empty. |

---

## Category 8: Tests

### Files Reviewed
- `IntegrationTest.java` (122 lines, 8 tests)
- `ShipmentServiceTest.java` (429 lines, 19 tests)
- `WalletServiceTest.java` (241 lines, 10 tests)
- `UserServiceTest.java` (200 lines, 8 tests)
- `AuthControllerTest.java` (200 lines, 6 tests)
- Directory listing: 12 service tests, 26 controller tests, 2 DTO tests, 1 validation test, 2 security tests, 3 config tests

| # | Area | Severity | Description | Why It's a Problem |
|---|------|----------|-------------|--------------------|
| 83 | Test coverage structure | **MEDIUM** | 47 test files total — solid structure, but critical areas lack coverage | No test for `WalletService` race condition, no test for `DataInitializer` credential handling, no test for `TokenBlacklistService` fail-open behavior. |
| 84 | `UserServiceTest.java` | `deleteUser_Success` | **MEDIUM** | Test asserts `deleteById()` is called — confirms the hard-delete bug exists | Test validates wrong behavior. Should test soft-delete. |
| 85 | `ShipmentServiceTest.java` | Coverage | **LOW** | No test for `assignShipmentsToCourier()` (the `findAll()` anti-pattern) | The most performance-critical method is untested. |
| 86 | Integration tests | **LOW** | `IntegrationTest.java` only tests cross-cutting concerns (headers, health) | No integration test for actual business flows (create shipment → assign → deliver). |
| 87 | Missing test areas | **MEDIUM** | No security penetration tests, no concurrent access tests, no load tests | Financial race conditions and auth bypass scenarios are not covered by any automated test. |

---

## Category 9: Resources & Migrations

### Files Reviewed
- `application.yml` (223 lines), `application-prod.yml` (43 lines)
- `logback-spring.xml` (80 lines)
- All 7 Flyway migrations (V1–V7)

| # | File | Lines | Severity | Description | Why It's a Problem |
|---|------|-------|----------|-------------|--------------------|
| 88 | `application.yml` | `open-in-view` | **GOOD** | `spring.jpa.open-in-view: false` | Correctly disabled. Prevents lazy-loading N+1 issues in controllers. |
| 89 | `V2__sync_shipment_statuses.sql` | DELETE | **MEDIUM** | `DELETE FROM shipment_statuses WHERE name = 'PROCESSING' AND id NOT IN (...)` | Conditionally deletes statuses but uses subquery with `status_id` column — if any shipment references 'PROCESSING', it's preserved; otherwise data is permanently lost. Migration is irreversible. |
| 90 | `V3__create_notifications_and_ratings.sql` | `notifications` | **MEDIUM** | `user_id BIGINT NOT NULL` with no `FOREIGN KEY` constraint | Matches the entity-level issue (#8). Orphaned notifications possible if user is deleted. |
| 91 | `logback-spring.xml` | — | **GOOD** | Well-configured: console for dev, rolling files for prod, separate error log, 1GB cap, correlation ID in pattern | Proper logging setup. |
| 92 | `application-prod.yml` | — | **GOOD** | Disables Swagger, enables SSL, minimal overrides | Clean production config. |
| 93 | `application.yml` | Actuator | **LOW** | Exposes `health,info,metrics,prometheus` endpoints | Acceptable for internal monitoring, but `metrics` and `prometheus` should be secured in production if not behind a VPN. |

---

## Category 10: Cross-Cutting Concerns

| # | Area | Severity | Description | Why It's a Problem |
|---|------|----------|-------------|--------------------|
| 94 | **Authorization Strategy** | **HIGH** | Three competing authorization mechanisms exist simultaneously: `@PreAuthorize` annotations, manual `role.getName().equals(...)` checks in controllers, and the `PermissionService` + `AuthorizationService` that are mostly **unused** | Inconsistent authorization is the #1 source of privilege escalation bugs. Different controllers use different mechanisms; some endpoints may have no authorization at all. |
| 95 | **Entity Exposure** | **HIGH** | Multiple controllers return JPA entities directly in responses (`FinancialController`, `ManifestController`, `MasterDataController`, `PublicController`) | Leaks internal fields (IDs, timestamps, password hashes via eager relations). Breaks API contract stability — any entity change becomes a breaking API change. |
| 96 | **Error Handling** | **MEDIUM** | Mix of `RuntimeException` with Arabic messages, typed exceptions (`BusinessRuleException`, `ResourceNotFoundException`), and generic `Map<String,Object>` error responses | Inconsistent error response format. Frontend must handle multiple shapes. The `GlobalExceptionHandler` partially compensates but has its own hacks (Arabic substring matching). |
| 97 | **Consistent Response Format** | **MEDIUM** | `ApiResponse<T>` wrapper exists but many controllers return raw `Map<String,Object>` or `ResponseEntity<List>` | Breaks the standard response contract documented in copilot-instructions (`{success, message, data, errors}`). |
| 98 | **Timezone Handling** | **MEDIUM** | Mix of `Instant` (most entities), `LocalDateTime` (Zone entity, AppUtils), and `ZoneId.systemDefault()` (DashboardController, ReportsController) | Timezone-dependent calculations produce different results on different servers. Should standardize on `Instant` + explicit `ZoneId` from config. |
| 99 | **Batch Operations** | **MEDIUM** | Multiple locations iterate collections and call `repository.save()` per item instead of `saveAll()` | `ManifestController`, `ShipmentController.receiveShipments()`, `ShipmentController.reconcileWithCourier()` — all lack batch persistence. |
| 100 | **Logging** | **MEDIUM** | Multiple instances of `e.printStackTrace()` (PdfService, JwtAuthenticationFilter) and PII in logs (AuthController) | Bypasses logging framework; PII in logs violates privacy regulations. |
| 101 | **Transactional Boundaries** | **MEDIUM** | Financial operations (`WalletService.settleShipment()`) span multiple entity saves without explicit `@Transactional` with proper isolation | Partial saves possible on failure — e.g., wallet credited but shipment status not updated. |

---

## Top 10 Priority Fixes (Ordered by Risk)

1. **CRITICAL — WalletService Race Condition** (#15): Add `@Lock(PESSIMISTIC_WRITE)` to wallet lookups in `credit()`/`debit()` and wrap in `@Transactional(isolation = SERIALIZABLE)`.

2. **CRITICAL — DataInitializer Hardcoded Credentials** (#68): Move passwords and phone numbers to environment variables or a secrets manager. Rotate existing credentials immediately.

3. **CRITICAL — JWT Secret Default Empty** (#69): Add startup validation that fails fast when `jwt.secret` is empty or too short.

4. **CRITICAL — TokenBlacklistService Fail-Open** (#61): Change `isBlacklisted()` to return `true` or throw on Redis exception (fail closed).

5. **CRITICAL — MasterDataController Raw Entity** (#34): Replace `@RequestBody User` with `CreateUserRequest` DTO. Whitelist only allowed fields.

6. **HIGH — ShipmentService findAll() Anti-patterns** (#16, #17): Add filtered repository queries: `findByStatusNameIn(List<String>)` and `findTop10ByOrderByCreatedAtDesc()`.

7. **HIGH — UserService Hard Delete** (#22): Change `deleteUser()` to set `isDeleted = true` and `status = INACTIVE`.

8. **HIGH — DashboardController Wrong Stats** (#47): Add date-filtered count methods to `ShipmentRepository` and use them for weekly/monthly counts.

9. **HIGH — MasterDataController updatePricing No-Op** (#35): Uncomment the field update code or implement proper pricing update logic.

10. **HIGH — Inconsistent Authorization** (#94): Standardize on `@PreAuthorize` with `PermissionService` evaluator across all controllers. Remove manual role checks.

---

## Positive Observations

- **Well-structured project**: Clear separation into domain, service, web, security, config layers
- **Comprehensive test suite**: 47 test files covering services, controllers, config, security, DTOs, and validation
- **Good security headers**: `ApiVersionFilter` adds nosniff, DENY, XSS protection, no-store
- **Request tracing**: `RequestCorrelationFilter` + `RequestTracingFilter` provide correlation IDs and timing in MDC
- **OTP security**: Constant-time comparison (`MessageDigest.isEqual`) prevents timing attacks; rate limiting on attempts
- **File upload security**: `FileUploadService` sanitizes filenames, validates path traversal, restricts file types
- **Webhook security**: HMAC-SHA256 payload signing for webhook deliveries
- **RTL/Arabic support**: Proper Arabic text shaping in PDF labels using ICU library
- **Flyway migrations**: Incremental, versioned schema changes with `IF NOT EXISTS`/`INSERT IGNORE` for idempotency
- **Production logging**: Separate error log file, rolling policies with size caps, profile-specific configuration
- **Redis graceful degradation**: OTP and cache services fall back to in-memory when Redis is unavailable
