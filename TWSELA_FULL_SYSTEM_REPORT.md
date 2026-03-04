# Twsela — Full System Report
### Comprehensive Courier Management Platform for MENA
**Version:** 1.0 | **Date:** March 2026 | **Status:** Active Development (Sprint 48 Complete)

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Business Model](#2-business-model)
3. [System Architecture](#3-system-architecture)
4. [User Roles & Dashboards](#4-user-roles--dashboards)
5. [Feature Catalog](#5-feature-catalog)
   - 5.1 Authentication & Security
   - 5.2 Shipment Lifecycle
   - 5.3 Zone & Pricing Management
   - 5.4 Manifest Generation
   - 5.5 Warehouse Operations
   - 5.6 Financial Management
   - 5.7 Communication & Notifications
   - 5.8 Support & Help Center
   - 5.9 Analytics & Reporting
   - 5.10 Fleet & Vehicle Management
   - 5.11 Carrier & 3PL Integration
   - 5.12 E-Commerce Integration
   - 5.13 Workflow & Automation
   - 5.14 Multi-Tenancy
   - 5.15 Gamification & Loyalty
   - 5.16 Document Management
   - 5.17 Data Pipelines & Export
   - 5.18 Platform Operations
   - 5.19 Wallet & Payments
   - 5.20 Chat & Real-Time Communication
   - 5.21 Mobile & Offline Support
   - 5.22 Smart Assignment & Route Optimization
   - 5.23 Compliance & Audit
6. [Data Model Overview](#6-data-model-overview)
7. [API Layer](#7-api-layer)
8. [Frontend Application](#8-frontend-application)
9. [Infrastructure & DevOps](#9-infrastructure--devops)
10. [Security Architecture](#10-security-architecture)
11. [Integration Ecosystem](#11-integration-ecosystem)
12. [Development History](#12-development-history)
13. [Current System Stats](#13-current-system-stats)
14. [Roadmap](#14-roadmap)

---

## 1. Executive Summary

**Twsela** is a comprehensive, enterprise-grade courier and logistics management platform purpose-built for the MENA (Middle East and North Africa) region. It orchestrates the complete shipment lifecycle — from order creation to proof of delivery — across five distinct user roles: Owner, Admin, Merchant, Courier, and Warehouse.

### Key Highlights

| Metric | Value |
|--------|-------|
| Backend Entities | 174 domain models |
| Controllers (API) | 88 REST controllers |
| Services | 134+ business services |
| Repositories | 170 data access interfaces |
| Database Tables | 176 tables |
| Flyway Migrations | 59 versioned migrations (V1–V59) |
| Frontend Pages | 49 HTML pages |
| JS Page Handlers | 47 modular handlers |
| API Methods (Frontend) | ~195 api_service.js methods |
| Unit Tests | 938+ passing |
| Build Status | BUILD SUCCESS |
| Languages | Arabic (primary), English |

### Core Differentiators

- **Arabic-First UX**: Full RTL (Right-to-Left) support, Noto Sans Arabic typography, Arabic content throughout
- **Role-Based Architecture**: Each user role has a purpose-built dashboard and workflow
- **End-to-End Lifecycle**: From shipment creation through delivery with complete traceability
- **Zone-Based Pricing**: Dynamic pricing engine with geographic zone management
- **Production-Ready Infrastructure**: Redis caching, Nginx reverse proxy, Prometheus/Grafana monitoring, Docker containerization
- **Enterprise Security**: JWT authentication, RBAC, BCrypt hashing, CSRF protection, TLS/SSL, rate limiting, IP blocking

---

## 2. Business Model

### 2.1 Problem Statement

SMEs and regional logistics providers in MENA face:
- Fragmented workflows across order intake, courier assignment, and proof-of-delivery
- Inefficient routing and poor courier utilization
- High manual overhead in scheduling and manifest generation
- Weak Arabic localization in existing tools
- Disconnected payment, communication (SMS), and monitoring systems

### 2.2 Solution

Twsela delivers a unified platform that:
- Automates shipment lifecycle management end-to-end
- Provides role-specific dashboards for every stakeholder
- Offers zone-based pricing and intelligent courier assignment
- Integrates SMS notifications, payment gateways, Google Maps
- Monitors everything with Prometheus/Grafana dashboards
- Prioritizes Arabic-first UX with responsive design

### 2.3 Customer Segments

| Segment | Description | Key Needs |
|---------|-------------|-----------|
| **Owner/Admin** | Logistics operator or large merchant | End-to-end visibility, SLA control, pricing rules, workforce utilization |
| **Merchant (SME)** | E-commerce stores, D2C businesses | Fast shipment creation, tracking, pricing transparency, payment reconciliation |
| **Courier** | Field delivery personnel | Clear manifests, route guidance, easy status updates, accountability |
| **Warehouse** | Warehouse operations staff | Intake, sorting, dispatch workflows, inventory management |
| **Finance/Operations** | Cross-role stakeholders | Settlement, payouts, cost control, compliance reporting |

### 2.4 Revenue Model

#### Subscription Tiers

| Tier | Target | Price | Includes |
|------|--------|-------|----------|
| **Starter** | SME | $99/month | 500 shipments, 5 users, core features, email support |
| **Growth** | Mid-Market | $299/month | 2,000 shipments, 20 users, advanced analytics, priority support, custom zones |
| **Enterprise** | Large Operators | Custom | Unlimited everything, white-labeling, dedicated CSM, SLA guarantees, SSO |

#### Usage-Based Fees
- Per-shipment fee: $0.10–$0.50 (decreasing with volume)
- SMS notifications: $0.02 per message
- API calls (enterprise): $0.001 per call

#### Value-Added Services
- Premium analytics: $50/month
- Advanced route optimization: $100/month
- Custom reporting: $75/month
- Training and onboarding: $500 one-time
- Dedicated support: $500/month

#### Payment Processing (Optional)
- 1.5%–2.5% transaction fee on processed payments

### 2.5 Market Focus

- **Primary**: GCC (Saudi Arabia, UAE, Kuwait, Qatar)
- **Secondary**: North Africa (Egypt, Morocco), Jordan, Lebanon
- **Future**: Broader MENA and emerging markets with similar logistics constraints

### 2.6 Competitive Advantages

1. Arabic-first design with RTL best practices
2. Role-based dashboards tightly mapped to logistics operations
3. Built-in observability for peak delivery windows (Redis + Prometheus)
4. Zone-based pricing engine and manifest generation out of the box
5. Seamless integrations with MENA SMS, payment, and mapping providers
6. Lower total cost of ownership through integrated stack

### 2.7 Key Business Metrics

- **MRR** (Monthly Recurring Revenue)
- **Customer Acquisition Cost** (CAC) and LTV/CAC ratio
- **Churn Rate** and Net Revenue Retention
- **Shipments Processed** and On-Time Delivery Rate
- **Courier Utilization** and First-Attempt Delivery Success
- **Average Revenue Per User** (ARPU)

---

## 3. System Architecture

### 3.1 High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                           Nginx (SSL/TLS)                        │
│                        Reverse Proxy Layer                        │
└────────────────────┬──────────────────────┬──────────────────────┘
                     │                      │
          ┌──────────▼──────────┐  ┌────────▼─────────────┐
          │   Frontend (Vite)   │  │   Spring Boot API    │
          │   49 HTML Pages     │  │   88 Controllers     │
          │   47 JS Handlers    │  │   134+ Services      │
          │   Vanilla JS/ES6+   │  │   Java 17            │
          └─────────────────────┘  └────────┬─────────────┘
                                            │
              ┌─────────────────────────────┼──────────────────────┐
              │                             │                      │
       ┌──────▼──────────┐   ┌──────────────▼──────┐   ┌──────────▼────────┐
       │  MySQL 9.4.0    │   │   Redis 6+          │   │ Prometheus+Grafana│
       │  176 Tables     │   │   Cache + Sessions  │   │ Monitoring Stack  │
       │  59 Migrations  │   │   Rate Limiting     │   │ Custom Dashboards │
       └─────────────────┘   └─────────────────────┘   └───────────────────┘
                                            │
              ┌─────────────────────────────┼──────────────────────┐
              │                             │                      │
       ┌──────▼──────────┐   ┌──────────────▼──────┐   ┌──────────▼────────┐
       │   Twilio SMS    │   │   Payment Gateways  │   │   Google Maps     │
       │   Notifications │   │   Stripe/Paymob/Tap │   │   Zone Mapping    │
       └─────────────────┘   └─────────────────────┘   └───────────────────┘
```

### 3.2 Technology Stack

| Layer | Technology | Version/Details |
|-------|-----------|-----------------|
| **Backend Framework** | Spring Boot | 3.3.3 |
| **Language** | Java | 17 |
| **Build Tool** | Maven | 3.6+ |
| **Database** | MySQL | 9.4.0, utf8mb4 charset |
| **ORM** | Spring Data JPA + Hibernate | MySQL8Dialect |
| **Cache** | Redis | 6+, pool max-active 16 |
| **Auth** | JWT (jjwt 0.12.6) | 24h token expiry |
| **API Docs** | SpringDoc OpenAPI | 2.7.0 |
| **Frontend Build** | Vite | 4.5.14 |
| **Frontend JS** | Vanilla ES6+ Modules | Modular architecture |
| **CSS** | Tailwind CSS + Custom | RTL-first responsive |
| **Icons** | Font Awesome | 6.4.0 |
| **UI Framework** | Bootstrap | 5.3.2 |
| **Charts** | Chart.js | Analytics dashboards |
| **Reverse Proxy** | Nginx | SSL termination |
| **Monitoring** | Prometheus + Grafana | Pre-configured dashboards |
| **Containerization** | Docker + Docker Compose | Production-ready |
| **SMS** | Twilio | With retry (3 attempts) |
| **Maps** | Google Maps API | Zone polygon + geocoding |
| **PDF** | iText7 | Arabic font support |
| **Excel** | Apache POI | Import/export |

### 3.3 Application Layers

```
┌─────────────────────────────────────────────────────┐
│  PRESENTATION LAYER                                 │
│  49 HTML pages + 47 JS handlers + shared services   │
├─────────────────────────────────────────────────────┤
│  API LAYER                                          │
│  88 REST controllers + 70 DTOs + validation         │
├─────────────────────────────────────────────────────┤
│  BUSINESS LOGIC LAYER                               │
│  134+ services + event handlers + workflows         │
├─────────────────────────────────────────────────────┤
│  DATA ACCESS LAYER                                  │
│  170 repositories + 174 entities + cache layer      │
├─────────────────────────────────────────────────────┤
│  INFRASTRUCTURE LAYER                               │
│  Security filters + config + integrations + jobs    │
└─────────────────────────────────────────────────────┘
```

### 3.4 Server Configuration

| Setting | Value |
|---------|-------|
| Backend Port | 8000 |
| Frontend Port (dev) | 5173 |
| Database URL | jdbc:mysql://localhost:3306/twsela |
| DDL Mode | validate (Flyway manages schema) |
| Redis TTL | 30 minutes (1,800,000 ms) |
| JWT Expiry | 24 hours (86,400,000 ms) |
| Tomcat Threads | Max 200, min-spare 20 |
| Backup Schedule | Daily at 2:00 AM |
| Backup Retention | 30 days |
| Log Max Size | 100MB, 30 day history |

---

## 4. User Roles & Dashboards

### 4.1 Owner (👑 System Owner)

**Purpose**: Complete system oversight with full administrative control

**Dashboard Features**:
- Revenue and shipment KPI cards
- Shipment volume charts (daily/weekly/monthly)
- Courier performance overview
- Merchant activity metrics
- Quick action buttons

**Pages (14 HTML + 14 JS handlers)**:

| Page | Description |
|------|-------------|
| Dashboard | Comprehensive KPIs, charts, performance overview |
| Merchants | Manage all merchants — onboard, activate, suspend, view details |
| Employees | Manage admins, couriers, warehouse staff — add, edit, assign roles |
| Zones | Create/edit delivery zones with map integration, set boundaries |
| Pricing | Configure zone-based, distance-based, weight-based pricing rules |
| Shipments | View all shipments system-wide, filter, search, override status |
| Payouts | Manage courier and merchant payouts, settlement batches |
| Wallets | View all wallet balances, transactions, adjustments |
| Returns | Manage returned shipments, process refunds, track exceptions |
| Reports Hub | Access to all report categories |
| → Courier Reports | Courier performance — deliveries, success rate, timing |
| → Merchant Reports | Merchant activity — shipment volume, revenue, growth |
| → Warehouse Reports | Warehouse throughput — intake speed, dispatch efficiency |
| Settings | System-wide configuration — SMS, payment, zone defaults |
| Analytics | Advanced charts, trends, predictions, comparison views |

### 4.2 Admin (🔧 Operations Manager)

**Purpose**: Day-to-day operational management and user administration

**Dashboard Features**:
- Operational KPIs (pending, in-progress, completed)
- Quick user and shipment management
- Recent activity feed

**Pages (2 HTML + 2 JS handlers)**:

| Page | Description |
|------|-------------|
| Dashboard | Operational overview — shipment counts, user stats, quick actions |
| Support Management | View all support tickets, assign agents, resolve, close, reply |

### 4.3 Merchant (🏪 Business Customer)

**Purpose**: Self-service shipment creation, tracking, and analytics

**Dashboard Features**:
- Shipment status distribution
- Recent shipments table
- Revenue metrics
- Quick create shipment button

**Pages (10 HTML + 10 JS handlers)**:

| Page | Description |
|------|-------------|
| Dashboard | Shipment overview, recent activity, KPIs |
| Create Shipment | Single shipment form — recipient, address, zone, weight, COD, priority |
| Shipments | Paginated shipment list with status filters, search, export |
| Shipment Details | Full shipment timeline, status history, courier info, proof of delivery |
| Bulk Upload | Upload Excel/CSV for mass shipment creation with validation |
| Invoices | View and download invoices, payment history |
| Labels | Generate and print shipping labels (PDF with barcode/QR) |
| Pickups | Schedule courier pickups — time slot selection, recurring options |
| Recipients | Manage recipient database — saved addresses, contact info |
| Returns | Track returned shipments, initiate returns |

### 4.4 Courier (🚚 Delivery Personnel)

**Purpose**: Efficient manifest execution, status updates, and route guidance

**Dashboard Features**:
- Today's delivery count and earnings
- Active delivery status
- Quick status update buttons

**Pages (5 HTML + 5 JS handlers)**:

| Page | Description |
|------|-------------|
| Dashboard | Today's deliveries, earnings, performance metrics |
| Manifest | Daily delivery list — sorted by priority and zone, checklists |
| Route | Google Maps integration — optimized route view, navigation links |
| Delivery | Active delivery management — status updates, proof of delivery capture |
| Pickups | View assigned pickups, confirm collection, report issues |

### 4.5 Warehouse (📦 Operations Staff)

**Purpose**: Intake, processing, sorting, dispatch, and inventory management

**Dashboard Features**:
- Intake/dispatch counts
- Pending processing queue
- Throughput metrics

**Pages (6 HTML + 6 JS handlers)**:

| Page | Description |
|------|-------------|
| Dashboard | Warehouse KPIs — intake, dispatch, pending, throughput rates |
| Zones | Manage warehouse zones and storage bins — capacity, types, utilization |
| Receiving | Process incoming shipments — create receiving orders, verify items, assign zones |
| Fulfillment | Prepare outgoing orders — assign pickers, prioritize, complete fulfillment |
| Pick Waves | Create and manage pick waves — batch multiple orders for efficient picking |
| Inventory | Track inventory movements — inbound, outbound, transfers, stock levels |

### 4.6 Shared Pages (All Roles)

| Page | Description |
|------|-------------|
| Login | JWT-based authentication with role-based redirect |
| Profile | View and edit personal information, change password |
| Settings | Personal preferences, notification settings |
| Wallet | Personal wallet balance, transaction history, top-up |
| Notifications | Notification center — filter, read, mark as read |
| Support | Submit support tickets, track status, reply to messages |
| Help Center | Browse help articles by category, search, view details |
| Tracking | Public shipment tracking by tracking number |
| Contact | Public contact form |
| 404 | Custom error page |
| Landing (index) | Public marketing landing page |

---

## 5. Feature Catalog

### 5.1 Authentication & Security

**What it does**: Secures the entire platform with multi-layer authentication, authorization, and threat protection.

**Capabilities**:
- **JWT Authentication**: Stateless token-based auth with 24-hour expiry and refresh mechanism
- **Role-Based Access Control (RBAC)**: 5 roles (Owner, Admin, Merchant, Courier, Warehouse) with hierarchical permissions
- **Password Security**: BCrypt hashing with configurable strength, complexity requirements, password reset
- **OTP Verification**: One-time password for phone verification — 5 minute validity, max 5 attempts
- **CSRF Protection**: Cookie-based CSRF tokens for state-changing operations
- **Rate Limiting**: Redis-backed per-endpoint and IP-based throttling (Bucket4j)
- **IP Blocking**: Blacklist management for malicious IPs, automatic blocking on repeated violations
- **Account Lockout**: Automatic lockout after repeated failed login attempts
- **Input Sanitization**: XSS prevention, SQL injection protection via parameterized queries
- **API Key Management**: Key generation, rotation, usage tracking, revocation for external integrations
- **Security Event Logging**: Comprehensive audit trail of all authentication and authorization events
- **Fraud Detection**: Blacklist management for known fraudulent entities

**Backend Components**: AuthController, SecurityConfig, JwtService, JwtAuthenticationFilter, RateLimitFilter, IpBlacklistFilter, InputSanitizationFilter, AccountLockoutService, ApiKeyService, SecurityEventService, OtpService, PasswordPolicyService, TokenBlacklistService

---

### 5.2 Shipment Lifecycle

**What it does**: Manages the complete journey of a shipment from creation to final delivery or return.

**Status Flow**:
```
CREATED → ASSIGNED_TO_COURIER → PICKED_UP → IN_TRANSIT → OUT_FOR_DELIVERY → DELIVERED
                                                                           ↓
                                                                   FAILED → RETURNED
```

**Capabilities**:
- **Shipment Creation**: Single form or bulk upload (Excel/CSV) with validation
- **Automatic Assignment**: Smart courier assignment based on zone, capacity, and performance scores
- **Real-Time Status**: Live updates at every lifecycle stage with event logging
- **Status History**: Complete audit trail with timestamps, location, and actor information
- **Proof of Delivery**: Capture delivery confirmation (signature, photo)
- **Exception Handling**: Failed delivery management — reschedule, return, redirect
- **Return Processing**: Full return flow with reason tracking and refund management
- **Delivery Attempts**: Track multiple delivery attempts per shipment
- **Delivery Redirects**: Allow recipients to redirect shipments to alternate addresses
- **Package Details**: Weight, dimensions, special handling instructions
- **Priority Handling**: Normal, High, Urgent with escalation
- **Bulk Operations**: Mass status updates, export, label generation
- **Public Tracking**: Anyone can track a shipment with the tracking number (no login required)

**Backend Components**: ShipmentController, ShipmentService, ShipmentEventHandler, DeliveryController, DeliveryAttemptService, DeliveryProofService, ReturnController, ReturnService, SmartAssignmentService, PublicTrackingController

---

### 5.3 Zone & Pricing Management

**What it does**: Defines geographic delivery zones and configures dynamic pricing rules.

**Capabilities**:
- **Zone Creation**: Define delivery zones with name (Arabic/English), geographic boundaries
- **Map Integration**: Google Maps for polygon drawing, geocoding, distance calculation
- **Zone Types**: Standard, express, remote, restricted areas
- **Pricing Models**:
  - Flat rate per zone
  - Distance-based (per km)
  - Weight-based (tiered)
  - Hybrid (combination)
- **Surcharges**: Remote area fees, express delivery premium, holiday surcharges
- **Custom Pricing Rules**: Per-merchant overrides, volume discounts, promotional rates
- **Courier Territory Assignment**: Map couriers to specific zones for specialization
- **Zone Capacity Management**: Load balancing across zones to prevent overload
- **Carrier Zone Mapping**: Map external carriers to specific zones

**Backend Components**: DashboardController (zones), ZoneRepository, DeliveryPricing, CustomPricingService, ContractPricingController, CarrierZoneMapping, CourierZone

---

### 5.4 Manifest Generation

**What it does**: Creates optimized daily delivery manifests for couriers.

**Capabilities**:
- **Territory Grouping**: Cluster shipments by courier zones for efficient routes
- **Priority Sorting**: Urgent deliveries first, then by time windows
- **Route Optimization**: Suggest optimal delivery sequence to minimize distance
- **Load Balancing**: Distribute shipments evenly across available couriers
- **Capacity Constraints**: Respect courier vehicle capacity limits
- **Output Formats**:
  - PDF manifests with Arabic support (printer-friendly)
  - Excel export for detailed data
  - Mobile-optimized view for courier devices
- **Daily Automation**: Option for automatic manifest generation at scheduled times

**Backend Components**: ManifestController, ShipmentManifest, OptimizedRoute, RouteOptimizationService

---

### 5.5 Warehouse Operations

**What it does**: Manages all warehouse workflows from receiving shipments to dispatch.

**Capabilities**:
- **Warehouse Zones**: Define zones by type (Receiving, Storage, Packing, Dispatch, Returns, Quarantine, Cold Storage) with capacity tracking
- **Storage Bins**: Manage individual bins within zones — capacity, location, status
- **Receiving Orders**: Process incoming shipments — verify items, assign to zones, track discrepancies
- **Fulfillment Orders**: Prepare outgoing orders — assign pickers, set priorities, track completion
- **Pick Waves**: Batch multiple fulfillment orders into waves for efficient picking — start, track progress, complete
- **Inventory Movements**: Track every stock movement — inbound, outbound, transfers, adjustments, returns, damage, cycle count
- **Inventory Summary**: Real-time stock levels by zone with movement history
- **Operation Types**: Receiving, Put-away, Picking, Packing, Shipping, Returns processing

**Backend Components**: WarehouseZoneController, WarehouseZoneService, StorageBinService, FulfillmentController, FulfillmentService, ReceivingService, PickWaveService, InventoryMovementService

---

### 5.6 Financial Management

**What it does**: Handles all monetary aspects — pricing, collections, payments, settlements, invoicing.

**Capabilities**:
- **Pricing Engine**: Dynamic price calculation based on zones, distance, weight, and custom rules
- **COD Management**: Cash-on-delivery collection tracking with courier accountability
- **Payment Gateway Integration**: Multiple gateways — Stripe, Paymob (Egypt), Fawry (Egypt), Tap (GCC)
- **Payment Intents**: Create and track payment sessions with status management
- **Refund Processing**: Full and partial refunds with reason tracking
- **Settlement System**: Batch settlement processing — group transactions, calculate net amounts
- **Payout Management**: Courier earnings and merchant payouts with scheduled disbursement
- **Invoicing**: Automated invoice generation with line items and Arabic PDF support
- **Cash Ledger**: Track all cash movements with double-entry bookkeeping
- **Exchange Rates**: Multi-currency support with configurable exchange rates
- **Tax Management**: Tax rules per country/region with E-Invoice support
- **Promo Codes**: Discount codes with validity periods, usage limits, and targeting rules
- **Subscription Billing**: Tiered subscription plans with metering

**Backend Components**: FinancialController, FinancialService, PaymentIntentService, PaymentRefundService, SettlementController, SettlementService, InvoiceController, InvoiceService, WalletController, WalletService, TaxController, TaxService, EInvoiceService, PromoCodeService, SubscriptionController, SubscriptionService, CurrencyService, ExchangeRate

---

### 5.7 Communication & Notifications

**What it does**: Keeps all stakeholders informed through multiple communication channels.

**Capabilities**:
- **SMS Notifications (Twilio)**: Automated SMS at each shipment lifecycle stage with retry (3 attempts, 30s timeout)
- **Push Notifications**: Mobile push via device tokens (FCM/APNS)
- **Email Notifications**: SMTP integration for detailed communications
- **WhatsApp Notifications**: WhatsApp Business API integration
- **In-App Notifications**: Real-time notification bell with read/unread states
- **Live Notifications**: WebSocket-based instant updates
- **Notification Templates**: Customizable templates with variable substitution
- **Notification Preferences**: Per-user channel preferences (SMS, email, push, in-app)
- **Notification Channels**: Define channels with priority and fallback behavior
- **Delivery Tracking**: Track notification delivery status per channel
- **Retry Management**: Automatic retry for failed notifications
- **Notification Analytics**: Delivery rates, open rates, click rates
- **Multi-Language**: Arabic and English message templates

**Backend Components**: SmsController, SmsService, TwilioSmsService, NotificationController, NotificationService, NotificationDispatcher, NotificationRetryService, LiveNotificationController, LiveNotificationService, PushNotificationService, EmailNotificationService, WhatsAppNotificationService, NotificationPreferenceController, NotificationTemplateController, NotificationAnalyticsService

---

### 5.8 Support & Help Center

**What it does**: Provides customer support infrastructure and self-service help resources.

**Capabilities**:
- **Support Tickets**: Users create tickets with category, priority, subject, and description
- **Ticket Categories**: Shipment Issue, Payment, Account, Technical, Pickup, Delivery, Billing, Other
- **Ticket Priorities**: Low, Medium, High, Urgent
- **Ticket Lifecycle**: Open → In Progress → Waiting Customer → Waiting Agent → Resolved → Closed
- **Message Thread**: Back-and-forth messaging between user and support agent
- **Agent Assignment**: Admins assign tickets to themselves or specific agents
- **Ticket Resolution**: Mark as resolved with resolution notes
- **Support Dashboard (Admin)**: View all tickets, filter by status/priority/category, statistics
- **Support Statistics**: Open count, in-progress count, resolved count, average response time
- **Help Center**: Browsable knowledge base organized by categories
- **Help Categories**: Getting Started, Shipments, Payments, Account, Courier, Warehouse
- **Help Articles**: Searchable how-to articles with rich content
- **Self-Service**: Find answers without creating support tickets

**Backend Components**: SupportController, SupportTicketService, KnowledgeArticle

**Frontend Pages**: support.html, admin/support.html, help.html + their JS handlers

---

### 5.9 Analytics & Reporting

**What it does**: Provides data-driven insights for decision-making at every level.

**Capabilities**:
- **Executive Dashboard**: High-level KPIs — revenue, shipment volume, delivery rate
- **Operational Reports**: Daily operations summary — pending, in-transit, delivered, failed
- **Courier Performance**: Deliveries per hour, success rate, on-time percentage, route efficiency
- **Merchant Analytics**: Shipment frequency, average value, growth trends
- **Warehouse Throughput**: Processing speed, bottleneck identification, capacity utilization
- **Financial Reports**: Revenue, costs, profitability analysis, outstanding payments
- **Custom Report Builder**: Define custom queries and visualization types
- **Report Scheduling**: Automated report generation on schedule
- **Export Formats**: PDF (with Arabic), Excel, CSV
- **KPI Snapshots**: Periodic snapshots of key metrics for trend analysis
- **BI Dashboard**: Business intelligence with advanced visualizations
- **Demand Prediction**: Forecast shipment volumes for resource planning
- **Platform Metrics**: System health, response times, error rates

**Backend Components**: AnalyticsController, AnalyticsService, ReportsController, ReportExportController, ReportExportService, BIDashboardController, BIDashboardService, CustomReportController, CustomReportService, KPISnapshotService, DemandPredictionService, CourierAnalyticsService, MerchantAnalyticsService, OperationsAnalyticsService, RevenueAnalyticsService, MetricsService

**Frontend Pages**: owner/analytics.html, owner/reports.html, owner/reports/couriers.html, owner/reports/merchants.html, owner/reports/warehouse.html

---

### 5.10 Fleet & Vehicle Management

**What it does**: Manages the courier fleet including vehicles, assignments, and maintenance.

**Capabilities**:
- **Vehicle Registry**: Register vehicles with type, capacity, license plate, insurance details
- **Vehicle Assignment**: Assign vehicles to couriers based on delivery requirements
- **Maintenance Scheduling**: Track maintenance schedules, service history, and fuel logs
- **Fuel Tracking**: Log fuel consumption for cost analysis and route efficiency
- **Vehicle Status**: Track availability, in-service, maintenance, retired
- **Capacity Planning**: Match vehicle capacity to delivery volumes

**Backend Components**: FleetController, FleetService, Vehicle, VehicleAssignment, VehicleMaintenance, FuelLog

---

### 5.11 Carrier & 3PL Integration

**What it does**: Enables integration with external carriers and third-party logistics providers.

**Capabilities**:
- **Multi-Carrier Support**: Register and manage multiple external carriers
- **Carrier Rates**: Configure rates per carrier/zone/service level
- **Carrier Selection Rules**: Automatic carrier selection based on cost, speed, or zone coverage
- **Carrier Shipment Tracking**: Track shipments handed off to external carriers
- **Carrier Zone Mapping**: Map carriers to specific geographic zones
- **Webhook Integration**: Receive carrier status updates via webhooks
- **3PL Partner Handoff**: Manage handoffs to third-party logistics partners
- **Partner Portal**: Third-party partners can access shared data

**Backend Components**: CarrierController, CarrierService, CarrierRate, CarrierSelectionRule, CarrierShipment, CarrierZoneMapping, CarrierWebhookLog, ThirdPartyPartnerController, ThirdPartyPartnerService, PartnerHandoff

---

### 5.12 E-Commerce Integration

**What it does**: Connects Twsela to major e-commerce platforms for automatic order import.

**Capabilities**:
- **Platform Connectors**: Shopify, WooCommerce, Salla, Zid (MENA platforms)
- **Connection Management**: Configure and manage store connections with credentials
- **Automatic Order Import**: Pull new orders from connected stores
- **Status Sync**: Push shipment status updates back to the e-commerce platform
- **Webhook Processing**: Receive real-time order events from platforms
- **Order Mapping**: Map e-commerce order fields to Twsela shipment fields

**Backend Components**: ECommerceController, ECommerceService, ECommerceWebhookController, ECommerceIntegration, ECommerceIntegrationFactory, ShopifyIntegration, WooCommerceIntegration, SallaIntegration, ZidIntegration, ECommerceConnection, ECommerceOrder

---

### 5.13 Workflow & Automation

**What it does**: Enables custom business process automation without code changes.

**Capabilities**:
- **Workflow Definitions**: Define multi-step workflows with conditions and actions
- **Workflow Templates**: Pre-built templates for common logistics processes
- **Step Execution**: Track each step's status, inputs, outputs, and errors
- **Automation Rules**: Trigger-action rules (e.g., "when status changes to DELIVERED, send SMS")
- **Scheduled Tasks**: Cron-based task scheduling with execution tracking
- **Event-Driven**: Workflows triggered by domain events
- **Workflow Execution History**: Complete audit trail of all workflow runs

**Backend Components**: WorkflowDefinitionController, WorkflowExecutionController, WorkflowDefinitionService, WorkflowExecutionService, WorkflowStepService, WorkflowTemplateService, AutomationRuleController, AutomationRuleService, ScheduledTaskController, ScheduledTaskService

---

### 5.14 Multi-Tenancy

**What it does**: Supports multiple independent organizations on a single platform instance.

**Capabilities**:
- **Tenant Isolation**: Complete data isolation between tenants
- **Tenant Configuration**: Per-tenant settings, defaults, and feature flags
- **Tenant Branding**: Custom logos, colors, and themes per tenant
- **Tenant Quotas**: Resource limits per tenant — users, shipments, storage
- **Tenant Users**: Manage users within a tenant with invitations
- **Tenant Audit**: Per-tenant audit logging
- **Tenant Migration**: Move tenants between environments
- **Default Tenant**: System-level default tenant for bootstrapping

**Backend Components**: TenantController, TenantService, TenantBrandingController, TenantBrandingService, TenantQuotaController, TenantQuotaService, TenantUserController, TenantInvitationService, TenantConfigService, TenantContextService, TenantIsolationService, TenantMigrationService, TenantContextFilter, TenantDataFilter, TenantAuditLog

---

### 5.15 Gamification & Loyalty

**What it does**: Motivates couriers and engages merchants through game mechanics and loyalty programs.

**Capabilities**:
- **Achievements**: Define and award badges/achievements for milestones
- **Leaderboards**: Rank couriers by performance metrics
- **Gamification Profiles**: Track individual progress, points, and level
- **Loyalty Programs**: Point-based loyalty for merchants
- **Loyalty Transactions**: Earn and redeem loyalty points
- **Campaigns**: Marketing campaigns with targeting and scheduling
- **Satisfaction Surveys**: Collect feedback from recipients after delivery

**Backend Components**: GamificationController, GamificationService, LoyaltyController, LoyaltyService, Achievement, LeaderboardEntry, GamificationProfile, LoyaltyProgram, LoyaltyTransaction, Campaign, CampaignService, SatisfactionSurvey, ServiceFeedback, MerchantServiceFeedback, CourierRating, RatingController

---

### 5.16 Document Management

**What it does**: Generates, manages, and distributes business documents.

**Capabilities**:
- **PDF Generation**: Manifests, invoices, labels, reports with full Arabic support (iText7)
- **Document Templates**: Customizable templates for different document types
- **Label Printing**: Shipping labels with barcode/QR code
- **AWB (Air Waybill)**: Generate AWB documents for shipments
- **Bulk Document Generation**: Batch processing for multiple documents
- **Document Audit Trail**: Track who generated/viewed/downloaded each document
- **Digital Signatures**: Electronic signature capture and verification
- **Customs Documents**: Cross-border shipping documentation

**Backend Components**: DocumentController, DocumentTemplateService, LabelController, PdfService, BarcodeService, AwbService, ExcelService, SignatureController, SignatureService, DigitalSignature, DocumentBatch, DocumentAuditLog, CustomsDocument

---

### 5.17 Data Pipelines & Export

**What it does**: Enables large-scale data processing, export, and integration.

**Capabilities**:
- **Data Export Jobs**: Schedule and execute data exports (full, incremental)
- **Pipeline Configuration**: Define data transformation and routing pipelines
- **Pipeline Execution**: Track pipeline runs with status and performance metrics
- **Data Archiving**: Automatic archiving of old records based on policies
- **Cleanup Tasks**: Scheduled housekeeping of expired data
- **Custom Reports**: Build and schedule custom data reports
- **Search Indexing**: Internal search index for fast data retrieval

**Backend Components**: DataPipelineController, DataPipelineService, DataExportJob, PipelineExecution, DataPipelineConfig, ArchivedRecord, ArchivePolicy, CleanupTask, SearchIndex

---

### 5.18 Platform Operations

**What it does**: System administration, health monitoring, and operational tools.

**Capabilities**:
- **System Health**: Real-time health checks for all services (DB, Redis, disk)
- **Maintenance Windows**: Schedule and announce maintenance periods
- **System Alerts**: Configurable alerts for system anomalies
- **System Settings**: Global configuration management with change tracking
- **System Audit Log**: Track all administrative actions
- **Feature Flags**: Toggle features on/off without deployment, with audit trail
- **App Version Config**: Control minimum app versions and force-update policies
- **Dead Letter Queue**: Manage failed events for retry or investigation
- **Async Job Management**: Monitor and manage background jobs
- **Backup Management**: Database backup scheduling and monitoring
- **Platform Metrics**: Track platform usage, active users, request volumes
- **Telemetry**: System and application telemetry collection
- **Usage Tracking**: Monitor resource consumption per tenant/user

**Backend Components**: PlatformOpsController, PlatformOpsService, SystemHealthController, SystemHealthService, PlatformConfigController, SettingsController, FeatureFlagService, ScheduledTaskService, AsyncJobController, AsyncJobService, BackupController, BackupService, DeadLetterController, DeadLetterService, TelemetryController, MasterDataController

---

### 5.19 Wallet & Payments

**What it does**: Manages internal wallet system for all users with transaction tracking.

**Capabilities**:
- **User Wallets**: Each user has an internal wallet with balance
- **Wallet Transactions**: Credit, debit, transfer, refund, top-up, withdrawal
- **Transaction History**: Full history with filtering and export
- **Balance Management**: Real-time balance tracking
- **COD Integration**: COD collections automatically credited to merchant wallets
- **Payout Processing**: Withdraw from wallet to bank account
- **Payment Webhooks**: Process webhook callbacks from payment gateways

**Backend Components**: WalletController, WalletService, Wallet, WalletTransaction, PaymentCallbackController, PaymentWebhookProcessor, PaymentWebhookLog

---

### 5.20 Chat & Real-Time Communication

**What it does**: Enables real-time messaging between users for operational coordination.

**Capabilities**:
- **Chat Rooms**: Create conversation rooms between users (e.g., merchant-courier)
- **Message History**: Persistent message storage with pagination
- **WebSocket Support**: Real-time message delivery via WebSocket
- **Presence Detection**: Track online/offline status of users
- **Typing Indicators**: Real-time typing indicators

**Backend Components**: ChatController, ChatService, ChatMessage, ChatRoom, WebSocketMessageController, WebSocketConfig, WebSocketAuthInterceptor, PresenceService

---

### 5.21 Mobile & Offline Support

**What it does**: Enables mobile usage and handles connectivity issues.

**Capabilities**:
- **Device Registration**: Register mobile devices for push notifications
- **Device Tokens**: FCM/APNS token management for push delivery
- **Offline Queue**: Queue operations when offline, sync when reconnected
- **Sync Sessions**: Track synchronization sessions between device and server
- **Sync Conflict Resolution**: Handle data conflicts from offline operations
- **Battery Optimization**: Configure background sync behavior for battery efficiency
- **Responsive Design**: All pages work on mobile, tablet, and desktop

**Backend Components**: DeviceMobileController, DeviceMobileService, OfflineSyncController, OfflineSyncService, DeviceRegistration, DeviceToken, OfflineQueue, SyncSession, SyncConflict, BatteryOptimizationConfig

---

### 5.22 Smart Assignment & Route Optimization

**What it does**: Intelligently assigns shipments to couriers and optimizes delivery routes.

**Capabilities**:
- **Assignment Rules**: Configurable rules for automatic courier assignment
- **Assignment Scoring**: Score couriers based on distance, capacity, performance, zone familiarity
- **Route Optimization**: Calculate optimal route for a set of deliveries
- **ETA Calculation**: Estimate delivery times based on distance and traffic
- **Demand Prediction**: Forecast delivery volumes for proactive resource allocation
- **Live Tracking**: Real-time courier location tracking with history
- **Location Pings**: Periodic GPS updates from courier devices
- **Tracking Sessions**: Track active delivery sessions

**Backend Components**: SmartAssignmentController, SmartAssignmentService, RouteController, RouteOptimizationService, ETACalculationService, DemandController, DemandPredictionService, CourierLocationController, CourierLocationService, LiveTrackingController, LiveTrackingService, TrackingSessionService, AssignmentRule, AssignmentScore, OptimizedRoute, LocationPing, CourierLocationHistory

---

### 5.23 Compliance & Audit

**What it does**: Ensures regulatory compliance and provides comprehensive audit capabilities.

**Capabilities**:
- **Compliance Rules**: Define compliance requirements per region/market
- **Compliance Reports**: Generate compliance status reports
- **Audit Trail**: Every significant action is logged with actor, timestamp, and details
- **Document Audit**: Track document access and modifications
- **Security Events**: Log all security-related events
- **Data Export**: Export audit logs for external compliance tools
- **SLA Policies**: Define and enforce service level agreements
- **Contract Management**: Manage contracts with merchants with SLA terms

**Backend Components**: ComplianceController, ComplianceService, AuditController, AuditService, SecurityAuditService, SecurityEventController, SecurityEventService, ContractController, ContractService, ContractSlaController, ContractSlaService, ComplianceReport, ComplianceRule, SystemAuditLog, DocumentAuditLog, SlaPolicy, Contract, ContractSlaTerms

---

## 6. Data Model Overview

The system uses 174 domain entities organized across 176 database tables (managed by 59 Flyway migrations). Below are the major entity groups:

### Core Entities
| Entity | Purpose |
|--------|---------|
| User | System users with role and authentication |
| Role | OWNER, ADMIN, MERCHANT, COURIER, WAREHOUSE |
| Shipment | Core shipment record |
| ShipmentStatus | Status definitions |
| ShipmentStatusHistory | Status change audit trail |
| ShipmentManifest | Courier delivery manifests |
| ShipmentPackageDetails | Package dimensions and weight |
| Zone | Delivery zones with geographic boundaries |
| DeliveryPricing | Zone-based pricing configuration |

### User & Profile Entities
| Entity | Purpose |
|--------|---------|
| CourierDetails | Courier profile extension |
| MerchantDetails | Merchant profile extension |
| RecipientDetails | Shipment recipient information |
| RecipientProfile | Saved recipient profiles |
| RecipientAddress | Saved addresses |
| UserStatus | Online/offline/active/inactive |
| AccountLockout | Failed login tracking |

### Financial Entities
| Entity | Purpose |
|--------|---------|
| Wallet | User wallet balances |
| WalletTransaction | Wallet credit/debit records |
| PaymentTransaction | External payment records |
| PaymentIntent | Payment session tracking |
| PaymentRefund | Refund processing |
| PaymentMethod | Saved payment methods |
| Invoice / InvoiceItem | Invoice generation |
| Payout / PayoutItem | Merchant/courier payouts |
| SettlementBatch / SettlementItem | Settlement processing |
| CashMovementLedger | Cash flow tracking |
| ExchangeRate | Multi-currency rates |
| Currency / Country | Reference data |
| TaxRule | Tax configuration |
| PromoCode | Discount codes |

### Warehouse Entities
| Entity | Purpose |
|--------|---------|
| Warehouse | Warehouse locations |
| WarehouseZone | Zones within warehouses |
| WarehouseInventory | Stock levels |
| StorageBin | Individual storage locations |
| ReceivingOrder / ReceivingOrderItem | Incoming shipment processing |
| FulfillmentOrder / FulfillmentOrderItem | Outgoing order preparation |
| PickWave | Batch picking waves |
| InventoryMovement | Stock movement tracking |

### Delivery & Logistics Entities
| Entity | Purpose |
|--------|---------|
| DeliveryAttempt | Delivery attempt records |
| DeliveryProof | Proof of delivery captures |
| DeliveryBooking | Scheduled deliveries |
| DeliveryPreference | Customer delivery preferences |
| DeliveryRedirect | Address redirections |
| DeliveryTimeSlot | Available time slots |
| OptimizedRoute | Calculated optimal routes |
| CourierZone | Courier-zone assignments |
| CourierLocationHistory | GPS tracking history |
| CourierRating | Delivery ratings |
| LocationPing | Real-time GPS pings |
| TrackingSession | Active tracking sessions |

### Notification Entities
| Entity | Purpose |
|--------|---------|
| Notification | Core notification records |
| NotificationTemplate | Message templates |
| NotificationPreference | Per-user channel preferences |
| NotificationChannel | Available channels definition |
| NotificationDeliveryLog | Delivery tracking |
| NotificationLog | Notification history |
| NotificationType | Notification categories |
| LiveNotification | WebSocket notifications |

### Integration Entities
| Entity | Purpose |
|--------|---------|
| Carrier / CarrierRate / CarrierShipment | External carrier integration |
| CarrierSelectionRule / CarrierZoneMapping | Carrier routing rules |
| ECommerceConnection / ECommerceOrder | E-commerce platform connections |
| WebhookSubscription / WebhookEvent | Webhook management |
| PaymentWebhookLog / CarrierWebhookLog | Webhook processing logs |
| ThirdPartyPartner / PartnerHandoff | 3PL partner management |

### Workflow & Automation Entities
| Entity | Purpose |
|--------|---------|
| WorkflowDefinition / WorkflowTemplate | Workflow configuration |
| WorkflowExecution / WorkflowStep / WorkflowStepExecution | Workflow execution tracking |
| AutomationRule | Trigger-action rules |
| ScheduledTask | Cron-based scheduled tasks |
| AsyncJob | Background job tracking |
| DomainEvent / DeadLetterEvent | Event-driven architecture |
| OutboxMessage | Transactional outbox pattern |

### Multi-Tenancy Entities
| Entity | Purpose |
|--------|---------|
| Tenant | Tenant organizations |
| TenantUser / TenantInvitation | Tenant user management |
| TenantConfiguration / TenantBranding | Per-tenant customization |
| TenantQuota | Resource limits |
| TenantAuditLog | Tenant-specific audit |

### Gamification & Engagement Entities
| Entity | Purpose |
|--------|---------|
| Achievement / UserAchievement | Badge system |
| GamificationProfile | Progress tracking |
| LeaderboardEntry | Performance rankings |
| LoyaltyProgram / LoyaltyTransaction | Loyalty points |
| Campaign | Marketing campaigns |
| SatisfactionSurvey / ServiceFeedback | Customer feedback |

### Platform & Configuration Entities
| Entity | Purpose |
|--------|---------|
| SystemSetting | Global configuration |
| FeatureFlag / FeatureFlagAudit | Feature toggles |
| AppVersionConfig | Mobile app version control |
| MaintenanceWindow | Scheduled maintenance |
| SystemAlert | System alerts |
| SystemHealthCheck | Health check records |
| PlatformMetric | Platform usage metrics |
| CachePolicy | Cache configuration |
| RateLimitPolicy / RateLimitOverride / RateLimitViolation | Rate limiting |
| DataPipelineConfig / PipelineExecution | Data pipeline management |
| SearchIndex | Search optimization |

### Security & Compliance Entities
| Entity | Purpose |
|--------|---------|
| SecurityEvent | Security event log |
| IpBlacklist / FraudBlacklist | Threat management |
| ComplianceReport / ComplianceRule | Regulatory compliance |
| SlaPolicy | SLA definitions |
| Contract / ContractSlaTerms | Contract management |
| SystemAuditLog / DocumentAuditLog | Audit trails |
| ApiKey / ApiKeyUsageLog | API key management |
| DigitalSignature / SignatureRequest | E-signatures |

### Document Entities
| Entity | Purpose |
|--------|---------|
| GeneratedDocument | Generated documents |
| DocumentTemplate | Document templates |
| DocumentBatch | Batch document processing |
| CustomsDocument | Cross-border documents |
| CustomReport / ReportExecution | Custom report system |
| EInvoice | Electronic invoices |
| KnowledgeArticle | Help center articles |
| SavedFilter | User-saved search filters |

### Vehicle & Fleet Entities
| Entity | Purpose |
|--------|---------|
| Vehicle | Vehicle registry |
| VehicleAssignment | Courier-vehicle mapping |
| VehicleMaintenance | Maintenance logs |
| FuelLog | Fuel consumption tracking |

### Subscription Entities
| Entity | Purpose |
|--------|---------|
| SubscriptionPlan | Available plans |
| MerchantSubscription | Active subscriptions |

### Chat Entities
| Entity | Purpose |
|--------|---------|
| ChatRoom | Conversation rooms |
| ChatMessage | Messages |

### Device & Mobile Entities
| Entity | Purpose |
|--------|---------|
| DeviceRegistration | Mobile device registry |
| DeviceToken | Push notification tokens |
| OfflineQueue | Offline operation queue |
| SyncSession / SyncConflict | Data synchronization |
| BatteryOptimizationConfig | Mobile battery settings |
| TelemetrySettings | Telemetry configuration |
| DataUsageLog / UsageTracking | Usage monitoring |

### Archival & Maintenance Entities
| Entity | Purpose |
|--------|---------|
| ArchivedRecord / ArchivePolicy | Data archiving |
| CleanupTask | Data cleanup |
| DataExportJob | Export job management |

---

## 7. API Layer

### 7.1 Controller Overview

The backend exposes **88 REST controllers** organized by domain:

| Category | Controllers | Endpoints (Est.) |
|----------|-------------|-------------------|
| **Auth & Security** | Auth, ApiKey, IpManagement, SecurityEvent | 25+ |
| **Shipment & Delivery** | Shipment, Delivery, DeliveryExperience, PublicTracking, Manifest, Label | 40+ |
| **Zone & Assignment** | Dashboard (zones), SmartAssignment, Route, Demand | 20+ |
| **Warehouse** | WarehouseZone, Fulfillment | 15+ |
| **Financial** | Financial, PaymentIntent, PaymentRefund, Settlement, Invoice, EInvoice, Tax, Wallet, Subscription | 50+ |
| **Notification** | Notification, NotificationPreference, NotificationTemplate, LiveNotification, Sms | 30+ |
| **Carrier & E-Commerce** | Carrier, ECommerce, ECommerceWebhook, ThirdPartyPartner | 25+ |
| **Fleet** | Fleet | 10+ |
| **Workflow & Automation** | WorkflowDefinition, WorkflowExecution, AutomationRule, ScheduledTask, AsyncJob | 25+ |
| **Tenant** | Tenant, TenantBranding, TenantQuota, TenantUser | 20+ |
| **Reports & Analytics** | Analytics, Reports, ReportExport, BIDashboard, CustomReport | 30+ |
| **Support & Chat** | Support, Chat | 15+ |
| **Platform Ops** | PlatformConfig, PlatformOps, SystemHealth, Health, Backup, DeadLetter, Telemetry, MasterData | 30+ |
| **Gamification** | Gamification, Loyalty, Rating | 15+ |
| **Document** | Document, Signature | 15+ |
| **Data** | DataPipeline | 10+ |
| **User** | User, Settings, Compliance, Audit, Event, Country, Currency, Contract, ContractPricing, ContractSla, Pickup, Recipient, OfflineSync, DeviceMobile, WebhookCtrl, LiveTracking, CourierLocation | 60+ |

**Total estimated endpoints: 400+**

### 7.2 Standard API Response Format

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "errors": [],
  "timestamp": "2026-03-04T10:30:00Z"
}
```

Error response:
```json
{
  "success": false,
  "message": "Validation failed",
  "data": null,
  "errors": [
    { "field": "recipientPhone", "message": "Invalid phone number format" }
  ],
  "timestamp": "2026-03-04T10:30:00Z"
}
```

### 7.3 API Documentation

The API is documented via **SpringDoc OpenAPI 2.7.0** with 14 API groups:
- all, auth, shipments, public, dashboard, financial, reports, master, manifests, settings, sms, audit, backup, health

Accessible at:
- API Docs: `/api-docs`
- Swagger UI: `/swagger-ui.html`

### 7.4 Frontend API Client

The frontend uses a centralized API service (`api_service.js`) with ~195 methods covering all backend endpoints. Every API call includes:
- JWT token in Authorization header
- Standard error handling
- Response parsing
- Network failure recovery

---

## 8. Frontend Application

### 8.1 Architecture

```
frontend/
├── index.html                 # Landing page
├── login.html                 # Authentication
├── support.html               # Support tickets (all roles)
├── help.html                  # Help center (all roles)
├── wallet.html                # Wallet (all roles)
├── notifications.html         # Notifications (all roles)
├── profile.html / settings.html
├── tracking.html / contact.html / 404.html
├── admin/                     # 2 pages
├── courier/                   # 5 pages
├── merchant/                  # 10 pages
├── owner/                     # 14 pages (+ 3 sub-reports)
├── warehouse/                 # 6 pages
└── src/
    ├── js/
    │   ├── app.js             # Main application + routing
    │   ├── pages/             # 47 page handlers
    │   ├── services/          # 3 services (api, auth, websocket)
    │   └── shared/            # 8 shared modules
    └── css/                   # Stylesheets
```

### 8.2 Page Handler Pattern

Every page follows a consistent architecture:

1. **HTML Page**: RTL Arabic, Bootstrap 5.3.2, Font Awesome 6.4.0, role-specific sidebar
2. **JS Handler**: Extends `BasePageHandler`, implements `initializePage()` and `setupEventListeners()`
3. **Services**: Access via `this.services.api`, `this.services.notification`, `this.services.auth`
4. **Utilities**: `UIUtils.showLoading()`, `ErrorHandler.handle()`, `escapeHtml()`
5. **Initialization**: Global instance created, `init()` called on DOMContentLoaded after path check

### 8.3 Shared Modules

| Module | Purpose |
|--------|---------|
| BasePageHandler.js | Abstract base class for all page handlers — provides services, lifecycle methods |
| config.js | API base URL, environment detection |
| GlobalUIHandler.js | Sidebar toggle, dropdowns, responsive behavior |
| Logger.js | Structured logging replacement for console.* |
| NotificationBell.js | Real-time notification badge and dropdown |
| NotificationService.js | Toast notifications (success/error/warning/info) |
| sanitizer.js | Input sanitization utilities |
| SharedDataUtils.js | Shared data processing utilities |

### 8.4 Services

| Service | Purpose |
|---------|---------|
| api_service.js | ~195 API methods — centralized HTTP client for all backend calls |
| auth_service.js | JWT token management, login/logout, role detection, user session |
| websocket_service.js | WebSocket connection for real-time notifications and chat |

### 8.5 Script Loading Order

Every HTML page loads scripts in this exact order:
1. `config.js` — Configuration
2. `SharedDataUtils.js` — Data utilities
3. `GlobalUIHandler.js` — UI management
4. `NotificationService.js` — Toast notifications
5. `auth_service.js` — Authentication
6. `api_service.js` — API client
7. `websocket_service.js` — WebSocket
8. `NotificationBell.js` — Notification bell
9. `app.js` — Main application + routing
10. `[page-specific].js` — Page handler

### 8.6 Design System

| Aspect | Details |
|--------|---------|
| Direction | RTL (Right-to-Left) for Arabic |
| Font | Noto Sans Arabic + system fonts |
| CSS Framework | Bootstrap 5.3.2 + Custom styles |
| Icons | Font Awesome 6.4.0 |
| Responsive | Mobile-first with sidebar collapse |
| Theme | Custom Twsela design system |
| Charts | Chart.js for analytics |

---

## 9. Infrastructure & DevOps

### 9.1 Production Architecture

| Component | Technology | Configuration |
|-----------|-----------|---------------|
| Web Server | Nginx | SSL termination, reverse proxy, static files |
| App Server | Spring Boot (Tomcat) | Port 8000, 200 threads |
| Database | MySQL 9.4.0 | utf8mb4, InnoDB, 176 tables |
| Cache | Redis 6+ | Pool: 16 active, cache TTL 30min |
| Monitoring | Prometheus | Metrics scraping, alert rules |
| Dashboards | Grafana | 5 pre-configured dashboards |
| Container | Docker + Compose | Production-ready configuration |
| Backup | Automated scripts | Daily at 2 AM, 30-day retention |

### 9.2 Docker Configuration

The system includes multiple Docker Compose configurations:
- `docker-compose.yml` — Core services (app, db, redis, nginx)
- `docker-compose.monitoring.yml` — Prometheus + Grafana stack
- `docker-compose.backup.yml` — Automated backup service

### 9.3 Monitoring Stack

**Prometheus Metrics** collected:
- JVM: Memory, GC, threads
- HTTP: Request count, latency, error rates
- Database: Connection pool, query performance
- Cache: Hit/miss ratio, eviction rate
- Custom: Shipments created, deliveries completed

**Grafana Dashboards** (pre-configured):
1. System Overview — request rate, latency, errors, JVM
2. Business Metrics — shipment volume, delivery success
3. Courier Performance — deliveries per courier, timing
4. Database Performance — queries, connections, locks
5. Cache Performance — hit/miss, memory, evictions

**Alert Rules**:
- High error rate (>5% for 5 minutes)
- Database down
- High memory usage (>90%)
- High latency

### 9.4 Health Checks

```
GET /actuator/health          — Overall health
GET /actuator/health/liveness — Kubernetes liveness
GET /actuator/health/readiness — Kubernetes readiness
GET /actuator/info            — Application info
GET /actuator/metrics         — Metrics list
GET /actuator/prometheus      — Prometheus-format metrics
```

### 9.5 Logging

| Setting | Value |
|---------|-------|
| Root Level | WARN |
| Application Level | INFO |
| Log File | /var/log/twsela/twsela.log |
| Max Size | 100MB per file |
| Retention | 30 days |
| Format | JSON structured logging |

---

## 10. Security Architecture

### 10.1 Authentication Flow

```
User → Login Request → AuthController → UserService → BCrypt Verify
  ↓
JWT Token Generated (24h expiry)
  ↓
All API Requests include: Authorization: Bearer <token>
  ↓
JwtAuthenticationFilter → Validate Token → Set Security Context
  ↓
Method-Level @PreAuthorize → Role Check → Allow/Deny
```

### 10.2 Security Filters (Request Pipeline)

```
Request → RequestCorrelationFilter (trace ID)
       → TenantContextFilter (tenant detection)
       → RateLimitFilter (throttling)
       → IpBlacklistFilter (IP check)
       → InputSanitizationFilter (XSS prevention)
       → JwtAuthenticationFilter (auth)
       → ApiKeyAuthFilter (API key auth)
       → Controller
```

### 10.3 Security Features Summary

| Feature | Implementation |
|---------|---------------|
| Authentication | JWT (jjwt 0.12.6), 24h expiry |
| Password Hashing | BCrypt with configurable strength |
| Authorization | Spring Security RBAC, method-level |
| CSRF | Cookie-based tokens |
| Rate Limiting | Redis-backed Bucket4j |
| IP Blocking | Configurable blacklist |
| Input Sanitization | Custom filter on all requests |
| SQL Injection | JPA parameterized queries |
| XSS Prevention | CSP headers, output encoding |
| TLS/SSL | Nginx termination, HSTS enabled |
| Account Lockout | After N failed attempts |
| OTP | Phone verification with time limits |
| API Keys | For external integrations |
| Token Blacklist | Invalidate tokens on logout |
| Audit Trail | All security events logged |
| Correlation IDs | Cross-request tracing |

---

## 11. Integration Ecosystem

### 11.1 SMS — Twilio
- Automated notifications at each shipment stage
- Arabic/English message templates
- Retry logic (3 attempts, 30s timeout)
- Configurable via environment variables

### 11.2 Maps — Google Maps API
- Zone polygon creation and editing
- Distance calculation for pricing
- Route optimization for couriers
- Address geocoding and validation

### 11.3 Payment Gateways
| Gateway | Region |
|---------|--------|
| Stripe | Global |
| Paymob | Egypt |
| Fawry | Egypt |
| Tap | GCC (Saudi Arabia, UAE, Kuwait) |

### 11.4 E-Commerce Platforms
| Platform | Region |
|----------|--------|
| Shopify | Global |
| WooCommerce | Global |
| Salla | Saudi Arabia |
| Zid | Saudi Arabia |

### 11.5 Communication Channels
- SMS (Twilio)
- Email (SMTP — Gmail, etc.)
- Push Notifications (FCM/APNS)
- WhatsApp Business
- WebSocket (in-app real-time)

---

## 12. Development History

### Sprint History (Sprints 1–48)

| Sprint | Focus | Key Outcome |
|--------|-------|-------------|
| **1** | Security fixes | Closed 12+ security vulnerabilities, fixed 5 endpoints |
| **2** | Data sync & performance | 8→17 shipment states, EAGER→LAZY, dashboard optimization |
| **3** | JWT + Rate Limiting + Cache | Bucket4j, Redis cache, constructor injection, first 14 tests |
| **4** | Tests + code quality | 75 new tests, Logger.js, 219 console.* replacements |
| **5** | Infrastructure + API docs | Docker secured, SLF4J, @Tag on 14 controllers |
| **6** | Security hardening | Input validation, entity relationship corrections |
| **7** | Performance + endpoints | 15+ new endpoints (auth, courier, merchant, notifications) |
| **8** | API documentation | 32 @Operation annotations, 6 DTOs, SystemSettings |
| **9** | Frontend overhaul | UIUtils, ErrorHandler, 10 page scripts, ARIA, RTL audit |
| **10** | Tests + upgrades | 22 new tests, jjwt 0.12.6, springdoc 2.7.0, Redis OTP |
| **11–47** | Massive feature expansion | 174 entities, 88 controllers, 134 services, 176 tables, 938+ tests |
| **48** | Warehouse ops + support | 5 warehouse pages, support system, help center, V59 migration |

### Database Migration History (59 Migrations)

| Range | Theme |
|-------|-------|
| V1–V4 | User lockout, shipment statuses, notifications, indexes |
| V5–V9 | Returns, wallets, webhooks, subscriptions |
| V10–V16 | Fleet, support, assignments, countries, currencies, taxes |
| V17–V22 | Delivery proof, pickup scheduling, notifications, KPIs, contracts |
| V23–V28 | API keys, e-commerce, tracking, chat, payments, settlements |
| V29–V35 | Security events, compliance, domain events, async jobs, multi-tenancy |
| V36–V43 | Workflows, automation, warehouse zones, fulfillment, recipients, delivery experience, rate limiting, search/feature flags |
| V44–V51 | Gamification, loyalty, carriers, 3PL, documents, signatures, offline sync, mobile config |
| V52–V59 | Custom reports, data pipelines, platform ops, alerts, maintenance, seed data (sprints 46–48) |

---

## 13. Current System Stats

| Metric | Count |
|--------|-------|
| **Backend Entities** | 174 |
| **REST Controllers** | 88 |
| **Business Services** | 134+ |
| **Data Repositories** | 170 |
| **Database Tables** | 176 |
| **Flyway Migrations** | 59 (V1–V59) |
| **DTOs** | 70 |
| **Exception Classes** | 4 custom |
| **Validation Classes** | 2 |
| **Security Filters** | 7 |
| **Config Classes** | 9 |
| **HTML Pages** | 49 |
| **JS Page Handlers** | 47 |
| **JS Services** | 3 |
| **JS Shared Modules** | 8 |
| **API Methods (Frontend)** | ~195 |
| **Unit Tests** | 938+ |
| **Test Failures** | 0 |
| **Build Status** | BUILD SUCCESS |

### Pages by Role

| Role | HTML Pages | JS Handlers |
|------|-----------|-------------|
| Owner | 14 | 14 |
| Merchant | 10 | 10 |
| Warehouse | 6 | 6 |
| Courier | 5 | 5 |
| Admin | 2 | 2 |
| Shared (all roles) | 11 | 9 |
| Public (no auth) | 1 (tracking) | 1 |
| **Total** | **49** | **47** |

---

## 14. Roadmap

### Completed Phases

- **Phase 1 (MVP)**: Core shipment management, multi-role auth, zone pricing, basic analytics, manifest generation
- **Phase 2 (Integrations)**: Twilio SMS, Google Maps, payment gateways, webhook system, e-commerce connectors
- **Phase 3 (Enterprise)**: Multi-tenancy, workflow engine, gamification, carrier integration, warehouse operations, support system

### Sprints 49–57 (Planned)

| Sprint | Focus | Planned Pages |
|--------|-------|---------------|
| 49 | Admin Operations | Admin shipments, users, reports, settings |
| 50 | Owner Advanced | Owner couriers, vehicles, contracts, carriers |
| 51 | Merchant Advanced | Merchant analytics, tracking, COD, ratings |
| 52 | Courier Advanced | Courier earnings, history, vehicle, settings |
| 53 | Platform Admin | Feature flags, system health, maintenance, backups |
| 54 | Communication | Notification center, templates, SMS dashboard, chat |
| 55 | Financial Advanced | Settlements, invoices, tax management, subscriptions |
| 56 | Analytics & BI | BI dashboard, custom reports, KPI snapshots, demand |
| 57 | Integration Hub | E-commerce dashboard, carrier dashboard, webhooks, API keys |

### Future Phases

- **Phase 4 — Scale & Optimize (Q2 2026)**:
  - Mobile apps (iOS/Android)
  - AI-powered route optimization
  - Predictive analytics
  - Kubernetes deployment
  - Multi-region support

- **Phase 5 — Ecosystem (Q3–Q4 2026)**:
  - Public API marketplace
  - White-label solution
  - Mobile SDK for developers
  - Blockchain proof of delivery
  - IoT package tracking
  - Carbon footprint tracking

---

## Appendix A: File Structure Summary

```
TWSELA/
├── frontend/                          # Frontend application
│   ├── 11 root HTML pages             # Shared/public pages
│   ├── admin/ (2 pages)               # Admin interface
│   ├── courier/ (5 pages)             # Courier interface
│   ├── merchant/ (10 pages)           # Merchant portal
│   ├── owner/ (14 pages)              # Owner dashboard
│   ├── warehouse/ (6 pages)           # Warehouse operations
│   └── src/
│       ├── js/
│       │   ├── app.js                 # Main application
│       │   ├── pages/ (47 handlers)   # Page logic
│       │   ├── services/ (3 files)    # API, auth, WebSocket
│       │   └── shared/ (8 files)      # Base handler, utils
│       └── css/                       # Stylesheets
│
├── twsela/                            # Backend application
│   ├── pom.xml                        # Maven config
│   ├── Dockerfile                     # Docker build
│   ├── docker-compose.*.yml           # Docker orchestration
│   ├── nginx.conf                     # Reverse proxy config
│   └── src/main/
│       ├── java/com/twsela/
│       │   ├── domain/ (174 entities) # Data models
│       │   ├── repository/ (170)      # Data access
│       │   ├── service/ (134+)        # Business logic
│       │   ├── web/ (88 controllers)  # REST API
│       │   ├── config/                # App configuration
│       │   └── security/              # Security layer
│       └── resources/
│           ├── application.yml        # App config
│           └── db/migration/ (59)     # Flyway migrations
│
├── docs/                              # Documentation
├── tools/                             # Build/export tools
└── backup/                            # Backup configs
```

## Appendix B: Configuration Reference

| Environment Variable | Description | Required |
|---------------------|-------------|----------|
| `DB_HOST` | Database host | Yes |
| `DB_PORT` | Database port (default: 3306) | Yes |
| `DB_NAME` | Database name (default: twsela) | Yes |
| `DB_USERNAME` | Database user | Yes |
| `DB_PASSWORD` | Database password | Yes |
| `REDIS_HOST` | Redis host (default: localhost) | Yes |
| `REDIS_PORT` | Redis port (default: 6379) | Yes |
| `JWT_SECRET` | JWT signing key (256-bit) | Yes |
| `JWT_EXPIRATION` | Token expiry ms (default: 86400000) | No |
| `TWILIO_ACCOUNT_SID` | Twilio account SID | For SMS |
| `TWILIO_AUTH_TOKEN` | Twilio auth token | For SMS |
| `TWILIO_PHONE_NUMBER` | Twilio phone number | For SMS |
| `GOOGLE_MAPS_API_KEY` | Google Maps API key | For maps |
| `PAYMENT_GATEWAY_API_KEY` | Payment gateway key | For payments |
| `MAIL_HOST` | SMTP mail host | For email |
| `SSL_ENABLED` | Enable SSL | Production |

---

**End of Report**

*Twsela — Making MENA logistics smarter, faster, and Arabic-first.*
*Report generated: March 2026 | Sprint 48 Complete | BUILD SUCCESS*
