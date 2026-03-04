/**
 * Twsela CMS - Help Center Page Handler
 * Browse help articles and categories
 */

class HelpPageHandler extends BasePageHandler {
    constructor() {
        super('Help');
        this.categories = [];
        this.articles = [];
        this.currentArticle = null;
        this.currentCategory = null;
    }

    /**
     * Initialize page-specific functionality
     */
    async initializePage() {
        try {
            UIUtils.showLoading();
            this.setupDashboardLink();
            await Promise.all([
                this.loadCategories(),
                this.loadArticles()
            ]);
        } catch (error) {
            ErrorHandler.handle(error, 'Help');
        } finally {
            UIUtils.hideLoading();
        }
    }

    /**
     * Setup dynamic dashboard link based on user role
     */
    setupDashboardLink() {
        const link = document.getElementById('backToDashboard');
        if (link && this.services.auth) {
            const user = this.services.auth.getUser?.() || {};
            const role = (user.role || '').toLowerCase();
            const roleMap = {
                'owner': '/owner/dashboard.html',
                'admin': '/admin/dashboard.html',
                'merchant': '/merchant/dashboard.html',
                'courier': '/courier/dashboard.html',
                'warehouse': '/warehouse/dashboard.html'
            };
            link.href = roleMap[role] || '/login.html';
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const searchBtn = document.getElementById('searchBtn');
        if (searchBtn) {
            searchBtn.addEventListener('click', () => this.handleSearch());
        }

        const searchInput = document.getElementById('helpSearch');
        if (searchInput) {
            searchInput.addEventListener('keypress', (e) => {
                if (e.key === 'Enter') this.handleSearch();
            });
        }

        const backBtn = document.getElementById('backToArticlesBtn');
        if (backBtn) {
            backBtn.addEventListener('click', () => this.showArticlesList());
        }
    }

    /**
     * Load help categories
     */
    async loadCategories() {
        try {
            const response = await this.services.api.getHelpCategories();
            if (response?.success) {
                this.categories = response.data || [];
            } else {
                this.categories = [];
            }
            this.renderCategories();
        } catch (error) {
            console.error('Failed to load categories:', error);
            this.categories = [];
            this.renderCategories();
        }
    }

    /**
     * Render categories grid
     */
    renderCategories() {
        const grid = document.getElementById('categoriesGrid');
        if (!grid) return;

        if (this.categories.length === 0) {
            grid.innerHTML = `
                <div class="col-12 text-center py-3 text-muted">
                    <p>لا توجد تصنيفات</p>
                </div>`;
            return;
        }

        const iconMap = {
            'GETTING_STARTED': 'fas fa-rocket',
            'SHIPMENTS': 'fas fa-shipping-fast',
            'PAYMENTS': 'fas fa-credit-card',
            'ACCOUNT': 'fas fa-user-cog',
            'COURIER': 'fas fa-motorcycle',
            'WAREHOUSE': 'fas fa-warehouse'
        };

        const colorMap = {
            'GETTING_STARTED': 'primary',
            'SHIPMENTS': 'info',
            'PAYMENTS': 'success',
            'ACCOUNT': 'warning',
            'COURIER': 'danger',
            'WAREHOUSE': 'secondary'
        };

        grid.innerHTML = this.categories.map(cat => {
            const icon = iconMap[cat.code] || 'fas fa-folder';
            const color = colorMap[cat.code] || 'primary';
            return `
                <div class="col-md-4 col-sm-6">
                    <div class="content-card h-100 cursor-pointer" onclick="window.helpPageHandler.filterByCategory('${escapeHtml(cat.code || cat.id)}')" style="cursor:pointer;">
                        <div class="card-body text-center py-4">
                            <div class="mb-3">
                                <i class="${icon} fa-2x text-${color}"></i>
                            </div>
                            <h6 class="mb-1">${escapeHtml(cat.name || '')}</h6>
                            <small class="text-muted">${cat.articleCount || 0} مقال</small>
                        </div>
                    </div>
                </div>`;
        }).join('');
    }

    /**
     * Load help articles
     */
    async loadArticles(params = {}) {
        try {
            const response = await this.services.api.getHelpArticles(params);
            if (response?.success) {
                this.articles = response.data?.content || response.data || [];
            } else {
                this.articles = [];
            }
            this.renderArticles();
        } catch (error) {
            console.error('Failed to load articles:', error);
            this.articles = [];
            this.renderArticles();
        }
    }

    /**
     * Render articles list
     */
    renderArticles() {
        const container = document.getElementById('articlesList');
        if (!container) return;

        if (this.articles.length === 0) {
            container.innerHTML = `
                <div class="content-card">
                    <div class="card-body text-center py-4 text-muted">
                        <i class="fas fa-file-alt fa-2x mb-2 d-block"></i>
                        <p>لا توجد مقالات</p>
                    </div>
                </div>`;
            return;
        }

        container.innerHTML = this.articles.map(article => `
            <div class="content-card mb-2">
                <div class="card-body d-flex align-items-center py-3" style="cursor:pointer;"
                     onclick="window.helpPageHandler.viewArticle(${article.id})">
                    <div class="flex-grow-1">
                        <h6 class="mb-1">${escapeHtml(article.title || '')}</h6>
                        <small class="text-muted">
                            ${article.categoryName ? `<span class="badge bg-light text-dark me-2">${escapeHtml(article.categoryName)}</span>` : ''}
                            ${article.updatedAt ? new Date(article.updatedAt).toLocaleDateString('ar-EG') : ''}
                        </small>
                    </div>
                    <i class="fas fa-chevron-left text-muted"></i>
                </div>
            </div>`).join('');
    }

    /**
     * Filter articles by category
     */
    filterByCategory(categoryCode) {
        this.currentCategory = categoryCode;
        this.loadArticles({ category: categoryCode });
    }

    /**
     * Handle search
     */
    async handleSearch() {
        const query = document.getElementById('helpSearch')?.value?.trim();
        if (!query) {
            await this.loadArticles();
            return;
        }

        try {
            const response = await this.services.api.searchHelpArticles(query);
            if (response?.success) {
                this.articles = response.data?.content || response.data || [];
            } else {
                this.articles = [];
            }
            this.renderArticles();
        } catch (error) {
            ErrorHandler.handle(error, 'SearchArticles');
        }
    }

    /**
     * View article detail
     */
    async viewArticle(articleId) {
        try {
            const response = await this.services.api.getHelpArticle(articleId);
            if (response?.success) {
                this.currentArticle = response.data;
                this.renderArticleDetail();
            }
        } catch (error) {
            ErrorHandler.handle(error, 'ViewArticle');
        }
    }

    /**
     * Render article detail
     */
    renderArticleDetail() {
        const article = this.currentArticle;
        if (!article) return;

        // Toggle sections
        document.getElementById('categoriesSection')?.classList.add('d-none');
        document.getElementById('articlesSection')?.classList.add('d-none');
        document.getElementById('articleDetailSection')?.classList.remove('d-none');

        document.getElementById('articleTitle').textContent = article.title || '';
        document.getElementById('articleCategory').textContent = article.categoryName || article.category || '';
        document.getElementById('articleDate').textContent = article.updatedAt ? new Date(article.updatedAt).toLocaleDateString('ar-EG') : '';
        document.getElementById('articleContent').innerHTML = article.content || '';
    }

    /**
     * Show articles list (back from detail)
     */
    showArticlesList() {
        document.getElementById('articleDetailSection')?.classList.add('d-none');
        document.getElementById('categoriesSection')?.classList.remove('d-none');
        document.getElementById('articlesSection')?.classList.remove('d-none');
        this.currentArticle = null;
    }
}

// Create global instance
window.helpPageHandler = new HelpPageHandler();

// Initialize when DOM is ready
document.addEventListener('DOMContentLoaded', function() {
    if (window.location.pathname.includes('/help.html')) {
        setTimeout(() => {
            window.helpPageHandler.init();
        }, 200);
    }
});
