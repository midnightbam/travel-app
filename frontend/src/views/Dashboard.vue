<template>
  <div class="dashboard">
    <div class="container">
      <!-- Header -->
      <div class="dashboard-header">
        <div>
          <h1>My Trips Dashboard</h1>
          <p>Manage all your travel destinations</p>
        </div>
        <button @click="$router.push('/destination/add')" class="btn btn-primary">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
          </svg>
          Add Destination
        </button>
      </div>

      <!-- Success/Error Messages -->
      <div v-if="successMessage" class="alert alert-success">
        {{ successMessage }}
      </div>
      <div v-if="errorMessage" class="alert alert-error">
        {{ errorMessage }}
      </div>

      <!-- Unauthenticated State -->
      <div v-if="!isAuthenticated" class="auth-required-state">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z" fill="currentColor"/>
        </svg>
        <h3>Authentication Required</h3>
        <p>Please log in to manage your travel destinations</p>
        <div class="auth-buttons">
          <router-link to="/login" class="btn btn-primary">Log In</router-link>
          <router-link to="/register" class="btn btn-secondary">Create Account</router-link>
        </div>
      </div>

      <!-- Loading State -->
      <div v-else-if="loading" class="loading">
        <div class="loading-spinner"></div>
        <p>Loading your trips...</p>
      </div>

      <!-- Empty State -->
      <div v-else-if="trips.length === 0" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M3 6h18M3 12h18M3 18h18" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
        </svg>
        <h3>You haven't added any trips yet</h3>
        <p>Start sharing your travel experiences by adding your first destination!</p>
        <button @click="$router.push('/destination/add')" class="btn btn-primary">Add Your First Trip</button>
      </div>

      <!-- Trips Table (Desktop) -->
      <div v-else class="trips-table-container">
        <table class="trips-table">
          <thead>
            <tr>
              <th>Image</th>
              <th>Name</th>
              <th>Province</th>
              <th>Last Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="trip in paginatedTrips" :key="trip.id">
              <td>
                <img :src="trip.coverImage" :alt="trip.title" class="trip-thumbnail" @error="handleImageError" />
              </td>
              <td>
                <router-link :to="`/trip/${trip.id}`" class="trip-title-link">
                  {{ trip.title }}
                </router-link>
              </td>
              <td>
                <span v-if="trip.province" class="province-badge">{{ trip.province }}</span>
                <span v-else class="text-muted">-</span>
              </td>
              <td class="date-cell">{{ formatDate(trip.updatedAt || trip.createdAt) }}</td>
              <td>
                <div class="action-buttons" v-if="canEdit(trip)">
                  <button @click="$router.push(`/destination/edit/${trip.id}`)" class="btn-icon btn-edit" title="Edit">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </button>
                  <button @click="confirmDelete(trip)" class="btn-icon btn-delete" title="Delete">
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M3 6h18M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </button>
                </div>
                <span v-else class="text-muted" style="font-size: 0.875rem;">View only</span>
              </td>
            </tr>
          </tbody>
        </table>

        <!-- Mobile Cards -->
        <div class="trips-cards">
          <div v-for="trip in paginatedTrips" :key="trip.id" class="trip-card-mobile">
            <img :src="trip.coverImage" :alt="trip.title" class="trip-thumbnail-mobile" @error="handleImageError" />
            <div class="trip-card-content">
              <router-link :to="`/trip/${trip.id}`" class="trip-title-link">
                <h3>{{ trip.title }}</h3>
              </router-link>
              <div class="trip-meta-mobile">
                <span v-if="trip.province" class="province-badge">{{ trip.province }}</span>
                <span class="date-text">{{ formatDate(trip.updatedAt || trip.createdAt) }}</span>
              </div>
              <div class="action-buttons" v-if="canEdit(trip)">
                <button @click="$router.push(`/destination/edit/${trip.id}`)" class="btn btn-secondary">Edit</button>
                <button @click="confirmDelete(trip)" class="btn btn-danger">Delete</button>
              </div>
              <div v-else class="view-only-badge">
                <span class="text-muted">View only</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- Pagination and Trip Count -->
      <div v-if="trips.length > 0" class="pagination-wrapper">
        <div class="trips-count-bottom">
          <p>Showing {{ paginatedTrips.length }} of {{ trips.length }} trips</p>
        </div>
        <div v-if="showPagination" class="pagination">
        <button 
          @click="currentPage--" 
          :disabled="currentPage === 1"
          class="pagination-btn pagination-arrow"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        
        <button 
          v-for="page in visiblePages" 
          :key="page"
          @click="currentPage = page"
          :class="['pagination-btn', 'pagination-number', { active: currentPage === page }]"
        >
          {{ page }}
        </button>
        
        <button 
          @click="currentPage++" 
          :disabled="currentPage === totalPages"
          class="pagination-btn pagination-arrow"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M9 18L15 12L9 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        </div>
      </div>
    </div>

    <!-- Create/Edit Modal -->
    <div v-if="showCreateModal || showEditModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ showEditModal ? 'Edit Destination' : 'Add New Destination' }}</h2>
          <button @click="closeModal" class="modal-close">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
        
        <form @submit.prevent="submitForm" class="trip-form">
          <div class="form-group">
            <label>Title *</label>
            <input v-model="formData.title" type="text" required placeholder="e.g., Beautiful Beach in Phuket" />
          </div>

          <div class="form-group">
            <label>Province/Location *</label>
            <input v-model="formData.location" type="text" required placeholder="e.g., Phuket" />
          </div>

          <div class="form-group">
            <label>Description *</label>
            <textarea v-model="formData.description" rows="5" required placeholder="Describe this amazing place..."></textarea>
          </div>

          <div class="form-group">
            <label>Rating (1-5)</label>
            <input v-model="formData.rating" type="number" min="1" max="5" step="0.1" placeholder="e.g., 4.5" />
          </div>

          <!-- Enhanced Location Section -->
          <div class="form-group location-section">
            <label>Location *</label>
            <div class="location-input-options">
              <div class="location-tabs">
                <button 
                  type="button" 
                  @click="locationInputType = 'map'" 
                  :class="['location-tab', { active: locationInputType === 'map' }]"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                  </svg>
                  Select on Map
                </button>
                <button 
                  type="button" 
                  @click="locationInputType = 'manual'" 
                  :class="['location-tab', { active: locationInputType === 'manual' }]"
                >
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                    <path d="M3.9 12c0-1.71 1.39-3.1 3.1-3.1h4V7H6.99C5.28 7 4 8.28 4 10s1.28 3 2.99 3H11v2H7c-1.71 0-3.1-1.39-3.1-3.1zM8 13h8v-2H8v2zm9-6h-4v1.9h4c1.71 0 3.1 1.39 3.1 3.1s-1.39 3.1-3.1 3.1h-4V17h4c2.76 0 5-2.24 5-5s-2.24-5-5-5z"/>
                  </svg>
                  Add Link
                </button>
              </div>
              
              <!-- Map Selection -->
              <div v-if="locationInputType === 'map'" class="map-container">
                <div class="map-placeholder" @click="showMapModal = true">
                  <div v-if="!selectedLocation" class="map-prompt">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                    </svg>
                    <p>Click to open map and select location</p>
                  </div>
                  <div v-else class="selected-location-info">
                    <div class="location-preview">
                      <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
                      </svg>
                      <div>
                        <p class="coordinates">{{ selectedLocation.lat.toFixed(6) }}, {{ selectedLocation.lng.toFixed(6) }}</p>
                        <p class="address">{{ selectedLocation.address || 'Click to change location' }}</p>
                      </div>
                    </div>
                    <button type="button" @click.stop="clearLocation" class="clear-location-btn">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
              
              <!-- Manual Link Input -->
              <div v-if="locationInputType === 'manual'" class="manual-input-container">
                <div class="input-group">
                  <input 
                    v-model="formData.locationLink" 
                    type="url" 
                    placeholder="https://maps.google.com/... or other map link"
                    class="location-link-input"
                  />
                  <button type="button" @click="parseLocationLink" class="parse-link-btn" :disabled="!formData.locationLink">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                      <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/>
                    </svg>
                    Parse
                  </button>
                </div>
                <p class="input-help">Paste a Google Maps, Apple Maps, or other location link</p>
              </div>
              
              <!-- Current Location Display -->
              <div v-if="currentLocationData" class="current-location-display">
                <div class="location-summary">
                  <strong>Selected Location:</strong>
                  <p>Latitude: {{ currentLocationData.latitude }}</p>
                  <p>Longitude: {{ currentLocationData.longitude }}</p>
                  <p v-if="currentLocationData.address">Address: {{ currentLocationData.address }}</p>
                  <p v-if="currentLocationData.link">Link: <a :href="currentLocationData.link" target="_blank">{{ currentLocationData.link }}</a></p>
                </div>
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>Photos * (Minimum 2 required)</label>
            <div class="photo-uploader">
              <div v-if="photoList.length < 8" class="photo-drop-zone" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleDrop">
                <input ref="fileInput" type="file" multiple accept="image/*" @change="handleFileSelect" style="display: none" />
                <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
                <p>Drag & drop images or click to browse</p>
                <small>Supports: JPG, PNG, GIF (max 8 photos)</small>
              </div>
              
              <div v-if="photoList.length > 0" class="photo-preview-grid">
                <div 
                  v-for="(photo, index) in photoList" 
                  :key="index" 
                  class="photo-preview-item"
                  :class="{ 'is-main': index === mainPhotoIndex }"
                  draggable="true"
                  @dragstart="startDrag($event, index)"
                  @dragover.prevent
                  @drop="onDrop($event, index)"
                >
                  <img :src="photo" :alt="`Photo ${index + 1}`" />
                  <div class="photo-overlay">
                    <button type="button" @click="removePhoto(index)" class="photo-btn remove-btn" title="Remove">
                      <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M18 6L6 18M6 6l12 12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                      </svg>
                    </button>
                    <button type="button" @click="setMainPhoto(index)" class="photo-btn star-btn" :class="{ active: index === mainPhotoIndex }" title="Set as main photo">
                      <svg viewBox="0 0 24 24" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
                        <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                      </svg>
                    </button>
                  </div>
                  <div v-if="index === mainPhotoIndex" class="main-badge">Main Photo</div>
                  <div class="drag-handle">⋮⋮</div>
                </div>
              </div>
              
              <div v-if="photoList.length < 2" class="photo-warning">
                ⚠️ Please add at least 2 photos ({{ photoList.length }}/2)
              </div>
            </div>
          </div>

          <div class="form-group">
            <label>Tags (comma-separated)</label>
            <input v-model="tagsText" type="text" placeholder="beach, island, relaxation" />
          </div>

          <div class="form-actions">
            <button type="button" @click="closeModal" class="btn btn-secondary">Cancel</button>
            <button type="submit" class="btn btn-primary" :disabled="submitting">
              {{ submitting ? 'Saving...' : (showEditModal ? 'Update' : 'Create') }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Delete Confirmation Modal -->
    <div v-if="showDeleteModal" class="modal-overlay" @click.self="showDeleteModal = false">
      <div class="modal-content modal-small">
        <div class="modal-header">
          <h2>Confirm Delete</h2>
        </div>
        <div class="modal-body">
          <p>Are you sure you want to delete "<strong>{{ tripToDelete?.title }}</strong>"?</p>
          <p class="warning-text">This action cannot be undone.</p>
        </div>
        <div class="form-actions">
          <button @click="showDeleteModal = false" class="btn btn-secondary">Cancel</button>
          <button @click="deleteTrip" class="btn btn-danger" :disabled="deleting">
            {{ deleting ? 'Deleting...' : 'Delete' }}
          </button>
        </div>
      </div>
    </div>
  </div>

  <!-- Map Selector Modal -->
  <div v-if="showMapModal" class="modal-overlay" @click="showMapModal = false">
    <div class="modal-content map-modal" @click.stop>
      <div class="modal-header">
        <h3>Select Location</h3>
        <button @click="showMapModal = false" class="close-btn">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </button>
      </div>
      <div class="modal-body">
        <EmbeddedMapSelector @location-selected="onLocationSelected" />
      </div>
    </div>
  </div>
</template>

<script>
import { travelService } from '../services/travel.js'
import authService from '../services/auth.js'
import { useNotification } from '../composables/useNotification.js'
import EmbeddedMapSelector from '../components/EmbeddedMapSelector.vue'

export default {
  name: 'Dashboard',
  components: {
    EmbeddedMapSelector
  },
  setup() {
    const { showNotification } = useNotification()
    return { showNotification }
  },
  data() {
    return {
      trips: [],
      currentUserId: null,
      loading: true,
      successMessage: '',
      errorMessage: '',
      showCreateModal: false,
      showEditModal: false,
      showDeleteModal: false,
      tripToDelete: null,
      editingTrip: null,
      submitting: false,
      deleting: false,
      currentPage: 1,
      itemsPerPage: 10,
      formData: {
        title: '',
        location: '',
        description: '',
        rating: null,
        locationLink: '',
        latitude: null,
        longitude: null,
        isManualLink: false
      },
      photoList: [],
      mainPhotoIndex: 0,
      draggedIndex: null,
      tagsText: '',
      locationInputType: 'map',
      selectedLocation: null,
      showMapModal: false,
      currentLocationData: null
    }
  },
  computed: {
    isAuthenticated() {
      return authService.isAuthenticated()
    },
    canEdit() {
      return (trip) => {
        // Allow editing if no author (legacy trips) or user owns the trip
        return !trip.authorId || trip.authorId === this.currentUserId
      }
    },
    paginatedTrips() {
      const start = (this.currentPage - 1) * this.itemsPerPage
      const end = start + this.itemsPerPage
      return this.trips.slice(start, end)
    },
    totalPages() {
      return Math.ceil(this.trips.length / this.itemsPerPage)
    },
    showPagination() {
      const shouldShow = this.trips.length > this.itemsPerPage
      console.log('Pagination debug:', {
        tripsLength: this.trips.length,
        itemsPerPage: this.itemsPerPage,
        shouldShow: shouldShow,
        totalPages: this.totalPages,
        currentPage: this.currentPage
      })
      return shouldShow
    },
    visiblePages() {
      const pages = []
      const total = this.totalPages
      const current = this.currentPage
      
      // Always show first page
      pages.push(1)
      
      // Show pages around current page
      for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
        pages.push(i)
      }
      
      // Always show last page if more than 1 page
      if (total > 1) {
        pages.push(total)
      }
      
      // Remove duplicates and sort
      return [...new Set(pages)].sort((a, b) => a - b)
    }
  },
  async mounted() {
    await this.loadTrips()
  },
  async beforeRouteEnter(to, from, next) {
    // Refresh data when entering dashboard
    next(vm => {
      if (from.path.includes('/destination/')) {
        vm.loadTrips()
      }
    })
  },
  methods: {
    async loadTrips() {
      this.loading = true
      this.errorMessage = ''
      
      // Check authentication first
      if (!authService.isAuthenticated()) {
        this.$router.push('/login')
        return
      }
      
      try {
        const response = await travelService.getMyTrips()
        this.trips = response.trips || []
        this.currentUserId = response.userId // Store current user ID for ownership checks
      } catch (error) {
        // Handle 401 Unauthorized
        if (error.response?.status === 401) {
          this.errorMessage = 'Please log in to continue.'
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        } else {
          this.errorMessage = 'Failed to load your trips. Please try again.'
        }
        console.error('Error loading trips:', error)
      } finally {
        this.loading = false
      }
    },

    editTrip(trip) {
      this.editingTrip = trip
      this.formData = {
        title: trip.title,
        location: trip.location || '',
        description: trip.description,
        rating: trip.rating,
        locationLink: trip.locationLink || ''
      }
      this.photoList = trip.photos ? [...trip.photos] : []
      this.mainPhotoIndex = 0
      this.tagsText = trip.tags ? trip.tags.join(', ') : ''
      this.showEditModal = true
    },

    confirmDelete(trip) {
      this.tripToDelete = trip
      this.showDeleteModal = true
    },

    async deleteTrip() {
      this.deleting = true
      this.errorMessage = ''
      
      try {
        await travelService.deleteTrip(this.tripToDelete.id)
        this.successMessage = 'Trip deleted successfully!'
        this.showDeleteModal = false
        this.tripToDelete = null
        await this.loadTrips()
        
        setTimeout(() => {
          this.successMessage = ''
        }, 3000)
      } catch (error) {
        this.showDeleteModal = false
        // Handle 401 Unauthorized
        if (error.response?.status === 401) {
          this.errorMessage = 'Please log in to continue.'
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        }
        // Handle 403 Forbidden specifically
        else if (error.response?.status === 403) {
          this.errorMessage = 'You can only edit or delete your own trips.'
        } else {
          this.errorMessage = error.message || 'Failed to delete trip'
        }
        
        // Auto-dismiss error after 5 seconds
        setTimeout(() => {
          this.errorMessage = ''
        }, 5000)
      } finally {
        this.deleting = false
      }
    },

    async submitForm() {
      this.submitting = true
      this.errorMessage = ''
      
      try {
        // Prepare data
        const data = {
          ...this.formData,
          photos: this.photosText.split('\n').filter(url => url.trim()).map(url => url.trim()),
          tags: this.tagsText.split(',').filter(tag => tag.trim()).map(tag => tag.trim())
        }

        if (this.showEditModal) {
          await travelService.updateTrip(this.editingTrip.id, data)
          this.successMessage = 'Trip updated successfully!'
        } else {
          await travelService.createTrip(data)
          this.successMessage = 'Trip created successfully!'
        }

        this.closeModal()
        await this.loadTrips()
        
        setTimeout(() => {
          this.successMessage = ''
        }, 3000)
      } catch (error) {
        this.closeModal()
        // Handle 401 Unauthorized
        if (error.response?.status === 401) {
          this.errorMessage = 'Please log in to continue.'
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        }
        // Handle 403 Forbidden specifically
        else if (error.response?.status === 403) {
          this.errorMessage = 'You can only edit or delete your own trips.'
        } else {
          this.errorMessage = error.message || 'Failed to save trip'
        }
        
        // Auto-dismiss error after 5 seconds
        setTimeout(() => {
          this.errorMessage = ''
        }, 5000)
      } finally {
        this.submitting = false
      }
    },

    closeModal() {
      this.showCreateModal = false
      this.showEditModal = false
      this.editingTrip = null
      this.formData = {
        title: '',
        location: '',
        description: '',
        rating: null,
        locationLink: ''
      }
      this.photoList = []
      this.mainPhotoIndex = 0
      this.tagsText = ''
    },
    
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    
    handleFileSelect(event) {
      const files = Array.from(event.target.files)
      this.processFiles(files)
    },
    
    handleDrop(event) {
      const files = Array.from(event.dataTransfer.files)
      this.processFiles(files)
    },
    
    processFiles(files) {
      const imageFiles = files.filter(file => file.type.startsWith('image/'))
      
      if (this.photoList.length + imageFiles.length > 8) {
        this.errorMessage = 'Maximum 8 photos allowed'
        setTimeout(() => this.errorMessage = '', 3000)
        return
      }
      
      imageFiles.forEach(file => {
        const reader = new FileReader()
        reader.onload = (e) => {
          this.photoList.push(e.target.result)
        }
        reader.readAsDataURL(file)
      })
    },
    
    removePhoto(index) {
      this.photoList.splice(index, 1)
      if (this.mainPhotoIndex >= this.photoList.length) {
        this.mainPhotoIndex = Math.max(0, this.photoList.length - 1)
      } else if (index < this.mainPhotoIndex) {
        this.mainPhotoIndex--
      } else if (index === this.mainPhotoIndex && this.photoList.length > 0) {
        this.mainPhotoIndex = 0
      }
    },
    
    setMainPhoto(index) {
      this.mainPhotoIndex = index
    },
    
    startDrag(event, index) {
      this.draggedIndex = index
      event.dataTransfer.effectAllowed = 'move'
    },
    
    onDrop(event, dropIndex) {
      if (this.draggedIndex === null) return
      
      const photos = [...this.photoList]
      const draggedPhoto = photos[this.draggedIndex]
      
      photos.splice(this.draggedIndex, 1)
      photos.splice(dropIndex, 0, draggedPhoto)
      
      // Update main photo index if needed
      if (this.draggedIndex === this.mainPhotoIndex) {
        this.mainPhotoIndex = dropIndex
      } else if (this.draggedIndex < this.mainPhotoIndex && dropIndex >= this.mainPhotoIndex) {
        this.mainPhotoIndex--
      } else if (this.draggedIndex > this.mainPhotoIndex && dropIndex <= this.mainPhotoIndex) {
        this.mainPhotoIndex++
      }
      
      this.photoList = photos
      this.draggedIndex = null
    },

    formatDate(dateString) {
      if (!dateString) return '-'
      const date = new Date(dateString)
      return date.toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      })
    },

    handleImageError(event) {
      event.target.src = '/placeholder-image.jpg'
    },

    // Location handling methods
    clearLocation() {
      this.selectedLocation = null
      this.formData.latitude = null
      this.formData.longitude = null
      this.currentLocationData = null
    },

    onLocationSelected(location) {
      this.selectedLocation = location
      this.formData.latitude = location.lat
      this.formData.longitude = location.lng
      this.formData.location = location.address || `${location.lat.toFixed(6)}, ${location.lng.toFixed(6)}`
      this.formData.isManualLink = false
      this.showMapModal = false
      
      this.currentLocationData = {
        latitude: location.lat,
        longitude: location.lng,
        address: location.address
      }
      
      this.showNotification('Location selected successfully!', 'success')
    },

    async parseLocationLink() {
      if (!this.formData.locationLink) {
        this.showNotification('Please enter a location link', 'warning')
        return
      }

      try {
        const link = this.formData.locationLink.trim()
        let lat, lng

        // Parse Google Maps links
        if (link.includes('google.com/maps') || link.includes('maps.google.com')) {
          // Different Google Maps formats
          const patterns = [
            /@(-?\d+\.?\d*),(-?\d+\.?\d*)/,  // @lat,lng
            /!3d(-?\d+\.?\d*)!4d(-?\d+\.?\d*)/,  // !3dlat!4dlng
            /q=(-?\d+\.?\d*),(-?\d+\.?\d*)/,  // q=lat,lng
            /ll=(-?\d+\.?\d*),(-?\d+\.?\d*)/   // ll=lat,lng
          ]
          
          for (const pattern of patterns) {
            const match = link.match(pattern)
            if (match) {
              lat = parseFloat(match[1])
              lng = parseFloat(match[2])
              break
            }
          }
        }
        
        // Parse Apple Maps links
        else if (link.includes('maps.apple.com')) {
          const match = link.match(/ll=(-?\d+\.?\d*),(-?\d+\.?\d*)/);
          if (match) {
            lat = parseFloat(match[1])
            lng = parseFloat(match[2])
          }
        }
        
        // Parse direct coordinates
        else if (link.match(/^-?\d+\.?\d*,-?\d+\.?\d*$/)) {
          const coords = link.split(',')
          lat = parseFloat(coords[0])
          lng = parseFloat(coords[1])
        }

        if (lat && lng && !isNaN(lat) && !isNaN(lng)) {
          this.formData.latitude = lat
          this.formData.longitude = lng
          this.formData.location = `${lat.toFixed(6)}, ${lng.toFixed(6)}`
          this.formData.isManualLink = true
          
          this.currentLocationData = {
            latitude: lat,
            longitude: lng,
            link: this.formData.locationLink
          }
          
          this.showNotification('Location parsed successfully!', 'success')
        } else {
          throw new Error('Could not parse coordinates from link')
        }
      } catch (error) {
        console.error('Error parsing location link:', error)
        this.showNotification('Could not parse location from the provided link. Please check the URL format.', 'error')
      }
    }
  }
}
</script>

