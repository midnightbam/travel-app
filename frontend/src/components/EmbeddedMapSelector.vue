<template>
  <div class="embedded-map-selector">
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
      <button @click="getCurrentLocation" class="location-btn">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <path d="M12 8c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm8.94 3A8.994 8.994 0 0013 3.06V1h-2v2.06A8.994 8.994 0 003.06 11H1v2h2.06A8.994 8.994 0 0011 20.94V23h2v-2.06A8.994 8.994 0 0020.94 13H23v-2h-2.06zM12 19c-3.87 0-7-3.13-7 7s3.13-7 7-7 7 3.13 7 7-3.13 7-7 7z"/>
        </svg>
      </button>
    </div>
    
    <div class="map-container">
      <div ref="mapElement" class="map-display"></div>
    </div>
    
    <!-- Debug info -->
    <div style="font-size: 12px; color: #666; margin-top: 8px;">
      Debug: selectedLocation = {{ selectedLocation ? 'SET' : 'NULL' }}
    </div>
    
    <div v-if="selectedLocation" class="selected-location-info">
      <div class="location-header">
        <h4>📍 Selected Location</h4>
      </div>
      <div class="location-details">
        <div class="coordinate-row">
          <span class="label">Latitude:</span>
          <span class="value">{{ selectedLocation.lat.toFixed(6) }}</span>
        </div>
        <div class="coordinate-row">
          <span class="label">Longitude:</span> 
          <span class="value">{{ selectedLocation.lng.toFixed(6) }}</span>
        </div>
        <div v-if="selectedLocation.address" class="coordinate-row">
          <span class="label">Address:</span>
          <span class="value address-text">{{ selectedLocation.address }}</span>
        </div>
      </div>
    </div>
    
    <div class="map-actions">
      <button @click="confirmSelection" class="btn btn-primary" :disabled="!selectedLocation">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
          <path d="M9 16.2L4.8 12l-1.4 1.4L9 19 21 7l-1.4-1.4L9 16.2z"/>
        </svg>
        Confirm Selection
      </button>
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
  name: 'EmbeddedMapSelector',
  props: {
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
  mounted() {
    this.initializeMap()
  },
  watch: {
    initialLocation: {
      handler(newLocation) {
        if (newLocation && this.map) {
          this.setLocation(newLocation.lat, newLocation.lng, newLocation.address)
        }
      },
      immediate: true
    },
    selectedLocation: {
      handler(newVal) {
        console.log('selectedLocation watcher triggered:', newVal);
      },
      deep: true
    }
  },
  beforeUnmount() {
    if (this.map) {
      this.map.remove()
    }
  },
  methods: {
    initializeMap() {
      this.$nextTick(() => {
        if (this.$refs.mapElement) {
          // Initialize map
          this.map = L.map(this.$refs.mapElement).setView([13.7563, 100.5018], 6) // Thailand center
          
          // Add tile layer
          L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            attribution: '© OpenStreetMap contributors'
          }).addTo(this.map)
          
          // Add click event listener
          this.map.on('click', this.onMapClick)
          
          // Force map to resize after multiple delays to ensure container is properly sized
          setTimeout(() => {
            this.map.invalidateSize()
            // Set initial location after map is fully loaded
            if (this.initialLocation) {
              this.setLocation(this.initialLocation.lat, this.initialLocation.lng, this.initialLocation.address)
            }
          }, 100)
          
          setTimeout(() => {
            this.map.invalidateSize()
          }, 300)
          
          setTimeout(() => {
            this.map.invalidateSize()
          }, 500)
        }
      })
    },
    
    onMapClick(e) {
      const { lat, lng } = e.latlng
      console.log('Map clicked at:', lat, lng)
      this.setLocation(lat, lng)
    },
    
    setLocation(lat, lng, address = null) {
      console.log('Setting location:', lat, lng, address)
      
      // Remove existing marker
      if (this.marker) {
        this.map.removeLayer(this.marker)
      }
      
      // Add new marker
      this.marker = L.marker([lat, lng]).addTo(this.map)
      
      // Update selected location
      this.selectedLocation = { lat, lng, address }
      console.log('Selected location set:', this.selectedLocation)
      
      // Center map on selected location
      this.map.setView([lat, lng], Math.max(this.map.getZoom(), 12))
      
      // Try to get address if not provided
      if (!address) {
        this.reverseGeocode(lat, lng)
      }
    },
    
    async reverseGeocode(lat, lng) {
      try {
        const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}&addressdetails=1`)
        const data = await response.json()
        
        if (data && data.display_name) {
          this.selectedLocation.address = data.display_name
        }
      } catch (error) {
        console.error('Error reverse geocoding:', error)
      }
    },
    
    async searchLocation() {
      if (!this.searchQuery.trim()) return
      
      try {
        const response = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(this.searchQuery)}`)
        const data = await response.json()
        
        if (data && data.length > 0) {
          const location = data[0]
          const lat = parseFloat(location.lat)
          const lng = parseFloat(location.lon)
          const address = location.display_name
          
          this.setLocation(lat, lng, address)
          this.searchQuery = ''
        } else {
          alert('Location not found. Please try a different search term.')
        }
      } catch (error) {
        console.error('Error searching location:', error)
        alert('Error searching for location. Please try again.')
      }
    },
    
    getCurrentLocation() {
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const lat = position.coords.latitude
            const lng = position.coords.longitude
            this.setLocation(lat, lng, 'Your current location')
          },
          (error) => {
            console.error('Error getting current location:', error)
            alert('Unable to get your current location. Please select a location on the map or search for a place.')
          },
          {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 600000
          }
        )
      } else {
        alert('Geolocation is not supported by this browser.')
      }
    },
    
    confirmSelection() {
      if (this.selectedLocation) {
        this.$emit('location-selected', this.selectedLocation)
      }
    }
  }
}
</script>

