/**
 * Twsela CMS - Owner Reports Couriers Page
 * Handles courier report data, charts, filtering, and export
 */

// Initialize couriers report page
document.addEventListener('DOMContentLoaded', function() {
    initializeCouriersReportPage();
});

function initializeCouriersReportPage() {
    loadCouriersData();
    initializeCharts();
    setupEventListeners();
}

async function loadCouriersData() {
    try {
        const couriers = await apiService.getCouriersReport();
        updateCouriersTable(couriers);
    } catch (error) {
        console.error('Error loading couriers data:', error);
    }
}

function updateCouriersTable(couriers) {
    const tbody = document.querySelector('#couriersTable tbody');
    if (!tbody) return;

    tbody.innerHTML = '';
    
    couriers.forEach(courier => {
        const row = createCourierRow(courier);
        tbody.appendChild(row);
    });
}

function createCourierRow(courier) {
    const row = document.createElement('tr');
    row.innerHTML = `
        <td>
            <div class="d-flex align-items-center">
                <div class="avatar-sm bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-2">
                    ${courier.name.charAt(0)}
                </div>
                <div>
                    <div class="fw-bold">${courier.name}</div>
                    <small class="text-muted">ID: ${courier.id}</small>
                </div>
            </div>
        </td>
        <td>${courier.phone}</td>
        <td><span class="badge bg-${getStatusBadgeClass(courier.status)}">${getStatusText(courier.status)}</span></td>
        <td>${courier.totalShipments}</td>
        <td>${courier.completedShipments}</td>
        <td>${courier.pendingShipments}</td>
        <td>${courier.deliveryRate}%</td>
        <td>
            <div class="rating">
                ${generateStars(courier.rating)}
                <span class="ms-1">${courier.rating}</span>
            </div>
        </td>
        <td>
            <div class="action-buttons">
                <button class="action-btn view" data-id="${courier.id}" title="عرض التفاصيل">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="action-btn edit" data-id="${courier.id}" title="تعديل">
                    <i class="fas fa-edit"></i>
                </button>
                <button class="action-btn chart" data-id="${courier.id}" title="عرض الرسم البياني">
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
    // Courier Performance Chart
    const performanceCtx = document.getElementById('courierPerformanceChart');
    if (performanceCtx) {
        const performanceData = await loadCourierPerformanceData();
        
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
                    label: 'الشحنات المعلقة',
                    data: performanceData.pendingShipments || [],
                    backgroundColor: '#f59e0b',
                    borderColor: '#d97706',
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

    // Courier Distribution Chart
    const distributionCtx = document.getElementById('courierDistributionChart');
    if (distributionCtx) {
        const distributionData = await loadCourierDistributionData();
        
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
}

// API Data Loading Functions
async function loadCourierPerformanceData() {
    try {
        const response = await window.apiService.getCourierPerformanceData();
        if (response.success) {
            return response.data;
        }
    } catch (error) {
        // Return empty data on error
    }
    return { labels: [], completedShipments: [], pendingShipments: [] };
}

async function loadCourierDistributionData() {
    try {
        const response = await window.apiService.getCourierDistributionData();
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
            viewCourierDetails(id);
        } else if (e.target.closest('.action-btn.edit')) {
            const id = e.target.closest('.action-btn').getAttribute('data-id');
            editCourier(id);
        } else if (e.target.closest('.action-btn.chart')) {
            const id = e.target.closest('.action-btn').getAttribute('data-id');
            showCourierChart(id);
        }
    });
}

function viewCourierDetails(id) {
    const modal = new bootstrap.Modal(document.getElementById('courierDetailsModal'));
    modal.show();
}

function editCourier(id) {
    // Edit courier implementation
}

function showCourierChart(id) {
    // Show courier chart implementation
}

function applyFilters() {
    const filters = {
        dateFrom: document.getElementById('dateFrom').value,
        dateTo: document.getElementById('dateTo').value,
        courier: document.getElementById('courierSelect').value,
        status: document.getElementById('statusSelect').value
    };
    loadCouriersData();
}

function clearFilters() {
    document.getElementById('dateFrom').value = '';
    document.getElementById('dateTo').value = '';
    document.getElementById('courierSelect').value = '';
    document.getElementById('statusSelect').value = '';
    loadCouriersData();
}

function exportReport(format) {
    showNotification(`جاري تصدير التقرير بصيغة ${format.toUpperCase()}...`, 'info');
}

function exportTable() {
    // Export table implementation
}

function refreshReport() {
    loadCouriersData();
}

function showNotification(message, type) {
    if (typeof NotificationService !== 'undefined') {
        NotificationService.show(message, type);
    }
}
