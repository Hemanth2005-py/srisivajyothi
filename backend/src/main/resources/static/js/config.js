/**
 * MYMART — Client Configuration
 */
const CONFIG = {
  // Use relative /api if served from same origin (or proxied), otherwise local Spring Boot port 8080
  API_BASE_URL: (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') && window.location.port !== '8080'
    ? 'http://localhost:8080/api'
    : (window.API_BASE_URL || '/api'),

  // Firebase Client SDK Configuration (configured via Firebase Console)
  FIREBASE_CONFIG: {
    apiKey: "AIzaSyDemoKeyForMymartApplication2026",
    authDomain: "mymart-app.firebaseapp.com",
    projectId: "mymart-app",
    storageBucket: "mymart-app.appspot.com",
    messagingSenderId: "123456789012",
    appId: "1:123456789012:web:abcdef123456"
  },

  DEFAULT_LOCATION: {
    city: "Bengaluru",
    area: "Indiranagar, 100ft Road",
    pincode: "560038"
  }
};

window.CONFIG = CONFIG;
