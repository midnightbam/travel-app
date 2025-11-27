<template>
  <div class="register">
    <div class="form-container">
      <h2>Create Account</h2>
      <p class="subtitle">Join Travel Explorer to share your amazing journeys</p>
      
      <div v-if="error" class="alert alert-error">
        {{ error }}
      </div>
      
      <div v-if="success" class="alert alert-success">
        {{ success }}
      </div>
      
      <form @submit.prevent="register">
        <div class="form-group">
          <label for="email">Email Address</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            required
            placeholder="your@email.com"
          />
        </div>
        
        <div class="form-group">
          <label for="displayName">Display Name</label>
          <input
            id="displayName"
            v-model="form.displayName"
            type="text"
            required
            placeholder="Your display name"
          />
        </div>
        
        <div class="form-group">
          <label for="password">Password</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            required
            placeholder="Create a strong password"
          />
          <div class="password-requirements">
            <p>Password must contain:</p>
            <ul>
              <li>At least 8 characters</li>
              <li>At least one letter (a-z, A-Z)</li>
              <li>At least one number (0-9)</li>
              <li>At least one special character (!@#$%^&*)</li>
            </ul>
          </div>
        </div>
        
        <div class="form-group">
          <label for="confirmPassword">Confirm Password</label>
          <input
            id="confirmPassword"
            v-model="form.confirmPassword"
            type="password"
            required
            placeholder="Confirm your password"
          />
        </div>
        
        <button type="submit" class="btn btn-primary" :disabled="loading">
          {{ loading ? 'Creating Account...' : 'Create Account' }}
        </button>
      </form>
      
      <div class="form-footer">
        <p>Already have an account? 
          <router-link to="/login">Sign in here</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import authService from '../services/auth.js'
import { useNotification } from '../composables/useNotification.js'

export default {
  name: 'Register',
  setup() {
    const { success, error: notifyError } = useNotification()
    return { notifySuccess: success, notifyError }
  },
  data() {
    return {
      form: {
        email: '',
        displayName: '',
        password: '',
        confirmPassword: ''
      },
      loading: false,
      error: null,
      success: null
    }
  },
  methods: {
    validatePassword(password) {
      // Minimum 8 characters
      if (password.length < 8) {
        return 'Password must be at least 8 characters long'
      }
      
      // Must contain at least one letter (a-z or A-Z)
      if (!/[a-zA-Z]/.test(password)) {
        return 'Password must contain at least one letter'
      }
      
      // Must contain at least one number (0-9)
      if (!/\d/.test(password)) {
        return 'Password must contain at least one number'
      }
      
      // Must contain at least one special character
      if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) {
        return 'Password must contain at least one special character (!@#$%^&*()_+-=[]{}|;:,.<>?)'
      }
      
      return null // Valid password
    },

    async register() {
      this.error = null
      this.success = null
      
      // Validation
      if (this.form.password !== this.form.confirmPassword) {
        this.error = 'Passwords do not match'
        return
      }
      
      // Enhanced password validation
      const passwordError = this.validatePassword(this.form.password)
      if (passwordError) {
        this.error = passwordError
        return
      }
      
      this.loading = true
      
      try {
        // Register the user
        const registerResponse = await authService.register({
          email: this.form.email,
          password: this.form.password,
          displayName: this.form.displayName
        })
        
        // Auto-login after successful registration
        try {
          await authService.login({
            email: this.form.email,
            password: this.form.password
          })
          
          this.notifySuccess('Welcome! Account created successfully')
          
          // Clear form
          this.form = {
            email: '',
            displayName: '',
            password: '',
            confirmPassword: ''
          }
          
          // Redirect to homepage after 1.5 seconds
          setTimeout(() => {
            this.$router.push('/')
          }, 1500)
          
        } catch (loginError) {
          // If auto-login fails, still show success but redirect to login
          this.notifySuccess('Account created successfully! Please login to continue.')
          setTimeout(() => {
            this.$router.push('/login')
          }, 2000)
        }
        
      } catch (error) {
        this.error = error.error || 'Registration failed. Please try again.'
        console.error('Registration error:', error)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.register {
  min-height: calc(100vh - 80px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.form-container {
  width: 100%;
  max-width: 400px;
}

h2 {
  text-align: center;
  margin-bottom: 0.5rem;
  color: #2d3748;
  font-size: 1.875rem;
  font-weight: 700;
}

.subtitle {
  text-align: center;
  margin-bottom: 2rem;
  color: #718096;
}

.form-footer {
  margin-top: 1.5rem;
  text-align: center;
  color: #718096;
}

.form-footer a {
  color: #e53e3e;
  text-decoration: none;
  font-weight: 500;
}

.form-footer a:hover {
  text-decoration: underline;
}

.btn:disabled {
  background: #a0aec0;
  cursor: not-allowed;
}

.btn:disabled:hover {
  background: #a0aec0;
}

.btn-primary {
  display: inline-block;
  margin: 0 auto;
  padding: 0.875rem 3rem;
  border-radius: 12px;
}

form {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.form-group {
  width: 100%;
}

.password-requirements {
  margin-top: 0.5rem;
  padding: 0.75rem;
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 0.875rem;
}

.password-requirements p {
  margin: 0 0 0.5rem 0;
  font-weight: 500;
  color: #4a5568;
}

.password-requirements ul {
  margin: 0;
  padding-left: 1.25rem;
  color: #718096;
}

.password-requirements li {
  margin-bottom: 0.25rem;
}
</style>