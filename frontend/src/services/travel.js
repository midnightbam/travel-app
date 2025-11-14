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
  }
};