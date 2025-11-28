<template>
  <div class="profile-container">
    <div class="profile-header">
      <h1>My Profile</h1>
    </div>

    <div v-if="loading" class="loading">
      <p>Loading profile...</p>
    </div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="loadProfile" class="retry-btn">Try Again</button>
    </div>

    <div v-else class="profile-content">
      <!-- Profile Picture Section -->
      <div class="profile-picture-section">
        <div class="profile-picture">
          <div class="profile-image-container">
            <img 
              :src="user?.profileImage || getDefaultAvatar()" 
              alt="Profile Picture"
              class="profile-img"
              @error="handleImageError"
            >
          </div>
        </div>
        <input 
          type="file" 
          ref="fileInput" 
          @change="handleImageUpload" 
          accept="image/*" 
          style="display: none;"
        >
        <button @click="$refs.fileInput.click()" class="change-picture-btn">
          Change Picture
        </button>
      </div>

      <!-- Profile Info Section -->
      <div class="profile-info">
        <div class="info-grid">
          <div class="info-item">
            <label>Full Name</label>
            <div class="editable-field">
              <input 
                v-if="isEditing" 
                v-model="editForm.name" 
                type="text" 
                class="edit-input"
              >
              <p v-else>{{ user?.name || user?.displayName || 'Not set' }}</p>
            </div>
          </div>
          
          <div class="info-item">
            <label>Email</label>
            <div class="editable-field">
              <input 
                v-if="isEditing" 
                v-model="editForm.email" 
                type="email" 
                class="edit-input"
              >
              <p v-else>{{ user?.email || 'Not set' }}</p>
            </div>
          </div>
          
          <div class="info-item">
            <label>Member Since</label>
            <p>{{ formatDate(user?.createdAt) }}</p>
          </div>
        </div>

        <div class="profile-actions">
          <button 
            v-if="!isEditing" 
            @click="startEditing" 
            class="edit-btn"
          >
            Edit Profile
          </button>
          <div v-else class="edit-actions">
            <button @click="saveProfile" class="save-btn">Save Changes</button>
            <button @click="cancelEditing" class="cancel-btn">Cancel</button>
          </div>
          
          <button @click="showPasswordModal = true" class="password-btn">
            Change Password
          </button>
        </div>
      </div>
    </div>

    <!-- Change Password Modal -->
    <div v-if="showPasswordModal" class="modal-overlay" @click="closePasswordModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>Change Password</h2>
          <button @click="closePasswordModal" class="close-btn">&times;</button>
        </div>
        
        <form @submit.prevent="changePassword" class="password-form">
          <div class="form-group">
            <label>Current Password</label>
            <input 
              v-model="passwordForm.currentPassword" 
              type="password" 
              required
              class="form-input"
            >
          </div>
          
          <div class="form-group">
            <label>New Password</label>
            <input 
              v-model="passwordForm.newPassword" 
              type="password" 
              required
              class="form-input"
            >
            <div v-if="passwordForm.newPassword" class="password-requirements">
              <p :class="{ valid: passwordValidation.minLength }">✓ At least 8 characters</p>
              <p :class="{ valid: passwordValidation.hasLetter }">✓ Contains letters</p>
              <p :class="{ valid: passwordValidation.hasNumber }">✓ Contains numbers</p>
              <p :class="{ valid: passwordValidation.hasSpecial }">✓ Contains special characters</p>
            </div>
          </div>
          
          <div class="form-group">
            <label>Confirm New Password</label>
            <input 
              v-model="passwordForm.confirmPassword" 
              type="password" 
              required
              class="form-input"
            >
            <p v-if="passwordForm.confirmPassword && passwordForm.newPassword !== passwordForm.confirmPassword" class="error-text">
              Passwords do not match
            </p>
          </div>
          
          <div class="modal-actions">
            <button 
              type="submit" 
              :disabled="!isPasswordValid || passwordLoading"
              class="submit-btn"
            >
              {{ passwordLoading ? 'Updating...' : 'Update Password' }}
            </button>
            <button type="button" @click="closePasswordModal" class="cancel-btn">
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import authService from '../services/auth.js'
import { useNotification } from '../composables/useNotification.js'

