# خطة تحسين الواجهة الأمامية

## 1. دمج وتنظيم ملفات CSS

### إنشاء نظام تصميم موحد
```scss
src/assets/css/
├── base/                  # الأنماط الأساسية
│   ├── _reset.css        # إعادة تعيين CSS
│   ├── _typography.css   # الخطوط والنصوص
│   ├── _variables.css    # المتغيرات
│   └── _rtl.css         # دعم اللغة العربية
├── components/           # مكونات واجهة المستخدم
├── layouts/             # تخطيطات الصفحات
└── main.css            # تجميع كل الملفات
```

## 2. تنظيم المكونات المشتركة

### إنشاء مكتبة مكونات
```javascript
src/components/
├── forms/               # نماذج الإدخال
│   ├── ShipmentForm/
│   ├── LoginForm/
│   └── SearchForm/
├── tables/             # جداول البيانات
│   ├── ShipmentsTable/
│   └── EmployeesTable/
└── ui/                # عناصر واجهة المستخدم
    ├── Button/
    ├── Card/
    └── Modal/
```

## 3. توحيد نمط الصفحات

### هيكل موحد للصفحات
```html
<!-- نموذج لهيكل الصفحات -->
<!DOCTYPE html>
<html dir="rtl" lang="ar">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="stylesheet" href="/assets/css/main.css">
    <title>Twsela - {PAGE_TITLE}</title>
</head>
<body class="page-{ROLE}">
    <header class="app-header"></header>
    <nav class="app-nav"></nav>
    <main class="app-main">
        <div class="page-content"></div>
    </main>
    <footer class="app-footer"></footer>
</body>
</html>
```

## 4. إدارة الحالة والخدمات

### تنظيم الخدمات
```javascript
src/services/
├── api/                 # خدمات API
│   ├── auth.js
│   ├── shipments.js
│   └── users.js
├── state/              # إدارة الحالة
│   ├── store.js
│   └── actions.js
└── utils/             # أدوات مساعدة
    ├── validation.js
    └── formatting.js
```

## 5. التحسينات التقنية

1. **تحسين الأداء**:
   - دمج وضغط ملفات CSS/JS
   - تحسين تحميل الصور
   - تنفيذ التحميل الكسول للمكونات

2. **تحسين SEO**:
   - تحسين العناوين ووصف الصفحات
   - إضافة خريطة الموقع
   - تحسين سرعة التحميل

3. **تحسين الأمان**:
   - تنفيذ CSP (Content Security Policy)
   - حماية النماذج من CSRF
   - تشفير البيانات الحساسة

## 6. خطة التنفيذ

1. **المرحلة الأولى** (1-2 أسابيع):
   - إعادة تنظيم هيكل المشروع
   - دمج ملفات CSS
   - إنشاء المكونات المشتركة

2. **المرحلة الثانية** (2-3 أسابيع):
   - تحديث صفحات التطبيق
   - تنفيذ نظام إدارة الحالة
   - تحسين الخدمات

3. **المرحلة الثالثة** (1-2 أسابيع):
   - اختبار وتصحيح الأخطاء
   - تحسين الأداء
   - توثيق التغييرات

## 7. متطلبات التنفيذ

1. **الأدوات**:
   - Webpack/Vite للبناء
   - SASS/SCSS لـ CSS
   - ESLint للتنسيق
   - Jest للاختبار

2. **الوثائق**:
   - دليل النمط
   - وثائق المكونات
   - دليل المساهمة