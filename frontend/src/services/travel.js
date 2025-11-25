import api from './api.js';

export const travelService = {
  async getTrips(query = null, page = 0, size = 10) {
    try {
      const params = new URLSearchParams();
      if (query) params.append('query', query);
      if (page !== 0) params.append('page', page);
      if (size !== 10) params.append('size', size);
      
      const queryString = params.toString();
      const url = queryString ? `/trips?${queryString}` : '/trips';
      
      const response = await api.get(url);
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: 'Failed to fetch trips' };
    }
  },

  async getTrip(id) {
    try {
      const response = await api.get(`/trips/${id}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: 'Failed to fetch trip' };
    }
  },

  async searchTrips(query) {
    try {
      const response = await api.get(`/trips?query=${encodeURIComponent(query)}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: 'Failed to search trips' };
    }
  },

  async getMyTrips() {
    try {
      const response = await api.get('/trips/mine');
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: 'Failed to fetch your trips' };
    }
  },

  async createTrip(tripData) {
    try {
      const response = await api.post('/trips', tripData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: 'Failed to create trip' };
    }
  },

  async updateTrip(id, tripData) {
    try {
      const response = await api.put(`/trips/${id}`, tripData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: 'Failed to update trip' };
    }
  },

  async deleteTrip(id) {
    try {
      const response = await api.delete(`/trips/${id}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { error: 'Failed to delete trip' };
    }
  }
};