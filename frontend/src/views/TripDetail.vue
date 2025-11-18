<template>
  <div class="trip-detail">
    <div class="container">
      <!-- Loading State -->
      <div v-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>Loading trip details...</p>
      </div>
      
      <!-- Error State -->
      <div v-else-if="error" class="alert alert-error">
        {{ error }}
        <button @click="$router.go(-1)" class="btn btn-primary" style="margin-left: 1rem;">
          Go Back
        </button>
      </div>
      
      <!-- Trip Content -->
      <div v-else-if="trip" class="trip-content">
        <!-- Header -->
        <div class="trip-header">
          <button @click="$router.go(-1)" class="back-btn">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M19 12H5M12 19L5 12L12 5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            Back
          </button>
          
          <h1 class="trip-title">{{ trip.title }}</h1>
          
          <div class="trip-meta">
            <span v-if="trip.province" class="province-badge">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z" fill="currentColor"/>
              </svg>
              {{ trip.province }}
            </span>
            <span v-if="trip.duration" class="meta-item">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm.5-13H11v6l5.2 3.2.8-1.3-4.5-2.7V7z" fill="currentColor"/>
              </svg>
              {{ trip.duration }}
            </span>
            <span v-if="trip.rating" class="meta-item rating">
              <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z"/>
              </svg>
              {{ trip.rating }}
            </span>
          </div>
        </div>
        
        <!-- Two Column Layout -->
        <div class="detail-layout">
          <!-- Left Column: Photos and Description -->
          <div class="detail-main">
            <!-- Photo Gallery -->
            <div class="photo-gallery" v-if="trip.photos && trip.photos.length > 0">
              <div class="main-photo">
                <img :src="currentPhoto" :alt="trip.title" @error="handleImageError" />
              </div>
              
              <div v-if="trip.photos.length > 1" class="photo-thumbnails">
                <img 
                  v-for="(photo, index) in trip.photos"
                  :key="index"
                  :src="photo"
                  :alt="`${trip.title} - Photo ${index + 1}`"
                  :class="{ active: currentPhoto === photo }"
                  @click="currentPhoto = photo"
                  @error="handleImageError"
                />
              </div>
            </div>

            <!-- Tags -->
            <div class="trip-tags" v-if="trip.tags && trip.tags.length > 0">
              <span class="tag" v-for="tag in trip.tags" :key="tag">
                {{ tag }}
              </span>
            </div>
            
            <!-- Description -->
            <div class="trip-description">
              <h2>About This Destination</h2>
              <div class="description-content">{{ trip.description }}</div>
            </div>

            <!-- Additional Info -->
            <div v-if="trip.location" class="address-section">
              <h3>
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z" fill="currentColor"/>
                </svg>
                Address
              </h3>
              <p>{{ trip.location }}</p>
            </div>
          </div>
          
          <!-- Right Column: Map -->
          <div class="detail-sidebar">
            <div class="map-section">
              <h3>Location</h3>
              
              <!-- Map -->
              <div v-if="hasCoordinates" class="map-container">
                <iframe
                  :src="googleMapEmbedUrl"
                  width="100%"
                  height="400"
                  style="border:0;"
                  allowfullscreen=""
                  loading="lazy"
                  referrerpolicy="no-referrer-when-downgrade"
                ></iframe>
                
                <a 
                  :href="googleMapLink" 
                  target="_blank" 
                  class="view-map-btn"
                >
                  <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" fill="currentColor"/>
                    <circle cx="12" cy="9" r="2.5" fill="white"/>
                  </svg>
                  View on Google Maps
                </a>
                
                <div class="coordinates">
                  <small>Coordinates: {{ trip.latitude }}, {{ trip.longitude }}</small>
                </div>
              </div>
              
              <!-- No Map Placeholder -->
              <div v-else class="map-placeholder">
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z" fill="currentColor"/>
                </svg>
                <p>Map information not available for this destination.</p>
              </div>

              <!-- Price Info -->
              <div v-if="trip.price" class="price-info">
                <h4>Estimated Cost</h4>
                <p class="price">฿{{ formatPrice(trip.price) }}</p>
                <small>Approximate cost per person</small>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { travelService } from '../services/travel.js'

export default {
  name: 'TripDetail',
  props: {
    id: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      trip: null,
      loading: true,
      error: null,
      currentPhoto: null
    }
  },
  computed: {
    hasCoordinates() {
      return this.trip && this.trip.latitude && this.trip.longitude
    },
    googleMapEmbedUrl() {
      if (!this.hasCoordinates) return ''
      return `https://www.google.com/maps/embed/v1/place?key=AIzaSyBFw0Qbyq9zTFTd-tUY6dZWTgaQzuU17R8&q=${this.trip.latitude},${this.trip.longitude}&zoom=15`
    },
    googleMapLink() {
      if (!this.hasCoordinates) return ''
      return `https://www.google.com/maps?q=${this.trip.latitude},${this.trip.longitude}`
    }
  },
  async mounted() {
    await this.loadTrip()
  },
  methods: {
    async loadTrip() {
      this.loading = true
      this.error = null
      
      try {
        this.trip = await travelService.getTrip(this.id)
        
        if (this.trip.photos && this.trip.photos.length > 0) {
          this.currentPhoto = this.trip.photos[0]
        }
        
      } catch (error) {
        this.error = 'Failed to load trip details. Please try again later.'
        console.error('Error loading trip:', error)
      } finally {
        this.loading = false
      }
    },
    
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    },
    
    formatPrice(price) {
      return price.toLocaleString('th-TH', { minimumFractionDigits: 0, maximumFractionDigits: 0 })
    },
    
    handleImageError(event) {
      event.target.src = '/placeholder-image.jpg'
    }
  }
}
</script>

