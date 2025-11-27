<template>
  <div class="destinations">
    <div class="destinations-header">
      <h1>Travel Destinations</h1>
      <button @click="showAddForm = true" class="btn btn-success">
        Add New Destination
      </button>
    </div>
    
    <div v-if="error" class="alert alert-error">
      {{ error }}
    </div>
    
    <div v-if="showAddForm" class="add-form card">
      <h3>Add New Destination</h3>
      <form @submit.prevent="addDestination">
        <div class="form-group">
          <label>Name:</label>
          <input v-model="newDestination.name" type="text" required />
        </div>
        <div class="form-group">
          <label>Country:</label>
          <input v-model="newDestination.country" type="text" required />
        </div>
        <div class="form-group">
          <label>Description:</label>
          <input v-model="newDestination.description" type="text" required />
        </div>
        <div class="form-group">
          <label>Price ($):</label>
          <input v-model.number="newDestination.price" type="number" required />
        </div>
        
        <!-- Enhanced Location Section -->
        <div class="form-group location-section">
          <label>Location:</label>
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
    
    <!-- Map Selector Modal -->
    <MapSelector 
      :show-modal="showMapModal"
      :initial-location="selectedLocation"
      @location-selected="onLocationSelected"
      @close="showMapModal = false"
    />            <!-- Manual Link Input -->
            <div v-if="locationInputType === 'manual'" class="manual-input-container">
              <div class="input-group">
                <input 
                  v-model="newDestination.locationLink" 
                  type="url" 
                  placeholder="https://maps.google.com/... or other map link"
                  class="location-link-input"
                />
                <button type="button" @click="parseLocationLink" class="parse-link-btn" :disabled="!newDestination.locationLink">
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
        <div class="form-actions">
          <button type="submit" class="btn btn-primary">Add Destination</button>
          <button type="button" @click="cancelAdd" class="btn">Cancel</button>
        </div>
      </form>
    </div>
    
    <div v-if="loading" class="loading">Loading destinations...</div>
    
    <div v-else class="destinations-grid">
      <div v-for="destination in destinations" :key="destination.id" class="destination-card">
        <h3>{{ destination.name }}</h3>
        <p class="country">{{ destination.country }}</p>
        <p class="description">{{ destination.description }}</p>
        <div class="price">${{ destination.price }}</div>
        <button class="btn btn-primary">Book Now</button>
      </div>
    </div>
  </div>
</template>

<script>
import { travelService } from '../services/travel.js'
import { useNotification } from '../composables/useNotification.js'
import MapSelector from '../components/MapSelector.vue'

export default {
  name: 'Destinations',
  components: {
    MapSelector
  },
  setup() {
    const { success, error: notifyError } = useNotification()
    return { notifySuccess: success, notifyError }
  },
  data() {
    return {
      destinations: [],
      loading: true,
      error: null,
      showAddForm: false,
      newDestination: {
        name: '',
        country: '',
        description: '',
        price: 0,
        locationLink: '',
        latitude: null,
        longitude: null,
        address: ''
      },
      locationInputType: 'map', // 'map' or 'manual'
      selectedLocation: null,
      currentLocationData: null,
      showMapModal: false
    }
  },
  async mounted() {
    await this.loadDestinations()
  },
  methods: {
    async loadDestinations() {
      this.loading = true
      this.error = null
      
      try {
        this.destinations = await travelService.getDestinations()
      } catch (error) {
        this.error = 'Failed to load destinations'
        console.error(error)
      } finally {
        this.loading = false
      }
    },
    async addDestination() {
      try {
        // Validate location data
        if (!this.currentLocationData) {
          this.notifyError('Please select a location before adding the destination')
          return
        }
        
        // Prepare destination data with location
        const destinationData = {
          ...this.newDestination,
          location: {
            latitude: this.currentLocationData.latitude,
            longitude: this.currentLocationData.longitude,
            address: this.currentLocationData.address,
            link: this.currentLocationData.link
          }
        }
        
        await travelService.addDestination(destinationData)
        this.notifySuccess('Destination added successfully!')
        this.showAddForm = false
        this.resetForm()
        await this.loadDestinations()
      } catch (error) {
        this.notifyError('Failed to add destination')
        console.error(error)
      }
    },
    cancelAdd() {
      this.showAddForm = false
      this.resetForm()
    },
    resetForm() {
      this.newDestination = {
        name: '',
        country: '',
        description: '',
        price: 0,
        locationLink: '',
        latitude: null,
        longitude: null,
        address: ''
      }
      this.selectedLocation = null
      this.currentLocationData = null
      this.locationInputType = 'map'
    },
    onLocationSelected(locationData) {
      // Update selected location from map
      this.selectedLocation = {
        lat: locationData.latitude,
        lng: locationData.longitude,
        address: locationData.address
      }
      
      // Update form data
      this.updateLocationData(locationData)
      
      // Show success notification
      this.notifySuccess('Location selected successfully!')
    },
    clearLocation() {
      this.selectedLocation = null
      this.currentLocationData = null
      this.newDestination.latitude = null
      this.newDestination.longitude = null
      this.newDestination.address = ''
    },
    parseLocationLink() {
      const link = this.newDestination.locationLink.trim()
      if (!link) return
      
      try {
        // Parse coordinates from various map service URLs
        let lat, lng, address = ''
        
        // Google Maps patterns
        const googleMapsRegex = /@(-?\d+\.\d+),(-?\d+\.\d+)/
        const googleMapsMatch = link.match(googleMapsRegex)
        
        if (googleMapsMatch) {
          lat = parseFloat(googleMapsMatch[1])
          lng = parseFloat(googleMapsMatch[2])
        } else {
          // Try query parameter pattern
          const queryRegex = /[?&]q=(-?\d+\.\d+),(-?\d+\.\d+)/
          const queryMatch = link.match(queryRegex)
          
          if (queryMatch) {
            lat = parseFloat(queryMatch[1])
            lng = parseFloat(queryMatch[2])
          } else {
            // Try coordinate pattern in URL path
            const pathRegex = /(-?\d+\.\d+),(-?\d+\.\d+)/
            const pathMatch = link.match(pathRegex)
            
            if (pathMatch) {
              lat = parseFloat(pathMatch[1])
              lng = parseFloat(pathMatch[2])
            }
          }
        }
        
        if (lat && lng) {
          this.updateLocationData({
            latitude: lat,
            longitude: lng,
            address: address || `Location at ${lat.toFixed(4)}, ${lng.toFixed(4)}`,
            link: link
          })
          
          // Show success notification
          if (this.notifySuccess) {
            this.notifySuccess('Location parsed successfully!')
          }
        } else {
          throw new Error('Could not parse coordinates from the provided link')
        }
      } catch (error) {
        console.error('Failed to parse location link:', error)
        if (this.notifyError) {
          this.notifyError('Could not parse location from the provided link. Please check the URL format.')
        }
      }
    },
    updateLocationData(locationData) {
      this.currentLocationData = locationData
      this.newDestination.latitude = locationData.latitude
      this.newDestination.longitude = locationData.longitude
      this.newDestination.address = locationData.address
      
      if (locationData.link && !this.newDestination.locationLink) {
        this.newDestination.locationLink = locationData.link
      }
    }
  }
}
</script>