<style scoped>
.dashboard {
  min-height: calc(100vh - 80px);
  padding: 2rem 0;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  gap: 1rem;
}

.dashboard-header h1 {
  font-size: 2rem;
  font-weight: 700;
  color: #2d3748;
  margin: 0 0 0.5rem 0;
}

.dashboard-header p {
  color: #718096;
  margin: 0;
}

.btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 8px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  text-decoration: none;
}

.btn svg {
  width: 20px;
  height: 20px;
}

.btn-primary {
  background: #3182ce;
  color: white;
}

.btn-primary:hover {
  background: #2c5282;
}

.btn-secondary {
  background: #e2e8f0;
  color: #2d3748;
}

.btn-secondary:hover {
  background: #cbd5e0;
}

.btn-danger {
  background: #e53e3e;
  color: white;
}

.btn-danger:hover {
  background: #c53030;
}

/* Table Styles */
.trips-table-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.trips-table {
  width: 100%;
  border-collapse: collapse;
}

.trips-table thead {
  background: #f7fafc;
}

.trips-table th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: #2d3748;
  border-bottom: 2px solid #e2e8f0;
}

.trips-table td {
  padding: 1rem;
  border-bottom: 1px solid #e2e8f0;
  vertical-align: middle;
}

.trip-thumbnail {
  width: 80px;
  height: 60px;
  object-fit: cover;
  border-radius: 8px;
}

