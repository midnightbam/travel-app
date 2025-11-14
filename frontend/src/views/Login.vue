<template>
  <div class="login">
    <div class="form-container">
      <h2>Welcome Back</h2>
      <p class="subtitle">Sign in to your Travel Explorer account</p>
      
      <div v-if="error" class="alert alert-error">
        {{ error }}
      </div>
      
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label for="email">Email Address</label>
          <input
            id="email"
            v-model="credentials.email"
            type="email"
            required
            placeholder="your@example.com"
          />
        </div>
        
        <div class="form-group">
          <label for="password">Password</label>
          <input
            id="password"
            v-model="credentials.password"
            type="password"
            required
            placeholder="Your password"
          />
        </div>
        
        <button type="submit" class="btn btn-primary" :disabled="loading">
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
      error: null
    }
  },
  mounted() {
    // Redirect if already authenticated
    if (authService.isAuthenticated()) {
      this.$router.push('/')
    }
  },
  methods: {
    async handleLogin() {
      this.loading = true
      this.error = null
      
      try {
        await authService.login(this.credentials)
        this.$router.push('/')
      } catch (error) {
        this.error = error.error || 'Login failed'
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
}

.btn:disabled:hover {
  background: #a0aec0;
}
</style>