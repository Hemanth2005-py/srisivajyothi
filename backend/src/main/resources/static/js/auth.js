/**
 * MYMART — Authentication Module
 */
const Auth = {
  TOKEN_KEY: 'mymart_auth_token',
  USER_KEY: 'mymart_auth_user',

  getToken() {
    return localStorage.getItem(this.TOKEN_KEY);
  },

  getUser() {
    try {
      const raw = localStorage.getItem(this.USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  },

  isAuthenticated() {
    return !!this.getToken() && !!this.getUser();
  },

  isAdmin() {
    const user = this.getUser();
    return user && user.role === 'ROLE_ADMIN';
  },

  isCustomer() {
    const user = this.getUser();
    return user && user.role === 'ROLE_CUSTOMER';
  },

  setSession(token, user) {
    if (token) localStorage.setItem(this.TOKEN_KEY, token);
    if (user) localStorage.setItem(this.USER_KEY, JSON.stringify(user));
    window.dispatchEvent(new CustomEvent('auth-state-changed', { detail: { user } }));
  },

  async login(emailOrUsername, password) {
    const res = await window.API.post('/auth/login', { emailOrUsername, password });
    this.setSession(res.token, res.user);

    // Record login event
    try {
      await window.API.post('/users/me/login-event', {});
    } catch (e) {
      console.debug('Login event recorded locally');
    }

    // Merge guest cart & wishlist if customer
    if (res.user.role === 'ROLE_CUSTOMER') {
      await window.Cart?.mergeGuestCart();
      await window.Wishlist?.mergeGuestWishlist();
    }

    return res.user;
  },

  async register(data) {
    const res = await window.API.post('/auth/register', data);
    this.setSession(res.token, res.user);

    if (res.user.role === 'ROLE_CUSTOMER') {
      await window.Cart?.mergeGuestCart();
      await window.Wishlist?.mergeGuestWishlist();
    }

    return res.user;
  },

  async loginWithDemo(role = 'customer') {
    const demoInfo = await window.API.get('/auth/demo-accounts');
    const target = role === 'admin' ? demoInfo.admin : demoInfo.customer;

    if (!target) throw new Error('Demo account not found');
    return await this.login(target.email, target.password);
  },

  async syncFirebaseUser(firebaseUser) {
    const res = await window.API.post('/auth/firebase-sync', {
      uid: firebaseUser.uid,
      email: firebaseUser.email,
      displayName: firebaseUser.displayName,
      phoneNumber: firebaseUser.phoneNumber
    });
    this.setSession(res.token, res.user);
    return res.user;
  },

  logout(redirect = true) {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    window.dispatchEvent(new CustomEvent('auth-state-changed', { detail: { user: null } }));
    if (redirect) {
      window.location.href = 'index.html';
    }
  },

  requireAdmin() {
    if (!this.isAuthenticated() || !this.isAdmin()) {
      window.location.href = 'admin-login.html';
      return false;
    }
    return true;
  },

  requireAuth(redirectUrl = 'login.html') {
    if (!this.isAuthenticated()) {
      window.location.href = `${redirectUrl}?redirect=${encodeURIComponent(window.location.pathname)}`;
      return false;
    }
    return true;
  }
};

window.Auth = Auth;