.trip-title-link {
  color: #3182ce;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.trip-title-link:hover {
  color: #2c5282;
  text-decoration: underline;
}

.province-badge {
  background: #e53e3e;
  color: white;
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.875rem;
  font-weight: 500;
}

.date-cell {
  color: #718096;
  font-size: 0.9rem;
}

.action-buttons {
  display: flex;
  gap: 0.5rem;
}

.btn-icon {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-icon svg {
  width: 18px;
  height: 18px;
}

.btn-edit {
  background: #bee3f8;
  color: #2c5282;
}

.btn-edit:hover {
  background: #90cdf4;
}

.btn-delete {
  background: #fed7d7;
  color: #c53030;
}

.btn-delete:hover {
  background: #feb2b2;
}

/* Mobile Cards - Hidden by default */
.trips-cards {
  display: none;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: white;
  border-radius: 16px;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

/* Custom scrollbar to preserve rounded corners */
.modal-content::-webkit-scrollbar {
  width: 8px;
}

.modal-content::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 16px;
}

.modal-content::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 4px;
}

.modal-content::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}

.modal-small {
  max-width: 400px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #2d3748;
}

.modal-close {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.3s;
}

.modal-close:hover {
  background: #f7fafc;
}

.modal-close svg {
  width: 20px;
  height: 20px;
  color: #4a5568;
}

