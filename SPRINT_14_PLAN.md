# Sprint 14 — ميزات جديدة: رفع جماعي، إثبات التسليم، تقييم الكوريير، وباركود AWB
**المهام: 30 | حزم العمل: 4**

> **الهدف:** تنفيذ أهم ميزات Phase 1 من الخريطة: Bulk Upload، Proof of Delivery، Courier Rating، Barcode/AWB، والمرتجعات.

---

## WP-1: Bulk Shipment Upload (8 مهام)

### T-14.01: إنشاء Excel/CSV Parser Service
- إضافة Apache POI dependency لـ Excel
- `BulkUploadService.java`:
  - `parseExcel(MultipartFile)` → `List<BulkShipmentRow>`
  - `parseCsv(MultipartFile)` → `List<BulkShipmentRow>`
  - Validation لكل row: required fields, phone format, zone name exists
  - Return: `BulkParseResult {validRows[], invalidRows[], errors[]}`

### T-14.02: BulkShipmentRow DTO + Validation
```java
public class BulkShipmentRow {
    private int rowNumber;
    @NotBlank private String recipientName;
    @NotBlank private String recipientPhone;
    @NotBlank private String recipientAddress;
    private String zoneName;
    private BigDecimal codAmount; // Cash on Delivery
    private String notes;
    private String priority; // NORMAL, URGENT
    private List<String> errors; // validation errors per row
}
```

### T-14.03: Bulk Upload Controller
- `POST /api/shipments/bulk/validate` → يرسل الملف، يرجع preview مع الأخطاء
  - Response: `ApiResponse<BulkValidationResult>` (validCount, invalidCount, rows[])
- `POST /api/shipments/bulk/confirm` → يؤكد الإنشاء بعد المعاينة
  - Response: `ApiResponse<BulkCreateResult>` (createdCount, failedCount, shipmentIds[])

### T-14.04: Template Download
- `GET /api/shipments/bulk/template?format=xlsx` → تحميل قالب Excel
- `GET /api/shipments/bulk/template?format=csv` → تحميل قالب CSV
- القالب يحتوي: headers عربية + إنجليزية، صف مثال، sheet ثاني بأسماء المناطق

### T-14.05: Frontend — صفحة الرفع الجماعي
- إنشاء `merchant/bulk-upload.html` + `bulk-upload-page.js`
- Drag-and-drop zone لرفع الملف
- تحميل القالب (Excel/CSV)
- Progress bar أثناء التحميل

### T-14.06: Frontend — معاينة وتأكيد
- جدول معاينة يعرض كل الصفوف
- الصفوف الصحيحة ✅ بخلفية خضراء
- الصفوف الخاطئة ❌ بخلفية حمراء مع رسائل الأخطاء
- زر "إنشاء الشحنات الصحيحة" + "تصحيح ورفع مرة أخرى"

### T-14.07: Bulk Upload Rate Limiting
- Max 500 shipments per upload
- Max 3 uploads per merchant per hour
- Max file size: 5MB
- تنظيف الملفات المؤقتة بعد المعالجة

### T-14.08: Bulk Upload History
- تسجيل كل عملية رفع: merchantId, filename, totalRows, validRows, timestamp
- `GET /api/shipments/bulk/history` → تاريخ الرفع الجماعي للتاجر

---

## WP-2: Proof of Delivery (POD) + إثبات التسليم (7 مهام)

### T-14.09: DeliveryProof Entity
```java
@Entity
public class DeliveryProof {
    private Long id;
    private Long shipmentId;
    private Long courierId;
    @Lob private byte[] signatureImage; // توقيع المستلم
    private String photoUrl; // صورة التسليم
    private String recipientIdNumber; // رقم الهوية (اختياري)
    private String notes;
    @Enumerated(EnumType.STRING)
    private DeliveryProofType type; // SIGNATURE, PHOTO, BOTH
    private Double latitude;
    private Double longitude;
    private Instant capturedAt;
}
```

### T-14.10: DeliveryProof Service + Controller
- `POST /api/shipments/{id}/proof` (multipart: signature image + photo + metadata)
- `GET /api/shipments/{id}/proof` → `ApiResponse<DeliveryProofDTO>`
- Business rule: لا يمكن تسجيل POD إلا عندما تكون الشحنة في حالة `OUT_FOR_DELIVERY` أو `DELIVERED`
- الكوريير المعيّن فقط يمكنه تسجيل POD

### T-14.11: File Upload Service
- `FileStorageService.java`: تخزين الملفات في `uploads/` directory
- دعم: JPEG, PNG, max 5MB per image
- Image compression: resize to max 1024x1024
- تنظيف: حذف الملفات الأقدم من 90 يوم (scheduled task)

### T-14.12: Signature Capture (Frontend — Courier)
- إضافة Canvas-based signature capture في courier workflow
- عند تأكيد التسليم → نافذة التوقيع
- زر "التقاط صورة" لأخذ صورة من الكاميرا
- الموقع الجغرافي يُسجل تلقائياً

### T-14.13: عرض POD (Frontend — Merchant)
- في صفحة `shipment-details.html`:
  - عرض صورة التسليم
  - عرض التوقيع
  - عرض الموقع على خريطة صغيرة
  - وقت التسليم الدقيق

### T-14.14: POD Requirements Configuration
- Owner يحدد: هل POD مطلوب لكل الشحنات أم اختياري
- إعداد في `/api/settings`: `pod_required` (true/false), `pod_type` (SIGNATURE/PHOTO/BOTH)
- الكوريير لا يمكنه إكمال التسليم بدون POD إذا كان مطلوباً

