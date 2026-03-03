# Sprint 12 — تقوية Backend: الاستجابة الموحدة، DTOs، ومعالجة الأخطاء
**المهام: 30 | حزم العمل: 4**

> **الهدف:** توحيد كل استجابات الـ API في wrapper واحد `ApiResponse<T>`، استبدال `Map<String,Object>` بـ DTOs حقيقية، وإضافة `@ControllerAdvice` شامل.

---

## WP-1: ApiResponse Wrapper + GlobalExceptionHandler (8 مهام)

### T-12.01: إنشاء `ApiResponse<T>` generic wrapper
```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private List<String> errors;
    private Instant timestamp;
}
```
- Factory methods: `ok(data)`, `ok(data, message)`, `error(message)`, `error(message, errors)`
- Pagination wrapper: `ApiPageResponse<T> extends ApiResponse<List<T>>` مع `page`, `size`, `totalPages`, `totalElements`

### T-12.02: إنشاء/تحديث `GlobalExceptionHandler` (@ControllerAdvice)
- `@ExceptionHandler(EntityNotFoundException.class)` → 404
- `@ExceptionHandler(AccessDeniedException.class)` → 403
- `@ExceptionHandler(MethodArgumentNotValidException.class)` → 400 مع تفاصيل
- `@ExceptionHandler(ConstraintViolationException.class)` → 400
- `@ExceptionHandler(DataIntegrityViolationException.class)` → 409
- `@ExceptionHandler(Exception.class)` → 500 (catch-all مع logging)
- كل الاستجابات تستخدم `ApiResponse`

### T-12.03: إنشاء custom exceptions
- `ResourceNotFoundException extends RuntimeException`
- `BusinessRuleException extends RuntimeException`
- `DuplicateResourceException extends RuntimeException`
- `InvalidOperationException extends RuntimeException`

### T-12.04: تطبيق ApiResponse في AuthController
- `/api/auth/login` → `ApiResponse<LoginResponseDTO>`
- `/api/auth/me` → `ApiResponse<UserResponseDTO>`
- `/api/auth/logout` → `ApiResponse<Void>`
- `/api/auth/change-password` → `ApiResponse<Void>`
- `/api/auth/refresh` → `ApiResponse<Map<String, String>>`

### T-12.05: تطبيق ApiResponse في ShipmentController
- جميع endpoints ترجع `ApiResponse<ShipmentResponseDTO>` أو `ApiResponse<List<ShipmentResponseDTO>>`
- Pagination: `ApiPageResponse<ShipmentResponseDTO>`

### T-12.06: تطبيق ApiResponse في UserController
- `/api/users`, `/api/couriers`, `/api/merchants`, `/api/employees`
- جميعها تستخدم `ApiPageResponse<UserResponseDTO>`

### T-12.07: تطبيق ApiResponse في Controllers المتبقية
- FinancialController → `ApiResponse<FinancialResponseDTO>`
- ManifestController → `ApiResponse<ManifestResponseDTO>`
- ReportsController → `ApiResponse<ReportDTO>` / `ApiResponse<DashboardStatsDTO>`
- SettingsController → `ApiResponse<Map<String, String>>`

### T-12.08: تحديث Frontend api_service.js
- تعديل `handleResponse()` لتوقع format الجديد `{success, message, data, errors}`
- استخراج `response.data` تلقائياً
- عرض `response.message` في التنبيهات

---

## WP-2: Request DTOs الكاملة + Validation (8 مهام)

### T-12.09: توحيد inner-class DTOs → package DTOs
- نقل `FinancialController.CreatePayoutRequest` → `dto/CreatePayoutRequest.java`
- نقل `ManifestController.CreateManifestRequest` → `dto/CreateManifestRequest.java`
- نقل `UserController.CreateUserRequest/UpdateUserRequest` → `dto/`
- حذف الـ inner classes الأصلية

### T-12.10: إنشاء DTOs مفقودة لبقية الـ Controllers
- `CreateShipmentRequest` (recipientName, recipientPhone, recipientAddress, zoneId, deliveryFee, notes, priority)
- `UpdateShipmentStatusRequest` (status, notes)
- `AssignCourierRequest` (courierId)
- `CreateZoneRequest` / `UpdateZoneRequest`

### T-12.11: إضافة Bean Validation على كل DTO
```java
@NotBlank(message = "اسم المستلم مطلوب")
private String recipientName;

@Pattern(regexp = "^(\\+20|0)?1[0-2,5][0-9]{8}$", message = "رقم الهاتف غير صحيح")
private String recipientPhone;

@Min(value = 0, message = "رسوم التوصيل يجب أن تكون أكبر من صفر")
private BigDecimal deliveryFee;
```

### T-12.12: تطبيق `@Valid` في Controllers
- إضافة `@Valid @RequestBody` على كل endpoint يقبل DTO
- التأكد من أن `GlobalExceptionHandler` يلتقط validation errors

