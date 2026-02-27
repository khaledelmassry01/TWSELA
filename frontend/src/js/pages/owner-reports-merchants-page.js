/**
 * Twsela CMS - Owner Reports Merchants Page
 * Handles merchant report data, charts, filtering, and export
 */

// Initialize merchants report page
document.addEventListener('DOMContentLoaded', function() {
    initializeMerchantsReportPage();
});

function initializeMerchantsReportPage() {
    loadMerchantsData();
    initializeCharts();
    setupEventListeners();
}

async function loadMerchantsData() {
    try {
        const merchants = await apiService.getMerchantsReport();
        updateMerchantsTable(merchants);
    } catch (error) {
        console.error('Error loading merchants data:', error);
    }
}

function updateMerchantsTable(merchants) {
    const tbody = document.querySelector('#merchantsTable tbody');
    if (!tbody) return;

    tbody.innerHTML = '';
    
    merchants.forEach(merchant => {
        const row = createMerchantRow(merchant);
        tbody.appendChild(row);
    });
}

function createMerchantRow(merchant) {
    const row = document.createElement('tr');
    row.innerHTML = `
        <td>
            <div class="d-flex align-items-center">
                <div class="avatar-sm bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-2">
                    ${merchant.name.charAt(0)}
                </div>
                <div>
                    <div class="fw-bold">${merchant.name}</div>
                    <small class="text-muted">ID: ${merchant.id}</small>
                </div>
            </div>
        </td>
        <td>${merchant.phone}</td>
        <td><span class="badge bg-${getStatusBadgeClass(merchant.status)}">${getStatusText(merchant.status)}</span></td>
        <td>${merchant.totalShipments}</td>
        <td>${merchant.completedShipments}</td>
        <td>${merchant.pendingShipments}</td>
        <td>${merchant.revenue} جنيه</td>
        <td>
            <div class="rating">
                ${generateStars(merchant.rating)}
                <span class="ms-1">${merchant.rating}</span>
            </div>
        </td>
        <td>
            <div class="action-buttons">
                <button class="action-btn view" data-id="${merchant.id}" title="عرض التفاصيل">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="action-btn edit" data-id="${merchant.id}" title="تعديل">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn chart" data-id="${merchant.id}" title="عرض الرسم البياني">
                    <i class="fas fa-chart-line"></i>
                </button>
            </div>
        </td>
    `;
    return row;
}

function getStatusBadgeClass(status) {
    const statusMap = {
        'ACTIVE': 'success',
        'INACTIVE': 'secondary',
        'SUSPENDED': 'warning'
    };
    return statusMap[status] || 'secondary';
}

function getStatusText(status) {
    const statusMap = {
        'ACTIVE': 'نشط',
        'INACTIVE': 'غير نشط',
        'SUSPENDED': 'معلق'
    };
    return statusMap[status] || status;
}

function generateStars(rating) {
    let stars = '';
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 !== 0;
    
    for (let i = 0; i < fullStars; i++) {
        stars += '<i class="fas fa-star text-warning"></i>';
    }
    
    if (hasHalfStar) {
        stars += '<i class="fas fa-star-half-alt text-warning"></i>';
    }
    
    const emptyStars = 5 - Math.ceil(rating);
    for (let i = 0; i < emptyStars; i++) {
        stars += '<i class="far fa-star text-warning"></i>';
    }
    
    return stars;
}

