# Sprint 15 — DevOps، اختبارات شاملة، وجاهزية الإنتاج
**المهام: 30 | حزم العمل: 4**

> **الهدف:** بناء CI/CD pipeline كامل، اختبارات E2E، Vite production build، API versioning، وتوثيق شامل للنشر.

---

## WP-1: CI/CD Pipeline — GitHub Actions (8 مهام)

### T-15.01: إنشاء GitHub Actions Workflow — Backend CI
```yaml
# .github/workflows/backend-ci.yml
name: Backend CI
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    services:
      mysql: { image: mysql:8.0, env: ... }
      redis: { image: redis:7 }
    steps:
      - checkout
      - setup-java 17
      - mvn test
      - mvn package -DskipTests
      - upload test results
      - upload coverage report
```

### T-15.02: GitHub Actions — Frontend CI
```yaml
# .github/workflows/frontend-ci.yml
name: Frontend CI
on: [push, pull_request]
jobs:
  build:
    steps:
      - checkout
      - setup-node 18
      - npm ci
      - npm run lint
      - npm run build
      - upload build artifacts
```

### T-15.03: GitHub Actions — Docker Build + Push
```yaml
# .github/workflows/docker.yml
name: Docker Build
on:
  push:
    branches: [main]
    tags: ['v*']
jobs:
  docker:
    steps:
      - build-push-action
      - tag: latest + git SHA + semver
      - push to GitHub Container Registry (ghcr.io)
```

### T-15.04: GitHub Actions — Deployment Pipeline
```yaml
# .github/workflows/deploy.yml
name: Deploy
on:
  workflow_dispatch:
    inputs:
      environment: [staging, production]
jobs:
  deploy:
    environment: ${{ inputs.environment }}
    steps:
      - SSH to server
      - docker compose pull
      - docker compose up -d
      - health check
      - rollback on failure
```

### T-15.05: Branch Protection Rules
- تكوين:
  - `main` branch: require PR, require CI pass, require 1 approval
  - `develop` branch: require CI pass
- PR template: `.github/PULL_REQUEST_TEMPLATE.md`
- Issue templates: bug report, feature request

### T-15.06: Secret Management
- GitHub Secrets configuration:
  - `MYSQL_PASSWORD`, `REDIS_PASSWORD`
  - `JWT_SECRET`
  - `TWILIO_SID`, `TWILIO_AUTH_TOKEN`
  - `SMTP_PASSWORD`
  - `DEPLOY_SSH_KEY`
- `.env.example` files updated (no secrets)

### T-15.07: Docker Compose Production
- تحديث `docker-compose.yml` لـ production:
  - مع health checks لكل service
  - restart policy: `unless-stopped`
  - Volume mounts: logs, uploads, backups
  - Network isolation: frontend ↔ nginx ↔ backend ↔ db
  - Resource limits: memory, CPU