.modal-body {
  padding: 1.5rem;
}

.warning-text {
  color: #e53e3e;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

/* Form Styles */
.trip-form {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1.25rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  color: #2d3748;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  font-size: 1rem;
  transition: border-color 0.3s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

.form-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
}

.form-actions {
  display: flex;
  gap: 1rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid #e2e8f0;
}

/* Alerts */
.alert {
  padding: 1rem 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  font-weight: 500;
  animation: slideDown 0.3s ease-out;
}

.alert-success {
  background: #c6f6d5;
  color: #22543d;
  border-left: 4px solid #38a169;
}

.alert-error {
  background: #fed7d7;
  color: #742a2a;
  border-left: 4px solid #e53e3e;
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

/* View Only Badge */
.view-only-badge {
  padding: 0.5rem 0;
}

.text-muted {
  color: #a0aec0;
  font-style: italic;
}

/* Photo Uploader Styles */
.photo-uploader {
  margin-top: 0.5rem;
}

.photo-drop-zone {
  border: 2px dashed #cbd5e0;
  border-radius: 8px;
  padding: 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  background: #f7fafc;
  margin-bottom: 1rem;
}

.photo-drop-zone:hover {
  border-color: #3182ce;
  background: #ebf8ff;
}

.photo-drop-zone svg {
  width: 32px;
  height: 32px;
  color: #718096;
  margin-bottom: 0.5rem;
}

.photo-drop-zone p {
  margin: 0.5rem 0 0.25rem 0;
  color: #2d3748;
  font-weight: 500;
}

.photo-drop-zone small {
  color: #718096;
  font-size: 0.875rem;
}

.photo-preview-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}

.photo-preview-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  cursor: move;
  border: 2px solid transparent;
  transition: border-color 0.2s;
}

