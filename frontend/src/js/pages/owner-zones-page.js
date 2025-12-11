/**
 * Twsela CMS - Owner Zones Page Initialization
 * Handles page-specific initialization and legacy functions
 * Follows DRY principle with clean separation of concerns
 */

let currentZoneId = null;

// Initialize zones page
document.addEventListener('DOMContentLoaded', function() {
    // انتظار تحميل app.js أولاً لتجنب التضارب
    waitForAppInitialization();
});

async function waitForAppInitialization() {
    let attempts = 0;
    const maxAttempts = 50; // 5 seconds max
    
    while (attempts < maxAttempts) {
        // فحص وجود app.js
        if (window.twselaApp && window.twselaApp.isInitialized) {
            console.log('✅ App.js is initialized, proceeding with zones page');
            await initializeZonesPage();
            return;
        }
        
        // فحص وجود الخدمات المطلوبة
        if (window.authService && window.apiService) {
            console.log('✅ Services are available, proceeding with zones page');
            await initializeZonesPage();
            return;
        }
        
        await new Promise(resolve => setTimeout(resolve, 100));
        attempts++;
    }
    
    console.warn('⚠️ App.js not initialized after timeout, proceeding anyway');
    await initializeZonesPage();
}

async function initializeZonesPage() {
    try {
        // فحص المصادقة أولاً
        if (!await checkAuthentication()) {
            return;
        }
        
        // Load zones data
        await loadZonesData();
        
        // Initialize charts
        initializeCharts();
        
        // Setup event listeners
        setupEventListeners();
        
        console.log('✅ Zones page initialized successfully');
    } catch (error) {
        console.error('❌ Error initializing zones page:', error);
        showNotification('خطأ في تحميل صفحة المناطق', 'error');
    }
}

/**
 * فحص المصادقة قبل تحميل الصفحة
 */
async function checkAuthentication() {
    try {
        // فحص وجود خدمة المصادقة
        if (!window.authService) {
            console.error('❌ AuthService not available');
            window.location.href = '/login.html';
            return false;
        }
        
        // فحص التوكن المحلي أولاً لتجنب استدعاء auth/me غير الضروري
        const token = window.authService.getToken();
        if (!token) {
            console.warn('⚠️ No authentication token found');
            window.location.href = '/login.html';
            return false;
        }
        
        // فحص بيانات المستخدم المحلية أولاً
        const user = window.authService.getCurrentUser();
        if (user && user.role) {
            // فحص الصلاحيات من البيانات المحلية
            if (!['OWNER', 'ADMIN'].includes(user.role)) {
                console.warn('⚠️ User does not have permission to access zones page');
                window.location.href = '/login.html';
                return false;
            }
            console.log('✅ Authentication verified from local data');
            return true;
        }
        
        // فحص إذا كان app.js قد قام بفحص المصادقة بالفعل
        if (window.twselaApp && window.twselaApp.isInitialized) {
            console.log('✅ App.js has already verified authentication');
            return true;
        }
        
        // فحص إذا كان هناك فحص مصادقة جاري بالفعل
        if (window.authCheckInProgress) {
            console.log('⏳ Auth check already in progress, waiting...');
            // انتظار انتهاء الفحص الحالي
            let attempts = 0;
            while (window.authCheckInProgress && attempts < 50) {
                await new Promise(resolve => setTimeout(resolve, 100));
                attempts++;
            }
            
            // فحص النتيجة بعد الانتظار
            const finalUser = window.authService.getCurrentUser();
            if (finalUser && finalUser.role && ['OWNER', 'ADMIN'].includes(finalUser.role)) {
                console.log('✅ Authentication verified after waiting');
                return true;
            }
        }
        
        // تعيين علامة أن فحص المصادقة جاري
        window.authCheckInProgress = true;
        
        try {
            // فقط إذا لم تكن البيانات المحلية متاحة، استدعاء auth/me
            console.log('🔄 Verifying authentication with server...');
            const isValid = await window.authService.checkAuthStatus();
            if (!isValid) {
                console.warn('⚠️ User not authenticated, redirecting to login');
                window.location.href = '/login.html';
                return false;
            }
            
            // فحص الصلاحيات بعد التحقق من الخادم
            const updatedUser = window.authService.getCurrentUser();
            if (!updatedUser || !['OWNER', 'ADMIN'].includes(updatedUser.role)) {
                console.warn('⚠️ User does not have permission to access zones page');
                window.location.href = '/login.html';
                return false;
            }
            
            console.log('✅ Authentication verified successfully');
            return true;
        } finally {
            // إزالة علامة فحص المصادقة
            window.authCheckInProgress = false;
        }
    } catch (error) {
        window.authCheckInProgress = false;
        console.error('❌ Authentication check failed:', error);
        window.location.href = '/login.html';
        return false;
    }
}