<style scoped>
.trip-detail {
  min-height: calc(100vh - 80px);
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: none;
  border: none;
  color: #4a5568;
  cursor: pointer;
  font-size: 1rem;
  padding: 0.5rem;
  border-radius: 6px;
  transition: all 0.3s;
  margin-bottom: 1rem;
}

.back-btn:hover {
  background: #f7fafc;
  color: #2d3748;
}

.back-btn svg {
  width: 20px;
  height: 20px;
}

.trip-header {
  margin-bottom: 2rem;
}

.trip-title {
  font-size: 2.5rem;
  font-weight: 700;
  color: #2d3748;
  line-height: 1.2;
  margin-bottom: 1rem;
}

.trip-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  align-items: center;
}

.province-badge {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  background: #e53e3e;
  color: white;
  padding: 0.5rem 1rem;
  border-radius: 20px;
  font-weight: 500;
  font-size: 0.9rem;
}

.province-badge svg {
  width: 16px;
  height: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  color: #4a5568;
  font-size: 0.9rem;
}

.meta-item svg {
  width: 18px;
  height: 18px;
  color: #718096;
}

.meta-item.rating {
  color: #d69e2e;
  font-weight: 600;
}

.meta-item.rating svg {
  color: #d69e2e;
  width: 20px;
  height: 20px;
}

/* Two Column Layout */
.detail-layout {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: 2rem;
  align-items: start;
}

.detail-main {
  min-width: 0;
}

.detail-sidebar {
  position: sticky;
  top: 100px;
}

.photo-gallery {
  margin-bottom: 3rem;
}

.main-photo {
  width: 100%;
  max-height: 500px;
  overflow: hidden;
  border-radius: 12px;
  margin-bottom: 1rem;
}

.main-photo img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-thumbnails {
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  padding: 0.5rem 0;
}

.photo-thumbnails img {
  width: 80px;
  height: 80px;
  object-fit: cover;
  border-radius: 8px;
  cursor: pointer;
  opacity: 0.7;
  transition: opacity 0.3s;
  flex-shrink: 0;
}

.photo-thumbnails img:hover,
.photo-thumbnails img.active {
  opacity: 1;
}

.trip-info {
  max-width: 800px;
}

.trip-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-bottom: 2rem;
}

.tag {
  background: #e53e3e;
  color: white;
  padding: 0.375rem 0.75rem;
  border-radius: 20px;
  font-size: 0.875rem;
  font-weight: 500;
}

.trip-description h2,
.address-section h3,
.map-section h3 {
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.trip-description {
  margin-top: 2rem;
}

.description-content {
  color: #4a5568;
  line-height: 1.8;
  font-size: 1.05rem;
  white-space: pre-wrap;
}

.address-section {
  margin-top: 2rem;
  padding: 1.5rem;
  background: #f7fafc;
  border-radius: 12px;
  border-left: 4px solid #3182ce;
}

.address-section h3 {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.1rem;
  margin-bottom: 0.75rem;
}

.address-section svg {
  width: 20px;
  height: 20px;
  color: #3182ce;
}

.address-section p {
  color: #2d3748;
  font-size: 1rem;
  line-height: 1.6;
}

/* Map Section */
.map-section {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.map-section h3 {
  font-size: 1.25rem;
  margin-bottom: 1rem;
}

.map-container {
  margin-bottom: 1rem;
}

.map-container iframe {
  border-radius: 8px;
  display: block;
}

.view-map-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.875rem;
  margin-top: 1rem;
  background: #3182ce;
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: 500;
  transition: background-color 0.3s;
}

.view-map-btn:hover {
  background: #2c5282;
}

.view-map-btn svg {
  width: 20px;
  height: 20px;
}

.coordinates {
  margin-top: 0.75rem;
  text-align: center;
  color: #718096;
}

.map-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem 1rem;
  background: #f7fafc;
  border-radius: 8px;
  border: 2px dashed #cbd5e0;
  text-align: center;
}

.map-placeholder svg {
  width: 48px;
  height: 48px;
  color: #cbd5e0;
  margin-bottom: 1rem;
}

.map-placeholder p {
  color: #718096;
  font-size: 0.95rem;
}

.price-info {
  margin-top: 1.5rem;
  padding: 1.5rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  color: white;
  text-align: center;
}

.price-info h4 {
  font-size: 0.9rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
  opacity: 0.9;
}

.price-info .price {
  font-size: 2rem;
  font-weight: 700;
  margin: 0.5rem 0;
}

.price-info small {
  font-size: 0.85rem;
  opacity: 0.8;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .detail-layout {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  
  .detail-sidebar {
    position: static;
  }
}

@media (max-width: 768px) {
  .trip-title {
    font-size: 2rem;
  }
  
  .trip-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }
  
  .main-photo {
    max-height: 300px;
  }
  
  .photo-thumbnails img {
    width: 60px;
    height: 60px;
  }
  
  .description-content {
    font-size: 1rem;
  }
  
  .map-container iframe {
    height: 300px !important;
  }
}

@media (max-width: 480px) {
  .trip-title {
    font-size: 1.75rem;
  }
  
  .province-badge,
  .meta-item {
    font-size: 0.85rem;
  }
}
</style>