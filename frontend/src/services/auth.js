import api from './api.js';

const authService = {
  async login(credentials) {
    try {
      const response = await api.post('/auth/login', credentials);
      const { token, user } = response.data;
      
      if (token) {
        localStorage.setItem('auth_token', token);
        localStorage.setItem('user_info', JSON.stringify(user));
      }
      
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: 'Login failed' };
    }
  },

  async register(userData) {
    try {
      const response = await api.post('/auth/register', userData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: 'Registration failed' };
    }
  },

  async getCurrentUser() {
    try {
      const response = await api.get('/auth/me');
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: 'Failed to get user info' };
    }
  },

  logout() {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('user_info');
    window.location.href = '/login';
  },

  isAuthenticated() {
    return !!localStorage.getItem('auth_token');
  },

  getUser() {
    const userInfo = localStorage.getItem('user_info');
    return userInfo ? JSON.parse(userInfo) : null;
  },

  setUser(userData) {
    localStorage.setItem('user_info', JSON.stringify(userData));
  },

  async getProfile() {
    try {
      const response = await api.get('/auth/me');
      return response;
    } catch (error) {
      throw error.response?.data || { error: 'Failed to get profile' };
    }
  },

  async updateProfile(profileData) {
    try {
      const response = await api.put('/auth/me', profileData);
      
      if (response.data) {
        localStorage.setItem('user_info', JSON.stringify(response.data));
      }
      
      return response;
    } catch (error) {
      throw error.response?.data || { error: 'Failed to update profile' };
    }
  },

  async changePassword(passwordData) {
    try {
      const response = await api.post('/auth/change-password', passwordData);
      return response;
    } catch (error) {
      throw error.response?.data || { error: 'Failed to change password' };
    }
  }
};

export default authService;