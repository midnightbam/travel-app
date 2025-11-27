<template>
  <div class="map-selector-modal" v-if="showModal" @click.self="closeModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>Select Location on Map</h3>
        <button @click="closeModal" class="close-btn">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
          </svg>
        </button>
      </div>
      
      <div class="modal-body">
        <div class="map-instructions">
          <p>Click anywhere on the map to select a location. You can also search for a place using the search box below.</p>
        </div>
        
        <div class="search-container">
          <input 
            v-model="searchQuery"
            @keyup.enter="searchLocation"
            type="text" 
            placeholder="Search for a place (e.g., Paris, Eiffel Tower, New York)"
            class="location-search-input"
          >
          <button @click="searchLocation" class="search-btn" :disabled="!searchQuery">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/>
            </svg>
          </button>
        </div>
        
        <div class="map-container">
          <div ref="mapElement" class="map-display"></div>
        </div>
        
        <div v-if="selectedLocation" class="selected-location-info">
          <h4>Selected Location:</h4>
          <p><strong>Latitude:</strong> {{ selectedLocation.lat.toFixed(6) }}</p>
          <p><strong>Longitude:</strong> {{ selectedLocation.lng.toFixed(6) }}</p>
          <p v-if="selectedLocation.address"><strong>Address:</strong> {{ selectedLocation.address }}</p>
        </div>
      </div>
      
      <div class="modal-footer">
        <button @click="getCurrentLocation" class="btn btn-secondary">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 8c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm8.94 3A8.994 8.994 0 0013 3.06V1h-2v2.06A8.994 8.994 0 003.06 11H1v2h2.06A8.994 8.994 0 0011 20.94V23h2v-2.06A8.994 8.994 0 0020.94 13H23v-2h-2.06zM12 19c-3.87 0-7-3.13-7-7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7z"/>
          </svg>
          Use My Location
        </button>
        <button @click="closeModal" class="btn btn-secondary">Cancel</button>
        <button @click="confirmSelection" class="btn btn-primary" :disabled="!selectedLocation">
          Confirm Selection
        </button>
      </div>
    </div>
  </div>
</template>

<script>
import L from 'leaflet'

// Import Leaflet CSS
import 'leaflet/dist/leaflet.css'

// Fix for default markers in webpack
import icon from 'leaflet/dist/images/marker-icon.png'
import iconShadow from 'leaflet/dist/images/marker-shadow.png'
import iconRetina from 'leaflet/dist/images/marker-icon-2x.png'

delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconUrl: icon,
  iconRetinaUrl: iconRetina,
  shadowUrl: iconShadow,
})

