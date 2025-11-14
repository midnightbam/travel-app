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
import { authService } from '../services/auth.js'

export default {
  name: 'Register',
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
    async register() {
      this.error = null
      this.success = null
      
      // Validation
      if (this.form.password !== this.form.confirmPassword) {
        this.error = 'Passwords do not match'
        return
      }
      
      if (this.form.password.length < 6) {
        this.error = 'Password must be at least 6 characters long'
        return
      }
      
      this.loading = true
      
      try {
        await authService.register({
          email: this.form.email,
          password: this.form.password,
          displayName: this.form.displayName
        })
        
        this.success = 'Account created successfully! Please login to continue.'
        
        // Clear form
        this.form = {
          email: '',
          displayName: '',
          password: '',
          confirmPassword: ''
        }
        
        // Redirect to login after 2 seconds
        setTimeout(() => {
          this.$router.push('/login')
        }, 2000)
        
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
</style>