### T-15.08: Nginx Production Configuration
- تحديث `nginx.conf`:
  - SSL termination (Let's Encrypt / certbot)
  - gzip compression
  - Security headers (HSTS, X-Frame-Options, CSP)
  - Rate limiting: login → 5 req/min, API → 100 req/min
  - Static file caching: 1 year for hashed assets
  - Proxy pass to Spring Boot (upstream)

---

## WP-2: Vite Production Build + API Versioning (7 مهام)

### T-15.09: Vite Multi-Page Configuration
- تحديث `vite.config.js`:
```js
export default defineConfig({
  build: {
    rollupOptions: {
      input: {
        main: 'index.html',
        login: 'login.html',
        profile: 'profile.html',
        settings: 'settings.html',
        contact: 'contact.html',
        '404': '404.html',
        'admin-dashboard': 'admin/dashboard.html',
        'courier-dashboard': 'courier/dashboard.html',
        'courier-manifest': 'courier/manifest.html',
        'merchant-dashboard': 'merchant/dashboard.html',
        'merchant-shipments': 'merchant/shipments.html',
        'merchant-create': 'merchant/create-shipment.html',
        'merchant-details': 'merchant/shipment-details.html',
        'owner-dashboard': 'owner/dashboard.html',
        // ... all other pages
      }
    }
  }
});
```

### T-15.10: Vite Build Optimization
- Code splitting: shared chunks لـ api_service, auth_service, shared utils
- Tree shaking: إزالة dead code
- CSS extraction: أحادي لكل صفحة
- Asset hashing: `[name].[hash].js`
- Source maps: enabled for staging, disabled for production

### T-15.11: Frontend Environment Variables
- إنشاء `.env.development` و `.env.production`:
  - `VITE_API_BASE_URL=https://api.twsela.com`
  - `VITE_WS_URL=wss://api.twsela.com/ws`
  - `VITE_GOOGLE_MAPS_KEY=...`
- تحديث `api_service.js` لاستخدام `import.meta.env.VITE_API_BASE_URL`

### T-15.12: API Versioning — Backend
- إضافة prefix `/api/v1/` لكل controllers
- Strategy: URL-based versioning
- تحديث `@RequestMapping` في كل controller
- Swagger documentation grouped by version

### T-15.13: API Versioning — Frontend
- تحديث `API_BASE_URL` في `api_service.js` → `/api/v1`
- تحديث كل endpoint paths

### T-15.14: Database Migrations — Flyway
- إضافة `flyway-core` dependency
- إنشاء baseline migration: `V1__baseline.sql` (من الـ schema الحالي)
- Migration لكل تغيير Sprint 12-14:
  - `V2__add_notifications_table.sql`
  - `V3__add_delivery_proof_table.sql`
  - `V4__add_courier_rating_table.sql`
  - `V5__add_returns_columns.sql`
  - `V6__add_awb_column.sql`
- تكوين Flyway في `application.yml`

### T-15.15: Database Backup Automation
- تحديث `backup-script.sh`:
  - mysqldump مع compression
  - Retention: 7 daily, 4 weekly, 12 monthly
  - Upload to remote storage (optional)
  - Email notification on failure
- Cron job: daily at 2 AM

---

## WP-3: E2E Tests + Security (8 مهام)

### T-15.16: Playwright Setup
- إضافة Playwright في `frontend/`:
  - `npm install -D @playwright/test`
  - `playwright.config.ts` مع base URL
  - Browser: Chromium (headless)
  - Screenshots on failure
  - Video recording for failed tests

### T-15.17: E2E — Authentication Flow
- Test 1: Login success → redirect to dashboard
- Test 2: Login failure → error message
- Test 3: Unauthorized access → redirect to login
- Test 4: Logout → session cleared → redirect to login

### T-15.18: E2E — Merchant Shipment Flow
- Test 1: Create shipment → appears in list
- Test 2: View shipment details → correct data
- Test 3: Filter shipments by status
- Test 4: Bulk upload → preview → confirm

### T-15.19: E2E — Owner Management Flow
- Test 1: Create employee → appears in list
- Test 2: Create zone → pricing updated
- Test 3: View reports → data loads
- Test 4: Manage settings → saved successfully

### T-15.20: E2E — Courier Workflow
- Test 1: View assigned shipments
- Test 2: Update shipment status → status changes
- Test 3: View manifest → correct data
- Test 4: Barcode scan → loads shipment

### T-15.21: OWASP Security Scan
- إضافة ZAP (OWASP Zed Attack Proxy) في CI:
  - Baseline scan on every PR
  - Full scan weekly
  - `zap-baseline.yml` configuration
- Fix critical/high findings:
  - Missing security headers
  - CORS misconfiguration
  - Information disclosure

### T-15.22: Security Headers Verification
- تأكيد headers في كل response:
  - `Strict-Transport-Security: max-age=31536000; includeSubDomains`
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `Content-Security-Policy: default-src 'self'; ...`
  - `Referrer-Policy: strict-origin-when-cross-origin`
  - `Permissions-Policy: camera=(self), geolocation=(self)`

### T-15.23: Dependency Vulnerability Scan
- إضافة `mvn dependency-check:check` في CI (OWASP dependency-check maven plugin)
- إضافة `npm audit` في Frontend CI
- Fix/update أي dependency بـ known vulnerability
- Fail CI on HIGH/CRITICAL vulnerabilities

---

## WP-4: Documentation + Performance + Final Checks (7 مهام)

### T-15.24: API Documentation — Swagger/OpenAPI
- تحديث كل endpoint بـ:
  - `@Operation(summary, description)` بالعربية
  - `@ApiResponse(responseCode, description)` لكل status code
  - `@Schema(description)` لكل DTO field
- Export: `/api/docs` → Swagger UI, `/api/docs.json` → OpenAPI spec

### T-15.25: Performance Benchmarks
- إضافة JMeter test plan أو k6 scripts:
  - Login: < 200ms P99
  - Create shipment: < 500ms P99
  - List shipments (100 items): < 300ms P99
  - Dashboard stats: < 400ms P99
- تسجيل النتائج في CI artifacts

### T-15.26: Production Deployment Guide
- إنشاء `DEPLOYMENT_GUIDE.md`:
  - Prerequisites: Docker, Docker Compose, SSL certificate
  - Step-by-step: clone → configure → build → deploy
  - Environment variables reference
  - Health check URLs
  - Rollback procedure
  - Monitoring setup (Prometheus + Grafana)

### T-15.27: Runbook — Incident Response
- إنشاء `RUNBOOK.md`:
  - Common issues + resolutions
  - How to restart services
  - How to check logs
  - How to restore from backup
  - Escalation procedures
  - Monitoring alerts reference

### T-15.28: Update SPRINTS_HISTORY_REPORT.md
- إضافة Sprints 11-15 بنفس format الحالي
- تحديث Cumulative Metrics table
- تحديث Deferred Items table
- Overall project status summary

### T-15.29: Final Integration Test Suite
- 10 integration tests تغطي كل الـ flows الرئيسية من Sprint 11-15:
  - Full shipment lifecycle (create → assign → deliver → POD → rate)
  - Bulk upload → track → deliver
  - Notification flow (create → receive → read)
  - WebSocket real-time update
  - Auth flow (login → refresh → logout)

### T-15.30: Production Readiness Checklist
- [ ] All 220+ tests pass
- [ ] Zero CRITICAL/HIGH security vulnerabilities
- [ ] All E2E tests pass
- [ ] Docker images built and pushed
- [ ] CI/CD pipelines green
- [ ] SSL configured
- [ ] Backup automation verified
- [ ] Monitoring dashboards active
- [ ] API documentation complete
- [ ] Deployment guide reviewed
- [ ] Performance benchmarks met
- [ ] Rollback procedure tested

---

## معايير القبول
- [ ] CI/CD pipeline يعمل: build → test → docker → deploy
- [ ] Vite يبني كل الصفحات بنجاح
- [ ] Environment variables تعمل في dev و production
- [ ] API versioned: `/api/v1/`
- [ ] Flyway migrations تعمل من scratch
- [ ] Playwright E2E: 20+ tests pass
- [ ] OWASP scan: zero critical, zero high
- [ ] Dependency scan: zero critical
- [ ] Security headers in place
- [ ] Performance: P99 < 500ms لكل endpoint
- [ ] Swagger documentation كاملة بالعربية
- [ ] Deployment guide + Runbook مكتملين
- [ ] 220+ tests, 0 failures, BUILD SUCCESS
- [ ] **Production Ready ✅**