export default {
  name: 'MapSelector',
  props: {
    showModal: {
      type: Boolean,
      default: false
    },
    initialLocation: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      map: null,
      marker: null,
      selectedLocation: null,
      searchQuery: ''
    }
  },
  watch: {
    showModal(newValue) {
      if (newValue) {
        this.$nextTick(() => {
          this.initializeMap()
        })
      } else {
        this.destroyMap()
      }
    }
  },
  methods: {
    initializeMap() {
      if (this.map) {
        this.destroyMap()
      }

      // Set initial coordinates (default to center of world)
      const initialLat = this.initialLocation?.lat || 20
      const initialLng = this.initialLocation?.lng || 0
      const initialZoom = this.initialLocation ? 15 : 2

      // Create map
      this.map = L.map(this.$refs.mapElement).setView([initialLat, initialLng], initialZoom)

      // Add tile layer (OpenStreetMap)
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap contributors',
        maxZoom: 19
      }).addTo(this.map)

      // Add click event listener
      this.map.on('click', (e) => {
        this.selectLocation(e.latlng.lat, e.latlng.lng)
      })

      // Set initial selection if provided
      if (this.initialLocation) {
        this.selectLocation(this.initialLocation.lat, this.initialLocation.lng, this.initialLocation.address)
      }
    },

    selectLocation(lat, lng, address = '') {
      // Remove existing marker
      if (this.marker) {
        this.map.removeLayer(this.marker)
      }

      // Add new marker
      this.marker = L.marker([lat, lng]).addTo(this.map)

      // Update selected location
      this.selectedLocation = {
        lat: lat,
        lng: lng,
        address: address
      }

      // Try to get address if not provided
      if (!address) {
        this.reverseGeocode(lat, lng)
      }
    },

    async reverseGeocode(lat, lng) {
      try {
        // Using Nominatim (free reverse geocoding)
        const response = await fetch(
          `https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&addressdetails=1`
        )
        const data = await response.json()
        
        if (data && data.display_name) {
          this.selectedLocation.address = data.display_name
        }
      } catch (error) {
        console.error('Reverse geocoding failed:', error)
        this.selectedLocation.address = `Location at ${lat.toFixed(4)}, ${lng.toFixed(4)}`
      }
    },

    async searchLocation() {
      if (!this.searchQuery.trim()) return

      try {
        // Using Nominatim for free geocoding
        const response = await fetch(
          `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(this.searchQuery)}&limit=1`
        )
        const data = await response.json()

        if (data && data.length > 0) {
          const result = data[0]
          const lat = parseFloat(result.lat)
          const lng = parseFloat(result.lon)
          
          // Move map to location
          this.map.setView([lat, lng], 15)
          
          // Select the location
          this.selectLocation(lat, lng, result.display_name)
        } else {
          alert('Location not found. Please try a different search term.')
        }
      } catch (error) {
        console.error('Geocoding failed:', error)
        alert('Failed to search for location. Please try again.')
      }
    },

    getCurrentLocation() {
      if (!navigator.geolocation) {
        alert('Geolocation is not supported by this browser.')
        return
      }

      navigator.geolocation.getCurrentPosition(
        (position) => {
          const lat = position.coords.latitude
          const lng = position.coords.longitude
          
          // Move map to current location
          this.map.setView([lat, lng], 15)
          
          // Select current location
          this.selectLocation(lat, lng)
        },
        (error) => {
          console.error('Geolocation failed:', error)
          alert('Failed to get your current location. Please select manually.')
        }
      )
    },

    confirmSelection() {
      if (!this.selectedLocation) return

      this.$emit('location-selected', {
        latitude: this.selectedLocation.lat,
        longitude: this.selectedLocation.lng,
        address: this.selectedLocation.address,
        link: `https://www.openstreetmap.org/?mlat=${this.selectedLocation.lat}&mlon=${this.selectedLocation.lng}&zoom=15`
      })

      this.closeModal()
    },

    closeModal() {
      this.$emit('close')
    },

    destroyMap() {
      if (this.map) {
        this.map.remove()
        this.map = null
        this.marker = null
      }
    }
  },

  beforeUnmount() {
    this.destroyMap()
  }
}
</script>

<style scoped>
.map-selector-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 800px;
  max-height: 90vh;
  overflow: auto;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h3 {
  margin: 0;
  color: #1f2937;
  font-size: 1.25rem;
  font-weight: 600;
}

.close-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #6b7280;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 1.5rem;
}

.map-instructions {
  margin-bottom: 1rem;
  padding: 1rem;
  background: #f0f9ff;
  border: 1px solid #bae6fd;
  border-radius: 8px;
}

.map-instructions p {
  margin: 0;
  color: #075985;
  font-size: 14px;
}

.search-container {
  display: flex;
  gap: 8px;
  margin-bottom: 1rem;
}

.location-search-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
}

.location-search-input:focus {
  outline: none;
  border-color: #4f46e5;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.search-btn {
  background: #4f46e5;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 10px 16px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
}

.search-btn:hover:not(:disabled) {
  background: #4338ca;
}

.search-btn:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}

.map-container {
  margin: 1rem 0;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e5e7eb;
}

.map-display {
  height: 400px;
  width: 100%;
}

.selected-location-info {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 1rem;
  margin-top: 1rem;
}

.selected-location-info h4 {
  margin: 0 0 8px 0;
  color: #1f2937;
  font-size: 16px;
}

.selected-location-info p {
  margin: 4px 0;
  color: #4b5563;
  font-size: 14px;
}

.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid #e5e7eb;
  background: #f9fafb;
  border-radius: 0 0 12px 12px;
}

.btn {
  padding: 10px 16px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-primary {
  background: #4f46e5;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #4338ca;
}

.btn-primary:disabled {
  background: #d1d5db;
  cursor: not-allowed;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background: #e5e7eb;
}

@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    margin: 1rem;
  }
  
  .modal-header,
  .modal-body,
  .modal-footer {
    padding: 1rem;
  }
  
  .map-display {
    height: 300px;
  }
  
  .search-container {
    flex-direction: column;
  }
  
  .modal-footer {
    flex-direction: column;
  }
}
</style>