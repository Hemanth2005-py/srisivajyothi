/**
 * MYMART — UI Utilities & Component System
 */
const UI = {
  THEME_KEY: 'mymart_theme',

  initTheme() {
    const saved = localStorage.getItem(this.THEME_KEY) || 'light';
    document.documentElement.setAttribute('data-theme', saved);
    document.documentElement.setAttribute('data-bs-theme', saved);
    this.updateThemeIcons(saved);
  },

  toggleTheme() {
    const current = document.documentElement.getAttribute('data-theme') || 'light';
    const next = current === 'dark' ? 'light' : 'dark';
    document.documentElement.setAttribute('data-theme', next);
    document.documentElement.setAttribute('data-bs-theme', next);
    localStorage.setItem(this.THEME_KEY, next);
    this.updateThemeIcons(next);
  },

  updateThemeIcons(theme) {
    const icons = document.querySelectorAll('.theme-toggle-icon');
    icons.forEach(i => {
      i.className = theme === 'dark' ? 'bi bi-sun-fill theme-toggle-icon' : 'bi bi-moon-stars-fill theme-toggle-icon';
    });
  },

  formatCurrency(val) {
    if (val === null || val === undefined || isNaN(val)) return '₹0.00';
    return '₹' + Number(val).toFixed(2);
  },

  showToast(message, type = 'success') {
    let container = document.getElementById('toast-container');
    if (!container) {
      container = document.createElement('div');
      container.id = 'toast-container';
      document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `custom-toast toast-${type}`;

    let icon = 'bi-check-circle-fill text-success';
    if (type === 'error') icon = 'bi-exclamation-triangle-fill text-danger';
    if (type === 'warning') icon = 'bi-exclamation-circle-fill text-warning';
    if (type === 'info') icon = 'bi-info-circle-fill text-info';

    toast.innerHTML = `
      <i class="bi ${icon} fs-5"></i>
      <div class="flex-grow-1 font-weight-500">${message}</div>
      <button type="button" class="btn-close btn-close-white ms-auto" aria-label="Close" style="font-size: 0.75rem;"></button>
    `;

    toast.querySelector('.btn-close').onclick = () => toast.remove();
    container.appendChild(toast);

    setTimeout(() => {
      toast.style.opacity = '0';
      toast.style.transition = 'opacity 0.3s ease';
      setTimeout(() => toast.remove(), 300);
    }, 3500);
  },

  renderNavbar(activePage = '') {
    const user = window.Auth?.getUser();
    const isAuth = window.Auth?.isAuthenticated();
    const isAdmin = window.Auth?.isAdmin();

    const nav = document.createElement('nav');
    nav.className = 'navbar-custom';
    nav.innerHTML = `
      <div class="container-fluid d-flex align-items-center justify-content-between gap-3">
        <!-- Logo & Location -->
        <div class="d-flex align-items-center gap-3">
          <a href="index.html" class="brand-logo">
            <span style="display:inline-flex; width:34px; height:34px; border-radius:10px; background:#0c831f; color:#fff; align-items:center; justify-content:center; font-size:1.1rem; box-shadow:0 4px 10px rgba(12,131,31,0.3);">
              <i class="bi bi-basket-fill"></i>
            </span>
            <span>MYMART</span>
            <span class="brand-badge">Supermarket</span>
          </a>

          <div class="location-selector d-none d-md-flex" onclick="UI.openLocationModal()">
            <i class="bi bi-geo-alt-fill text-success fs-5"></i>
            <div>
              <span class="loc-title" id="nav-location-title">Delivery in 10 Mins</span>
              <span class="loc-sub" id="nav-location-sub">Indiranagar, Bengaluru</span>
            </div>
            <i class="bi bi-chevron-down ms-1 text-muted" style="font-size: 0.75rem;"></i>
          </div>
        </div>

        <!-- Live Search -->
        <div class="search-wrapper flex-grow-1 mx-2 d-none d-sm-block">
          <form onsubmit="UI.handleSearchSubmit(event)">
            <i class="bi bi-search search-icon"></i>
            <input type="text" id="global-search-input" class="search-input" placeholder="Search for atta, milk, chips, fresh vegetables, soap..." autocomplete="off">
            <i class="bi bi-x-circle-fill search-clear" id="global-search-clear" onclick="UI.clearSearch()"></i>
          </form>
        </div>

        <!-- Navigation Right Actions -->
        <div class="d-flex align-items-center gap-2">
          <!-- Categories link -->
          <a href="products.html" class="btn btn-outline-custom d-none d-lg-inline-flex align-items-center gap-1 ${activePage === 'products' ? 'active' : ''}">
            <i class="bi bi-grid"></i> All Products
          </a>

          <!-- Favorites -->
          <a href="favorites.html" class="nav-icon-btn position-relative ${activePage === 'favorites' ? 'text-danger' : ''}" title="Favorites">
            <i class="bi bi-heart-fill"></i>
            <span class="wishlist-count-badge nav-badge" style="display:none;">0</span>
          </a>

          <!-- Cart Button -->
          <a href="cart.html" class="cart-btn-nav" title="Cart">
            <i class="bi bi-cart3 fs-5"></i>
            <span class="d-none d-md-inline">My Cart</span>
            <span class="cart-count-badge">0</span>
          </a>

          <!-- Theme Toggle -->
          <button class="nav-icon-btn" onclick="UI.toggleTheme()" title="Toggle Dark/Light Mode">
            <i class="theme-toggle-icon bi bi-moon-stars-fill"></i>
          </button>

          <!-- User Menu -->
          ${isAuth ? `
            <div class="dropdown">
              <button class="btn btn-outline-custom dropdown-toggle d-flex align-items-center gap-2" type="button" data-bs-toggle="dropdown">
                <i class="bi bi-person-circle fs-5"></i>
                <span class="d-none d-md-inline">${user?.fullName?.split(' ')[0] || 'Account'}</span>
              </button>
              <ul class="dropdown-menu dropdown-menu-end shadow border-0" style="background: var(--bg-surface); border-radius: 12px;">
                <li class="px-3 py-2 border-bottom">
                  <div class="fw-bold">${user?.fullName || 'User'}</div>
                  <div class="text-muted small">${user?.email || ''}</div>
                </li>
                ${isAdmin ? `
                  <li><a class="dropdown-item py-2 text-primary fw-bold" href="admin-dashboard.html"><i class="bi bi-speedometer2 me-2"></i>Admin Dashboard</a></li>
                  <li><hr class="dropdown-divider"></li>
                ` : ''}
                <li><a class="dropdown-item py-2" href="profile.html"><i class="bi bi-person me-2"></i>My Profile</a></li>
                <li><a class="dropdown-item py-2" href="order-history.html"><i class="bi bi-bag-check me-2"></i>My Orders</a></li>
                <li><a class="dropdown-item py-2" href="favorites.html"><i class="bi bi-heart me-2"></i>Wishlist</a></li>
                <li><hr class="dropdown-divider"></li>
                <li><a class="dropdown-item py-2 text-danger" href="javascript:void(0)" onclick="Auth.logout()"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
              </ul>
            </div>
          ` : `
            <a href="login.html" class="btn btn-outline-custom d-flex align-items-center gap-1">
              <i class="bi bi-person"></i> Login
            </a>
          `}
        </div>
      </div>

      <!-- Mobile Search Sub-row -->
      <div class="container-fluid mt-2 d-sm-none">
        <form onsubmit="UI.handleSearchSubmit(event)" class="position-relative w-100">
          <i class="bi bi-search search-icon"></i>
          <input type="text" id="mobile-search-input" class="search-input" placeholder="Search products..." autocomplete="off">
        </form>
      </div>
    `;

    const placeholder = document.getElementById('navbar-placeholder');
    if (placeholder) {
      placeholder.replaceWith(nav);
    } else {
      document.body.prepend(nav);
    }

    // Attach search events
    const input = document.getElementById('global-search-input');
    const clearBtn = document.getElementById('global-search-clear');
    if (input && clearBtn) {
      input.addEventListener('input', () => {
        clearBtn.style.display = input.value ? 'block' : 'none';
      });
    }

    // Initialize badges
    window.Cart?.updateNavBadge();
    window.Wishlist?.updateNavBadge();
  },

  handleSearchSubmit(e) {
    e.preventDefault();
    const input = document.getElementById('global-search-input') || document.getElementById('mobile-search-input');
    const query = input?.value?.trim();
    if (query) {
      window.location.href = `products.html?query=${encodeURIComponent(query)}`;
    }
  },

  clearSearch() {
    const input = document.getElementById('global-search-input');
    if (input) {
      input.value = '';
      document.getElementById('global-search-clear').style.display = 'none';
    }
  },

  renderFooter() {
    const footer = document.createElement('footer');
    footer.className = 'footer-custom';
    footer.innerHTML = `
      <div class="container">
        <div class="row g-4">
          <div class="col-lg-4 col-md-6">
            <div class="brand-logo mb-3">
              <span style="display:inline-flex; width:30px; height:30px; border-radius:8px; background:#0c831f; color:#fff; align-items:center; justify-content:center; font-size:1rem;">
                <i class="bi bi-basket-fill"></i>
              </span>
              <span>MYMART</span>
            </div>
            <p class="text-muted small mb-3">
              MYMART is your neighborhood supermarket delivering authentic groceries, farm-fresh produce, dairy, staples, and daily home essentials directly to your doorstep in minutes.
            </p>
            <div class="d-flex gap-3 text-muted">
              <a href="#" class="text-muted fs-5"><i class="bi bi-facebook"></i></a>
              <a href="#" class="text-muted fs-5"><i class="bi bi-instagram"></i></a>
              <a href="#" class="text-muted fs-5"><i class="bi bi-twitter-x"></i></a>
              <a href="#" class="text-muted fs-5"><i class="bi bi-youtube"></i></a>
            </div>
          </div>
          <div class="col-lg-2 col-md-3 col-6">
            <h6 class="fw-bold mb-3">Shop Categories</h6>
            <ul class="list-unstyled small d-flex flex-column gap-2 text-muted">
              <li><a href="products.html?categoryId=" class="text-muted">Vegetables & Fruits</a></li>
              <li><a href="products.html?categoryId=" class="text-muted">Dairy, Bread & Eggs</a></li>
              <li><a href="products.html?categoryId=" class="text-muted">Rice, Atta & Dals</a></li>
              <li><a href="products.html?categoryId=" class="text-muted">Snacks & Beverages</a></li>
              <li><a href="products.html?categoryId=" class="text-muted">Cleaning Essentials</a></li>
            </ul>
          </div>
          <div class="col-lg-2 col-md-3 col-6">
            <h6 class="fw-bold mb-3">Customer Service</h6>
            <ul class="list-unstyled small d-flex flex-column gap-2 text-muted">
              <li><a href="profile.html" class="text-muted">My Account</a></li>
              <li><a href="order-history.html" class="text-muted">Track Orders</a></li>
              <li><a href="favorites.html" class="text-muted">My Wishlist</a></li>
              <li><a href="cart.html" class="text-muted">View Cart</a></li>
              <li><a href="admin-login.html" class="text-muted"><i class="bi bi-shield-lock me-1"></i>Admin Portal</a></li>
            </ul>
          </div>
          <div class="col-lg-4 col-md-12">
            <h6 class="fw-bold mb-3">Supermarket Promise</h6>
            <div class="p-3 rounded-3" style="background: var(--bg-subtle); border: 1px solid var(--border-color);">
              <div class="d-flex align-items-center gap-3 mb-2">
                <i class="bi bi-lightning-charge-fill text-warning fs-3"></i>
                <div>
                  <div class="fw-bold small">Superfast 10-Minute Delivery</div>
                  <div class="text-muted" style="font-size:0.75rem;">Dispatched from local Main & store fulfilment centers</div>
                </div>
              </div>
              <div class="d-flex align-items-center gap-3">
                <i class="bi bi-shield-check text-success fs-3"></i>
                <div>
                  <div class="fw-bold small">100% Quality Assurance</div>
                  <div class="text-muted" style="font-size:0.75rem;">Trusted brands, verified weights, easy return policy</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <hr class="my-4" style="border-color: var(--border-color);">
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 small text-muted">
          <div>© 2026 MYMART Supermarket Pvt Ltd. All rights reserved.</div>
          <div>Built with Spring Boot 3 & MongoDB Atlas Cloud.</div>
        </div>
      </div>
    `;

    const placeholder = document.getElementById('footer-placeholder');
    if (placeholder) {
      placeholder.replaceWith(footer);
    } else {
      document.body.appendChild(footer);
    }
  },

  renderProductCard(product) {
    const isFav = window.Wishlist?.isFavorited(product.id);
    const qtyInCart = window.Cart?.getItemQuantity(product.id) || 0;
    const isOutOfStock = !product.inStock || product.totalStock <= 0;

    const discountBadge = product.discountPercentage > 0
      ? `<span class="discount-tag">${product.discountPercentage}% OFF</span>`
      : '';

    const originalPriceHtml = (product.discountPrice && product.discountPrice < product.originalPrice)
      ? `<span class="original-price">${this.formatCurrency(product.originalPrice)}</span>`
      : '';

    let actionBtnHtml = '';
    if (isOutOfStock) {
      actionBtnHtml = `<div class="out-of-stock-pill"><i class="bi bi-slash-circle me-1"></i>Out of Stock</div>`;
    } else if (qtyInCart > 0) {
      actionBtnHtml = `
        <div class="qty-control-group" id="card-qty-ctrl-${product.id}">
          <button class="qty-control-btn" onclick="UI.decrementCart('${product.id}', event)"><i class="bi bi-dash"></i></button>
          <span class="qty-display">${qtyInCart}</span>
          <button class="qty-control-btn" onclick="UI.incrementCart('${product.id}', event)"><i class="bi bi-plus"></i></button>
        </div>
      `;
    } else {
      actionBtnHtml = `
        <button class="btn-add-cart" id="card-add-btn-${product.id}" onclick="UI.addToCartBtn('${product.id}', event)">
          <i class="bi bi-plus-lg me-1"></i> ADD
        </button>
      `;
    }

    return `
      <div class="product-card" data-product-id="${product.id}">
        ${discountBadge}
        <button class="wishlist-btn ${isFav ? 'active' : ''}" onclick="UI.toggleFav('${product.id}', event)" title="Wishlist">
          <i class="bi ${isFav ? 'bi-heart-fill' : 'bi-heart'}"></i>
        </button>
        <a href="product-details.html?id=${product.id}" class="text-decoration-none">
          <div class="product-img-wrapper">
            <img src="${product.imageUrl || 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=400&q=80'}"
                 alt="${product.name}"
                 loading="lazy"
                 onerror="this.src='https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=400&q=80'">
          </div>
          <div class="product-brand">${product.brandName || ''}</div>
          <div class="product-title">${product.name}</div>
          <div class="product-unit">${product.weight ? (product.weight + ' ' + (product.unit || '')) : (product.unit || '')}</div>
        </a>
        <div class="price-row">
          <span class="effective-price">${this.formatCurrency(product.effectivePrice)}</span>
          ${originalPriceHtml}
        </div>
        <div class="cart-action-container mt-auto" id="cart-box-${product.id}">
          ${actionBtnHtml}
        </div>
      </div>
    `;
  },

  async addToCartBtn(productId, e) {
    if (e) e.stopPropagation();
    await window.Cart?.addToCart(productId, 1);
    this.refreshProductCardButton(productId);
  },

  async incrementCart(productId, e) {
    if (e) e.stopPropagation();
    const current = window.Cart?.getItemQuantity(productId) || 0;
    await window.Cart?.updateQuantity(productId, current + 1);
    this.refreshProductCardButton(productId);
  },

  async decrementCart(productId, e) {
    if (e) e.stopPropagation();
    const current = window.Cart?.getItemQuantity(productId) || 0;
    await window.Cart?.updateQuantity(productId, current - 1);
    this.refreshProductCardButton(productId);
  },

  refreshProductCardButton(productId) {
    const box = document.getElementById(`cart-box-${productId}`);
    if (!box) return;

    const qty = window.Cart?.getItemQuantity(productId) || 0;
    if (qty > 0) {
      box.innerHTML = `
        <div class="qty-control-group">
          <button class="qty-control-btn" onclick="UI.decrementCart('${productId}', event)"><i class="bi bi-dash"></i></button>
          <span class="qty-display">${qty}</span>
          <button class="qty-control-btn" onclick="UI.incrementCart('${productId}', event)"><i class="bi bi-plus"></i></button>
        </div>
      `;
    } else {
      box.innerHTML = `
        <button class="btn-add-cart" onclick="UI.addToCartBtn('${productId}', event)">
          <i class="bi bi-plus-lg me-1"></i> ADD
        </button>
      `;
    }
  },

  async toggleFav(productId, e) {
    if (e) e.stopPropagation();
    const isFav = await window.Wishlist?.toggleFavorite(productId);
    const btns = document.querySelectorAll(`[data-product-id="${productId}"] .wishlist-btn`);
    btns.forEach(b => {
      if (isFav) {
        b.classList.add('active');
        b.querySelector('i').className = 'bi bi-heart-fill';
      } else {
        b.classList.remove('active');
        b.querySelector('i').className = 'bi bi-heart';
      }
    });
  },

  openLocationModal() {
    let modalEl = document.getElementById('location-modal');
    if (!modalEl) {
      modalEl = document.createElement('div');
      modalEl.id = 'location-modal';
      modalEl.className = 'modal fade';
      modalEl.innerHTML = `
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content border-0 shadow" style="background:var(--bg-surface); border-radius:18px;">
            <div class="modal-header border-0 pb-0">
              <h5 class="modal-title fw-bold">Select Delivery Location</h5>
              <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
            </div>
            <div class="modal-body">
              <p class="text-muted small">Choose your supermarket service area for 10-minute grocery delivery.</p>
              <div class="d-flex flex-column gap-2 mb-3">
                <button class="btn btn-outline-custom text-start d-flex justify-content-between align-items-center py-2" onclick="UI.setLocation('Indiranagar, Bengaluru', '560038')">
                  <div><strong>Indiranagar</strong><br><small class="text-muted">100ft Road, Bengaluru - 560038</small></div>
                  <i class="bi bi-chevron-right"></i>
                </button>
                <button class="btn btn-outline-custom text-start d-flex justify-content-between align-items-center py-2" onclick="UI.setLocation('Koramangala, Bengaluru', '560034')">
                  <div><strong>Koramangala</strong><br><small class="text-muted">4th Block, Bengaluru - 560034</small></div>
                  <i class="bi bi-chevron-right"></i>
                </button>
                <button class="btn btn-outline-custom text-start d-flex justify-content-between align-items-center py-2" onclick="UI.setLocation('HSR Layout, Bengaluru', '560102')">
                  <div><strong>HSR Layout</strong><br><small class="text-muted">Sector 2, Bengaluru - 560102</small></div>
                  <i class="bi bi-chevron-right"></i>
                </button>
                <button class="btn btn-outline-custom text-start d-flex justify-content-between align-items-center py-2" onclick="UI.setLocation('Bellandur, Bengaluru', '560103')">
                  <div><strong>Bellandur</strong><br><small class="text-muted">Green Glen Layout, Bengaluru - 560103</small></div>
                  <i class="bi bi-chevron-right"></i>
                </button>
              </div>
            </div>
          </div>
        </div>
      `;
      document.body.appendChild(modalEl);
    }
    const bsModal = new bootstrap.Modal(modalEl);
    bsModal.show();
  },

  setLocation(area, pincode) {
    const titleEl = document.getElementById('nav-location-title');
    const subEl = document.getElementById('nav-location-sub');
    if (titleEl) titleEl.textContent = 'Delivery in 10 Mins';
    if (subEl) subEl.textContent = area;

    localStorage.setItem('mymart_user_location', JSON.stringify({ area, pincode }));
    const modalEl = document.getElementById('location-modal');
    if (modalEl) {
      bootstrap.Modal.getInstance(modalEl)?.hide();
    }
    this.showToast(`Delivery location set to ${area}`, 'success');
  }
};

// Global init on load
document.addEventListener('DOMContentLoaded', () => {
  UI.initTheme();
  window.Cart?.loadCart();
  window.Wishlist?.loadWishlist();
});

window.UI = UI;