.photo-preview-item:hover {
  border-color: #3182ce;
}

.photo-preview-item.is-main {
  border-color: #f6ad55;
  box-shadow: 0 0 0 3px rgba(246, 173, 85, 0.2);
}

.photo-preview-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.photo-overlay {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  display: flex;
  gap: 0.25rem;
}

.photo-btn {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 4px;
  padding: 0.25rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.photo-btn svg {
  width: 14px;
  height: 14px;
}

.remove-btn {
  color: #e53e3e;
}

.remove-btn:hover {
  background: #fed7d7;
}

.star-btn {
  color: #d69e2e;
}

.star-btn.active {
  background: #f6ad55;
  color: white;
}

.star-btn:hover {
  background: #faf089;
}

.main-badge {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(246, 173, 85, 0.9);
  color: white;
  text-align: center;
  font-size: 0.75rem;
  font-weight: 600;
  padding: 0.25rem;
}

/* Authentication Required State */
.auth-required-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.auth-required-state svg {
  width: 80px;
  height: 80px;
  color: #3182ce;
  margin-bottom: 1.5rem;
}

.auth-required-state h3 {
  font-size: 1.75rem;
  color: #2d3748;
  margin-bottom: 0.75rem;
}

.auth-required-state p {
  color: #718096;
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.auth-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.empty-state svg {
  width: 64px;
  height: 64px;
  color: #cbd5e0;
  margin-bottom: 1rem;
}

.empty-state h3 {
  font-size: 1.5rem;
  color: #2d3748;
  margin-bottom: 0.5rem;
}

.empty-state p {
  color: #718096;
  margin-bottom: 1.5rem;
}

/* Responsive Design */
@media (max-width: 1024px) {
  .trips-table {
    display: none;
  }

  .trips-cards {
    display: block;
    padding: 1rem;
  }

  .trip-card-mobile {
    background: white;
    border-radius: 12px;
    padding: 1rem;
    margin-bottom: 1rem;
    display: flex;
    gap: 1rem;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .trip-thumbnail-mobile {
    width: 100px;
    height: 100px;
    object-fit: cover;
    border-radius: 8px;
    flex-shrink: 0;
  }

  .trip-card-content {
    flex: 1;
    min-width: 0;
  }

  .trip-card-content h3 {
    margin: 0 0 0.5rem 0;
    font-size: 1.1rem;
  }

  .trip-meta-mobile {
    display: flex;
    flex-wrap: wrap;
    gap: 0.5rem;
    margin-bottom: 0.75rem;
    align-items: center;
  }

  .date-text {
    color: #718096;
    font-size: 0.875rem;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dashboard-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .dashboard-header h1 {
    font-size: 1.5rem;
  }

  .modal-content {
    max-height: 95vh;
  }

  .form-actions {
    flex-direction: column-reverse;
  }

  .form-actions .btn {
    width: 100%;
    justify-content: center;
  }
}

/* Location Section Styles */
.location-section {
  margin-bottom: 24px;
}

.location-input-options {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  background: #f8fafc;
}

.location-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.location-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  color: #64748b;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.location-tab:hover {
  border-color: #10b981;
  color: #10b981;
}

.location-tab.active {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  border-color: #10b981;
}

.map-container {
  margin-top: 16px;
}

.map-placeholder {
  border: 2px dashed #cbd5e0;
  border-radius: 8px;
  padding: 32px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  min-height: 120px;
  background: white;
}

.map-placeholder:hover {
  border-color: #10b981;
  background: #f0fdf4;
}

.map-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  color: #64748b;
}

.map-prompt svg {
  opacity: 0.6;
}

.selected-location-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  background: #f0fdf4;
  border: 1px solid #10b981;
  border-radius: 8px;
}

.location-preview {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.location-preview svg {
  color: #10b981;
  margin-top: 2px;
}

.coordinates {
  font-weight: 600;
  color: #064e3b;
  margin: 0 0 4px 0;
  font-size: 14px;
}

.address {
  color: #065f46;
  margin: 0;
  font-size: 13px;
}

.clear-location-btn {
  background: #ef4444;
  color: white;
  border: none;
  border-radius: 4px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.clear-location-btn:hover {
  background: #dc2626;
}

.manual-input-container {
  margin-top: 16px;
}

.input-group {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.location-link-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s ease;
}

.location-link-input:focus {
  outline: none;
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.parse-link-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 16px;
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.parse-link-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #059669, #047857);
  transform: translateY(-1px);
}

.parse-link-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  transform: none;
}

.input-help {
  font-size: 12px;
  color: #64748b;
  margin: 0;
}

.current-location-display {
  margin-top: 16px;
  padding: 16px;
  background: #f0fdf4;
  border: 1px solid #10b981;
  border-radius: 8px;
}

.location-summary {
  color: #065f46;
}

.location-summary strong {
  color: #064e3b;
}

.location-summary p {
  margin: 4px 0;
  font-size: 14px;
}

.location-summary a {
  color: #10b981;
  text-decoration: none;
}

.location-summary a:hover {
  text-decoration: underline;
}

/* Map Modal Styles */
.map-modal {
  width: 90vw;
  max-width: 800px;
  height: 80vh;
  max-height: 600px;
  padding: 0;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e2e8f0;
  background: white;
  border-radius: 12px 12px 0 0;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  color: #64748b;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.close-btn:hover {
  color: #ef4444;
  background: #fef2f2;
}

.modal-body {
  flex: 1;
  padding: 0;
  overflow: hidden;
}

/* Pagination Wrapper */
.pagination-wrapper {
  margin-top: 24px;
}

.trips-count-bottom {
  padding: 16px 0;
  text-align: left;
}

.trips-count-bottom p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
  font-weight: 500;
}

/* Pagination Styles */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px 0;
}

.pagination-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  height: 44px;
  border: 1px solid #e2e8f0;
  background: white;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #64748b;
  font-size: 15px;
  font-weight: 500;
  padding: 0 12px;
}

.pagination-arrow {
  min-width: 44px;
  padding: 0;
}

.pagination-number {
  min-width: 44px;
}

.pagination-btn:hover:not(:disabled):not(.active) {
  background: #f8fafc;
  border-color: #cbd5e0;
  color: #1f2937;
}

.pagination-btn.active {
  background: #ff6b35;
  border-color: #ff6b35;
  color: white;
  font-weight: 600;
}

.pagination-btn:disabled {
  opacity: 0.3;
  cursor: not-allowed;
  background: white;
}

/* Responsive Pagination */
@media (max-width: 768px) {
  .pagination {
    gap: 6px;
    padding: 16px;
  }
  
  .pagination-btn {
    min-width: 40px;
    height: 40px;
    font-size: 14px;
  }
  
  .pagination-arrow {
    min-width: 40px;
  }
  
  .pagination-number {
    min-width: 40px;
  }
}
</style>
