# Twsela Frontend - Comprehensive Code Audit Report

> **Generated**: 2025  
> **Scope**: All files under `frontend/` directory  
> **Total Files Analyzed**: ~107 source files (excluding `node_modules`)

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Architecture Overview](#2-architecture-overview)
3. [Critical Issues (Must Fix)](#3-critical-issues-must-fix)
4. [High-Priority Issues](#4-high-priority-issues)
5. [Medium-Priority Issues](#5-medium-priority-issues)
6. [Low-Priority Issues](#6-low-priority-issues)
7. [File-by-File Analysis](#7-file-by-file-analysis)
8. [Security Audit](#8-security-audit)
9. [Performance Audit](#9-performance-audit)
10. [Accessibility & RTL/Arabic Audit](#10-accessibility--rtlarabic-audit)
11. [Recommendations](#11-recommendations)

---

## 1. Executive Summary

The Twsela frontend is a **vanilla JavaScript + HTML/CSS** courier management system supporting multiple user roles (Owner, Admin, Merchant, Courier, Warehouse). The codebase has **two parallel architectures** — a legacy JS system currently in use and an unintegrated TypeScript module system — creating significant maintenance overhead and confusion.

### Key Metrics
| Metric | Value |
|--------|-------|
| Total HTML files | ~20 |
| Total JS files (legacy) | ~25 |
| Total TS files (unused) | ~18 |
| Total CSS files | ~25 |
| Largest JS file | `api_service.js` (1,143 lines) |
| Largest CSS file | `styles.css` (2,606 lines) |
| Build tool | Vite 4.4.11 (misconfigured for current usage) |
| UI framework | Bootstrap 5.3.2 (CDN) |

### Overall Health: ⚠️ Needs Significant Work

| Category | Rating | Notes |
|----------|--------|-------|
| Architecture | 🔴 Poor | Dual JS/TS codebase, dead TS code |
| Security | 🔴 Poor | XSS vectors, unsafe auth patterns |
| Code Quality | 🟡 Fair | Inconsistent patterns, empty catch blocks |
| Performance | 🟡 Fair | No code splitting, CDN dependencies |
| Accessibility | 🔴 Poor | Missing ARIA, keyboard nav, focus management |
| RTL/Arabic | 🟡 Fair | RTL present but inconsistent |
| Error Handling | 🟡 Fair | Empty catch blocks throughout |
| Testing | 🔴 None | Zero tests |

---

## 2. Architecture Overview

### Current Architecture (In Use)
```
HTML pages → <script> tags → Global window objects
                                  ↓
                    TwselaApp (app.js) orchestrator
                           ↓
              AuthService + ApiService (globals)
                           ↓
           Page Handlers (BasePageHandler subclasses)
                           ↓
         SharedDataUtils + GlobalUIHandler (static utilities)
```

### Unused TypeScript Architecture
```
ES Modules → import/export → Store pattern
                                ↓
                    api.ts → auth.ts → services/*.ts
                           ↓
                    pages/*.ts → types/index.ts
```

**Critical Problem**: The TypeScript files are **never loaded** by any HTML page. They represent a parallel, disconnected codebase with different API base URLs, different state management, and different patterns. This is dead code that creates confusion.

### Dependency Map
| Dependency | Version | Loading Method |
|-----------|---------|----------------|
| Bootstrap | 5.3.2 | CDN `<link>` + `<script>` |
| Font Awesome | 6.4.0 | CDN `<link>` |
| Chart.js | 4.x | CDN `<script>` |
| Noto Sans Arabic | - | Google Fonts import in CSS |
| axios | ^1.6.0 | npm (TS only, unused) |
| date-fns | ^2.30.0 | npm (TS only, unused) |
| html-to-docx | ^1.8.0 | npm (unused) |
| lodash | ^4.17.21 | npm (unused) |
| marked | ^16.4.1 | npm (unused) |

---

## 3. Critical Issues (Must Fix)

### CRIT-1: XSS Vulnerabilities via Template Literals
**Files**: `GlobalUIHandler.js`, `owner-employees-page.js`, `owner-shipments-page.js`, `owner-zones-page.js`, `courier-dashboard-page.js`, `merchant-dashboard-page.js`, `owner-reports-page.js`

All table rows are generated using template literals that directly interpolate server data without sanitization:

```javascript
// GlobalUIHandler.js - createShipmentRow()
row.innerHTML = `<td>${shipment.recipientName || 'غير محدد'}</td>`;

// owner-employees-page.js
row.innerHTML = `<td>${employee.name || 'غير محدد'}</td>`;
```

If any API response contains malicious HTML/JavaScript (e.g., `<script>alert('xss')</script>` as a name), it will be executed. `SharedDataUtils.sanitizeHTML()` exists but is **never used** in table row generation.

**Fix**: Use `SharedDataUtils.sanitizeHTML()` on all interpolated values, or use `textContent` instead of `innerHTML`.

---

### CRIT-2: Undefined Variable References in `api_service.js`
**File**: `src/js/services/api_service.js`

The `verifyToken()` and `getCurrentUser()` methods reference undefined variables `url` and `options` in their catch blocks:

```javascript
async verifyToken() {
    const url = `${this.baseUrl}/api/auth/verify`;
    // ...
    catch (error) {
        console.error(`API Error [GET ${url}]:`, error); // 'url' is defined
        // But 'options' is referenced somewhere — undefined
    }
}
```

This will cause `ReferenceError` at runtime when these methods encounter errors.

---

### CRIT-3: Auth Returns `true` on Network Errors
**File**: `src/js/services/auth_service.js`

```javascript
async checkAuthStatus() {
    // ...
    catch (error) {
        // Returns TRUE on error to prevent logout on temporary network issues
        return true;
    }
}
```

This means a user with an expired or revoked token will remain "authenticated" during any network issue. An attacker could exploit this by blocking the auth endpoint while using a stolen/expired token.

**Fix**: Return `false` on error with a separate retry mechanism, or cache the last successful auth check result with a timestamp.

---

### CRIT-4: Sensitive Data in `localStorage`
**Files**: `auth_service.js`, `auth.ts`, `app.js`

JWT tokens and full user objects (including role, permissions) are stored in `localStorage`:

```javascript
localStorage.setItem('twsela_token', token);
localStorage.setItem('twsela_user', JSON.stringify(user));
```

`localStorage` is accessible to any JavaScript running on the page. Combined with CRIT-1 (XSS), an attacker could steal tokens. Consider using `httpOnly` cookies instead.

---

## 4. High-Priority Issues

### HIGH-1: Dead TypeScript Codebase
**Files**: All `*.ts` files (~18 files)

The entire TypeScript codebase (`api.ts`, `auth.ts`, `shipment.ts`, `courier.ts`, `merchant.ts`, `report.ts`, `zone.ts`, `base-page.ts`, `store/index.ts`, `types/index.ts`, and all `pages/*.ts`) is never loaded by any HTML page. It uses different API URLs (`localhost:8080` vs `localhost:8000`), different state management (Store vs globals), and different patterns.

**Impact**: ~3,000+ lines of dead code that must be maintained or removed.  
**Fix**: Either migrate fully to TS modules with Vite's build pipeline, or remove the TS files entirely.

### HIGH-2: Inconsistent Service Access Patterns
**Files**: Multiple page handlers

Some pages use `window.apiService` directly:
```javascript
// owner-zones-page.js
const response = await window.apiService.getZones();
```

Others use `this.services.api`:
```javascript
// owner-employees-page.js (extends BasePageHandler)
const response = await this.services.api.getEmployees({...});
```

And some mix both:
```javascript
// owner-dashboard-page.js
const [shipmentsResponse] = await Promise.allSettled([
    window.apiService.getShipments({ limit: 1 })
]);
```

**Impact**: Confusing, error-prone, and fragile if service initialization order changes.

### HIGH-3: `\`n` Artifacts in HTML Meta Tags
**Files**: `merchant/create-shipment.html`, `404.html`, `contact.html`, `settings.html`, and likely others

```html
<meta name="viewport" content="width=device-width, initial-scale=1.0">`n    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">`n
```

The literal string `` `n `` (backtick + n) appears between meta tags, likely from a broken template literal or automated script. This produces visible garbage text in the HTML `<head>` and may cause meta tags to be misinterpreted.

### HIGH-4: Empty Error Handlers Throughout Codebase
**Files**: `owner-payouts.js`, `owner-shipments-page.js`, `merchant-dashboard-page.js`, `owner-reports-page.js`, `merchant-create-shipment.js`

Dozens of catch blocks are completely empty:
```javascript
} catch (error) {

}
```

This silently swallows errors, making debugging extremely difficult in production.

### HIGH-5: Broken Navigation Links
**Files**: Multiple HTML files

- `settings.html` links to `/dashboard.html` which doesn't exist (should be role-based)
- `contact.html` loads `contact.js` which doesn't exist in the project
- Sidebar nav links use paths like `/merchant/dashboard` (no `.html` extension) which won't work without server-side URL rewriting
- `owner/merchants` and `owner/zones` exist as extension-less files (likely empty/broken)

### HIGH-6: No Vite Build Configuration for Multi-Page App
**File**: `vite.config.js`

```javascript
export default defineConfig({
    root: '.',
    server: { port: 5173 },
    build: { sourcemap: true }
});
```

For a multi-page app with 20+ HTML files, Vite needs `rollupOptions.input` to build all pages. Currently only `index.html` would be built. The TypeScript path aliases in `tsconfig.json` are also not configured in Vite's `resolve.alias`.

### HIGH-7: Race Conditions in Initialization
**Files**: `app.js`, `owner-zones-page.js`, `owner-employees-page.js`

Multiple mechanisms compete for auth checking:
- `app.js` checks auth on load with a 100ms delay
- `BasePageHandler` checks auth in its constructor
- `owner-zones-page.js` implements its own `waitForAppInitialization()` with 50 retries
- Global flags like `window.authCheckInProgress` are used to coordinate

This creates brittle race conditions where pages may or may not have auth info when they start rendering.

---

## 5. Medium-Priority Issues

### MED-1: Massive CSS File with Duplicate Utility Classes
**Files**: `styles.css` (2,606 lines), `twsela-design.css` (418 lines), `_variables.css`

`twsela-design.css` reimplements Bootstrap's utility classes (`.d-flex`, `.m-1`, `.p-3`, `.text-primary`, etc.) that are already provided by the Bootstrap CSS loaded via CDN. This creates:
- 418 lines of redundant CSS
- Potential specificity conflicts with `!important` declarations
- Confusion about which utility classes to use

CSS variables are defined in three places:
1. `styles.css` `:root` block
2. `twsela-design.css` `:root` block  
3. `_variables.css` `:root` block

All three define the same variables (e.g., `--primary-color: #667eea`).

### MED-2: Missing Form CSRF Protection
**Files**: All form submissions

No CSRF token is included in form submissions or API calls. While JWT auth provides some protection, state-changing operations should still include CSRF tokens.

### MED-3: `confirm()` and `prompt()` Used for Critical Operations
**Files**: `owner-payouts.js`, `owner-zones-page.js`, `owner-employees-page.js`, `courier-dashboard-page.js`

Native browser dialogs are used for destructive operations:
```javascript
if (confirm('هل أنت متأكد من حذف هذه المنطقة؟')) { ... }
const reason = prompt('يرجى إدخال سبب الرفض:');
```

These are not styleable, not Arabic-friendly, and can be suppressed by the browser. The project already has `GlobalUIHandler.createModal()` which should be used instead.

### MED-4: No Input Sanitization on Search
**Files**: `owner-zones-page.js`, `owner-payouts.js`

Search/filter inputs directly filter table content without sanitization:
```javascript
const searchLower = searchTerm.toLowerCase().trim();
rows.forEach(row => {
    const text = row.textContent.toLowerCase();
    const matches = text.includes(searchLower);
});
```

While `textContent` is safe for reading, the search term should be validated for ReDoS patterns if ever used in regex.

### MED-5: Phone Validation Inconsistency
**Files**: `SharedDataUtils.js`, `merchant-create-shipment.js`, `login.js`

Three different phone validation patterns exist:
- `SharedDataUtils`: `/^(\+20|0)?1[0-2,5][0-9]{8}$/` (Egyptian)
- `merchant-create-shipment.js`: `/^(\+966|0)?[5-9][0-9]{8}$/` (Saudi)  
- `login.js`: `/^01[0-2,5][0-9]{8}$/` (Egyptian simplified)

The system can't decide if it's for Egypt or Saudi Arabia. The currency description also says "جنيه سعودي" (Saudi Pound) mixing Egyptian "جنيه" (Pound) with Saudi context (should be "ريال سعودي" / Saudi Riyal).

### MED-6: `innerHTML` Used Extensively
**Files**: Nearly all page handlers

`innerHTML` is used to build entire tables, modals, and UI sections. While some uses might be safe (hardcoded strings), the pattern is dangerous and should be replaced with DOM manipulation or a templating library.

### MED-7: `.htaccess` Disables All Caching
**File**: `.htaccess`

```
Header set Cache-Control "no-cache, no-store, must-revalidate"
```

This disables caching for ALL JS, CSS, and HTML files. In production, static assets should be cached with versioned filenames. Combined with cache-busting query params already on `<script>` tags (e.g., `?v=2.0.3`), this is redundant and harmful to performance.

### MED-8: No Loading/Error States in Stub Pages
**Files**: `owner-pricing-page.js`, `owner-reports-couriers-page.js`, `owner-reports-merchants-page.js`, `owner-reports-warehouse-page.js`

These are skeleton files with empty function bodies:
```javascript
function updatePricingDisplay(pricingData) {
    // Update pricing plans display
}

function initializeCharts() {
    // Initialize any charts if needed
}
```

They call API endpoints but do nothing with the results. No loading indicators, no error messages, no fallback UI.

---

## 6. Low-Priority Issues

### LOW-1: Verbose Console Logging
**Files**: `owner-zones-page.js` (extensive emoji logging), `app.js`

```javascript
console.log('✅ App.js is initialized, proceeding with zones page');
console.log('🔄 Loading zones data...');
console.log(`🔍 Filter applied: "${searchTerm}" - ${visibleCount} results`);
```

While some files had console.log "removed for cleaner console" (replaced with comments), zones page and app.js still have extensive console output.

### LOW-2: Inconsistent Class vs Function Patterns
**Files**: Page handlers

Some pages use class-based handlers extending `BasePageHandler`:
```javascript
class OwnerDashboardHandler extends BasePageHandler { ... }
```

Others use plain functions:
```javascript
document.addEventListener('DOMContentLoaded', function() {
    initializeZonesPage();
});
```

This inconsistency makes the codebase harder to understand and maintain.

### LOW-3: Cache-Busting Query Strings on Scripts
**Files**: Most HTML files

```html
<script src="../src/js/shared/SharedDataUtils.js?v=2.0.3"></script>
<script src="../src/js/services/auth_service.js?v=2.0.3"></script>
```

Manual cache-busting with hardcoded version strings requires updating every HTML file on each release. A build tool (Vite, already configured but unused) should handle this automatically.

### LOW-4: Duplicate Font Files
**Files**: `frontend/frontend/src/css/fonts/` and `frontend/src/css/fonts/`

The Noto Sans Arabic font files exist in two locations. The `frontend/frontend/` directory appears to be a stale/duplicate structure.

### LOW-5: Copyright Year Hardcoded
**Files**: Multiple HTML files

```html
<p>© 2024 Twsela - جميع الحقوق محفوظة</p>
```

Should be dynamically generated or updated.

### LOW-6: `NotificationService.show()` Calls `.error()` Method
**File**: `404-page.js`

```javascript
window.notificationManager.error('تم إرسال تقرير الخطأ بنجاح. شكراً لك!', 'success');
```

Uses `.error()` method but passes `'success'` as type — confusing API. The method name suggests error, but it's used for all notification types.

### LOW-7: `ProfilePageHandler` Doesn't Extend `BasePageHandler`
**File**: `profile.js`

Unlike other page handlers, `ProfilePageHandler` is standalone with its own init logic, bypassing the shared auth checking in `BasePageHandler`.

---

## 7. File-by-File Analysis

### Configuration Files

| File | Lines | Status | Notes |
|------|-------|--------|-------|
| `package.json` | ~30 | ⚠️ | 5 npm deps (axios, date-fns, html-to-docx, lodash, marked) are **never used** in the active codebase |
| `vite.config.js` | ~15 | 🔴 | Missing multi-page rollup config, missing TS path alias resolution |
| `tsconfig.json` | ~25 | ⚠️ | Path aliases defined but not usable without Vite alias config |
| `.babelrc` | ~5 | ⚠️ | Preset-env targeting Node — wrong for browser frontend |
| `.eslintrc.json` | ~15 | ✅ | Standard config, 2-space indent, single quotes |
| `.prettierrc` | ~5 | ✅ | Standard formatting |
| `.htaccess` | ~15 | ⚠️ | Disables all caching — harmful in production |

### Core JavaScript (`src/js/`)

| File | Lines | Purpose | Issues |
|------|-------|---------|--------|
| `app.js` | 675 | Main orchestrator, auth routing, global setup | Hardcoded `localhost:8000`, 100ms init delay, complex auth coordination |
| `shared/SharedDataUtils.js` | 555 | Data formatting utilities (currency, dates, status badges) | "جنيه سعودي" translation error, unused `sanitizeHTML()` |
| `shared/GlobalUIHandler.js` | 775 | UI utilities, table row/modal HTML generation | XSS via template literals, massive file |
| `shared/BasePageHandler.js` | 357 | Base class for page handlers | Good pattern, but competes with app.js auth checking |
| `shared/NotificationService.js` | ~50 | Notification wrapper | Thin facade over `window.notificationManager`, good fallback |

### Service JavaScript (`src/js/services/`)

| File | Lines | Purpose | Issues |
|------|-------|---------|--------|
| `auth_service.js` | 719 | Authentication, JWT management | Returns `true` on error, 5-min cache, complex global flags |
| `api_service.js` | 1,143 | 80+ API endpoints | Undefined `url`/`options` in catch blocks, massive god-class |

### Page JavaScript (`src/js/pages/`)

| File | Lines | Purpose | Issues |
|------|-------|---------|--------|
| `login.js` | 790 | Login form handling | Large file, Egyptian phone validation, good UX |
| `owner-zones-page.js` | 598 | Zone CRUD management | Own auth checking (duplicates BasePageHandler), doesn't extend BasePageHandler |
| `owner-payouts.js` | 591 | Payout approval/rejection | Uses `confirm()`/`prompt()`, empty catch blocks |
| `owner-reports-page.js` | 533 | Reports & analytics | Loads up to 1000 records per request, empty catches |
| `merchant-create-shipment.js` | 540 | Shipment creation form | Saudi phone regex (contradicts Egyptian elsewhere), good validation |
| `owner-employees-page.js` | 407 | Employee management | Uses inline `onclick` handlers, `alert()` for errors |
| `courier-dashboard-page.js` | 393 | Courier dashboard | Uses `confirm()` for delivery, inline onclick |
| `owner-dashboard-page.js` | 341 | Owner dashboard | Good `Promise.allSettled` usage, but counts from `limit: 1` queries |
| `owner-shipments-page.js` | 314 | Shipment management | TODO stubs for view/edit, inline onclick |
| `merchant-dashboard-page.js` | 319 | Merchant dashboard | Undefined `this.charts` object, empty catches |
| `profile.js` | 266 | User profile | Doesn't extend BasePageHandler, uses `alert()` |
| `404-page.js` | 199 | Error page | `.error()` method used for success messages |
| `courier-manifest-page.js` | 102 | Manifest display | Stub functions empty implementation |
| `owner-pricing-page.js` | 42 | Pricing management | Almost entirely empty stubs |
| `owner-reports-couriers-page.js` | 39 | Courier reports | Empty stubs |
| `owner-reports-merchants-page.js` | 39 | Merchant reports | Empty stubs |
| `owner-reports-warehouse-page.js` | 39 | Warehouse reports | Empty stubs |

### TypeScript Files (All Unused/Dead Code)

| File | Lines | Purpose | Status |
|------|-------|---------|--------|
| `services/api.ts` | ~300 | API service with caching/retry | Different base URL (8080 vs 8000), never loaded |
| `services/auth.ts` | ~170 | Auth service | Uses Store, different patterns, never loaded |
| `services/shipment.ts` | ~100 | Shipment CRUD | Never loaded |
| `services/courier.ts` | ~100 | Courier service | Never loaded |
| `services/merchant.ts` | ~100 | Merchant service | Never loaded |
| `services/report.ts` | ~100 | Reporting service | Never loaded |
| `services/zone.ts` | ~100 | Zone service | Never loaded |
| `store/index.ts` | 216 | Custom state management | Never loaded |
| `types/index.ts` | ~200 | Type definitions | Never loaded |
| `pages/base-page.ts` | 221 | Base page handler (TS version) | Never loaded |
| `pages/index.ts` | 10 | Page exports | Never loaded |
| `pages/courier/*.ts` | ~800 | Courier pages (TS versions) | Never loaded |
| `pages/merchant/*.ts` | ~450 | Merchant pages (TS versions) | Never loaded |
| `pages/owner/dashboard.ts` | 291 | Owner dashboard (TS version) | Never loaded |
| `pages/warehouse/dashboard.ts` | 239 | Warehouse dashboard (TS version) | Never loaded |

### HTML Files

| File | Lines | Purpose | Issues |
|------|-------|---------|--------|
| `index.html` | 292 | Landing page | CSP allows unsafe-inline, good structure |
| `login.html` | ~200 | Login page | Duplicate `class` attributes, CSP connect-src: localhost:8000 |
| `404.html` | ~180 | Error page | `\`n` artifacts in meta tags |
| `contact.html` | 511 | Contact form | References non-existent `contact.js`, `\`n` artifacts |
| `profile.html` | ~200 | User profile | Clean structure |
| `settings.html` | 664 | App settings | Links to non-existent `/dashboard.html`, OTP password reset |
| `owner/dashboard.html` | ~300 | Owner dashboard | Clean, loads all deps correctly |
| `merchant/create-shipment.html` | 259 | Create shipment form | `\`n` artifacts, sidebar links without `.html` extension |
| `admin/dashboard.html` | ~200 | Admin dashboard | — |
| `courier/dashboard.html` | ~200 | Courier dashboard | — |
| `courier/manifest.html` | ~150 | Courier manifest | — |
| `merchant/dashboard.html` | ~200 | Merchant dashboard | — |
| `merchant/shipments.html` | ~200 | Shipment listing | — |
| `merchant/shipment-details.html` | ~200 | Shipment detail | — |
| `owner/employees.html` | ~200 | Employee management | — |
| `owner/merchants.html` | ~200 | Merchant management | — |
| `owner/payouts.html` | ~200 | Payout management | — |
| `owner/pricing.html` | ~150 | Pricing management | — |
| `owner/reports.html` | ~200 | Reports overview | — |
| `owner/settings.html` | ~200 | Owner settings | — |
| `owner/shipments.html` | ~200 | Shipment management | — |
| `owner/zones.html` | ~200 | Zone management | — |
| `owner/reports/couriers.html` | ~150 | Courier reports | — |
| `owner/reports/merchants.html` | ~150 | Merchant reports | — |
| `owner/reports/warehouse.html` | ~150 | Warehouse reports | — |
| `warehouse/dashboard.html` | ~200 | Warehouse dashboard | — |

### CSS Files

| File | Lines | Purpose | Issues |
|------|-------|---------|--------|
| `src/css/styles.css` | 2,606 | Main stylesheet | Massive monolithic file, variable duplication |
| `src/css/twsela-design.css` | 418 | Design utilities | Reimplements Bootstrap utilities |
| `src/css/main.css` | 232 | Additional styles | — |
| `src/css/pages/404-page.css` | 239 | 404 page styles | — |
| `src/css/pages/settings-page.css` | 166 | Settings page styles | — |
| `src/assets/css/main.css` | — | Asset main CSS | — |
| `src/assets/css/base/_variables.css` | 61 | CSS variables (third copy) | Triplicate variable definitions |
| `src/assets/css/base/*.css` | ~8 files | Base styles | — |
| `src/assets/css/components/*.css` | ~5 files | Component styles | — |
| `src/assets/css/layouts/*.css` | ~3 files | Layout styles | — |
| `src/assets/css/themes/*.css` | ~3 files | Theme styles | — |

---

## 8. Security Audit

### Vulnerability Summary

| ID | Severity | Category | Description |
|----|----------|----------|-------------|
| SEC-1 | 🔴 Critical | XSS | Template literal injection in all table rows and modals |
| SEC-2 | 🔴 Critical | Auth | Token stored in localStorage (accessible to XSS) |
| SEC-3 | 🔴 Critical | Auth | Auth check returns `true` on network error |
| SEC-4 | 🟠 High | CSP | `unsafe-inline` allowed in Content Security Policy |
| SEC-5 | 🟠 High | Hardcoded | API URL `http://localhost:8000` hardcoded (no HTTPS) |
| SEC-6 | 🟡 Medium | CSRF | No CSRF token on state-changing operations |
| SEC-7 | 🟡 Medium | Input | No rate limiting on login attempts (client-side) |
| SEC-8 | 🟡 Medium | Auth | No token refresh mechanism |
| SEC-9 | 🟢 Low | Info | Detailed error stacks logged to console in production |
| SEC-10 | 🟢 Low | Auth | 5-minute auth cache could allow stale permissions |

### CSP Analysis (`index.html`)
```html
<meta http-equiv="Content-Security-Policy" content="
    default-src 'self'; 
    script-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com; 
    style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://cdnjs.cloudflare.com https://fonts.googleapis.com; 
    connect-src 'self' http://localhost:8000;
">
```

- `'unsafe-inline'` for scripts defeats much of CSP's XSS protection
- `connect-src` hardcoded to `http://localhost:8000` (non-HTTPS, localhost only)

---

## 9. Performance Audit

### Issues Found

| ID | Impact | Description |
|----|--------|-------------|
| PERF-1 | 🟠 High | All CDN assets loaded on every page (Bootstrap CSS/JS, Font Awesome, Chart.js) even when not needed |
| PERF-2 | 🟠 High | `styles.css` is 2,606 lines loaded as single file, no CSS code splitting |
| PERF-3 | 🟠 High | `.htaccess` disables all browser caching for static assets |
| PERF-4 | 🟡 Medium | Reports page loads up to 1000 records per API call without pagination |
| PERF-5 | 🟡 Medium | No lazy loading for page-specific JS (all shared JS loaded on every page) |
| PERF-6 | 🟡 Medium | Chart.js loaded on pages that may not have charts |
| PERF-7 | 🟡 Medium | Owner dashboard makes 3 API calls with `limit: 1` just to get record counts (inefficient) |
| PERF-8 | 🟢 Low | Google Fonts loaded via `@import` in CSS (blocks rendering) vs `<link>` tag |
| PERF-9 | 🟢 Low | No image optimization or lazy loading |
| PERF-10 | 🟢 Low | 5 npm packages installed but never imported in production code |

### Bundle Size (Estimated)
| Asset | Size (approx) |
|-------|---------------|
| Bootstrap CSS (CDN) | ~230 KB |
| Bootstrap JS (CDN) | ~80 KB |
| Font Awesome (CDN) | ~90 KB |
| Chart.js (CDN) | ~200 KB |
| `styles.css` | ~80 KB |
| `twsela-design.css` | ~12 KB |
| All JS files combined | ~200 KB |
| Fonts (Noto Sans Arabic) | ~300 KB |
| **Total per page** | **~1.2 MB** |

---

## 10. Accessibility & RTL/Arabic Audit

### Accessibility Issues

| ID | WCAG | Description |
|----|------|-------------|
| A11Y-1 | 1.1.1 | No `alt` text on decorative images/icons |
| A11Y-2 | 1.3.1 | Tables missing `<caption>` and `scope` attributes on headers |
| A11Y-3 | 2.1.1 | Dropdown menus not keyboard navigable |
| A11Y-4 | 2.4.1 | No skip links for navigation |
| A11Y-5 | 2.4.3 | Focus management missing after modal open/close |
| A11Y-6 | 2.4.7 | No visible focus indicators on many interactive elements |
| A11Y-7 | 3.3.1 | Form validation errors not announced to screen readers |
| A11Y-8 | 4.1.2 | Custom dropdowns missing ARIA roles/states |
| A11Y-9 | 4.1.3 | Status messages (notifications) not using `role="alert"` or `aria-live` |
| A11Y-10 | 1.4.3 | Some text on gradient backgrounds may not meet contrast ratios |

### RTL/Arabic Issues

| ID | Description |
|----|-------------|
| RTL-1 | All HTML files correctly set `lang="ar" dir="rtl"` ✅ |
| RTL-2 | Pagination uses chevron-right for "previous" and chevron-left for "next" — correct for RTL ✅ |
| RTL-3 | Sidebar is positioned on the right side — correct for RTL ✅ |
| RTL-4 | CSS has `[dir="rtl"]` selectors for layout adjustments ✅ |
| RTL-5 | `twsela-design.css` uses `margin-right`/`margin-left` instead of logical properties (`margin-inline-start`/`margin-inline-end`) ⚠️ |
| RTL-6 | Dates formatted with `ar-SA` locale ✅ |
| RTL-7 | Phone validation inconsistent (Egyptian vs Saudi formats) ⚠️ |
| RTL-8 | Currency: "جنيه" (Pound) used with Saudi context — should be "ريال" (Riyal) ⚠️ |
| RTL-9 | English brand "Twsela" and status codes (`PENDING`, `ACTIVE`) appear in RTL context — acceptable but could be localized |
| RTL-10 | Chart.js labels are in Arabic ✅ |

---

## 11. Recommendations

### Immediate (Week 1-2)

1. **Fix XSS vulnerabilities**: Apply `SharedDataUtils.sanitizeHTML()` to all user data before inserting into DOM via template literals. Better yet, refactor to use `textContent` or a micro-templating library.

2. **Fix undefined variable bugs** in `api_service.js` catch blocks.

3. **Fix auth error handling**: Change `checkAuthStatus()` to return `false` on error instead of `true`.

4. **Remove `\`n` artifacts** from all HTML meta tags (search and replace across all HTML files).

5. **Fix or remove broken links**: `contact.js`, `/dashboard.html`, extension-less sidebar nav links.

### Short-term (Week 3-4)

6. **Decide on JS vs TS**: Either complete the TypeScript migration with proper Vite config, or remove all `.ts` files and unused npm dependencies. The current dual architecture is untenable.

7. **Standardize page handler pattern**: All pages should either extend `BasePageHandler` or use a consistent function-based pattern. Remove duplicate auth checking in page-specific code.

8. **Add proper error handling**: Replace empty `catch` blocks with user-facing error messages and logging.

9. **Replace `confirm()`/`prompt()`/`alert()`** with custom modals using the existing `GlobalUIHandler.createModal()` system.

10. **Implement CSRF protection** for all state-changing operations.

### Medium-term (Month 2)

11. **Configure Vite properly**: Add `rollupOptions.input` for all HTML pages, configure path aliases, enable code splitting.

12. **Split monolithic CSS**: Break `styles.css` (2,606 lines) into page-specific modules. Remove duplicate utility classes from `twsela-design.css`.

13. **Implement page-level code splitting**: Load page-specific JS only on the pages that need it.

14. **Fix caching strategy**: Enable proper browser caching with content-hash filenames (Vite generates these automatically).

15. **Implement stub pages**: Complete the empty pricing, reports-couriers, reports-merchants, and reports-warehouse pages.

### Long-term (Month 3+)

16. **Move auth tokens to httpOnly cookies** to prevent XSS token theft.

17. **Add automated tests**: Unit tests for services, integration tests for page handlers.

18. **Add accessibility**: ARIA labels, keyboard navigation, screen reader support, focus management.

19. **Standardize locale**: Decide Egypt vs Saudi Arabia and ensure consistent phone validation, currency formatting, and address formats.

20. **Consider a framework**: The codebase has outgrown vanilla JS with 20+ pages and complex state management. Consider migrating to Vue, React, or even Alpine.js for better maintainability.

---

## Appendix A: File Inventory

```
frontend/
├── .babelrc
├── .eslintrc.json
├── .gitignore
├── .htaccess
├── .prettierrc
├── 404.html
├── contact.html
├── IMPROVEMENT_PLAN.md
├── index.html
├── login.html
├── package.json
├── profile.html
├── settings.html
├── tsconfig.json
├── vite.config.js
├── admin/
│   └── dashboard.html
├── courier/
│   ├── dashboard.html
│   └── manifest.html
├── frontend/          ← STALE DUPLICATE DIRECTORY
│   └── src/css/fonts/ (duplicate font files)
├── merchant/
│   ├── create-shipment.html
│   ├── dashboard.html
│   ├── shipment-details.html
│   └── shipments.html
├── owner/
│   ├── dashboard.html
│   ├── employees.html
│   ├── merchants         ← EXTENSION-LESS FILE (broken)
│   ├── merchants.html
│   ├── payouts.html
│   ├── pricing.html
│   ├── reports.html
│   ├── settings.html
│   ├── shipments.html
│   ├── zones             ← EXTENSION-LESS FILE (broken)
│   ├── zones.html
│   └── reports/
│       ├── couriers.html
│       ├── merchants.html
│       └── warehouse.html
├── src/
│   ├── assets/css/       (22 CSS files in base/, components/, layouts/, themes/)
│   ├── css/
│   │   ├── main.css
│   │   ├── styles.css    (2,606 lines - MONOLITH)
│   │   ├── twsela-design.css (418 lines - REDUNDANT)
│   │   ├── fonts/
│   │   └── pages/
│   └── js/
│       ├── app.js        (675 lines - ORCHESTRATOR)
│       ├── pages/        (25+ files - PAGE HANDLERS)
│       ├── services/     (2 JS + 7 TS files)
│       ├── shared/       (4 files - UTILITIES)
│       ├── store/        (1 TS file - DEAD CODE)
│       └── types/        (2 TS files - DEAD CODE)
└── warehouse/
    └── dashboard.html
```

---

## Appendix B: Dependency Graph

```
index.html / owner/dashboard.html / etc.
  ├── [CDN] Bootstrap 5.3.2 CSS
  ├── [CDN] Font Awesome 6.4.0
  ├── [CDN] Chart.js 4.x
  ├── src/css/styles.css
  │   └── @import Google Fonts (Noto Sans Arabic)
  ├── src/js/shared/SharedDataUtils.js     → window.SharedDataUtils
  ├── src/js/shared/GlobalUIHandler.js     → window.GlobalUIHandler
  │   └── depends on: SharedDataUtils
  ├── src/js/shared/NotificationService.js → window.NotificationService
  │   └── depends on: window.notificationManager
  ├── src/js/shared/BasePageHandler.js     → window.BasePageHandler
  │   └── depends on: authService, apiService, NotificationService
  ├── src/js/services/auth_service.js      → window.authService
  ├── src/js/services/api_service.js       → window.apiService
  │   └── depends on: authService
  ├── src/js/app.js                        → window.twselaApp
  │   └── depends on: authService, apiService, all shared
  └── src/js/pages/<handler>.js            → window.<handlerInstance> (some)
      └── depends on: all of the above
```

---

*End of Audit Report*