### T-14.15: POD Reports
- إضافة في تقارير Owner: نسبة الشحنات مع POD، شحنات بدون POD
- تصفية: شحنات مسلمة بدون إثبات → تنبيه

---

## WP-3: Courier Rating + Barcode/AWB (8 مهام)

### T-14.16: CourierRating Entity
```java
@Entity
public class CourierRating {
    private Long id;
    private Long shipmentId;
    private Long courierId;
    private Long ratedByUserId; // Merchant or system
    @Min(1) @Max(5) private int rating;
    private String comment;
    @Enumerated(EnumType.STRING)
    private RatingType type; // DELIVERY_SPEED, COMMUNICATION, PACKAGE_HANDLING
    private Instant createdAt;
}
```

### T-14.17: Rating Service + Controller
- `POST /api/shipments/{id}/rate` → `{rating, comment, types[]}` (Merchant يقيّم بعد التسليم)
- `GET /api/couriers/{id}/ratings` → `ApiPageResponse<CourierRatingDTO>`
- `GET /api/couriers/{id}/rating-summary` → `{avgRating, totalRatings, breakdown[5]}`
- Business rule: تقييم واحد فقط لكل شحنة

### T-14.18: Auto-rating triggers
- شحنة مسلمة في الوقت → auto 4 stars
- شحنة متأخرة → auto 3 stars
- شحنة مرتجعة بسبب الكوريير → auto 2 stars
- التاجر يمكنه تعديل التقييم التلقائي

### T-14.19: Rating display في لوحات التحكم
- Owner dashboard: ترتيب الكوريير حسب التقييم
- Courier dashboard: عرض متوسط التقييم + آخر التقييمات
- Merchant: عرض تقييم الكوريير المعيّن في تفاصيل الشحنة

### T-14.20: AWB (Air Waybill) Number Generation
- `AwbService.java`:
  - Format: `TWS-{YYYYMMDD}-{6-digit-sequence}` (مثال: TWS-20250101-000001)
  - يتم توليده تلقائياً عند إنشاء الشحنة
  - فريد unique constraint في database
  - Checksum digit للتحقق

### T-14.21: Barcode Generation
- إضافة ZXing dependency
- `BarcodeService.java`:
  - `generateBarcode(awbNumber)` → `byte[]` PNG image (Code128)
  - `generateQRCode(shipmentData)` → `byte[]` PNG image
- `GET /api/shipments/{id}/barcode` → صورة الباركود
- `GET /api/shipments/{id}/qrcode` → صورة QR

### T-14.22: AWB Sticker/Label Print
- `GET /api/shipments/{id}/label` → HTML label (100mm × 150mm) ready for print
- يحتوي: باركود، AWB number، اسم المرسل، اسم المستلم، العنوان، المنطقة، رسوم التوصيل
- `GET /api/shipments/labels?ids=1,2,3` → batch labels (A4 paper, 4 per page)

### T-14.23: Barcode Scanner (Frontend — Courier)
- إضافة `barcode-scanner.js` باستخدام camera API
- الكوريير يمسح الباركود → يحمّل تفاصيل الشحنة
- Fallback: إدخال AWB يدوي
- ربط بـ manifest page: مسح → تأكيد استلام

---

## WP-4: المرتجعات + اختبارات Sprint 14 (7 مهام)

### T-14.24: Returns Management
- إضافة حالات جديدة: `RETURN_REQUESTED`, `RETURN_IN_TRANSIT`, `RETURNED`
- `POST /api/shipments/{id}/return` → التاجر يطلب إرجاع
- `PUT /api/shipments/{id}/return/accept` → Owner/Admin يقبل
- Business rules: لا يمكن الإرجاع بعد 7 أيام من التسليم

### T-14.25: Returns Frontend (Merchant)
- إضافة زر "طلب إرجاع" في shipment-details.html (بعد التسليم)
- تصفية جديدة في shipments.html: "المرتجعات"
- عرض حالة الإرجاع في التفاصيل

### T-14.26: Returns Reports
- تقارير المرتجعات: عدد، أسباب، نسبة من إجمالي الشحنات
- تقرير لكل تاجر: معدل الإرجاع (flag if > 15%)

### T-14.27: اختبارات Bulk Upload
- 6 tests: parse Excel, parse CSV, validation, confirm, rate limiting, history

### T-14.28: اختبارات POD
- 5 tests: create proof, get proof, required validation, unauthorized courier, file upload

### T-14.29: اختبارات Rating
- 5 tests: create rating, duplicate prevention, rating summary, auto-rating, GET ratings

### T-14.30: اختبارات AWB + Returns
- 4 tests: AWB generation, barcode generation, return request, return lifecycle

---

## معايير القبول
- [ ] Bulk upload يعمل مع Excel و CSV (حتى 500 شحنة)
- [ ] معاينة الشحنات قبل التأكيد مع validation
- [ ] POD يعمل: توقيع + صورة + موقع جغرافي
- [ ] POD مطلوب/اختياري حسب إعدادات Owner
- [ ] Courier rating يعمل (يدوي + تلقائي)
- [ ] AWB يتولد تلقائياً لكل شحنة
- [ ] Barcode + QR code generation يعمل
- [ ] طباعة labels (فردي + مجمّع)
- [ ] Barcode scanner يعمل من الموبايل
- [ ] نظام المرتجعات يعمل مع lifecycle كامل
- [ ] 200+ tests, 0 failures, BUILD SUCCESS
