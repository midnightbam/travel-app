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
            <span v-if="trip.author" class="author">
              by {{ trip.author.displayName }}
            </span>
            <span class="date">
              {{ formatDate(trip.createdAt) }}
            </span>
          </div>
        </div>
        
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
        
        <!-- Trip Info -->
        <div class="trip-info">
          <div class="trip-tags" v-if="trip.tags && trip.tags.length > 0">
            <span class="tag" v-for="tag in trip.tags" :key="tag">
              {{ tag }}
            </span>
          </div>
          
          <div class="trip-description">
            <h3>About this trip</h3>
            <div class="description-content" v-html="formatDescription(trip.description)"></div>
          </div>
          
          <!-- Location Info -->
          <div v-if="trip.latitude && trip.longitude" class="location-info">
            <h3>Location</h3>
            <p>Coordinates: {{ trip.latitude }}, {{ trip.longitude }}</p>
            <!-- You could add a map component here -->
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
    
    formatDescription(description) {
      if (!description) return ''
      // Convert line breaks to <br> tags and handle simple formatting
      return description
        .replace(/\n\n/g, '</p><p>')
        .replace(/\n/g, '<br>')
        .replace(/^/, '<p>')
        .replace(/$/, '</p>')
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
  gap: 1rem;
  color: #718096;
  font-size: 0.9rem;
}

.author {
  font-weight: 500;
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

.trip-description h3,
.location-info h3 {
  color: #2d3748;
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1rem;
}

.description-content {
  color: #4a5568;
  line-height: 1.7;
  font-size: 1.1rem;
}

.description-content p {
  margin-bottom: 1rem;
}

.location-info {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 1px solid #e2e8f0;
}

/* Responsive Design */
@media (max-width: 768px) {
  .trip-title {
    font-size: 2rem;
  }
  
  .trip-meta {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .main-photo {
    max-height: 300px;
  }
  
  .photo-thumbnails img {
    width: 60px;
    height: 60px;
  }
}
</style>