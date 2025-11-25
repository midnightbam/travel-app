<template>
  <div id="app">
    <nav class="navbar">
      <div class="nav-brand">
        <router-link to="/">Travel Explorer</router-link>
      </div>
      <button class="mobile-menu-toggle" @click="toggleMobileMenu" v-if="isMobile">
        <span></span>
        <span></span>
        <span></span>
      </button>
      <div class="nav-menu" :class="{ 'mobile-open': mobileMenuOpen }">
        <template v-if="isAuthenticated">
          <router-link to="/dashboard" @click="closeMobileMenu">Dashboard</router-link>
          <button @click="logout" class="logout-btn">Logout</button>
        </template>
        <template v-else>
          <router-link to="/login" class="nav-btn login-btn" @click="closeMobileMenu">Login</router-link>
          <router-link to="/register" class="nav-btn register-btn" @click="closeMobileMenu">Register</router-link>
        </template>
      </div>
    </nav>
    
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script>
import { authService } from './services/auth.js'

export default {
  name: 'App',
  data() {
    return {
      isAuthenticated: false,
      mobileMenuOpen: false,
      isMobile: false
    }
  },
  mounted() {
    this.checkAuth()
    this.checkMobile()
    window.addEventListener('resize', this.checkMobile)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.checkMobile)
  },
  methods: {
    checkAuth() {
      this.isAuthenticated = authService.isAuthenticated()
    },
    logout() {
      authService.logout()
      this.isAuthenticated = false
      this.closeMobileMenu()
    },
    checkMobile() {
      this.isMobile = window.innerWidth <= 768
      if (!this.isMobile) {
        this.mobileMenuOpen = false
      }
    },
    toggleMobileMenu() {
      this.mobileMenuOpen = !this.mobileMenuOpen
    },
    closeMobileMenu() {
      this.mobileMenuOpen = false
    }
  },
  watch: {
    $route() {
      this.checkAuth()
      this.closeMobileMenu()
    }
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: 'Inter', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  line-height: 1.6;
  color: #2d3748;
  background-color: #f7fafc;
}

#app {
  min-height: 100vh;
}

.navbar {
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 1rem 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
}

.nav-brand a {
  color: #e53e3e;
  text-decoration: none;
  font-size: 1.5rem;
  font-weight: 700;
  letter-spacing: -0.5px;
}

.nav-menu {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.nav-menu a {
  color: #4a5568;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.nav-menu a:hover,
.nav-menu a.router-link-active {
  color: #e53e3e;
}

.nav-btn {
  padding: 0.5rem 1rem !important;
  border-radius: 6px !important;
  transition: all 0.3s !important;
  font-weight: 500 !important;
}

.login-btn {
  background: transparent !important;
  border: 1px solid #e2e8f0 !important;
}

.login-btn:hover {
  background: #f7fafc !important;
  color: #2d3748 !important;
}

.register-btn {
  background: #e53e3e !important;
  color: white !important;
}

.register-btn:hover {
  background: #c53030 !important;
  color: white !important;
}

.logout-btn {
  background: #e53e3e;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  transition: background-color 0.3s;
}

.logout-btn:hover {
  background: #c53030;
}

.main-content {
  min-height: calc(100vh - 80px);
}

/* Container styles */
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

/* Form styles */
.form-container {
  max-width: 400px;
  margin: 2rem auto;
  padding: 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2d3748;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

.btn {
  background: #3182ce;
  color: white;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: background-color 0.3s;
  text-decoration: none;
  display: inline-block;
  text-align: center;
}

.btn:hover {
  background: #2c5282;
  color: white;
  text-decoration: none;
}

.btn-primary {
  background: #3182ce;
}

.btn-success {
  background: #38a169;
}

.btn-success:hover {
  background: #2f855a;
}

.btn-danger {
  background: #e53e3e;
}

.btn-danger:hover {
  background: #c53030;
}

/* Grid layouts */
.trips-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 1.5rem;
  margin-top: 2rem;
}

/* Alert styles */
.alert {
  padding: 1rem;
  margin-bottom: 1rem;
  border-radius: 8px;
  font-weight: 500;
}

.alert-error {
  background: #fed7d7;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.alert-success {
  background: #c6f6d5;
  color: #2f855a;
  border: 1px solid #9ae6b4;
}

.alert-info {
  background: #bee3f8;
  color: #2c5282;
  border: 1px solid #90cdf4;
}

/* Loading state */
.loading {
  text-align: center;
  padding: 3rem;
  color: #718096;
}

.loading-spinner {
  display: inline-block;
  width: 40px;
  height: 40px;
  border: 4px solid #e2e8f0;
  border-radius: 50%;
  border-top-color: #3182ce;
  animation: spin 1s ease-in-out infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #718096;
}

.empty-state h3 {
  color: #4a5568;
  margin-bottom: 1rem;
}

/* Mobile Menu Toggle */
.mobile-menu-toggle {
  display: none;
  flex-direction: column;
  gap: 4px;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
}

.mobile-menu-toggle span {
  display: block;
  width: 24px;
  height: 3px;
  background: #2d3748;
  border-radius: 2px;
  transition: all 0.3s ease;
}

/* Responsive Design */
@media (max-width: 768px) {
  .navbar {
    padding: 1rem;
    position: relative;
  }
  
  .nav-brand a {
    font-size: 1.25rem;
  }
  
  .mobile-menu-toggle {
    display: flex;
  }
  
  .nav-menu {
    position: fixed;
    top: 60px;
    right: -100%;
    width: 70%;
    max-width: 300px;
    height: calc(100vh - 60px);
    background: white;
    flex-direction: column;
    align-items: flex-start;
    padding: 2rem 1rem;
    box-shadow: -2px 0 10px rgba(0, 0, 0, 0.1);
    transition: right 0.3s ease;
    gap: 1rem;
  }
  
  .nav-menu.mobile-open {
    right: 0;
  }
  
  .nav-menu a,
  .nav-menu button {
    width: 100%;
    text-align: left;
    padding: 0.75rem 1rem !important;
    border-radius: 6px;
  }
  
  .nav-btn {
    padding: 0.75rem 1rem !important;
    font-size: 0.875rem !important;
  }
  
  .logout-btn {
    width: 100%;
  }
  
  .container {
    padding: 1rem;
  }
  
  .trips-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .form-container {
    margin: 1rem;
    padding: 1.5rem;
  }
}

@media (max-width: 480px) {
  .nav-brand a {
    font-size: 1.1rem;
  }
  
  .trips-grid {
    grid-template-columns: 1fr;
    gap: 0.75rem;
  }
}
</style>