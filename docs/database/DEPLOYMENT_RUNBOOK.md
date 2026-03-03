# Twsela Deployment Runbook

## Prerequisites

| Component         | Version     | Purpose                      |
|-------------------|-------------|------------------------------|
| Docker & Compose  | 24.x / 2.x | Container orchestration      |
| Java (local dev)  | 17 (Temurin)| Backend compilation          |
| Node.js (build)   | 20 LTS      | Frontend build               |
| MySQL             | 8.0         | Primary database             |
| Redis             | 7.x Alpine  | Session cache & rate limits  |

---

## Quick Start (Docker)

```bash
# 1. Clone and configure
git clone https://github.com/<org>/TWSELA.git && cd TWSELA
cp .env.example .env
# Edit .env with production values

# 2. Place SSL certificates
mkdir -p ssl
cp /path/to/cert.pem ssl/cert.pem
cp /path/to/key.pem  ssl/key.pem

# 3. Launch all services
docker compose up -d

# 4. Verify
docker compose ps
curl -k https://localhost/api/health
```

---

## Service Architecture

```
                   ┌──────────┐
                   │  Nginx   │ :80/:443
                   │ (proxy)  │
                   └────┬─────┘
          ┌──────────┬──┴───────┬──────────┐
          │          │          │          │
     /api/*     /ws/*      /static    /actuator
          │          │                    │
          ▼          ▼                    ▼
   ┌─────────────────────┐        ┌──────────┐
   │  Spring Boot App    │        │Prometheus │
   │  :8000              │        │  :9090    │
   └──────┬────────┬─────┘        └──────────┘
          │        │
     ┌────▼┐  ┌───▼──┐
     │MySQL│  │Redis │
     │:3306│  │:6379 │
     └─────┘  └──────┘
```

---

## Health Checks

| Endpoint                    | Expected  | Purpose            |
|-----------------------------|-----------|---------------------|
| `GET /api/health`           | 200 OK    | Application health   |
| `GET /actuator/health`      | 200 UP    | Spring Actuator      |
| `GET /actuator/prometheus`  | 200       | Metrics scrape       |

---

## Database Migrations

Flyway runs automatically on startup.

- Migrations directory: `twsela/src/main/resources/db/migration/`
- Current migrations:
  - `V1__add_user_lockout_columns.sql`
  - `V2__sync_shipment_statuses.sql`
  - `V3__create_notifications_and_ratings.sql`
- Strategy: `baseline-on-migrate: true`, `baseline-version: 0`

**Manual migration (if needed):**
```bash
docker compose exec app java -jar app.jar --spring.flyway.repair
```

---

## Backups

```bash
# On-demand MySQL backup
docker compose -f twsela/docker-compose.backup.yml up backup

# Automated (add to crontab)
0 2 * * * cd /opt/twsela && docker compose -f twsela/docker-compose.backup.yml up backup
```

---

## Monitoring Stack

```bash
# Start Prometheus + Grafana alongside main stack
docker compose -f twsela/docker-compose.monitoring.yml up -d

# Access
# Prometheus: http://localhost:9090
# Grafana:    http://localhost:3001 (admin / $GRAFANA_ADMIN_PASSWORD)
```

---

## Scaling Notes

- **Horizontal**: Run multiple `app` replicas behind Nginx with `upstream` block
- **WebSocket**: Sticky sessions required for STOMP; use `ip_hash` in Nginx upstream
- **Redis**: Single node sufficient for <10k concurrent connections
- **MySQL**: Read replicas supported via Spring `@Transactional(readOnly=true)`

---

## Troubleshooting

| Issue                     | Command                                           |
|---------------------------|----------------------------------------------------|
| App won't start           | `docker compose logs app --tail 100`               |
| DB connection refused     | `docker compose exec mysql mysqladmin ping -p`     |
| Redis connection error    | `docker compose exec redis redis-cli ping`         |
| Flyway checksum mismatch  | `mvn flyway:repair -Dflyway.url=...`               |
| 502 from Nginx            | Check app container is healthy                     |
| WebSocket not connecting  | Verify `/ws` location block in nginx.conf          |

---

## Rollback Procedure

```bash
# 1. Stop current deployment
docker compose down

# 2. Restore previous image
docker compose pull app  # or tag a specific version
# OR: docker tag twsela-app:previous twsela-app:latest

# 3. Bring back up
docker compose up -d

# 4. Verify
curl -k https://localhost/api/health
```

---

## CI/CD Pipeline

The GitHub Actions workflow (`.github/workflows/ci-cd.yml`) runs:

1. **build-backend** — Maven package (skip tests)
2. **test-backend** — `mvn test` with Redis service container
3. **build-frontend** — `npm ci && npm run build`
4. **test-api** — Newman against running backend
5. **test-e2e** — Playwright against running stack
6. **security-scan** — OWASP Dependency Check
7. **deploy-staging** — On `main` branch only

---

## Security Checklist

- [ ] Change all default passwords in `.env`
- [ ] JWT secret is ≥64 chars, base64-encoded
- [ ] SSL certificates are valid and not expired
- [ ] Nginx `server_tokens off` (already configured)
- [ ] Security headers active (X-Frame-Options, CSP, HSTS)
- [ ] Rate limiting active (10r/s API, 5r/m login)
- [ ] Redis not exposed to public network
- [ ] MySQL not exposed to public network
- [ ] Actuator endpoints restricted in production
- [ ] File upload size limits configured
