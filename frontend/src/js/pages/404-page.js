import { Logger } from '../shared/Logger.js';
const log = Logger.getLogger('404-page');

/**
 * Twsela CMS - 404 Page Handler
 * Handles page-specific initialization and all 404 page functionality
 * Follows DRY principle with clean separation of concerns
 */

// Initialize 404 page
document.addEventListener('DOMContentLoaded', function() {
    initialize404Page();
});

function initialize404Page() {
    // Set browser info
    document.getElementById('browserInfo').value = getBrowserInfo();
    
    // Setup event listeners
    setupEventListeners();
    
    // Initialize animations
    addFloatingAnimation();
}

function setupEventListeners() {
    // Go Home button
    const goHomeBtn = document.getElementById('goHomeBtn');
    if (goHomeBtn) {
        goHomeBtn.addEventListener('click', goHome);
    }
    
    // Go Back button
    const goBackBtn = document.getElementById('goBackBtn');
    if (goBackBtn) {
        goBackBtn.addEventListener('click', goBack);
    }
    
    // Show Help link
    const showHelpLink = document.getElementById('showHelpLink');
    if (showHelpLink) {
        showHelpLink.addEventListener('click', showHelp);
    }
    
    // Report Error link
    const reportErrorLink = document.getElementById('reportErrorLink');
    if (reportErrorLink) {
        reportErrorLink.addEventListener('click', reportError);
    }
    
    // Search input enter key
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                performSearch();
            }
        });
    }
    
    // Perform Search button
    const performSearchBtn = document.getElementById('performSearchBtn');
    if (performSearchBtn) {
        performSearchBtn.addEventListener('click', performSearch);
    }
    
    // Submit Error Report button
    const submitErrorReportBtn = document.getElementById('submitErrorReportBtn');
    if (submitErrorReportBtn) {
        submitErrorReportBtn.addEventListener('click', submitErrorReport);
    }
}

function goHome() {
    window.location.href = '/';
}

function goBack() {
    if (window.history.length > 1) {
        window.history.back();
    } else {
        window.location.href = '/';
    }
}

function showHelp() {
    if (window.notificationManager) {
        window.notificationManager.error('Ø¬Ø§Ø±ÙŠ ÙØªØ­ Ù…Ø±ÙƒØ² Ø§Ù„Ù…Ø³Ø§Ø¹Ø¯Ø©...', 'info');
    }
    // This would typically open a help center or knowledge base
}

function reportError() {
    const modal = new bootstrap.Modal(document.getElementById('reportErrorModal'));
    modal.show();
}

function performSearch() {
    const searchTerm = document.getElementById('searchInput').value.trim();
    
    if (!searchTerm) {
        if (window.notificationManager) {
            window.notificationManager.error('ÙŠØ±Ø¬Ù‰ Ø¥Ø¯Ø®Ø§Ù„ ÙƒÙ„Ù…Ø© Ø§Ù„Ø¨Ø­Ø«', 'error');
        }
        return;
    }

    // This would typically perform a search across the site
    if (window.notificationManager) {
        window.notificationManager.error(`Ø¬Ø§Ø±ÙŠ Ø§Ù„Ø¨Ø­Ø« Ø¹Ù†: ${searchTerm}`, 'info');
    }
    
    // For demo purposes, redirect to contact page
    setTimeout(() => {
        window.location.href = '/contact.html';
    }, 1000);
}

function submitErrorReport() {
    const form = document.getElementById('errorReportForm');
    const formData = new FormData(form);
    
    const errorReport = {
        description: formData.get('errorDescription'),
        email: formData.get('userEmail'),
        browserInfo: formData.get('browserInfo'),
        url: window.location.href,
        timestamp: new Date().toISOString()
    };

    if (!errorReport.description) {
        if (window.notificationManager) {
            window.notificationManager.error('ÙŠØ±Ø¬Ù‰ ÙƒØªØ§Ø¨Ø© ÙˆØµÙ Ù„Ù„Ù…Ø´ÙƒÙ„Ø©', 'error');
        }
        return;
    }

    try {
        // This would typically send the error report to the server
        
        if (window.notificationManager) {
            window.notificationManager.error('ØªÙ… Ø¥Ø±Ø³Ø§Ù„ ØªÙ‚Ø±ÙŠØ± Ø§Ù„Ø®Ø·Ø£ Ø¨Ù†Ø¬Ø§Ø­. Ø´ÙƒØ±Ø§Ù‹ Ù„Ùƒ!', 'success');
        }
        
        // Close modal and clear form
        const modal = bootstrap.Modal.getInstance(document.getElementById('reportErrorModal'));
        modal.hide();
        form.reset();
        } catch (error) {
            log.error('âŒ 404 Page Error - Error Report Submission:', {
                error: error.message,
                stack: error.stack,
                errorReport: errorReport,
                timestamp: new Date().toISOString()
            });
            
            if (window.notificationManager) {
                window.notificationManager.error('Ø­Ø¯Ø« Ø®Ø·Ø£ ÙÙŠ Ø¥Ø±Ø³Ø§Ù„ Ø§Ù„ØªÙ‚Ø±ÙŠØ±', 'error');
            }
        }
}

function getBrowserInfo() {
    const userAgent = navigator.userAgent;
    const browserName = getBrowserName(userAgent);
    const browserVersion = getBrowserVersion(userAgent);
    const osName = getOSName(userAgent);
    
    return `${browserName} ${browserVersion} - ${osName}`;
}

function getBrowserName(userAgent) {
    if (userAgent.includes('Chrome')) return 'Chrome';
    if (userAgent.includes('Firefox')) return 'Firefox';
    if (userAgent.includes('Safari')) return 'Safari';
    if (userAgent.includes('Edge')) return 'Edge';
    if (userAgent.includes('Opera')) return 'Opera';
    return 'Unknown Browser';
}

function getBrowserVersion(userAgent) {
    const match = userAgent.match(/(Chrome|Firefox|Safari|Edge|Opera)\/(\d+\.\d+)/);
    return match ? match[2] : 'Unknown Version';
}

function getOSName(userAgent) {
    if (userAgent.includes('Windows')) return 'Windows';
    if (userAgent.includes('Mac')) return 'macOS';
    if (userAgent.includes('Linux')) return 'Linux';
    if (userAgent.includes('Android')) return 'Android';
    if (userAgent.includes('iOS')) return 'iOS';
    return 'Unknown OS';
}

// Add some interactive animations
function addFloatingAnimation() {
    const shapes = document.querySelectorAll('.bg-shape');
    shapes.forEach((shape, index) => {
        shape.style.animationDelay = `${index * 0.5}s`;
    });
}