<style scoped>
.destinations-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.destinations-header h1 {
  color: #2c3e50;
}

.add-form {
  margin-bottom: 2rem;
}

.add-form h3 {
  margin-bottom: 1rem;
  color: #2c3e50;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.loading {
  text-align: center;
  padding: 2rem;
  color: #7f8c8d;
}

.destinations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
}

/* Location Section Styles */
.location-section {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem;
  margin: 1rem 0;
}

.location-section label {
  font-weight: 600;
  margin-bottom: 0.5rem;
  display: block;
}

.location-input-options {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.location-tabs {
  display: flex;
  background: #f8f9fa;
  border-radius: 6px;
  padding: 4px;
  gap: 4px;
}

.location-tab {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: #6b7280;
}

.location-tab.active {
  background: white;
  color: #4f46e5;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.location-tab:hover:not(.active) {
  background: #e5e7eb;
}

.map-container {
  margin-top: 1rem;
}

.map-placeholder {
  border: 2px dashed #d1d5db;
  border-radius: 8px;
  padding: 2rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.map-placeholder:hover {
  border-color: #4f46e5;
  background: #f8fafc;
}

.map-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  color: #6b7280;
}

.map-prompt svg {
  opacity: 0.5;
}

.selected-location-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  text-align: left;
}

.location-preview {
  display: flex;
  align-items: center;
  gap: 12px;
}

.location-preview svg {
  color: #10b981;
}

.coordinates {
  font-family: monospace;
  font-weight: 600;
  color: #374151;
  margin: 0;
  font-size: 14px;
}

.address {
  color: #6b7280;
  margin: 4px 0 0 0;
  font-size: 13px;
}

.clear-location-btn {
  background: #fee2e2;
  color: #dc2626;
  border: none;
  border-radius: 4px;
  padding: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.clear-location-btn:hover {
  background: #fecaca;
}

.manual-input-container {
  margin-top: 1rem;
}

.input-group {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.location-link-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.location-link-input:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.parse-link-btn {
  background: #4f46e5;
  color: white;
  border: none;
  border-radius: 6px;
  padding: 8px 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.parse-link-btn:hover:not(:disabled) {
  background: #4338ca;
}

.parse-link-btn:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}

.input-help {
  color: #6b7280;
  font-size: 12px;
  margin: 0;
}

.current-location-display {
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
}

.location-summary {
  font-size: 14px;
}

.location-summary strong {
  color: #1e40af;
  display: block;
  margin-bottom: 8px;
}

.location-summary p {
  margin: 4px 0;
  color: #374151;
}

.location-summary a {
  color: #4f46e5;
  text-decoration: none;
  word-break: break-all;
}

.location-summary a:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .location-tabs {
    flex-direction: column;
  }
  
  .location-tab {
    justify-content: center;
  }
  
  .input-group {
    flex-direction: column;
  }
  
  .parse-link-btn {
    justify-content: center;
  }
}
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.destination-card {
  background: white;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease;
}

.destination-card:hover {
  transform: translateY(-2px);
}

.destination-card h3 {
  color: #2c3e50;
  margin-bottom: 0.5rem;
  font-size: 1.3rem;
}

.country {
  color: #7f8c8d;
  font-weight: 500;
  margin-bottom: 1rem;
}

.description {
  color: #555;
  margin-bottom: 1rem;
  line-height: 1.4;
}

.price {
  font-size: 1.5rem;
  font-weight: bold;
  color: #27ae60;
  margin-bottom: 1rem;
}

.destination-card .btn {
  width: 100%;
}
</style>