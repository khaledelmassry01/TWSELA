# Sprint 13 — Real-time Features: WebSocket، إشعارات متعددة القنوات، وأمان Token المتقدم
**المهام: 30 | حزم العمل: 4**

> **الهدف:** إضافة WebSocket للتتبع اللحظي، نظام إشعارات متعدد القنوات (Email + In-App + SMS)، وتحسين دورة حياة JWT مع httpOnly cookies.

---

## WP-1: WebSocket Infrastructure (8 مهام)

### T-13.01: إعداد Spring WebSocket + STOMP
- إضافة `spring-boot-starter-websocket` dependency
- إنشاء `WebSocketConfig.java`:
  - `@EnableWebSocketMessageBroker`
  - STOMP endpoint: `/ws`
  - Application destination prefix: `/app`
  - Topic prefix: `/topic`, Queue prefix: `/queue`
  - SockJS fallback enabled

### T-13.02: WebSocket Authentication
- إنشاء `WebSocketAuthInterceptor` (ChannelInterceptor)
- استخراج JWT من STOMP CONNECT headers
- ربط الـ user principal بالـ WebSocket session
- رفض الاتصالات بدون token صالح

### T-13.03: ShipmentTrackingService (Real-time)
- عند تغيير حالة الشحنة → broadcast لجميع المشتركين:
  - `/topic/shipment/{shipmentId}` — التاجر + المستلم
  - `/topic/courier/{courierId}` — Courier الخاص
  - `/topic/dashboard/{role}` — لوحة التحكم
- Payload: `ShipmentStatusUpdate {shipmentId, oldStatus, newStatus, timestamp, courierName, location}`

### T-13.04: Dashboard Live Updates
- عند تغيير أي counter → broadcast:
  - `/topic/dashboard/stats/{merchantId}` — إحصائيات التاجر
  - `/topic/dashboard/stats/admin` — إحصائيات الإدارة
  - `/topic/dashboard/stats/courier/{courierId}` — إحصائيات الكوريير
- Payload: `DashboardUpdate {type, metric, oldValue, newValue}`

### T-13.05: Frontend WebSocket Client
- إنشاء `websocket_service.js`:
  - `connect(token)`, `disconnect()`, `subscribe(topic, callback)`, `unsubscribe(topic)`
  - Auto-reconnect مع exponential backoff (1s, 2s, 4s, 8s, max 30s)
  - Connection state management: CONNECTING, CONNECTED, DISCONNECTED, RECONNECTING
  - Heart-beat: 10s/10s

### T-13.06: ربط WebSocket بلوحات التحكم
- `merchant-dashboard-page.js`: subscribe لـ shipment updates + stats
- `owner-dashboard-page.js`: subscribe لـ global stats
- `courier-dashboard-page.js`: subscribe لـ assigned shipments
- تحديث UI بدون page refresh عند وصول message

### T-13.07: Shipment Tracking Timeline (Frontend)
- إضافة قسم "التتبع اللحظي" في `shipment-details.html`
- Timeline component يعرض كل تغيير حالة بالوقت
- WebSocket subscription لتحديثات لحظية
- Fallback: polling كل 30 ثانية إذا فشل WebSocket

### T-13.08: WebSocket Connection Health
- Frontend: عرض مؤشر حالة الاتصال (أخضر/أصفر/أحمر)
- Backend: `WebSocketEventListener` يسجل connect/disconnect events
- Prometheus metrics: `ws_connections_active`, `ws_messages_sent_total`

---

## WP-2: نظام الإشعارات المتقدم (8 مهام)

### T-13.09: Notification Entity + Repository
```java
@Entity
public class Notification {
    private Long id;
    private Long userId;
    @Enumerated(EnumType.STRING)
    private NotificationType type; // SHIPMENT_STATUS, PAYOUT, SYSTEM, ASSIGNMENT
    @Enumerated(EnumType.STRING)
    private NotificationChannel channel; // IN_APP, EMAIL, SMS
    private String title;
    private String message;
    private String actionUrl;
    private boolean read;
    private Instant createdAt;
    private Instant readAt;
}
```

### T-13.10: NotificationService
- `send(userId, type, channel, title, message, actionUrl)`
- `sendMultiChannel(userId, type, channels[], title, message)` — يرسل عبر كل القنوات
- `markAsRead(notificationId, userId)`
- `markAllAsRead(userId)`
- `getUnread(userId)` → `List<NotificationDTO>`
- `getAll(userId, pageable)` → `Page<NotificationDTO>`

### T-13.11: Notification Preferences
- إضافة `notification_preferences` table:
  - `userId`, `type`, `email_enabled`, `sms_enabled`, `inapp_enabled`
- API: `GET/PUT /api/notifications/preferences`
- Default: in-app ON لكل شيء، email ON لـ PAYOUT فقط، SMS ON لـ critical فقط

### T-13.12: Email Service Integration
- إنشاء `EmailNotificationService` باستخدام Spring Mail
- SMTP configuration (SendGrid أو أي provider)
- Templates بالعربية:
  - `shipment-status-change.html`
  - `payout-processed.html`
  - `welcome-email.html`
  - `password-reset.html`
- Async sending مع `@Async`

