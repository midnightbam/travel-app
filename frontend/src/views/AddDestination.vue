<template>
  <div class="add-destination-page">
    <div class="page-header">
      <div class="container">
        <div class="header-content">
          <button @click="goBack" class="back-btn">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
              <path d="M20 11H7.83l5.59-5.59L12 4l-8 8 8 8 1.41-1.41L7.83 13H20v-2z"/>
            </svg>
            Back to Dashboard
          </button>
          <h1>{{ isEditing ? 'Edit Destination' : 'Add New Destination' }}</h1>
        </div>
      </div>
    </div>

    <div class="page-content">
      <div class="container">
        <form @submit.prevent="submitDestination" class="destination-form">
          <div class="form-grid">
            <!-- Left Column - Basic Info -->
            <div class="form-column">
              <div class="form-card">
                <h3>Basic Information</h3>
                
                <div class="form-group">
                  <label>Title *</label>
                  <input 
                    v-model="formData.title" 
                    type="text" 
                    required 
                    placeholder="e.g., Beautiful Beach in Phuket"
                    class="form-input"
                  />
                </div>

                <div class="form-group">
                  <label>Province/Location *</label>
                  <input 
                    v-model="formData.province" 
                    type="text" 
                    required 
                    placeholder="e.g., Phuket"
                    class="form-input"
                  />
                </div>

                <div class="form-group">
                  <label>Description *</label>
                  <textarea 
                    v-model="formData.description" 
                    rows="5" 
                    required 
                    placeholder="Describe this amazing place..."
                    class="form-textarea"
                  ></textarea>
                </div>

                <div class="form-group">
                  <label>Tags</label>
                  <input 
                    v-model="tagsText" 
                    type="text" 
                    placeholder="e.g., beach, sunset, adventure (separate with commas)"
                    class="form-input"
                  />
                  <small class="form-help">Add tags separated by commas to help others find your destination</small>
                </div>
              </div>

              <!-- Photos Section -->
              <div class="form-card">
                <h3>Photos * (Minimum 2 required)</h3>
                <div class="photo-uploader">
                  <div v-if="photoList.length < 8" class="photo-drop-zone" @click="triggerFileInput" @dragover.prevent @drop.prevent="handleDrop">
                    <input ref="fileInput" type="file" multiple accept="image/*" @change="handleFileSelect" style="display: none" />
                    <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                    <p>Drag & drop images or click to browse</p>
                    <small>Supports: JPG, PNG, GIF (max 8 photos)</small>
                  </div>
                  
                  <div v-if="validPhotoList.length > 0" class="photo-preview-grid">
                    <div 
                      v-for="(photo, index) in validPhotoList" 
                      :key="photo.url || index"
                      class="photo-preview-item"
                        :class="{ 'main-photo': index === mainPhotoIndex }"
                        @dragstart="handleDragStart(index)"
                        @dragover.prevent
                        @drop.prevent="handleDragEnd(index)"
                        draggable="true"
                      >
                        <img :src="photo.url" :alt="`Photo ${index + 1}`" @error="handleImageError" />
                      <div class="photo-controls">
                        <button type="button" @click="setMainPhoto(index)" class="main-photo-btn" :class="{ active: index === mainPhotoIndex }">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"/>
                          </svg>
                        </button>
                        <button type="button" @click="removePhoto(index)" class="remove-photo-btn">
                          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                            <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
                          </svg>
                        </button>
                      </div>
                      <div v-if="index === mainPhotoIndex" class="main-photo-badge">Main Photo</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Right Column - Location -->
            <div class="form-column">
              <div class="form-card location-card">
                <h3>Location Selection</h3>
                
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
                      Interactive Map
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
                  
                  <!-- Interactive Map -->
                  <div v-if="locationInputType === 'map'" class="map-section">
                    <EmbeddedMapSelector 
                      @location-selected="onLocationSelected"
                      :initial-location="selectedLocationData ? {
                        lat: selectedLocationData.latitude,
                        lng: selectedLocationData.longitude,
                        address: selectedLocationData.address
                      } : null"
                    />
                  </div>
                  
                  <!-- Manual Link Input -->
                  <div v-if="locationInputType === 'manual'" class="manual-input-section">
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
                </div>
              </div>
            </div>
          </div>

          <!-- Form Actions -->
          <div class="form-actions">
            <button type="button" @click="goBack" class="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" class="btn btn-primary" :disabled="submitting || !isFormValid">
              <svg v-if="submitting" width="16" height="16" viewBox="0 0 24 24" fill="currentColor" class="spinning">
                <path d="M12 4V2A10 10 0 0 0 2 12h2a8 8 0 0 1 8-8z"/>
              </svg>
              {{ submitting ? 'Saving...' : (isEditing ? 'Update Destination' : 'Create Destination') }}
            </button>
          </div>
        </form>
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
  name: 'AddDestination',
  components: {
    EmbeddedMapSelector
  },
  setup() {
    const { showNotification } = useNotification()
    return { showNotification }
  },
  data() {
    return {
      isEditing: false,
      tripId: null,
      submitting: false,
      locationInputType: 'map',
      selectedLocationData: null,
      formData: {
        title: '',
        location: '',
        province: '',
        description: '',
        locationLink: '',
        latitude: null,
        longitude: null,
        isManualLink: false
      },
      photoList: [],
      mainPhotoIndex: 0,
      draggedIndex: null,
      tagsText: ''
    }
  },
  computed: {
    isFormValid() {
      return this.formData.title &&
             this.formData.province &&
             this.formData.description &&
             this.photoList.length >= 2
    },
    validPhotoList() {
      const valid = this.photoList.filter(photo => 
        photo && 
        photo.url && 
        typeof photo.url === 'string' && 
        photo.url.trim().length > 0
      )
      console.log('validPhotoList:', valid.length, 'photos out of', this.photoList.length)
      return valid
    }
  },
  async created() {
    // Check if editing existing destination
    if (this.$route.params.id) {
      this.isEditing = true
      this.tripId = parseInt(this.$route.params.id)
      await this.loadDestination()
    }
  },
  methods: {
    goBack() {
      this.$router.push('/dashboard')
    },

    async loadDestination() {
      try {
        const tripData = await travelService.getTrip(this.tripId)
        console.log('Raw API Response:', tripData) // Debug logging
        
        // Handle case where trip data might be null or undefined
        if (!tripData) {
          throw new Error('No trip data received from server')
        }
        
        // Check for required fields
        if (!tripData.title && !tripData.id) {
          throw new Error('Invalid trip data structure - missing required fields')
        }
        
        this.formData = {
          title: tripData.title || '',
          location: tripData.location || '',
          province: tripData.province || '',
          description: tripData.description || '',
          locationLink: tripData.locationLink || '',
          latitude: tripData.latitude || null,
          longitude: tripData.longitude || null,
          isManualLink: Boolean(tripData.locationLink)
        }
        
        // Handle tags - convert array to comma-separated string
        if (tripData.tags && Array.isArray(tripData.tags)) {
          this.tagsText = tripData.tags.join(', ')
        } else if (tripData.tags && typeof tripData.tags === 'string') {
          this.tagsText = tripData.tags
        } else {
          this.tagsText = ''
        }
        
        console.log('Debug - formData location:', this.formData.location)
        console.log('Debug - tagsText:', this.tagsText)
        
        // Handle photos - convert from backend format if needed
        this.photoList = []
        if (tripData.photos) {
          let photoUrls = []
          
          // Handle if photos is a string (comma-separated URLs)
          if (typeof tripData.photos === 'string') {
            photoUrls = tripData.photos.split(',')
              .map(url => url.trim())
              .filter(url => url.length > 0 && url !== '' && url !== 'null' && url !== 'undefined')
          } 
          // Handle if photos is already an array
          else if (Array.isArray(tripData.photos)) {
            photoUrls = tripData.photos.filter(url => url && typeof url === 'string' && url.trim().length > 0)
          }
          
          // Create photo objects only for valid URLs
          this.photoList = photoUrls.map((url, index) => ({
            url: url,
            name: `Photo ${index + 1}`,
            file: null // Existing photos don't have file objects
          }))
          
          console.log('Parsed photo URLs:', photoUrls)
        }
        
        console.log('Loaded photoList:', this.photoList)
        console.log('PhotoList length:', this.photoList.length)
        
        this.mainPhotoIndex = 0 // Default to first photo
        
        // Set location data if coordinates exist
        if (tripData.latitude && tripData.longitude) {
          this.selectedLocationData = {
            latitude: tripData.latitude,
            longitude: tripData.longitude,
            address: tripData.location
          }
        }
        
        console.log('Loaded form data:', this.formData) // Debug logging
        console.log('Loaded province:', this.formData.province) // Debug province specifically
        // Removed automatic success notification to prevent alert on edit load
      } catch (error) {
        console.error('Error loading destination:', error)
        this.showNotification('Error loading destination: ' + (error.message || 'Unknown error'), 'error')
        // Don't automatically go back - let user see the error
      }
    },

    onLocationSelected(location) {
      this.selectedLocationData = {
        latitude: location.lat,
        longitude: location.lng,
        address: location.address
      }
      
      this.formData.latitude = location.lat
      this.formData.longitude = location.lng
      this.formData.location = location.address || `${location.lat.toFixed(6)}, ${location.lng.toFixed(6)}`
      this.formData.isManualLink = false
      
      this.showNotification('Location selected successfully!', 'success')
    },

    async parseLocationLink() {
      // Same parseLocationLink method from Dashboard
      if (!this.formData.locationLink) {
        this.showNotification('Please enter a location link', 'warning')
        return
      }

      try {
        const link = this.formData.locationLink.trim()
        let lat, lng

        // Parse Google Maps links
        if (link.includes('google.com/maps') || link.includes('maps.google.com')) {
          const patterns = [
            /@(-?\d+\.?\d*),(-?\d+\.?\d*)/,
            /!3d(-?\d+\.?\d*)!4d(-?\d+\.?\d*)/,
            /q=(-?\d+\.?\d*),(-?\d+\.?\d*)/,
            /ll=(-?\d+\.?\d*),(-?\d+\.?\d*)/
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
        else if (link.includes('maps.apple.com')) {
          const match = link.match(/ll=(-?\d+\.?\d*),(-?\d+\.?\d*)/);
          if (match) {
            lat = parseFloat(match[1])
            lng = parseFloat(match[2])
          }
        }
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
          
          this.selectedLocationData = {
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
      const remainingSlots = 8 - this.photoList.length

      imageFiles.slice(0, remainingSlots).forEach(file => {
        const reader = new FileReader()
        reader.onload = (e) => {
          this.photoList.push({
            file: file,
            url: e.target.result,
            name: file.name
          })
        }
        reader.readAsDataURL(file)
      })
    },

    removePhoto(index) {
      this.photoList.splice(index, 1)
      if (this.mainPhotoIndex >= index && this.mainPhotoIndex > 0) {
        this.mainPhotoIndex--
      }
      if (this.photoList.length === 0) {
        this.mainPhotoIndex = 0
      }
    },

    setMainPhoto(index) {
      this.mainPhotoIndex = index
    },

    handleDragStart(index) {
      this.draggedIndex = index
    },

    handleDragEnd(dropIndex) {
      if (this.draggedIndex !== null && this.draggedIndex !== dropIndex) {
        const draggedPhoto = this.photoList.splice(this.draggedIndex, 1)[0]
        this.photoList.splice(dropIndex, 0, draggedPhoto)
        
        if (this.mainPhotoIndex === this.draggedIndex) {
          this.mainPhotoIndex = dropIndex
        } else if (this.mainPhotoIndex > this.draggedIndex && this.mainPhotoIndex <= dropIndex) {
          this.mainPhotoIndex--
        } else if (this.mainPhotoIndex < this.draggedIndex && this.mainPhotoIndex >= dropIndex) {
          this.mainPhotoIndex++
        }
      }
      this.draggedIndex = null
    },

    handleImageError(event) {
      event.target.src = '/placeholder-image.jpg'
    },

    async submitDestination() {
      if (!this.isFormValid) return

      this.submitting = true
      
      try {
        const formData = new FormData()
        
        // Add basic fields
        formData.append('title', this.formData.title)
        formData.append('province', this.formData.province)
        formData.append('description', this.formData.description)
        
        // Add optional location data
        if (this.formData.location) {
          formData.append('location', this.formData.location)
        }
        if (this.formData.latitude != null) {
          formData.append('latitude', this.formData.latitude.toString())
        }
        if (this.formData.longitude != null) {
          formData.append('longitude', this.formData.longitude.toString())
        }
        if (this.formData.locationLink) {
          formData.append('locationLink', this.formData.locationLink)
        }
        
        // Add tags
        if (this.tagsText) {
          const tags = this.tagsText.split(',').map(tag => tag.trim()).filter(tag => tag).join(',')
          formData.append('tags', tags)
        }
        
        // Handle photos
        const existingPhotos = []
        const newPhotoFiles = []
        
        this.photoList.forEach(photo => {
          if (photo.file) {
            // New file to upload
            newPhotoFiles.push(photo.file)
          } else if (photo.url && !photo.url.startsWith('data:')) {
            // Existing URL (not base64)
            existingPhotos.push(photo.url)
          }
        })
        
        // Add existing photos as comma-separated string
        if (existingPhotos.length > 0) {
          formData.append('existingPhotos', existingPhotos.join(','))
        }
        
        // Add new photo files
        newPhotoFiles.forEach(file => {
          formData.append('photos', file)
        })
        
        console.log('Submitting FormData with:')
        console.log('- Province:', this.formData.province)
        console.log('- Latitude:', this.formData.latitude)
        console.log('- Longitude:', this.formData.longitude)
        console.log('- Existing photos:', existingPhotos.length)
        console.log('- New photo files:', newPhotoFiles.length)

        let response
        if (this.isEditing) {
          response = await travelService.updateTripWithFiles(this.tripId, formData)
          console.log('Update response:', response)
          this.showNotification('Destination updated successfully!', 'success')
        } else {
          response = await travelService.createTripWithFiles(formData)
          console.log('Create response:', response)
          this.showNotification('Destination created successfully!', 'success')
        }
        
        // Slight delay to show notification then navigate back
        setTimeout(() => {
          this.$router.push('/dashboard')
        }, 1000)
      } catch (error) {
        console.error('Error saving destination:', error)
        this.showNotification('Error saving destination. Please try again.', 'error')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.add-destination-page {
  min-height: 100vh;
  background: #f8fafc;
}

.page-header {
  background: white;
  border-bottom: 1px solid #e2e8f0;
  padding: 20px 0;
  margin-bottom: 32px;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: none;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s ease;
}

.back-btn:hover {
  background: #f8fafc;
  border-color: #10b981;
  color: #10b981;
}

.page-header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.page-content {
  padding-bottom: 40px;
}

.destination-form {
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  position: relative;
  z-index: 1;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0;
}

.form-column {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-column:first-child {
  border-right: 1px solid #e2e8f0;
}

.form-card {
  background: #f8fafc;
  border-radius: 8px;
  padding: 24px;
  border: 1px solid #e2e8f0;
}

.form-card h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 8px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #374151;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s ease;
  background: white;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.1);
}

.form-help {
  font-size: 12px;
  color: #6b7280;
  margin-top: 4px;
  display: block;
}

.form-textarea {
  resize: vertical;
  min-height: 120px;
}

.location-card {
  background: #f0fdf4;
  border-color: #10b981;
  min-height: 700px;
  display: flex;
  flex-direction: column;
}

.location-input-options {
  background: white;
  border-radius: 8px;
  padding: 20px;
  border: 1px solid #10b981;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.location-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
}

.location-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  color: #64748b;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
  flex: 1;
  justify-content: center;
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

.map-section {
  flex: 1;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  min-height: 550px;
  position: relative;
  z-index: 1;
}

.map-section :deep(.embedded-map-selector) {
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.map-section :deep(.search-container) {
  flex-shrink: 0;
  position: relative;
  z-index: 5;
}

.map-section :deep(.map-container) {
  flex: 0 0 280px;
  border: none;
  border-radius: 0;
  min-height: 280px;
  position: relative;
  z-index: 1;
}

.map-section :deep(.map-display) {
  height: 100% !important;
  min-height: 280px !important;
  position: relative;
  z-index: 1;
}

.map-section :deep(.leaflet-container) {
  z-index: 1 !important;
}

.map-section :deep(.leaflet-pane) {
  z-index: 1 !important;
}

.map-section :deep(.leaflet-control) {
  z-index: 2 !important;
}

.map-section :deep(.selected-location-info) {
  margin-top: 16px;
  flex: 1;
  min-height: 150px;
  overflow-y: auto;
}

.map-section :deep(.map-actions) {
  margin-top: 16px;
  padding-top: 16px;
  flex-shrink: 0;
}

.manual-input-section .input-group {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.location-link-input {
  flex: 1;
  padding: 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
}

.parse-link-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 12px 16px;
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.parse-link-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #059669, #047857);
}

.parse-link-btn:disabled {
  background: #9ca3af;
  cursor: not-allowed;
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

.location-summary strong {
  color: #064e3b;
}

.location-summary p {
  margin: 8px 0;
  font-size: 14px;
  color: #065f46;
}

.location-summary a {
  color: #10b981;
  text-decoration: none;
}

.location-summary a:hover {
  text-decoration: underline;
}

/* Photo Uploader Styles */
.photo-uploader {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.photo-drop-zone {
  border: 2px dashed #cbd5e0;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.photo-drop-zone:hover {
  border-color: #10b981;
  background: #f0fdf4;
}

.photo-drop-zone svg {
  width: 48px;
  height: 48px;
  color: #9ca3af;
  margin-bottom: 16px;
}

.photo-drop-zone p {
  margin: 0 0 8px 0;
  font-weight: 500;
  color: #374151;
}

.photo-drop-zone small {
  color: #6b7280;
}

.photo-preview-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.photo-preview-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  background: white;
  border: 2px solid #e2e8f0;
  transition: all 0.2s ease;
  cursor: grab;
  width: 150px;
  flex-shrink: 0;
}

.photo-preview-item:hover {
  border-color: #10b981;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.15);
}

.photo-preview-item.main-photo {
  border-color: #f59e0b;
  box-shadow: 0 0 0 2px rgba(245, 158, 11, 0.2);
}

.photo-preview-item img {
  width: 150px;
  height: 150px;
  object-fit: cover;
  display: block;
}

.photo-controls {
  position: absolute;
  top: 4px;
  right: 4px;
  display: flex;
  gap: 4px;
}

.main-photo-btn, .remove-photo-btn {
  width: 24px;
  height: 24px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.2s ease;
}

.main-photo-btn {
  background: rgba(0, 0, 0, 0.5);
  color: white;
}

.main-photo-btn.active {
  background: #f59e0b;
}

.remove-photo-btn {
  background: rgba(239, 68, 68, 0.8);
  color: white;
}

.remove-photo-btn:hover {
  background: #ef4444;
}

.main-photo-badge {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  padding: 2px 6px;
  font-size: 10px;
  font-weight: 600;
  text-align: center;
}

.form-actions {
  padding: 24px 32px;
  border-top: 1px solid #e2e8f0;
  background: #f8fafc;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn {
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-secondary {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
}

.btn-secondary:hover {
  background: #e5e7eb;
  border-color: #9ca3af;
}

.btn-primary {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #059669, #047857);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.btn-primary:disabled {
  background: #9ca3af;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Manual Input Section */
.manual-input-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

/* Responsive Design */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
  
  .form-column:first-child {
    border-right: none;
    border-bottom: 1px solid #e2e8f0;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .location-tabs {
    flex-direction: column;
  }
  
  .form-actions {
    flex-direction: column-reverse;
  }
  
  .btn {
    width: 100%;
    justify-content: center;
  }
  
  .location-card {
    min-height: 600px;
  }
  
  .map-section {
    min-height: 500px;
  }
}
</style>