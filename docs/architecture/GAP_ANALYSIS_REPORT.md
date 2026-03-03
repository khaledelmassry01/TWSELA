# تقرير فجوات الكود و قاعدة البيانات — Twsela
# Code-to-Database Gap Analysis Report
**التاريخ:** 2026-03-03

---

## ملخص تنفيذي (Executive Summary)

بعد دراسة شاملة للـ codebase (الـ Java entities + repositories + services + controllers) ومقارنتها بقاعدة البيانات MySQL الفعلية (72 جدول)، تم اكتشاف **5 أنواع من الفجوات الحرجة**:

| الفئة | العدد | الخطورة |
|-------|-------|---------|
| أعمدة بأسماء مختلفة بين Entity و DB | 4 | 🔴 حرج — خطأ وقت التشغيل |
| أعمدة موجودة في Entity ولا توجد في DB | 4 | 🔴 حرج — خطأ وقت التشغيل |
| Entities بدون جداول في DB (29) | 29 | 🔴 حرج — خطأ وقت التشغيل |
| `tenant_id` في Entity بدون عمود DB | 6 | 🔴 حرج — خطأ وقت التشغيل |
| أعمدة `updated_at` ناقصة في Entity | 3 | 🟡 تحذير — لا خطأ مباشر |

---

## الفجوة 1: أعمدة بأسماء مختلفة (Column Name Mismatches)

| Entity | حقل Entity (`@Column name`) | عمود DB الفعلي | الأثر |
|--------|------------------------------|-----------------|-------|
| `PaymentTransaction` | `gateway` | `gateway_type` | `SQLGrammarException` عند أي query |
| `PaymentTransaction` | `type` | `payment_type` | `SQLGrammarException` عند أي query |
| `TicketMessage` | `is_internal` | `internal` | `SQLGrammarException` عند أي query |
| `SlaPolicy` | `is_active` | `active` | `SQLGrammarException` عند أي query |

**حل:** تعديل أسماء الأعمدة في DB لتتطابق مع الـ Entity (أأمن من تعديل الكود لأن الكود يعتمد على getter/setter names).

---

## الفجوة 2: أعمدة موجودة في Entity فقط (Phantom Columns)

| Entity | عمود في `@Column` | موجود في DB؟ | الأثر |
|--------|-------------------|---------------|-------|
| `TaxRule` | `name` | ❌ | `SQLGrammarException` |
| `TaxRule` | `name_ar` | ❌ | `SQLGrammarException` |
| `Invoice` | `payment_transaction_id` | ❌ | `SQLGrammarException` |
| `SubscriptionPlan` | `display_name_ar` | ❌ | `SQLGrammarException` |

**حل:** إضافة هذه الأعمدة لجداول DB.

---

## الفجوة 3: 29 Entity بدون جداول في DB

### مجموعة Multi-Tenant (8 جداول)
| Entity | Table Name |
|--------|-----------|
| `Tenant` | `tenants` |
| `TenantUser` | `tenant_users` |
| `TenantBranding` | `tenant_branding` |
| `TenantConfiguration` | `tenant_configurations` |
| `TenantInvitation` | `tenant_invitations` |
| `TenantQuota` | `tenant_quotas` |
| `TenantAuditLog` | `tenant_audit_logs` |
| `SystemSetting` | `system_settings` |

### مجموعة Payment/Settlement (6 جداول)
| Entity | Table Name |
|--------|-----------|
| `PaymentIntent` | `payment_intents` |
| `PaymentMethod` | `payment_methods` |
| `PaymentRefund` | `payment_refunds` |
| `PaymentWebhookLog` | `payment_webhook_logs` |
| `SettlementBatch` | `settlement_batches` |
| `SettlementItem` | `settlement_items` |

### مجموعة Real-time/Chat (5 جداول)
| Entity | Table Name |
|--------|-----------|
| `ChatRoom` | `chat_rooms` |
| `ChatMessage` | `chat_messages` |
| `LocationPing` | `location_pings` |
| `TrackingSession` | `tracking_sessions` |
| `LiveNotification` | `live_notifications` |

### مجموعة Event Sourcing (5 جداول)
| Entity | Table Name |
|--------|-----------|
| `DomainEvent` | `domain_events` |
| `DeadLetterEvent` | `dead_letter_events` |
| `EventSubscription` | `event_subscriptions` |
| `OutboxMessage` | `outbox_messages` |
| `AsyncJob` | `async_jobs` |

### مجموعة Security/Compliance (5 جداول)
| Entity | Table Name |
|--------|-----------|
| `SecurityEvent` | `security_events` |
| `AccountLockout` | `account_lockouts` |
| `IpBlacklist` | `ip_blacklist` |
| `ComplianceRule` | `compliance_rules` |
| `ComplianceReport` | `compliance_reports` |

**كل الـ 29 entities مستخدمة فعلياً:** لكل واحد Repository + Service + Controller (أو scheduled job).

---

## الفجوة 4: `tenant_id` بدون عمود DB

| Entity | Table |
|--------|-------|
| `User` | `users` |
| `Shipment` | `shipments` |
| `Zone` | `zones` |
| `Wallet` | `wallets` |
| `Contract` | `contracts` |
| `ApiKey` | `api_keys` |

**حل:** إضافة عمود `tenant_id BIGINT NULL` لكل جدول.

---

## الفجوة 5: `updated_at` ناقص من Entity (تحذيري)

| Entity | Table | `updated_at` في DB؟ |
|--------|-------|---------------------|
| `TicketMessage` | `ticket_messages` | ✅ موجود |
| `SlaPolicy` | `sla_policies` | ✅ موجود |
| `SubscriptionPlan` | `subscription_plans` | ✅ موجود |

هذه ليست كسر — Hibernate سيتجاهل الأعمدة الإضافية في DB. لكن يُفضل إضافتها للـ Entity.

---

## خطة الإصلاح

### المرحلة 1: تصحيح أسماء الأعمدة (DB ALTER)
- `payment_transactions`: RENAME `gateway_type` → `gateway`, `payment_type` → `type`
- `ticket_messages`: RENAME `internal` → `is_internal`
- `sla_policies`: RENAME `active` → `is_active`

### المرحلة 2: إضافة أعمدة ناقصة
- `tax_rules`: ADD `name`, `name_ar`
- `invoices`: ADD `payment_transaction_id`
- `subscription_plans`: ADD `display_name_ar`
- 6 جداول: ADD `tenant_id`

### المرحلة 3: إنشاء 29 جدول مفقود
- Flyway migration V25 لكل الجداول

### المرحلة 4: إضافة `updatedAt` للـ Entities الناقصة
- `TicketMessage`, `SlaPolicy`, `SubscriptionPlan`