async function initializeCharts() {
    // Merchant Performance Chart
    const performanceCtx = document.getElementById('merchantPerformanceChart');
    if (performanceCtx) {
        const performanceData = await loadMerchantsPerformanceData();
        
        new Chart(performanceCtx, {
            type: 'bar',
            data: {
                labels: performanceData.labels || [],
                datasets: [{
                    label: 'الشحنات المنجزة',
                    data: performanceData.completedShipments || [],
                    backgroundColor: '#3b82f6',
                    borderColor: '#2563eb',
                    borderWidth: 1
                }, {
                    label: 'الإيرادات (×100)',
                    data: performanceData.revenues || [],
                    backgroundColor: '#10b981',
                    borderColor: '#059669',
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { beginAtZero: true } },
                plugins: { legend: { position: 'top' } }
            }
        });
    }

    // Merchant Distribution Chart
    const distributionCtx = document.getElementById('merchantDistributionChart');
    if (distributionCtx) {
        const distributionData = await loadMerchantsDistributionData();
        
        new Chart(distributionCtx, {
            type: 'doughnut',
            data: {
                labels: distributionData.labels || [],
                datasets: [{
                    data: distributionData.values || [],
                    backgroundColor: ['#10b981', '#6b7280', '#f59e0b'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom' } }
            }
        });
    }

    // Revenue Chart
    const revenueCtx = document.getElementById('revenueChart');
    if (revenueCtx) {
        const revenueData = await loadMerchantsRevenueData();
        
        new Chart(revenueCtx, {
            type: 'line',
            data: {
                labels: revenueData.labels || [],
                datasets: [{
                    label: 'الإيرادات (جنيه)',
                    data: revenueData.values || [],
                    borderColor: '#8b5cf6',
                    backgroundColor: 'rgba(139, 92, 246, 0.1)',
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { beginAtZero: true } },
                plugins: { legend: { position: 'top' } }
            }
        });
    }
}

// API Data Loading Functions
async function loadMerchantsPerformanceData() {
    try {
        const response = await window.apiService.getMerchantsPerformanceData();
        if (response.success) {
            return response.data;
        }
    } catch (error) {
        // Return empty data on error
    }
    return { labels: [], completedShipments: [], revenues: [] };
}

async function loadMerchantsDistributionData() {
    try {
        const response = await window.apiService.getMerchantsDistributionData();
        if (response.success) {
            return response.data;
        }
    } catch (error) {
        // Return empty data on error
    }
    return { labels: [], values: [] };
}

async function loadMerchantsRevenueData() {
    try {
        const response = await window.apiService.getMerchantsRevenueData();
        if (response.success) {
            return response.data;
        }
    } catch (error) {
        // Return empty data on error
    }
    return { labels: [], values: [] };
}

function setupEventListeners() {
    document.addEventListener('click', function(e) {
        if (e.target.closest('.action-btn.view')) {
            const id = e.target.closest('.action-btn').getAttribute('data-id');
            viewMerchantDetails(id);
        } else if (e.target.closest('.action-btn.edit')) {
            const id = e.target.closest('.action-btn').getAttribute('data-id');
            editMerchant(id);
        } else if (e.target.closest('.action-btn.chart')) {
            const id = e.target.closest('.action-btn').getAttribute('data-id');
            showMerchantChart(id);
        }
    });
}

function viewMerchantDetails(id) {
    const modal = new bootstrap.Modal(document.getElementById('merchantDetailsModal'));
    modal.show();
}

function editMerchant(id) {
    // Edit merchant implementation
}

function showMerchantChart(id) {
    // Show merchant chart implementation
}

function applyFilters() {
    const filters = {
        dateFrom: document.getElementById('dateFrom').value,
        dateTo: document.getElementById('dateTo').value,
        merchant: document.getElementById('merchantSelect').value,
        status: document.getElementById('statusSelect').value
    };
    loadMerchantsData();
}

function clearFilters() {
    document.getElementById('dateFrom').value = '';
    document.getElementById('dateTo').value = '';
    document.getElementById('merchantSelect').value = '';
    document.getElementById('statusSelect').value = '';
    loadMerchantsData();
}

function exportReport(format) {
    showNotification(`جاري تصدير التقرير بصيغة ${format.toUpperCase()}...`, 'info');
}

function exportTable() {
    // Export table implementation
}

function refreshReport() {
    loadMerchantsData();
}

function showNotification(message, type) {
    if (typeof NotificationService !== 'undefined') {
        NotificationService.show(message, type);
    }
}
