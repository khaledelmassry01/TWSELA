/**
 * Twsela CMS - Owner Reports Warehouse Page
 * Handles warehouse operations report data, charts, filtering, and export
 */

// Initialize warehouse report page
document.addEventListener('DOMContentLoaded', function() {
    initializeWarehouseReportPage();
});

function initializeWarehouseReportPage() {
    loadWarehouseData();
    initializeCharts();
    setupEventListeners();
}

async function loadWarehouseData() {
    try {
        const operations = await apiService.getWarehouseOperations();
        updateWarehouseTable(operations);
    } catch (error) {
        console.error('Error loading warehouse data:', error);
    }
}

function updateWarehouseTable(operations) {
    const tbody = document.querySelector('#warehouseTable tbody');
    if (!tbody) return;

    tbody.innerHTML = '';
    
    operations.forEach(operation => {
        const row = createOperationRow(operation);
        tbody.appendChild(row);
    });
}

function createOperationRow(operation) {
    const row = document.createElement('tr');
    row.innerHTML = `
        <td>${operation.timestamp}</td>
        <td><span class="badge bg-${getOperationBadgeClass(operation.type)}">${getOperationText(operation.type)}</span></td>
        <td>${operation.shipmentNumber}</td>
        <td>${operation.warehouse}</td>
        <td>${operation.employee}</td>
        <td><span class="badge bg-${getStatusBadgeClass(operation.status)}">${getStatusText(operation.status)}</span></td>
        <td>${operation.notes || '-'}</td>
        <td>
            <div class="action-buttons">
                <button class="action-btn view" data-id="${operation.id}" title="عرض التفاصيل">
                    <i class="fas fa-eye"></i>
                </button>
                <button class="action-btn edit" data-id="${operation.id}" title="تعديل">
                    <i class="fas fa-edit"></i>
                </button>
            </div>
        </td>
    `;
    return row;
}

function getOperationBadgeClass(type) {
    const typeMap = {
        'RECEIVE': 'primary',
        'DISPATCH': 'success',
        'INVENTORY': 'warning',
        'RECONCILE': 'info'
    };
    return typeMap[type] || 'secondary';
}

function getOperationText(type) {
    const typeMap = {
        'RECEIVE': 'استلام',
        'DISPATCH': 'إرسال',
        'INVENTORY': 'جرد',
        'RECONCILE': 'مطابقة'
    };
    return typeMap[type] || type;
}

function getStatusBadgeClass(status) {
    const statusMap = {
        'COMPLETED': 'success',
        'IN_PROGRESS': 'warning',
        'PENDING': 'info',
        'FAILED': 'danger'
    };
    return statusMap[status] || 'secondary';
}

function getStatusText(status) {
    const statusMap = {
        'COMPLETED': 'مكتمل',
        'IN_PROGRESS': 'قيد المعالجة',
        'PENDING': 'معلق',
        'FAILED': 'فشل'
    };
    return statusMap[status] || status;
}

async function initializeCharts() {
    // Warehouse Operations Chart
    const operationsCtx = document.getElementById('warehouseOperationsChart');
    if (operationsCtx) {
        const operationsData = await loadWarehouseOperationsData();
        
        new Chart(operationsCtx, {
            type: 'line',
            data: {
                labels: operationsData.labels || [],
                datasets: [{
                    label: 'استلام',
                    data: operationsData.receiving || [],
                    borderColor: '#3b82f6',
                    backgroundColor: 'rgba(59, 130, 246, 0.1)',
                    tension: 0.4
                }, {
                    label: 'إرسال',
                    data: operationsData.shipping || [],
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    tension: 0.4
                }, {
                    label: 'جرد',
                    data: operationsData.inventory || [],
                    borderColor: '#f59e0b',
                    backgroundColor: 'rgba(245, 158, 11, 0.1)',
                    tension: 0.4
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

    // Operations Distribution Chart
    const distributionCtx = document.getElementById('operationsDistributionChart');
    if (distributionCtx) {
        const distributionData = await loadOperationsDistributionData();
        
        new Chart(distributionCtx, {
            type: 'doughnut',
            data: {
                labels: distributionData.labels || [],
                datasets: [{
                    data: distributionData.values || [],
                    backgroundColor: ['#3b82f6', '#10b981', '#f59e0b', '#8b5cf6'],
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

    // Errors Chart
    const errorsCtx = document.getElementById('errorsChart');
    if (errorsCtx) {
        const errorsData = await loadErrorsData();
        
        new Chart(errorsCtx, {
            type: 'bar',
            data: {
                labels: errorsData.labels || [],
                datasets: [{
                    label: 'عدد الأخطاء',
                    data: errorsData.values || [],
                    backgroundColor: ['#f59e0b', '#ef4444', '#3b82f6', '#6b7280']
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: { y: { beginAtZero: true } },
                plugins: { legend: { display: false } }
            }
        });
    }
}

// API Data Loading Functions
async function loadWarehouseOperationsData() {
    try {
        const response = await window.apiService.getWarehouseOperationsData();
        if (response.success) {
            return response.data;
        }
    } catch (error) {
        // Return empty data on error
    }
    return { labels: [], receiving: [], shipping: [], inventory: [] };
}

async function loadOperationsDistributionData() {
    try {
        const response = await window.apiService.getOperationsDistributionData();
        if (response.success) {
            return response.data;
        }
    } catch (error) {
        // Return empty data on error
    }
    return { labels: [], values: [] };
}

async function loadErrorsData() {
    try {
        const response = await window.apiService.getErrorsData();
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
            viewOperationDetails(id);
        } else if (e.target.closest('.action-btn.edit')) {
            const id = e.target.closest('.action-btn').getAttribute('data-id');
            editOperation(id);
        }
    });
}

function viewOperationDetails(id) {
    const modal = new bootstrap.Modal(document.getElementById('operationDetailsModal'));
    modal.show();
}

function editOperation(id) {
    // Edit operation implementation
}

function applyFilters() {
    const filters = {
        dateFrom: document.getElementById('dateFrom').value,
        dateTo: document.getElementById('dateTo').value,
        warehouse: document.getElementById('warehouseSelect').value,
        operation: document.getElementById('operationSelect').value
    };
    loadWarehouseData();
}

function clearFilters() {
    document.getElementById('dateFrom').value = '';
    document.getElementById('dateTo').value = '';
    document.getElementById('warehouseSelect').value = '';
    document.getElementById('operationSelect').value = '';
    loadWarehouseData();
}

function exportReport(format) {
    showNotification(`جاري تصدير التقرير بصيغة ${format.toUpperCase()}...`, 'info');
}

function exportTable() {
    // Export table implementation
}

function refreshReport() {
    loadWarehouseData();
}

function showNotification(message, type) {
    if (typeof NotificationService !== 'undefined') {
        NotificationService.show(message, type);
    }
}
