# Twsela Backend — Comprehensive Code Audit Report

## Table of Contents
1. [Executive Summary](#1-executive-summary)
2. [Technology Stack](#2-technology-stack)
3. [Project Structure](#3-project-structure)
4. [Infrastructure & Deployment](#4-infrastructure--deployment)
5. [Configuration Files](#5-configuration-files)
6. [Domain / Entity Layer](#6-domain--entity-layer)
7. [Repository Layer](#7-repository-layer)
8. [Service Layer](#8-service-layer)
9. [Security Layer](#9-security-layer)
10. [Controller / Web Layer](#10-controller--web-layer)
11. [DTO Layer](#11-dto-layer)
12. [Utility Layer](#12-utility-layer)
13. [Critical Security Findings](#13-critical-security-findings)
14. [Performance Issues](#14-performance-issues)
15. [Code Quality Issues](#15-code-quality-issues)
16. [Missing Functionality](#16-missing-functionality)
17. [Prioritised Recommendations](#17-prioritised-recommendations)

---

## 1. Executive Summary

The Twsela backend is a **Spring Boot 3.3.3 / Java 17** courier management system with ~80 Java source files across 7 packages. The system supports 5 roles (Owner, Admin, Merchant, Courier, Warehouse Manager) and handles shipment lifecycle, financial payouts, PDF generation, SMS notifications, and warehouse operations.

### Severity Breakdown

| Severity | Count | Examples |
|----------|-------|---------|
| 🔴 Critical | 12 | Debug controller in production, hardcoded passwords, `anyRequest().permitAll()`, DB password in CLI, insecure OTP generation |
| 🟠 High | 15 | Load-all-then-filter patterns, missing authorization checks, hardcoded courier ID, missing input validation |
| 🟡 Medium | 20+ | Field injection, `System.out.println` logging, inconsistent date types, commented-out caching, duplicate code |
| 🔵 Low | 15+ | Missing Swagger on some endpoints, code style, minor naming inconsistencies |

---

## 2. Technology Stack

**Source:** `pom.xml` — `twsela/pom.xml`

| Component | Version | Notes |
|-----------|---------|-------|
| Spring Boot | 3.3.3 | Parent POM |
| Java | 17 | `<java.version>17</java.version>` |
| MySQL Connector | 8.x (managed) | `com.mysql:mysql-connector-j` |
| Spring Data JPA | managed | `spring-boot-starter-data-jpa` |
| Spring Security | managed | `spring-boot-starter-security` |
| Redis | managed | `spring-boot-starter-data-redis` (commented out in YAML) |
| JJWT | 0.11.5 | `io.jsonwebtoken:jjwt-api/impl/jackson` |
| SpringDoc OpenAPI | 2.2.0 | Swagger UI |
| Apache POI | 5.2.4 | Excel import/export |
| iText | 8.0.4 | PDF generation with Arabic support |
| ICU4J | 75.1 | Arabic BiDi text shaping |
| ZXing | 3.5.3 | QR code / barcode generation |
| Twilio | 10.1.5 | SMS service |
| Micrometer Prometheus | managed | Metrics export |
| H2 | managed (runtime) | Dev/test fallback DB |

---

## 3. Project Structure

```
twsela/src/main/java/com/twsela/
├── config/          (4 files)  — CacheConfig, DataInitializer, JacksonConfig, SwaggerConfig
├── domain/          (29 files) — JPA entities and enums
├── repository/      (26 files) — Spring Data JPA repositories
├── security/        (7 files)  — JWT, Spring Security config, permissions
├── service/         (14 files) — Business logic services
├── util/            (1 file)   — AppUtils static helpers
└── web/             (16 files) — REST controllers
    └── dto/         (9 files)  — Request DTOs
```

**Total: ~80 Java source files.**

---

## 4. Infrastructure & Deployment

### 4.1 Dockerfile (`twsela/Dockerfile`)
- Multi-stage build: Maven 3.8.4 (build) → Eclipse Temurin 17 JRE Alpine (runtime).
- 🟠 **Port mismatch**: `EXPOSE 8080` but `application.yml` sets `server.port: 8000`.
- Creates `/app/logs` and `/app/uploads` directories.
- Runs as non-root user `appuser`.

### 4.2 Nginx (`twsela/nginx.conf`)
- SSL/TLS with TLS 1.2/1.3.
- Rate limiting: 10 req/s (general), 5 req/s (login).
- Proxy pass to `http://localhost:8000`.
- Security headers: X-Frame-Options, X-Content-Type-Options, X-XSS-Protection, CSP, HSTS.
- `/api/` and `/actuator/` reverse-proxied to Spring Boot.

### 4.3 Docker Compose
- **`docker-compose.backup.yml`**: MySQL 8, Redis 7-alpine, custom backup service.
- **`docker-compose.monitoring.yml`**: Prometheus, Grafana, Redis, MySQL.
- Both use hardcoded sensitive values (passwords) — should use Docker secrets.

### 4.4 application.yml (`twsela/src/main/resources/application.yml`)
- 🔴 **Hardcoded database credentials**: `username: root`, `password: root`.
- 🔴 **JWT secret committed to VCS**: `secret: O+ERbjSi7ohmxmUgxmhg+8kzartnn2XbxMtN5n5L3Ys=` (fallback default).
- Redis configuration is entirely **commented out** — cache will silently fail or use in-memory fallback.
- `ddl-auto: validate` is correct for production, but comment says "Auto-update schema for development".
- `open-in-view: false` — good.
- SSL is disabled by default (`SERVER_SSL_ENABLED: false`).
- Actuator exposes `env`, `beans`, `configprops` publicly — 🟠 **information leak**.
- JWT expiration: 24 hours — reasonable.

---

## 5. Configuration Files

### 5.1 CacheConfig.java
**Path:** `config/CacheConfig.java`

Redis cache manager with `ConcurrentMapCacheManager` fallback. Defines TTLs:
- `users`: 30 min
- `roles`, `zones`: 1 hour
- `pricing`: 2 hours
- `dashboard`: 5 min

🟡 **Issue**: `@EnableCaching` is active but Redis is commented out in YAML. The `RedisCacheManager` bean will fail at startup if no Redis connection is available — the try/catch only protects from `RedisConnectionFactory` being null, but dependency injection will fail first.

### 5.2 DataInitializer.java
**Path:** `config/DataInitializer.java`

`@Component` annotation is **commented out** (disabled). Seeds roles, user statuses, shipment statuses, payout statuses, zones (Egyptian cities), and telemetry settings.

🔴 **CRITICAL — Hardcoded credentials**:
- Owner account: phone `01023782584`, password `150620KkZz@#$`
- Admin account: phone `01023782585`, password `150620KkZz@#$`
- Seeds additional test users (courier `01023782586`, warehouse `01023782588`, merchant `01126538767`)

These passwords are committed to version control.

### 5.3 JacksonConfig.java
**Path:** `config/JacksonConfig.java`

Registers `JavaTimeModule`, disables `WRITE_DATES_AS_TIMESTAMPS`. Simple and correct.

### 5.4 SwaggerConfig.java
**Path:** `config/SwaggerConfig.java`

OpenAPI 3 configuration with JWT bearer authentication. Lists two servers: `localhost:8080` (🟡 should be 8000) and `api.twsela.com`. Arabic API documentation.

---

## 6. Domain / Entity Layer

### 6.1 User.java
- JPA entity with `Role` (EAGER), `UserStatus` (EAGER), phone (unique), password (`@JsonIgnore`).
- Has database indexes on `phone`, `role_id`, `status_id`.
- `isActive()` checks status name AND `isDeleted` flag.
- 🟡 `setActive()` and `setDeleted()` are stub methods that do nothing ("for testing") — dead code.

### 6.2 Shipment.java
- Core entity with ~20 fields. Tracking number (unique).
- Contains enums: `PodType`, `ShippingFeePaidBy`, `SourceType`.
- Relationships: merchant (EAGER), manifest (LAZY), zone (EAGER), status (EAGER), recipientDetails (EAGER), statusHistory, payout (LAZY).
- 🟠 **Multiple EAGER fetches** — performance concern when loading shipment lists. Each `findAll()` triggers at minimum 3 additional queries per row.
- Has `cashReconciled` boolean flag for financial tracking.

### 6.3 RecipientDetails.java
- Has unique index on `phone`.
- 🟠 **Design issue**: Unique phone constraint means the same recipient phone can't have different addresses across shipments. This is likely unintended — a recipient should be able to receive at different addresses.

### 6.4 ShipmentManifest.java
- Groups shipments for courier delivery.
- `ManifestStatus` enum: `CREATED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`.
- `manifestNumber` is unique but generation logic not visible.

### 6.5 Zone.java
- Has lat/long, `defaultFee`, `ZoneStatus` enum.
- 🟡 Uses `LocalDateTime` for `createdAt` — inconsistent with other entities using `Instant`.

### 6.6 Financial Entities
- **Payout.java**: PayoutType enum (COURIER, MERCHANT). Has period dates and netAmount.
- **PayoutItem.java**: Line items with SourceType enum. Has a `setItemType()` method that silently defaults to SHIPMENT_FEE — code smell.
- **DeliveryPricing.java**: Merchant-zone specific pricing with unique constraint.
- **CashMovementLedger.java**: Financial ledger with immutability flag (`isImmutable`). Has TransactionType and TransactionStatus enums.

### 6.7 Tracking & Audit Entities
- **CourierLocationHistory.java**: GPS coordinates with timestamp.
- **ShipmentStatusHistory.java**: Tracks status transitions with notes.
- **SystemAuditLog.java**: Comprehensive audit (user, action, entity, old/new values, IP, user agent).
- **NotificationLog.java**: SMS/notification audit trail.

### 6.8 Other Entities
- **CourierDetails.java** / **MerchantDetails.java**: One-to-one with User via `@MapsId`.
- **CourierZone.java**: Many-to-many with composite key (`@EmbeddedId`).
- **FraudBlacklist.java**: Blacklisted entities.
- **ReturnShipment.java**: Links original and return shipments.
- **ServiceFeedback.java**: Rating (short) — 🟡 no `@Min`/`@Max` validation.
- **MerchantServiceFeedback.java**: Rating (Integer, 1-5) — 🟡 no `@Min`/`@Max` validation.
- **TelemetrySettings.java**: Key-value system settings with `@PreUpdate` hook.
- **Warehouse.java** / **WarehouseInventory.java**: Warehouse management with InventoryStatus enum.
- **ShipmentPackageDetails.java**: One-to-one with Shipment for dimensions/weight.

### 6.9 Simple Lookup Entities
- **Role.java**, **ShipmentStatus.java**, **UserStatus.java**, **PayoutStatus.java**: Simple name/description entities.
- **ZoneStatus.java**: Enum (`ZONE_ACTIVE`, `ZONE_INACTIVE`, `ZONE_MAINTENANCE`).

---

## 7. Repository Layer

26 Spring Data JPA repository interfaces. Key observations:

### 7.1 ShipmentRepository.java (most complex — 102 lines)
- Well-designed with `@Query` annotations and `JOIN FETCH` to avoid N+1.
- `findByCourierId()` uses `JOIN FETCH s.manifest m WHERE m.courier.id = :courierId` — courier is resolved through manifest, not a direct relationship.
- Has `searchShipments()` with LIKE queries — 🟡 potential SQL injection via unescaped `%` wildcards (mitigated by JPA parameterisation, but performance concern with leading wildcards).
- Contains a **cross‑cutting query** `findByStatusName` that returns `ShipmentStatus` from a `ShipmentRepository` — 🟡 poor separation of concerns.

### 7.2 UserRepository.java
- `findByPhoneWithRoleAndStatus()` with `JOIN FETCH` — good optimisation for auth.
- `deleteByRoleNot()` — dangerous bulk delete method.
- All necessary finder methods present.

### 7.3 PayoutRepository.java
- Contains `findPayoutStatusByName()` query for `PayoutStatus` — 🟡 same cross-entity query issue as ShipmentRepository.

### 7.4 Other Repositories
- `CashMovementLedgerRepository`: Has sum aggregation queries — good.
- `SystemAuditLogRepository`: Date range queries — good.
- `ZoneRepository`: Has named queries for active zones.
- `MerchantDetailsRepository`, `ServiceFeedbackRepository`: Empty interfaces (rely on JpaRepository defaults).
- All 26 repositories verified — no custom implementations needed beyond interface methods.

---

## 8. Service Layer

### 8.1 UserService.java
- CRUD for users and roles. Caching annotations are **commented out**.
- 🟠 `listByRole()` loads ALL users then filters in memory (`findAll().stream().filter()`) — should use `findByRole()` repository method.
- 🟠 `deleteUser()` does **hard delete** (`repository.deleteById()`) — inconsistent with soft-delete pattern (`isDeleted` flag in User entity).
- `createUser()` properly hashes passwords with BCrypt and validates phone uniqueness.

### 8.2 ShipmentService.java (642 lines — largest file)
Core business logic.

- 🔴 `assignShipmentsToCourier()` calls `findAll()` to find unassigned shipments — loads **entire shipments table** into memory.
- 🔴 `updateCourierLocation()` only does `System.out.println` — **does not persist** the location update.
- 🟠 `findAll().stream().limit()` pattern used in multiple places — should use `Pageable` queries.
- 🟡 Multiple overloaded `createShipment()` methods for backward compatibility — needs cleanup.
- Has good tracking number generation and status transition logic.

### 8.3 BaseService.java
- Abstract class with `@Autowired` field injection — 🟡 anti-pattern (use constructor injection).
- Provides common user lookup/validation helpers.

### 8.4 AuthorizationService.java
- Well-structured with `canAccessShipment()`, `canModifyShipment()`, etc.
- 🟠 ADMIN role is missing from several checks — only OWNER has full access.

### 8.5 FinancialService.java
- Payout creation for couriers (70% of delivery fees) and merchants.
- 🟠 **Hardcoded 70% commission** — should be configurable.
- Uses `findByMerchantIdAndStatusNameAndPayoutIsNull` — appropriate custom repo method.

### 8.6 AuditService.java
- Uses `@Autowired` field injection.
- Silently catches and logs audit failures (graceful degradation — acceptable).
- `getClientIpAddress()` properly checks `X-Forwarded-For` and `X-Real-IP`.

### 8.7 BackupService.java (315 lines)
- Executes `mysqldump` via `ProcessBuilder`.
- 🔴 **CRITICAL**: Database password passed as CLI argument (`"-p" + dbPassword`) — visible in `ps aux` or `/proc` on Linux.
- Has gzip compression, retention cleanup (7 days), scheduled via `@Scheduled` cron.
- Restore function uses `mysql` CLI — same password exposure issue.

### 8.8 ExcelService.java
- Processes Excel uploads for bulk shipment creation.
- Validates headers and data with Arabic error messages.
- 🟠 **No file size limit** validation — potential DoS vector.
- Good error handling with per-row error collection.

### 8.9 FileUploadService.java
- 🔴 **CRITICAL**: Saves files to `src/main/resources/static/uploads/pod/` — inside the source directory. **Will not work** in a production JAR deployment.
- 🟠 No file size limits.
- 🟠 No filename sanitisation (uses tracking number directly).
- 🟡 No file type validation.

### 8.10 MetricsService.java
- Custom Micrometer metrics: login attempts, shipment counts, API calls, cache hits/misses, errors.
- Well-structured with counters, timers, and gauges. No issues.

### 8.11 OtpService.java
- 🔴 **CRITICAL**: Uses `java.util.Random` instead of `java.security.SecureRandom` — predictable OTP generation.
- 🟠 In-memory `ConcurrentHashMap` storage — OTPs lost on restart, won't work in multi-instance deployments.
- 5-minute expiration — appropriate.

### 8.12 PdfService.java (~270 lines)
- Generates shipping labels with Arabic text support using iText + ICU4J.
- Has barcode generation via ZXing.
- 🟡 Hardcoded font path (`/fonts/NotoSansArabic-Regular.ttf`) and company info.

### 8.13 SmsService.java (Interface)
- Defines `sendSms()` and default `sendOtp()` methods.

### 8.14 TwilioSmsService.java
- Twilio implementation with retry logic (exponential backoff, max 3 attempts).
- Phone number formatting: Egypt (+20), Saudi Arabia (+966), UAE (+971).
- 🟠 `Thread.sleep()` in retry — blocks the calling thread. Should use async retry / `@Retryable`.
- Lazy Twilio initialisation — good.

---

## 9. Security Layer

### 9.1 SecurityConfig.java
🔴 **Most critical file for security**:

```java
.anyRequest().permitAll()  // ← DANGEROUS catch-all
```

- CSRF disabled (acceptable for stateless REST API).
- CORS origins **hardcoded** to localhost variants.
- Many paths `permitAll`: `/api/auth/**`, `/api/health`, `/api/public/**`, `/api/debug/**`, `/swagger-ui/**`, `/actuator/**`.
- 🔴 **`/api/debug/**` is permitAll** — the DebugController (password hash generator, password reset) is publicly accessible.
- 🔴 **`anyRequest().permitAll()`** — any unmapped path is accessible without authentication.
- Actuator health, info, metrics, prometheus all public.

### 9.2 JwtService.java
- HMAC-SHA256 (HS256) via jjwt 0.11.5.
- Secret key handling: if key length > 64, Base64 decode; else use raw bytes — 🟡 fragile logic.
- 🟡 Uses **deprecated JJWT API** (`SignatureAlgorithm.HS256`, `setClaims`, `signWith(key, algo)`).
- Token claims include `role` as `ROLE_<NAME>`.

### 9.3 JwtAuthenticationFilter.java
- Extracts JWT from `Authorization: Bearer` header.
- 🟠 **Extracts role from JWT claims and overrides UserDetails authorities** — if JWT role doesn't match DB role, a user with a tampered JWT could potentially elevate privileges (mitigated by HMAC signature verification, but defence-in-depth recommends loading from DB).
- 🟡 Uses `e.printStackTrace()` — should use SLF4J logger.

### 9.4 ApplicationConfig.java
- `DaoAuthenticationProvider` with BCrypt.
- `UserDetailsService` loads by phone.
- 🟡 Uses `System.err.println` instead of SLF4J.

### 9.5 PermissionService.java
- Defines granular permissions (user:view, shipment:create, etc.) per role.
- 🟠 **Permission system is defined but never enforced** — controllers only use `@PreAuthorize("hasRole(...)")`, not permission-based checks.

### 9.6 SecurityExceptionHandler.java
- Custom `AuthenticationEntryPoint` returning 401 JSON.
- 🟡 Creates new `ObjectMapper` per class rather than injecting.

---

## 10. Controller / Web Layer

### 10.1 AuthController.java (308 lines)
- POST `/api/auth/login`: JWT login with phone/password.
- GET `/api/auth/me`: Current user info.
- GET `/api/auth/health`: Auth service health check.
- Good: Metrics recording, audit logging, inactive user check.
- 🟡 Excessive `System.out.println` logging with emoji — should use SLF4J.
- 🟡 Returns full `User` object including potentially sensitive fields in login response.
- Response format uses `Map<String, Object>` everywhere — no type safety.

### 10.2 ShipmentController.java (981 lines — largest controller)
- CRUD for shipments with pagination and sorting.
- Warehouse operations merged in (receive, dispatch, reconcile, inventory, stats).
- Courier location update, return requests.
- 🔴 **`updateCourierLocation()`** hardcodes `courierId = 1L` — completely broken: `Long courierId = 1L; // This should be extracted from security context`.
- 🟠 `createShipment()` accepts raw `Map<String, Object>` instead of using the `CreateShipmentRequest` DTO (which exists but is unused).
- 🟡 `generateTrackingNumber()` uses `Math.random()` — not cryptographically secure but acceptable for tracking numbers.
- `getShipments()` endpoint (`/list`) returns hardcoded empty list — stub implementation.

### 10.3 DashboardController.java
- Role-specific dashboards (Owner, Admin, Merchant, Courier, Warehouse).
- 🔴 `getOwnerDashboardSummary()` calls `shipmentRepository.findAll()` then sorts in memory for "recent activity" — loads **all shipments**.
- 🔴 Same pattern in all dashboard methods.
- 🟠 `getWarehouseDashboardSummary()` returns `shipmentRepository.count()` for all fields (received, dispatched, inventory, returns) — all identical, clearly stub code.
- 🟠 `getDashboardStatistics()` returns **hardcoded values** (15 shipments, 12 deliveries, etc.) — stub data.
- 🟠 `getRevenueChart()` and `getShipmentsChart()` return hardcoded arrays — stub data.

### 10.4 UserController.java
- CRUD for users, couriers, merchants, employees.
- Inline DTOs (`CreateUserRequest`, `UpdateUserRequest`) defined as static inner classes.
- 🟠 `getCouriers()` and `getMerchants()` use `listByRole()` which loads all users and filters in memory.
- 🟠 Manual pagination (`subList`) on in-memory lists — should use database-level pagination.
- `deleteUser()` calls `userService.deleteUser()` which hard-deletes.

### 10.5 DebugController.java
🔴 **CRITICAL SECURITY ISSUE — Must be removed before production**:
- GET `/api/debug/generate-hash?password=X` — publicly accessible password hash generator.
- POST `/api/debug/test-password` — publicly accessible password verifier.
- POST `/api/debug/reset-test-passwords` — **resets ALL test account passwords** to `150620KkZz@#$`.
- Lists real phone numbers and passwords in source code.
- This controller is accessible to anyone (`/api/debug/**` is `permitAll` in SecurityConfig).

### 10.6 FinancialController.java
- Payout CRUD with role-based access.
- `getCurrentUser()` casts `authentication.getPrincipal()` to `User` — may throw `ClassCastException` if principal is a `UserDetails` string.
- Inline `CreatePayoutRequest` DTO duplicates the DTO in `dto/` package.
- Authorization checks are manual (in-method) rather than using `@PreAuthorize`.

### 10.7 ManifestController.java
- Manifest CRUD with role-based access.
- `assignShipmentsToManifest()` has a TODO: "Implement shipment assignment logic" — 🟠 incomplete.
- Same `getCurrentUser()` casting issue.
- Duplicate `CreateManifestRequest` as inner class (also exists in `dto/`).

### 10.8 ReportsController.java
- Shipment, courier, merchant, warehouse reports with date ranges.
- 🟠 **Loads all data then filters in memory** for every report — `findAll().stream().filter()`.
- 🟡 Warehouse report returns `shipmentRepository.count()` for all metrics — stub implementation.
- Same `getCurrentUser()` casting issue.

### 10.9 SettingsController.java
- GET/POST/reset for settings.
- 🟠 **No persistence** — settings are not saved to the database. `saveSettings()` returns success but does nothing.
- Returns hardcoded defaults.

### 10.10 GlobalExceptionHandler.java
- `@ControllerAdvice` with handlers for validation, auth, access denied, runtime exceptions.
- 🟡 Generic `RuntimeException` handler returns 400 instead of 500 — hides true server errors.
- 🟡 Login path detection via string matching on request path — fragile.
- Good: handles `NoSuchElementException` as 404.

### 10.11 HealthController.java
- Simple `/api/health` endpoint returning status.
- Version hardcoded as `1.0.1`.
- Environment hardcoded as `development`.

### 10.12 MasterDataController.java (368 lines)
- CRUD for users, zones, pricing, telemetry settings.
- `createUser()` accepts raw `User` entity in request body — 🔴 **mass assignment vulnerability** (attacker can set any field including password, role, status).
- `deleteUser()`, `deleteZone()`, `deletePricing()` do hard deletes.
- ADMIN comment says "can only see non-OWNER users" but the code does `findAll(pageable)` for both — 🟡 access control not implemented as intended.

### 10.13 PublicController.java (365 lines)
- Public tracking (`/api/public/track/{trackingNumber}`).
- Feedback submission.
- Forgot password, OTP send/verify, password reset.
- Contact form.
- 🟠 `forgotPassword()` generates password using `java.util.Random` — insecure.
- 🟠 `forgotPassword()` logs new password to console — information leak.
- Contact form stub — not persisted.

### 10.14 BackupController.java
- Create/restore backup, status check.
- `restoreBackup()` takes `backupFilePath` as query parameter — 🔴 **path traversal risk** if not validated.
- Only OWNER/ADMIN access — appropriate.

### 10.15 AuditController.java
- Audit log retrieval with filtering (date range, action, user).
- OWNER/ADMIN access — appropriate.
- No pagination support — 🟡 could return very large result sets.

### 10.16 SmsController.java
- Send SMS, OTP, notifications.
- 🔴 `sendOtp()` returns the OTP in the response body (`response.put("otp", otp)`) — **leaks OTP to client** even with the comment "In production, don't return OTP in response".
- Uses `Math.random()` for OTP generation — insecure.
- OWNER/ADMIN access — appropriate for admin SMS sending.

---

## 11. DTO Layer

9 well-structured DTOs with Jakarta Validation annotations and Swagger documentation:

| DTO | Validations | Status |
|-----|------------|--------|
| `LoginRequest` | `@NotBlank` phone/password, phone regex, min length 6 | ✅ Good |
| `PasswordResetRequest` | `@NotBlank` phone, phone regex | ✅ Good, but `newPassword` has no strength validation |
| `CreateShipmentRequest` | Full validation: `@NotBlank`, `@NotNull`, `@Positive`, `@Size` on all fields | ✅ Good — but **not used** by ShipmentController |
| `AssignShipmentsRequest` | `@NotEmpty` tracking numbers list | ✅ Good |
| `ContactFormRequest` | `@NotBlank`, `@Email`, `@Size` on all fields | ✅ Good — but **not used** by PublicController |
| `CreateManifestRequest` | `@NotNull` courierId | ✅ Good — but duplicated as inner class |
| `CreatePayoutRequest` | `@NotNull`/`@NotBlank` on all fields | ✅ Good — but duplicated as inner class |
| `LocationUpdateRequest` | `@NotNull` lat/long | ✅ Good — but **not used** by ShipmentController |
| `ReturnRequest` | `@NotBlank` reason | ✅ Good — but **not used** by ShipmentController |

🟠 **Major issue**: Several DTOs are properly defined with validations but controllers accept `Map<String, Object>` or have duplicate inner-class DTOs instead. This defeats the purpose of validation.

---

## 12. Utility Layer

### AppUtils.java
- Static utility class with response builders (`success()`, `error()`, `unauthorized()`, `forbidden()`, `validationError()`).
- Date/time utilities.
- Validation regex patterns (phone, OTP, password, name with Arabic support).
- 🟡 Uses `LocalDateTime.now()` for response timestamps — should use `Instant.now()` for timezone safety.

---

## 13. Critical Security Findings

### SEC-01: Debug Controller Publicly Accessible 🔴
**Files:** `DebugController.java`, `SecurityConfig.java`  
**Impact:** Anyone can generate password hashes, test passwords, and reset all test account passwords.  
**Fix:** Remove `DebugController` entirely. Remove `/api/debug/**` from `permitAll()`.

### SEC-02: `anyRequest().permitAll()` 🔴
**File:** `SecurityConfig.java`  
**Impact:** Any URL not explicitly configured is accessible without authentication.  
**Fix:** Change to `anyRequest().authenticated()`.

### SEC-03: Hardcoded Credentials in Source Code 🔴
**Files:** `DataInitializer.java`, `DebugController.java`, `application.yml`  
**Impact:** Passwords, JWT secrets, and database credentials committed to VCS.  
**Fix:** Use environment variables exclusively. Remove hardcoded passwords.

### SEC-04: Database Password in CLI Arguments 🔴
**File:** `BackupService.java`  
**Impact:** DB password visible in process listings (`ps aux`).  
**Fix:** Use `MYSQL_PWD` environment variable or `--defaults-file` with mysqldump.

### SEC-05: Insecure Random for OTP/Password Generation 🔴
**Files:** `OtpService.java`, `PublicController.java`, `SmsController.java`  
**Impact:** Predictable OTPs and passwords.  
**Fix:** Use `java.security.SecureRandom`.

### SEC-06: OTP Returned in API Response 🔴
**File:** `SmsController.java`  
**Impact:** OTP leaked to client.  
**Fix:** Never return OTP in response.

### SEC-07: Mass Assignment via Raw Entity Binding 🔴
**File:** `MasterDataController.java`  
**Impact:** Attackers can set any User field (role, password, status) via `createUser(@RequestBody User user)`.  
**Fix:** Use DTOs for all request bodies.

### SEC-08: Path Traversal in Backup Restore 🔴
**File:** `BackupController.java`  
**Impact:** `restoreBackup(@RequestParam String backupFilePath)` could be exploited to restore arbitrary files.  
**Fix:** Validate path is within backup directory.

### SEC-09: File Upload Stored in Source Directory 🔴
**File:** `FileUploadService.java`  
**Impact:** Files saved to `src/main/resources/static/uploads/` — broken in JAR deployment, no sanitisation.  
**Fix:** Use external configurable upload directory.

### SEC-10: Actuator Endpoints Publicly Accessible 🟠
**Files:** `SecurityConfig.java`, `application.yml`  
**Impact:** `/actuator/env`, `/actuator/beans`, `/actuator/configprops` expose internal configuration.  
**Fix:** Restrict actuator to `health` and `prometheus` only for public access.

### SEC-11: Forgot Password Logs New Password 🟠
**File:** `PublicController.java`  
**Impact:** New passwords written to application log file.  
**Fix:** Remove console logging of passwords.

### SEC-12: Deprecated JJWT API 🟡
**File:** `JwtService.java`  
**Impact:** Deprecated methods may be removed in future versions.  
**Fix:** Migrate to jjwt 0.12.x builder API.

---

## 14. Performance Issues

### PERF-01: Load All Then Filter Pattern 🔴
**Files:** `UserService.java`, `ShipmentService.java`, `DashboardController.java`, `ReportsController.java`, `UserController.java`  
**Impact:** Loads entire database tables into memory then filters with Java streams.  
**Examples:**
- `UserService.listByRole()`: `findAll().stream().filter()`
- `ShipmentService.assignShipmentsToCourier()`: `findAll()` to find unassigned
- `DashboardController.getOwnerDashboardSummary()`: `findAll()` for recent activity
- `ReportsController.getOwnerShipmentReport()`: `findAll().stream().filter()`  
**Fix:** Use repository queries with `Pageable`, WHERE clauses, and `@Query` annotations.

### PERF-02: Excessive EAGER Fetching 🟠
**File:** `Shipment.java`  
**Impact:** Loading a shipment always loads merchant, zone, status, recipientDetails. Loading lists triggers N+1 queries.  
**Fix:** Change to `LAZY` and use `JOIN FETCH` in repository queries where needed.

### PERF-03: Disabled Caching 🟠
**Files:** `application.yml`, `UserService.java`  
**Impact:** Redis is commented out, `@Cacheable` annotations are commented out. Every request hits the database.  
**Fix:** Enable Redis caching or use in-memory cache as fallback.

### PERF-04: In-Memory Pagination 🟡
**File:** `UserController.java`  
**Impact:** `getCouriers()`, `getMerchants()`, `getEmployees()` load all records then do `subList()`.  
**Fix:** Use `PageRequest` with repository queries.

### PERF-05: Thread-Blocking SMS Retry 🟡
**File:** `TwilioSmsService.java`  
**Impact:** `Thread.sleep()` blocks the request thread during retry backoff.  
**Fix:** Use `@Async` + `@Retryable` or a scheduled retry queue.

---

## 15. Code Quality Issues

### CQ-01: System.out.println Everywhere 🟡
**Impact:** Console logging with emojis throughout production code.  
**Files:** `AuthController`, `DashboardController`, `ShipmentController`, `MasterDataController`, `BackupService`, all services.  
**Fix:** Use SLF4J `Logger` consistently. The project already has SLF4J on the classpath.

### CQ-02: Field Injection Anti-Pattern 🟡
**Files:** `BaseService.java`, `AuditService.java`, `ShipmentController.java`, `DashboardController.java`, `FinancialController.java`, `ManifestController.java`, `ReportsController.java`, `MasterDataController.java`, `SmsController.java`, `BackupController.java`  
**Fix:** Use constructor injection (already done in `AuthController`, `PublicController`).

### CQ-03: DTOs Created But Not Used 🟠
**Files:** All 9 DTOs in `web/dto/`  
**Impact:** Well-validated DTOs exist but controllers accept `Map<String, Object>`, duplicate inner classes, or raw entities.  
**Fix:** Wire DTOs into controllers and add `@Valid` annotation.

### CQ-04: Duplicate Inner-Class DTOs 🟡
**Files:** `FinancialController.CreatePayoutRequest`, `ManifestController.CreateManifestRequest`, `UserController.CreateUserRequest/UpdateUserRequest`  
**Fix:** Use the DTOs from `web/dto/` package.

### CQ-05: Inconsistent Date Types 🟡
**Impact:** Mix of `Instant`, `LocalDateTime`, `LocalDate` across entities and services.  
**Fix:** Standardise on `Instant` for persistence and `LocalDate` for business dates.

### CQ-06: Mixed Response Patterns 🟡
**Impact:** Some controllers return `ResponseEntity<Map<String, Object>>`, others return `ResponseEntity<Entity>`, some return `ResponseEntity<?>`.  
**Fix:** Create a standard `ApiResponse<T>` wrapper class.

### CQ-07: Exception Handling Inconsistency 🟡
**Impact:** Some controllers catch all exceptions and return 500, others let exceptions propagate to `GlobalExceptionHandler`. Some return Arabic messages, others English.  
**Fix:** Establish consistent error handling via `@ControllerAdvice` and remove per-method try-catch blocks.

### CQ-08: Hardcoded Courier ID 🔴
**File:** `ShipmentController.java` line ~890  
```java
Long courierId = 1L; // This should be extracted from security context
```
**Fix:** Extract from `Authentication` principal.

### CQ-09: Stub Implementations in Production Code 🟠
**Files:**
- `ShipmentController.getShipments()`: Returns empty list always
- `DashboardController.getDashboardStatistics()`: Returns hardcoded values
- `DashboardController.getRevenueChart()` / `getShipmentsChart()`: Hardcoded data
- `DashboardController.getWarehouseDashboardSummary()`: All metrics = total count
- `SettingsController`: Settings not persisted
- `ManifestController.assignShipmentsToManifest()`: TODO comment, just prints  
**Fix:** Implement or remove these endpoints.

### CQ-10: getCurrentUser() Casting Issue 🟠
**Files:** `FinancialController`, `ManifestController`, `ReportsController`, `MasterDataController`  
```java
private User getCurrentUser(Authentication auth) {
    return (User) auth.getPrincipal();
}
```
**Impact:** May throw `ClassCastException` depending on authentication configuration. Other controllers (`AuthController`, `ShipmentController`) correctly use `authentication.getName()` → `userRepository.findByPhone()`.  
**Fix:** Standardise on the phone-lookup pattern.

---

## 16. Missing Functionality

| Feature | Status | Notes |
|---------|--------|-------|
| Email notifications | ❌ Missing | Only SMS implemented |
| Rate limiting at app level | ❌ Missing | Only nginx-level rate limiting |
| Request body size limits | ❌ Missing | No multipart/upload size config |
| API versioning | ❌ Missing | All APIs under `/api/` without version |
| Database migrations | ❌ Missing | No Flyway/Liquibase. Uses `ddl-auto: validate` — schema changes must be manual |
| Integration tests | ❌ Missing | No test files found |
| Unit tests | ❌ Missing | No test files found |
| Password strength validation | ❌ Missing | `PasswordResetRequest.newPassword` has no strength rules |
| Account lockout | ❌ Missing | No brute-force protection beyond nginx rate limiting |
| Token refresh | ❌ Missing | Single JWT with 24h expiry, no refresh mechanism |
| Token revocation/blacklist | ❌ Missing | No way to invalidate JWTs |
| Pagination on audit logs | ❌ Missing | `AuditController` returns full lists |
| Warehouse entity usage | ❌ Missing | `Warehouse` and `WarehouseInventory` entities exist but no controller uses them |
| Permission-based access control | ❌ Defined but unused | `PermissionService` maps permissions but no `@PreAuthorize` uses permission expressions |

---

## 17. Prioritised Recommendations

### P0 — Must Fix Before Production (Security)
1. **Remove `DebugController.java`** entirely and remove `/api/debug/**` from security config.
2. **Change `anyRequest().permitAll()` to `anyRequest().authenticated()`** in `SecurityConfig`.
3. **Remove all hardcoded passwords** from source code. Use env vars or secrets manager.
4. **Fix BackupService** to use `--defaults-file` or env var for MySQL password.
5. **Use `SecureRandom`** for all OTP and password generation.
6. **Remove OTP from SMS controller response**.
7. **Fix `MasterDataController.createUser()`** to use DTO instead of raw entity binding.
8. **Validate backup file path** in `BackupController.restoreBackup()`.
9. **Move file uploads** to external configurable directory.
10. **Fix hardcoded `courierId = 1L`** in `ShipmentController.updateCourierLocation()`.

### P1 — High Priority (Performance & Correctness)
1. **Eliminate load-all-then-filter patterns** — use repository queries with `WHERE` and `Pageable`.
2. **Fix EAGER fetching** in `Shipment.java` — use LAZY + `JOIN FETCH`.
3. **Wire DTOs into controllers** — replace `Map<String, Object>` request bodies.
4. **Implement or remove stub endpoints** — dashboard charts, settings save, shipment list.
5. **Fix `getCurrentUser()` casting** — standardise on phone lookup pattern.
6. **Implement `updateCourierLocation()`** to actually persist data.
7. **Enable caching** — at minimum use in-memory cache if Redis is not available.
8. **Restrict actuator endpoints** — only expose `health` and `prometheus` publicly.

### P2 — Medium Priority (Code Quality)
1. **Replace all `System.out.println`** with SLF4J `Logger`.
2. **Migrate to constructor injection** everywhere.
3. **Standardise response format** with a typed `ApiResponse<T>` class.
4. **Fix date type inconsistencies** — use `Instant` for timestamps.
5. **Add database migration tool** (Flyway or Liquibase).
6. **Remove duplicate inner-class DTOs**.
7. **Add integration and unit tests**.
8. **Fix Dockerfile `EXPOSE`** to match actual port (8000).

### P3 — Low Priority (Improvements)
1. Add API versioning (`/api/v1/`).
2. Implement token refresh mechanism.
3. Add account lockout after failed login attempts.
4. Implement the defined `PermissionService` in `@PreAuthorize` expressions.
5. Add email notification support.
6. Add request/response logging with MDC correlation IDs.
7. Upgrade JJWT to 0.12.x to address deprecated API usage.

---

*Report generated from exhaustive analysis of all 80+ Java source files, configuration files, and infrastructure files in `twsela/`.*
