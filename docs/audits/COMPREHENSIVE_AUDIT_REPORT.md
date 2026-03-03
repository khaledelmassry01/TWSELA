# Twsela — Comprehensive Deep Audit Report

> **Audit Date:** 2025  
> **Scope:** Full codebase — Backend (Java/Spring Boot), Frontend (Vanilla JS), Infrastructure (Docker/Nginx)  
> **Baseline:** After Sprints 1–5  
> **Target:** 100+ actionable items for Sprints 6–10  

---

## Table of Contents

1. [Executive Summary](#executive-summary)
2. [P0 — Critical (Must-Fix, Blocks Production)](#p0--critical)
3. [P1 — High (Major Bugs / Security / Performance)](#p1--high)
4. [P2 — Medium (Correctness, Maintainability, UX)](#p2--medium)
5. [P3 — Low (Polish, Tech Debt, Nice-to-Have)](#p3--low)
6. [Sprint Allocation Suggestion](#sprint-allocation-suggestion)

---

## Executive Summary

| Priority | Count |
|----------|-------|
| P0 — Critical | 14 |
| P1 — High | 32 |
| P2 — Medium | 42 |
| P3 — Low | 28 |
| **Total** | **116** |

---

## P0 — Critical

### BACK-P0-001 · `forgotPassword()` uses `java.util.Random`
- **File:** `twsela/src/main/java/com/twsela/web/PublicController.java`
- **Description:** Password reset generates a temporary password using `java.util.Random`, which is predictable and NOT cryptographically secure.
- **Fix:** Replace with `java.security.SecureRandom`.

### BACK-P0-002 · `SmsController.sendOtp()` returns OTP in response body
- **File:** `twsela/src/main/java/com/twsela/web/SmsController.java`
- **Description:** The OTP code is returned directly in the API response ("In production, don't return OTP" comment in code). An attacker can read the OTP without access to the phone.
- **Fix:** Remove OTP from response; deliver only via SMS.

### BACK-P0-003 · Settings are never persisted
- **File:** `twsela/src/main/java/com/twsela/web/SettingsController.java`
- **Description:** `getSettings()` returns hardcoded defaults. `saveSettings()` echoes back input without saving. No database entity exists for settings.
- **Fix:** Create a `Settings` entity/table and persist values.

### BACK-P0-004 · ReportsController loads ALL data in memory
- **File:** `twsela/src/main/java/com/twsela/web/ReportsController.java`
- **Description:** `getCourierReport()` and `getMerchantReport()` call `shipmentRepository.findAll()` per user, then stream-filter in Java. With 100 k shipments this will OOM.
- **Fix:** Add date-ranged `@Query` methods in `ShipmentRepository` and let the DB filter.

### BACK-P0-005 · Warehouse report returns identical metrics
- **File:** `twsela/src/main/java/com/twsela/web/ReportsController.java`
- **Description:** `getWarehouseReport()` returns `shipmentRepository.count()` for all three KPIs (received, dispatched, returned). All three numbers are always equal.
- **Fix:** Implement proper status-filtered queries for each metric.

### BACK-P0-006 · Dashboard revenue chart returns all zeros
- **File:** `twsela/src/main/java/com/twsela/web/DashboardController.java`
- **Description:** `getRevenueChart()` explicitly sets `data[i] = BigDecimal.ZERO` for every month. The chart in the frontend is always empty.
- **Fix:** Query actual monthly revenue from shipments and populate.

### BACK-P0-007 · Dashboard week/month stats reuse today's delivered count
- **File:** `twsela/src/main/java/com/twsela/web/DashboardController.java`
- **Description:** `getDashboardStatistics()` uses the same today-only query for "this week" and "this month" delivered counts. Rendered KPIs are incorrect.
- **Fix:** Query using week-start and month-start boundaries.

### BACK-P0-008 · `ShipmentService.calculateCourierEarnings()` ignores date params
- **File:** `twsela/src/main/java/com/twsela/service/ShipmentService.java`
- **Description:** `startDate` and `endDate` arguments are accepted but never used — always returns all unreconciled shipments.
- **Fix:** Filter by date range in the repository query.

### BACK-P0-009 · `FinancialService.createCourierPayout()` ignores date range
- **File:** `twsela/src/main/java/com/twsela/service/FinancialService.java`
- **Description:** Same bug as above — gets all unreconciled shipments regardless of period; hardcoded 70% courier commission rate.
- **Fix:** Add date filtering; make commission configurable.

### BACK-P0-010 · `MasterDataController.createUser()` accepts raw entity, no validation
- **File:** `twsela/src/main/java/com/twsela/web/MasterDataController.java`
- **Description:** `@RequestBody User user` is saved directly — no `@Valid`, no DTO, no phone/role validation. Attacker can create an OWNER-role user.
- **Fix:** Accept a validated DTO; prevent role escalation.

### BACK-P0-011 · `MasterDataController.updatePricing()` has commented-out fields
- **File:** `twsela/src/main/java/com/twsela/web/MasterDataController.java`
- **Description:** Lines updating `weightFrom`, `weightTo`, `price`, `status` are all commented out. PUT effectively does nothing besides changing the zone reference.
- **Fix:** Uncomment or rewrite the DeliveryPricing update logic.

### BACK-P0-012 · `MasterDataController.deleteZone()` does hard delete
- **File:** `twsela/src/main/java/com/twsela/web/MasterDataController.java`
- **Description:** `zoneRepository.deleteById(id)` performs a hard delete. Shipments referencing this zone will have broken FK or null references.
- **Fix:** Implement soft-delete (status change) and validate no active shipments reference the zone.

### FRONT-P0-013 · Currency hardcoded to SAR (Saudi Riyal) instead of EGP
- **File:** `frontend/src/js/shared/BasePageHandler.js` (line ~323), `frontend/src/js/shared/SharedDataUtils.js` (line 126)
- **Description:** `formatCurrency()` uses `'ar-SA'` locale and `currency: 'SAR'` across entire UI. Twsela is an Egyptian system; should use EGP.
- **Fix:** Change `currency` to `'EGP'`, locale to `'ar-EG'`.

### FRONT-P0-014 · Phone validation regex uses Saudi format in Egyptian app
- **File:** `frontend/src/js/pages/login.js`, `frontend/src/js/pages/merchant-create-shipment.js`
- **Description:** Regex `^(\+966|0)?[5-9][0-9]{8}$` validates Saudi phone numbers. Egyptian numbers are `01[0-9]{9}` or `+201[0-9]{9}`.
- **Fix:** Replace with Egyptian phone regex.

---

## P1 — High

### BACK-P1-001 · `UserService.listByRole()` loads ALL users then filters in-memory
- **File:** `twsela/src/main/java/com/twsela/service/UserService.java`
- **Description:** Fetches every user from DB, then `stream().filter(u -> u.getRole().getName().equals(role))`.
- **Fix:** Use `userRepository.findByRole_Name(role)` or a `@Query`.

### BACK-P1-002 · `UserController.getCouriers/getMerchants()` in-memory pagination
- **File:** `twsela/src/main/java/com/twsela/web/UserController.java`
- **Description:** Loads ALL users of a role, then `subList`s for pagination. N+1 at scale.
- **Fix:** Use `Pageable` in the repository query.

### BACK-P1-003 · `UserService.deleteUser()` does hard delete  
- **File:** `twsela/src/main/java/com/twsela/service/UserService.java`
- **Description:** `userRepository.deleteById(id)` contradicts the soft-delete pattern used in `MasterDataController`.
- **Fix:** Call `setIsDeleted(true)` + `setDeletedAt(Instant.now())` consistently.

### BACK-P1-004 · No `@Valid` on most controller DTOs / request bodies
- **Files:** `FinancialController`, `ManifestController`, `MasterDataController`, `PublicController`
- **Description:** Inner DTOs and `@RequestBody` parameters lack `@Valid`. Bean-validation annotations are defined but never triggered.
- **Fix:** Add `@Valid` on every `@RequestBody` parameter.

### BACK-P1-005 · `AuthController.login()` returns full `User` entity
- **File:** `twsela/src/main/java/com/twsela/web/AuthController.java`
- **Description:** Password hash, `isDeleted`, internal timestamps leak to the client.
- **Fix:** Map to a `LoginResponseDTO` that excludes sensitive fields.

### BACK-P1-006 · No logout / token-blacklist endpoint
- **File:** `twsela/src/main/java/com/twsela/web/AuthController.java`
- **Description:** There's no server-side token invalidation. `logout` on the frontend just clears `sessionStorage`.
- **Fix:** Implement a Redis-backed token blacklist checked in `JwtAuthenticationFilter`.

### BACK-P1-007 · `ShipmentController.generateTrackingNumber()` uses `Math.random()`
- **File:** `twsela/src/main/java/com/twsela/web/ShipmentController.java`
- **Description:** Tracking numbers generated with an insecure RNG. Collision risk grows with volume.
- **Fix:** Use `SecureRandom` and a longer / structured format (e.g., UUID prefix + sequence).

### BACK-P1-008 · `ShipmentService.assignShipmentsToCourier()` loads ALL shipments then filters
- **File:** `twsela/src/main/java/com/twsela/service/ShipmentService.java`
- **Description:** `shipmentRepository.findAll()` followed by in-memory filter for IDs.
- **Fix:** Use `shipmentRepository.findAllById(ids)`.

### BACK-P1-009 · `getCurrentUser()` duplicated across 6+ controllers
- **Files:** `ShipmentController`, `FinancialController`, `ManifestController`, `MasterDataController`, `DashboardController`, `ReportsController`
- **Description:** Each has its own `getCurrentUser(Authentication)` that casts `getPrincipal()` to `User`. If the principal is a `String`, it throws a `ClassCastException`.
- **Fix:** Extract to a shared base class or utility. Add a safe-cast check.

### BACK-P1-010 · OTP stored in `ConcurrentHashMap` — lost on restart, no TTL cleanup
- **File:** `twsela/src/main/java/com/twsela/service/OtpService.java`
- **Description:** No scheduled cleanup of expired OTPs (memory leak). Data is lost if the server restarts.
- **Fix:** Store OTPs in Redis with TTL.

### BACK-P1-011 · Missing `@Operation` / OpenAPI annotations on ~50% of endpoints
- **Files:** `UserController`, `FinancialController`, `ManifestController`, `SmsController`, many in `ShipmentController`
- **Description:** Swagger UI shows endpoints without summaries/descriptions.
- **Fix:** Add `@Operation(summary=…)` to every endpoint method.

### BACK-P1-012 · Many endpoints return `Map<String, Object>` instead of typed DTOs
- **File:** `twsela/src/main/java/com/twsela/web/ShipmentController.java` (≥10 endpoints)
- **Description:** Makes the API undocumented and fragile.
- **Fix:** Create proper response DTOs.

### BACK-P1-013 · No pagination on `getAllPayouts()` and `getAllManifests()`
- **Files:** `FinancialController`, `ManifestController`
- **Description:** Returns unbounded lists.
- **Fix:** Accept `Pageable` parameter and return `Page<>`.

### BACK-P1-014 · Pricing calculation duplicated between controller and service
- **Files:** `ShipmentController.calculateDeliveryFee()`, `ShipmentService`
- **Description:** Two copies of the pricing hierarchy logic.
- **Fix:** Keep only the service method; have the controller delegate.

### BACK-P1-015 · `Shipment.setCourier()` is a no-op
- **File:** `twsela/src/main/java/com/twsela/domain/Shipment.java`
- **Description:** Method body is empty, yet multiple places call it.
- **Fix:** Remove the no-op method; document that courier is accessed via `manifest.courier`.

### BACK-P1-016 · `PublicController.track()` throws `NoSuchElement` with no friendly error
- **File:** `twsela/src/main/java/com/twsela/web/PublicController.java`
- **Description:** Uses `.get()` without `.isPresent()` check on `Optional`.
- **Fix:** Return 404 with a localized error message.

### BACK-P1-017 · `PublicController.feedback()` accepts raw entity, no validation
- **File:** `twsela/src/main/java/com/twsela/web/PublicController.java`
- **Description:** `@RequestBody ServiceFeedback` is saved directly. No DTO, no `@Valid`.
- **Fix:** Add `@Valid` and a DTO.

### BACK-P1-018 · `PublicController.submitContactForm()` is a stub
- **File:** `twsela/src/main/java/com/twsela/web/PublicController.java`
- **Description:** Contact form submissions go nowhere — not saved, not emailed.
- **Fix:** Persist to a `ContactForm` entity and/or send an email notification.

### BACK-P1-019 · Timezone hardcoded to `ZoneId.systemDefault()`
- **File:** `twsela/src/main/java/com/twsela/web/DashboardController.java`
- **Description:** `getTodayStart()`/`getTodayEnd()` use system default tz. Results differ depending on server zone.
- **Fix:** Use a configurable Africa/Cairo zone.

### BACK-P1-020 · `ShipmentService.createShipment()` catch-all rethrows as `RuntimeException`
- **File:** `twsela/src/main/java/com/twsela/service/ShipmentService.java`
- **Description:** `catch (Exception e) { throw new RuntimeException(e.getMessage()); }` hides root cause and stack trace.
- **Fix:** Let exceptions propagate or wrap in a domain-specific exception.

### BACK-P1-021 · Dockerfile exposes port 8080 but application runs on 8000
- **File:** `twsela/Dockerfile`
- **Description:** `EXPOSE 8080` and `HEALTHCHECK` hit port 8080, but `application.yml` sets `server.port: 8000`.
- **Fix:** Change to `EXPOSE 8000` and update healthcheck URL.

### BACK-P1-022 · Docker MySQL init script path references non-existent file
- **File:** `twsela/docker-compose.monitoring.yml`
- **Description:** `./twsela/src/main/resources/db/migration/twsela.sql` path relative to compose file may not exist; no migration files were found.
- **Fix:** Create proper SQL migration or use Flyway/Liquibase.

### FRONT-P1-023 · `BasePageHandler.waitForServices()` spin-waits up to 5 seconds
- **File:** `frontend/src/js/shared/BasePageHandler.js`
- **Description:** Busy-loop `while` with `setTimeout(100ms)` up to 50 iterations waiting for globals.
- **Fix:** Use `Promise`-based event or `MutationObserver` instead of polling.

### FRONT-P1-024 · `owner-zones-page.js` duplicates auth check logic already in `BasePageHandler`
- **File:** `frontend/src/js/pages/owner-zones-page.js`
- **Description:** 60+ lines of manual auth checking that re-implements what BasePageHandler does. Also spins with `while (window.authCheckInProgress)`.
- **Fix:** Extend `BasePageHandler` like other pages.

### FRONT-P1-025 · `OwnerDashboardHandler` uses `window.apiService` directly
- **File:** `frontend/src/js/pages/owner-dashboard-page.js`
- **Description:** Uses `window.apiService.getShipments({ limit: 1 })` to "get count" — but `limit: 1` isn't a standard API param. It actually returns one item and reads `.data?.length || 0`, which is always 0 or 1.
- **Fix:** Use the dashboard statistics endpoint.

### FRONT-P1-026 · UTF-8 encoding issues (mojibake) in many JS files
- **Files:** `courier-dashboard-page.js`, `owner-shipments-page.js`, `owner-employees-page.js`, and others
- **Description:** Arabic strings appear as `Ù„Ø§ ØªÙˆØ¬Ø¯` instead of proper Arabic. Files were likely saved in a wrong encoding.
- **Fix:** Re-save all affected files as UTF-8 without BOM.

### FRONT-P1-027 · `escapeHtml()` used as a global function without import
- **Files:** `owner-dashboard-page.js`, `owner-shipments-page.js`, `courier-dashboard-page.js`, many others
- **Description:** `escapeHtml()` is called as a bare global but it's defined as `SharedDataUtils.escapeHtml()`. Would throw `ReferenceError` at runtime.
- **Fix:** Use `SharedDataUtils.escapeHtml()` or define as global.

### FRONT-P1-028 · `owner-payouts.js` references `DataUtils.formatCurrency()` — undefined
- **File:** `frontend/src/js/pages/owner-payouts.js`
- **Description:** `DataUtils` is not declared or imported. Should be `SharedDataUtils`.
- **Fix:** Replace `DataUtils` with `SharedDataUtils`.

### FRONT-P1-029 · `auth_service.js` references `UIUtils.showLoading()` — not imported / undefined
- **File:** `frontend/src/js/services/auth_service.js`
- **Description:** `changePassword()`, `requestPasswordReset()`, `resetPassword()` call `UIUtils.showLoading/showSuccess` but `UIUtils` is never imported or defined.
- **Fix:** Replace with `GlobalUIHandler` or ensure `UIUtils` is defined globally.

### FRONT-P1-030 · `api_service.js` `getCurrentUser()` catch block references undefined `url` and `options`
- **File:** `frontend/src/js/services/api_service.js` (~line 247)
- **Description:** Catch block logs `url` and `options.method` which are scoped to the `request()` method, not `getCurrentUser()`. Causes `ReferenceError`.
- **Fix:** Log the correct local variables.

### FRONT-P1-031 · Inline `onclick` attributes instead of `addEventListener`
- **Files:** `courier-dashboard-page.js`, `owner-shipments-page.js`, `owner-zones-page.js`, `owner-employees-page.js`
- **Description:** Template literals contain `onclick="handler.method(${id})"`. Violates CSP; relies on globals.
- **Fix:** Use `addEventListener` delegation after rendering rows.

### FRONT-P1-032 · CSP in `login.html` allows `'unsafe-inline'` for scripts
- **File:** `frontend/login.html`
- **Description:** `script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net` weakens CSP.
- **Fix:** Remove `'unsafe-inline'`; move all JS to external files (already done via modules, so just remove the directive).

---

## P2 — Medium

### BACK-P2-001 · Inner DTO classes (public fields) inside controllers
- **Files:** `FinancialController.CreatePayoutRequest`, `ManifestController`, `UserController.CreateUserRequest`
- **Description:** DTOs with `public` fields; should be in `dto/` package with proper encapsulation.
- **Fix:** Move to `com.twsela.dto.*`; use private fields + getters.

### BACK-P2-002 · User entity has no email field
- **File:** `twsela/src/main/java/com/twsela/domain/User.java`
- **Description:** Only `phone` field. Password reset and notifications need email.
- **Fix:** Add an `email` column.

### BACK-P2-003 · `User.setActive(boolean)` toggles `isDeleted` — confusing
- **File:** `twsela/src/main/java/com/twsela/domain/User.java`
- **Description:** `setActive(true)` sets `isDeleted = false`. Counter-intuitive API.
- **Fix:** Rename or separate the concerns.

### BACK-P2-004 · `User` phone regex `^[0-9]{10,15}$` doesn't match Egyptian format
- **File:** `twsela/src/main/java/com/twsela/domain/User.java`
- **Description:** E.164 Egyptian phones (e.g., `+201023782584`) include `+` which this regex rejects.
- **Fix:** Use `^\+?[0-9]{10,15}$` or a proper Egyptian pattern.

### BACK-P2-005 · `Shipment` entity has no `@PreUpdate` for `updatedAt`
- **File:** `twsela/src/main/java/com/twsela/domain/Shipment.java`
- **Description:** `updatedAt` is only set manually. Forgotten updates leave stale timestamps.
- **Fix:** Add `@PreUpdate` callback.

### BACK-P2-006 · `DeliveryPricing` uses `FetchType.EAGER` for both relations
- **File:** `twsela/src/main/java/com/twsela/domain/DeliveryPricing.java`
- **Description:** Both `merchant` and `zone` are EAGER. Loading a pricing list loads all merchants and zones.
- **Fix:** Change to LAZY; use fetch joins where needed.

### BACK-P2-007 · `ShipmentManifest` uses `FetchType.EAGER` for courier
- **File:** `twsela/src/main/java/com/twsela/domain/ShipmentManifest.java`
- **Description:** Similar EAGER issue.
- **Fix:** Change to LAZY.

### BACK-P2-008 · `Payout.user` uses `FetchType.EAGER`
- **File:** `twsela/src/main/java/com/twsela/domain/Payout.java`
- **Description:** User entity and its relations loaded for every payout.
- **Fix:** Change to LAZY.

### BACK-P2-009 · `Zone` entity has no `updatedAt` timestamp
- **File:** `twsela/src/main/java/com/twsela/domain/Zone.java`
- **Description:** Only `createdAt` exists. No audit trail for modifications.
- **Fix:** Add `updatedAt` with `@PreUpdate`.

### BACK-P2-010 · `MasterDataController` caches zones by username — busted on role-change
- **File:** `twsela/src/main/java/com/twsela/web/MasterDataController.java`
- **Description:** `@Cacheable(value = "zones", key = "T(String).valueOf(#authentication.name)")`. If a user's role changes from ADMIN→OWNER, they still see the ADMIN cache.
- **Fix:** Include role in cache key or evict on role change.

### BACK-P2-011 · `MasterDataController.getAllPricing()` has dead-code branch
- **File:** `twsela/src/main/java/com/twsela/web/MasterDataController.java`
- **Description:** ADMIN branch is identical to OWNER branch — both call `findAll()`.
- **Fix:** Implement an `isActive` filter for ADMINs.

### BACK-P2-012 · `JwtAuthenticationFilter` hard-skips specific paths instead of using SecurityConfig
- **File:** `twsela/src/main/java/com/twsela/security/JwtAuthenticationFilter.java`
- **Description:** `if (requestUri.equals("/api/auth/login") ...)` duplicates what `SecurityConfig.permitAll()` should handle.
- **Fix:** Remove redundant path checks; rely on SecurityConfig.

### BACK-P2-013 · `JwtAuthenticationFilter` calls `e.printStackTrace()` — not proper logging
- **File:** `twsela/src/main/java/com/twsela/security/JwtAuthenticationFilter.java` (line ~95)
- **Description:** Uses `e.printStackTrace()` instead of SLF4J logger.
- **Fix:** `log.error("JWT processing error", e)`.

### BACK-P2-014 · `JwtService` uses deprecated `SignatureAlgorithm.HS256` constant
- **File:** `twsela/src/main/java/com/twsela/security/JwtService.java`
- **Description:** JJWT 0.11.5 deprecates the `SignatureAlgorithm` enum.
- **Fix:** Upgrade JJWT to 0.12.x and use `Jwts.SIG.HS256`.

### BACK-P2-015 · No database migration tool (Flyway/Liquibase)
- **File:** `twsela/src/main/resources/application.yml`
- **Description:** `ddl-auto: validate` but no migration scripts found. Schema changes require manual SQL.
- **Fix:** Add Flyway and baseline migration scripts.

### BACK-P2-016 · `application.yml` has no profile separation for dev/prod
- **File:** `twsela/src/main/resources/application.yml`
- **Description:** Single file mixes dev defaults (H2, SSL disabled) with prod paths.
- **Fix:** Split into `application-dev.yml` and `application-prod.yml`.

### BACK-P2-017 · Swagger UI is accessible in production config
- **File:** `twsela/src/main/resources/application.yml`
- **Description:** No profile-based toggle for SpringDoc.
- **Fix:** Disable Swagger UI in prod profile.

### BACK-P2-018 · `ShipmentRepository` has an unwieldy 3-field OR query method
- **File:** `twsela/src/main/java/com/twsela/repository/ShipmentRepository.java`
- **Description:** `findByTrackingNumberContainingIgnoreCaseOrRecipientNameContainingIgnoreCaseOrRecipientPhoneContainingIgnoreCase` — 100+ chars.
- **Fix:** Use `@Query` with a cleaner JPQL or Specification.

### BACK-P2-019 · JJWT version 0.11.5 is outdated
- **File:** `twsela/pom.xml`
- **Description:** Latest is 0.12.6. Version 0.11.5 has deprecated APIs.
- **Fix:** Upgrade `jjwt.version` to `0.12.6`.

### BACK-P2-020 · Both iText7 AND OpenPDF in dependencies
- **File:** `twsela/pom.xml`
- **Description:** Two PDF libraries. Extra attack surface and dependency bloat.
- **Fix:** Remove one (keep iText7 for Arabic shaping).

### BACK-P2-021 · H2 runtime dependency in main scope
- **File:** `twsela/pom.xml`
- **Description:** H2 is listed both as `runtime` in main deps and `test` scope. Runtime scope means it ships in production.
- **Fix:** Keep only the `test` scope entry.

### BACK-P2-022 · No CSRF protection for state-changing operations
- **File:** `twsela/src/main/java/com/twsela/security/SecurityConfig.java`
- **Description:** `csrf().disable()` in SecurityConfig. Copilot instructions say CSRF protection is required.
- **Fix:** Enable CSRF for non-API browser sessions or confirm JWT-only usage.

### BACK-P2-023 · No test coverage for `MasterDataController`, `ManifestController`, `SmsController`
- **Files:** `twsela/src/test/java/`
- **Description:** Tests exist for 7 controllers/services. Several controllers have zero tests.
- **Fix:** Add integration tests for all controllers.

### BACK-P2-024 · Multiple overloaded `createShipment()` methods in `ShipmentService`
- **File:** `twsela/src/main/java/com/twsela/service/ShipmentService.java`
- **Description:** Several variants (Shipment only, Shipment+merchantId, Shipment+merchantId+zoneId). Hard to maintain.
- **Fix:** Consolidate into a single method accepting a builder/DTO.

### FRONT-P2-025 · `OwnerDashboardHandler` does not extend `BasePageHandler` consistently
- **File:** `frontend/src/js/pages/owner-dashboard-page.js`
- **Description:** Missing Logger import unlike other pages. Missing `super.init()` call patterns.
- **Fix:** Align with other page handlers.

### FRONT-P2-026 · `owner-payouts.js` does NOT extend `BasePageHandler`
- **File:** `frontend/src/js/pages/owner-payouts.js`
- **Description:** Has its own `init()` lifecycle. All other role pages extend `BasePageHandler`.
- **Fix:** Refactor to extend `BasePageHandler` for consistency.

### FRONT-P2-027 · `owner-zones-page.js` is procedural, not class-based
- **File:** `frontend/src/js/pages/owner-zones-page.js`
- **Description:** Uses standalone functions + `DOMContentLoaded` instead of the class-based `BasePageHandler` pattern.
- **Fix:** Refactor to a class extending `BasePageHandler`.

### FRONT-P2-028 · No frontend error boundary / fallback UI
- **Files:** All page JS files
- **Description:** If API fails or JS throws, users see a blank or broken page with no feedback.
- **Fix:** Add a global error handler and fallback UI.

### FRONT-P2-029 · `merchant-dashboard.html` has wrong CSS path
- **File:** `frontend/merchant/dashboard.html`
- **Description:** `href="../../src/css/styles.css"` — path traversal from `merchant/` goes up two levels, which may not resolve correctly.
- **Fix:** Verify and fix to `../src/css/styles.css`.

### FRONT-P2-030 · `shipmentCRUD` uses `/api/shipments/list` as base endpoint for all CRUD
- **File:** `frontend/src/js/services/api_service.js`
- **Description:** Create/update/delete also target `/api/shipments/list/*` which is likely only a GET endpoint on the backend.
- **Fix:** Separate the list endpoint from the CRUD base endpoint.

### FRONT-P2-031 · `NotificationService` falls back to `alert()` if `notificationManager` undefined
- **File:** `frontend/src/js/shared/NotificationService.js`
- **Description:** `alert()` is blocking and bad UX.
- **Fix:** Implement inline toast fallback; never use `alert()`.

### FRONT-P2-032 · `auth_service.js` `checkAuthStatus()` returns `true` on network errors
- **File:** `frontend/src/js/services/auth_service.js`
- **Description:** Catch block returns `true` "to keep user logged in on network errors". This means an attacker can block auth/me and bypass authentication.
- **Fix:** Return `false` or show offline banner.

### FRONT-P2-033 · Sidebar navigation hardcoded in every HTML file
- **Files:** All dashboard HTML files (owner, merchant, courier, warehouse, admin)
- **Description:** Identical sidebar markup duplicated in 20+ HTML files.
- **Fix:** Use a shared sidebar component (JS-injected or build-time include).

### FRONT-P2-034 · 'Remember Me' checkbox has no implementation
- **File:** `frontend/login.html`, `frontend/src/js/pages/login.js`
- **Description:** Checkbox exists in HTML but login flow doesn't read it.
- **Fix:** Implement token persistence in `localStorage` when checked.

### FRONT-P2-035 · `login.html` forgot-password link goes to `settings.html`
- **File:** `frontend/login.html`
- **Description:** `<a href="settings.html" id="forgotPasswordLink">` — should open the forgot-password modal or a dedicated page.
- **Fix:** Change to open `#forgotPasswordModal` or a reset page.

### FRONT-P2-036 · `merchant/shipment-details.html` is in sidebar but requires a shipment ID to work
- **File:** `frontend/merchant/dashboard.html`
- **Description:** Static sidebar link to `shipment-details.html` without an ID param makes no sense.
- **Fix:** Remove from sidebar; access only from shipments table.

### FRONT-P2-037 · No loading skeletons or spinners during data fetch
- **Files:** Most page handlers
- **Description:** Tables show empty, then suddenly populate. No feedback while loading.
- **Fix:** Show skeleton loaders or a spinner overlay.

### FRONT-P2-038 · `BasePageHandler.showError()` is a no-op
- **File:** `frontend/src/js/shared/BasePageHandler.js`
- **Description:** Method body is empty — errors are swallowed silently.
- **Fix:** Use `NotificationService.error()`.

### FRONT-P2-039 · `HTMLmeta` viewport line has escaped newlines in owner/merchant dashboards
- **Files:** `frontend/owner/dashboard.html`, `frontend/merchant/dashboard.html`
- **Description:** Rendered source has `\`n` characters in meta tags — likely a copy/paste bug.
- **Fix:** Replace with proper line breaks.

### FRONT-P2-040 · No 403 / access-denied page
- **Files:** All frontend
- **Description:** If a merchant accesses `/owner/dashboard.html`, they get redirected to login instead of seeing an access-denied page.
- **Fix:** Add a 403 page; redirect there on role mismatch.

### FRONT-P2-041 · `owner-dashboard-page.js` has `Date.toLocaleDateString('ar-SA')` not `'ar-EG'`
- **File:** `frontend/src/js/pages/owner-dashboard-page.js`
- **Description:** Saudi date formatting used in Egyptian context.
- **Fix:** Change to `'ar-EG'`.

### FRONT-P2-042 · `courier-dashboard-page.js` uses `escapeHtml` in `innerHTML` but not consistently
- **File:** `frontend/src/js/pages/courier-dashboard-page.js`
- **Description:** Some fields use `escapeHtml()`, others (e.g., `delivery.customerName`) are inserted raw.
- **Fix:** Escape all user-supplied data.

---

## P3 — Low

### BACK-P3-001 · Shipment entity has `setCourier()` kept for "backward compatibility"
- **File:** `twsela/src/main/java/com/twsela/domain/Shipment.java`
- **Description:** Dead code that confuses developers.
- **Fix:** Remove after verifying no callers.

### BACK-P3-002 · Office data hardcoded in `PublicController`
- **File:** `twsela/src/main/java/com/twsela/web/PublicController.java`
- **Description:** Phone, email, address hardcoded in controller.
- **Fix:** Move to configuration or database.

### BACK-P3-003 · Missing `toString()` on several entities
- **Files:** `User.java`, `Zone.java`, `Payout.java` (partial)
- **Description:** Debugging is harder without readable `toString()`.
- **Fix:** Add `toString()` to all entities.

### BACK-P3-004 · No caching on `getAllPricing()`
- **File:** `twsela/src/main/java/com/twsela/web/MasterDataController.java`
- **Description:** `@Cacheable(value = "pricing", key = "'all'")` ignores `authentication` param — cache key doesn't distinguish roles.
- **Fix:** Add role to cache key.

### BACK-P3-005 · `ShipmentStatusConstants.ALL_STATUSES` uses array instead of `List.of()`
- **File:** `twsela/src/main/java/com/twsela/domain/ShipmentStatusConstants.java`
- **Description:** Array is mutable.
- **Fix:** Use `List.of(...)` for immutability.

### BACK-P3-006 · `SecurityConfig` has `WAREHOUSE_MANAGER` role missing from shipment access
- **File:** `twsela/src/main/java/com/twsela/security/SecurityConfig.java`
- **Description:** `/api/shipments/**` allows OWNER, ADMIN, MERCHANT, COURIER — but not WAREHOUSE_MANAGER, who needs to receive/dispatch.
- **Fix:** Add WAREHOUSE_MANAGER to the access list.

### BACK-P3-007 · No request/response logging middleware
- **Description:** Only `AuditService` logs certain events. Full request/response tracing is missing.
- **Fix:** Add `CommonsRequestLoggingFilter` or MDC-based access logging.

### BACK-P3-008 · `RateLimitFilter` uses custom `RateBucket` — consider Spring's Bucket4j starter
- **File:** `twsela/src/main/java/com/twsela/security/RateLimitFilter.java`
- **Description:** Custom rate-limit implementation; Bucket4j already in dependency list.
- **Fix:** Consolidate to one mechanism.

### BACK-P3-009 · ShipmentManifest has no `updatedAt` field
- **File:** `twsela/src/main/java/com/twsela/domain/ShipmentManifest.java`
- **Fix:** Add `updatedAt` with `@PreUpdate`.

### BACK-P3-010 · `GlobalExceptionHandler` doesn't log the original exception for all handlers
- **File:** `twsela/src/main/java/com/twsela/web/GlobalExceptionHandler.java`
- **Fix:** Ensure every handler logs at `WARN` or `ERROR` level.

### BACK-P3-011 · No API versioning (`/api/v1/`)
- **Description:** All endpoints use bare `/api/`. Future breaking changes cannot coexist.
- **Fix:** Add `v1` prefix via `server.servlet.context-path` or `@RequestMapping`.

### BACK-P3-012 · `Nginx` config nested `location` loses parent security headers
- **File:** `twsela/nginx.conf`
- **Description:** Nested `location ~* \.(js|css|...)` block must re-add all security headers because Nginx clears parent `add_header` directives.
- **Fix:** Already partially done; verify all 6 headers are re-added (CSP is missing in the nested block).

### BACK-P3-013 · Docker Compose `version: '3.8'` is deprecated
- **File:** `twsela/docker-compose.monitoring.yml`
- **Fix:** Remove the `version` key (Docker Compose v2+ ignores it).

### BACK-P3-014 · Grafana admin password via env without secrets management
- **File:** `twsela/docker-compose.monitoring.yml`
- **Description:** `GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_ADMIN_PASSWORD}` — no Docker secrets or vault.
- **Fix:** Use Docker secrets or .env file with restricted permissions.

### BACK-P3-015 · Redis in Docker has no password
- **File:** `twsela/docker-compose.monitoring.yml`
- **Description:** `redis-server --appendonly yes` without `--requirepass`.
- **Fix:** Set `requirepass` and configure in `application.yml`.

### FRONT-P3-016 · `TwselaApp` class in `app.js` is very large (678 lines)
- **File:** `frontend/src/js/app.js`
- **Fix:** Split into `AppRouter`, `AppAuth`, `AppUIBootstrapper`.

### FRONT-P3-017 · `api_service.js` is 1148 lines — too large
- **File:** `frontend/src/js/services/api_service.js`
- **Fix:** Split into domain-specific services (ShipmentApiService, UserApiService, etc.).

### FRONT-P3-018 · `SharedDataUtils.js` has 569 lines of mixed concerns
- **File:** `frontend/src/js/shared/SharedDataUtils.js`
- **Fix:** Split into `FormatUtils`, `ValidationUtils`, `StatusUtils`.

### FRONT-P3-019 · `GlobalUIHandler.js` builds full HTML table rows (775 lines)
- **File:** `frontend/src/js/shared/GlobalUIHandler.js`
- **Fix:** Move table rendering to per-entity renderers; keep GlobalUIHandler for modals/spinners.

### FRONT-P3-020 · No Vite build configuration for cache-busting
- **File:** `frontend/vite.config.js`
- **Description:** Static asset names don't include content hashes.
- **Fix:** Configure Vite output filenames with hashes.

### FRONT-P3-021 · CDN dependencies (Bootstrap, FontAwesome) have no SRI hashes
- **Files:** All HTML files
- **Description:** `<link>` and `<script>` tags from CDN lack `integrity` attributes.
- **Fix:** Add SRI hashes for every CDN resource.

### FRONT-P3-022 · No PWA / offline support
- **Description:** No `manifest.json`, no service worker.
- **Fix:** Add basic PWA support for couriers in the field.

### FRONT-P3-023 · No accessibility (ARIA) attributes in dashboard cards/tables
- **Files:** All HTML dashboard files
- **Description:** Screen readers can't navigate dashboard KPIs or tables meaningfully.
- **Fix:** Add `aria-label`, `role="status"`, `<caption>` for tables.

### FRONT-P3-024 · `login.html` has duplicate `class` attribute on `#otpInput` div
- **File:** `frontend/login.html`
- **Description:** `<div class="mb-3" id="otpInput" class="d-none">` — second `class` is ignored by browsers.
- **Fix:** Merge into one: `class="mb-3 d-none"`.

### FRONT-P3-025 · Register link in login page goes to `#` (nowhere)
- **File:** `frontend/login.html`
- **Description:** `<a href="#" id="registerLink">` has no handler.
- **Fix:** Link to a register page or hide the link.

### FRONT-P3-026 · `courier-manifest.html` exists but no corresponding JS page handler found
- **File:** `frontend/courier/manifest.html`
- **Description:** Page exists in HTML but `courier-manifest-page.js` may not be loaded.
- **Fix:** Verify and create the page handler.

### FRONT-P3-027 · `owner-settings-page.js` likely has no backend persistence (see P0-003)
- **File:** `frontend/src/js/pages/owner-settings-page.js`
- **Description:** Frontend saves settings but backend discards them.
- **Fix:** Fix together with BACK-P0-003.

### FRONT-P3-028 · `contact.html` form goes nowhere (frontend + backend stub)
- **Files:** `frontend/contact.html`, `PublicController.submitContactForm()`
- **Description:** Both sides are non-functional.
- **Fix:** Complete the full flow.

---

## Sprint Allocation Suggestion

### Sprint 6 — Security & Critical Fixes (P0 items)
| # | Item |
|---|------|
| 1 | BACK-P0-001 SecureRandom for password reset |
| 2 | BACK-P0-002 Remove OTP from response |
| 3 | BACK-P0-010 MasterData input validation / DTOs |
| 4 | BACK-P0-012 Soft-delete zones |
| 5 | FRONT-P0-013 Fix SAR→EGP currency |
| 6 | FRONT-P0-014 Fix phone regex (Saudi→Egyptian) |
| 7 | BACK-P1-005 LoginResponseDTO (don't leak password hash) |
| 8 | BACK-P1-006 Token blacklist (Redis) |
| 9 | BACK-P1-007 SecureRandom for tracking numbers |
| 10 | BACK-P1-004 Add @Valid everywhere |
| 11 | FRONT-P1-032 Remove `unsafe-inline` from CSP |
| 12 | FRONT-P1-029 Fix UIUtils undefined references |
| 13 | FRONT-P1-030 Fix undefined `url` in api_service catch |
| 14 | FRONT-P2-032 Fix auth returning `true` on network error |

### Sprint 7 — Performance & Data Integrity (P0/P1 perf items)
| # | Item |
|---|------|
| 1 | BACK-P0-004 ReportsController — push filtering to DB |
| 2 | BACK-P0-005 Warehouse report — proper status queries |
| 3 | BACK-P0-006 Revenue chart — query real data |
| 4 | BACK-P0-007 Dashboard week/month stats fix |
| 5 | BACK-P0-008/009 Date-range filtering for earnings/payouts |
| 6 | BACK-P0-003 Settings persistence (create entity + table) |
| 7 | BACK-P0-011 Fix updatePricing commented-out fields |
| 8 | BACK-P1-001/002 DB-level pagination for users |
| 9 | BACK-P1-008 Use findAllById for assignment |
| 10 | BACK-P1-013 Pagination on payouts/manifests |
| 11 | BACK-P1-010 OTP in Redis |
| 12 | BACK-P1-019 Configurable timezone |
| 13 | BACK-P2-005 @PreUpdate for Shipment.updatedAt |
| 14 | BACK-P2-006/007/008 EAGER→LAZY for pricing/manifest/payout |

### Sprint 8 — Backend Cleanup & API Quality
| # | Item |
|---|------|
| 1 | BACK-P1-003 Consistent soft-delete in UserService |
| 2 | BACK-P1-009 Extract shared getCurrentUser() |
| 3 | BACK-P1-011 Add @Operation annotations everywhere |
| 4 | BACK-P1-012 Replace Map<String,Object> with DTOs |
| 5 | BACK-P1-014 Remove duplicated pricing logic |
| 6 | BACK-P1-015 Remove no-op setCourier() |
| 7 | BACK-P1-016/017/018 Fix PublicController track/feedback/contact |
| 8 | BACK-P1-020 Fix catch-all RuntimeException |
| 9 | BACK-P1-021/022 Fix Dockerfile port + Docker init script |
| 10 | BACK-P2-001 Move inner DTOs to dto package |
| 11 | BACK-P2-012/013 Clean up JwtAuthFilter |
| 12 | BACK-P2-015 Add Flyway migrations |
| 13 | BACK-P2-016 Profile separation (dev/prod) |
| 14 | BACK-P2-017 Disable Swagger in prod |
| 15 | BACK-P2-018 Clean up ShipmentRepository query names |
| 16 | BACK-P2-019/020/021 Dependency cleanup (JJWT, iText/OpenPDF, H2) |
| 17 | BACK-P2-023 Add missing tests |
| 18 | BACK-P2-024 Consolidate createShipment overloads |

### Sprint 9 — Frontend Overhaul
| # | Item |
|---|------|
| 1 | FRONT-P1-023 Replace spin-wait with Promise-based service init |
| 2 | FRONT-P1-024 Refactor owner-zones-page to use BasePageHandler |
| 3 | FRONT-P1-025 Fix OwnerDashboard count logic |
| 4 | FRONT-P1-026 Fix UTF-8 encoding across all JS files |
| 5 | FRONT-P1-027 Fix escapeHtml global reference |
| 6 | FRONT-P1-028 Fix DataUtils→SharedDataUtils |
| 7 | FRONT-P1-031 Replace inline onclick with addEventListener |
| 8 | FRONT-P2-025/026/027 Align all pages to BasePageHandler pattern |
| 9 | FRONT-P2-028 Add global error boundary / fallback UI |
| 10 | FRONT-P2-029 Fix merchant dashboard CSS path |
| 11 | FRONT-P2-030 Fix shipmentCRUD base endpoint |
| 12 | FRONT-P2-031 Replace alert() fallback in NotificationService |
| 13 | FRONT-P2-033 Shared sidebar component |
| 14 | FRONT-P2-034 Implement Remember Me |
| 15 | FRONT-P2-035 Fix forgot-password link |
| 16 | FRONT-P2-036 Remove shipment-details from sidebar |
| 17 | FRONT-P2-037/038 Loading skeletons + fix showError no-op |
| 18 | FRONT-P2-039 Fix escaped newlines in HTML meta |
| 19 | FRONT-P2-040 Add 403 page |
| 20 | FRONT-P2-041/042 Fix ar-SA→ar-EG and inconsistent escaping |

### Sprint 10 — Polish, Infra, Missing Features
| # | Item |
|---|------|
| 1 | BACK-P2-002 Add email field to User |
| 2 | BACK-P2-003 Rename User.setActive confusion |
| 3 | BACK-P2-004 Fix User phone regex for Egyptian format |
| 4 | BACK-P2-009/010/011 Zone updatedAt, cache key fix, dead code |
| 5 | BACK-P2-022 Review CSRF posture |
| 6 | BACK-P3-001 through P3-015 (all backend P3 items) |
| 7 | FRONT-P3-016/017/018/019 Split large JS files |
| 8 | FRONT-P3-020 Vite cache-busting |
| 9 | FRONT-P3-021 SRI hashes for CDN |
| 10 | FRONT-P3-022 PWA basics for couriers |
| 11 | FRONT-P3-023 Accessibility (ARIA) |
| 12 | FRONT-P3-024/025/026/027/028 HTML/page fixes |

---

*End of audit — 116 actionable items across 5 sprints.*
