<template>
  <div class="login">
    <div class="form-container">
      <h2>Welcome Back</h2>
      <p class="subtitle">Sign in to your Travel Explorer account</p>
      
      <div v-if="redirectMessage" class="alert alert-info">
        {{ redirectMessage }}
      </div>
      
      <div v-if="error" class="alert alert-error">
        {{ error }}
      </div>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="email">Email Address *</label>
          <input
            id="email"
            v-model="credentials.email"
            type="email"
            required
            placeholder="your@example.com"
            @blur="validateEmail"
            :class="{ 'input-error': emailError }"
            autocomplete="email"
          />
          <span v-if="emailError" class="field-error">{{ emailError }}</span>
        </div>
        
        <div class="form-group">
          <label for="password">Password *</label>
          <input
            id="password"
            v-model="credentials.password"
            type="password"
            required
            placeholder="Your password"
            @blur="validatePassword"
            :class="{ 'input-error': passwordError }"
            autocomplete="current-password"
          />
          <span v-if="passwordError" class="field-error">{{ passwordError }}</span>
        </div>
        
        <button type="submit" class="btn btn-primary" :disabled="loading || !isFormValid">
          <span v-if="loading" class="btn-spinner"></span>
          {{ loading ? 'Signing in...' : 'Sign In' }}
        </button>
      </form>
      
      <div class="demo-credentials">
        <p><strong>Demo Account:</strong></p>
        <p>Email: demo@example.com</p>
        <p>Password: password</p>
      </div>
      
      <div class="form-footer">
        <p>Don't have an account? 
          <router-link to="/register">Create one here</router-link>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { authService } from '../services/auth.js'

export default {
  name: 'Login',
  data() {
    return {
      credentials: {
        email: '',
        password: ''
      },
      loading: false,
      error: null,
      redirectMessage: null,
      emailError: null,
      passwordError: null,
      attemptCount: 0
    }
  },
  computed: {
    isFormValid() {
      return this.credentials.email && 
             this.credentials.password && 
             !this.emailError && 
             !this.passwordError
    }
  },
  mounted() {
    // Show message if redirected from protected route
    if (this.$route.query.redirect) {
      this.redirectMessage = 'Please log in to continue.'
    }
    
    // Show welcome message for newly registered users
    if (this.$route.query.registered === 'true') {
      this.redirectMessage = 'Account created successfully! Please log in to continue.'
    }
    
    // Redirect if already authenticated
    if (authService.isAuthenticated()) {
      this.$router.push('/')
    }
  },
  methods: {
    validateEmail() {
      this.emailError = null
      const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      
      if (!this.credentials.email) {
        this.emailError = 'Email is required'
      } else if (!emailRegex.test(this.credentials.email)) {
        this.emailError = 'Please enter a valid email address'
      }
      
      return !this.emailError
    },
    
    validatePassword() {
      this.passwordError = null
      
      if (!this.credentials.password) {
        this.passwordError = 'Password is required'
      }
      
      return !this.passwordError
    },
    
    validateForm() {
      const emailValid = this.validateEmail()
      const passwordValid = this.validatePassword()
      return emailValid && passwordValid
    },
    
    async handleLogin() {
      // Clear previous errors
      this.error = null
      this.redirectMessage = null
      
      // Validate form
      if (!this.validateForm()) {
        this.error = 'Please enter your email and password'
        return
      }
      
      this.loading = true
      this.attemptCount++
      
      try {
        const response = await authService.login(this.credentials)
        
        // Show success message briefly
        this.redirectMessage = response.message || 'Login successful!'
        
        // Clear form for security
        this.credentials = {
          email: '',
          password: ''
        }
        
        // Redirect after brief delay to show success message
        setTimeout(() => {
          const redirectTo = this.$route.query.redirect || '/dashboard'
          this.$router.push(redirectTo)
        }, 500)
        
      } catch (error) {
        // Handle specific error messages
        if (error.error === 'Invalid credentials') {
          this.error = 'Invalid email or password. Please try again.'
        } else if (error.error === 'Email and password are required') {
          this.error = 'Please enter both email and password'
        } else {
          this.error = error.error || 'Login failed. Please try again.'
        }
        
        // Show demo credentials hint after 2 failed attempts
        if (this.attemptCount >= 2 && !this.credentials.email.includes('demo')) {
          this.error += ' (Try the demo account below)'
        }
        
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login {
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

.alert-info {
  background: #bee3f8;
  color: #2c5282;
  border-left: 4px solid #3182ce;
  padding: 1rem 1.5rem;
  border-radius: 8px;
  margin-bottom: 1.5rem;
  font-weight: 500;
}

.demo-credentials {
  margin: 1.5rem 0;
  padding: 1rem;
  background: #f7fafc;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  text-align: center;
}

.demo-credentials p {
  margin: 0.25rem 0;
  color: #4a5568;
}

.demo-credentials p:first-child {
  color: #2d3748;
  font-weight: 600;
  margin-bottom: 0.5rem;
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
  opacity: 0.6;
}

.btn:disabled:hover {
  background: #a0aec0;
  transform: none;
}

/* Field validation styles */
.input-error {
  border-color: #e53e3e !important;
  background-color: #fff5f5 !important;
}

.input-error:focus {
  box-shadow: 0 0 0 3px rgba(229, 62, 62, 0.1) !important;
}

.field-error {
  display: block;
  margin-top: 0.5rem;
  color: #e53e3e;
  font-size: 0.875rem;
  font-weight: 500;
}

/* Button loading spinner */
.btn-spinner {
  display: inline-block;
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  margin-right: 0.5rem;
  animation: spin 0.6s linear infinite;
  vertical-align: middle;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Enhanced mobile responsiveness */
@media (max-width: 768px) {
  .login {
    padding: 1rem;
  }
  
  .form-container {
    max-width: 100%;
    padding: 1.5rem;
  }
  
  h2 {
    font-size: 1.5rem;
  }
  
  .subtitle {
    font-size: 0.9rem;
  }
  
  .demo-credentials {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .login {
    padding: 0.5rem;
  }
  
  .form-container {
    padding: 1rem;
  }
  
  h2 {
    font-size: 1.25rem;
  }
  
  .btn {
    padding: 0.875rem;
  }
}

/* Focus states for better accessibility */
input:focus {
  outline: none;
  border-color: #3182ce;
  box-shadow: 0 0 0 3px rgba(49, 130, 206, 0.1);
}

/* Auto-fill styling */
input:-webkit-autofill,
input:-webkit-autofill:hover,
input:-webkit-autofill:focus {
  -webkit-box-shadow: 0 0 0 30px white inset !important;
  -webkit-text-fill-color: #2d3748 !important;
}
</style>