async function loadZonesData() {
    try {
        // فحص وجود خدمة API
        if (!window.apiService) {
            throw new Error('API service not available');
        }
        
        // فحص المصادقة مرة أخرى قبل الطلب
        if (!window.authService || !window.authService.isAuthenticated()) {
            throw new Error('User not authenticated');
        }
        
        console.log('🔄 Loading zones data...');
        const response = await window.apiService.getZones();
        
        if (response.success) {
            updateZonesTable(response.data || []);
            console.log('✅ Zones data loaded successfully');
        } else {
            throw new Error(response.message || 'Failed to load zones data');
        }
    } catch (error) {
        console.error('❌ Error loading zones data:', error);
        
        // إذا كان الخطأ متعلق بالمصادقة، توجيه لصفحة تسجيل الدخول
        if (error.message.includes('authentication') || 
            error.message.includes('401') || 
            error.message.includes('not authenticated')) {
            console.warn('⚠️ Authentication error, redirecting to login');
            window.location.href = '/login.html';
            return;
        }
        
        // عرض رسالة خطأ للمستخدم
        showNotification('خطأ في تحميل بيانات المناطق: ' + error.message, 'error');
        
        // عرض جدول فارغ مع رسالة خطأ
        updateZonesTable([]);
    }
}

function updateZonesTable(zones) {
    const tbody = document.querySelector('#zonesTable tbody');
    if (!tbody) {
        console.error('❌ Zones table body not found');
        return;
    }

    tbody.innerHTML = '';
    
    if (!zones || zones.length === 0) {
        // عرض رسالة "لا توجد بيانات"
        const emptyRow = document.createElement('tr');
        emptyRow.innerHTML = `
            <td colspan="5" class="text-center py-4">
                <div class="text-muted">
                    <i class="fas fa-info-circle me-2"></i>
                    لا توجد مناطق متاحة
                </div>
            </td>
        `;
        tbody.appendChild(emptyRow);
        return;
    }
    
    zones.forEach(zone => {
        const row = createZoneRow(zone);
        tbody.appendChild(row);
    });
    
    console.log(`✅ Updated zones table with ${zones.length} zones`);
}

function createZoneRow(zone) {
    const row = document.createElement('tr');
    if (window.GlobalUIHandler) {
        row.innerHTML = window.GlobalUIHandler.createTableRow(zone, 'zone');
    } else {
        // Fallback if GlobalUIHandler is not available
        row.innerHTML = `
            <td>${zone.name || 'غير محدد'}</td>
            <td>${zone.status || 'غير محدد'}</td>
            <td>${zone.deliveryFee || '0'} جنيه</td>
            <td>${zone.couriers || '0'}</td>
            <td>
                <button class="btn btn-sm btn-primary" onclick="editZone(${zone.id})">تعديل</button>
                <button class="btn btn-sm btn-danger" onclick="deleteZone(${zone.id})">حذف</button>
            </td>
        `;
    }
    return row;
}

// Using unified DataUtils functions directly

function editZone(zoneId) {
    try {
        if (!zoneId) {
            throw new Error('Zone ID is required');
        }
        
        currentZoneId = zoneId;
        console.log('🔄 Editing zone:', zoneId);
        
        // Use the handler's editZone method
        if (window.ownerZonesHandler) {
            window.ownerZonesHandler.editZone(zoneId);
        } else {
            // Fallback implementation
            const modal = document.getElementById('editZoneModal');
            if (modal) {
                const bootstrapModal = new bootstrap.Modal(modal);
                bootstrapModal.show();
            } else {
                throw new Error('Edit modal not found');
            }
        }
    } catch (error) {
        console.error('❌ Error editing zone:', error);
        showNotification('خطأ في تحميل بيانات المنطقة للتعديل', 'error');
    }
}

function viewZone(zoneId) {
    try {
        if (!zoneId) {
            throw new Error('Zone ID is required');
        }
        
        console.log('🔄 Viewing zone:', zoneId);
        
        // Use the handler's viewZone method
        if (window.ownerZonesHandler) {
            window.ownerZonesHandler.viewZone(zoneId);
        } else {
            // Fallback implementation
            showNotification('عرض تفاصيل المنطقة غير متاح حالياً', 'info');
        }
    } catch (error) {
        console.error('❌ Error viewing zone:', error);
        showNotification('خطأ في عرض تفاصيل المنطقة', 'error');
    }
}