export default {
  name: 'Profile',
  setup() {
    const { success, error: notifyError, warning } = useNotification()
    const user = ref(null)
    const loading = ref(true)
    const error = ref(null)
    const isEditing = ref(false)
    const editForm = ref({})
    const showPasswordModal = ref(false)
    const passwordLoading = ref(false)
    
    const passwordForm = ref({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })

    const loadProfile = async () => {
      try {
        loading.value = true
        error.value = null
        
        const response = await authService.getProfile()
        user.value = response.data
        editForm.value = { ...response.data }
      } catch (err) {
        console.error('Failed to load profile:', err)
        error.value = 'Failed to load profile information'
        
        // If token is invalid, redirect to login
        if (err.response?.status === 401) {
          authService.logout()
          this.$router.push('/login')
        }
      } finally {
        loading.value = false
      }
    }

    const handleImageUpload = async (event) => {
      const file = event.target.files[0]
      if (!file) return
      
      // Validate file type
      if (!file.type.startsWith('image/')) {
        warning('Please select an image file')
        return
      }
      
      // Validate file size (max 2MB)
      if (file.size > 2 * 1024 * 1024) {
        warning('Image size should be less than 2MB')
        return
      }
      
      try {
        const reader = new FileReader()
        reader.onload = async (e) => {
          const base64Image = e.target.result
          
          // Update user profile with new image
          const updatedUser = { ...user.value, profileImage: base64Image }
          const response = await authService.updateProfile(updatedUser)
          user.value = response.data
          
          // Emit event to update navbar
          document.dispatchEvent(new CustomEvent('profile-updated', {
            detail: response.data
          }))
          
          success('Profile image updated successfully!')
        }
        reader.readAsDataURL(file)
      } catch (err) {
        console.error('Failed to upload image:', err)
        notifyError('Failed to update profile image')
      }
    }

    const startEditing = () => {
      isEditing.value = true
      editForm.value = { ...user.value }
    }

    const cancelEditing = () => {
      isEditing.value = false
      editForm.value = { ...user.value }
    }

    const saveProfile = async () => {
      try {
        const response = await authService.updateProfile(editForm.value)
        user.value = response.data
        isEditing.value = false
        
        // Emit event to update navbar
        document.dispatchEvent(new CustomEvent('profile-updated', {
          detail: response.data
        }))
        
        success('Profile updated successfully!')
      } catch (err) {
        console.error('Failed to update profile:', err)
        notifyError('Failed to update profile')
      }
    }

    const passwordValidation = computed(() => {
      const password = passwordForm.value.newPassword
      return {
        minLength: password.length >= 8,
        hasLetter: /[a-zA-Z]/.test(password),
        hasNumber: /\d/.test(password),
        hasSpecial: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)
      }
    })

    const isPasswordValid = computed(() => {
      const validation = passwordValidation.value
      return validation.minLength && validation.hasLetter && validation.hasNumber && validation.hasSpecial &&
             passwordForm.value.newPassword === passwordForm.value.confirmPassword &&
             passwordForm.value.currentPassword.length > 0
    })

    const changePassword = async () => {
      try {
        passwordLoading.value = true
        await authService.changePassword({
          currentPassword: passwordForm.value.currentPassword,
          newPassword: passwordForm.value.newPassword
        })
        
        success('Password changed successfully!')
        closePasswordModal()
      } catch (err) {
        console.error('Failed to change password:', err)
        notifyError(err.error || err.message || 'Failed to change password')
      } finally {
        passwordLoading.value = false
      }
    }

    const closePasswordModal = () => {
      showPasswordModal.value = false
      passwordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
      }
    }

    const formatDate = (dateString) => {
      if (!dateString) return 'N/A'
      return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }

    const getInitials = () => {
      const name = user.value?.name || user.value?.displayName || user.value?.email
      if (!name) return 'U'
      
      const parts = name.split(' ')
      if (parts.length >= 2) {
        return (parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
      }
      return name.charAt(0).toUpperCase()
    }

    const handleImageError = (event) => {
      // If profile image fails to load, remove it and show avatar instead
      if (user.value) {
        user.value.profileImage = null
      }
    }

    const getDefaultAvatar = () => {
      // Return a default avatar image - using a simple SVG data URL
      return 'data:image/svg+xml;base64,' + btoa(`
        <svg width="150" height="150" viewBox="0 0 150 150" xmlns="http://www.w3.org/2000/svg">
          <circle cx="75" cy="75" r="75" fill="#6366f1"/>
          <circle cx="75" cy="60" r="25" fill="white"/>
          <ellipse cx="75" cy="120" rx="45" ry="25" fill="white"/>
        </svg>
      `)
    }

    onMounted(() => {
      loadProfile()
    })

    return {
      user,
      loading,
      error,
      isEditing,
      editForm,
      showPasswordModal,
      passwordLoading,
      passwordForm,
      passwordValidation,
      isPasswordValid,
      loadProfile,
      handleImageUpload,
      startEditing,
      cancelEditing,
      saveProfile,
      changePassword,
      closePasswordModal,
      formatDate,
      getInitials,
      handleImageError,
      getDefaultAvatar
    }
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.profile-header {
  text-align: center;
  margin-bottom: 2rem;
}

.profile-header h1 {
  color: #1f2937;
  font-size: 2.5rem;
  font-weight: bold;
}

.loading, .error {
  text-align: center;
  padding: 2rem;
}

.error {
  color: #ef4444;
}

.retry-btn {
  background: #6366f1;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  cursor: pointer;
  margin-top: 1rem;
}

.retry-btn:hover {
  background: #4f46e5;
}

.profile-content {
  display: grid;
  grid-template-columns: 1fr 2fr;
  gap: 2rem;
  align-items: start;
}

.profile-picture-section {
  text-align: center;
}

.profile-picture {
  margin-bottom: 1rem;
  display: flex;
  justify-content: center;
}

.profile-image-container {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid #e5e7eb;
  transition: all 0.3s ease;
}

.profile-image-container:hover {
  border-color: #6366f1;
}

.profile-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.profile-avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 3rem;
  font-weight: bold;
  border: 4px solid #e5e7eb;
  transition: all 0.3s ease;
}

