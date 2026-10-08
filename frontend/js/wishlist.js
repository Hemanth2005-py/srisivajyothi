/**
 * MYMART — Wishlist / Favorites Module
 */
const Wishlist = {
  GUEST_WISHLIST_KEY: 'mymart_guest_wishlist',
  favoritedIds: new Set(),

  getGuestWishlist() {
    try {
      const raw = localStorage.getItem(this.GUEST_WISHLIST_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  },

  async loadWishlist() {
    if (window.Auth?.isAuthenticated() && window.Auth?.isCustomer()) {
      try {
        const res = await window.API.get('/wishlist');
        this.favoritedIds = new Set((res.products || []).map(p => p.id));
        this.updateNavBadge();
        window.dispatchEvent(new CustomEvent('wishlist-updated', { detail: res }));
        return res;
      } catch (e) {
        console.warn('Failed to load online wishlist:', e);
      }
    }

    const guestList = this.getGuestWishlist();
    this.favoritedIds = new Set(guestList);
    this.updateNavBadge();
    return { products: [], count: guestList.length };
  },

  isFavorited(productId) {
    return this.favoritedIds.has(productId);
  },

  async toggleFavorite(productId) {
    if (window.Auth?.isAuthenticated() && window.Auth?.isCustomer()) {
      try {
        const res = await window.API.post(`/wishlist/${productId}/toggle`, {});
        const isFav = res.favorited;
        if (isFav) {
          this.favoritedIds.add(productId);
          window.UI?.showToast('Added to Favorites!', 'success');
        } else {
          this.favoritedIds.delete(productId);
          window.UI?.showToast('Removed from Favorites', 'info');
        }
        this.updateNavBadge();
        window.dispatchEvent(new CustomEvent('wishlist-updated'));
        return isFav;
      } catch (err) {
        window.UI?.showToast(err.message, 'error');
        throw err;
      }
    } else {
      // Guest local storage
      let guestList = this.getGuestWishlist();
      let isFav = false;
      if (guestList.includes(productId)) {
        guestList = guestList.filter(id => id !== productId);
        this.favoritedIds.delete(productId);
        window.UI?.showToast('Removed from Favorites', 'info');
      } else {
        guestList.push(productId);
        this.favoritedIds.add(productId);
        isFav = true;
        window.UI?.showToast('Added to Favorites!', 'success');
      }
      localStorage.setItem(this.GUEST_WISHLIST_KEY, JSON.stringify(guestList));
      this.updateNavBadge();
      window.dispatchEvent(new CustomEvent('wishlist-updated'));
      return isFav;
    }
  },

  async mergeGuestWishlist() {
    const guestList = this.getGuestWishlist();
    if (guestList.length > 0) {
      try {
        await window.API.post('/wishlist/sync', guestList);
        localStorage.removeItem(this.GUEST_WISHLIST_KEY);
        await this.loadWishlist();
      } catch (e) {
        console.error('Failed to merge guest wishlist:', e);
      }
    }
  },

  updateNavBadge() {
    const count = this.favoritedIds.size;
    const badges = document.querySelectorAll('.wishlist-count-badge');
    badges.forEach(b => {
      b.textContent = count;
      b.style.display = count > 0 ? 'inline-block' : 'none';
    });
  }
};

window.Wishlist = Wishlist;
