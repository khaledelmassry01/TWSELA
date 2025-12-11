# AI Coding Assistant Instructions for Twsela Project

## Project Overview
Twsela is a comprehensive courier management system with a Spring Boot backend (`twsela/`) and a vanilla JavaScript frontend (`frontend/`). The system supports multiple user roles (Owner, Admin, Merchant, Courier, Warehouse) with distinct interfaces and capabilities.

## Key Architecture Points

### Frontend Structure
- HTML files are in role-specific directories (`frontend/admin/`, `frontend/courier/`, etc.)
- JavaScript code is modularized in `frontend/src/js/`:
  - Page-specific logic in `pages/`
  - Shared services in `services/`
  - Core business logic in `shared/`
- Strict separation of concerns:
  - No inline JavaScript in HTML files
  - No inline CSS - use external stylesheets or Tailwind utility classes
  - Page scripts are loaded as ES6 modules

### Backend Structure (Java/Spring Boot)
- Standard Maven project structure in `twsela/`
- Configuration files in `twsela/src/main/resources/`
- Main application code in `twsela/src/main/java/`
- Critical services:
  - Redis for caching
  - MySQL/PostgreSQL for persistence
  - Prometheus/Grafana for monitoring
  - Nginx as reverse proxy

## Core Development Guidelines

### 1. Language Support
- All user-facing content must support Arabic (RTL)
- Use `dir="rtl"` for Arabic content
- Include Noto Sans Arabic fonts for text rendering

### 2. Security Practices
- JWT authentication required for all API endpoints
- Role-based access control for all operations
- BCrypt password hashing
- CSRF protection for state-changing operations

### 3. Frontend Development
- Use ES6+ features consistently
- Follow modular JavaScript patterns - see `frontend/src/js/pages/courier-dashboard-page.js`
- Handle errors uniformly using shared error utilities
- Implement responsive design using Tailwind CSS

### 4. API Integration
- Standard response format:
```json
{
  "success": boolean,
  "message": string,
  "data": object,
  "errors": array
}
```
- Always include error handling for network failures
- Use the `api_service.js` utility for all API calls

## Development Workflow

### Local Setup
1. Start Redis server (see `REDIS_SETUP_INSTRUCTIONS.md`)
2. Configure SSL certificates (see `SSL_SETUP_INSTRUCTIONS.md`)
3. Start Spring Boot application with local profile
4. Access frontend through Nginx (see `nginx.conf`)

### Testing & Debug
- Monitor logs in `twsela/logs/` and `frontend/logs/`
- Use Prometheus/Grafana dashboards for performance monitoring
- Run regular security scans (especially for Arabic content handling)

## Integration Points
1. Twilio SMS service - configured via `TWILIO_SMS_SETUP_GUIDE.md`
2. Payment gateway - see `twsela/src/main/resources/application.yml`
3. Google Maps API - used for delivery zone management
4. Redis cache - critical for performance optimization

## Common Patterns
1. Shipment status transitions: CREATED → PICKED_UP → IN_TRANSIT → DELIVERED
2. Role-specific dashboard layouts (see `frontend/*/dashboard.html`)
3. Zone-based pricing and courier assignment
4. Manifest generation for delivery optimization

## Need Help?
- System architecture: See `PERFORMANCE_OPTIMIZATION_GUIDE.md`
- Security setup: Check `SSL_SETUP_INSTRUCTIONS.md`
- Deployment: Reference `BACKUP_SETUP_GUIDE.md` and `MONITORING_SETUP_GUIDE.md`