.profile-avatar:hover {
  border-color: #6366f1;
  transform: scale(1.05);
}

.change-picture-btn {
  background: #6366f1;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.375rem;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s;
}

.change-picture-btn:hover {
  background: #4f46e5;
  transform: translateY(-1px);
}

.profile-info {
  background: white;
  padding: 2rem;
  border-radius: 0.75rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.info-grid {
  display: grid;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.info-item {
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 1rem;
}

.info-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.info-item label {
  display: block;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5rem;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.editable-field p {
  color: #1f2937;
  font-size: 1rem;
  margin: 0;
}

.edit-input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.edit-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.profile-actions {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.edit-actions {
  display: flex;
  gap: 0.5rem;
  flex: 1;
}

.edit-btn, .password-btn, .save-btn, .cancel-btn {
  padding: 0.75rem 1rem;
  border: none;
  border-radius: 0.375rem;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s;
  font-size: 0.875rem;
}

.edit-btn, .save-btn {
  background: #6366f1;
  color: white;
  flex: 1;
}

.edit-btn:hover, .save-btn:hover {
  background: #4f46e5;
  transform: translateY(-1px);
}

.password-btn {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  flex: 1;
}

.password-btn:hover {
  background: #e5e7eb;
}

.cancel-btn {
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #d1d5db;
  flex: 1;
}

.cancel-btn:hover {
  background: #e5e7eb;
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
}

.modal-content {
  background: white;
  border-radius: 0.75rem;
  padding: 2rem;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 1rem;
}

.modal-header h2 {
  margin: 0;
  color: #1f2937;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #6b7280;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #374151;
}

.password-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.form-input {
  padding: 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 0.375rem;
  font-size: 1rem;
  transition: border-color 0.2s;
}

.form-input:focus {
  outline: none;
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.password-requirements {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 0.375rem;
  padding: 0.75rem;
  font-size: 0.75rem;
}

.password-requirements p {
  margin: 0.25rem 0;
  color: #ef4444;
  transition: color 0.2s;
}

.password-requirements p.valid {
  color: #10b981;
}

.error-text {
  color: #ef4444;
  font-size: 0.75rem;
  margin: 0;
}

.modal-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid #e5e7eb;
}

.submit-btn {
  background: #6366f1;
  color: white;
  border: none;
  padding: 0.75rem 1rem;
  border-radius: 0.375rem;
  cursor: pointer;
  font-weight: 500;
  flex: 1;
  transition: all 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: #4f46e5;
  transform: translateY(-1px);
}

.submit-btn:disabled {
  background: #d1d5db;
  cursor: not-allowed;
  transform: none;
}

@media (max-width: 768px) {
  .profile-container {
    padding: 1rem;
  }
  
  .profile-content {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .profile-actions {
    flex-direction: column;
  }
  
  .edit-actions {
    flex-direction: column;
  }
  
  .modal-content {
    padding: 1.5rem;
    margin: 1rem;
  }
  
  .modal-actions {
    flex-direction: column;
  }
}
</style>