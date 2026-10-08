/**
 * MYMART — Shopping Cart Module
 */
const Cart = {
  GUEST_CART_KEY: 'mymart_guest_cart',
  currentCart: null,

  getGuestCart() {
    try {
      const raw = localStorage.getItem(this.GUEST_CART_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  },

  setGuestCart(items) {
    localStorage.setItem(this.GUEST_CART_KEY, JSON.stringify(items));
    this.updateNavBadge();
    window.dispatchEvent(new CustomEvent('cart-updated', { detail: { items } }));
  },

  async loadCart() {
    if (window.Auth?.isAuthenticated() && window.Auth?.isCustomer()) {
      try {
        const cart = await window.API.get('/cart');
        this.currentCart = cart;
        this.updateNavBadge();
        window.dispatchEvent(new CustomEvent('cart-updated', { detail: cart }));
        return cart;
      } catch (e) {
        console.warn('Failed to load online cart:', e.message);
      }
    }

    // Guest fallback
    const items = this.getGuestCart();
    this.updateNavBadge();
    return { items, totalItemCount: items.reduce((acc, i) => acc + (i.quantity || 1), 0) };
  },

  async addToCart(productId, quantity = 1) {
    if (window.Auth?.isAuthenticated() && window.Auth?.isCustomer()) {
      try {
        const updatedCart = await window.API.post('/cart/items', { productId, quantity });
        this.currentCart = updatedCart;
        this.updateNavBadge();
        window.dispatchEvent(new CustomEvent('cart-updated', { detail: updatedCart }));
        window.UI?.showToast('Added to cart!', 'success');
        return updatedCart;
      } catch (err) {
        window.UI?.showToast(err.message, 'error');
        throw err;
      }
    } else {
      // Guest local storage
      const guestItems = this.getGuestCart();
      const existing = guestItems.find(i => i.productId === productId);
      if (existing) {
        existing.quantity += quantity;
      } else {
        guestItems.push({ productId, quantity });
      }
      this.setGuestCart(guestItems);
      window.UI?.showToast('Added to cart!', 'success');
      return { items: guestItems };
    }
  },

  async updateQuantity(productId, quantity) {
    if (window.Auth?.isAuthenticated() && window.Auth?.isCustomer()) {
      try {
        const updatedCart = await window.API.patch(`/cart/items/${productId}`, { quantity });
        this.currentCart = updatedCart;
        this.updateNavBadge();
        window.dispatchEvent(new CustomEvent('cart-updated', { detail: updatedCart }));
        return updatedCart;
      } catch (err) {
        window.UI?.showToast(err.message, 'error');
        throw err;
      }
    } else {
      // Guest local storage
      let guestItems = this.getGuestCart();
      if (quantity <= 0) {
        guestItems = guestItems.filter(i => i.productId !== productId);
      } else {
        const item = guestItems.find(i => i.productId === productId);
        if (item) {
          item.quantity = quantity;
        } else {
          guestItems.push({ productId, quantity });
        }
      }
      this.setGuestCart(guestItems);
      return { items: guestItems };
    }
  },

  async removeItem(productId) {
    return this.updateQuantity(productId, 0);
  },

  async clearCart() {
    if (window.Auth?.isAuthenticated() && window.Auth?.isCustomer()) {
      await window.API.delete('/cart');
      this.currentCart = null;
    }
    localStorage.removeItem(this.GUEST_CART_KEY);
    this.updateNavBadge();
    window.dispatchEvent(new CustomEvent('cart-updated', { detail: null }));
  },

  async mergeGuestCart() {
    const guestItems = this.getGuestCart();
    if (guestItems.length > 0) {
      try {
        const merged = await window.API.post('/cart/sync', guestItems);
        this.currentCart = merged;
        localStorage.removeItem(this.GUEST_CART_KEY);
        this.updateNavBadge();
      } catch (e) {
        console.error('Failed to merge guest cart:', e);
      }
    }
  },

  getItemQuantity(productId) {
    if (this.currentCart && this.currentCart.items) {
      const match = this.currentCart.items.find(i => i.productId === productId);
      return match ? match.quantity : 0;
    }
    const guestItems = this.getGuestCart();
    const match = guestItems.find(i => i.productId === productId);
    return match ? match.quantity : 0;
  },

  getTotalItemCount() {
    if (this.currentCart?.totalItemCount !== undefined) {
      return this.currentCart.totalItemCount;
    }
    const guestItems = this.getGuestCart();
    return guestItems.reduce((acc, i) => acc + (i.quantity || 0), 0);
  },

  updateNavBadge() {
    const count = this.getTotalItemCount();
    const badges = document.querySelectorAll('.cart-count-badge');
    badges.forEach(b => {
      b.textContent = count;
      b.style.display = count > 0 ? 'inline-block' : 'none';
    });
  }
};

window.Cart = Cart;
