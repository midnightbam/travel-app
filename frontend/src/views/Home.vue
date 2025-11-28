<template>
  <div class="home">
    <!-- Search Section -->
    <div class="container">
      <SearchBar @search="handleSearch" />
      
      <!-- Logout Success Message -->
      <div v-if="logoutMessage" class="alert alert-success">
        {{ logoutMessage }}
      </div>
      
      <!-- Error Message -->
      <div v-if="error" class="alert alert-error">
        {{ error }}
      </div>
      
      <!-- Loading State -->
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>{{ loadingMessage }}</p>
      </div>
      
      <!-- Search Results Info -->
      <div v-if="searchQuery && !loading" class="search-info">
        <p v-if="trips.length > 0">
          Found {{ trips.length }} trip(s) for "<strong>{{ searchQuery }}</strong>"
        </p>
        <p v-else>
          ไม่พบสถานที่ท่องเที่ยวสำหรับ "<strong>{{ searchQuery }}</strong>" | No trips found for "<strong>{{ searchQuery }}</strong>"
        </p>
      </div>
      
      <!-- Empty State -->
      <div v-if="!loading && trips.length === 0 && !searchQuery" class="empty-state">
        <h3>No trips available</h3>
        <p>No trips have been added yet. Be the first to share your favorite spot!</p>
      </div>
      
      <!-- Trips Grid -->
      <div v-if="!loading && trips.length > 0" class="trips-grid">
        <TripCard 
          v-for="trip in displayedTrips" 
          :key="trip.id" 
          :trip="trip" 
        />
      </div>
      
      <!-- View More Button -->
      <div v-if="!loading && trips.length > visibleCount" class="view-more-container">
        <button @click="loadMore" class="btn-view-more">
          <span>View More Destinations</span>
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M19 9l-7 7-7-7" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <p class="showing-text">Showing {{ visibleCount }} of {{ trips.length }} trips</p>
      </div>
    </div>
  </div>
</template>

<script>
import { travelService } from '../services/travel.js'
import TripCard from '../components/TripCard.vue'
import SearchBar from '../components/SearchBar.vue'

export default {
  name: 'Home',
  components: {
    TripCard,
    SearchBar
  },
  data() {
    return {
      trips: [],
      visibleCount: 6,
      loading: true,
      error: null,
      searchQuery: '',
      loadingMessage: 'Loading amazing trips...',
      loadingTimer: null,
      logoutMessage: null
    }
  },
  computed: {
    displayedTrips() {
      return this.trips.slice(0, this.visibleCount)
    }
  },
  async mounted() {
    // Check for logout success message
    const logoutMsg = localStorage.getItem('logout_message')
    if (logoutMsg) {
      this.logoutMessage = logoutMsg
      localStorage.removeItem('logout_message')
      
      // Auto-dismiss after 5 seconds
      setTimeout(() => {
        this.logoutMessage = null
      }, 5000)
    }
    
    await this.loadTrips()
  },
  beforeUnmount() {
    if (this.loadingTimer) {
      clearTimeout(this.loadingTimer)
    }
  },
  methods: {
    async loadTrips() {
      this.loading = true
      this.error = null
      this.loadingMessage = 'Loading amazing trips...'
      
      // After 10 seconds, show a message about backend waking up
      this.loadingTimer = setTimeout(() => {
        this.loadingMessage = 'Waking up the server... This may take up to 60 seconds on first load.'
      }, 10000)
      
      try {
        const response = await travelService.getTrips()
        this.trips = response.trips || []
        if (this.loadingTimer) {
          clearTimeout(this.loadingTimer)
        }
      } catch (error) {
        this.error = 'Failed to load trips. The server may be starting up. Please refresh the page in a moment.'
        console.error('Error loading trips:', error)
        if (this.loadingTimer) {
          clearTimeout(this.loadingTimer)
        }
      } finally {
        this.loading = false
      }
    },
    
    async handleSearch(query) {
      this.searchQuery = query
      this.loading = true
      this.error = null
      this.visibleCount = 6 // Reset visible count on search
      
      try {
        if (query.trim()) {
          const response = await travelService.searchTrips(query)
          this.trips = response.trips || []
        } else {
          // If no search query, load all trips
          const response = await travelService.getTrips()
          this.trips = response.trips || []
        }
      } catch (error) {
        this.error = 'Failed to search trips. Please try again.'
        console.error('Error searching trips:', error)
      } finally {
        this.loading = false
      }
    },
    
    loadMore() {
      this.visibleCount += 6
    }
  }
}
</script>

<style scoped>
.search-info {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #e6fffa;
  border: 1px solid #81e6d9;
  border-radius: 8px;
  color: #234e52;
}

/* Alert styles */
.alert-success {
  background: #c6f6d5;
  color: #22543d;
  border-left: 4px solid #38a169;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  font-weight: 500;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* View More Section */
.view-more-container {
  text-align: center;
  margin: 3rem 0 2rem;
}

.btn-view-more {
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 2rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 1.1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-view-more:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 25px rgba(102, 126, 234, 0.6);
}

.btn-view-more:active {
  transform: translateY(0);
}

.btn-view-more svg {
  width: 20px;
  height: 20px;
  animation: bounce 2s infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(4px);
  }
}

.showing-text {
  margin-top: 1rem;
  color: #718096;
  font-size: 0.95rem;
}

/* Responsive Design */
@media (max-width: 768px) {
  .btn-view-more {
    width: 100%;
    justify-content: center;
  }
}
</style>