# Twsela Backend Codebase Map — Comprehensive Gap Analysis Report

**Generated:** 2026-03-03  
**Total Java Files:** 462  
**Base Package:** `com.twsela`  
**Framework:** Spring Boot with JPA/Hibernate, MySQL, Redis, WebSocket, Flyway migrations

---

## Table of Contents

1. [Package Structure](#1-package-structure)
2. [Entities (Domain Model)](#2-entities-domain-model)
3. [Enums](#3-enums)
4. [Repositories](#4-repositories)
5. [Services](#5-services)
6. [Controllers (API Endpoints)](#6-controllers-api-endpoints)
7. [Configuration Classes](#7-configuration-classes)
8. [Security Layer](#8-security-layer)
9. [DTOs (Data Transfer Objects)](#9-dtos)
10. [Exceptions & Validation](#10-exceptions--validation)
11. [Utility Classes](#11-utility-classes)
12. [Application Configuration (YAML)](#12-application-configuration)

---

## 1. Package Structure

```
com.twsela/
├── TwselaApplication.java          (@SpringBootApplication, @EnableScheduling, @EnableRetry)
├── config/                          (9 files)
│   ├── ApiKeyAuthFilter.java
│   ├── ApiVersionFilter.java
│   ├── CacheConfig.java
│   ├── DataInitializer.java
│   ├── JacksonConfig.java
│   ├── RequestTracingFilter.java
│   ├── SwaggerConfig.java
│   ├── WebSocketAuthInterceptor.java
│   └── WebSocketConfig.java
├── domain/                          (103 files — entities + enums)
├── repository/                      (96 files)
├── security/                        (15 files)
│   ├── ApplicationConfig.java
│   ├── AuthenticationHelper.java
│   ├── InputSanitizationFilter.java
│   ├── IpBlacklistFilter.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtService.java
│   ├── Permission.java
│   ├── PermissionService.java
│   ├── RateLimitFilter.java
│   ├── RequestCorrelationFilter.java
│   ├── SecurityConfig.java
│   ├── SecurityExceptionHandler.java
│   ├── TenantContextFilter.java
│   ├── TenantDataFilter.java
│   └── TokenBlacklistService.java
├── service/                         (114 files)
├── util/
│   └── AppUtils.java
└── web/                             (controllers + dto + exception + validation)
    ├── (56 controller files)
    ├── ErrorMessages.java
    ├── GlobalExceptionHandler.java
    ├── dto/                          (58 files)
    ├── exception/                    (4 files)
    └── validation/                   (2 files)
```

---

## 2. Entities (Domain Model)

### 2.1 Core User & Role Entities

#### `User` — Table: `users`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | @Id @GeneratedValue |
| role | Role | @ManyToOne |
| status | UserStatus | @ManyToOne |
| phone | String | |
| name | String | |
| password | String | |
| isDeleted | Boolean | |
| deletedAt | Instant | |
| createdAt | Instant | |
| updatedAt | Instant | |
| failedLoginAttempts | int | |
| lockedUntil | Instant | |
| merchantDetails | MerchantDetails | @OneToOne |
| courierDetails | CourierDetails | @OneToOne |
| createdShipments | Set\<Shipment\> | @OneToMany |
| payouts | Set\<Payout\> | @OneToMany |
| tenantId | Long | |

#### `Role` — Table: `roles`
| Field | Type |
|-------|------|
| id | Long |
| name | String |
| description | String |

#### `UserStatus` — Table: `user_statuses`
| Field | Type |
|-------|------|
| id | Long |
| name | String |

#### `MerchantDetails` — Table: `merchant_details`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| userId | Long | @Id |
| user | User | @OneToOne |
| businessName | String | |
| pickupAddress | String | |
| bankAccountDetails | String | |

#### `CourierDetails` — Table: `courier_details`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| userId | Long | @Id |
| user | User | @OneToOne |
| vehicleType | VehicleType | enum: BICYCLE, MOTORCYCLE, CAR, VAN, TRUCK |
| licensePlateNumber | String | |
| onboardingDate | LocalDate | |

### 2.2 Shipment & Delivery Entities

#### `Shipment` — Table: `shipments`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | @Id @GeneratedValue |
| trackingNumber | String | |
| merchant | User | @ManyToOne |
| manifest | ShipmentManifest | @ManyToOne |
| zone | Zone | @ManyToOne |
| status | ShipmentStatus | @ManyToOne |
| recipientDetails | RecipientDetails | @ManyToOne |
| deliveryLatitude | BigDecimal | |
| deliveryLongitude | BigDecimal | |
| itemValue | BigDecimal | |
| shippingFeePaidBy | ShippingFeePaidBy | enum: MERCHANT, RECIPIENT |
| codAmount | BigDecimal | |
| deliveryFee | BigDecimal | |
| sourceType | SourceType | enum |
| externalTrackingNumber | String | |
| cashReconciled | Boolean | |
| payout | Payout | @ManyToOne |
| createdAt | Instant | |
| updatedAt | Instant | |
| deliveredAt | Instant | |
| podType | PodType | enum |
| podData | String | |
| recipientNotes | String | |
| statusHistory | Set\<ShipmentStatusHistory\> | @OneToMany |
| packageDetails | ShipmentPackageDetails | @OneToOne |
| tenantId | Long | |

**Enums:**
- `PodType` — Proof of Delivery type
- `ShippingFeePaidBy` — MERCHANT, RECIPIENT
- `SourceType` — source of shipment creation

#### `ShipmentStatus` — Table: `shipment_statuses`
| Field | Type |
|-------|------|
| id | Long |
| name | String |
| description | String |

#### `ShipmentStatusConstants` — (not a table, constants class)

#### `ShipmentStatusHistory` — Table: `shipment_status_history`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| shipment | Shipment | @ManyToOne |
| status | ShipmentStatus | @ManyToOne |
| notes | String | |
| createdAt | Instant | |

#### `ShipmentPackageDetails` — Table: `shipment_package_details`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| shipmentId | Long | @Id |
| shipment | Shipment | @OneToOne |
| packageType | String | |
| weightKg | BigDecimal | |
| lengthCm | BigDecimal | |
| widthCm | BigDecimal | |
| heightCm | BigDecimal | |

#### `ShipmentManifest` — Table: `shipment_manifests`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| courier | User | @ManyToOne |
| manifestNumber | String | |
| status | ManifestStatus | enum |
| createdAt | Instant | |
| assignedAt | Instant | |
| shipments | Set\<Shipment\> | @OneToMany |

#### `RecipientDetails` — Table: `recipient_details`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| phone | String | |
| name | String | |
| address | String | |
| alternatePhone | String | |
| shipments | Set\<Shipment\> | @OneToMany |

#### `DeliveryAttempt` — Table: `delivery_attempts`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| shipment | Shipment | @ManyToOne |
| attemptNumber | int | |
| status | AttemptStatus | enum |
| failureReason | FailureReason | enum |
| photoUrl | String | |
| latitude | Double | |
| longitude | Double | |
| notes | String | |
| attemptedAt | Instant | |
| nextAttemptDate | LocalDate | |
| courier | User | @ManyToOne |
| createdAt | Instant | |

**Enums:** `AttemptStatus`, `FailureReason`

#### `DeliveryProof` — Table: `delivery_proofs`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| shipment | Shipment | @OneToOne |
| photoUrl | String | |
| signatureUrl | String | |
| latitude | Double | |
| longitude | Double | |
| recipientName | String | |
| notes | String | |
| deliveredAt | Instant | |
| capturedBy | User | @ManyToOne |
| createdAt | Instant | |

#### `DeliveryPricing` — Table: `delivery_pricing`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| merchant | User | @ManyToOne |
| zone | Zone | @ManyToOne |
| deliveryFee | BigDecimal | |
| isActive | Boolean | |
| createdAt | Instant | |
| updatedAt | Instant | |

#### `ReturnShipment` — Table: `return_shipments`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| originalShipment | Shipment | @ManyToOne |
| returnShipment | Shipment | @ManyToOne |
| reason | String | |
| status | ReturnStatusEnum | enum |
| assignedCourier | User | @ManyToOne |
| returnFee | BigDecimal | |
| notes | String | |
| approvedAt | Instant | |
| pickedUpAt | Instant | |
| deliveredAt | Instant | |
| createdAt | Instant | |
| createdBy | String | |

**Enums:** `ReturnStatusEnum`

#### `PickupSchedule` — Table: `pickup_schedules`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| merchant | User | @ManyToOne |
| pickupDate | LocalDate | |
| timeSlot | TimeSlot | enum |
| address | String | |
| latitude | Double | |
| longitude | Double | |
| estimatedShipments | int | |
| notes | String | |
| assignedCourier | User | @ManyToOne |
| status | PickupStatus | enum |
| completedAt | Instant | |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `TimeSlot`, `PickupStatus`

### 2.3 Zone & Location Entities

#### `Zone` — Table: `zones`
| Field | Type |
|-------|------|
| id | Long |
| name | String |
| description | String |
| centerLatitude | BigDecimal |
| centerLongitude | BigDecimal |
| defaultFee | BigDecimal |
| status | ZoneStatus |
| createdAt | LocalDateTime |
| tenantId | Long |

#### `ZoneStatus` — (Enum, not a table)
Values: Enum defining zone statuses

#### `CourierZone` — Table: `courier_zones`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| courierId | Long | composite key |
| zoneId | Long | composite key |
| id | Id | @EmbeddedId |
| courier | User | @ManyToOne |
| zone | Zone | @ManyToOne |

#### `CourierLocationHistory` — Table: `courier_location_history`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| courier | User | @ManyToOne |
| latitude | BigDecimal | |
| longitude | BigDecimal | |
| timestamp | Instant | |

#### `LocationPing` — Table: `location_pings`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| trackingSession | TrackingSession | @ManyToOne |
| lat | Double | |
| lng | Double | |
| accuracy | Float | |
| speed | Float | |
| heading | Float | |
| batteryLevel | Integer | |
| timestamp | Instant | |

#### `TrackingSession` — Table: `tracking_sessions`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| shipment | Shipment | @ManyToOne |
| courier | User | @ManyToOne |
| status | SessionStatus | enum |
| startedAt | Instant | |
| lastPingAt | Instant | |
| endedAt | Instant | |
| estimatedArrival | Instant | |
| currentLat | Double | |
| currentLng | Double | |
| totalDistanceKm | Double | |
| totalPings | Integer | |
| createdAt | Instant | |

**Enums:** `SessionStatus`

#### `OptimizedRoute` — Table: `optimized_routes`
| Field | Type |
|-------|------|
| id | Long |
| courierId | Long |
| manifestId | Long |
| waypoints | String |
| totalDistanceKm | double |
| estimatedDurationMinutes | int |
| optimizedAt | Instant |

### 2.4 Financial Entities

#### `Wallet` — Table: `wallets`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| user | User | @OneToOne |
| walletType | WalletType | enum: MERCHANT, COURIER, COMPANY |
| balance | BigDecimal | |
| currency | String | default "EGP" |
| createdAt | Instant | |
| updatedAt | Instant | |
| tenantId | Long | |

#### `WalletTransaction` — Table: `wallet_transactions`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| wallet | Wallet | @ManyToOne |
| type | TransactionType | enum: CREDIT, DEBIT |
| amount | BigDecimal | |
| reason | TransactionReason | enum: COD_COLLECTED, DELIVERY_FEE, COMMISSION, WITHDRAWAL, SETTLEMENT, RETURN_FEE, ADJUSTMENT |
| referenceId | Long | |
| balanceBefore | BigDecimal | |
| balanceAfter | BigDecimal | |
| description | String | |
| createdAt | Instant | |

#### `Payout` — Table: `payouts`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| user | User | @ManyToOne |
| payoutType | PayoutType | enum |
| status | PayoutStatus | @ManyToOne |
| payoutPeriodStart | LocalDate | |
| payoutPeriodEnd | LocalDate | |
| netAmount | BigDecimal | |
| paidAt | Instant | |
| description | String | |
| notes | String | |
| createdAt | Instant | |
| payoutItems | Set\<PayoutItem\> | @OneToMany |
| shipments | Set\<Shipment\> | @OneToMany |

**Enums:** `PayoutType`

#### `PayoutItem` — Table: `payout_items`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| payout | Payout | @ManyToOne |
| sourceType | SourceType | enum |
| sourceId | Long | |
| amount | BigDecimal | |
| description | String | |
| createdAt | Instant | |

#### `PayoutStatus` — Table: `payout_statuses`
| Field | Type |
|-------|------|
| id | Long |
| name | String |
| description | String |

#### `CashMovementLedger` — Table: `cash_movement_ledger`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| user | User | @ManyToOne |
| transactionType | TransactionType | enum |
| amount | BigDecimal | |
| shipmentId | Long | |
| description | String | |
| status | TransactionStatus | enum |
| createdAt | Instant | |
| reconciledAt | Instant | |
| immutable | Boolean | |

**Enums:** `TransactionType`, `TransactionStatus`

### 2.5 Payment Entities

#### `PaymentIntent` — Table: `payment_intents`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| shipment | Shipment | @ManyToOne |
| amount | BigDecimal | |
| currency | String | |
| status | IntentStatus | enum |
| provider | PaymentGatewayType | |
| providerRef | String | |
| paymentMethod | PaymentMethod | @ManyToOne |
| metadata | String | |
| attempts | Integer | |
| expiresAt | Instant | |
| confirmedAt | Instant | |
| failedAt | Instant | |
| failureReason | String | |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `IntentStatus`

#### `PaymentMethod` — Table: `payment_methods`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| user | User | @ManyToOne |
| type | PaymentType | enum |
| provider | PaymentGatewayType | |
| last4 | String | |
| brand | String | |
| isDefault | boolean | |
| active | boolean | |
| metadata | String | |
| tokenizedRef | String | |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `PaymentType`

#### `PaymentTransaction` — Table: `payment_transactions`
| Field | Type |
|-------|------|
| id | Long |
| externalId | String |
| gateway | PaymentGatewayType |
| type | PaymentType |
| amount | BigDecimal |
| currency | String |
| status | PaymentStatus |
| merchantId | Long |
| invoiceId | Long |
| metadata | String |
| errorMessage | String |
| createdAt | Instant |
| updatedAt | Instant |

**Enums:** `PaymentGatewayType`, `PaymentType`, `PaymentStatus`

#### `PaymentRefund` — Table: `payment_refunds`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| paymentIntent | PaymentIntent | @ManyToOne |
| amount | BigDecimal | |
| reason | String | |
| status | RefundStatus | enum |
| providerRef | String | |
| approvedBy | User | @ManyToOne |
| approvedAt | Instant | |
| processedAt | Instant | |
| rejectedReason | String | |
| createdAt | Instant | |

**Enums:** `RefundStatus`

#### `PaymentWebhookLog` — Table: `payment_webhook_logs`
| Field | Type |
|-------|------|
| id | Long |
| provider | String |
| eventType | String |
| payload | String |
| signature | String |
| verified | boolean |
| processed | boolean |
| processedAt | Instant |
| error | String |
| createdAt | Instant |

#### `SettlementBatch` — Table: `settlement_batches`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| settlementNumber | String | |
| period | SettlementPeriod | enum |
| startDate | LocalDate | |
| endDate | LocalDate | |
| totalTransactions | Integer | |
| totalAmount | BigDecimal | |
| totalFees | BigDecimal | |
| netAmount | BigDecimal | |
| status | BatchStatus | enum |
| generatedBy | User | @ManyToOne |
| processedAt | Instant | |
| notes | String | |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `SettlementPeriod`, `BatchStatus`

#### `SettlementItem` — Table: `settlement_items`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| batch | SettlementBatch | @ManyToOne |
| paymentIntent | PaymentIntent | @ManyToOne |
| shipment | Shipment | @ManyToOne |
| merchant | User | @ManyToOne |
| amount | BigDecimal | |
| fee | BigDecimal | |
| netAmount | BigDecimal | |
| type | ItemType | enum |
| description | String | |
| createdAt | Instant | |

**Enums:** `ItemType`

### 2.6 Subscription & Invoice Entities

#### `SubscriptionPlan` — Table: `subscription_plans`
| Field | Type |
|-------|------|
| id | Long |
| name | PlanName | enum |
| displayNameAr | String |
| monthlyPrice | BigDecimal |
| annualPrice | BigDecimal |
| maxShipmentsPerMonth | int |
| maxWebhooks | int |
| features | String |
| apiRateLimit | int |
| active | boolean |
| sortOrder | int |
| createdAt | Instant |

**Enums:** `PlanName`

#### `MerchantSubscription` — Table: `merchant_subscriptions`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| merchant | User | @ManyToOne |
| plan | SubscriptionPlan | @ManyToOne |
| status | SubscriptionStatus | enum |
| billingCycle | BillingCycle | enum |
| currentPeriodStart | Instant | |
| currentPeriodEnd | Instant | |
| trialEndsAt | Instant | |
| cancelledAt | Instant | |
| autoRenew | boolean | |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `SubscriptionStatus`, `BillingCycle`

#### `Invoice` — Table: `invoices`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| invoiceNumber | String | |
| subscription | MerchantSubscription | @ManyToOne |
| amount | BigDecimal | |
| tax | BigDecimal | |
| totalAmount | BigDecimal | |
| status | InvoiceStatus | enum |
| dueDate | Instant | |
| paidAt | Instant | |
| paymentGateway | String | |
| paymentTransactionId | String | |
| items | List\<InvoiceItem\> | @OneToMany |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `InvoiceStatus`

#### `InvoiceItem` — Table: `invoice_items`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| invoice | Invoice | @ManyToOne |
| description | String | |
| quantity | int | |
| unitPrice | BigDecimal | |
| totalPrice | BigDecimal | |

#### `EInvoice` — Table: `e_invoices`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| invoice | Invoice | @OneToOne |
| countryCode | String | |
| format | EInvoiceFormat | enum |
| serialNumber | String | |
| signedPayload | String | |
| qrCode | String | |
| submissionId | String | |
| status | EInvoiceStatus | enum |
| submittedAt | Instant | |
| responseData | String | |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `EInvoiceFormat`, `EInvoiceStatus`

### 2.7 Contract Entities

#### `Contract` — Table: `contracts`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| contractNumber | String | |
| contractType | ContractType | enum |
| party | User | @ManyToOne |
| startDate | LocalDate | |
| endDate | LocalDate | |
| status | ContractStatus | enum |
| autoRenew | boolean | |
| renewalNoticeDays | int | |
| signedAt | Instant | |
| signatureOtp | String | |
| termsDocument | String | |
| notes | String | |
| createdBy | User | @ManyToOne |
| createdAt | Instant | |
| updatedAt | Instant | |
| tenantId | Long | |

**Enums:** `ContractType`, `ContractStatus`

#### `ContractSlaTerms` — Table: `contract_sla_terms`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| contract | Contract | @OneToOne |
| targetDeliveryRate | double | |
| maxDeliveryHours | int | |
| latePenaltyPerShipment | BigDecimal | |
| lostPenaltyFixed | BigDecimal | |
| slaReviewPeriod | SlaReviewPeriod | enum |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `SlaReviewPeriod`

#### `CustomPricingRule` — Table: `custom_pricing_rules`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| contract | Contract | @ManyToOne |
| zoneFrom | Zone | @ManyToOne |
| zoneTo | Zone | @ManyToOne |
| shipmentType | String | |
| basePrice | BigDecimal | |
| perKgPrice | BigDecimal | |
| codFeePercent | BigDecimal | |
| minimumCharge | BigDecimal | |
| discountPercent | BigDecimal | |
| minMonthlyShipments | int | |
| active | boolean | |
| createdAt | Instant | |
| updatedAt | Instant | |

### 2.8 Notification Entities

#### `Notification` — Table: `notifications`
| Field | Type |
|-------|------|
| id | Long |
| userId | Long |
| type | NotificationType |
| channel | NotificationChannel |
| title | String |
| message | String |
| actionUrl | String |
| read | boolean |
| createdAt | Instant |
| readAt | Instant |

#### `NotificationType` — (Enum, not a table)
#### `NotificationChannel` — (Enum, not a table)

#### `NotificationTemplate` — Table: `notification_templates`
| Field | Type |
|-------|------|
| id | Long |
| eventType | NotificationType |
| channel | NotificationChannel |
| subjectTemplate | String |
| bodyTemplateAr | String |
| bodyTemplateEn | String |
| active | boolean |
| createdAt | Instant |
| updatedAt | Instant |

#### `NotificationPreference` — Table: `notification_preferences`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| user | User | @ManyToOne |
| enabledChannelsJson | String | |
| quietHoursStart | LocalTime | |
| quietHoursEnd | LocalTime | |
| digestMode | DigestMode | enum |
| pausedUntil | Instant | |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `DigestMode`

#### `NotificationLog` — Table: `notification_log`
| Field | Type |
|-------|------|
| id | Long |
| recipientPhone | String |
| messageType | String |
| messageContent | String |
| sentAt | Instant |
| status | String |
| errorMessage | String |

#### `NotificationDeliveryLog` — Table: `notification_delivery_log`
| Field | Type |
|-------|------|
| id | Long |
| notificationId | Long |
| channel | NotificationChannel |
| recipient | String |
| status | DeliveryStatus |
| externalId | String |
| errorMessage | String |
| sentAt | Instant |
| deliveredAt | Instant |
| retryCount | int |
| nextRetryAt | Instant |
| createdAt | Instant |

**Enums:** `DeliveryStatus`

#### `LiveNotification` — Table: `live_notifications`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| user | User | @ManyToOne |
| type | String | |
| title | String | |
| body | String | |
| payload | String | |
| read | boolean | |
| deliveredAt | Instant | |
| readAt | Instant | |
| createdAt | Instant | |

#### `DeviceToken` — Table: `device_tokens`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| user | User | @ManyToOne |
| token | String | |
| platform | Platform | enum |
| active | boolean | |
| createdAt | Instant | |
| lastUsedAt | Instant | |

**Enums:** `Platform`

### 2.9 Chat Entities

#### `ChatRoom` — Table: `chat_rooms`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| shipment | Shipment | @ManyToOne |
| participants | String | |
| roomType | RoomType | enum |
| status | RoomStatus | enum |
| createdAt | Instant | |

**Enums:** `RoomType`, `RoomStatus`

#### `ChatMessage` — Table: `chat_messages`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| chatRoom | ChatRoom | @ManyToOne |
| sender | User | @ManyToOne |
| messageType | MessageType | enum |
| content | String | |
| readBy | String | |
| sentAt | Instant | |

**Enums:** `MessageType`

### 2.10 Warehouse Entities

#### `Warehouse` — Table: `warehouses`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| name | String | |
| address | String | |
| latitude | BigDecimal | |
| longitude | BigDecimal | |
| inventory | Set\<WarehouseInventory\> | @OneToMany |

#### `WarehouseInventory` — Table: `warehouse_inventory`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| shipmentId | Long | @Id |
| shipment | Shipment | @OneToOne |
| warehouse | Warehouse | @ManyToOne |
| receivedBy | User | @ManyToOne |
| receivedAt | Instant | |
| dispatchedAt | Instant | |
| status | InventoryStatus | enum |

**Enums:** `InventoryStatus`

### 2.11 Fleet / Vehicle Entities

#### `Vehicle` — Table: `vehicles`
| Field | Type |
|-------|------|
| id | Long |
| plateNumber | String |
| vehicleType | VehicleType | enum |
| make | String |
| model | String |
| modelYear | Integer |
| color | String |
| status | VehicleStatus | enum |
| currentMileage | Integer |
| insuranceExpiry | LocalDate |
| licenseExpiry | LocalDate |
| createdAt | Instant |
| updatedAt | Instant |

**Enums:** `VehicleType`, `VehicleStatus`

#### `VehicleAssignment` — Table: `vehicle_assignments`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| vehicle | Vehicle | @ManyToOne |
| courier | User | @ManyToOne |
| status | AssignmentStatus | enum |
| assignedDate | LocalDate | |
| returnedDate | LocalDate | |
| startMileage | Integer | |
| endMileage | Integer | |
| notes | String | |
| createdAt | Instant | |

**Enums:** `AssignmentStatus`

#### `VehicleMaintenance` — Table: `vehicle_maintenance`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| vehicle | Vehicle | @ManyToOne |
| maintenanceType | MaintenanceType | enum |
| status | MaintenanceStatus | enum |
| description | String | |
| scheduledDate | LocalDate | |
| completedDate | LocalDate | |
| cost | BigDecimal | |
| serviceProvider | String | |
| mileageAtService | Integer | |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `MaintenanceType` (OIL_CHANGE, TIRE_ROTATION, BRAKE_SERVICE, ENGINE_SERVICE, GENERAL_INSPECTION, OTHER), `MaintenanceStatus` (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)

#### `FuelLog` — Table: `fuel_logs`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| vehicle | Vehicle | @ManyToOne |
| courier | User | @ManyToOne |
| fuelDate | LocalDate | |
| liters | BigDecimal | |
| costPerLiter | BigDecimal | |
| totalCost | BigDecimal | |
| mileageAtFill | Integer | |
| fuelStation | String | |
| createdAt | Instant | |

### 2.12 Security Entities

#### `SecurityEvent` — Table: `security_events`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| user | User | @ManyToOne |
| eventType | EventType | enum |
| ipAddress | String | |
| userAgent | String | |
| details | String | |
| severity | Severity | enum |
| createdAt | Instant | |

**Enums:** `EventType`, `Severity`

#### `AccountLockout` — Table: `account_lockouts`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| user | User | @ManyToOne |
| failedAttempts | int | |
| lockoutStart | Instant | |
| lockoutEnd | Instant | |
| lockoutReason | String | |
| autoUnlockAt | Instant | |
| unlockedBy | User | @ManyToOne |
| unlockedAt | Instant | |
| createdAt | Instant | |
| updatedAt | Instant | |

#### `IpBlacklist` — Table: `ip_blacklist`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| ipAddress | String | |
| reason | String | |
| blockedBy | User | @ManyToOne |
| blockedAt | Instant | |
| expiresAt | Instant | |
| permanent | boolean | |
| createdAt | Instant | |

#### `FraudBlacklist` — Table: `fraud_blacklist`
| Field | Type |
|-------|------|
| id | Long |
| entityType | String |
| entityValue | String |
| reason | String |
| createdAt | Instant |
| createdBy | String |
| isActive | Boolean |

### 2.13 Compliance Entities

#### `ComplianceRule` — Table: `compliance_rules`
| Field | Type |
|-------|------|
| id | Long |
| name | String |
| category | Category | enum |
| description | String |
| severity | SecurityEvent.Severity |
| checkExpression | String |
| enabled | boolean |
| lastCheckedAt | Instant |
| lastResult | CheckResult | enum |
| createdAt | Instant |
| updatedAt | Instant |

**Enums:** `Category`, `CheckResult`

#### `ComplianceReport` — Table: `compliance_reports`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| generatedBy | User | @ManyToOne |
| reportDate | LocalDate | |
| totalRules | int | |
| passedRules | int | |
| failedRules | int | |
| warningRules | int | |
| details | String | |
| status | ReportStatus | enum |
| createdAt | Instant | |

**Enums:** `ReportStatus`

### 2.14 Eventing & Async Entities

#### `DomainEvent` — Table: `domain_events`
| Field | Type |
|-------|------|
| id | Long |
| eventId | String |
| eventType | String |
| aggregateType | String |
| aggregateId | Long |
| payload | String |
| metadata | String |
| version | int |
| status | EventStatus | enum |
| publishedAt | Instant |
| processedAt | Instant |
| createdAt | Instant |

**Enums:** `EventStatus`

#### `EventSubscription` — Table: `event_subscriptions`
| Field | Type |
|-------|------|
| id | Long |
| subscriberName | String |
| eventType | String |
| handlerClass | String |
| filterExpression | String |
| active | boolean |
| retryPolicy | String |
| lastProcessedAt | Instant |
| failureCount | int |
| createdAt | Instant |
| updatedAt | Instant |

#### `DeadLetterEvent` — Table: `dead_letter_events`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| originalEvent | DomainEvent | @ManyToOne |
| failureReason | String | |
| failureCount | int | |
| lastAttemptAt | Instant | |
| nextRetryAt | Instant | |
| resolved | boolean | |
| resolvedBy | User | @ManyToOne |
| resolvedAt | Instant | |
| createdAt | Instant | |

#### `AsyncJob` — Table: `async_jobs`
| Field | Type |
|-------|------|
| id | Long |
| jobId | String |
| jobType | String |
| payload | String |
| status | JobStatus | enum |
| priority | int |
| scheduledAt | Instant |
| startedAt | Instant |
| completedAt | Instant |
| result | String |
| retryCount | int |
| maxRetries | int |
| errorMessage | String |
| createdAt | Instant |
| updatedAt | Instant |

**Enums:** `JobStatus`

#### `OutboxMessage` — Table: `outbox_messages`
| Field | Type |
|-------|------|
| id | Long |
| aggregateType | String |
| aggregateId | Long |
| eventType | String |
| payload | String |
| published | boolean |
| publishedAt | Instant |
| createdAt | Instant |

### 2.15 Tenant / Multi-Tenancy Entities

#### `Tenant` — Table: `tenants`
| Field | Type |
|-------|------|
| id | Long |
| tenantId | String |
| name | String |
| slug | String |
| domain | String |
| status | TenantStatus | enum |
| plan | TenantPlan | enum |
| contactName | String |
| contactPhone | String |
| contactEmail | String |
| settings | String |
| createdAt | Instant |
| updatedAt | Instant |

**Enums:** `TenantStatus`, `TenantPlan`

#### `TenantUser` — Table: `tenant_users`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| user | User | @ManyToOne |
| tenant | Tenant | @ManyToOne |
| role | TenantRole | enum |
| active | boolean | |
| joinedAt | Instant | |
| createdAt | Instant | |

**Enums:** `TenantRole`

#### `TenantBranding` — Table: `tenant_branding`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| tenant | Tenant | @OneToOne |
| logoUrl | String | |
| faviconUrl | String | |
| primaryColor | String | |
| secondaryColor | String | |
| accentColor | String | |
| fontFamily | String | |
| companyNameAr | String | |
| companyNameEn | String | |
| taglineAr | String | |
| taglineEn | String | |
| footerText | String | |
| customCSS | String | |
| emailTemplate | String | |
| createdAt | Instant | |
| updatedAt | Instant | |

#### `TenantConfiguration` — Table: `tenant_configurations`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| tenant | Tenant | @ManyToOne |
| configKey | String | |
| configValue | String | |
| category | ConfigCategory | enum |
| encrypted | boolean | |
| description | String | |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `ConfigCategory`

#### `TenantQuota` — Table: `tenant_quotas`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| tenant | Tenant | @ManyToOne |
| quotaType | QuotaType | enum |
| maxValue | long | |
| currentValue | long | |
| resetPeriod | ResetPeriod | enum |
| lastResetAt | Instant | |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `QuotaType`, `ResetPeriod`

#### `TenantInvitation` — Table: `tenant_invitations`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| tenant | Tenant | @ManyToOne |
| email | String | |
| phone | String | |
| role | TenantRole | |
| invitedBy | User | @ManyToOne |
| token | String | |
| status | InvitationStatus | enum |
| expiresAt | Instant | |
| createdAt | Instant | |

**Enums:** `InvitationStatus`

#### `TenantAuditLog` — Table: `tenant_audit_logs`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| tenant | Tenant | @ManyToOne |
| user | User | @ManyToOne |
| action | String | |
| entityType | String | |
| entityId | Long | |
| oldValues | String | |
| newValues | String | |
| ipAddress | String | |
| createdAt | Instant | |

### 2.16 E-Commerce Integration Entities

#### `ECommerceConnection` — Table: `ecommerce_connections`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| merchant | User | @ManyToOne |
| platform | ECommercePlatform | enum |
| storeName | String | |
| storeUrl | String | |
| accessToken | String | |
| webhookSecret | String | |
| active | boolean | |
| autoCreateShipments | boolean | |
| defaultZone | Zone | @ManyToOne |
| lastSyncAt | Instant | |
| syncErrors | int | |
| createdAt | Instant | |

**Enums:** `ECommercePlatform`

#### `ECommerceOrder` — Table: `ecommerce_orders`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| connection | ECommerceConnection | @ManyToOne |
| externalOrderId | String | |
| externalOrderNumber | String | |
| shipment | Shipment | @ManyToOne |
| platform | ECommercePlatform | |
| status | OrderStatus | enum |
| rawPayload | String | |
| errorMessage | String | |
| receivedAt | Instant | |
| processedAt | Instant | |

**Enums:** `OrderStatus`

### 2.17 Webhook Entities

#### `WebhookSubscription` — Table: `webhook_subscriptions`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| merchant | User | @ManyToOne |
| url | String | |
| secret | String | |
| events | String | |
| active | boolean | |
| createdAt | Instant | |
| updatedAt | Instant | |

#### `WebhookEvent` — Table: `webhook_events`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| subscription | WebhookSubscription | @ManyToOne |
| eventType | String | |
| payload | String | |
| status | DeliveryStatus | enum |
| attempts | int | |
| lastAttemptAt | Instant | |
| responseCode | Integer | |
| responseBody | String | |
| createdAt | Instant | |

**Enums:** `DeliveryStatus`

### 2.18 Ratings & Feedback Entities

#### `CourierRating` — Table: `courier_ratings`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| courier | User | @ManyToOne |
| shipment | Shipment | @ManyToOne |
| rating | Integer | |
| comment | String | |
| ratedByPhone | String | |
| createdAt | Instant | |

#### `ServiceFeedback` — Table: `service_feedback`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| shipment | Shipment | @OneToOne |
| rating | short | |
| comment | String | |
| createdAt | Instant | |

#### `MerchantServiceFeedback` — Table: `merchant_service_feedback`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| merchant | User | @ManyToOne |
| courier | User | @ManyToOne |
| relatedShipment | Shipment | @ManyToOne |
| rating | Integer | |
| comment | String | |
| createdAt | Instant | |

### 2.19 API Key Entities

#### `ApiKey` — Table: `api_keys`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| merchant | User | @ManyToOne |
| keyValue | String | |
| secretHash | String | |
| name | String | |
| scopes | String | |
| rateLimit | int | |
| active | boolean | |
| lastUsedAt | Instant | |
| requestCount | long | |
| expiresAt | Instant | |
| createdAt | Instant | |
| tenantId | Long | |

#### `ApiKeyUsageLog` — Table: `api_key_usage_log`
| Field | Type |
|-------|------|
| id | Long |
| apiKeyId | Long |
| endpoint | String |
| method | String |
| responseStatus | int |
| ipAddress | String |
| userAgent | String |
| requestedAt | Instant |

### 2.20 Support Entities

#### `SupportTicket` — Table: `support_tickets`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| ticketNumber | String | |
| subject | String | |
| description | String | |
| priority | TicketPriority | enum |
| status | TicketStatus | enum |
| category | TicketCategory | enum |
| reporter | User | @ManyToOne |
| assignee | User | @ManyToOne |
| shipmentId | Long | |
| firstResponseAt | Instant | |
| resolvedAt | Instant | |
| closedAt | Instant | |
| messages | List\<TicketMessage\> | @OneToMany |
| createdAt | Instant | |
| updatedAt | Instant | |

**Enums:** `TicketPriority`, `TicketStatus`, `TicketCategory`

#### `TicketMessage` — Table: `ticket_messages`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| ticket | SupportTicket | @ManyToOne |
| sender | User | @ManyToOne |
| content | String | |
| internal | boolean | |
| createdAt | Instant | |

#### `KnowledgeArticle` — Table: `knowledge_articles`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| title | String | |
| content | String | |
| category | TicketCategory | |
| published | boolean | |
| viewCount | int | |
| author | User | @ManyToOne |
| createdAt | Instant | |
| updatedAt | Instant | |

#### `SlaPolicy` — Table: `sla_policies`
| Field | Type |
|-------|------|
| id | Long |
| name | String |
| priority | TicketPriority |
| firstResponseHours | int |
| resolutionHours | int |
| active | boolean |
| createdAt | Instant |

### 2.21 Audit & System Entities

#### `SystemAuditLog` — Table: `system_audit_log`
| Field | Type |
|-------|------|
| id | Long |
| userId | Long |
| actionType | String |
| entityType | String |
| entityId | Long |
| oldValues | String |
| newValues | String |
| ipAddress | String |
| userAgent | String |
| createdAt | Instant |

#### `SystemSetting` — Table: `system_settings`
| Field | Type | Annotations/Notes |
|-------|------|-------------------|
| id | Long | |
| user | User | @ManyToOne |
| settingKey | String | |
| settingValue | String | |
| updatedAt | Instant | |

#### `TelemetrySettings` — Table: `telemetry_settings`
| Field | Type |
|-------|------|
| id | Long |
| settingKey | String |
| settingValue | String |
| description | String |
| createdAt | Instant |
| updatedAt | Instant |

#### `UsageTracking` — Table: `usage_tracking`
| Field | Type |
|-------|------|
| id | Long |
| merchantId | Long |
| period | String |
| shipmentsCreated | int |
| apiCalls | int |
| webhookEvents | int |
| lastUpdated | Instant |

#### `KPISnapshot` — Table: `kpi_snapshots`
| Field | Type |
|-------|------|
| id | Long |
| snapshotDate | LocalDate |
| totalRevenue | BigDecimal |
| totalShipments | int |
| deliveredShipments | int |
| returnedShipments | int |
| firstAttemptRate | double |
| avgDeliveryHours | double |
| activeCouriers | int |
| activeMerchants | int |
| newMerchants | int |
| slaComplianceRate | double |
| createdAt | Instant |

### 2.22 Internationalization / Country Entities

#### `Country` — Table: `countries`
| Field | Type |
|-------|------|
| id | Long |
| code | String |
| nameEn | String |
| nameAr | String |
| currencyCode | String |
| phonePrefix | String |
| addressFormat | String |
| timeZone | String |
| active | boolean |
| defaultPaymentGateway | String |
| createdAt | Instant |
| updatedAt | Instant |

#### `Currency` — Table: `currencies`
| Field | Type |
|-------|------|
| id | Long |
| code | String |
| nameEn | String |
| nameAr | String |
| symbol | String |
| decimalPlaces | int |
| active | boolean |
| createdAt | Instant |

#### `ExchangeRate` — Table: `exchange_rates`
| Field | Type |
|-------|------|
| id | Long |
| baseCurrency | String |
| targetCurrency | String |
| rate | BigDecimal |
| effectiveDate | LocalDate |
| source | RateSource | enum |
| createdAt | Instant |

**Enums:** `RateSource`

#### `TaxRule` — Table: `tax_rules`
| Field | Type |
|-------|------|
| id | Long |
| countryCode | String |
| taxType | TaxType | enum |
| name | String |
| nameAr | String |
| rate | BigDecimal |
| exemptCategories | String |
| validFrom | LocalDate |
| validTo | LocalDate |
| active | boolean |
| createdAt | Instant |
| updatedAt | Instant |

**Enums:** `TaxType`

### 2.23 Smart Assignment Entities

#### `AssignmentRule` — Table: `assignment_rules`
| Field | Type |
|-------|------|
| id | Long |
| ruleKey | String |
| ruleValue | String |
| description | String |
| active | boolean |
| createdAt | Instant |
| updatedAt | Instant |

#### `AssignmentScore` — Table: `assignment_scores`
| Field | Type |
|-------|------|
| id | Long |
| shipmentId | Long |
| courierId | Long |
| totalScore | double |
| distanceScore | double |
| loadScore | double |
| ratingScore | double |
| zoneScore | double |
| vehicleScore | double |
| historyScore | double |
| calculatedAt | Instant |

---

## 3. Enums

All enums are defined inside their parent entity classes:

| Entity | Enum Name | 
|--------|-----------|
| AsyncJob | `JobStatus` |
| CashMovementLedger | `TransactionType`, `TransactionStatus` |
| ChatMessage | `MessageType` |
| ChatRoom | `RoomType`, `RoomStatus` |
| ComplianceReport | `ReportStatus` |
| ComplianceRule | `Category`, `CheckResult` |
| Contract | `ContractType`, `ContractStatus` |
| ContractSlaTerms | `SlaReviewPeriod` |
| CourierDetails | `VehicleType` |
| DeliveryAttempt | `AttemptStatus`, `FailureReason` |
| DeviceToken | `Platform` |
| DomainEvent | `EventStatus` |
| ECommerceConnection | `ECommercePlatform` |
| ECommerceOrder | `OrderStatus` |
| EInvoice | `EInvoiceFormat`, `EInvoiceStatus` |
| ExchangeRate | `RateSource` |
| Invoice | `InvoiceStatus` |
| MerchantSubscription | `SubscriptionStatus`, `BillingCycle` |
| NotificationChannel | `NotificationChannel` |
| NotificationDeliveryLog | `DeliveryStatus` |
| NotificationPreference | `DigestMode` |
| NotificationType | `NotificationType` |
| PaymentIntent | `IntentStatus` |
| PaymentMethod | `PaymentType` |
| PaymentRefund | `RefundStatus` |
| PaymentTransaction | `PaymentGatewayType`, `PaymentType`, `PaymentStatus` |
| Payout | `PayoutType` |
| PayoutItem | `SourceType` |
| PickupSchedule | `TimeSlot`, `PickupStatus` |
| ReturnShipment | `ReturnStatusEnum` |
| SecurityEvent | `EventType`, `Severity` |
| SettlementBatch | `SettlementPeriod`, `BatchStatus` |
| SettlementItem | `ItemType` |
| Shipment | `PodType`, `ShippingFeePaidBy`, `SourceType` |
| ShipmentManifest | `ManifestStatus` |
| SubscriptionPlan | `PlanName` |
| SupportTicket | `TicketPriority`, `TicketStatus`, `TicketCategory` |
| TaxRule | `TaxType` |
| Tenant | `TenantStatus`, `TenantPlan` |
| TenantConfiguration | `ConfigCategory` |
| TenantInvitation | `InvitationStatus` |
| TenantQuota | `QuotaType`, `ResetPeriod` |
| TenantUser | `TenantRole` |
| TrackingSession | `SessionStatus` |
| Vehicle | `VehicleType`, `VehicleStatus` |
| VehicleAssignment | `AssignmentStatus` |
| VehicleMaintenance | `MaintenanceType`, `MaintenanceStatus` |
| Wallet | `WalletType` |
| WalletTransaction | `TransactionType`, `TransactionReason` |
| WarehouseInventory | `InventoryStatus` |
| WebhookEvent | `DeliveryStatus` |
| ZoneStatus | `ZoneStatus` |

**Standalone Enum (in security package):**
`Permission` — USER_VIEW, USER_CREATE, USER_UPDATE, USER_DELETE, SHIPMENT_VIEW, SHIPMENT_CREATE, SHIPMENT_UPDATE, SHIPMENT_DELETE, SHIPMENT_ASSIGN, SHIPMENT_STATUS_UPDATE, ZONE_VIEW, ZONE_CREATE, ZONE_UPDATE, ZONE_DELETE, ZONE_ASSIGN, DASHBOARD_VIEW, REPORTS_VIEW, ANALYTICS_VIEW, SYSTEM_CONFIG, SYSTEM_LOGS

---

## 4. Repositories

All repositories extend `JpaRepository<Entity, IdType>`.

| Repository | Entity | ID Type | Custom Methods |
|-----------|--------|---------|----------------|
| AccountLockoutRepository | AccountLockout | Long | findActiveByUserId, findActiveLockouts, findByUserIdOrderByCreatedAtDesc, findTopByUserIdOrderByCreatedAtDesc |
| ApiKeyRepository | ApiKey | Long | findByKeyValue, findByMerchantId, findByMerchantIdAndActiveTrue, findByActiveTrue |
| ApiKeyUsageLogRepository | ApiKeyUsageLog | Long | countByApiKeyIdAndRequestedAtBetween, findByApiKeyIdOrderByRequestedAtDesc |
| AssignmentRuleRepository | AssignmentRule | Long | findByRuleKey, findByActiveTrue |
| AssignmentScoreRepository | AssignmentScore | Long | findByShipmentIdOrderByTotalScoreDesc, findTopByShipmentIdOrderByTotalScoreDesc, findByShipmentIdAndCourierId, deleteByCalculatedAtBefore |
| AsyncJobRepository | AsyncJob | Long | findByStatusAndScheduledAtBefore, findByJobType, findByStatusIn, findRunningJobs, findByJobId, countByStatus |
| CashMovementLedgerRepository | CashMovementLedger | Long | findByUserIdOrderByCreatedAtDesc, findByTransactionTypeOrderByCreatedAtDesc, findByStatusOrderByCreatedAtDesc, findByUserIdAndTransactionType, findByCreatedAtBetween |
| ChatMessageRepository | ChatMessage | Long | findByChatRoomIdOrderBySentAtAsc, findByChatRoomIdOrderBySentAtDesc, countByChatRoomId |
| ChatRoomRepository | ChatRoom | Long | findByShipmentId, findByParticipantsContaining, findByShipmentIdAndRoomType, findByShipmentIdAndStatus |
| ComplianceReportRepository | ComplianceReport | Long | findLatest, findByReportDateBetween, findByStatus |
| ComplianceRuleRepository | ComplianceRule | Long | findByCategoryAndEnabledTrue, findByEnabledTrue, findByLastResult |
| ContractRepository | Contract | Long | findByPartyId, findByPartyIdAndStatus, findActiveByPartyId, findExpiringWithin, findByStatus, findByContractNumber, findAutoRenewableExpiring |
| ContractSlaTermsRepository | ContractSlaTerms | Long | findByContractId |
| CountryRepository | Country | Long | findByCode, findByActiveTrue, existsByCode |
| CourierDetailsRepository | CourierDetails | Long | findByUserId |
| CourierLocationHistoryRepository | CourierLocationHistory | Long | findByCourierIdOrderByTimestampDesc, findByCourierIdAndTimestampAfter, findByCourierIdAndTimestampBetween |
| CourierRatingRepository | CourierRating | Long | findByCourierIdOrderByCreatedAtDesc, findByShipmentId, countByCourierId |
| CourierZoneRepository | CourierZone | Id | findByCourierId, findByZoneId, findByCourierIdAndZoneId |
| CurrencyRepository | Currency | Long | findByCode, findByActiveTrue, existsByCode |
| CustomPricingRuleRepository | CustomPricingRule | Long | findByContractId, findByContractIdAndActiveTrue, findActiveByContractIdAndZones |
| DeadLetterEventRepository | DeadLetterEvent | Long | findUnresolved, findByOriginalEventEventType, countByResolved |
| DeliveryAttemptRepository | DeliveryAttempt | Long | findByShipmentIdOrderByAttemptNumberAsc, findByShipmentIdOrderByAttemptNumberDesc, countByShipmentId, findByCourierAndDateRange, countFailuresByReason |
| DeliveryPricingRepository | DeliveryPricing | Long | findByMerchantIdAndZoneId, findByMerchantId, findByZoneId, findByIsActiveTrue, findByMerchantIdAndIsActiveTrue, findByZoneIdAndIsActiveTrue, existsByMerchantIdAndZoneId |
| DeliveryProofRepository | DeliveryProof | Long | findByShipmentId, existsByShipmentId |
| DeviceTokenRepository | DeviceToken | Long | findByUserIdAndActiveTrue, findByToken, deleteByUserIdAndToken, findByUserId |
| DomainEventRepository | DomainEvent | Long | findByAggregateTypeAndAggregateIdOrderByVersionAsc, findByEventType, findByStatusOrderByCreatedAtAsc, findByStatusAndCreatedAtBefore, findByEventId, findMaxVersionByAggregate |
| ECommerceConnectionRepository | ECommerceConnection | Long | findByMerchantId, findByMerchantIdAndPlatform, findByActiveTrue, findByPlatformAndActiveTrue |
| ECommerceOrderRepository | ECommerceOrder | Long | findByExternalOrderIdAndPlatform, findByConnectionId, findByStatus, countByConnectionIdAndStatus |
| EInvoiceRepository | EInvoice | Long | findByInvoiceId, findByStatus, findByCountryAndSubmittedAtBetween |
| EventSubscriptionRepository | EventSubscription | Long | findByEventTypeAndActiveTrue, findBySubscriberName, findActiveSubscriptions |
| ExchangeRateRepository | ExchangeRate | Long | findByBaseCurrencyAndTargetCurrencyAndEffectiveDate, findLatestRate |
| FraudBlacklistRepository | FraudBlacklist | Long | findByEntityTypeAndEntityValue, findByEntityTypeAndIsActiveTrue, existsByEntityTypeAndEntityValueAndIsActiveTrue |
| FuelLogRepository | FuelLog | Long | findByVehicleIdOrderByFuelDateDesc, findByCourierIdOrderByFuelDateDesc |
| InvoiceRepository | Invoice | Long | findByInvoiceNumber, findBySubscriptionIdOrderByCreatedAtDesc, findBySubscriptionMerchantIdOrderByCreatedAtDesc, findByStatus, findOverdue, existsBySubscriptionIdAndStatus |
| IpBlacklistRepository | IpBlacklist | Long | findByIpAddress, findByPermanentTrue, findNonExpired, findByBlockedByIdOrderByBlockedAtDesc |
| KnowledgeArticleRepository | KnowledgeArticle | Long | findByPublishedTrueOrderByViewCountDesc, findByCategoryAndPublishedTrue, searchPublished |
| KPISnapshotRepository | KPISnapshot | Long | findBySnapshotDate, findBySnapshotDateBetweenOrderBySnapshotDateAsc, findLatest |
| LiveNotificationRepository | LiveNotification | Long | findByUserIdAndReadFalseOrderByCreatedAtDesc, countByUserIdAndReadFalse, findByUserIdOrderByCreatedAtDesc |
| LocationPingRepository | LocationPing | Long | findByTrackingSessionIdOrderByTimestampDesc, countByTrackingSessionId, findTop10ByTrackingSessionIdOrderByTimestampDesc |
| MerchantDetailsRepository | MerchantDetails | Long | *(no custom methods)* |
| MerchantServiceFeedbackRepository | MerchantServiceFeedback | Long | findByMerchantIdOrderByCreatedAtDesc, findByCourierIdOrderByCreatedAtDesc, findByRelatedShipmentIdAndCourierId |
| MerchantSubscriptionRepository | MerchantSubscription | Long | findByMerchantIdAndStatusIn, findByMerchantId, findByStatus, findExpired, findExpiredTrials, existsByMerchantIdAndStatusIn |
| NotificationDeliveryLogRepository | NotificationDeliveryLog | Long | findByNotificationId, findByStatusAndNextRetryAtBefore, countByChannelAndStatusAndSentAtBetween, findByRecipientAndSentAtBetween, getDeliveryStatsByChannelAndStatus |
| NotificationLogRepository | NotificationLog | Long | findByRecipientPhoneOrderBySentAtDesc, findByMessageTypeOrderBySentAtDesc, findByStatusOrderBySentAtDesc, findBySentAtBetween, countByRecipientPhoneAndMessageTypeSince |
| NotificationPreferenceRepository | NotificationPreference | Long | findByUserId, existsByUserId |
| NotificationRepository | Notification | Long | findByUserIdOrderByCreatedAtDesc, findByUserIdAndReadFalseOrderByCreatedAtDesc, countByUserIdAndReadFalse, markAllAsReadByUserId, findByUserIdAndTypeOrderByCreatedAtDesc |
| NotificationTemplateRepository | NotificationTemplate | Long | findByEventTypeAndChannel, findByEventType, findByActiveTrue, findByEventTypeAndActiveTrue |
| OptimizedRouteRepository | OptimizedRoute | Long | findByCourierIdAndOptimizedAtAfterOrderByOptimizedAtDesc, findByManifestId, findTopByCourierIdOrderByOptimizedAtDesc |
| OutboxMessageRepository | OutboxMessage | Long | findByPublishedFalse, findByPublishedFalseOrderByCreatedAtAsc |
| PaymentIntentRepository | PaymentIntent | Long | findByShipmentId, findByStatus, findByProviderRef, findExpired, findByShipmentIdAndStatus |
| PaymentMethodRepository | PaymentMethod | Long | findByUserIdAndActiveTrue, findByUserIdAndIsDefaultTrue, findByTokenizedRef, findByUserId |
| PaymentRefundRepository | PaymentRefund | Long | findByPaymentIntentId, findByStatus, findByStatusOrderByCreatedAtDesc |
| PaymentTransactionRepository | PaymentTransaction | Long | findByMerchantIdOrderByCreatedAtDesc, findByInvoiceId, findByExternalId, findByStatus |
| PaymentWebhookLogRepository | PaymentWebhookLog | Long | findByProviderAndEventType, findByProcessedFalseOrderByCreatedAtAsc, findByProviderOrderByCreatedAtDesc |
| PayoutItemRepository | PayoutItem | Long | findByPayoutId, findBySourceTypeAndSourceId |
| PayoutRepository | Payout | Long | findByUserId, findByUserIdOrderByPayoutPeriodEndDesc, findByPayoutTypeAndPayoutPeriodEndBetween, findActivePayoutsForUser, findByStatus, findPayoutStatusByName |
| PayoutStatusRepository | PayoutStatus | Long | findByName |
| PickupScheduleRepository | PickupSchedule | Long | findByMerchantIdOrderByPickupDateDesc, findByMerchantIdAndStatusOrderByPickupDateDesc, findByAssignedCourierIdAndPickupDate, findByPickupDateAndStatus, findOverdue, findByStatusOrderByPickupDateAsc, countByMerchantId |
| RecipientDetailsRepository | RecipientDetails | Long | findByPhone |
| ReturnShipmentRepository | ReturnShipment | Long | findByOriginalShipmentIdOrderByCreatedAtDesc, findByReturnShipmentIdOrderByCreatedAtDesc, findByOriginalShipmentIdAndReturnShipmentId, findByReasonContainingIgnoreCaseOrderByCreatedAtDesc, findByStatusOrderByCreatedAtDesc, findByMerchantId, findByAssignedCourierId, existsByOriginalShipmentIdAndStatusNot |
| RoleRepository | Role | Long | findByName, existsByName |
| SecurityEventRepository | SecurityEvent | Long | findByUserIdOrderByCreatedAtDesc, countByEventTypeAndCreatedAtBetween, findByIpAddressAndEventType, findBySeverity, findBySeverityOrderByCreatedAtDesc, findRecentByIpAndType |
| ServiceFeedbackRepository | ServiceFeedback | Long | *(no custom methods)* |
| SettlementBatchRepository | SettlementBatch | Long | findByStatus, findByPeriodAndStartDate, findByStatusOrderByCreatedAtDesc, findByDateRange |
| SettlementItemRepository | SettlementItem | Long | findByBatchId, findByMerchantId, findByBatchIdAndType |
| ShipmentManifestRepository | ShipmentManifest | Long | findByManifestNumber, findByCourierId, findByCourierIdAndStatus |
| ShipmentPackageDetailsRepository | ShipmentPackageDetails | Long | findByShipmentId |
| ShipmentRepository | Shipment | Long | **49+ custom methods** (findByTrackingNumber, findByStatus, searchShipments, countByMerchantIdAndCreatedAtBetween, countByStatusName, findByCourierId, findByMerchantId, findByManifestId, findByPayoutId, findByStatusIn, findUnreconciledDeliveredShipments, etc.) |
| ShipmentStatusHistoryRepository | ShipmentStatusHistory | Long | deleteByShipment |
| ShipmentStatusRepository | ShipmentStatus | Long | findByName, existsByName |
| SlaPolicyRepository | SlaPolicy | Long | findByPriorityAndActiveTrue |
| SubscriptionPlanRepository | SubscriptionPlan | Long | findByName, findByActiveTrueOrderBySortOrderAsc |
| SupportTicketRepository | SupportTicket | Long | findByTicketNumber, findByReporterIdOrderByCreatedAtDesc, findByAssigneeIdOrderByCreatedAtDesc, findByStatus, findByStatusOrderByCreatedAtDesc, findOpenByPriority, countByStatus |
| SystemAuditLogRepository | SystemAuditLog | Long | findByUserIdOrderByCreatedAtDesc, findByActionTypeOrderByCreatedAtDesc, findByEntityTypeAndEntityIdOrderByCreatedAtDesc, findByCreatedAtBetween |
| SystemSettingRepository | SystemSetting | Long | findByUserId, findByUserIdAndSettingKey, deleteByUserId |
| TaxRuleRepository | TaxRule | Long | findByCountryCodeAndActiveTrue, findApplicable |
| TelemetrySettingsRepository | TelemetrySettings | Long | findBySettingKey, existsBySettingKey |
| TenantAuditLogRepository | TenantAuditLog | Long | findByTenantIdOrderByCreatedAtDesc, findByTenantIdAndAction |
| TenantBrandingRepository | TenantBranding | Long | findByTenantId |
| TenantConfigurationRepository | TenantConfiguration | Long | findByTenantIdAndConfigKey, findByTenantIdAndCategory, findByTenantId |
| TenantInvitationRepository | TenantInvitation | Long | findByToken, findByTenantIdAndStatus, findExpired, existsByTenantIdAndPhoneAndStatus |
| TenantQuotaRepository | TenantQuota | Long | findByTenantIdAndQuotaType, findByTenantId, findExceeded |
| TenantRepository | Tenant | Long | findBySlug, findByDomain, findByStatus, findByPlan, findByTenantId, existsBySlug, existsByDomain |
| TenantUserRepository | TenantUser | Long | findByUserId, findByTenantId, findByUserIdAndTenantId, findByTenantIdAndRole, existsByUserIdAndTenantId |
| TrackingSessionRepository | TrackingSession | Long | findByShipmentIdAndStatus, findByCourierIdAndStatus, findFirstByShipmentIdAndStatusOrderByStartedAtDesc, findByCourierId |
| UsageTrackingRepository | UsageTracking | Long | findByMerchantIdAndPeriod, incrementShipments, incrementApiCalls |
| UserRepository | User | Long | findByPhone, findByPhoneWithRoleAndStatus, existsByPhone, findAllExcludingOwners, findAllNonDeleted, findByRoleName, findActiveUsersByRole, findNonDeletedUsers, countActiveUsers, deleteByRoleNot, findByStatusAndNotDeleted, findByRoleAndStatus |
| UserStatusRepository | UserStatus | Long | findByName |
| VehicleAssignmentRepository | VehicleAssignment | Long | findByVehicleIdOrderByCreatedAtDesc, findByCourierIdOrderByCreatedAtDesc, findByCourierIdAndStatus, findByVehicleIdAndStatus, existsByVehicleIdAndStatus, existsByCourierIdAndStatus |
| VehicleMaintenanceRepository | VehicleMaintenance | Long | findByVehicleIdOrderByScheduledDateDesc, findByStatus, findDueForService, findPendingByVehicle |
| VehicleRepository | Vehicle | Long | findByPlateNumber, findByStatus, findByVehicleType, findByStatusAndVehicleType, existsByPlateNumber |
| WalletRepository | Wallet | Long | findByUserId, findByWalletType, existsByUserId |
| WalletTransactionRepository | WalletTransaction | Long | findByWalletIdOrderByCreatedAtDesc, existsByWalletIdAndReferenceIdAndReason |
| WebhookEventRepository | WebhookEvent | Long | findBySubscriptionIdOrderByCreatedAtDesc, findByStatusAndAttemptsLessThan, countBySubscriptionIdAndStatus |
| WebhookSubscriptionRepository | WebhookSubscription | Long | findByMerchantIdAndActiveTrue, findByMerchantId, findActiveByEventType |
| ZoneRepository | Zone | Long | findByStatus, findByNameAndStatus, findByNameIgnoreCase, findAllActiveZonesOrdered, countActiveZones, existsByNameAndStatus |

**Total: 96 repositories**

---

## 5. Services

| Service Class | Annotation | Injected Dependencies |
|--------------|------------|----------------------|
| AccountLockoutService | @Service | AccountLockoutRepository, UserRepository |
| AnalyticsService | @Service | ShipmentRepository, UserRepository, FinancialService |
| ApiKeyService | @Service | ApiKeyRepository, ApiKeyUsageLogRepository, UserRepository |
| AsyncJobExecutor | @Service | AsyncJobRepository, AsyncJobService |
| AsyncJobService | @Service | AsyncJobRepository |
| AuditService | @Service | SystemAuditLogRepository, UserRepository |
| AuthorizationService | @Service | UserRepository, ShipmentRepository |
| AwbService | @Service | *(none)* |
| BackupService | @Service | *(none)* |
| BarcodeService | @Service | *(none)* |
| BaseService | *(abstract class)* | *(none)* |
| BIDashboardService | @Service | RevenueAnalyticsService, OperationsAnalyticsService, CourierAnalyticsService, MerchantAnalyticsService |
| ChatService | @Service | ChatRoomRepository, ChatMessageRepository, ShipmentRepository, UserRepository |
| ComplianceService | @Service | ComplianceRuleRepository, ComplianceReportRepository, UserRepository |
| ContractService | @Service | ContractRepository, UserRepository, OtpService |
| ContractSlaService | @Service | ContractSlaTermsRepository, ShipmentRepository, ContractService |
| CountryService | @Service | CountryRepository |
| CourierAnalyticsService | @Service | ShipmentRepository, UserRepository |
| CourierLocationService | @Service | CourierLocationHistoryRepository, UserRepository |
| CurrencyService | @Service | CurrencyRepository, ExchangeRateRepository |
| CustomPricingService | @Service | ContractRepository, CustomPricingRuleRepository, ShipmentRepository, ZoneRepository |
| DeadLetterService | @Service | DeadLetterEventRepository, UserRepository, EventPublisher |
| DeliveryAttemptService | @Service | DeliveryAttemptRepository, ShipmentRepository, UserRepository |
| DeliveryProofService | @Service | DeliveryProofRepository, ShipmentRepository, UserRepository, FileStorageService |
| DemandPredictionService | @Service | ShipmentRepository, ZoneRepository |
| ECommerceIntegration | *(interface)* | — |
| ECommerceIntegrationFactory | @Component | — |
| ECommerceService | @Service | ECommerceConnectionRepository, ECommerceOrderRepository, ECommerceIntegrationFactory, UserRepository, ZoneRepository |
| EInvoiceService | @Service | EInvoiceRepository, InvoiceRepository |
| EmailNotificationService | @Service | *(none)* |
| ETACalculationService | @Service | *(none)* |
| EventProcessor | @Service | DomainEventRepository, EventSubscriptionRepository, DeadLetterService |
| EventPublisher | @Service | DomainEventRepository, OutboxMessageRepository |
| ExcelService | @Service | ShipmentService, UserRepository, ZoneRepository |
| FawryGateway | @Service | *(none — implements PaymentGateway)* |
| FileStorageService | @Service | *(none)* |
| FileUploadService | @Service | *(none)* |
| FinancialService | @Service | PayoutRepository, PayoutItemRepository, ShipmentRepository, UserRepository, PayoutStatusRepository |
| FleetService | @Service | VehicleRepository, VehicleAssignmentRepository, VehicleMaintenanceRepository, FuelLogRepository, UserRepository |
| InputSanitizationService | @Service | *(none)* |
| InvoiceService | @Service | InvoiceRepository, SubscriptionService |
| IpBlockingService | @Service | IpBlacklistRepository, UserRepository, SecurityEventService |
| KPISnapshotService | @Service | KPISnapshotRepository, ShipmentRepository, UserRepository |
| LiveNotificationService | @Service | LiveNotificationRepository, UserRepository |
| LiveTrackingService | @Service | LocationPingRepository, TrackingSessionRepository, ETACalculationService |
| LocalizationService | @Service | CountryRepository |
| MerchantAnalyticsService | @Service | ShipmentRepository, UserRepository |
| MetricsService | @Service | *(none)* |
| NotificationAnalyticsService | @Service | NotificationDeliveryLogRepository |
| NotificationDispatcher | @Service | NotificationPreferenceRepository, NotificationDeliveryLogRepository, UserRepository, TemplateEngine, NotificationService, EmailNotificationService, PushNotificationService, WhatsAppNotificationService |
| NotificationRetryService | @Service | NotificationDeliveryLogRepository, EmailNotificationService, PushNotificationService, WhatsAppNotificationService |
| NotificationService | @Service | NotificationRepository |
| OperationsAnalyticsService | @Service | ShipmentRepository, DeliveryAttemptRepository |
| OtpService | @Service | *(none)* |
| OutboxPoller | @Service | OutboxMessageRepository, DomainEventRepository |
| PasswordPolicyService | @Service | *(none)* |
| PaymentEventHandler | @Service | *(none)* |
| PaymentGateway | *(interface)* | — |
| PaymentGatewayFactory | @Component | — |
| PaymentIntentService | @Service | PaymentIntentRepository, PaymentMethodRepository, ShipmentRepository, PaymentGatewayFactory |
| PaymentRefundService | @Service | PaymentRefundRepository, PaymentIntentRepository, UserRepository, PaymentGatewayFactory |
| PaymentService | @Service | PaymentTransactionRepository, PaymentGatewayFactory, InvoiceService |
| PaymentWebhookProcessor | @Service | PaymentWebhookLogRepository, PaymentIntentRepository, PaymentGatewayFactory |
| PaymobGateway | @Service | *(none — implements PaymentGateway)* |
| PdfService | @Service | *(none)* |
| PickupScheduleService | @Service | PickupScheduleRepository, UserRepository |
| PresenceService | @Service | *(none)* |
| PushNotificationService | @Service | DeviceTokenRepository |
| ReportExportService | @Service | BIDashboardService |
| ReturnService | @Service | ReturnShipmentRepository, ShipmentRepository, UserRepository |
| RevenueAnalyticsService | @Service | ShipmentRepository |
| RouteOptimizationService | @Service | OptimizedRouteRepository |
| SallaIntegration | @Component | *(implements ECommerceIntegration)* |
| SecureRandomService | @Service | *(none)* |
| SecurityAuditService | @Service | SecurityEventRepository, ComplianceRuleRepository |
| SecurityEventService | @Service | SecurityEventRepository |
| SettlementService | @Service | SettlementBatchRepository, SettlementItemRepository, PaymentIntentRepository, UserRepository |
| ShipmentEventHandler | @Service | *(none)* |
| ShipmentService | @Service | ShipmentRepository, UserRepository, ZoneRepository, CourierZoneRepository, ShipmentStatusHistoryRepository, ShipmentStatusRepository, DeliveryPricingRepository, RecipientDetailsRepository, ShipmentManifestRepository, TelemetrySettingsRepository, CourierLocationHistoryRepository, ReturnShipmentRepository |
| ShopifyIntegration | @Component | *(implements ECommerceIntegration)* |
| SmartAssignmentService | @Service | AssignmentRuleRepository, AssignmentScoreRepository, ShipmentRepository, UserRepository, CourierLocationHistoryRepository, CourierZoneRepository, CourierRatingRepository, VehicleAssignmentRepository |
| SmsService | *(interface)* | — |
| StripeGateway | @Service | *(none — implements PaymentGateway)* |
| SubscriptionService | @Service | MerchantSubscriptionRepository, SubscriptionPlanRepository, UserRepository |
| SupportTicketService | @Service | SupportTicketRepository, SlaPolicyRepository, KnowledgeArticleRepository, UserRepository |
| TapGateway | @Service | *(none — implements PaymentGateway)* |
| TaxService | @Service | TaxRuleRepository |
| TemplateEngine | @Service | NotificationTemplateRepository |
| TenantBrandingService | @Service | TenantBrandingRepository |
| TenantConfigService | @Service | TenantConfigurationRepository |
| TenantContextService | @Service | TenantRepository |
| TenantInvitationService | @Service | TenantInvitationRepository, TenantUserRepository, TenantRepository, UserRepository |
| TenantIsolationService | @Service | TenantContextService, TenantUserRepository, TenantAuditLogRepository |
| TenantMigrationService | @Service | TenantRepository, TenantUserRepository, TenantQuotaRepository, TenantBrandingRepository, UserRepository |
| TenantQuotaService | @Service | TenantQuotaRepository |
| TenantService | @Service | TenantRepository, TenantBrandingRepository |
| TrackingSessionService | @Service | TrackingSessionRepository, ShipmentRepository, UserRepository |
| TwilioSmsService | @Service | *(implements SmsService)* |
| UsageTrackingService | @Service | UsageTrackingRepository, SubscriptionService |
| UserService | @Service | UserRepository, RoleRepository, MerchantDetailsRepository, UserStatusRepository |
| WalletService | @Service | WalletRepository, WalletTransactionRepository, UserRepository, ShipmentRepository |
| WebhookService | @Service | WebhookSubscriptionRepository, WebhookEventRepository |
| WhatsAppNotificationService | @Service | *(none)* |
| WooCommerceIntegration | @Component | *(implements ECommerceIntegration)* |
| ZidIntegration | @Component | *(implements ECommerceIntegration)* |

**Total: 114 service files (88 @Service, 6 @Component, 3 interfaces, 2 abstract/plain classes)**

---

## 6. Controllers (API Endpoints)

### 6.1 Authentication & User Management

#### AuthController — `/api/auth`
| Method | Path | Description |
|--------|------|-------------|
| POST | /login | Login |
| GET | /me | Get current user |
| GET | /health | Auth service health |
| POST | /logout | Logout |
| POST | /change-password | Change password |
| POST | /refresh | Refresh token |

#### UserController — `/api`
| Method | Path | Description |
|--------|------|-------------|
| GET | /users | List users |
| POST | /users | Create user |
| PUT | /users/{id} | Update user |
| DELETE | /users/{id} | Delete user |
| PUT | /users/profile | Update own profile |
| GET | /couriers/{id} | Get courier |
| POST | /couriers | Create courier |
| PUT | /couriers/{id} | Update courier |
| DELETE | /couriers/{id} | Delete courier |
| GET | /couriers/{id}/location | Get courier location |
| PUT | /couriers/{id}/location | Update courier location |
| GET | /merchants/{id} | Get merchant |
| POST | /merchants | Create merchant |
| PUT | /merchants/{id} | Update merchant |
| GET | /employees/{id} | Get employee |
| PUT | /employees/{id} | Update employee |
| GET | /couriers | List couriers |
| GET | /merchants | List merchants |
| GET | /employees | List employees |
| POST | /employees | Create employee |

### 6.2 Shipment Management

#### ShipmentController — `/api/shipments`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{id} | Get shipment |
| GET | /count | Count shipments |
| POST | /warehouse/receive | Receive in warehouse |
| GET | /warehouse/inventory | Warehouse inventory |
| POST | /warehouse/dispatch/{courierId} | Dispatch to courier |
| POST | /warehouse/reconcile/courier/{courierId} | Reconcile courier cash |
| GET | /warehouse/couriers | List warehouse couriers |
| GET | /warehouse/courier/{courierId}/shipments | Courier shipments in warehouse |
| GET | /warehouse/stats | Warehouse statistics |
| POST | /{id}/return-request | Request return |
| PUT | /courier/location/update | Update courier location |
| GET | /list | List shipments |

#### ManifestController — `/api/manifests`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{manifestId} | Get manifest |
| POST | /{manifestId}/shipments | Add shipments to manifest |
| PUT | /{manifestId}/status | Update manifest status |
| POST | /{manifestId}/assign | Assign manifest |

#### LabelController — `/api/shipments`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{id}/label | Get shipping label |
| POST | /labels/bulk | Bulk generate labels |
| GET | /{id}/barcode | Get barcode |
| GET | /{id}/qrcode | Get QR code |
| GET | /{id}/pod | Get proof of delivery |

#### BulkUploadController — `/api/shipments/bulk`
| Method | Path | Description |
|--------|------|-------------|
| GET | /template | Download bulk upload template |

### 6.3 Delivery & Tracking

#### DeliveryController — `/api/delivery`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{shipmentId}/proof | Get delivery proof |
| POST | /{shipmentId}/attempt | Record delivery attempt |
| GET | /{shipmentId}/attempts | List delivery attempts |
| GET | /admin/failures | List failed deliveries |

#### LiveTrackingController — `/api/tracking`
| Method | Path | Description |
|--------|------|-------------|
| POST | /sessions/start | Start tracking session |
| POST | /sessions/{sessionId}/pause | Pause session |
| POST | /sessions/{sessionId}/resume | Resume session |
| POST | /sessions/{sessionId}/end | End session |
| POST | /ping | Send location ping |
| GET | /sessions/shipment/{shipmentId} | Get session by shipment |
| GET | /sessions/courier | Get courier sessions |
| GET | /sessions/{sessionId}/pings | Get session pings |

#### CourierLocationController — `/api/couriers`
| Method | Path | Description |
|--------|------|-------------|
| POST | /location | Update location |
| GET | /{courierId}/location | Get location |
| GET | /{courierId}/location/history | Get location history |

#### ReturnController — `/api/returns`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{id} | Get return |
| PUT | /{id}/status | Update return status |
| PUT | /{id}/assign | Assign return |

#### PickupScheduleController — `/api/pickups`
| Method | Path | Description |
|--------|------|-------------|
| GET | /my | My pickups |
| GET | /today | Today's pickups |
| PUT | /{id}/assign/{courierId} | Assign pickup |
| PUT | /{id}/start | Start pickup |
| PUT | /{id}/complete | Complete pickup |
| PUT | /{id}/cancel | Cancel pickup |
| GET | /admin | Admin list |
| GET | /admin/overdue | Overdue pickups |

### 6.4 Public / Tracking

#### PublicController — `/api/public`
| Method | Path | Description |
|--------|------|-------------|
| GET | /track/{trackingNumber} | Track shipment |
| POST | /feedback/{trackingNumber} | Submit feedback |
| POST | /forgot-password | Forgot password |
| POST | /send-otp | Send OTP |
| POST | /reset-password | Reset password |
| POST | /contact | Contact form |
| GET | /contact/offices | Office info |

#### PublicTrackingController — `/api/public/tracking`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{trackingNumber} | Get tracking info |
| GET | /{trackingNumber}/eta | Get ETA |

### 6.5 Financial

#### FinancialController — `/api/financial`
| Method | Path | Description |
|--------|------|-------------|
| GET | /payouts | List payouts |
| POST | /payouts | Create payout |
| GET | /payouts/{payoutId} | Get payout |
| PUT | /payouts/{payoutId}/status | Update payout status |
| GET | /payouts/pending | Pending payouts |
| GET | /payouts/user/{userId} | User payouts |
| GET | /payouts/{payoutId}/items | Payout items |

#### WalletController — `/api/wallet`
| Method | Path | Description |
|--------|------|-------------|
| GET | /balance | Get wallet balance |
| GET | /transactions | List transactions |
| POST | /withdraw | Withdraw funds |
| GET | /admin/all | Admin list all wallets |

#### SettlementController — `/api/settlements`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{id} | Get settlement |
| POST | /generate | Generate settlement batch |
| GET | /{id}/items | Settlement items |
| POST | /{id}/process | Process settlement |

#### InvoiceController — `/api/invoices`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{id} | Get invoice |
| POST | /{id}/pay | Pay invoice |
| GET | /admin | Admin list invoices |
| POST | /admin/{id}/refund | Refund invoice |

### 6.6 Payment

#### PaymentIntentController — `/api/payments`
| Method | Path | Description |
|--------|------|-------------|
| POST | /intents | Create payment intent |
| GET | /intents/{id} | Get intent |
| POST | /intents/{id}/confirm | Confirm payment |
| POST | /intents/{id}/cancel | Cancel payment |
| GET | /methods | List payment methods |
| POST | /methods | Add payment method |
| DELETE | /methods/{id} | Remove payment method |

#### PaymentRefundController — `/api/payments/refunds`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{id} | Get refund |
| POST | /{id}/approve | Approve refund |
| POST | /{id}/reject | Reject refund |

#### PaymentCallbackController — `/api/payments/callback`
| Method | Path | Description |
|--------|------|-------------|
| POST | /paymob | Paymob callback |
| POST | /tap | Tap callback |
| POST | /stripe | Stripe callback |
| POST | /fawry | Fawry callback |

### 6.7 Dashboard & Analytics

#### DashboardController — `/api/dashboard`
| Method | Path | Description |
|--------|------|-------------|
| GET | /summary | Dashboard summary |
| GET | /statistics | Statistics |
| GET | /dashboard-stats | Dashboard stats |
| GET | /revenue-chart | Revenue chart data |
| GET | /shipments-chart | Shipments chart data |

#### AnalyticsController — `/api/analytics`
| Method | Path | Description |
|--------|------|-------------|
| GET | /revenue | Revenue analytics |
| GET | /status-distribution | Status distribution |
| GET | /courier-ranking | Courier ranking |
| GET | /top-merchants | Top merchants |

#### BIDashboardController — `/api/bi-analytics`
| Method | Path | Description |
|--------|------|-------------|
| GET | /summary | BI summary |
| GET | /revenue | Revenue analytics |
| GET | /operations | Operations analytics |
| GET | /couriers | Courier analytics |
| GET | /merchants | Merchant analytics |
| GET | /kpi/trends | KPI trends |

#### ReportsController — `/api/reports`
| Method | Path | Description |
|--------|------|-------------|
| GET | /shipments | Shipment reports |
| GET | /couriers | Courier reports |
| GET | /merchants | Merchant reports |
| GET | /warehouse | Warehouse reports |
| GET | /dashboard | Dashboard reports |

#### ReportExportController — `/api/reports/export`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{reportType} | Export report |

### 6.8 Notifications

#### NotificationController — `/api/notifications`
| Method | Path | Description |
|--------|------|-------------|
| GET | /unread | Unread notifications |
| PUT | /{id}/read | Mark as read |
| PUT | /read-all | Mark all as read |

#### NotificationPreferenceController — `/api/notifications`
| Method | Path | Description |
|--------|------|-------------|
| GET | /preferences | Get preferences |
| PUT | /preferences | Update preferences |
| PUT | /preferences/pause | Pause notifications |
| POST | /devices | Register device |
| DELETE | /devices/{token} | Remove device |

#### NotificationTemplateController — `/api/admin/notifications`
| Method | Path | Description |
|--------|------|-------------|
| GET | /templates | List templates |
| GET | /templates/{eventType} | Get template |
| PUT | /templates/{id} | Update template |
| POST | /templates/{id}/test | Test template |
| GET | /analytics | Notification analytics |

#### LiveNotificationController — `/api/live-notifications`
| Method | Path | Description |
|--------|------|-------------|
| GET | /unread | Unread live notifications |
| GET | /unread/count | Unread count |
| POST | /{notificationId}/read | Mark as read |
| POST | /read-all | Mark all as read |
| GET | /presence/online | Online users |
| GET | /presence/{userId} | User presence |

### 6.9 Chat

#### ChatController — `/api/chat`
| Method | Path | Description |
|--------|------|-------------|
| POST | /rooms | Create chat room |
| POST | /messages | Send message |
| GET | /rooms/{roomId}/messages | Room messages |
| GET | /rooms/shipment/{shipmentId} | Shipment chat rooms |
| GET | /rooms/my | My chat rooms |
| POST | /rooms/{roomId}/archive | Archive room |

#### WebSocketMessageController *(WebSocket endpoints, no REST mapping)*

### 6.10 Tenant Management

#### TenantController — `/api/tenants`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{id} | Get tenant |
| PUT | /{id} | Update tenant |
| POST | /{id}/suspend | Suspend tenant |
| POST | /{id}/activate | Activate tenant |

#### TenantBrandingController — *(no base path)*
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/tenants/{tenantId}/branding | Get branding |
| PUT | /api/tenants/{tenantId}/branding | Update branding |
| POST | /api/tenants/{tenantId}/branding/logo | Upload logo |

#### TenantUserController — *(no base path)*
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/tenants/{tenantId}/users | List tenant users |
| POST | /api/tenants/{tenantId}/invitations | Create invitation |
| GET | /api/tenants/{tenantId}/invitations | List invitations |
| POST | /api/invitations/{token}/accept | Accept invitation |
| PUT | /api/tenants/{tenantId}/users/{userId}/role | Change role |
| DELETE | /api/tenants/{tenantId}/users/{userId} | Remove user |

#### TenantQuotaController — `/api/tenants/{tenantId}/quotas`
| Method | Path | Description |
|--------|------|-------------|
| PUT | /{quotaType} | Update quota |
| GET | /usage | Quota usage |

### 6.11 Security

#### SecurityEventController — `/api/security`
| Method | Path | Description |
|--------|------|-------------|
| GET | /events | List security events |
| GET | /events/summary | Events summary |
| GET | /events/threats | Threat events |
| GET | /lockouts | Active lockouts |
| POST | /lockouts/{userId}/unlock | Unlock account |
| GET | /audit | Security audit |

#### IpManagementController — `/api/security/ip-blacklist`
| Method | Path | Description |
|--------|------|-------------|
| DELETE | /{id} | Remove IP from blacklist |

### 6.12 Compliance

#### ComplianceController — `/api/compliance`
| Method | Path | Description |
|--------|------|-------------|
| GET | /rules | List rules |
| POST | /check | Run compliance check |
| GET | /reports/{id} | Get report |
| GET | /status | Status overview |
| GET | /reports | List reports |

### 6.13 Smart Assignment & Routes

#### SmartAssignmentController — `/api/assignment`
| Method | Path | Description |
|--------|------|-------------|
| GET | /suggest/{shipmentId} | Suggest courier |
| GET | /score/{shipmentId} | Score breakdown |
| GET | /rules | Get assignment rules |
| PUT | /rules | Update rules |

#### RouteController — `/api/routes`
| Method | Path | Description |
|--------|------|-------------|
| POST | /optimize/{courierId} | Optimize route |
| GET | /{courierId} | Get optimized route |

#### DemandController — `/api/demand`
| Method | Path | Description |
|--------|------|-------------|
| GET | /predict | Demand prediction |
| GET | /courier-need | Courier need forecast |
| GET | /patterns | Demand patterns |

### 6.14 Fleet Management

#### FleetController — `/api/fleet`
| Method | Path | Description |
|--------|------|-------------|
| POST | /vehicles | Create vehicle |
| GET | /vehicles | List vehicles |
| GET | /vehicles/{id} | Get vehicle |
| GET | /vehicles/available | Available vehicles |
| PUT | /vehicles/{id}/retire | Retire vehicle |
| POST | /assignments | Create assignment |
| PUT | /assignments/{id}/return | Return vehicle |
| POST | /maintenance | Schedule maintenance |
| PUT | /maintenance/{id}/complete | Complete maintenance |
| GET | /vehicles/{vehicleId}/maintenance | Vehicle maintenance history |
| POST | /fuel | Add fuel log |
| GET | /vehicles/{vehicleId}/fuel | Vehicle fuel logs |

### 6.15 Developer / Integrations

#### ApiKeyController — `/api/developer/keys`
| Method | Path | Description |
|--------|------|-------------|
| PUT | /{id}/rotate | Rotate API key |
| DELETE | /{id} | Delete API key |
| GET | /{id}/usage | API key usage |

#### ECommerceController — `/api/integrations`
| Method | Path | Description |
|--------|------|-------------|
| POST | /connect | Connect store |
| GET | /connections | List connections |
| DELETE | /{id} | Disconnect |
| GET | /{id}/orders | List orders |
| POST | /{id}/retry | Retry sync |
| GET | /{id}/stats | Connection stats |

#### ECommerceWebhookController — `/api/ecommerce/webhook`
| Method | Path | Description |
|--------|------|-------------|
| POST | /shopify/{connectionId} | Shopify webhook |
| POST | /woocommerce/{connectionId} | WooCommerce webhook |
| POST | /salla/{connectionId} | Salla webhook |
| POST | /zid/{connectionId} | Zid webhook |

#### WebhookController — `/api/webhooks`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{id} | Get webhook subscription |
| DELETE | /{id} | Delete subscription |
| GET | /{id}/events | List webhook events |
| POST | /{id}/test | Test webhook |
| POST | /retry | Retry failed events |

### 6.16 Eventing

#### EventController — `/api/events`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{eventId} | Get event |
| GET | /subscriptions | List subscriptions |
| POST | /subscriptions | Create subscription |
| PUT | /subscriptions/{id} | Update subscription |

#### DeadLetterController — `/api/events/dead-letter`
| Method | Path | Description |
|--------|------|-------------|
| POST | /{id}/retry | Retry dead letter |
| POST | /{id}/resolve | Resolve dead letter |
| GET | /stats | Dead letter stats |

#### AsyncJobController — `/api/jobs`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{jobId} | Get job |
| POST | /{jobId}/cancel | Cancel job |
| GET | /stats | Job stats |

### 6.17 Subscriptions

#### SubscriptionController — `/api/subscriptions`
| Method | Path | Description |
|--------|------|-------------|
| GET | /plans | List plans |
| GET | /my | My subscription |
| PUT | /upgrade | Upgrade plan |
| PUT | /downgrade | Downgrade plan |
| PUT | /cancel | Cancel subscription |
| GET | /usage | Usage data |

### 6.18 E-Invoicing

#### EInvoiceController — `/api/admin/einvoice`
| Method | Path | Description |
|--------|------|-------------|
| POST | /generate/{invoiceId} | Generate e-invoice |
| GET | /{id} | Get e-invoice |
| POST | /{id}/submit | Submit to authority |
| GET | /pending | List pending |

### 6.19 Contract Management

#### ContractController — `/api`
| Method | Path | Description |
|--------|------|-------------|
| POST | /admin/contracts | Create contract |
| GET | /admin/contracts | List contracts |
| GET | /admin/contracts/{id} | Get contract |
| PUT | /admin/contracts/{id} | Update contract |
| POST | /admin/contracts/{id}/send-signature | Send for signature |
| PUT | /admin/contracts/{id}/terminate | Terminate contract |
| GET | /admin/contracts/expiring | Expiring contracts |
| POST | /contracts/{id}/sign | Sign contract |
| GET | /contracts/my | My contracts |

#### ContractPricingController — `/api`
| Method | Path | Description |
|--------|------|-------------|
| POST | /admin/contracts/{contractId}/pricing | Create pricing rule |
| GET | /admin/contracts/{contractId}/pricing | Get pricing rules |
| PUT | /admin/contracts/pricing/{ruleId} | Update pricing rule |
| GET | /pricing/calculate | Calculate price |

#### ContractSlaController — `/api/admin/contracts`
| Method | Path | Description |
|--------|------|-------------|
| GET | /{contractId}/sla | Get SLA terms |
| PUT | /{contractId}/sla | Update SLA terms |
| GET | /{contractId}/sla/compliance | SLA compliance |
| GET | /{contractId}/sla/penalties | SLA penalties |

### 6.20 Ratings

#### RatingController — `/api/ratings`
| Method | Path | Description |
|--------|------|-------------|
| GET | /courier/{courierId} | Courier ratings |
| GET | /shipment/{shipmentId} | Shipment rating |

### 6.21 Support

#### SupportController — `/api/support`
| Method | Path | Description |
|--------|------|-------------|
| POST | /tickets | Create ticket |
| GET | /tickets/my | My tickets |
| GET | /tickets/{id} | Get ticket |
| GET | /tickets/{id}/messages | Ticket messages |
| POST | /tickets/{id}/messages | Add message |
| PUT | /tickets/{id}/assign/{assigneeId} | Assign ticket |
| PUT | /tickets/{id}/resolve | Resolve ticket |
| PUT | /tickets/{id}/close | Close ticket |
| GET | /tickets/admin | Admin list tickets |
| POST | /articles | Create article |
| PUT | /articles/{id}/publish | Publish article |
| GET | /articles | List articles |
| GET | /articles/search | Search articles |
| GET | /articles/{id} | Get article |

### 6.22 Internationalization

#### CountryController — *(no base path)*
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/countries | List countries |
| GET | /api/countries/{code} | Get country |
| POST | /api/admin/countries | Create country |
| PUT | /api/admin/countries/{code} | Update country |
| PATCH | /api/admin/countries/{code}/toggle | Toggle country |

#### CurrencyController — *(no base path)*
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/currencies | List currencies |
| GET | /api/currencies/convert | Convert currency |
| GET | /api/currencies/rate | Get rate |
| PUT | /api/admin/currencies/exchange-rate | Update rate |

#### TaxController — *(no base path)*
| Method | Path | Description |
|--------|------|-------------|
| GET | /api/tax/calculate | Calculate tax |
| GET | /api/admin/tax/rules | List rules |
| GET | /api/admin/tax/rules/{countryCode} | Rules by country |
| POST | /api/admin/tax/rules | Create rule |
| PUT | /api/admin/tax/rules/{id} | Update rule |

### 6.23 Master Data & Settings

#### MasterDataController — `/api/master`
| Method | Path | Description |
|--------|------|-------------|
| GET | /users | List users |
| POST | /users | Create user |
| PUT | /users/{id} | Update user |
| DELETE | /users/{id} | Delete user |
| GET | /zones | List zones |
| POST | /zones | Create zone |
| PUT | /zones/{id} | Update zone |
| DELETE | /zones/{id} | Delete zone |
| GET | /pricing | List pricing |
| POST | /pricing | Create pricing |
| PUT | /pricing/{id} | Update pricing |
| DELETE | /pricing/{id} | Delete pricing |
| GET | /telemetry | List telemetry settings |
| PUT | /telemetry | Update telemetry |
| GET | /telemetry/{key} | Get telemetry setting |
| DELETE | /telemetry/{key} | Delete setting |

#### SettingsController — `/api/settings`
| Method | Path | Description |
|--------|------|-------------|
| POST | /reset | Reset settings |

### 6.24 SMS

#### SmsController — `/api/sms`
| Method | Path | Description |
|--------|------|-------------|
| POST | /send | Send SMS |
| POST | /send-otp | Send OTP |
| POST | /send-notification | Send notification SMS |
| GET | /test | Test SMS |

### 6.25 System

#### HealthController — `/api`
| Method | Path | Description |
|--------|------|-------------|
| GET | /health | Health check |

#### AuditController — `/api/audit`
| Method | Path | Description |
|--------|------|-------------|
| GET | /logs | List audit logs |
| GET | /entity/{entityType}/{entityId} | Entity audit logs |
| GET | /user/{userId} | User audit logs |

#### BackupController — `/api/backup`
| Method | Path | Description |
|--------|------|-------------|
| POST | /create | Create backup |
| POST | /restore | Restore backup |
| GET | /status | Backup status |
| GET | /test | Test backup |

#### TelemetryController — `/api/telemetry`
*(No GET/POST/PUT endpoints detected via regex — likely uses @MessageMapping or other annotations)*

**Total: 56 controller classes, ~250+ endpoints**

---

## 7. Configuration Classes

| Class | Annotations | Purpose |
|-------|------------|---------|
| CacheConfig | @Configuration, @EnableCaching | Redis/simple cache configuration |
| DataInitializer | @Component | Seeds initial data (roles, statuses) |
| JacksonConfig | @Configuration | JSON serialization/deserialization settings |
| SwaggerConfig | @Configuration | Swagger/OpenAPI documentation config |
| WebSocketConfig | @Configuration, @EnableWebSocket | WebSocket configuration |
| WebSocketAuthInterceptor | @Component | WebSocket authentication |
| ApiKeyAuthFilter | @Component | API key-based authentication filter |
| ApiVersionFilter | @Component, @Order | API versioning filter |
| RequestTracingFilter | @Component, @Order | Request tracing/correlation |

---

## 8. Security Layer

| Class | Annotation | Purpose |
|-------|-----------|---------|
| SecurityConfig | @Configuration, @EnableWebSecurity | Main security configuration (CORS, CSRF, filter chain) |
| ApplicationConfig | @Configuration | Authentication manager, password encoder beans |
| JwtAuthenticationFilter | @Component | JWT token validation filter |
| JwtService | @Service | JWT token creation/validation |
| TokenBlacklistService | @Service | Token blacklisting for logout |
| AuthenticationHelper | @Component | Helper for extracting authenticated user |
| PermissionService | @Service | Permission checking logic |
| Permission | *(enum)* | Defines all permissions (20 permissions) |
| InputSanitizationFilter | @Component | XSS/injection protection |
| IpBlacklistFilter | @Component | IP-level blocking |
| RateLimitFilter | @Component | Rate limiting |
| RequestCorrelationFilter | @Component, @Order | Adds correlation ID to requests |
| TenantContextFilter | @Component, @Order | Sets tenant context per request |
| TenantDataFilter | @Component, @Order | Filters data by tenant |
| SecurityExceptionHandler | @Component | Handles security exceptions |

---

## 9. DTOs

| DTO Class | Notable Records/Inner Classes |
|-----------|-------------------------------|
| AddPaymentMethodRequest | type, provider, last4, brand, isDefault, tokenizedRef, metadata |
| AdvancedNotificationDTO | PreferenceRequest, PreferenceResponse, PauseRequest, RegisterDeviceRequest, DeviceTokenResponse, TemplateResponse, UpdateTemplateRequest, TestNotificationRequest, DeliveryStatsResponse |
| AnalyticsDTO | RevenueReport, PeriodRevenue, StatusDistribution, CourierPerformance, TopMerchant |
| ApiPageResponse | page, size, totalPages, totalElements (generic paginated response) |
| ApiResponse | success, message, data, errors, timestamp (standard API response wrapper) |
| AssignmentDTO | ScoreBreakdownResponse, SuggestionResponse, AutoAssignRequest/Response, RuleResponse, UpdateRuleRequest, OptimizedRouteResponse, DemandPredictionResponse, DemandPatternResponse |
| AssignShipmentsRequest | trackingNumbers |
| AsyncJobDTO | AsyncJobRequest (jobType, payload, priority, maxRetries) |
| ChangePasswordRequest | oldPassword, newPassword |
| ComplianceDTO | ComplianceCheckRequest (category) |
| ContactFormRequest | firstName, lastName, email, subject, message |
| ContractDTO | CreateContractRequest, UpdateContractRequest, ContractResponse, SignContractRequest, TerminateContractRequest, CreatePricingRuleRequest, PricingRuleResponse, PricingCalculationRequest, SlaTermsRequest, SlaTermsResponse |
| CountryDTO | CreateCountryRequest, CountryResponse, CurrencyResponse, ExchangeRateResponse, UpdateExchangeRateRequest, ConvertRequest/Response, CreateTaxRuleRequest, TaxRuleResponse, TaxCalculationResponse, EInvoiceResponse |
| CourierRatingDTO | id, courierId, courierName, shipmentId, trackingNumber, rating, comment, createdAt |
| CourierRatingRequest | shipmentId, rating, comment, ratedByPhone |
| CreateChatRoomRequest | shipmentId, roomType, participants |
| CreateManifestRequest | courierId |
| CreatePaymentIntentRequest | shipmentId, amount, currency, gateway, paymentMethodId |
| CreatePayoutRequest | userId, payoutType, startDate, endDate |
| CreateRefundRequest | paymentIntentId, amount, reason |
| CreateShipmentRequest | recipientName, recipientPhone, alternatePhone, recipientAddress, packageDescription, packageWeight, itemValue, codAmount, zoneId, priority, shippingFeePaidBy, specialInstructions |
| CreateUserRequest | name, phone, password, role, active |
| DashboardStatsDTO | totalShipments, todayShipments, deliveredShipments, totalRevenue, activeUsers, role, timestamp |
| DeliveryDTO | SubmitProofRequest, ProofResponse, RecordAttemptRequest, AttemptResponse, FailureReportResponse, SchedulePickupRequest, PickupResponse |
| DeveloperDTO | CreateApiKeyRequest, ApiKeyResponse, ApiKeyCreatedResponse, ApiKeyUsageResponse, ConnectStoreRequest, ConnectionResponse, ECommerceOrderResponse |
| DtoMapper | Static utility: toUserResponseDTO, toLoginResponseDTO, toShipmentResponseDTO |
| EventDTO | SubscriptionRequest (subscriberName, eventType, handlerClass, filterExpression, retryPolicy) |
| FinancialResponseDTO | id, userName, payoutType, status, amount, periodStart, periodEnd, paidAt |
| FleetDTO | CreateVehicleRequest, VehicleResponse, AssignVehicleRequest, ReturnVehicleRequest, AssignmentResponse, ScheduleMaintenanceRequest, MaintenanceResponse, AddFuelLogRequest, FuelLogResponse |
| GenerateSettlementRequest | period, startDate, endDate |
| LocationDTO | latitude, longitude, timestamp |
| LocationPingRequest | sessionId, lat, lng, accuracy, speed, heading, batteryLevel |
| LocationUpdateRequest | latitude, longitude |
| LoginRequest | phone, password |
| LoginResponseDTO | id, name, phone, role, status |
| ManifestResponseDTO | id, courierName, status, shipmentCount, createdAt, trackingNumbers |
| NotificationDTO | id, type, channel, title, message, actionUrl, read, createdAt, readAt |
| PasswordResetRequest | phone, otp, newPassword, confirmPassword |
| ReconcileRequest | cash_confirmed_shipment_ids, returned_shipment_ids |
| ReportDTO | totalShipments, deliveredShipments, totalRevenue, totalEarnings |
| ReturnRequest | reason |
| ReturnRequestDTO | shipmentId, reason, notes |
| ReturnResponseDTO | Full return details |
| SecurityDTO | UnlockRequest (userId), IpBlockRequest (ipAddress, reason, permanent) |
| SendChatMessageRequest | roomId, content, messageType |
| ShipmentResponseDTO | id, trackingNumber, status, merchantName, courierName, recipientName, recipientPhone, deliveryFee, createdAt, updatedAt |
| StartTrackingRequest | shipmentId |
| SubscriptionDTO | PlanResponse, SubscribeRequest, UpgradeRequest, SubscriptionResponse, UsageResponse, InvoiceResponse, InvoiceItemResponse, PaymentRequest |
| SupportDTO | CreateTicketRequest, AddMessageRequest, TicketResponse, MessageResponse, ArticleRequest, ArticleResponse |
| TenantBrandingDTO | BrandingRequest, BrandingResponse |
| TenantDTO | CreateTenantRequest, UpdateTenantRequest, TenantResponse, TenantSummaryResponse |
| TenantUserDTO | InvitationRequest, InvitationResponse, TenantUserResponse, ChangeRoleRequest, QuotaResponse |
| TrackingResponseDTO | trackingNumber, currentStatus, courierName, lastCourierLocation, estimatedMinutesToDelivery, podType, statusTimeline |
| UpdateUserRequest | name, phone, active, password |
| UserResponseDTO | id, name, phone, role, status, active |
| WalletDTO | id, balance, currency, walletType, updatedAt, recentTransactions; TransactionDTO |
| WebhookDTO | id, url, events, active, createdAt, totalEvents, failedEvents; CreateWebhookRequest, WebhookEventDTO |

**Total: 58 DTO files**

---

## 10. Exceptions & Validation

### Custom Exceptions
| Exception | Extends | HTTP Status |
|-----------|---------|-------------|
| ResourceNotFoundException | RuntimeException | 404 Not Found |
| BusinessRuleException | RuntimeException | 400 Bad Request |
| DuplicateResourceException | RuntimeException | 409 Conflict |
| InvalidOperationException | RuntimeException | 400 Bad Request |

### GlobalExceptionHandler
Handles: ResourceNotFoundException, BusinessRuleException, DuplicateResourceException, InvalidOperationException, MethodArgumentNotValidException, ConstraintViolationException, DataIntegrityViolationException, AccessDeniedException, BadCredentialsException, AuthenticationException, HttpMessageNotReadableException, HttpRequestMethodNotSupportedException, HttpMediaTypeNotSupportedException, MaxUploadSizeExceededException, NoSuchElementException, generic Exception

### Validation
| Class | Purpose |
|-------|---------|
| ValidPassword | Custom annotation for password validation |
| PasswordValidator | Implements ConstraintValidator for @ValidPassword |

### Error Messages
`ErrorMessages.java` — Centralized bilingual (Arabic/English) error and success message constants.

---

## 11. Utility Classes

| Class | Purpose |
|-------|---------|
| AppUtils | Response builders (success, error, notFound, unauthorized, forbidden), DateTime operations, validation patterns |

---

## 12. Application Configuration

### application.yml Key Settings
| Setting | Value |
|---------|-------|
| Database | MySQL 8 via `jdbc:mysql://localhost:3306/twsela` |
| DB Auth | root/root (overridable via env vars) |
| ORM | Hibernate with `ddl-auto: validate` |
| Migrations | Flyway enabled, baseline-on-migrate |
| Cache | Redis (lettuce pool, 16 max-active) or simple |
| Server Port | 8000 |
| SSL | Configurable, disabled by default |
| JWT | HMAC-SHA256, 24h expiration |
| OTP | 5m validity, 5 max attempts |
| CORS | localhost:5173, 3000, 8000, 8080 |
| Monitoring | Prometheus + Grafana (actuator endpoints) |
| Swagger | Enabled at /swagger-ui.html with 14 API groups |
| SMS | Twilio (configurable to console for dev) |
| Logging | Logback, file + console, WARN for root, INFO for com.twsela |
| Backup | Cron at 2 AM, 30-day retention |

---

## Summary Statistics

| Category | Count |
|----------|-------|
| Entity Classes | 100 (96 with @Table + 4 enums/constants) |
| Database Tables | 96 |
| Repositories | 96 |
| Service Classes | 114 (88 @Service, 6 @Component, 3 interfaces) |
| Controllers | 56 |
| REST Endpoints | ~250+ |
| DTOs | 58 |
| Config Classes | 9 |
| Security Classes | 15 |
| Custom Exceptions | 4 |
| Enums | 65+ (all inline in entity classes + 1 standalone Permission) |
| Utility Classes | 1 (AppUtils) |