<style scoped>
.embedded-map-selector {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.search-container {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.location-search-input {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  transition: border-color 0.2s ease;
}

.location-search-input:focus {
  outline: none;
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.search-btn, .location-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 12px;
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 44px;
}

.search-btn:hover:not(:disabled), 
.location-btn:hover {
  background: linear-gradient(135deg, #059669, #047857);
  transform: translateY(-1px);
}

.search-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  transform: none;
}

.map-container {
  flex: 1;
  min-height: 300px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
}

.map-display {
  width: 100%;
  height: 100%;
  min-height: 300px;
}

.selected-location-info {
  margin-top: 16px;
  padding: 20px;
  background: #f0fdf4;
  border: 1px solid #10b981;
  border-radius: 8px;
  min-height: 150px;
  width: 100%;
  box-sizing: border-box;
  overflow-y: auto;
  flex: 1;
}

.location-header h4 {
  margin: 0 0 16px 0;
  color: #064e3b;
  font-size: 15px;
  font-weight: 600;
  border-bottom: 1px solid rgba(16, 185, 129, 0.2);
  padding-bottom: 6px;
}

.location-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.coordinate-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 10px 0;
}

.coordinate-row:last-child {
  padding-bottom: 0;
}

.coordinate-row .label {
  font-weight: 600;
  color: #065f46;
  font-size: 13px;
  min-width: 85px;
  flex-shrink: 0;
}

.coordinate-row .value {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  color: #064e3b;
  font-size: 12px;
  background: rgba(16, 185, 129, 0.1);
  padding: 6px 10px;
  border-radius: 4px;
  word-break: break-all;
  text-align: left;
  flex: 1;
  margin-left: 10px;
  line-height: 1.5;
  max-width: 100%;
}

.coordinate-row .value.address-text {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', sans-serif;
  word-break: break-word;
  white-space: normal;
}

.map-actions {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  border-top: 1px solid #e2e8f0;
  padding-top: 14px;
  flex-shrink: 0;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-primary {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #059669, #047857);
  transform: translateY(-1px);
}

.btn-primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  transform: none;
}
</style>