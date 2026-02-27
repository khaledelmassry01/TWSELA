package com.twsela.web;

/**
 * Centralized error and success message constants for all controllers.
 * All user-facing messages should be defined here to make them i18n-ready.
 *
 * جميع الرسائل الموجهة للمستخدم معرفة هنا لتسهيل الترجمة مستقبلاً.
 */
public final class ErrorMessages {

    private ErrorMessages() {
        // Utility class — no instances
    }

    // ── Authentication & Security ────────────────────────────────
    public static final String USER_NOT_AUTHENTICATED = "User not authenticated";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String NOT_AUTHENTICATED = "Not authenticated";
    public static final String USER_NOT_FOUND_BY_PHONE = "المستخدم غير موجود برقم الهاتف هذا";
    public static final String PASSWORD_MISMATCH = "كلمة السر وتأكيد كلمة السر غير متطابقين";
    public static final String PASSWORD_CHANGED_SUCCESS = "تم تغيير كلمة السر بنجاح";
    public static final String PASSWORD_MIN_LENGTH = "كلمة المرور الجديدة يجب أن تكون 6 أحرف على الأقل";
    public static final String CURRENT_PASSWORD_INCORRECT = "كلمة المرور الحالية غير صحيحة";
    public static final String PASSWORD_CHANGED_SUCCESSFULLY = "تم تغيير كلمة المرور بنجاح";

    // ── OTP ───────────────────────────────────────────────────────
    public static final String OTP_SENT = "تم إرسال رمز التحقق إلى هاتفك";
    public static final String OTP_SENT_WITH_RESET_INSTRUCTIONS = "تم إرسال رمز التحقق إلى هاتفك. استخدمه لإعادة تعيين كلمة المرور.";
    public static final String OTP_SENT_DEV_MODE = "تم إرسال رمز التحقق (تحقق من console في وضع التطوير)";
    public static final String OTP_INVALID = "رمز التحقق غير صحيح أو منتهي الصلاحية";

    // ── Shipments ────────────────────────────────────────────────
    public static final String SHIPMENT_NOT_FOUND = "الشحنة غير موجودة";
    public static final String SHIPMENT_CREATED = "تم إنشاء الشحنة بنجاح";
    public static final String ZONE_NOT_FOUND = "المنطقة غير موجودة - Zone ID: ";
    public static final String STATUS_NOT_FOUND = "حالة الشحنة غير موجودة في النظام: ";
    public static final String RETURN_REASON_REQUIRED = "سبب الإرجاع مطلوب";
    public static final String SHIPMENT_NOT_ELIGIBLE_FOR_RETURN = "هذه الشحنة غير مؤهلة للإرجاع";
    public static final String RETURN_REQUEST_CREATED = "تم إنشاء طلب الإرجاع بنجاح";
    public static final String LOCATION_UPDATED = "تم تحديث الموقع بنجاح";
    public static final String SHIPMENTS_RETRIEVED = "Shipments retrieved successfully";

    // ── Warehouse ────────────────────────────────────────────────
    public static final String NO_TRACKING_NUMBERS_PROVIDED = "No tracking numbers provided";
    public static final String NO_SHIPMENT_IDS_PROVIDED = "No shipment IDs provided";
    public static final String INVALID_COURIER_ID = "Invalid courier ID";

    // ── Dashboard ────────────────────────────────────────────────
    public static final String UNAUTHORIZED_ROLE = "غير مصرح بالوصول لهذا الدور";

    // ── Contact ──────────────────────────────────────────────────
    public static final String CONTACT_FORM_SUCCESS = "تم إرسال رسالتك بنجاح! سنتواصل معك قريباً.";
    public static final String TRACKING_NUMBERS_REQUIRED = "أرقام التتبع مطلوبة";

    // ── Users ────────────────────────────────────────────────────
    public static final String NAME_REQUIRED = "الاسم مطلوب";
    public static final String PHONE_REQUIRED = "رقم الهاتف مطلوب";
    public static final String PASSWORD_REQUIRED = "كلمة المرور مطلوبة";
    public static final String ROLE_REQUIRED = "الدور مطلوب";
    public static final String ROLE_INVALID = "الدور غير صحيح";
    public static final String PROFILE_UPDATED = "تم تحديث الملف الشخصي بنجاح";
    public static final String COURIER_NOT_FOUND = "Courier not found";
    public static final String COURIER_CREATED = "تم إنشاء المندوب بنجاح";
    public static final String COURIER_UPDATED = "تم تحديث المندوب بنجاح";
    public static final String COURIER_DELETED = "تم حذف المندوب بنجاح";
    public static final String MERCHANT_NOT_FOUND = "Merchant not found";
    public static final String MERCHANT_CREATED = "تم إنشاء التاجر بنجاح";
    public static final String MERCHANT_UPDATED = "تم تحديث التاجر بنجاح";
    public static final String EMPLOYEE_NOT_FOUND = "Employee not found";
    public static final String EMPLOYEE_UPDATED = "تم تحديث الموظف بنجاح";
    public static final String EMPLOYEE_CREATED = "تم إنشاء الموظف بنجاح";
    public static final String NO_LOCATION_DATA = "No location data available";

    // ── Settings ─────────────────────────────────────────────────
    public static final String SETTINGS_SAVED = "تم حفظ الإعدادات بنجاح";
    public static final String SETTINGS_RESET = "تم إعادة تعيين الإعدادات بنجاح";

    // ── Notifications ────────────────────────────────────────────
    public static final String NOTIFICATION_NOT_FOUND = "Notification not found";
    public static final String NOTIFICATION_MARKED_READ = "تم تحديد الإشعار كمقروء";
    public static final String ALL_NOTIFICATIONS_MARKED_READ = "تم تحديد جميع الإشعارات كمقروءة";
}