### T-12.13: إنشاء Response DTOs المفقودة
- `ZoneResponseDTO` (id, name, description, deliveryFee, active)
- `AuditLogResponseDTO` (id, action, user, details, timestamp)
- `NotificationResponseDTO` (id, message, type, read, createdAt)
- `BackupResponseDTO` (id, filename, size, status, createdAt)

### T-12.14: Entity → DTO mapping
- إنشاء `DtoMapper.java` utility class مع static methods:
  - `toUserDTO(User)`, `toShipmentDTO(Shipment)`, `toZoneDTO(Zone)`
  - `toManifestDTO(ShipmentManifest)`, `toPayoutDTO(Payout)`
- أو استخدام constructor-based mapping في الـ DTOs أنفسهم

### T-12.15: تطبيق password validation
- إنشاء `@ValidPassword` custom annotation
- كلمة المرور: 8+ أحرف، حرف كبير، حرف صغير، رقم، رمز خاص
- تطبيق على `PasswordResetRequest`, `ChangePasswordRequest`, `CreateUserRequest`

### T-12.16: إضافة request body size limits
- تكوين `spring.servlet.multipart.max-file-size=10MB`
- تكوين `spring.servlet.multipart.max-request-size=10MB`
- تطبيق على file upload endpoints

---

## WP-3: إصلاح getCurrentUser() + Authorization (7 مهام)

### T-12.17: توحيد `getCurrentUser()` pattern
- استبدال `(User) auth.getPrincipal()` في كل Controllers
- توحيد على: `auth.getName()` → `userRepository.findByPhone(phone)`
- إنشاء `AuthenticationHelper` utility class

### T-12.18: إنشاء `AuthenticationHelper.java`
```java
@Component
public class AuthenticationHelper {
    private final UserRepository userRepository;
    
    public User getCurrentUser(Authentication auth) { ... }
    public Long getCurrentUserId(Authentication auth) { ... }
    public String getCurrentUserRole(Authentication auth) { ... }
    public boolean hasRole(Authentication auth, String role) { ... }
}
```

### T-12.19: تطبيق `@PreAuthorize` على كل Controller
- التأكد من أن كل endpoint لها `@PreAuthorize` مناسب
- Owner endpoints: `@PreAuthorize("hasRole('OWNER')")`
- Merchant endpoints: `@PreAuthorize("hasAnyRole('MERCHANT', 'OWNER')")`
- Courier endpoints: `@PreAuthorize("hasAnyRole('COURIER', 'OWNER')")`

### T-12.20: تطبيق account lockout
- إضافة `failedLoginAttempts` و `lockedUntil` في `User` entity
- بعد 5 محاولات فاشلة → قفل 15 دقيقة
- إصلاح في `AuthController.login()` و/أو `JwtAuthenticationFilter`

### T-12.21: إضافة audit logging للعمليات الحساسة
- `AuditService.log()` عند: login/logout, password change, user create/delete, shipment status change
- تسجيل IP address + User-Agent

### T-12.22: Token blacklist/revocation
- Redis-based token blacklist
- عند logout → إضافة token للـ blacklist مع TTL = remaining expiry
- `JwtAuthenticationFilter` يفحص blacklist قبل القبول

### T-12.23: تحديث اختبارات Controller لـ ApiResponse format
- تحديث assertions في جميع test files لتوقع `$.success`, `$.data`, `$.message`
- التأكد من أن جميع الاختبارات تنجح مع الـ wrapper الجديد

---

## WP-4: اختبارات Sprint 12 (7 مهام)

### T-12.24: اختبارات GlobalExceptionHandler
- 6 tests: كل نوع exception يرجع status code والـ format الصحيح

### T-12.25: اختبارات Validation
- 4 tests: validation errors على DTOs مختلفة
- تأكيد أن الرسائل العربية تظهر في الاستجابة

### T-12.26: اختبارات AuthenticationHelper
- 3 tests: getCurrentUser, hasRole, null handling

### T-12.27: اختبارات Account Lockout
- 3 tests: lockout بعد 5 محاولات، unlock بعد الوقت، reset بعد login ناجح

### T-12.28: اختبارات DtoMapper
- 5 tests: تحويل كل entity رئيسي لـ DTO والتأكد من القيم

### T-12.29: اختبارات Token Blacklist
- 3 tests: إضافة token للـ blacklist، فحص token مرفوض، انتهاء TTL

### T-12.30: Integration smoke tests
- 4 tests: سيناريوهات كاملة (login → create shipment → update status → logout)

---

## معايير القبول
- [ ] كل endpoint ترجع `ApiResponse<T>` format
- [ ] صفر `Map<String,Object>` في request/response bodies
- [ ] كل DTO لها `@Valid` + validation annotations
- [ ] `GlobalExceptionHandler` يلتقط كل أنواع الأخطاء
- [ ] `getCurrentUser()` موحد عبر كل Controllers
- [ ] Account lockout فعّال
- [ ] Token blacklist فعّال
- [ ] 150+ tests, 0 failures, BUILD SUCCESS