function deleteZone(zoneId) {
    try {
        if (!zoneId) {
            throw new Error('Zone ID is required');
        }
        
        console.log('🔄 Deleting zone:', zoneId);
        
        // Use the handler's deleteZone method
        if (window.ownerZonesHandler) {
            window.ownerZonesHandler.deleteZone(zoneId);
        } else {
            // Fallback implementation with confirmation
            if (confirm('هل أنت متأكد من حذف هذه المنطقة؟ لا يمكن التراجع عن هذا الإجراء.')) {
                performDeleteZone(zoneId);
            }
        }
    } catch (error) {
        console.error('❌ Error deleting zone:', error);
        showNotification('خطأ في حذف المنطقة', 'error');
    }
}

async function performDeleteZone(zoneId) {
    try {
        if (!window.apiService) {
            throw new Error('API service not available');
        }
        
        const response = await window.apiService.deleteZone(zoneId);
        if (response.success) {
            showNotification('تم حذف المنطقة بنجاح', 'success');
            await loadZonesData(); // إعادة تحميل البيانات
        } else {
            throw new Error(response.message || 'Failed to delete zone');
        }
    } catch (error) {
        console.error('❌ Error performing delete:', error);
        showNotification('خطأ في حذف المنطقة: ' + error.message, 'error');
    }
}

function initializeCharts() {
    // Initialize any charts if needed
}

function filterZones(searchTerm) {
    try {
        const rows = document.querySelectorAll('#zonesTable tbody tr');
        let visibleCount = 0;
        
        if (!searchTerm || searchTerm.trim() === '') {
            // إظهار جميع الصفوف إذا كان البحث فارغاً
            rows.forEach(row => {
                row.style.display = '';
                visibleCount++;
            });
        } else {
            const searchLower = searchTerm.toLowerCase().trim();
            
            rows.forEach(row => {
                const text = row.textContent.toLowerCase();
                const matches = text.includes(searchLower);
                row.style.display = matches ? '' : 'none';
                
                if (matches) {
                    visibleCount++;
                }
            });
        }
        
        console.log(`🔍 Filter applied: "${searchTerm}" - ${visibleCount} results`);
        
        // تحديث معلومات الصفحة إذا كانت موجودة
        const paginationInfo = document.querySelector('.pagination-info');
        if (paginationInfo) {
            if (searchTerm && searchTerm.trim() !== '') {
                paginationInfo.textContent = `عرض ${visibleCount} نتيجة للبحث عن "${searchTerm}"`;
            } else {
                paginationInfo.textContent = `عرض جميع النتائج (${visibleCount})`;
            }
        }
        
    } catch (error) {
        console.error('❌ Error filtering zones:', error);
        showNotification('خطأ في البحث', 'error');
    }
}

function setupEventListeners() {
    try {
        console.log('🔄 Setting up event listeners...');
        
        // Setup event listeners for action buttons
        document.addEventListener('click', (e) => {
            try {
                if (e.target.closest('.action-btn')) {
                    const button = e.target.closest('.action-btn');
                    const zoneId = parseInt(button.dataset.zoneId);
                    const action = button.dataset.action;
                    
                    console.log(`🔄 Action button clicked: ${action} for zone ${zoneId}`);
                    
                    switch (action) {
                        case 'edit':
                            editZone(zoneId);
                            break;
                        case 'view':
                            viewZone(zoneId);
                            break;
                        case 'delete':
                            deleteZone(zoneId);
                            break;
                        default:
                            console.warn('⚠️ Unknown action:', action);
                    }
                }
            } catch (error) {
                console.error('❌ Error handling action button click:', error);
                showNotification('خطأ في تنفيذ الإجراء', 'error');
            }
        });

        // Setup search functionality
        const searchInput = document.querySelector('.search-input');
        if (searchInput) {
            searchInput.addEventListener('input', function(e) {
                try {
                    const searchTerm = e.target.value;
                    console.log('🔍 Searching for:', searchTerm);
                    filterZones(searchTerm);
                } catch (error) {
                    console.error('❌ Error in search:', error);
                }
            });
        }

        // Setup form submissions
        const addZoneBtn = document.getElementById('addZoneBtn');
        if (addZoneBtn) {
            addZoneBtn.addEventListener('click', function(e) {
                e.preventDefault();
                try {
                    addZone();
                } catch (error) {
                    console.error('❌ Error adding zone:', error);
                    showNotification('خطأ في إضافة المنطقة', 'error');
                }
            });
        }

        const updateZoneBtn = document.getElementById('updateZoneBtn');
        if (updateZoneBtn) {
            updateZoneBtn.addEventListener('click', function(e) {
                e.preventDefault();
                try {
                    updateZone();
                } catch (error) {
                    console.error('❌ Error updating zone:', error);
                    showNotification('خطأ في تحديث المنطقة', 'error');
                }
            });
        }
        
        console.log('✅ Event listeners setup completed');
    } catch (error) {
        console.error('❌ Error setting up event listeners:', error);
        showNotification('خطأ في إعداد الصفحة', 'error');
    }
}

