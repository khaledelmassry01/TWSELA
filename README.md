# Twsela - Comprehensive Courier Management System

<div align="center">

![Twsela Logo](docs/images/logo.png)

**A Modern, Role-Based Delivery Orchestration Platform for the MENA Region**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Redis](https://img.shields.io/badge/Redis-Cache-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](https://www.docker.com/)

[Features](#-key-features) • [Architecture](#-architecture) • [Getting Started](#-getting-started) • [Documentation](#-documentation) • [Technologies](#-technology-stack)

</div>

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [User Roles & Capabilities](#-user-roles--capabilities)
- [System Features](#-system-features)
- [Getting Started](#-getting-started)
- [Configuration](#-configuration)
- [API Documentation](#-api-documentation)
- [Security](#-security)
- [Monitoring & Analytics](#-monitoring--analytics)
- [Integrations](#-integrations)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [Business Model](#-business-model)
- [Roadmap](#-roadmap)
- [License](#-license)

---

## 🌟 Overview

**Twsela** is an enterprise-grade, full-stack courier management system designed specifically for the MENA (Middle East and North Africa) region. It provides end-to-end shipment lifecycle management from order creation to proof-of-delivery, featuring Arabic-first UX with RTL (Right-to-Left) support, robust security, and comprehensive operational analytics.

### Why Twsela?

- **🌍 Arabic-First Design**: Native RTL support with Noto Sans Arabic fonts, designed for MENA markets
- **👥 Multi-Role Architecture**: Five distinct user roles with tailored dashboards and workflows
- **🚀 Production Ready**: Complete with monitoring, caching, SSL, and containerization
- **📊 Data-Driven**: Real-time analytics, performance metrics, and operational insights
- **🔒 Enterprise Security**: JWT authentication, RBAC, BCrypt hashing, CSRF protection
- **🎯 Business Focused**: Zone-based pricing, automatic assignment, manifest generation

---

## ✨ Key Features

### 🎭 Multi-Role System
- **Owner Dashboard**: Complete system oversight, merchant management, pricing control
- **Admin Dashboard**: Operational management, user administration, system configuration
- **Merchant Portal**: Self-service shipment creation, tracking, and analytics
- **Courier App**: Optimized manifest view, route guidance, status updates
- **Warehouse Interface**: Intake, sorting, dispatch workflows

### 📦 Shipment Management
- Complete lifecycle tracking: `CREATED → PICKED_UP → IN_TRANSIT → DELIVERED`
- Real-time status updates and notifications
- Proof of delivery capture
- Exception handling and issue resolution
- Bulk shipment operations
- Excel/CSV import/export capabilities

### 🗺️ Zone-Based Operations
- Dynamic zone creation and management
- Distance-based and zone-based pricing
- Intelligent courier assignment
- Surcharge configuration
- Territory mapping with Google Maps integration

### 📱 Communication & Notifications
- Twilio SMS integration
- Automated status notifications
- Custom alert configurations
- Multi-language support (Arabic/English)

### 💰 Financial Management
- Zone-based pricing engine
- COD (Cash on Delivery) reconciliation
- Payment gateway integration
- Automated payout calculations
- Financial reporting and analytics

### 📊 Analytics & Reporting
- Real-time operational dashboards
- Courier performance metrics
- Merchant analytics
- Warehouse throughput reports
- SLA monitoring and compliance
- Custom report generation (PDF/Excel)

### 🚚 Manifest Generation
- Automated daily route optimization
- Courier-specific manifests
- Territory-based grouping
- Priority handling
- Load balancing

---

## 🛠️ Technology Stack

### Backend
- **Framework**: Spring Boot 3.3.3
- **Language**: Java 17
- **Build Tool**: Maven
- **Database**: MySQL/PostgreSQL (Production), H2 (Development)
- **ORM**: Spring Data JPA with Hibernate
- **Caching**: Redis
- **Authentication**: JWT (JSON Web Tokens)
- **Security**: Spring Security with BCrypt
- **Validation**: Jakarta Bean Validation
- **API Documentation**: OpenAPI/Swagger (planned)

### Frontend
- **Architecture**: Vanilla JavaScript (ES6+ Modules)
- **Build Tool**: Vite 4.4.11
- **Styling**: Tailwind CSS
- **UI Components**: Custom modular components
- **HTTP Client**: Axios
- **Date Handling**: date-fns
- **Charts**: Chart.js (for analytics)
- **Icons**: Font Awesome/Custom SVG

### Infrastructure & DevOps
- **Reverse Proxy**: Nginx
- **Containerization**: Docker & Docker Compose
- **Monitoring**: Prometheus + Grafana
- **Metrics**: Micrometer with Spring Boot Actuator
- **SSL/TLS**: Let's Encrypt ready
- **Logging**: SLF4J with Logback
- **Backup**: Automated backup scripts

### Third-Party Integrations
- **SMS**: Twilio API
- **Maps**: Google Maps API
- **Payment Gateway**: Configurable (Stripe/PayPal ready)
- **PDF Generation**: iText7 with Arabic support
- **Excel Processing**: Apache POI

### Development Tools
- **Code Quality**: ESLint, Prettier
- **Version Control**: Git
- **TypeScript**: Gradual adoption (tsconfig.json included)
- **Package Management**: npm

---

## 🏗️ Architecture

### System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         Nginx (SSL)                         │
│                     Reverse Proxy Layer                     │
└──────────────────┬──────────────────┬───────────────────────┘
                   │                  │
        ┌──────────▼────────┐  ┌─────▼──────────┐
        │  Frontend (Vite)  │  │  Spring Boot   │
        │  Vanilla JS/HTML  │  │   Backend API  │
        └───────────────────┘  └────────┬───────┘
                                        │
                   ┌────────────────────┼────────────────────┐
                   │                    │                    │
            ┌──────▼────────┐  ┌────────▼────────┐  ┌──────▼──────┐
            │  MySQL/       │  │     Redis       │  │  Prometheus │
            │  PostgreSQL   │  │  (Cache Layer)  │  │  + Grafana  │
            └───────────────┘  └─────────────────┘  └─────────────┘
```

### Application Layers

#### 1. **Presentation Layer** (Frontend)
```
frontend/
├── admin/          # Admin-specific pages
├── courier/        # Courier interface
├── merchant/       # Merchant portal
├── owner/          # Owner dashboard
├── warehouse/      # Warehouse operations
└── src/
    ├── js/
    │   ├── pages/       # Page-specific logic
    │   ├── services/    # API services
    │   ├── shared/      # Shared utilities
    │   └── store/       # State management
    └── assets/
        ├── css/         # Stylesheets
        └── fonts/       # Arabic fonts
```

#### 2. **Business Logic Layer** (Spring Boot)
```
twsela/src/main/java/com/twsela/
├── config/          # Spring configuration
├── controller/      # REST API endpoints
├── service/         # Business logic
├── repository/      # Data access layer
├── model/          # Domain entities
├── dto/            # Data transfer objects
├── security/       # Security & JWT
├── exception/      # Exception handling
└── util/           # Utilities
```

#### 3. **Data Layer**
- **Primary Database**: MySQL/PostgreSQL for persistence
- **Cache Layer**: Redis for high-performance caching
- **Session Management**: Redis-backed sessions

#### 4. **Integration Layer**
- Twilio SMS Gateway
- Payment Gateway Adapters
- Google Maps API Client
- Email Service (optional)

---

## 👥 User Roles & Capabilities

### 1. 👑 Owner
**Primary Role**: System owner with full administrative control

**Capabilities**:
- Complete system oversight and analytics
- Merchant onboarding and management
- Pricing and zone configuration
- Financial reports and payout management
- Courier performance monitoring
- System-wide settings and configuration
- Employee management
- Revenue and KPI dashboards

**Key Pages**:
- Dashboard with comprehensive metrics
- Merchant management
- Employee management
- Zone and pricing configuration
- Financial reports
- Shipment oversight
- System settings

### 2. 🔧 Admin
**Primary Role**: Operational administrator

**Capabilities**:
- User account management
- Daily operations oversight
- Shipment monitoring and intervention
- Courier assignment and dispatch
- Issue resolution
- Report generation
- System configuration

**Key Pages**:
- Administrative dashboard
- User management
- Shipment management
- Report generation

### 3. 🏪 Merchant
**Primary Role**: Business customer creating and tracking shipments

**Capabilities**:
- Self-service shipment creation
- Bulk shipment upload (Excel/CSV)
- Real-time shipment tracking
- Pricing calculator
- Performance analytics
- Payment and COD management
- Proof of delivery access
- Customer database management

**Key Pages**:
- Merchant dashboard
- Create shipment (single/bulk)
- Shipment list and tracking
- Shipment details
- Analytics and reports
- Account settings

### 4. 🚚 Courier
**Primary Role**: Field delivery personnel

**Capabilities**:
- Daily manifest view
- Route guidance (Google Maps integration)
- Status updates (pickup, in-transit, delivered)
- Proof of delivery capture
- Issue reporting
- Performance metrics
- Navigation assistance

**Key Pages**:
- Courier dashboard
- Manifest view
- Shipment details
- Status update interface
- Performance statistics

### 5. 📦 Warehouse
**Primary Role**: Warehouse operations staff

**Capabilities**:
- Shipment intake and processing
- Sorting and organization
- Dispatch preparation
- Inventory management
- Exception handling
- Throughput monitoring

**Key Pages**:
- Warehouse dashboard
- Intake processing
- Dispatch management
- Inventory view

---

## 🎯 System Features

### Shipment Lifecycle Management

#### Status Flow
```
CREATED
   ↓
ASSIGNED_TO_COURIER
   ↓
PICKED_UP
   ↓
IN_TRANSIT
   ↓
OUT_FOR_DELIVERY
   ↓
DELIVERED / FAILED / RETURNED
```

#### Features
- **Smart Assignment**: Automatic courier assignment based on zone, capacity, performance
- **Real-time Tracking**: Live status updates and location tracking
- **Event Logging**: Complete audit trail of all shipment events
- **Exception Handling**: Failed delivery management, return processing
- **Notifications**: Automated SMS/email notifications at each stage

### Zone Management

#### Capabilities
- **Geographic Zones**: Define delivery zones with map integration
- **Pricing Rules**: Zone-specific pricing with distance calculation
- **Courier Territory**: Assign couriers to specific zones
- **Capacity Management**: Load balancing across zones
- **Surcharges**: Configurable additional fees (remote areas, express delivery)

#### Pricing Models
- **Flat Rate**: Fixed price per zone
- **Distance-Based**: Price calculated by distance
- **Weight-Based**: Tiered pricing by shipment weight
- **Hybrid**: Combination of factors

### Manifest Generation

#### Algorithm Features
- **Territory Grouping**: Group shipments by courier zones
- **Priority Sorting**: Handle urgent deliveries first
- **Route Optimization**: Suggest optimal delivery sequence
- **Load Balancing**: Distribute shipments evenly
- **Capacity Constraints**: Respect courier capacity limits

#### Output Formats
- **PDF Manifest**: Printer-friendly with Arabic support
- **Excel Export**: Detailed shipment data
- **Mobile View**: Optimized for courier devices

### Financial System

#### Components
- **Pricing Engine**: Dynamic price calculation
- **COD Management**: Cash collection tracking
- **Payment Gateway**: Online payment processing
- **Reconciliation**: Automated settlement
- **Payout System**: Courier and merchant payouts
- **Invoicing**: Automated invoice generation

#### Reports
- Revenue by merchant
- Courier earnings
- COD reconciliation
- Outstanding payments
- Profit/loss analysis

### Analytics & Reporting

#### Real-Time Metrics
- **Shipment Volume**: Hourly, daily, monthly trends
- **Delivery Performance**: On-time delivery rate, SLA compliance
- **Courier Efficiency**: Deliveries per hour, success rate
- **Merchant Activity**: Shipment frequency, average value
- **Warehouse Throughput**: Processing speed, bottlenecks

#### Custom Reports
- **Executive Dashboard**: High-level KPIs
- **Operational Reports**: Daily operations summary
- **Financial Reports**: Revenue, costs, profitability
- **Compliance Reports**: SLA adherence, audit trails
- **Export Formats**: PDF, Excel, CSV

---

## 🚀 Getting Started

### Prerequisites

#### Required
- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ or PostgreSQL 13+
- Redis 6.0+
- Node.js 16+ and npm
- Nginx (for production)

#### Optional
- Docker & Docker Compose (recommended)
- Git

### Installation

#### Option 1: Docker Compose (Recommended)

```bash
# Clone the repository
git clone https://github.com/khaledelmassry01/TWSELA.git
cd TWSELA

# Copy environment template
cp twsela/env.template twsela/.env

# Configure your environment variables
nano twsela/.env

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f
```

Services will be available at:
- Frontend: `http://localhost`
- Backend API: `http://localhost/api`
- Prometheus: `http://localhost:9090`
- Grafana: `http://localhost:3000`

#### Option 2: Manual Installation

##### 1. Database Setup

```sql
-- Create database
CREATE DATABASE twsela CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user
CREATE USER 'twsela_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON twsela.* TO 'twsela_user'@'localhost';
FLUSH PRIVILEGES;
```

##### 2. Redis Setup

```bash
# Install Redis (Ubuntu/Debian)
sudo apt-get update
sudo apt-get install redis-server

# Start Redis
sudo systemctl start redis-server
sudo systemctl enable redis-server

# Verify
redis-cli ping  # Should return PONG
```

##### 3. Backend Setup

```bash
cd twsela

# Configure application properties
cp src/main/resources/application.yml.template src/main/resources/application.yml
nano src/main/resources/application.yml

# Build the application
mvn clean install

# Run the application
mvn spring-boot:run

# Or run the JAR
java -jar target/twsela-0.0.1-SNAPSHOT.jar
```

##### 4. Frontend Setup

```bash
cd frontend

# Install dependencies
npm install

# Development server
npm run dev

# Production build
npm run build

# Preview production build
npm run preview
```

##### 5. Nginx Setup (Production)

```bash
# Copy nginx configuration
sudo cp twsela/nginx.conf /etc/nginx/sites-available/twsela
sudo ln -s /etc/nginx/sites-available/twsela /etc/nginx/sites-enabled/

# Test configuration
sudo nginx -t

# Reload nginx
sudo systemctl reload nginx
```

### Quick Start Script

For Windows:
```bash
# Run the system manager
system-manager.bat

# Or use PowerShell script
.\rebuild.ps1
```

### Initial Setup

#### 1. Create Owner Account

```bash
# Using API
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "owner",
    "email": "owner@twsela.com",
    "password": "SecurePassword123!",
    "role": "OWNER",
    "phone": "+966501234567"
  }'
```

#### 2. Login

Navigate to `http://localhost/login.html` and use your credentials.

#### 3. Configure System

1. Set up delivery zones
2. Configure pricing rules
3. Add merchants
4. Onboard couriers
5. Configure integrations (Twilio, Payment Gateway, Google Maps)

---

## ⚙️ Configuration

### Environment Variables

Create a `.env` file in the `twsela/` directory:

```env
# Application
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8080

# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=twsela
DB_USERNAME=twsela_user
DB_PASSWORD=your_secure_password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# JWT Configuration
JWT_SECRET=your-256-bit-secret-key-change-this-in-production
JWT_EXPIRATION=86400000

# Twilio SMS
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_AUTH_TOKEN=your_twilio_auth_token
TWILIO_PHONE_NUMBER=+1234567890

# Google Maps
GOOGLE_MAPS_API_KEY=your_google_maps_api_key

# Payment Gateway
PAYMENT_GATEWAY_API_KEY=your_payment_api_key
PAYMENT_GATEWAY_SECRET=your_payment_secret

# Email (Optional)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=noreply@twsela.com
MAIL_PASSWORD=your_email_password

# Monitoring
PROMETHEUS_ENABLED=true
GRAFANA_ENABLED=true

# SSL/TLS
SSL_ENABLED=true
SSL_CERTIFICATE=/path/to/cert.pem
SSL_CERTIFICATE_KEY=/path/to/key.pem
```

### Application Configuration

Edit `twsela/src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: twsela
  
  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&serverTimezone=UTC
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
  
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    password: ${REDIS_PASSWORD}
  
  cache:
    type: redis
    redis:
      time-to-live: 600000

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION}

twilio:
  account-sid: ${TWILIO_ACCOUNT_SID}
  auth-token: ${TWILIO_AUTH_TOKEN}
  phone-number: ${TWILIO_PHONE_NUMBER}

google:
  maps:
    api-key: ${GOOGLE_MAPS_API_KEY}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 📚 API Documentation

### Authentication

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "string",
  "email": "string",
  "password": "string",
  "phone": "string",
  "role": "OWNER|ADMIN|MERCHANT|COURIER|WAREHOUSE"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}

Response:
{
  "token": "jwt_token",
  "type": "Bearer",
  "user": {
    "id": "uuid",
    "username": "string",
    "role": "string"
  }
}
```

### Shipment Management

#### Create Shipment
```http
POST /api/shipments
Authorization: Bearer {token}
Content-Type: application/json

{
  "recipientName": "string",
  "recipientPhone": "string",
  "recipientAddress": "string",
  "zoneId": "uuid",
  "weight": 2.5,
  "codAmount": 100.00,
  "notes": "string",
  "priority": "NORMAL|HIGH|URGENT"
}
```

#### Get Shipments
```http
GET /api/shipments?status=CREATED&page=0&size=20
Authorization: Bearer {token}
```

#### Update Shipment Status
```http
PUT /api/shipments/{id}/status
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "PICKED_UP|IN_TRANSIT|DELIVERED",
  "notes": "string",
  "location": {
    "latitude": 24.7136,
    "longitude": 46.6753
  }
}
```

### Zone Management

#### Create Zone
```http
POST /api/zones
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Central Riyadh",
  "nameAr": "الرياض المركزية",
  "baseFee": 15.00,
  "perKmFee": 2.00,
  "polygon": [
    {"lat": 24.7136, "lng": 46.6753},
    {"lat": 24.7200, "lng": 46.6800}
  ]
}
```

### Manifest Generation

#### Generate Manifest
```http
POST /api/manifests/generate
Authorization: Bearer {token}
Content-Type: application/json

{
  "date": "2025-12-11",
  "courierId": "uuid",
  "zoneIds": ["uuid1", "uuid2"]
}
```

### Analytics

#### Get Dashboard Metrics
```http
GET /api/analytics/dashboard?from=2025-12-01&to=2025-12-11
Authorization: Bearer {token}
```

#### Courier Performance
```http
GET /api/analytics/couriers/{courierId}/performance
Authorization: Bearer {token}
```

### Standard Response Format

All API endpoints return responses in this format:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data
  },
  "errors": [],
  "timestamp": "2025-12-11T10:30:00Z"
}
```

Error responses:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "errors": [
    {
      "field": "recipientPhone",
      "message": "Invalid phone number format"
    }
  ],
  "timestamp": "2025-12-11T10:30:00Z"
}
```

---

## 🔒 Security

### Authentication & Authorization

#### JWT-Based Authentication
- Stateless authentication using JSON Web Tokens
- Token expiration and refresh mechanism
- Secure token storage (HttpOnly cookies recommended for web)

#### Role-Based Access Control (RBAC)
- Five distinct roles with hierarchical permissions
- Method-level security annotations
- Resource-level authorization

#### Password Security
- BCrypt hashing with configurable strength
- Password complexity requirements
- Password reset functionality

### Security Features

#### 1. CSRF Protection
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        http.csrf()
            .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
        return http.build();
    }
}
```

#### 2. CORS Configuration
```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        // Configurable allowed origins
        // Restrict in production
    }
}
```

#### 3. SQL Injection Prevention
- Parameterized queries via JPA
- Input validation and sanitization
- Prepared statements

#### 4. XSS Protection
- Content Security Policy headers
- Output encoding
- Input validation

#### 5. Rate Limiting
- Redis-based rate limiting
- Configurable per endpoint
- IP-based throttling

### SSL/TLS Configuration

```nginx
server {
    listen 443 ssl http2;
    server_name twsela.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # Additional security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
}
```

### Security Best Practices

1. **Keep Dependencies Updated**: Regular security patches
2. **Environment Variables**: Never commit secrets to version control
3. **Audit Logging**: Complete audit trail of security events
4. **Regular Backups**: Automated daily backups
5. **Penetration Testing**: Regular security assessments
6. **Monitoring**: Real-time security monitoring with alerts

---

## 📊 Monitoring & Analytics

### Prometheus Metrics

#### Application Metrics
- **JVM Metrics**: Memory usage, garbage collection, thread count
- **HTTP Metrics**: Request count, latency, error rates
- **Database Metrics**: Connection pool, query performance
- **Cache Metrics**: Hit rate, eviction rate
- **Custom Business Metrics**: Shipments created, deliveries completed

#### Sample Queries
```promql
# Request rate per endpoint
rate(http_server_requests_seconds_count[5m])

# Average response time
rate(http_server_requests_seconds_sum[5m]) / rate(http_server_requests_seconds_count[5m])

# Cache hit rate
redis_cache_hits_total / (redis_cache_hits_total + redis_cache_misses_total)

# Active shipments
twsela_shipments_active{status="IN_TRANSIT"}
```

### Grafana Dashboards

#### Pre-configured Dashboards

1. **System Overview**
   - Request rate and latency
   - Error rates
   - JVM memory and CPU
   - Active users

2. **Business Metrics**
   - Daily shipment volume
   - Delivery success rate
   - Average delivery time
   - Revenue metrics

3. **Courier Performance**
   - Deliveries per courier
   - On-time delivery rate
   - Average delivery time
   - Active vs idle couriers

4. **Database Performance**
   - Query performance
   - Connection pool status
   - Slow queries
   - Lock contention

5. **Cache Performance**
   - Hit/miss ratio
   - Memory usage
   - Key eviction rate

### Application Logging

#### Log Levels
- **ERROR**: System errors and exceptions
- **WARN**: Warning conditions
- **INFO**: General information (startup, shutdown, major events)
- **DEBUG**: Detailed debugging information
- **TRACE**: Very detailed tracing

#### Log Structure
```json
{
  "timestamp": "2025-12-11T10:30:00.000Z",
  "level": "INFO",
  "logger": "com.twsela.service.ShipmentService",
  "message": "Shipment created",
  "context": {
    "shipmentId": "uuid",
    "merchantId": "uuid",
    "userId": "uuid",
    "action": "CREATE_SHIPMENT"
  }
}
```

#### Log Aggregation
- Centralized logging with ELK stack (optional)
- Log rotation and retention policies
- Log analysis and search

### Health Checks

#### Endpoints
```http
GET /actuator/health
GET /actuator/health/liveness
GET /actuator/health/readiness
```

#### Response
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "SELECT 1"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "6.2.6"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 300000000000,
        "threshold": 10485760
      }
    }
  }
}
```

### Alerting

#### Alert Rules (Prometheus)
```yaml
groups:
  - name: twsela_alerts
    rules:
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate detected"
      
      - alert: DatabaseDown
        expr: up{job="mysql"} == 0
        for: 1m
        annotations:
          summary: "Database is down"
      
      - alert: HighMemoryUsage
        expr: jvm_memory_used_bytes / jvm_memory_max_bytes > 0.9
        for: 5m
        annotations:
          summary: "JVM memory usage above 90%"
```

---

## 🔌 Integrations

### Twilio SMS

#### Configuration
```yaml
twilio:
  account-sid: ${TWILIO_ACCOUNT_SID}
  auth-token: ${TWILIO_AUTH_TOKEN}
  phone-number: ${TWILIO_PHONE_NUMBER}
  enabled: true
```

#### Usage
```java
@Service
public class NotificationService {
    public void sendShipmentNotification(Shipment shipment) {
        smsService.send(
            shipment.getRecipientPhone(),
            "Your shipment #" + shipment.getTrackingNumber() + " is out for delivery"
        );
    }
}
```

#### Message Templates
- Shipment created
- Courier assigned
- Out for delivery
- Delivered
- Failed delivery

### Google Maps API

#### Features
- Zone polygon creation
- Distance calculation
- Route optimization
- Geocoding addresses
- Location validation

#### Configuration
```yaml
google:
  maps:
    api-key: ${GOOGLE_MAPS_API_KEY}
    enabled: true
```

### Payment Gateway

#### Supported Gateways
- Stripe
- PayPal
- Local MENA gateways (configurable)

#### Features
- Online payment processing
- COD management
- Refund processing
- Payment reconciliation
- Webhook handling

#### Configuration
```yaml
payment:
  gateway: stripe
  api-key: ${PAYMENT_API_KEY}
  webhook-secret: ${PAYMENT_WEBHOOK_SECRET}
  currencies:
    - SAR
    - AED
    - EGP
```

### Email Service (Optional)

#### Configuration
```yaml
mail:
  host: ${MAIL_HOST}
  port: ${MAIL_PORT}
  username: ${MAIL_USERNAME}
  password: ${MAIL_PASSWORD}
  smtp:
    auth: true
    starttls:
      enable: true
```

#### Templates
- Welcome email
- Password reset
- Shipment notifications
- Weekly reports
- Invoice delivery

---

## 🚢 Deployment

### Docker Deployment

#### Full Stack with Docker Compose
```bash
# Production deployment
docker-compose -f docker-compose.yml up -d

# With monitoring
docker-compose -f docker-compose.yml -f docker-compose.monitoring.yml up -d

# With backup
docker-compose -f docker-compose.yml -f docker-compose.backup.yml up -d
```

#### Docker Images
```dockerfile
# Backend
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/twsela-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

# Frontend (with Nginx)
FROM nginx:alpine
COPY frontend/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80 443
```

### Production Deployment

#### Prerequisites
- Linux server (Ubuntu 20.04+ recommended)
- Domain name with DNS configured
- SSL certificate (Let's Encrypt)
- Minimum 4GB RAM, 2 CPU cores

#### Deployment Steps

1. **Server Setup**
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

2. **Clone and Configure**
```bash
git clone https://github.com/khaledelmassry01/TWSELA.git
cd TWSELA
cp twsela/env.production.template twsela/.env
nano twsela/.env  # Configure environment variables
```

3. **SSL Setup**
```bash
# Using Let's Encrypt
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d twsela.com -d www.twsela.com
```

4. **Deploy**
```bash
docker-compose -f docker-compose.yml up -d
```

5. **Verify**
```bash
docker-compose ps
docker-compose logs -f
```

### Scaling

#### Horizontal Scaling
```yaml
# docker-compose.override.yml
services:
  backend:
    deploy:
      replicas: 3
    
  nginx:
    deploy:
      replicas: 2
```

#### Load Balancing
```nginx
upstream backend {
    least_conn;
    server backend1:8080;
    server backend2:8080;
    server backend3:8080;
}
```

### Backup Strategy

#### Automated Backups
```bash
# Database backup (daily)
0 2 * * * /path/to/backup-script.sh

# Configuration backup
/twsela/backup/backup-script.sh
```

#### Backup Script
```bash
#!/bin/bash
BACKUP_DIR="/backups/$(date +%Y-%m-%d)"
mkdir -p $BACKUP_DIR

# Database backup
mysqldump -u $DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/database.sql

# Redis backup
redis-cli SAVE
cp /var/lib/redis/dump.rdb $BACKUP_DIR/

# Application files
tar -czf $BACKUP_DIR/app.tar.gz /app

# Upload to cloud storage (optional)
aws s3 sync $BACKUP_DIR s3://twsela-backups/
```

### Maintenance

#### Zero-Downtime Deployment
```bash
# Blue-green deployment
docker-compose -f docker-compose.blue.yml up -d
# Test new version
# Switch traffic
docker-compose -f docker-compose.green.yml down
```

#### Database Migrations
```bash
# Using Flyway (integrated)
mvn flyway:migrate

# Manual migration
mysql -u root -p twsela < migration.sql
```

---

## 🤝 Contributing

We welcome contributions to Twsela! Please follow these guidelines:

### Development Setup

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write/update tests
5. Submit a pull request

### Code Standards

#### Backend (Java)
- Follow Java Code Conventions
- Use Spring Boot best practices
- Write JavaDoc for public methods
- Maintain test coverage > 80%

#### Frontend (JavaScript)
- ES6+ features
- Modular architecture
- ESLint configuration
- Prettier formatting

### Pull Request Process

1. Update documentation
2. Add tests for new features
3. Ensure all tests pass
4. Update CHANGELOG.md
5. Request review from maintainers

### Reporting Issues

- Use GitHub Issues
- Include reproduction steps
- Provide system information
- Attach relevant logs

---

## 💼 Business Model

Twsela operates on a SaaS-based business model with multiple revenue streams:

### Revenue Streams

#### 1. Subscription Tiers
- **Starter** (SME): $99/month
  - Up to 500 shipments/month
  - 5 users
  - Basic features
  - Email support

- **Growth** (Mid-Market): $299/month
  - Up to 2,000 shipments/month
  - 20 users
  - Advanced analytics
  - Priority support
  - Custom zones

- **Enterprise**: Custom pricing
  - Unlimited shipments
  - Unlimited users
  - White-labeling
  - Dedicated account manager
  - SLA guarantees
  - Custom integrations

#### 2. Usage-Based Fees
- Per-shipment fee: $0.10 - $0.50 (volume-based)
- SMS notifications: $0.02 per message
- API calls (enterprise): $0.001 per call

#### 3. Value-Added Services
- Premium analytics: $50/month
- Advanced route optimization: $100/month
- Custom reporting: $75/month
- Training and onboarding: $500 one-time
- Dedicated support: $500/month

#### 4. Payment Processing (Optional)
- 1.5% - 2.5% transaction fee on processed payments

### Target Market

#### Primary
- **SMEs**: E-commerce stores, small businesses
- **Mid-Market**: Regional logistics providers
- **Enterprise**: Large merchants, fulfillment centers

#### Geographic Focus
- Initial: GCC countries (Saudi Arabia, UAE, Kuwait, Qatar)
- Expansion: North Africa, Jordan, Lebanon
- Future: Broader MENA region

### Competitive Advantages

1. **Arabic-First Design**: Native RTL support
2. **Regional Focus**: MENA-specific features
3. **Comprehensive Solution**: End-to-end platform
4. **Transparent Pricing**: No hidden fees
5. **Modern Architecture**: Scalable and performant
6. **Security First**: Enterprise-grade security

### Key Metrics

- **MRR** (Monthly Recurring Revenue)
- **Customer Acquisition Cost** (CAC)
- **Lifetime Value** (LTV)
- **Churn Rate**
- **Net Revenue Retention**
- **Shipments Processed**
- **Active Users**

---

## 🗺️ Roadmap

### Phase 1: MVP ✅ (Completed)
- [x] Core shipment management
- [x] Multi-role authentication
- [x] Zone-based pricing
- [x] Basic analytics
- [x] Courier manifest generation

### Phase 2: Integrations 🚧 (In Progress)
- [x] Twilio SMS integration
- [x] Google Maps integration
- [ ] Payment gateway integration
- [ ] Email notifications
- [ ] Webhook system

### Phase 3: Advanced Features 📋 (Planned - Q1 2026)
- [ ] Mobile apps (iOS/Android)
- [ ] AI-powered route optimization
- [ ] Predictive analytics
- [ ] Advanced reporting engine
- [ ] Multi-language support (beyond Arabic/English)
- [ ] Voice notifications

### Phase 4: Scale & Optimize 🎯 (Q2 2026)
- [ ] Microservices architecture
- [ ] Kubernetes deployment
- [ ] Multi-region support
- [ ] Real-time tracking with WebSockets
- [ ] Advanced security features (2FA, biometrics)

### Phase 5: Ecosystem 🌐 (Q3-Q4 2026)
- [ ] Public API marketplace
- [ ] Partner integrations
- [ ] White-label solution
- [ ] Mobile SDK for developers
- [ ] Blockchain for proof of delivery

### Future Considerations
- Machine learning for delivery time prediction
- IoT integration for package tracking
- Drone delivery support
- Electric vehicle routing optimization
- Carbon footprint tracking

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### MIT License Summary
- ✅ Commercial use
- ✅ Modification
- ✅ Distribution
- ✅ Private use
- ❌ Liability
- ❌ Warranty

---

## 👨‍💻 Authors & Contributors

### Core Team
- **Khaled Elmassry** - Project Lead & Architecture
- Development Team - Full Stack Development

### Special Thanks
- All contributors who have helped shape Twsela
- Open source community for amazing tools and libraries

---

## 📞 Support & Contact

### Documentation
- [API Documentation](https://api.twsela.com/docs)
- [User Guide](https://docs.twsela.com)
- [Video Tutorials](https://youtube.com/twsela)

### Community
- [GitHub Discussions](https://github.com/khaledelmassry01/TWSELA/discussions)
- [Discord Server](https://discord.gg/twsela)
- [Stack Overflow](https://stackoverflow.com/questions/tagged/twsela)

### Commercial Support
- Email: support@twsela.com
- Enterprise: enterprise@twsela.com
- Phone: +966 XX XXX XXXX

### Social Media
- Twitter: [@twsela](https://twitter.com/twsela)
- LinkedIn: [Twsela](https://linkedin.com/company/twsela)
- Facebook: [Twsela](https://facebook.com/twsela)

---

## 🙏 Acknowledgments

### Technologies
- Spring Boot team for excellent framework
- Redis team for high-performance caching
- Twilio for reliable SMS service
- Google Maps for mapping capabilities
- All open source contributors

### Inspiration
- Modern logistics challenges in MENA
- Need for Arabic-first solutions
- Community feedback and requirements

---

## 📊 Project Statistics

![GitHub stars](https://img.shields.io/github/stars/khaledelmassry01/TWSELA?style=social)
![GitHub forks](https://img.shields.io/github/forks/khaledelmassry01/TWSELA?style=social)
![GitHub watchers](https://img.shields.io/github/watchers/khaledelmassry01/TWSELA?style=social)

![Code size](https://img.shields.io/github/languages/code-size/khaledelmassry01/TWSELA)
![Languages](https://img.shields.io/github/languages/count/khaledelmassry01/TWSELA)
![Top language](https://img.shields.io/github/languages/top/khaledelmassry01/TWSELA)
![Last commit](https://img.shields.io/github/last-commit/khaledelmassry01/TWSELA)

---

<div align="center">

**Made with ❤️ for the MENA logistics community**

[⬆ Back to Top](#twsela---comprehensive-courier-management-system)

</div>
