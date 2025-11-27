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
          
          <!-- User Avatar Dropdown -->
          <div class="user-dropdown" @click.stop>
            <button @click="toggleUserDropdown" class="user-avatar-btn">
              <div class="user-avatar">
                <img :src="currentUser?.profileImage || getDefaultAvatar()" 
                     alt="Profile" 
                     class="avatar-image" 
                     @error="handleAvatarError">
              </div>
              <span class="user-name">{{ currentUser?.name || currentUser?.displayName || 'User' }}</span>
              <svg class="dropdown-icon" :class="{ 'rotated': userDropdownOpen }" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                <path d="M8 11l-4-4h8l-4 4z"/>
              </svg>
            </button>
            
            <div v-if="userDropdownOpen" class="dropdown-menu" @click.stop>
              <router-link to="/profile" @click="closeDropdowns" class="dropdown-item">
                <svg class="dropdown-item-icon" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                  <path d="M8 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6zm2-3a2 2 0 1 1-4 0 2 2 0 0 1 4 0zm4 8c0 1-1 1-1 1H3s-1 0-1-1 1-4 6-4 6 3 6 4zm-1-.004c-.001-.246-.154-.986-.832-1.664C11.516 10.68 10.289 10 8 10c-2.29 0-3.516.68-4.168 1.332-.678.678-.83 1.418-.832 1.664h10z"/>
                </svg>
                Profile
              </router-link>
              
              <div class="dropdown-divider"></div>
              
              <button @click="logout" class="dropdown-item logout-item">
                <svg class="dropdown-item-icon" width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
                  <path d="M10 12.5a.5.5 0 0 1-.5.5h-8a.5.5 0 0 1-.5-.5v-9a.5.5 0 0 1 .5-.5h8a.5.5 0 0 1 .5.5v2a.5.5 0 0 0 1 0v-2A1.5 1.5 0 0 0 9.5 2h-8A1.5 1.5 0 0 0 0 3.5v9A1.5 1.5 0 0 0 1.5 14h8a1.5 1.5 0 0 0 1.5-1.5v-2a.5.5 0 0 0-1 0v2z"/>
                  <path d="M15.854 8.354a.5.5 0 0 0 0-.708l-3-3a.5.5 0 0 0-.708.708L14.293 7.5H5.5a.5.5 0 0 0 0 1h8.793l-2.147 2.146a.5.5 0 0 0 .708.708l3-3z"/>
                </svg>
                Log out
              </button>
            </div>
          </div>
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
    
    <!-- Global Notification Container -->
    <NotificationContainer />
  </div>
</template>

<script>
import authService from './services/auth.js'
import NotificationContainer from './components/NotificationContainer.vue'

export default {
  name: 'App',
  components: {
    NotificationContainer
  },
  data() {
    return {
      isAuthenticated: false,
      mobileMenuOpen: false,
      isMobile: false,
      userDropdownOpen: false,
      currentUser: null
    }
  },
  mounted() {
    this.checkAuth()
    this.checkMobile()
    window.addEventListener('resize', this.checkMobile)
    document.addEventListener('click', this.closeDropdowns)
    
    // Listen for profile updates
    document.addEventListener('profile-updated', this.handleProfileUpdate)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.checkMobile)
    document.removeEventListener('click', this.closeDropdowns)
    document.removeEventListener('profile-updated', this.handleProfileUpdate)
  },
  methods: {
    checkAuth() {
      this.isAuthenticated = authService.isAuthenticated()
      if (this.isAuthenticated) {
        this.currentUser = authService.getUser()
        // Load fresh profile data if user is authenticated
        this.loadUserProfile()
      }
    },
    async loadUserProfile() {
      try {
        const profileData = await authService.getProfile()
        this.currentUser = { ...this.currentUser, ...profileData.data }
        // Store updated user data
        authService.setUser(this.currentUser)
      } catch (error) {
        console.error('Failed to load user profile:', error)
      }
    },
    handleAvatarError() {
      // If avatar image fails to load, remove it from currentUser
      if (this.currentUser) {
        this.currentUser.profileImage = null
      }
    },
    logout() {
      authService.logout()
      this.isAuthenticated = false
      this.currentUser = null
      this.closeMobileMenu()
      this.closeDropdowns()
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
    },
    toggleUserDropdown() {
      this.userDropdownOpen = !this.userDropdownOpen
    },
    closeDropdowns() {
      this.userDropdownOpen = false
      this.mobileMenuOpen = false
    },
    handleProfileUpdate(event) {
      // Update currentUser with new profile data
      this.currentUser = { ...this.currentUser, ...event.detail }
    },
    getDefaultAvatar() {
      // Return a default avatar image - using a simple SVG data URL
      return 'data:image/svg+xml;base64,' + btoa(`
        <svg width="150" height="150" viewBox="0 0 150 150" xmlns="http://www.w3.org/2000/svg">
          <circle cx="75" cy="75" r="75" fill="#6366f1"/>
          <circle cx="75" cy="60" r="25" fill="white"/>
          <ellipse cx="75" cy="120" rx="45" ry="25" fill="white"/>
        </svg>
      `)
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

/* User Avatar Dropdown Styles */
.user-dropdown {
  position: relative;
  display: inline-block;
}

.user-avatar-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 0.5rem;
  transition: background-color 0.2s;
}

.user-avatar-btn:hover {
  background: #f7fafc;
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #10b981;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.875rem;
  position: relative;
  overflow: hidden;
}

.avatar-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-initials {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.user-name {
  font-weight: 500;
  color: #4a5568;
  font-size: 0.875rem;
}

.dropdown-icon {
  color: #a0aec0;
  transition: transform 0.2s;
}

.dropdown-icon.rotated {
  transform: rotate(180deg);
}

.dropdown-menu {
  position: absolute;
  right: 0;
  top: 100%;
  margin-top: 0.5rem;
  background: white;
  border-radius: 0.5rem;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  border: 1px solid #e2e8f0;
  min-width: 200px;
  z-index: 50;
  padding: 0.5rem 0;
  animation: dropdownFadeIn 0.15s ease-out;
}

@keyframes dropdownFadeIn {
  from {
    opacity: 0;
    transform: translateY(-4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  color: #4a5568;
  text-decoration: none;
  background: none;
  border: none;
  width: 100%;
  text-align: left;
  cursor: pointer;
  font-size: 0.875rem;
  transition: background-color 0.15s;
}

.dropdown-item:hover {
  background: #f7fafc;
  color: #2d3748;
}

.dropdown-item-icon {
  flex-shrink: 0;
  color: #718096;
}

.dropdown-divider {
  height: 1px;
  background: #e2e8f0;
  margin: 0.5rem 0;
}

.logout-item {
  color: #e53e3e;
}

.logout-item:hover {
  background: #fed7d7;
  color: #c53030;
}

.logout-item .dropdown-item-icon {
  color: #e53e3e;
}

@media (max-width: 768px) {
  .user-dropdown {
    width: 100%;
  }
  
  .user-avatar-btn {
    width: 100%;
    justify-content: flex-start;
    padding: 1rem;
  }
  
  .dropdown-menu {
    position: static;
    box-shadow: none;
    border: none;
    border-top: 1px solid #e2e8f0;
    border-radius: 0;
    margin-top: 0;
  }
}
</style>