### T-13.13: تحديث SMS Service
- توحيد `TwilioSmsService` مع `NotificationService`
- إزالة `Thread.sleep()` blocking retry → `@Retryable` مع Spring Retry
- إضافة `SmsTemplate` للرسائل المنسقة
- Rate limiting: max 3 SMS per user per hour

### T-13.14: Notification Triggers
- ربط الإشعارات بأحداث النظام:
  - Shipment created → Merchant (in-app)
  - Shipment assigned → Courier (in-app + SMS)
  - Shipment status change → Merchant (in-app + email optional)
  - Shipment delivered → Merchant + Courier (in-app)
  - Payout processed → Merchant (in-app + email)
  - New courier assigned → Owner (in-app)

### T-13.15: Notification Bell (Frontend)
- إضافة bell icon في الـ navbar لكل الأدوار
- Badge بعدد الإشعارات غير المقروءة
- Dropdown عند الضغط يعرض آخر 10 إشعارات
- "عرض الكل" → صفحة إشعارات مخصصة
- WebSocket subscription لإشعارات جديدة

### T-13.16: NotificationController
- `GET /api/notifications` → paginated list
- `GET /api/notifications/unread` → unread count + list
- `PUT /api/notifications/{id}/read`
- `PUT /api/notifications/read-all`
- `GET /api/notifications/preferences`
- `PUT /api/notifications/preferences`

---

## WP-3: تحسين أمان JWT + httpOnly Cookies (7 مهام)

### T-13.17: JWT Token Refresh Mechanism
- إنشاء refresh token strategy:
  - Access token: 15 minutes TTL
  - Refresh token: 7 days TTL, stored in Redis
- `POST /api/auth/refresh` → يقبل refresh token، يرجع access token جديد
- Refresh token rotation: كل refresh يلغي القديم ويصدر جديد

### T-13.18: httpOnly Cookie Implementation
- تعديل `AuthController.login()`:
  - Access token → httpOnly cookie (`twsela_access`, Secure, SameSite=Strict)
  - Refresh token → httpOnly cookie (`twsela_refresh`, Secure, SameSite=Strict, Path=/api/auth/refresh)
  - لا ترسل tokens في response body
- تعديل `JwtAuthenticationFilter` لقراءة Token من Cookie أولاً، ثم Authorization header (backward compatibility)

### T-13.19: CSRF Protection مع Cookies
- إضافة CSRF token generation (Double Submit Cookie pattern)
- `X-CSRF-Token` header مطلوب لكل POST/PUT/DELETE
- Exempt: login, public endpoints

### T-13.20: تحديث Frontend Auth
- تعديل `auth_service.js`:
  - حذف `localStorage.setItem('token', ...)` / `sessionStorage`
  - إضافة `credentials: 'include'` لكل fetch request
  - Auto-refresh: عند 401 → call `/api/auth/refresh` → retry original request
  - CSRF: قراءة token من cookie → إضافته في headers

### T-13.21: تعديل `api_service.js` لـ Cookie-based Auth
- إزالة `Authorization: Bearer ${token}` manual header
- إضافة `credentials: 'include'` في كل request
- إضافة CSRF header في non-GET requests
- تعديل error handling: 401 → trigger refresh flow

### T-13.22: Fix auth_service.js checkAuthStatus()
- إصلاح CRIT-3: `checkAuthStatus()` يرجع `true` عند network error
- تعديل: عند network error → `return false` + show connection error
- إضافة retry logic: حتى 3 محاولات قبل failure

### T-13.23: إزالة sensitive data من Client Storage
- حذف أي شيء حساس من localStorage / sessionStorage
- User info (non-sensitive) ممكن تبقى في sessionStorage
- Token لن يكون في client-accessible storage أبداً

---

## WP-4: اختبارات Sprint 13 (7 مهام)

### T-13.24: اختبارات WebSocket Connection
- 4 tests: connect مع token صالح، رفض بدون token، reconnect، heartbeat

### T-13.25: اختبارات WebSocket Messaging
- 4 tests: subscribe/publish لكل topic رئيسي
- تأكيد أن المشتركين يستقبلون الرسائل الصحيحة فقط

### T-13.26: اختبارات NotificationService
- 5 tests: send, sendMultiChannel, markAsRead, markAllAsRead, getUnread

### T-13.27: اختبارات NotificationController
- 6 tests: كل endpoint مع pagination + authorization

### T-13.28: اختبارات JWT Refresh
- 4 tests: refresh ناجح، expired refresh، rotation، blacklist after refresh

### T-13.29: اختبارات Cookie Auth
- 3 tests: login sets cookies, request reads cookie, CSRF validation

### T-13.30: Integration Tests
- 4 tests: سيناريو كامل (login → receive notification → refresh token → logout)

---

## معايير القبول
- [ ] WebSocket يعمل مع authentication
- [ ] تحديثات لحظية في لوحات التحكم بدون refresh
- [ ] Timeline تتبع الشحنة يعمل real-time
- [ ] إشعارات in-app + email + SMS مع preferences
- [ ] Notification bell يظهر عدد غير المقروءة
- [ ] JWT refresh tokens مع rotation
- [ ] httpOnly cookies (مع backward compatibility)
- [ ] CSRF protection فعّال
- [ ] localStorage خالي من tokens
- [ ] `checkAuthStatus()` يرجع false عند network error
- [ ] 180+ tests, 0 failures, BUILD SUCCESS