async function addZone() {
    try {
        const form = document.getElementById('addZoneForm');
        if (!form) {
            throw new Error('Add zone form not found');
        }
        
        const formData = new FormData(form);
        const zoneData = {
            name: formData.get('zoneName'),
            code: formData.get('zoneCode'),
            deliveryFee: parseFloat(formData.get('deliveryFee')),
            status: formData.get('status'),
            description: formData.get('description'),
            latitude: parseFloat(formData.get('latitude')) || null,
            longitude: parseFloat(formData.get('longitude')) || null
        };
        
        // التحقق من صحة البيانات
        if (!zoneData.name || !zoneData.code) {
            throw new Error('اسم المنطقة ورمز المنطقة مطلوبان');
        }
        
        console.log('🔄 Adding new zone:', zoneData);
        
        if (!window.apiService) {
            throw new Error('API service not available');
        }
        
        const response = await window.apiService.createZone(zoneData);
        if (response.success) {
            showNotification('تم إضافة المنطقة بنجاح', 'success');
            
            // إغلاق النافذة وإعادة تعيين النموذج
            const modal = bootstrap.Modal.getInstance(document.getElementById('addZoneModal'));
            if (modal) {
                modal.hide();
            }
            form.reset();
            
            // إعادة تحميل البيانات
            await loadZonesData();
        } else {
            throw new Error(response.message || 'Failed to create zone');
        }
    } catch (error) {
        console.error('❌ Error adding zone:', error);
        showNotification('خطأ في إضافة المنطقة: ' + error.message, 'error');
    }
}

async function updateZone() {
    try {
        if (!currentZoneId) {
            throw new Error('No zone selected for update');
        }
        
        const form = document.getElementById('editZoneForm');
        if (!form) {
            throw new Error('Edit zone form not found');
        }
        
        const formData = new FormData(form);
        const zoneData = {
            name: formData.get('zoneName'),
            code: formData.get('zoneCode'),
            deliveryFee: parseFloat(formData.get('deliveryFee')),
            status: formData.get('status'),
            description: formData.get('description'),
            latitude: parseFloat(formData.get('latitude')) || null,
            longitude: parseFloat(formData.get('longitude')) || null
        };
        
        // التحقق من صحة البيانات
        if (!zoneData.name || !zoneData.code) {
            throw new Error('اسم المنطقة ورمز المنطقة مطلوبان');
        }
        
        console.log('🔄 Updating zone:', currentZoneId, zoneData);
        
        if (!window.apiService) {
            throw new Error('API service not available');
        }
        
        const response = await window.apiService.updateZone(currentZoneId, zoneData);
        if (response.success) {
            showNotification('تم تحديث المنطقة بنجاح', 'success');
            
            // إغلاق النافذة وإعادة تعيين النموذج
            const modal = bootstrap.Modal.getInstance(document.getElementById('editZoneModal'));
            if (modal) {
                modal.hide();
            }
            form.reset();
            currentZoneId = null;
            
            // إعادة تحميل البيانات
            await loadZonesData();
        } else {
            throw new Error(response.message || 'Failed to update zone');
        }
    } catch (error) {
        console.error('❌ Error updating zone:', error);
        showNotification('خطأ في تحديث المنطقة: ' + error.message, 'error');
    }
}

function showNotification(message, type = 'info') {
    try {
        // محاولة استخدام خدمة الإشعارات المتاحة
        if (window.notificationManager) {
            window.notificationManager.show({ message, type });
        } else if (window.NotificationService) {
            window.NotificationService.show(message, type);
        } else if (window.GlobalUIHandler && window.GlobalUIHandler.showNotification) {
            window.GlobalUIHandler.showNotification(message, type);
        } else {
            // استخدام alert كبديل
            console.log(`📢 ${type.toUpperCase()}: ${message}`);
            alert(message);
        }
    } catch (error) {
        console.error('❌ Error showing notification:', error);
        console.log(`📢 ${type.toUpperCase()}: ${message}`);
    }
}
