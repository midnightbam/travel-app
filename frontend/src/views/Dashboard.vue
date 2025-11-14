<template>
  <div class="dashboard">
    <div class="dashboard-header">
      <h1>Welcome, {{ user?.username }}!</h1>
      <p>Manage your travel plans and explore destinations</p>
    </div>
    
    <div class="dashboard-stats">
      <div class="stat-card">
        <h3>{{ destinationCount }}</h3>
        <p>Available Destinations</p>
      </div>
      <div class="stat-card">
        <h3>{{ bookingCount }}</h3>
        <p>Your Bookings</p>
      </div>
      <div class="stat-card">
        <h3>{{ wishlistCount }}</h3>
        <p>Wishlist Items</p>
      </div>
    </div>
    
    <div class="quick-actions">
      <h2>Quick Actions</h2>
      <div class="action-buttons">
        <router-link to="/destinations" class="btn btn-primary">
          Browse Destinations
        </router-link>
        <button @click="refreshData" class="btn btn-success">
          Refresh Data
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import { authService } from '../services/auth.js'
import { travelService } from '../services/travel.js'

export default {
  name: 'Dashboard',
  data() {
    return {
      user: null,
      destinationCount: 0,
      bookingCount: 3,  // Mock data
      wishlistCount: 7  // Mock data
    }
  },
  async mounted() {
    await this.loadUserData()
    await this.loadDestinations()
  },
  methods: {
    async loadUserData() {
      try {
        this.user = authService.getUser()
        if (!this.user) {
          // Try to fetch from API
          this.user = await authService.getCurrentUser()
        }
      } catch (error) {
        console.error('Failed to load user data:', error)
      }
    },
    async loadDestinations() {
      try {
        const destinations = await travelService.getDestinations()
        this.destinationCount = destinations.length
      } catch (error) {
        console.error('Failed to load destinations:', error)
      }
    },
    async refreshData() {
      await this.loadDestinations()
      // You could add a success message here
    }
  }
}
</script>

<style scoped>
.dashboard-header {
  text-align: center;
  margin-bottom: 3rem;
}

.dashboard-header h1 {
  color: #2c3e50;
  margin-bottom: 0.5rem;
}

.dashboard-header p {
  color: #7f8c8d;
  font-size: 1.1rem;
}

.dashboard-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.5rem;
  margin-bottom: 3rem;
}

.stat-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
  border-left: 4px solid #3498db;
}

.stat-card h3 {
  font-size: 2.5rem;
  color: #3498db;
  margin-bottom: 0.5rem;
}

.stat-card p {
  color: #7f8c8d;
  font-weight: 500;
}

.quick-actions {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.quick-actions h2 {
  color: #2c3e50;
  margin-bottom: 1.5rem;
}

.action-buttons {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.action-buttons .btn {
  min-width: 160px;
}
</style>