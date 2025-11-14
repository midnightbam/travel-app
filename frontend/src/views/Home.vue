<template>
  <div class="home">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <h1>Discover Amazing Travel Experiences</h1>
        <p>Explore the most beautiful destinations and hidden gems from around the world</p>
      </div>
    </div>

    <!-- Search Section -->
    <div class="container">
      <SearchBar @search="handleSearch" />
      
      
      <!-- Error Message -->
      <div v-if="error" class="alert alert-error">
        {{ error }}
      </div>
      
      <!-- Loading State -->
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>Loading amazing trips...</p>
      </div>
      
      <!-- Search Results Info -->
      <div v-if="searchQuery && !loading" class="search-info">
        <p v-if="trips.length > 0">
          Found {{ trips.length }} trip(s) for "<strong>{{ searchQuery }}</strong>"
        </p>
        <p v-else>
          No trips found for "<strong>{{ searchQuery }}</strong>". Try a different keyword.
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
          v-for="trip in trips" 
          :key="trip.id" 
          :trip="trip" 
        />
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
      loading: true,
      error: null,
      searchQuery: ''
    }
  },
  async mounted() {
    await this.loadTrips()
  },
  methods: {
    async loadTrips() {
      this.loading = true
      this.error = null
      
      try {
        const response = await travelService.getTrips()
        this.trips = response.trips || []
      } catch (error) {
        this.error = 'Failed to load trips. Please try again later.'
        console.error('Error loading trips:', error)
      } finally {
        this.loading = false
      }
    },
    
    async handleSearch(query) {
      this.searchQuery = query
      this.loading = true
      this.error = null
      
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
    }
  }
}
</script>

<style scoped>
.hero-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  text-align: center;
  padding: 4rem 2rem;
  margin-bottom: 3rem;
}

.hero-content h1 {
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 1rem;
  line-height: 1.2;
}

.hero-content p {
  font-size: 1.25rem;
  opacity: 0.9;
  max-width: 600px;
  margin: 0 auto;
}

.search-info {
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #e6fffa;
  border: 1px solid #81e6d9;
  border-radius: 8px;
  color: #234e52;
}

/* Responsive Design */
@media (max-width: 768px) {
  .hero-section {
    padding: 3rem 1rem;
    margin-bottom: 2rem;
  }
  
  .hero-content h1 {
    font-size: 2rem;
  }
  
  .hero-content p {
    font-size: 1.1rem;
  }
}

@media (max-width: 480px) {
  .hero-content h1 {
    font-size: 1.75rem;
  }
  
  .hero-content p {
    font-size: 1rem;
  }
}
</style>